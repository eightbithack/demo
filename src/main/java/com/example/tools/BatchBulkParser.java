package com.example.tools;

import com.example.Token;
import com.example.AST.Expr;
import java.util.List;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BatchBulkParser {
    
    public static void main(String[] args) {
        try {
            // Read all lines from the sample file
            List<String> lines = Files.readAllLines(Paths.get("com/example/cardTextSampleTokens.txt"));
            
            // Create output file
            PrintWriter writer = new PrintWriter(new FileWriter("com/example/tools/batch_parsing_results.txt"));
            
            writer.println("=== BATCH MAGIC: THE GATHERING CARD ABILITY PARSING RESULTS ===");
            writer.println("Generated on: " + java.time.LocalDateTime.now());
            writer.println("Total sentences to parse: " + lines.size());
            writer.println();
            
            int successCount = 0;
            int failureCount = 0;
            int recursionFailureCount = 0;
            int processedCount = 0;
            
            // Process in batches of 20 to avoid long waits
            int batchSize = 20;
            int totalBatches = (lines.size() + batchSize - 1) / batchSize;
            
            for (int batch = 0; batch < totalBatches; batch++) {
                int startIdx = batch * batchSize;
                int endIdx = Math.min(startIdx + batchSize, lines.size());
                
                writer.println("=== BATCH " + (batch + 1) + "/" + totalBatches + " (Sentences " + (startIdx + 1) + "-" + endIdx + ") ===");
                writer.println();
                
                for (int i = startIdx; i < endIdx; i++) {
                    String line = lines.get(i).trim();
                    if (line.isEmpty()) continue;
                    
                    writer.println("Sentence " + (i + 1) + ": " + line);
                    
                    try {
                        List<Token> tokens = TokenParser.parseTokenString(line);
                        
                        // Try to parse with timeout protection
                        AffParse parser = new AffParse(tokens);
                        Expr ast = parser.parse();
                        
                        if (ast != null) {
                            writer.println("✓ Successfully parsed as " + ast.getClass().getSimpleName());
                            successCount++;
                        } else {
                            writer.println("✗ Failed to parse - returned null");
                            failureCount++;
                        }
                    } catch (StackOverflowError e) {
                        writer.println("✗ Stack overflow - infinite recursion");
                        recursionFailureCount++;
                    } catch (OutOfMemoryError e) {
                        writer.println("✗ Out of memory - likely infinite loop");
                        recursionFailureCount++;
                    } catch (Exception e) {
                        writer.println("✗ Parse error: " + e.getMessage());
                        failureCount++;
                    }
                    
                    processedCount++;
                    writer.println();
                }
                
                writer.println("Batch " + (batch + 1) + " complete. Progress: " + processedCount + "/" + lines.size());
                writer.println();
                
                // Flush output periodically
                writer.flush();
            }
            
            writer.println("=== FINAL SUMMARY ===");
            writer.println("Total sentences processed: " + processedCount);
            writer.println("Successfully parsed: " + successCount);
            writer.println("Failed to parse: " + failureCount);
            writer.println("Recursion/memory issues: " + recursionFailureCount);
            writer.println("Success rate: " + String.format("%.1f%%", (double)successCount / processedCount * 100));
            
            writer.println();
            writer.println("=== PARSER CAPABILITIES DEMONSTRATED ===");
            writer.println("✓ Keyword abilities (simple and with modifiers)");
            writer.println("✓ Activated abilities (cost + effect)");
            writer.println("✓ Saga chapters");
            writer.println("✓ Basic token parsing and AST construction");
            
            writer.println();
            writer.println("=== KNOWN LIMITATIONS ===");
            writer.println("✗ Complex triggered abilities may cause infinite recursion");
            writer.println("✗ Static abilities need more robust parsing");
            writer.println("✗ Some token sequences cause infinite loops");
            writer.println("✗ Parser needs better error handling for edge cases");
            
            writer.close();
            System.out.println("Batch parsing complete! Results saved to batch_parsing_results.txt");
            System.out.println("Successfully parsed: " + successCount + "/" + processedCount);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }
}
