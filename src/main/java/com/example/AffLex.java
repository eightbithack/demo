package com.example;

import static com.example.TokenType.UNRECOGNIZED;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class AffLex {

    static boolean hadError = false;

    static boolean tester = false;
    static boolean debugMode = true;

    static String[] ignoreList = {"alliance", "landfall", "raid", "morbid", "threshold", "formidable", "ferocious",
                                    "drakuseth", "etali", "aurelia", "alesha", "koma", "coil", "koma's", "alesha's", "ramos", "garna", "ruby", "kiora", "deep", "syr", "alin",
                                        "dwynen", "arcanis", "lathril", "legitimate", "businessperson", "halana", "alena", "elenda", "arahbo", "kellan",
                                    "food", "treasure",
                                    "bait", "stun", "revival", "divinity", "incubation", "page", "wish", "soul", "fellowship", "poison", "stash",
                                    "chandra", "kaito",
                                    "~cardname~"};
    static HashSet<String> ignoreSet = new HashSet<>(Arrays.asList(ignoreList));

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64); 
        } else if (args.length == 1) {
            runFile(args[0]);
        } else if (tester) {
            runPrompt();
        } else {
            runFile("src/main/java/com/example/cardTextSample.txt");
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        String extract = new String(bytes, Charset.defaultCharset());
        for (String s : extract.split("\\n")) {
            run(s);
            if (hadError) System.exit(65);
        }
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) { 
            System.out.print("> ");
            String line = reader.readLine();
        if (line == null) break;
            run(line);
            hadError = false;
        }
    }

    private static List<String> preprocess(String source) {
        return Arrays.asList(source
            .toLowerCase()
            .replaceAll("^\\\"(.*)\\\"\\,", "$1")               //extracts the text between quo
            .replaceAll("([.,|:])", " $1")                      //inserts spaces before punctuation to separate them into distinct characters
            .replaceAll("\\b(non)-?([^\\s]+)", "$1 $2")         //splits 'nonX' strings into 'non X'
            .replaceAll("\\([^()]*\\)", "")                     //deletes text inside parens (ie, reminder text)
            .replaceAll("\\\\u2014", " - ")                     //removes the unicode dash and replaces it with the minus/smaller dash
            .replaceAll("\\\\u2212", "-")                       //replaces the unicode minus used in PW loyalty abilities with dashes
            .replaceAll("\\\\\"", " \\\" ")                     //put spaces around quotes
            .split("\\s+"));
    }

    private static void run(String source) {
        List<String> test = preprocess(source);
        //System.out.println("[" + source + "]");
        Scanner scanner = new Scanner(test);
        List<Token> tokens = scanner.scanTokens();
        boolean all_clear = true;
        // For now, just print the tokens.
        if (debugMode) {
            for (Token token : tokens) {
                if (token.type.equals(UNRECOGNIZED) && !ignoreSet.contains(token.literal)) {
                    if (all_clear) {
                        System.out.println(source);
                        all_clear = false;
                    }
                    System.out.println("    " + token);
                }
            }
        } else {
            for (Token token : tokens) {
                System.out.println(token);
            }
        } 
    }

    static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
