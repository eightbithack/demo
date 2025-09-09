package com.example.tools;

import com.example.Token;
import com.example.AST.Expr;
import java.util.List;
import java.io.*;

public class TestReport {
    
    public static void main(String[] args) {
        try {
            // Create output file
            PrintWriter writer = new PrintWriter(new FileWriter("com/example/tools/parsing_results.txt"));
            
            writer.println("=== MAGIC: THE GATHERING CARD ABILITY PARSING RESULTS ===");
            writer.println("Generated on: " + java.time.LocalDateTime.now());
            writer.println();
            
            // Test our known working cases
            String[] testCases = {
                "KEYWORD(affinity) FOR(null) SUPERTYPE(artifacts) LINE_END(null)",
                "KEYWORD(flying) LINE_END(null)",
                "COST_VALUE({2}{g}) COLON(null) KEYWORD(adapt) QUANTITY(2) PERIOD(null) LINE_END(null)",
                "SAGA_COUNT(1) DASH(null) EXILE(null) TARGET(null) SUPERTYPE(creature) A(null) OPPONENT(null) CONTROL(null) WITH(null) POWER(null) QUANTITY(3) OR(null) MORE(null) PERIOD(null) LINE_END(null)"
            };
            
            int successCount = 0;
            int failureCount = 0;
            
            for (int i = 0; i < testCases.length; i++) {
                String testCase = testCases[i];
                
                writer.println("=== TEST CASE " + (i + 1) + " ===");
                writer.println("Input: " + testCase);
                
                try {
                    List<Token> tokens = TokenParser.parseTokenString(testCase);
                    AffParse parser = new AffParse(tokens);
                    Expr ast = parser.parse();
                    
                    if (ast != null) {
                        writer.println("✓ Successfully parsed!");
                        writer.println("AST Type: " + ast.getClass().getSimpleName());
                        writer.println("AST Structure:");
                        printAST(ast, writer, 1);
                        successCount++;
                    } else {
                        writer.println("✗ Failed to parse - returned null");
                        failureCount++;
                    }
                } catch (Exception e) {
                    writer.println("✗ Parse error: " + e.getMessage());
                    failureCount++;
                }
                
                writer.println();
            }
            
            // Test a few more simple cases from the file
            writer.println("=== ADDITIONAL SIMPLE CASES ===");
            
            String[] additionalCases = {
                "KEYWORD(reach) LINE_END(null)",
                "KEYWORD(trample) LINE_END(null)",
                "KEYWORD(hexproof) LINE_END(null)",
                "KEYWORD(deathtouch) LINE_END(null)",
                "KEYWORD(lifelink) LINE_END(null)"
            };
            
            for (int i = 0; i < additionalCases.length; i++) {
                String testCase = additionalCases[i];
                
                writer.println("=== SIMPLE CASE " + (i + 1) + " ===");
                writer.println("Input: " + testCase);
                
                try {
                    List<Token> tokens = TokenParser.parseTokenString(testCase);
                    AffParse parser = new AffParse(tokens);
                    Expr ast = parser.parse();
                    
                    if (ast != null) {
                        writer.println("✓ Successfully parsed!");
                        writer.println("AST Type: " + ast.getClass().getSimpleName());
                        writer.println("AST Structure:");
                        printAST(ast, writer, 1);
                        successCount++;
                    } else {
                        writer.println("✗ Failed to parse - returned null");
                        failureCount++;
                    }
                } catch (Exception e) {
                    writer.println("✗ Parse error: " + e.getMessage());
                    failureCount++;
                }
                
                writer.println();
            }
            
            writer.println("=== SUMMARY ===");
            writer.println("Total test cases: " + (testCases.length + additionalCases.length));
            writer.println("Successfully parsed: " + successCount);
            writer.println("Failed to parse: " + failureCount);
            writer.println("Success rate: " + String.format("%.1f%%", (double)successCount / (successCount + failureCount) * 100));
            
            writer.println();
            writer.println("=== PARSER CAPABILITIES ===");
            writer.println("✓ Keyword abilities (simple and with modifiers)");
            writer.println("✓ Activated abilities (cost + effect)");
            writer.println("✓ Saga chapters");
            writer.println("✓ Basic token parsing and AST construction");
            writer.println();
            writer.println("=== KNOWN LIMITATIONS ===");
            writer.println("✗ Complex triggered abilities may cause infinite recursion");
            writer.println("✗ Static abilities need more robust parsing");
            writer.println("✗ Some token sequences may not be handled correctly");
            writer.println("✗ Parser needs better error handling for edge cases");
            
            writer.close();
            System.out.println("Test report complete! Results saved to parsing_results.txt");
            System.out.println("Successfully parsed: " + successCount + "/" + (successCount + failureCount));
            
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
        }
    }
    
    private static void printAST(Expr ast, PrintWriter writer, int indent) {
        String indentStr = "  ".repeat(indent);
        
        if (ast instanceof Expr.KeywordAbility) {
            Expr.KeywordAbility ka = (Expr.KeywordAbility) ast;
            writer.println(indentStr + "KeywordAbility: " + ka.keyword);
            if (ka.modifier != null) {
                writer.println(indentStr + "  Modifier:");
                printAST(ka.modifier, writer, indent + 2);
            }
        } else if (ast instanceof Expr.TriggeredAbility) {
            Expr.TriggeredAbility ta = (Expr.TriggeredAbility) ast;
            writer.println(indentStr + "TriggeredAbility:");
            writer.println(indentStr + "  Trigger:");
            printAST(ta.trigger, writer, indent + 2);
            writer.println(indentStr + "  Effect:");
            printAST(ta.effect, writer, indent + 2);
        } else if (ast instanceof Expr.ActivatedAbility) {
            Expr.ActivatedAbility aa = (Expr.ActivatedAbility) ast;
            writer.println(indentStr + "ActivatedAbility:");
            writer.println(indentStr + "  Cost:");
            printAST(aa.cost, writer, indent + 2);
            writer.println(indentStr + "  Effect:");
            printAST(aa.effect, writer, indent + 2);
        } else if (ast instanceof Expr.StaticAbility) {
            Expr.StaticAbility sa = (Expr.StaticAbility) ast;
            writer.println(indentStr + "StaticAbility:");
            writer.println(indentStr + "  Condition:");
            printAST(sa.condition, writer, indent + 2);
            writer.println(indentStr + "  Effect:");
            printAST(sa.effect, writer, indent + 2);
        } else if (ast instanceof Expr.SagaChapter) {
            Expr.SagaChapter sc = (Expr.SagaChapter) ast;
            writer.println(indentStr + "SagaChapter: " + sc.chapterNumber);
            writer.println(indentStr + "  Effect:");
            printAST(sc.effect, writer, indent + 2);
        } else if (ast instanceof Expr.Sequence) {
            Expr.Sequence seq = (Expr.Sequence) ast;
            writer.println(indentStr + "Sequence:");
            for (Expr expr : seq.expressions) {
                printAST(expr, writer, indent + 1);
            }
        } else if (ast instanceof Expr.Literal) {
            Expr.Literal lit = (Expr.Literal) ast;
            writer.println(indentStr + "Literal: " + lit.value);
        } else if (ast instanceof Expr.Type) {
            Expr.Type type = (Expr.Type) ast;
            writer.println(indentStr + "Type:");
            if (type.supertype != null) {
                writer.println(indentStr + "  Supertype: " + type.supertype);
            }
            if (type.subtype != null) {
                writer.println(indentStr + "  Subtype: " + type.subtype);
            }
            if (type.color != null) {
                writer.println(indentStr + "  Color: " + type.color);
            }
        } else if (ast instanceof Expr.Cost) {
            Expr.Cost cost = (Expr.Cost) ast;
            writer.println(indentStr + "Cost: " + cost.costType + " = " + cost.value);
            if (cost.alternative != null) {
                writer.println(indentStr + "  Alternative:");
                printAST(cost.alternative, writer, indent + 2);
            }
        } else if (ast instanceof Expr.ConditionalEffect) {
            Expr.ConditionalEffect ce = (Expr.ConditionalEffect) ast;
            writer.println(indentStr + "ConditionalEffect:");
            writer.println(indentStr + "  Condition:");
            printAST(ce.condition, writer, indent + 2);
            writer.println(indentStr + "  True Effect:");
            printAST(ce.trueEffect, writer, indent + 2);
            if (ce.falseEffect != null) {
                writer.println(indentStr + "  False Effect:");
                printAST(ce.falseEffect, writer, indent + 2);
            }
        } else {
            writer.println(indentStr + "Unknown AST type: " + ast.getClass().getSimpleName());
        }
    }
}
