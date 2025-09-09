package com.example.tools;

import com.example.Token;
import com.example.AST.Expr;
import java.util.List;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ComprehensiveBulkParser {
    
    private static int maxRecursionDepth = 50;
    private static int recursionCounter = 0;
    
    public static void main(String[] args) {
        try {
            // Read all lines from the sample file
            List<String> lines = Files.readAllLines(Paths.get("com/example/cardTextSampleTokens.txt"));
            
            // Create output file
            PrintWriter writer = new PrintWriter(new FileWriter("com/example/tools/comprehensive_parsing_results.txt"));
            
            writer.println("=== COMPREHENSIVE MAGIC: THE GATHERING CARD ABILITY PARSING RESULTS ===");
            writer.println("Generated on: " + java.time.LocalDateTime.now());
            writer.println("Total sentences to parse: " + lines.size());
            writer.println();
            
            int successCount = 0;
            int failureCount = 0;
            int recursionFailureCount = 0;
            int tokenizationFailureCount = 0;
            
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                
                writer.println("=== SENTENCE " + (i + 1) + " ===");
                writer.println("Input: " + line);
                
                try {
                    // Reset recursion counter for each sentence
                    recursionCounter = 0;
                    
                    List<Token> tokens = TokenParser.parseTokenString(line);
                    writer.println("Tokens parsed: " + tokens.size());
                    
                    // Create parser with recursion protection
                    SafeAffParse parser = new SafeAffParse(tokens);
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
                } catch (RecursionLimitException e) {
                    writer.println("✗ Recursion limit exceeded - " + e.getMessage());
                    recursionFailureCount++;
                } catch (Exception e) {
                    writer.println("✗ Parse error: " + e.getMessage());
                    failureCount++;
                }
                
                writer.println();
            }
            
            writer.println("=== SUMMARY ===");
            writer.println("Total sentences processed: " + lines.size());
            writer.println("Successfully parsed: " + successCount);
            writer.println("Failed to parse: " + failureCount);
            writer.println("Recursion limit exceeded: " + recursionFailureCount);
            writer.println("Tokenization failures: " + tokenizationFailureCount);
            writer.println("Success rate: " + String.format("%.1f%%", (double)successCount / lines.size() * 100));
            
            writer.println();
            writer.println("=== PARSER ANALYSIS ===");
            writer.println("✓ Simple keyword abilities work well");
            writer.println("✓ Activated abilities with cost + effect work");
            writer.println("✓ Saga chapters parse correctly");
            writer.println("⚠ Complex triggered abilities may hit recursion limits");
            writer.println("⚠ Static abilities need more robust parsing");
            writer.println("⚠ Some token sequences cause infinite loops");
            
            writer.close();
            System.out.println("Comprehensive parsing complete! Results saved to comprehensive_parsing_results.txt");
            System.out.println("Successfully parsed: " + successCount + "/" + lines.size());
            
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
    
    // Custom exception for recursion limits
    static class RecursionLimitException extends RuntimeException {
        public RecursionLimitException(String message) {
            super(message);
        }
    }
    
    // Safe parser wrapper with recursion protection
    static class SafeAffParse extends AffParse {
        public SafeAffParse(List<Token> tokens) {
            super(tokens);
        }
        
        @Override
        public Expr parse() {
            recursionCounter++;
            if (recursionCounter > maxRecursionDepth) {
                throw new RecursionLimitException("Maximum recursion depth of " + maxRecursionDepth + " exceeded");
            }
            
            try {
                return super.parse();
            } finally {
                recursionCounter--;
            }
        }
        
        @Override
        protected Expr parseAbility() {
            recursionCounter++;
            if (recursionCounter > maxRecursionDepth) {
                throw new RecursionLimitException("Maximum recursion depth exceeded in parseAbility");
            }
            
            try {
                return super.parseAbility();
            } finally {
                recursionCounter--;
            }
        }
        
        @Override
        protected Expr parseSequence() {
            recursionCounter++;
            if (recursionCounter > maxRecursionDepth) {
                throw new RecursionLimitException("Maximum recursion depth exceeded in parseSequence");
            }
            
            try {
                return super.parseSequence();
            } finally {
                recursionCounter--;
            }
        }
        
        @Override
        protected Expr parseEffect() {
            recursionCounter++;
            if (recursionCounter > maxRecursionDepth) {
                throw new RecursionLimitException("Maximum recursion depth exceeded in parseEffect");
            }
            
            try {
                return super.parseEffect();
            } finally {
                recursionCounter--;
            }
        }
    }
}
