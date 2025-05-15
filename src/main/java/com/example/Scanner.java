package com.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashSet;

import static com.example.TokenType.*; 

//keywords that dont have reminder text but describe the effect
    //think the answer is to just ignore anything in parens - could strip it out during initial processing
//costs
    //think this is a regex - mana costs are contained within {}, so those can be matched to
//numerical descriptors (first/second, once/twice, etc)
    //this feels like it's solved with either a library or just a map of words to tokens
    //I think it has to map to tokens bc "twice" and "second" probably can't just map to 2? (can they?)
//counter types - especially for one-ofs like Drake Hatcher's incubation counters.
    //either all types need to be hardcoded, or we just keep a generic String object token that can then be contextually typed during parsing
//planeswalker loyalty abilities
//activated abilities (rune-sealed wall)
//modal spells
//granted abilities (in quotation marks)
//type-belonging conjugation (ie, "target player's graveyard", "target creature's mana cost", etc)
//dash character in lines like "Ward-Pay 7 Life"
    //replace the double dash character with a space?

class Scanner {

    private final List<String> source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private String fulltext = "";
    private int line = 1;

    private static final Map<String, TokenType> keywords;
    private static final Map<String, Integer> numbers;
    private static final HashSet<String> shorthand_keywords;
    private static final HashSet<String> supertypes;
    private static final HashSet<String> subtypes;
    private static final HashSet<String> colors;
    

    private static final Pattern stat_block = Pattern.compile("([0-9]+)\\/([0-9])");
    private static final Pattern stat_change = Pattern.compile("[\\+|\\-]([0-9]+)\\/[\\+|\\-]([0-9]+)");

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
        shorthand_keywords.add("prowess");
        shorthand_keywords.add("haste");
        shorthand_keywords.add("kicker");
        shorthand_keywords.add("flashback");
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
        supertypes.add("sorceries");
        supertypes.add("planeswalker");
        supertypes.add("token");
        supertypes.add("permanent");
        supertypes.add("spell");
        supertypes.add("legendary");
        supertypes.add("basic");
    }

    public static String[] loadArrayFromFile(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line.toLowerCase());
            }
        }
        return lines.toArray(new String[0]);
    }

    public static String[] getTypes() {
        String filePath = "src/main/java/com/example/CreatureTypes.txt"; 
        try {
            String[] stringArray = loadArrayFromFile(filePath);
            return stringArray;
        } catch (IOException e) {
            System.err.println("Error loading array from file: " + e.getMessage());
        }
        return null;
    }

    //types extraction into a subtypes array
    static {
        subtypes = new HashSet<>(Arrays.asList(getTypes()));
        subtypes.add("equipment");
        subtypes.add("aura");
        subtypes.add("vehicle");
    }

    static {
        numbers = new HashMap<>();
        numbers.put("one", 1);
        numbers.put("two", 2);
        numbers.put("three", 3);
        numbers.put("four", 4);
        numbers.put("five", 5);
        numbers.put("six", 6);
        numbers.put("seven", 7);
        numbers.put("eight", 8);
        numbers.put("nine", 9);
        numbers.put("ten", 10);
        numbers.put("eleven", 11);
        numbers.put("twelve", 12);
        numbers.put("thirteen", 13);
        numbers.put("fourteen", 14);
        numbers.put("fifteen", 15);
    }

    //token types?

    static {
        keywords = new HashMap<>();
        //punctuation
        keywords.put(",",           COMMA);
        keywords.put(".",           PERIOD);
        keywords.put("-",           DASH);

        //logical grouping/connection
        keywords.put("and",         AND);
        keywords.put("or",          OR);
        keywords.put("to",          TO);
        keywords.put("equal",       EQUAL);
        keywords.put("from",        FROM);
        keywords.put("then",        THEN);
        keywords.put("with",        WITH);

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
        keywords.put("attacked",    ATTACK);
        keywords.put("block",       BLOCK);
        keywords.put("blocks",      BLOCK);
        keywords.put("blocked",     BLOCK);
        keywords.put("attach",      ATTACH);
        keywords.put("gain",        GAIN);
        keywords.put("gains",       GAIN);
        keywords.put("has",         HAS);
        keywords.put("had",         HAS);
        keywords.put("have",        HAS);
        keywords.put("cost",        COST);
        keywords.put("costs",       COST);
        keywords.put("prevent",     PREVENT);
        keywords.put("exile",       EXILE);
        keywords.put("sacrifice",   SACRIFICE);
        keywords.put("destroy",     DESTROY);
        keywords.put("draw",        DRAW);
        keywords.put("deal",        DEAL);
        keywords.put("deals",       DEAL);
        keywords.put("dealt",       DEAL);
        keywords.put("be",          BE);
        keywords.put("can",         CAN);
        keywords.put("can't",       CANNOT);
        keywords.put("is",          IS);
        keywords.put("targets",     TARGETS);
        keywords.put("return",      RETURN);
        keywords.put("look",        LOOK);
        keywords.put("may",         MAY);
        keywords.put("reveal",      REVEAL);
        keywords.put("enchant",     ENCHANT);
        keywords.put("dies",        DIES);
        keywords.put("surveil",     SURVEIL);
        keywords.put("scry",        SCRY);
        keywords.put("remove",      REMOVE);
        keywords.put("tap",         TAP);
        keywords.put("copy",        COPY);
        keywords.put("discard",     DISCARD);
        keywords.put("mill",        MILL);
        keywords.put("choose",      CHOOSE);
        keywords.put("chooses",     CHOOSE);
        keywords.put("choice",      CHOOSE);
        
        //targeting distinctions
        keywords.put("other",       OTHER);
        keywords.put("you",         YOU);
        keywords.put("another",     ANOTHER);
        keywords.put("non",         NON);
        keywords.put("target",      TARGET);
        keywords.put("it",          IT);
        keywords.put("its",         IT);
        keywords.put("it's",        IT_IS);
        keywords.put("that",        THAT);
        keywords.put("each",        EACH);
        keywords.put("all",         ALL);
        keywords.put("on",          ON);
        keywords.put("this",        THIS);
        keywords.put("only",        ONLY);
        keywords.put("the",         THE);
        keywords.put("number",      NUMBER);
        keywords.put("named",       NAMED);
        keywords.put("your",        YOUR);
        keywords.put("opponent",    OPPONENT);
        keywords.put("opponents",   OPPONENT);
        keywords.put("attacking",   ATTACKING);
        keywords.put("blocking",    BLOCKING);
        keywords.put("among",       AMONG);
        keywords.put("them",        THEM);
        keywords.put("they",        THEY);
        keywords.put("enchanted",   ENCHANTED);
        keywords.put("equipped",    EQUIPPED);
        keywords.put("player",      PLAYER);
        keywords.put("different",   DIFFERENT);
        keywords.put("their",       THEIR);

        //game concepts
        keywords.put("control",     CONTROL);
        keywords.put("cardname",    CARDNAME);
        keywords.put("spellname",   SPELLNAME);
        keywords.put("life",        LIFE);
        keywords.put("counter",     COUNTER);
        keywords.put("counters",    COUNTER);
        keywords.put("countered",   COUNTER);
        keywords.put("end",         END);
        keywords.put("turn",        TURN);
        keywords.put("turns",       TURN);
        keywords.put("combat",      COMBAT);
        keywords.put("damage",      DAMAGE);
        keywords.put("card",        CARD);
        keywords.put("cards",       CARD);
        keywords.put("card's",      CARDS);
        keywords.put("ability",     ABILITY);
        keywords.put("triggers",    TRIGGERS);
        keywords.put("lose",        LOSE);
        keywords.put("game",        GAME);
        keywords.put("win",         WIN);
        keywords.put("graveyard",   GRAVEYARD);
        keywords.put("library",     LIBRARY);
        keywords.put("hand",        HAND);
        keywords.put("mana",        MANA);
        keywords.put("top",         TOP);
        keywords.put("bottom",      BOTTOM);
        keywords.put("rest",        REST);
        keywords.put("random",      RANDOM);
        keywords.put("order",       ORDER);
        keywords.put("kicked",      KICKED);
        keywords.put("type",        TYPE);
        keywords.put("types",       TYPE);
        keywords.put("deck",        DECK);
        keywords.put("owner",       OWNER);
        keywords.put("owner's",     OWNER);
        keywords.put("owners'",     OWNER);

        //triggers
        keywords.put("whenever",    WHENEVER);
        keywords.put("when",        WHEN);
        keywords.put("would",       WOULD);
        keywords.put("if",          IF);
        keywords.put("was",         WAS);
        keywords.put("instead",     INSTEAD);
        keywords.put("as",          AS);
        keywords.put("except",      EXCEPT);

        //timing
        keywords.put("during",      DURING);
        keywords.put("until",       UNTIL);
        
        //quantifiers
        keywords.put("more",        MORE);
        keywords.put("less",        LESS);
        keywords.put("up",          UP);
        keywords.put("value",       VALUE);
        keywords.put("many",        MANY);
        
        //annoying vestigial tokens
        keywords.put("long",        LONG);
        keywords.put("in",          IN);
        keywords.put("addition",    ADDITION);
        keywords.put("into",        INTO);
        keywords.put("under",       UNDER);
        keywords.put("a",           A);
        keywords.put("an",          A);
        keywords.put("there",       THERE);
        keywords.put("are",         ARE);
        keywords.put("those",       THOSE);
        keywords.put("time",        TIME);
        keywords.put("do",          DO);
        keywords.put("though",      THOUGH);
        keywords.put("that's",      THATS);
        keywords.put("of",          OF);
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
        //System.out.println("current: " + source.get(current) + ", expected: " + expected + ", check is " + (source.get(current) != expected));
        if (isAtEnd()) return false;
        if (!source.get(current).equals(expected)) return false;
    
        current++;
        return true;
    }

    //prescan rules
        //card names are removed
        //non's are split away

    private void scanToken() {
        String t = advance().toLowerCase();
        //regex for statblock
        //regex for statchange
        //regex for plural supertypes?
            //TYPEs
            //TYPE's
        switch(t) {
            case "first":
                if (match("strike")) {
                    addToken(KEYWORD, "first strike");
                }
                break;
            case "double":
                if (match("strike")) {
                    addToken(KEYWORD, "double strike");
                }
                break;
            default:
                TokenType s = keywords.get(t);
                if (s != null) {
                    addToken(s);
                } else if (shorthand_keywords.contains(t)) {
                    addToken(KEYWORD, t);
                } else if (supertypes.contains(t) || supertypes.contains(t.substring(0, t.length() - 1))) {
                    addToken(SUPERTYPE, t);
                } else if (subtypes.contains(t) || subtypes.contains(t.substring(0, t.length() - 1))) {
                    addToken(SUBTYPE, t);
                } else if (colors.contains(t)) {
                    addToken(COLOR, t);
                } else if (numbers.containsKey(t)) {
                    addToken(QUANTITY, numbers.get(t));
                } else if (t.matches("^(\\d+)$")) {
                    addToken(QUANTITY, Integer.parseInt(t));
                } else {
                    Matcher block_match = stat_block.matcher(t);
                    Matcher change_match = stat_change.matcher(t);
                    if (block_match.find()) {
                        String statArray = "" + block_match.group(1) + " " + block_match.group(2);
                        addToken(STAT_BLOCK, statArray);
                    }
                    else if (change_match.find()){
                        String statArray = "" + change_match.group(1) + " " + change_match.group(2);
                        addToken(STAT_CHANGE, statArray);
                    }
                    else {
                        addToken(UNRECOGNIZED, t);
                    }        
                }
        }

    }

}

