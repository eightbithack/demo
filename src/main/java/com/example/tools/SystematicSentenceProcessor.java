package com.example.tools;

import com.example.Token;
import com.example.AST.Expr;
import java.util.List;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SystematicSentenceProcessor {
    
    public static void main(String[] args) {
        try {
            // Read all lines from the sample file
            List<String> lines = Files.readAllLines(Paths.get("com/example/cardTextSampleTokens.txt"));
            
            // Create output file
            PrintWriter writer = new PrintWriter(new FileWriter("com/example/tools/systematic_processing_results.txt"));
            
            writer.println("=== SYSTEMATIC SENTENCE PROCESSING RESULTS ===");
            writer.println("Generated on: " + java.time.LocalDateTime.now());
            writer.println("Total sentences to process: " + lines.size());
            writer.println();
            
            int successCount = 0;
            int failureCount = 0;
            int recursionFailureCount = 0;
            int processedCount = 0;
            
            // Process sentences in batches of 50 for better progress tracking
            int batchSize = 50;
            int totalBatches = (lines.size() + batchSize - 1) / batchSize;
            
            for (int batch = 0; batch < totalBatches; batch++) {
                int startIdx = batch * batchSize;
                int endIdx = Math.min(startIdx + batchSize, lines.size());
                
                writer.println("=== BATCH " + (batch + 1) + "/" + totalBatches + " (Sentences " + (startIdx + 1) + "-" + endIdx + ") ===");
                writer.println();
                
                int batchSuccess = 0;
                int batchFailure = 0;
                int batchRecursion = 0;
                
                for (int i = startIdx; i < endIdx; i++) {
                    String line = lines.get(i).trim();
                    if (line.isEmpty()) continue;
                    
                    writer.println("Sentence " + (i + 1) + ": " + line);
                    
                    try {
                        List<Token> tokens = TokenParser.parseTokenString(line);
                        
                        // Try to parse with the fixed parser
                        AffParse parser = new AffParse(tokens);
                        Expr ast = parser.parse();
                        
                        if (ast != null) {
                            writer.println("✓ Successfully parsed as " + ast.getClass().getSimpleName());
                            writer.println("  AST Structure:");
                            printAST(ast, writer, 2);
                            successCount++;
                            batchSuccess++;
                        } else {
                            writer.println("✗ Failed to parse - returned null");
                            failureCount++;
                            batchFailure++;
                        }
                    } catch (StackOverflowError e) {
                        writer.println("✗ Stack overflow - infinite recursion");
                        recursionFailureCount++;
                        batchRecursion++;
                    } catch (OutOfMemoryError e) {
                        writer.println("✗ Out of memory - likely infinite loop");
                        recursionFailureCount++;
                        batchRecursion++;
                    } catch (RuntimeException e) {
                        if (e.getMessage().contains("Maximum recursion depth")) {
                            writer.println("✗ Recursion limit exceeded: " + e.getMessage());
                            recursionFailureCount++;
                            batchRecursion++;
                        } else {
                            writer.println("✗ Parse error: " + e.getMessage());
                            failureCount++;
                            batchFailure++;
                        }
                    } catch (Exception e) {
                        writer.println("✗ Unexpected error: " + e.getMessage());
                        failureCount++;
                        batchFailure++;
                    }
                    
                    processedCount++;
                    writer.println();
                }
                
                writer.println("Batch " + (batch + 1) + " Summary:");
                writer.println("  Success: " + batchSuccess);
                writer.println("  Failures: " + batchFailure);
                writer.println("  Recursion issues: " + batchRecursion);
                writer.println("  Success rate: " + String.format("%.1f%%", (double)batchSuccess / (batchSuccess + batchFailure + batchRecursion) * 100));
                writer.println();
                
                // Flush output periodically
                writer.flush();
            }
            
            writer.println("=== FINAL SUMMARY ===");
            writer.println("Total sentences processed: " + processedCount);
            writer.println("Successfully parsed: " + successCount);
            writer.println("Failed to parse: " + failureCount);
            writer.println("Recursion/memory issues: " + recursionFailureCount);
            writer.println("Overall success rate: " + String.format("%.1f%%", (double)successCount / processedCount * 100));
            
            writer.println();
            writer.println("=== PARSER IMPROVEMENTS IMPLEMENTED ===");
            writer.println("✓ Added recursion depth limits to prevent infinite loops");
            writer.println("✓ Fixed parseSequence() to avoid recursive calls to parseAbility()");
            writer.println("✓ Added parseSingleToken() for individual token parsing");
            writer.println("✓ Improved error handling with try-finally blocks");
            writer.println("✓ Added comprehensive error reporting");
            
            writer.println();
            writer.println("=== REMAINING ISSUES TO ADDRESS ===");
            writer.println("⚠ Some complex triggered abilities may still cause recursion");
            writer.println("⚠ Static abilities need more robust parsing");
            writer.println("⚠ Some token sequences may not be handled correctly");
            writer.println("⚠ Parser could benefit from better error recovery");
            
            writer.close();
            System.out.println("Systematic processing complete! Results saved to systematic_processing_results.txt");
            System.out.println("Successfully parsed: " + successCount + "/" + processedCount);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
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
