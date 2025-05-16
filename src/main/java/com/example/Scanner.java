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
    //need to decide when the cost object should be further processed?
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
    private static final Map<String, Integer> iterations;
    private static final HashSet<String> shorthand_keywords;
    private static final HashSet<String> supertypes;
    private static final HashSet<String> subtypes;
    private static final HashSet<String> colors;
    

    private static final Pattern stat_block = Pattern.compile("([0-9]+|x)\\/([0-9]+|x)");
    private static final Pattern stat_change = Pattern.compile("(\\+|\\-)([0-9]+|x)\\/(\\+|\\-)([0-9]+|x)");

    static {
        shorthand_keywords = new HashSet<String>();
        shorthand_keywords.add("vigilance");
        shorthand_keywords.add("menace");
        shorthand_keywords.add("trample");
        shorthand_keywords.add("lifelink");
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
        shorthand_keywords.add("deathtouch");
        shorthand_keywords.add("protection");
        shorthand_keywords.add("affinity");
        shorthand_keywords.add("changeling");
        shorthand_keywords.add("crew");
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
        subtypes.add("elves");
        subtypes.add("plains");
        subtypes.add("island");
        subtypes.add("swamp");
        subtypes.add("mountain");
        subtypes.add("forest");
        subtypes.add("gate");
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

    static {
        iterations = new HashMap<>();
        iterations.put("first", 1);
        iterations.put("second", 2);
        iterations.put("third", 3);
        iterations.put("fourth", 4);
        iterations.put("fifth", 5);
        iterations.put("sixth", 6);
        iterations.put("seventh", 7);
        iterations.put("eighth", 8);
        iterations.put("ninth", 9);
        iterations.put("tenth", 10);
        iterations.put("eleventh", 11);
        iterations.put("twelfth", 12);
        iterations.put("thirteenth", 13);
        iterations.put("fourteenth", 14);
        iterations.put("fifteenth", 15);
    }

    //token types?

    //clean up the subordering to group similar subcategories
    //fix the inconsistency about type pluralization - should be the singular

    static {
        keywords = new HashMap<>();
        //punctuation
        keywords.put(",",           COMMA);
        keywords.put(".",           PERIOD);
        keywords.put("-",           DASH);
        keywords.put(":",           COLON);
        keywords.put("\\u2022",     DOT);
        keywords.put("\"",          QUOTE);

        //logical grouping/connection
        keywords.put("and",         AND);
        keywords.put("or",          OR);
        keywords.put("to",          TO);
        keywords.put("equal",       EQUAL);
        keywords.put("from",        FROM);
        keywords.put("then",        THEN);
        keywords.put("with",        WITH);
        keywords.put("and/or",      AND_OR);
        keywords.put("unless",      UNLESS);
        keywords.put("still",       STILL);
        keywords.put("already",     ALREADY);
        keywords.put("otherwise",   OTHERWISE);

        //actions/events
        keywords.put("pay",         PAY);
        keywords.put("pays",        PAY);
        keywords.put("paid",        PAY);
        keywords.put("paying",      PAY);
        keywords.put("put",         PUT);
        keywords.put("puts",        PUT);
        keywords.put("create",      CREATE);  
        keywords.put("creates",     CREATE); 
        keywords.put("enter",       ENTERS);
        keywords.put("enters",      ENTERS);
        keywords.put("entered",     ENTERS);
        keywords.put("get",         GET);
        keywords.put("gets",        GET);
        keywords.put("attack",      ATTACK);
        keywords.put("attacks",     ATTACK);
        keywords.put("attacked",    ATTACK);
        keywords.put("block",       BLOCK);
        keywords.put("blocks",      BLOCK);
        keywords.put("blocked",     BLOCK);
        keywords.put("attach",      ATTACH);
        keywords.put("attached",    ATTACH);
        keywords.put("gain",        GAIN);
        keywords.put("gains",       GAIN);
        keywords.put("gained",      GAIN);
        keywords.put("has",         HAS);
        keywords.put("had",         HAS);
        keywords.put("have",        HAS);
        keywords.put("hasn't",      HAS_NOT);
        keywords.put("cost",        COST);
        keywords.put("costs",       COST);
        keywords.put("prevent",     PREVENT);
        keywords.put("exile",       EXILE);
        keywords.put("exiles",      EXILE);
        keywords.put("exiled",      EXILE);
        keywords.put("sacrifice",   SACRIFICE);
        keywords.put("sacrifices",  SACRIFICE);
        keywords.put("sacrificed",  SACRIFICE);
        keywords.put("destroy",     DESTROY);
        keywords.put("destroys",    DESTROY);
        keywords.put("destroyed",   DESTROY);
        keywords.put("draw",        DRAW);
        keywords.put("draws",       DRAW);
        keywords.put("drawn",       DRAW);
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
        keywords.put("reveals",     REVEAL);
        keywords.put("revealed",    REVEAL);
        keywords.put("enchant",     ENCHANT);
        keywords.put("equip",       EQUIP);
        keywords.put("die",         DIE);
        keywords.put("dies",        DIE);
        keywords.put("died",        DIE);
        keywords.put("surveil",     SURVEIL);
        keywords.put("scry",        SCRY);
        keywords.put("scries",      SCRY);
        keywords.put("remove",      REMOVE);
        keywords.put("removing",    REMOVE);
        keywords.put("tap",         TAP);
        keywords.put("tapped",      TAP);
        keywords.put("untap",       UNTAP);
        keywords.put("untapped",    UNTAP);
        keywords.put("copy",        COPY);
        keywords.put("copies",      COPY);
        keywords.put("discard",     DISCARD);
        keywords.put("discards",    DISCARD);
        keywords.put("discarded",   DISCARD);
        keywords.put("mill",        MILL);
        keywords.put("mills",       MILL);
        keywords.put("choose",      CHOOSE);
        keywords.put("chooses",     CHOOSE);
        keywords.put("choice",      CHOOSE);
        keywords.put("chosen",      CHOOSE);
        keywords.put("cast",        CAST);
        keywords.put("casts",       CAST);
        keywords.put("play",        PLAY);
        keywords.put("spend",       SPEND);
        keywords.put("spent",       SPEND);
        keywords.put("activate",    ACTIVATE);
        keywords.put("become",      BECOME);
        keywords.put("becomes",     BECOME);
        keywords.put("begin",       BEGIN);
        keywords.put("resolved",    RESOLVE);
        keywords.put("cause",       CAUSE);
        keywords.put("causes",      CAUSE);
        keywords.put("search",      SEARCH);
        keywords.put("shuffle",     SHUFFLE);
        keywords.put("leave",       LEAVE);
        keywords.put("leaves",      LEAVE);
        keywords.put("add",         ADD);
        keywords.put("distribute",  DISTRIBUTE);
        keywords.put("fight",       FIGHT);
        keywords.put("fights",      FIGHT);
        keywords.put("separate",    SEPARATE);
        keywords.put("change",      CHANGE);
        keywords.put("exchange",    EXCHANGE);
        keywords.put("proliferate", PROLIFERATE);
        keywords.put("cycle",       CYCLE);
        keywords.put("change",      CHANGE);
        keywords.put("exploit",     EXPLOIT);
        keywords.put("exploits",    EXPLOIT);
        keywords.put("contain",     CONTAIN);
        keywords.put("manifest",    MANIFEST);
        keywords.put("remain",      REMAIN);
        keywords.put("remains",     REMAIN);
        keywords.put("exert",       EXERT);
        keywords.put("flip",        FLIP);
        keywords.put("discover",    DISCOVER);
        keywords.put("connive",     CONNIVE);
        keywords.put("connives",    CONNIVE);
        keywords.put("skip",        SKIP);
        keywords.put("amass",       AMASS);
        keywords.put("move",        MOVE);
        keywords.put("investigate", INVESTIGATE);
        keywords.put("populate",    POPULATE);
        
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
        keywords.put("players",      PLAYER);
        keywords.put("different",   DIFFERENT);
        keywords.put("their",       THEIR);
        keywords.put("divided",     DIVIDED);
        keywords.put("source",      SOURCE);
        keywords.put("sources",     SOURCE);
        keywords.put("kind",        KIND);
        keywords.put("x",           X_VAR);
        keywords.put("controller",  CONTROLLER);
        keywords.put("anywhere",    ANYWHERE);
        keywords.put("onto",        ONTO);
        keywords.put("they're",     THEY_ARE);
        keywords.put("who",         WHO);
        keywords.put("modified",    MODIFIED);

        //game concepts
        keywords.put("control",     CONTROL);
        keywords.put("controls",    CONTROL);
        keywords.put("controlled",  CONTROL);
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
        keywords.put("abilities",   ABILITY);
        keywords.put("triggers",    TRIGGER);
        keywords.put("triggered",   TRIGGER);
        keywords.put("lose",        LOSE);
        keywords.put("loses",       LOSE);
        keywords.put("lost",        LOSE);
        keywords.put("game",        GAME);
        keywords.put("win",         WIN);
        keywords.put("graveyard",   GRAVEYARD);
        keywords.put("graveyards",  GRAVEYARD);
        keywords.put("library",     LIBRARY);
        keywords.put("battlefield", BATTLEFIELD);
        keywords.put("hand",        HAND);
        keywords.put("hands",       HAND);
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
        keywords.put("effect",      EFFECT);
        keywords.put("own",         OWN);
        keywords.put("emblem",      EMBLEM);
        keywords.put("excess",      EXCESS);
        keywords.put("starting",    STARTING);
        keywords.put("any",         ANY);
        keywords.put("everything",  EVERYTHING);
        keywords.put("power",       POWER);
        keywords.put("toughness",   TOUGHNESS);
        keywords.put("color",       COLOR);
        keywords.put("colors",      COLOR);
        keywords.put("name",        NAME);
        keywords.put("names",       NAME);
        keywords.put("face-up",     FACEUP);
        keywords.put("face-down",   FACEDOWN);
        keywords.put("pile",        PILE);
        keywords.put("piles",       PILE);
        keywords.put("activated",   ACTIVATED);
        keywords.put("loyalty",     LOYALTY);
        keywords.put("base",        BASE);
        keywords.put("new",         NEW);
        keywords.put("devotion",    DEVOTION);

        //triggers
        keywords.put("whenever",    WHENEVER);
        keywords.put("when",        WHEN);
        keywords.put("would",       WOULD);
        keywords.put("where",       WHERE);
        keywords.put("if",          IF);
        keywords.put("was",         WAS);
        keywords.put("were",        WERE);
        keywords.put("wasn't",      WAS_NOT);
        keywords.put("weren't",     WERE_NOT);
        keywords.put("instead",     INSTEAD);
        keywords.put("as",          AS);
        keywords.put("except",      EXCEPT);
        keywords.put("must",        MUST);

        //timing
        keywords.put("during",      DURING);
        keywords.put("until",       UNTIL);
        keywords.put("beginning",   BEGINNING);
        keywords.put("next",        NEXT);
        keywords.put("step",        STEP);
        keywords.put("steps",       STEP);
        keywords.put("opening",     OPENING);
        keywords.put("at",          AT);
        keywords.put("after",       AFTER);
        keywords.put("phase",       PHASE);
        keywords.put("phases",      PHASE);
        keywords.put("upkeep",      UPKEEP);
        keywords.put("while",       WHILE);
        keywords.put("before",      BEFORE);
        keywords.put("main",        MAIN);
        keywords.put("postcombat",  POSTCOMBAT);
        
        //quantifiers
        keywords.put("more",        MORE);
        keywords.put("greater",     MORE);
        keywords.put("less",        LESS);
        keywords.put("fewer",       LESS);
        keywords.put("up",          UP);
        keywords.put("value",       VALUE);
        keywords.put("many",        MANY);
        keywords.put("once",        ONCE);
        keywords.put("twice",       TWICE);
        keywords.put("much",        MUCH);
        keywords.put("amount",      AMOUNT);
        keywords.put("least",       LEAST);
        keywords.put("greatest",    GREATEST);
        keywords.put("single",      SINGLE);
        keywords.put("total",       TOTAL);
        keywords.put("maximum",     MAXIMUM);
        keywords.put("size",        SIZE);
        keywords.put("-X",          X_LOYALTY);
        keywords.put("plus",        PLUS);
        keywords.put("minus",       MINUS);
        keywords.put("exactly",     EXACTLY);
        keywords.put("half",        HALF);
        keywords.put("rounded",     ROUND);
        keywords.put("down",        DOWN);
        
        //annoying vestigial tokens
        keywords.put("long",        LONG);
        keywords.put("in",          IN);
        keywords.put("addition",    ADDITION);
        keywords.put("additional",  ADDITION);
        keywords.put("into",        INTO);
        keywords.put("under",       UNDER);
        keywords.put("a",           A);
        keywords.put("an",          A);
        keywords.put("there",       THERE);
        keywords.put("are",         ARE);
        keywords.put("those",       THOSE);
        keywords.put("time",        TIME);
        keywords.put("do",          DO);
        keywords.put("does",        DO);
        keywords.put("though",      THOUGH);
        keywords.put("that's",      THATS);
        keywords.put("of",          OF);
        keywords.put("for",         FOR);
        keywords.put("you've",      YOU_HAVE);
        keywords.put("rather",      RATHER);
        keywords.put("than",        THAN);
        keywords.put("back",        BACK);
        keywords.put("way",         WAY);
        keywords.put("don't",       DO_NOT);
        keywords.put("doesn't",     DO_NOT);
        keywords.put("no",          NO);
        keywords.put("without",     WITHOUT);
        keywords.put("by",          BY);
        keywords.put("putting",     PUTTING);
        keywords.put("been",        BEEN);
        keywords.put("able",        ABLE);
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

    private boolean possessive_check(String t) {
        return supertypes.contains(t.substring(0, t.length() - 2)) && "'s".equals(t.substring(t.length() - 2, t.length() - 1));
    }

    //prescan rules
        //card names are removed
        //non's are split away

    private void scanToken() {
        String t = advance().toLowerCase();
        //regex for plural supertypes?
            //TYPE's
        switch(t) {
            case "first":
                if (match("strike")) {
                    addToken(KEYWORD, "first strike");
                }
                else {
                    addToken(ITERATION, 1);
                }
                break;
            case "double":
                if (match("strike")) {
                    addToken(KEYWORD, "double strike");
                }
                else {
                    addToken(DOUBLE);
                }
                break;
            default:
                TokenType s = keywords.get(t);
                if (s != null) {                                                                                                //checking the major keyword tokens - when, or, etc
                    addToken(s);
                } else if (shorthand_keywords.contains(t)) {                                                                    //checking the shorthand keywords - vigilance, kicker, etc
                    addToken(KEYWORD, t);
                } else if (supertypes.contains(t) || supertypes.contains(t.substring(0, t.length() - 1))) {          //checking the supertypes - creature, spell, etc - plus their plurals
                    addToken(SUPERTYPE, t);
                } else if (subtypes.contains(t) || subtypes.contains(t.substring(0, t.length() - 1))) {              //checking the subtypes - human, aura, etc - plus their generic plurals
                    addToken(SUBTYPE, t);
                } else if (colors.contains(t)) {                                                                                //checking the color references - white, colorless, etc
                    addToken(COLOR_SPEC, t);
                } else if (numbers.containsKey(t)) {                                                                            //checking words that map to numbers - one, seven, etc
                    addToken(QUANTITY, numbers.get(t));
                } else if (iterations.containsKey(t)) {                                                                         //checking for iterations - first, seventh, etc
                    addToken(ITERATION, iterations.get(t));
                } else if (t.matches("^(\\d+)$")) {                                                                       //checking for outright digits/numbers - 7, 14, etc
                    addToken(QUANTITY, Integer.parseInt(t));
                } else if (t.matches("^(\\{.+\\})+$")) {                                                                  //checking for cost formatting - {2}, {T}, etc
                    addToken(COST_VALUE, t);
                } else if (t.matches("^(\\+|\\-)([0-9]+)$")) {                                                            //checking for planeswalker loyalty - +2, -12, etc
                    addToken(LOYALTY_COST, t);
                } else if (t.length() > 3 && "'s".equals(t.substring(t.length() - 2, t.length()))) {                            //checking for possessives - player's, creature's, etc
                    String pre = t.substring(0, t.length() - 2);
                    if (keywords.containsKey(pre)) {
                        addToken(keywords.get(pre));
                        addToken(_S);
                    }
                    else if (supertypes.contains(pre)) {
                        addToken(SUPERTYPE, pre);
                        addToken(_S);
                    }
                    else {
                        addToken(UNRECOGNIZED, pre);
                    }
                } else {
                    Matcher block_match = stat_block.matcher(t);
                    Matcher change_match = stat_change.matcher(t);
                    if (block_match.find()) {                                                                                   //checking for stat block formatting - 1/1, 7/2, etc
                        String statArray = "" + block_match.group(1) + " " + block_match.group(2);
                        addToken(STAT_BLOCK, statArray);
                    }
                    else if (change_match.find()){                                                                              //checking for stat change formatting - +0/+1, -3/+3, etc
                        String statArray = "" + change_match.group(1) + change_match.group(2) + " " + change_match.group(3) + change_match.group(4);
                        addToken(STAT_CHANGE, statArray);
                    }
                    else {                                                                                                      //base case for token types, potentially card names, and errors
                        addToken(UNRECOGNIZED, t);
                    }        
                }
        }

    }

}

