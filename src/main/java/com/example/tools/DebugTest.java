package com.example.tools;

import com.example.Token;
import com.example.AST.Expr;
import java.util.List;

public class DebugTest {
    
    public static void main(String[] args) {
        System.out.println("=== DEBUG TEST FOR PARSER ===\n");
        
        // Test cases that might cause issues
        String[] testCases = {
            "KEYWORD(flying) LINE_END(null)",
            "WHEN(null) THIS(null) SUPERTYPE(creature) ENTERS(null) COMMA(null) EACH(null) OPPONENT(null) DISCARD(null) A(null) CARD(null) PERIOD(null) LINE_END(null)",
            "COST_VALUE({2}{g}) COLON(null) KEYWORD(adapt) QUANTITY(2) PERIOD(null) LINE_END(null)",
            "AT(null) THE(null) BEGINNING(null) OF(null) YOUR(null) ITERATION(1) MAIN(null) PHASE(null) COMMA(null) YOU(null) MAY(null) DISCARD(null) A(null) CARD(null) PERIOD(null) LINE_END(null)"
        };
        
        for (int i = 0; i < testCases.length; i++) {
            System.out.println("=== TEST CASE " + (i + 1) + " ===");
            System.out.println("Input: " + testCases[i]);
            System.out.println();
            
            try {
                List<Token> tokens = TokenParser.parseTokenString(testCases[i]);
                System.out.println("Tokens parsed: " + tokens.size());
                
                // Create parser with debug mode enabled
                AffParse parser = new AffParse(tokens, true);
                Expr ast = parser.parse();
                
                if (ast != null) {
                    System.out.println("✓ Successfully parsed as " + ast.getClass().getSimpleName());
                } else {
                    System.out.println("✗ Failed to parse - returned null");
                }
                
            } catch (Exception e) {
                System.out.println("✗ Error: " + e.getMessage());
                e.printStackTrace();
            }
            
            System.out.println("\n" + "=".repeat(50) + "\n");
        }
    }
}
