package com.example.tools;

import com.example.Token;
import com.example.TokenType;
import java.util.*;

public class TokenParser {
    
    public static List<Token> parseTokenString(String tokenString) {
        List<Token> tokens = new ArrayList<>();
        String[] parts = tokenString.trim().split("\\s+");
        
        for (String part : parts) {
            if (part.isEmpty()) continue;
            
            Token token = parseToken(part);
            if (token != null) {
                tokens.add(token);
            }
        }
        
        // Add EOF token
        tokens.add(new Token(TokenType.EOF, "", null, 0));
        return tokens;
    }
    
    private static Token parseToken(String tokenStr) {
        if (tokenStr.contains("(") && tokenStr.contains(")")) {
            String[] parts = tokenStr.split("\\(");
            if (parts.length == 2) {
                String typeStr = parts[0];
                String valueStr = parts[1].substring(0, parts[1].length() - 1);
                
                TokenType type = getTokenType(typeStr);
                Object value = parseValue(valueStr);
                
                return new Token(type, tokenStr, value, 0);
            }
        }
        return null;
    }
    
    private static TokenType getTokenType(String typeStr) {
        switch (typeStr.toUpperCase()) {
            case "KEYWORD": return TokenType.KEYWORD;
            case "WHEN": return TokenType.WHEN;
            case "WHENEVER": return TokenType.WHENEVER;
            case "AT": return TokenType.AT;
            case "THE": return TokenType.THE;
            case "BEGINNING": return TokenType.BEGINNING;
            case "OF": return TokenType.OF;
            case "YOUR": return TokenType.YOUR;
            case "EACH": return TokenType.EACH;
            case "OPPONENT": return TokenType.OPPONENT;
            case "YOU": return TokenType.YOU;
            case "CANNOT": return TokenType.CANNOT;
            case "COMMA": return TokenType.COMMA;
            case "PERIOD": return TokenType.PERIOD;
            case "LINE_END": return TokenType.LINE_END;
            case "THIS": return TokenType.THIS;
            case "ENTERS": return TokenType.ENTERS;
            case "DIE": return TokenType.DIE;
            case "ATTACK": return TokenType.ATTACK;
            case "CAST": return TokenType.CAST;
            case "DRAW": return TokenType.DRAW;
            case "DISCARD": return TokenType.DISCARD;
            case "CARD": return TokenType.CARD;
            case "GET": return TokenType.GET;
            case "PAY": return TokenType.PAY;
            case "SACRIFICE": return TokenType.SACRIFICE;
            case "CREATE": return TokenType.CREATE;
            case "PUT": return TokenType.PUT;
            case "TARGET": return TokenType.TARGET;
            case "CHOOSE": return TokenType.CHOOSE;
            case "RETURN": return TokenType.RETURN;
            case "EXILE": return TokenType.EXILE;
            case "DESTROY": return TokenType.DESTROY;
            case "TAP": return TokenType.TAP;
            case "UNTAP": return TokenType.UNTAP;
            case "GAIN": return TokenType.GAIN;
            case "LOSE": return TokenType.LOSE;
            case "DEAL": return TokenType.DEAL;
            case "DAMAGE": return TokenType.DAMAGE;
            case "EQUAL": return TokenType.EQUAL;
            case "TO": return TokenType.TO;
            case "FROM": return TokenType.FROM;
            case "WITH": return TokenType.WITH;
            case "ON": return TokenType.ON;
            case "IT": return TokenType.IT;
            case "HAS": return TokenType.HAS;
            case "GETS": return TokenType.GETS;
            case "BECOME": return TokenType.BECOME;
            case "LEAVE": return TokenType.LEAVE;
            case "CONTROL": return TokenType.CONTROL;
            case "OWNER": return TokenType.OWNER;
            case "HAND": return TokenType.HAND;
            case "LIBRARY": return TokenType.LIBRARY;
            case "BATTLEFIELD": return TokenType.BATTLEFIELD;
            case "GRAVEYARD": return TokenType.GRAVEYARD;
            case "MANA": return TokenType.MANA;
            case "COST": return TokenType.COST;
            case "VALUE": return TokenType.VALUE;
            case "POWER": return TokenType.POWER;
            case "TOUGHNESS": return TokenType.TOUGHNESS;
            case "STAT_CHANGE": return TokenType.STAT_CHANGE;
            case "COUNTER": return TokenType.COUNTER;
            case "COUNTERS": return TokenType.COUNTERS;
            case "PLUS": return TokenType.PLUS;
            case "MINUS": return TokenType.MINUS;
            case "TIMES": return TokenType.TIMES;
            case "DIVIDED": return TokenType.DIVIDED;
            case "AMONG": return TokenType.AMONG;
            case "THEM": return TokenType.THEM;
            case "THOSE": return TokenType.THOSE;
            case "OTHER": return TokenType.OTHER;
            case "ANOTHER": return TokenType.ANOTHER;
            case "ALL": return TokenType.ALL;
            case "ANY": return TokenType.ANY;
            case "NUMBER": return TokenType.NUMBER;
            case "QUANTITY": return TokenType.QUANTITY;
            case "AMOUNT": return TokenType.AMOUNT;
            case "MUCH": return TokenType.MUCH;
            case "MANY": return TokenType.MANY;
            case "LESS": return TokenType.LESS;
            case "MORE": return TokenType.MORE;
            case "OR": return TokenType.OR;
            case "AND": return TokenType.AND;
            case "BUT": return TokenType.BUT;
            case "IF": return TokenType.IF;
            case "THEN": return TokenType.THEN;
            case "ELSE": return TokenType.ELSE;
            case "UNLESS": return TokenType.UNLESS;
            case "UNTIL": return TokenType.UNTIL;
            case "WHILE": return TokenType.WHILE;
            case "FOR": return TokenType.FOR;
            case "AS": return TokenType.AS;
            case "LONG": return TokenType.LONG;
            case "INSTEAD": return TokenType.INSTEAD;
            case "RATHER": return TokenType.RATHER;
            case "THAN": return TokenType.THAN;
            case "ONLY": return TokenType.ONLY;
            case "TWICE": return TokenType.TWICE;
            case "ONCE": return TokenType.ONCE;
            case "FIRST": return TokenType.FIRST;
            case "SECOND": return TokenType.SECOND;
            case "THIRD": return TokenType.THIRD;
            case "NEXT": return TokenType.NEXT;
            case "PREVIOUS": return TokenType.PREVIOUS;
            case "LAST": return TokenType.LAST;
            case "FINAL": return TokenType.FINAL;
            case "SUPERTYPE": return TokenType.SUPERTYPE;
            case "CREATURE": return TokenType.CREATURE;
            case "SPELL": return TokenType.SPELL;
            case "INSTANT": return TokenType.INSTANT;
            case "SORCERY": return TokenType.SORCERY;
            case "ENCHANTMENT": return TokenType.ENCHANTMENT;
            case "ARTIFACT": return TokenType.ARTIFACT;
            case "LAND": return TokenType.LAND;
            case "PLANESWALKER": return TokenType.PLANESWALKER;
            case "TOKEN": return TokenType.TOKEN;
            case "PERMANENT": return TokenType.PERMANENT;
            case "BASIC": return TokenType.BASIC;
            case "NON": return TokenType.NON_BASIC;
            case "SUBTYPE": return TokenType.SUBTYPE;
            case "COLOR_SPEC": return TokenType.COLOR_SPEC;
            case "WHITE": return TokenType.WHITE;
            case "BLUE": return TokenType.BLUE;
            case "BLACK": return TokenType.BLACK;
            case "RED": return TokenType.RED;
            case "GREEN": return TokenType.GREEN;
            case "COLORLESS": return TokenType.COLORLESS;
            case "MULTICOLORED": return TokenType.MULTICOLORED;
            case "COST_VALUE": return TokenType.COST_VALUE;
            case "STAT_BLOCK": return TokenType.STAT_BLOCK;
            case "SAGA_COUNT": return TokenType.SAGA_COUNT;
            case "X_VAR": return TokenType.X_VAR;
            case "ITERATION": return TokenType.ITERATION;
            case "UNRECOGNIZED": return TokenType.UNRECOGNIZED;
            case "QUOTE": return TokenType.QUOTE;
            case "DOT": return TokenType.DOT;
            case "DASH": return TokenType.DASH;
            case "COLON": return TokenType.COLON;
            case "SEMICOLON": return TokenType.SEMICOLON;
            case "PAREN_OPEN": return TokenType.PAREN_OPEN;
            case "PAREN_CLOSE": return TokenType.PAREN_CLOSE;
            case "BRACKET_OPEN": return TokenType.BRACKET_OPEN;
            case "BRACKET_CLOSE": return TokenType.BRACKET_CLOSE;
            case "BRACE_OPEN": return TokenType.BRACE_OPEN;
            case "BRACE_CLOSE": return TokenType.BRACE_CLOSE;
            case "SEARCH": return TokenType.SEARCH;
            case "REVEAL": return TokenType.REVEAL;
            case "SHUFFLE": return TokenType.SHUFFLE;
            case "MILL": return TokenType.MILL;
            case "SCRY": return TokenType.SCRY;
            case "INVESTIGATE": return TokenType.INVESTIGATE;
            case "MANIFEST": return TokenType.MANIFEST;
            case "POPULATE": return TokenType.POPULATE;
            case "PROLIFERATE": return TokenType.UNRECOGNIZED;
            case "COUNTER_SPELL": return TokenType.COUNTER_SPELL;
            case "COUNTER_ABILITY": return TokenType.COUNTER_ABILITY;
            case "ACTIVATE": return TokenType.ACTIVATE;
            case "TRIGGER": return TokenType.TRIGGER;
            case "RESOLVE": return TokenType.RESOLVE;
            case "STACK_ABILITY": return TokenType.STACK_ABILITY;
            case "STACK_SPELL": return TokenType.STACK_SPELL;
            case "COPY": return TokenType.COPY;
            case "CLONE": return TokenType.CLONE;
            case "MIRROR": return TokenType.MIRROR;
            case "UPKEEP": return TokenType.UPKEEP;
            case "DRAW_STEP": return TokenType.DRAW_STEP;
            case "MAIN_PHASE": return TokenType.MAIN_PHASE;
            case "COMBAT_PHASE": return TokenType.COMBAT_PHASE;
            case "DECLARE_ATTACKERS": return TokenType.DECLARE_ATTACKERS;
            case "DECLARE_BLOCKERS": return TokenType.DECLARE_BLOCKERS;
            case "COMBAT_DAMAGE": return TokenType.COMBAT_DAMAGE;
            case "END_STEP": return TokenType.END_STEP;
            case "CLEANUP": return TokenType.CLEANUP;
            case "MODIFIED": return TokenType.MODIFIED;
            case "ENCHANTED": return TokenType.ENCHANTED;
            case "EQUIPPED": return TokenType.UNRECOGNIZED;
            case "CREWED": return TokenType.CREWED;
            case "TAPPED": return TokenType.TAPPED;
            case "UNTAPPED": return TokenType.UNTAPPED;
            case "SUMMONING_SICK": return TokenType.SUMMONING_SICK;
            case "COMBAT": return TokenType.COMBAT;
            case "MAIN": return TokenType.MAIN;
            case "END": return TokenType.END;
            case "OTHERWISE": return TokenType.OTHERWISE;
            default: return TokenType.UNRECOGNIZED;
        }
    }
    
    private static Object parseValue(String valueStr) {
        if ("null".equals(valueStr)) {
            return "null"; // Keep as string instead of null
        } else if (valueStr.matches("\\d+")) {
            return Integer.parseInt(valueStr);
        } else if (valueStr.startsWith("{") && valueStr.endsWith("}")) {
            return valueStr; // Keep mana cost format as string
        } else {
            return valueStr;
        }
    }
}
