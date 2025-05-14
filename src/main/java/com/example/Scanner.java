package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

import static com.example.TokenType.*; 

//for the first time
<<<<<<< HEAD
//during each of your turns
//keywords that dont have reminder text but describe the effect
=======
//costs

>>>>>>> a24d680c720a6dda2b8b38aa0f8e271b30b90a1c
class Scanner {

    private final List<String> source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private String fulltext = "";
    private int line = 1;

    private static final Map<String, TokenType> keywords;
    private static final HashSet<String> shorthand_keywords;
    private static final HashSet<String> casting_keywords;
    private static final HashSet<String> supertypes;
    private static final HashSet<String> colors;

    static {
        shorthand_keywords = new HashSet<String>();
        shorthand_keywords.add("vigilance");
        shorthand_keywords.add("menace");
        shorthand_keywords.add("trample");
        shorthand_keywords.add("lifeline");
        shorthand_keywords.add("reach");
        shorthand_keywords.add("ward");
        shorthand_keywords.add("flash");
        shorthand_keywords.add("hexproof");
        shorthand_keywords.add("indestructible");
        shorthand_keywords.add("flying");
        shorthand_keywords.add("defender");
    }

    static {
        casting_keywords = new HashSet<String>();
        casting_keywords.add("kicker");
    }

    static {
        colors = new HashSet<>();
        colors.add("white");
        colors.add("blue");
        colors.add("black");
        colors.add("red");
        colors.add("green");
        colors.add("colorless");
        colors.add("multicolored");
    }

    static {
        supertypes = new HashSet<>();
        supertypes.add("creature");
        supertypes.add("land");
        supertypes.add("artifact");
        supertypes.add("enchantment");
        supertypes.add("instant");
        supertypes.add("sorcery");
        supertypes.add("planeswalker");
        supertypes.add("token");
        supertypes.add("permanent");
        supertypes.add("spell");
    }

    //types extraction into a subtypes array

    //token types?

    static {
        keywords = new HashMap<>();
        //punctuation
        keywords.put(",",           COMMA);
        keywords.put(".",           PERIOD);

        //logical grouping/connection
        keywords.put("and",         AND);
        keywords.put("or",          OR);
        keywords.put("to",          TO);

        //actions/events
        keywords.put("pay",         PAY);
        keywords.put("put",         PUT);
        keywords.put("create",      CREATE);  
        keywords.put("enter",       ENTERS);
        keywords.put("enters",      ENTERS);
        keywords.put("get",         GET);
        keywords.put("gets",        GET);
        keywords.put("attack",      ATTACK);
        keywords.put("attacks",     ATTACK);
        keywords.put("attach",      ATTACH);
        keywords.put("gain",        GAIN);
        keywords.put("gains",       GAIN);
        keywords.put("has",         HAS);
        keywords.put("have",        HAS);
        keywords.put("cost",        COST);
        keywords.put("costs",       COST);
        keywords.put("prevent",     PREVENT);
<<<<<<< HEAD
        keywords.put("exile",       EXILE);
        keywords.put("sacrifice",   SACRIFICE);
        keywords.put("destroy",      DESTROY);
        keywords.put("draw",        DRAW);
=======
        keywords.put("deal",        DEAL);
        keywords.put("dealt",       DEAL);
        keywords.put("be",          BE);
>>>>>>> a24d680c720a6dda2b8b38aa0f8e271b30b90a1c
        
        //targeting distinctions
        keywords.put("other",       OTHER);
        keywords.put("you",         YOU);
        keywords.put("another",     ANOTHER);
        keywords.put("non",         NON);
        keywords.put("target",      TARGET);
        keywords.put("it",          IT);
        keywords.put("that",        THAT);
        keywords.put("each",        EACH);
        keywords.put("all",         ALL);

        //game concepts
        keywords.put("control",     CONTROL);
        keywords.put("cardname",    CARDNAME);
        keywords.put("spellname",   SPELLNAME);
        keywords.put("life",        LIFE);
        keywords.put("counter",     COUNTER);
        keywords.put("end",         END);
        keywords.put("turn",        TURN);
        keywords.put("turns",       TURN);
        keywords.put("combat",      COMBAT);
        keywords.put("damage",      DAMAGE);

        //triggers
        keywords.put("whenever",    WHENEVER);
        keywords.put("when",        WHEN);
        keywords.put("would",       WOULD);
        keywords.put("if",          IF);
        keywords.put("was",         WAS);
        keywords.put("instead",     INSTEAD);

        //timing
        keywords.put("during",      DURING);
        keywords.put("until",       UNTIL);
        
        //quantifiers
        keywords.put("more",        MORE);
        keywords.put("less",        LESS);
        
    }

    Scanner(List<String> source) {
        this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }
        tokens.add(new Token(LINE_END, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= source.size();
    }

    private String advance() {
        return source.get(current++);
    }
    
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    
    private void addToken(TokenType type, Object literal) {
        String text = fulltext;
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(String expected) {
        if (isAtEnd()) return false;
        if (source.get(current) != expected) return false;
    
        current++;
        return true;
    }

    private void scanToken() {
        String t = advance();
        //regex for statblock
        //regex for statchange
        //regex for plural supertypes?
        switch(t) {
            case "first":
                if (match("strike")) {
                    addToken(KEYWORD, "First Strike");
                }
                break;
            case "a": addToken(QUANTITY, 1);
        }

    }

}

