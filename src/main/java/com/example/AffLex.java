package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class AffLex {

    static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64); 
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
        if (hadError) System.exit(65);
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
            .replaceAll("([.,])", " $1")                        //inserts spaces before punctuation to separate them into distinct characters
            .replaceAll("\\b(non)([^\\s]+)", "$1 $2")           //splits 'nonX' strings into 'non X'
            .replaceAll("\\([^()]*\\)", "")                     //deletes text inside parens (ie, reminder text)
            .replaceAll("\\\\u2014", " - ")                     //removes the unicode dash and replaces it with the minus/smaller dash
            .split("\\s+"));
    }

    private static void run(String source) {
        List<String> test = preprocess(source);
        Scanner scanner = new Scanner(test);
        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
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
