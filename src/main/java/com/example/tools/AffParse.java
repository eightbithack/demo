package com.example.tools;

import com.example.Token;
import com.example.TokenType;
import com.example.AST.Expr;
import java.util.*;

public class AffParse {
    private final List<Token> tokens;
    private int current = 0;
    private int recursionDepth = 0;
    private static final int MAX_RECURSION_DEPTH = 50;
    private boolean debugMode = true;
    
    public AffParse(List<Token> tokens) {
        this.tokens = tokens;
    }
    
    public AffParse(List<Token> tokens, boolean debugMode) {
        this.tokens = tokens;
        this.debugMode = debugMode;
    }
    
    private void debugPrint(String method, String message) {
        if (debugMode) {
            System.out.println("[DEBUG " + method + " @depth=" + recursionDepth + " @pos=" + current + "] " + message);
        }
    }
    
    private void debugPrint(String method, String message, Token token) {
        if (debugMode) {
            System.out.println("[DEBUG " + method + " @depth=" + recursionDepth + " @pos=" + current + "] " + message + " Token: " + token.type + "(" + token.literal + ")");
        }
    }
    
    public Expr parse() {
        try {
            return parseAbility();
        } catch (ParseError error) {
            return null;
        }
    }
    
    private Expr parseAbility() {
        debugPrint("parseAbility", "Starting parseAbility, current token: " + (isAtEnd() ? "EOF" : peek().type));
        
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            debugPrint("parseAbility", "RECURSION LIMIT EXCEEDED! Depth: " + recursionDepth);
            throw new RuntimeException("Maximum recursion depth exceeded in parseAbility");
        }
        
        recursionDepth++;
        try {
            if (match(TokenType.KEYWORD)) {
                debugPrint("parseAbility", "Matched KEYWORD, parsing keyword ability");
                return parseKeywordAbility();
            } else if (match(TokenType.WHEN, TokenType.WHENEVER)) {
                debugPrint("parseAbility", "Matched WHEN/WHENEVER, parsing triggered ability");
                return parseTriggeredAbility();
            } else if (match(TokenType.COST_VALUE)) {
                debugPrint("parseAbility", "Matched COST_VALUE, parsing activated ability");
                return parseActivatedAbility();
            } else if (match(TokenType.AT)) {
                debugPrint("parseAbility", "Matched AT, parsing static ability");
                return parseStaticAbility();
            } else if (match(TokenType.SAGA_COUNT)) {
                debugPrint("parseAbility", "Matched SAGA_COUNT, parsing saga chapter");
                return parseSagaChapter();
            } else if (match(TokenType.NON_BASIC) || match(TokenType.UNRECOGNIZED) || match(TokenType.MODIFIED) || match(TokenType.OTHER)) {
                debugPrint("parseAbility", "Matched static ability token, parsing static ability");
                return parseStaticAbility();
            } else {
                debugPrint("parseAbility", "No specific ability type matched, parsing single token");
                // Don't call parseSequence here to avoid infinite recursion
                // Instead, try to parse individual tokens
                return parseSingleToken();
            }
        } finally {
            recursionDepth--;
            debugPrint("parseAbility", "Finished parseAbility, depth now: " + recursionDepth);
        }
    }
    
    private Expr parseKeywordAbility() {
        Token keyword = previous(); // Get the token that was already consumed by match()
        String keywordName = keyword.literal.toString();
        
        Expr modifier = null;
        if (match(TokenType.FOR)) {
            modifier = parseType();
        } else if (match(TokenType.COST_VALUE)) {
            modifier = parseCost();
        } else if (match(TokenType.QUANTITY)) {
            modifier = parseLiteral();
        }
        
        // Skip to LINE_END
        while (!isAtEnd() && !check(TokenType.LINE_END)) {
            advance();
        }
        if (!isAtEnd()) {
            advance(); // consume LINE_END
        }
        return new Expr.KeywordAbility(keywordName, modifier);
    }
    
    private Expr parseTriggeredAbility() {
        debugPrint("parseTriggeredAbility", "Starting parseTriggeredAbility, current token: " + peek().type);
        
        advance(); // consume WHEN or WHENEVER
        debugPrint("parseTriggeredAbility", "Consumed WHEN/WHENEVER, parsing trigger");
        
        Expr trigger = parseTrigger();
        debugPrint("parseTriggeredAbility", "Parsed trigger: " + (trigger != null ? trigger.getClass().getSimpleName() : "null"));
        
        // Try to consume comma, but don't fail if it's not there
        if (match(TokenType.COMMA)) {
            debugPrint("parseTriggeredAbility", "Matched comma");
        } else {
            debugPrint("parseTriggeredAbility", "No comma found, continuing");
        }
        
        Expr effect = parseEffect();
        debugPrint("parseTriggeredAbility", "Parsed effect: " + (effect != null ? effect.getClass().getSimpleName() : "null"));
        
        // Try to consume period, but don't fail if it's not there
        if (match(TokenType.PERIOD)) {
            debugPrint("parseTriggeredAbility", "Matched period");
        } else {
            debugPrint("parseTriggeredAbility", "No period found, continuing");
        }
        
        // Try to consume LINE_END, but don't fail if it's not there
        if (match(TokenType.LINE_END)) {
            debugPrint("parseTriggeredAbility", "Matched LINE_END");
        } else {
            debugPrint("parseTriggeredAbility", "No LINE_END found, continuing");
        }
        
        debugPrint("parseTriggeredAbility", "Successfully created TriggeredAbility");
        return new Expr.TriggeredAbility(trigger, effect);
    }
    
    private Expr parseActivatedAbility() {
        Expr cost = parseCost();
        
        // Try to consume COLON
        if (match(TokenType.COLON)) {
            debugPrint("parseActivatedAbility", "Matched COLON");
        } else {
            debugPrint("parseActivatedAbility", "No COLON found, continuing");
        }
        
        Expr effect = parseEffect();
        
        // Try to consume PERIOD
        if (match(TokenType.PERIOD)) {
            debugPrint("parseActivatedAbility", "Matched PERIOD");
        } else {
            debugPrint("parseActivatedAbility", "No PERIOD found, continuing");
        }
        
        // Try to consume LINE_END
        if (match(TokenType.LINE_END)) {
            debugPrint("parseActivatedAbility", "Matched LINE_END");
        } else {
            debugPrint("parseActivatedAbility", "No LINE_END found, continuing");
        }
        
        return new Expr.ActivatedAbility(cost, effect);
    }
    
    private Expr parseStaticAbility() {
        Expr condition = null;
        
        // Check if this starts with "AT THE BEGINNING OF"
        if (check(TokenType.AT)) {
            advance(); // consume AT
            advance(); // consume THE
            advance(); // consume BEGINNING
            advance(); // consume OF
            condition = parseTimeTrigger();
        } else {
            // For other static abilities, just parse the effect directly
            debugPrint("parseStaticAbility", "Non-time-based static ability");
        }
        
        // Try to consume COMMA
        if (match(TokenType.COMMA)) {
            debugPrint("parseStaticAbility", "Matched COMMA");
        } else {
            debugPrint("parseStaticAbility", "No COMMA found, continuing");
        }
        
        Expr effect = parseEffect();
        
        // Try to consume PERIOD
        if (match(TokenType.PERIOD)) {
            debugPrint("parseStaticAbility", "Matched PERIOD");
        } else {
            debugPrint("parseStaticAbility", "No PERIOD found, continuing");
        }
        
        // Try to consume LINE_END
        if (match(TokenType.LINE_END)) {
            debugPrint("parseStaticAbility", "Matched LINE_END");
        } else {
            debugPrint("parseStaticAbility", "No LINE_END found, continuing");
        }
        
        return new Expr.StaticAbility(condition, effect);
    }
    
    private Expr parseSagaChapter() {
        Token chapterToken = advance();
        String chapterStr = chapterToken.literal.toString();
        int chapterNumber = "null".equals(chapterStr) ? 0 : Integer.parseInt(chapterStr);
        
        Expr effect = null;
        if (match(TokenType.DASH)) {
            advance(); // consume dash
            effect = parseEffect();
        }
        
        // Skip to PERIOD
        while (!isAtEnd() && !check(TokenType.PERIOD)) {
            advance();
        }
        if (!isAtEnd()) {
            advance(); // consume PERIOD
        }
        
        // Skip to LINE_END
        while (!isAtEnd() && !check(TokenType.LINE_END)) {
            advance();
        }
        if (!isAtEnd()) {
            advance(); // consume LINE_END
        }
        
        return new Expr.SagaChapter(chapterNumber, effect);
    }
    
    private Expr parseTrigger() {
        debugPrint("parseTrigger", "Starting parseTrigger, current token: " + peek().type);
        
        if (match(TokenType.THIS)) {
            debugPrint("parseTrigger", "Matched THIS, parsing type and event");
            advance();
            Expr type = parseType();
            Expr event = parseEvent();
            debugPrint("parseTrigger", "Created THIS sequence with type and event");
            return new Expr.Sequence(new Expr[]{new Expr.Literal("THIS"), type, event});
        } else if (match(TokenType.YOU)) {
            debugPrint("parseTrigger", "Matched YOU, parsing action");
            advance();
            Expr action = parseAction();
            debugPrint("parseTrigger", "Created YOU sequence with action");
            return new Expr.Sequence(new Expr[]{new Expr.Literal("YOU"), action});
        } else if (match(TokenType.AT)) {
            debugPrint("parseTrigger", "Matched AT, parsing time trigger");
            return parseTimeTrigger();
        } else {
            debugPrint("parseTrigger", "No specific trigger matched, parsing event");
            return parseEvent();
        }
    }
    
    private Expr parseTimeTrigger() {
        advance(); // consume AT
        advance(); // consume THE
        advance(); // consume BEGINNING
        advance(); // consume OF
        advance(); // consume YOUR/COMBAT/etc
        Expr phase = parsePhase();
        return new Expr.Sequence(new Expr[]{new Expr.Literal("AT_THE_BEGINNING_OF"), phase});
    }
    
    private Expr parsePhase() {
        if (match(TokenType.COMBAT)) {
            advance();
            return new Expr.Literal("COMBAT");
        } else if (match(TokenType.MAIN)) {
            advance();
            advance(); // consume PHASE
            return new Expr.Literal("MAIN_PHASE");
        } else if (match(TokenType.END)) {
            advance();
            advance(); // consume STEP
            return new Expr.Literal("END_STEP");
        }
        return new Expr.Literal("UNKNOWN_PHASE");
    }
    
    private Expr parseEvent() {
        if (match(TokenType.ENTERS)) {
            advance();
            return new Expr.Literal("ENTERS");
        } else if (match(TokenType.DIE)) {
            advance();
            return new Expr.Literal("DIE");
        } else if (match(TokenType.ATTACK)) {
            advance();
            return new Expr.Literal("ATTACK");
        } else if (match(TokenType.CAST)) {
            advance();
            return new Expr.Literal("CAST");
        } else if (match(TokenType.DEAL)) {
            advance();
            advance(); // consume COMBAT
            advance(); // consume DAMAGE
            return new Expr.Literal("DEAL_COMBAT_DAMAGE");
        }
        return new Expr.Literal("UNKNOWN_EVENT");
    }
    
    private Expr parseAction() {
        if (match(TokenType.CAST)) {
            advance();
            return new Expr.Literal("CAST");
        } else if (match(TokenType.DRAW)) {
            advance();
            return new Expr.Literal("DRAW");
        } else if (match(TokenType.DISCARD)) {
            advance();
            return new Expr.Literal("DISCARD");
        } else if (match(TokenType.GET)) {
            advance();
            return new Expr.Literal("GET");
        } else if (match(TokenType.PAY)) {
            advance();
            return new Expr.Literal("PAY");
        } else if (match(TokenType.CREATE)) {
            advance();
            return new Expr.Literal("CREATE");
        } else if (match(TokenType.PUT)) {
            advance();
            return new Expr.Literal("PUT");
        } else if (match(TokenType.SACRIFICE)) {
            advance();
            return new Expr.Literal("SACRIFICE");
        } else if (match(TokenType.EXILE)) {
            advance();
            return new Expr.Literal("EXILE");
        } else if (match(TokenType.DESTROY)) {
            advance();
            return new Expr.Literal("DESTROY");
        } else if (match(TokenType.TAP)) {
            advance();
            return new Expr.Literal("TAP");
        } else if (match(TokenType.UNTAP)) {
            advance();
            return new Expr.Literal("UNTAP");
        } else if (match(TokenType.GAIN)) {
            advance();
            return new Expr.Literal("GAIN");
        } else if (match(TokenType.LOSE)) {
            advance();
            return new Expr.Literal("LOSE");
        } else if (match(TokenType.DEAL)) {
            advance();
            return new Expr.Literal("DEAL");
        } else if (match(TokenType.RETURN)) {
            advance();
            return new Expr.Literal("RETURN");
        } else if (match(TokenType.SEARCH)) {
            advance();
            return new Expr.Literal("SEARCH");
        } else if (match(TokenType.REVEAL)) {
            advance();
            return new Expr.Literal("REVEAL");
        } else if (match(TokenType.CHOOSE)) {
            advance();
            return new Expr.Literal("CHOOSE");
        } else if (match(TokenType.KEYWORD)) {
            Token keyword = previous();
            return new Expr.Literal(keyword.literal.toString());
        } else if (match(TokenType.QUANTITY)) {
            Token quantity = previous();
            return new Expr.Literal(quantity.literal.toString());
        } else if (match(TokenType.CARD)) {
            advance();
            return new Expr.Literal("CARD");
        } else if (match(TokenType.POWER)) {
            advance();
            return new Expr.Literal("POWER");
        } else if (match(TokenType.TOUGHNESS)) {
            advance();
            return new Expr.Literal("TOUGHNESS");
        } else if (match(TokenType.MANA)) {
            advance();
            return new Expr.Literal("MANA");
        } else if (match(TokenType.VALUE)) {
            advance();
            return new Expr.Literal("VALUE");
        } else if (match(TokenType.DAMAGE)) {
            advance();
            return new Expr.Literal("DAMAGE");
        } else if (match(TokenType.COUNTER)) {
            advance();
            return new Expr.Literal("COUNTER");
        } else if (match(TokenType.LIBRARY)) {
            advance();
            return new Expr.Literal("LIBRARY");
        } else if (match(TokenType.HAND)) {
            advance();
            return new Expr.Literal("HAND");
        } else if (match(TokenType.GRAVEYARD)) {
            advance();
            return new Expr.Literal("GRAVEYARD");
        } else if (match(TokenType.BATTLEFIELD)) {
            advance();
            return new Expr.Literal("BATTLEFIELD");
        } else if (match(TokenType.OPPONENT)) {
            advance();
            return new Expr.Literal("OPPONENT");
        } else if (match(TokenType.CONTROL)) {
            advance();
            return new Expr.Literal("CONTROL");
        } else if (match(TokenType.OWNER)) {
            advance();
            return new Expr.Literal("OWNER");
        } else if (match(TokenType.THIS)) {
            advance();
            return new Expr.Literal("THIS");
        } else if (match(TokenType.IT)) {
            advance();
            return new Expr.Literal("IT");
        } else if (match(TokenType.THEM)) {
            advance();
            return new Expr.Literal("THEM");
        } else if (match(TokenType.YOU)) {
            advance();
            return new Expr.Literal("YOU");
        } else if (match(TokenType.THE)) {
            advance();
            return new Expr.Literal("THE");
        } else if (match(TokenType.OR)) {
            advance();
            return new Expr.Literal("OR");
        } else if (match(TokenType.AND)) {
            advance();
            return new Expr.Literal("AND");
        } else if (match(TokenType.WITH)) {
            advance();
            return new Expr.Literal("WITH");
        } else if (match(TokenType.FROM)) {
            advance();
            return new Expr.Literal("FROM");
        } else if (match(TokenType.TO)) {
            advance();
            return new Expr.Literal("TO");
        } else if (match(TokenType.ON)) {
            advance();
            return new Expr.Literal("ON");
        } else if (match(TokenType.AT)) {
            advance();
            return new Expr.Literal("AT");
        } else if (match(TokenType.OF)) {
            advance();
            return new Expr.Literal("OF");
        } else if (match(TokenType.FOR)) {
            advance();
            return new Expr.Literal("FOR");
        } else if (match(TokenType.AS)) {
            advance();
            return new Expr.Literal("AS");
        } else if (match(TokenType.UNTIL)) {
            advance();
            return new Expr.Literal("UNTIL");
        } else if (match(TokenType.WHEN)) {
            advance();
            return new Expr.Literal("WHEN");
        } else if (match(TokenType.WHENEVER)) {
            advance();
            return new Expr.Literal("WHENEVER");
        } else if (match(TokenType.IF)) {
            advance();
            return new Expr.Literal("IF");
        } else if (match(TokenType.UNLESS)) {
            advance();
            return new Expr.Literal("UNLESS");
        } else if (match(TokenType.INSTEAD)) {
            advance();
            return new Expr.Literal("INSTEAD");
        } else if (match(TokenType.RATHER)) {
            advance();
            return new Expr.Literal("RATHER");
        } else if (match(TokenType.THAN)) {
            advance();
            return new Expr.Literal("THAN");
        } else if (match(TokenType.EQUAL)) {
            advance();
            return new Expr.Literal("EQUAL");
        } else if (match(TokenType.LESS)) {
            advance();
            return new Expr.Literal("LESS");
        } else if (match(TokenType.MORE)) {
            advance();
            return new Expr.Literal("MORE");
        } else if (match(TokenType.PLUS)) {
            advance();
            return new Expr.Literal("PLUS");
        } else if (match(TokenType.MINUS)) {
            advance();
            return new Expr.Literal("MINUS");
        } else if (match(TokenType.TIMES)) {
            advance();
            return new Expr.Literal("TIMES");
        } else if (match(TokenType.DIVIDED)) {
            advance();
            return new Expr.Literal("DIVIDED");
        } else if (match(TokenType.AMONG)) {
            advance();
            return new Expr.Literal("AMONG");
        } else if (match(TokenType.AMOUNT)) {
            advance();
            return new Expr.Literal("AMOUNT");
        } else if (match(TokenType.NUMBER)) {
            advance();
            return new Expr.Literal("NUMBER");
        } else if (match(TokenType.OTHER)) {
            advance();
            return new Expr.Literal("OTHER");
        } else if (match(TokenType.ANOTHER)) {
            advance();
            return new Expr.Literal("ANOTHER");
        } else if (match(TokenType.EACH)) {
            advance();
            return new Expr.Literal("EACH");
        } else if (match(TokenType.ALL)) {
            advance();
            return new Expr.Literal("ALL");
        } else if (match(TokenType.ANY)) {
            advance();
            return new Expr.Literal("ANY");
        } else if (match(TokenType.NON_BASIC)) {
            advance();
            return new Expr.Literal("NON_BASIC");
        } else if (match(TokenType.UNRECOGNIZED)) {
            advance();
            return new Expr.Literal("UNRECOGNIZED");
        } else if (match(TokenType.MODIFIED)) {
            advance();
            return new Expr.Literal("MODIFIED");
        } else {
            // For any unrecognized token, advance and return a literal
            Token token = advance();
            return new Expr.Literal(token.literal.toString());
        }
    }
    
    private Expr parseEffect() {
        debugPrint("parseEffect", "Starting parseEffect, current token: " + (isAtEnd() ? "EOF" : peek().type));
        
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            debugPrint("parseEffect", "RECURSION LIMIT EXCEEDED! Depth: " + recursionDepth);
            throw new RuntimeException("Maximum recursion depth exceeded in parseEffect");
        }
        
        recursionDepth++;
        try {
            List<Expr> effects = new ArrayList<>();
            int effectCount = 0;
            
            while (!isAtEnd() && !check(TokenType.PERIOD) && !check(TokenType.LINE_END)) {
                effectCount++;
                if (effectCount > 50) {
                    debugPrint("parseEffect", "WARNING: Processing many effects (" + effectCount + "), possible infinite loop");
                }
                
                debugPrint("parseEffect", "Processing effect " + effectCount + ", current token: " + peek().type);
                
                if (match(TokenType.IF)) {
                    debugPrint("parseEffect", "Matched IF, parsing conditional effect");
                    effects.add(parseConditionalEffect());
                } else if (match(TokenType.THEN)) {
                    debugPrint("parseEffect", "Matched THEN, consuming and parsing action");
                    advance(); // consume THEN
                    effects.add(parseAction());
                } else {
                    debugPrint("parseEffect", "Parsing action");
                    Expr effect = parseAction();
                    effects.add(effect);
                    debugPrint("parseEffect", "Added effect: " + effect.getClass().getSimpleName());
                }
                
                if (match(TokenType.COMMA)) {
                    debugPrint("parseEffect", "Matched comma, consuming");
                    advance(); // consume comma
                }
            }
            
            debugPrint("parseEffect", "Finished processing effects, found " + effects.size() + " effects");
            return new Expr.Sequence(effects.toArray(new Expr[0]));
        } finally {
            recursionDepth--;
            debugPrint("parseEffect", "Finished parseEffect, depth now: " + recursionDepth);
        }
    }
    
    private Expr parseConditionalEffect() {
        advance(); // consume IF
        Expr condition = parseCondition();
        
        consume(TokenType.COMMA, "Expected comma after condition");
        Expr trueEffect = parseEffect();
        
        Expr falseEffect = null;
        if (match(TokenType.COMMA)) {
            advance(); // consume comma
            if (match(TokenType.OTHERWISE)) {
                advance(); // consume OTHERWISE
                consume(TokenType.COMMA, "Expected comma after OTHERWISE");
                falseEffect = parseEffect();
            }
        }
        
        return new Expr.ConditionalEffect(condition, trueEffect, falseEffect);
    }
    
    private Expr parseCondition() {
        if (match(TokenType.YOU)) {
            advance();
            Expr action = parseAction();
            return new Expr.Sequence(new Expr[]{new Expr.Literal("YOU"), action});
        } else if (match(TokenType.THIS)) {
            advance();
            Expr type = parseType();
            Expr event = parseEvent();
            return new Expr.Sequence(new Expr[]{new Expr.Literal("THIS"), type, event});
        }
        return new Expr.Literal("UNKNOWN_CONDITION");
    }
    
    private Expr parseCost() {
        Token costToken = previous(); // Get the token that was already consumed by match()
        String costValue = costToken.literal.toString();
        
        Expr alternative = null;
        if (match(TokenType.COMMA)) {
            alternative = parseAction();
        }
        
        return new Expr.Cost("MANA", costValue, alternative);
    }
    
    private Expr parseType() {
        String supertype = null;
        String subtype = null;
        String color = null;
        
        if (match(TokenType.SUPERTYPE)) {
            supertype = previous().literal.toString();
        }
        
        if (match(TokenType.SUBTYPE)) {
            subtype = previous().literal.toString();
        }
        
        if (match(TokenType.COLOR_SPEC)) {
            color = previous().literal.toString();
        }
        
        return new Expr.Type(supertype, subtype, color);
    }
    
    private Expr parseLiteral() {
        Token token = advance();
        return new Expr.Literal(token.literal);
    }
    
    private Expr parseSequence() {
        debugPrint("parseSequence", "Starting parseSequence, current token: " + (isAtEnd() ? "EOF" : peek().type));
        
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            debugPrint("parseSequence", "RECURSION LIMIT EXCEEDED! Depth: " + recursionDepth);
            throw new RuntimeException("Maximum recursion depth exceeded in parseSequence");
        }
        
        recursionDepth++;
        try {
            List<Expr> expressions = new ArrayList<>();
            int tokenCount = 0;
            
            while (!isAtEnd() && !check(TokenType.LINE_END)) {
                tokenCount++;
                if (tokenCount > 100) {
                    debugPrint("parseSequence", "WARNING: Processing many tokens (" + tokenCount + "), possible infinite loop");
                }
                
                debugPrint("parseSequence", "Processing token " + tokenCount + ": " + peek().type);
                Expr expr = parseSingleToken();
                if (expr != null) {
                    expressions.add(expr);
                    debugPrint("parseSequence", "Added expression: " + expr.getClass().getSimpleName());
                } else {
                    debugPrint("parseSequence", "parseSingleToken returned null, skipping");
                }
                
                if (match(TokenType.COMMA)) {
                    debugPrint("parseSequence", "Matched comma, consuming");
                    advance(); // consume comma
                }
            }
            
            debugPrint("parseSequence", "Finished processing tokens, found " + expressions.size() + " expressions");
            consume(TokenType.LINE_END, "Expected LINE_END after sequence");
            return new Expr.Sequence(expressions.toArray(new Expr[0]));
        } finally {
            recursionDepth--;
            debugPrint("parseSequence", "Finished parseSequence, depth now: " + recursionDepth);
        }
    }
    
    private Expr parseSingleToken() {
        if (isAtEnd()) {
            debugPrint("parseSingleToken", "At end of tokens, returning null");
            return null;
        }
        
        Token token = peek();
        debugPrint("parseSingleToken", "Processing token: " + token.type + "(" + token.literal + ")");
        
        // Handle different token types
        if (match(TokenType.QUANTITY)) {
            Token quantityToken = previous();
            debugPrint("parseSingleToken", "Matched QUANTITY, creating literal: " + quantityToken.literal);
            return new Expr.Literal(quantityToken.literal.toString());
        } else if (match(TokenType.SUPERTYPE)) {
            Token supertypeToken = previous();
            debugPrint("parseSingleToken", "Matched SUPERTYPE, creating type: " + supertypeToken.literal);
            return new Expr.Type(supertypeToken.literal.toString(), null, null);
        } else if (match(TokenType.SUBTYPE)) {
            Token subtypeToken = previous();
            debugPrint("parseSingleToken", "Matched SUBTYPE, creating type: " + subtypeToken.literal);
            return new Expr.Type(null, subtypeToken.literal.toString(), null);
        } else if (match(TokenType.COLOR_SPEC)) {
            Token colorToken = previous();
            debugPrint("parseSingleToken", "Matched COLOR_SPEC, creating type: " + colorToken.literal);
            return new Expr.Type(null, null, colorToken.literal.toString());
        } else if (match(TokenType.KEYWORD)) {
            Token keywordToken = previous();
            debugPrint("parseSingleToken", "Matched KEYWORD, creating literal: " + keywordToken.literal);
            return new Expr.Literal(keywordToken.literal.toString());
        } else {
            // Skip unrecognized tokens
            debugPrint("parseSingleToken", "Unrecognized token type, skipping: " + token.type);
            advance();
            return null;
        }
    }
    
    // Helper methods
    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }
    
    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }
    
    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private boolean isAtEnd() {
        return peek().type == TokenType.EOF;
    }
    
    private Token peek() {
        return tokens.get(current);
    }
    
    private Token previous() {
        return tokens.get(current - 1);
    }
    
    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
        
        throw error(peek(), message);
    }
    
    private ParseError error(Token token, String message) {
        return new ParseError(token, message);
    }
    
    private static class ParseError extends RuntimeException {
        @SuppressWarnings("unused")
        final Token token;
        @SuppressWarnings("unused")
        final String message;
        
        ParseError(Token token, String message) {
            this.token = token;
            this.message = message;
        }
    }
}
