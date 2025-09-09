package com.example.tools;

import com.example.Token;
import com.example.AST.Expr;
import java.util.List;
import java.io.*;

public class SentenceAnalyzer {
    
    public static void main(String[] args) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter("com/example/tools/sentence_analysis.txt"));
            
            writer.println("=== SENTENCE-BY-SENTENCE PARSER ANALYSIS ===");
            writer.println("Generated on: " + java.time.LocalDateTime.now());
            writer.println();
            
            // Test sentences 1-15
            String[] testSentences = {
                "KEYWORD(affinity) FOR(null) SUPERTYPE(artifacts) LINE_END(null)",
                "KEYWORD(flying) LINE_END(null)",
                "WHEN(null) THIS(null) SUPERTYPE(creature) ENTERS(null) COMMA(null) EACH(null) OPPONENT(null) DISCARD(null) A(null) CARD(null) PERIOD(null) FOR(null) EACH(null) OPPONENT(null) WHO(null) CANNOT(null) COMMA(null) YOU(null) DRAW(null) A(null) CARD(null) PERIOD(null) LINE_END(null)",
                "NON(null) SUPERTYPE(creature) SUPERTYPE(spells) YOU(null) CAST(null) FROM(null) EXILE(null) HAS(null) KEYWORD(convoke) PERIOD(null) LINE_END(null)",
                "AT(null) THE(null) BEGINNING(null) OF(null) YOUR(null) ITERATION(1) MAIN(null) PHASE(null) COMMA(null) YOU(null) MAY(null) DISCARD(null) A(null) CARD(null) PERIOD(null) IF(null) YOU(null) DO(null) COMMA(null) EXILE(null) THE(null) TOP(null) QUANTITY(2) CARD(null) OF(null) YOUR(null) LIBRARY(null) COMMA(null) THEN(null) CHOOSE(null) QUANTITY(1) OF(null) THEM(null) PERIOD(null) YOU(null) MAY(null) PLAY(null) THAT(null) CARD(null) THIS(null) TURN(null) PERIOD(null) LINE_END(null)",
                "CARDNAME(null) _S(null) POWER(null) IS(null) EQUAL(null) TO(null) THE(null) NUMBER(null) OF(null) CARD(null) TYPE(null) AMONG(null) CARD(null) IN(null) YOUR(null) GRAVEYARD(null) AND(null) IT(null) TOUGHNESS(null) IS(null) EQUAL(null) TO(null) THAT(null) NUMBER(null) PLUS(null) QUANTITY(1) PERIOD(null) LINE_END(null)",
                "KEYWORD(escape) DASH(null) COST_VALUE({2}{b}) COMMA(null) EXILE(null) ANY(null) NUMBER(null) OF(null) OTHER(null) CARD(null) FROM(null) YOUR(null) GRAVEYARD(null) WITH(null) QUANTITY(4) OR(null) MORE(null) CARD(null) TYPE(null) AMONG(null) THEM(null) PERIOD(null) LINE_END(null)",
                "COST_VALUE({3}{b}) COLON(null) RETURN(null) THIS(null) CARD(null) FROM(null) YOUR(null) GRAVEYARD(null) TO(null) THE(null) BATTLEFIELD(null) TAP(null) WITH(null) QUANTITY(2) STAT_CHANGE(+1 +1) COUNTER(null) ON(null) IT(null) PERIOD(null) LINE_END(null)",
                "OTHER(null) SUPERTYPE(creatures) HAS(null) BASE(null) POWER(null) AND(null) TOUGHNESS(null) STAT_BLOCK(2 2) AND(null) ARE(null) SUBTYPE(bears) IN(null) ADDITION(null) TO(null) THEIR(null) OTHER(null) TYPE(null) PERIOD(null) LINE_END(null)",
                "COST_VALUE({2}{g}) COLON(null) KEYWORD(adapt) QUANTITY(2) PERIOD(null) LINE_END(null)",
                "MODIFIED(null) SUPERTYPE(creatures) YOU(null) CONTROL(null) HAS(null) KEYWORD(trample) PERIOD(null) LINE_END(null)",
                "KEYWORD(flying) LINE_END(null)",
                "WHEN(null) THIS(null) SUPERTYPE(creature) DIE(null) COMMA(null) RETURN(null) ANOTHER(null) TARGET(null) SUPERTYPE(artifact) CARD(null) FROM(null) YOUR(null) GRAVEYARD(null) TO(null) YOUR(null) HAND(null) PERIOD(null) LINE_END(null)",
                "KEYWORD(flying) COMMA(null) KEYWORD(hexproof) FROM(null) ACTIVATED(null) AND(null) TRIGGER(null) ABILITY(null) LINE_END(null)",
                "WHEN(null) THIS(null) SUPERTYPE(creature) ENTERS(null) COMMA(null) EXCHANGE(null) CONTROL(null) OF(null) THIS(null) SUPERTYPE(creature) AND(null) TARGET(null) SUPERTYPE(creature) A(null) OPPONENT(null) CONTROL(null) PERIOD(null) IF(null) YOU(null) DO(null) COMMA(null) YOU(null) GET(null) COST_VALUE({e}{e}{e}{e}) COMMA(null) THEN(null) SACRIFICE(null) THAT(null) SUPERTYPE(creature) UNLESS(null) YOU(null) PAY(null) A(null) AMOUNT(null) OF(null) COST_VALUE({e}) EQUAL(null) TO(null) IT(null) MANA(null) VALUE(null) PERIOD(null) LINE_END(null)"
            };
            
            int successCount = 0;
            int failureCount = 0;
            
            for (int i = 0; i < testSentences.length; i++) {
                String sentence = testSentences[i];
                
                writer.println("=== SENTENCE " + (i + 1) + " ===");
                writer.println("Input: " + sentence);
                writer.println();
                
                try {
                    List<Token> tokens = TokenParser.parseTokenString(sentence);
                    writer.println("Tokens parsed: " + tokens.size());
                    
                    // Show first few tokens for debugging
                    writer.println("First 5 tokens:");
                    for (int j = 0; j < Math.min(5, tokens.size()); j++) {
                        Token token = tokens.get(j);
                        writer.println("  " + token.type + "(" + token.literal + ")");
                    }
                    writer.println();
                    
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
                } catch (StackOverflowError e) {
                    writer.println("✗ Stack overflow - infinite recursion detected");
                    writer.println("Error: " + e.getMessage());
                    failureCount++;
                } catch (OutOfMemoryError e) {
                    writer.println("✗ Out of memory - likely infinite loop");
                    failureCount++;
                } catch (Exception e) {
                    writer.println("✗ Parse error: " + e.getMessage());
                    writer.println("Exception type: " + e.getClass().getSimpleName());
                    failureCount++;
                }
                
                writer.println();
                writer.println("---");
                writer.println();
            }
            
            writer.println("=== SUMMARY ===");
            writer.println("Total sentences tested: " + testSentences.length);
            writer.println("Successfully parsed: " + successCount);
            writer.println("Failed to parse: " + failureCount);
            writer.println("Success rate: " + String.format("%.1f%%", (double)successCount / testSentences.length * 100));
            
            writer.close();
            System.out.println("Sentence analysis complete! Results saved to sentence_analysis.txt");
            System.out.println("Successfully parsed: " + successCount + "/" + testSentences.length);
            
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
