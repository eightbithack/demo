package com.example.tools;

import com.example.Token;
import com.example.AST.Expr;
import java.util.List;

public class SimpleTest {
    
    public static void main(String[] args) {
        String testSentence = "KEYWORD(affinity) FOR(null) SUPERTYPE(artifacts) LINE_END(null)";
        
        System.out.println("Input: " + testSentence);
        
        List<Token> tokens = TokenParser.parseTokenString(testSentence);
        System.out.println("Tokens: " + tokens.size());
        for (Token token : tokens) {
            System.out.println("  " + token.type + "(" + token.literal + ")");
        }
        
        AffParse parser = new AffParse(tokens);
        Expr ast = parser.parse();
        
        if (ast != null) {
            System.out.println("Success! AST: " + ast.getClass().getSimpleName());
        } else {
            System.out.println("Failed to parse");
        }
    }
}
