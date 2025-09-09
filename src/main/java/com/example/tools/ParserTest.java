package com.example.tools;

import com.example.Token;
import com.example.AST.Expr;
import java.util.List;

public class ParserTest {
    
    public static void main(String[] args) {
        // Test with sample token sentences from the file
        String[] testSentences = {
            "KEYWORD(affinity) FOR(null) SUPERTYPE(artifacts) LINE_END(null)",
            "KEYWORD(flying) LINE_END(null)",
            "WHEN(null) THIS(null) SUPERTYPE(creature) ENTERS(null) COMMA(null) EACH(null) OPPONENT(null) DISCARD(null) A(null) CARD(null) PERIOD(null) FOR(null) EACH(null) OPPONENT(null) WHO(null) CANNOT(null) COMMA(null) YOU(null) DRAW(null) A(null) CARD(null) PERIOD(null) LINE_END(null)",
            "COST_VALUE({2}{g}) COLON(null) KEYWORD(adapt) QUANTITY(2) PERIOD(null) LINE_END(null)",
            "SAGA_COUNT(1) DASH(null) EXILE(null) TARGET(null) SUPERTYPE(creature) A(null) OPPONENT(null) CONTROL(null) WITH(null) POWER(null) QUANTITY(3) OR(null) MORE(null) PERIOD(null) LINE_END(null)"
        };
        
        for (int i = 0; i < testSentences.length; i++) {
            System.out.println("=== Test " + (i + 1) + " ===");
            System.out.println("Input: " + testSentences[i]);
            
            List<Token> tokens = TokenParser.parseTokenString(testSentences[i]);
            System.out.println("Tokens: " + tokens.size());
            for (Token token : tokens) {
                System.out.println("  " + token.type + "(" + token.literal + ")");
            }
            AffParse parser = new AffParse(tokens);
            Expr ast = parser.parse();
            
            if (ast != null) {
                System.out.println("AST: " + ast.getClass().getSimpleName());
                printAST(ast, 0);
            } else {
                System.out.println("Failed to parse");
            }
            System.out.println();
        }
    }
    
    private static void printAST(Expr expr, int depth) {
        String indent = "  ".repeat(depth);
        
        if (expr instanceof Expr.KeywordAbility) {
            Expr.KeywordAbility ka = (Expr.KeywordAbility) expr;
            System.out.println(indent + "KeywordAbility: " + ka.keyword);
            if (ka.modifier != null) {
                System.out.println(indent + "  Modifier:");
                printAST(ka.modifier, depth + 2);
            }
        } else if (expr instanceof Expr.TriggeredAbility) {
            Expr.TriggeredAbility ta = (Expr.TriggeredAbility) expr;
            System.out.println(indent + "TriggeredAbility:");
            System.out.println(indent + "  Trigger:");
            printAST(ta.trigger, depth + 2);
            System.out.println(indent + "  Effect:");
            printAST(ta.effect, depth + 2);
        } else if (expr instanceof Expr.ActivatedAbility) {
            Expr.ActivatedAbility aa = (Expr.ActivatedAbility) expr;
            System.out.println(indent + "ActivatedAbility:");
            System.out.println(indent + "  Cost:");
            printAST(aa.cost, depth + 2);
            System.out.println(indent + "  Effect:");
            printAST(aa.effect, depth + 2);
        } else if (expr instanceof Expr.StaticAbility) {
            Expr.StaticAbility sa = (Expr.StaticAbility) expr;
            System.out.println(indent + "StaticAbility:");
            System.out.println(indent + "  Condition:");
            printAST(sa.condition, depth + 2);
            System.out.println(indent + "  Effect:");
            printAST(sa.effect, depth + 2);
        } else if (expr instanceof Expr.SagaChapter) {
            Expr.SagaChapter sc = (Expr.SagaChapter) expr;
            System.out.println(indent + "SagaChapter: " + sc.chapterNumber);
            if (sc.effect != null) {
                System.out.println(indent + "  Effect:");
                printAST(sc.effect, depth + 2);
            }
        } else if (expr instanceof Expr.Sequence) {
            Expr.Sequence seq = (Expr.Sequence) expr;
            System.out.println(indent + "Sequence:");
            for (Expr e : seq.expressions) {
                printAST(e, depth + 1);
            }
        } else if (expr instanceof Expr.Literal) {
            Expr.Literal lit = (Expr.Literal) expr;
            System.out.println(indent + "Literal: " + lit.value);
        } else if (expr instanceof Expr.Type) {
            Expr.Type type = (Expr.Type) expr;
            System.out.println(indent + "Type:");
            if (type.supertype != null) System.out.println(indent + "  Supertype: " + type.supertype);
            if (type.subtype != null) System.out.println(indent + "  Subtype: " + type.subtype);
            if (type.color != null) System.out.println(indent + "  Color: " + type.color);
        } else if (expr instanceof Expr.Cost) {
            Expr.Cost cost = (Expr.Cost) expr;
            System.out.println(indent + "Cost: " + cost.costType + " = " + cost.value);
            if (cost.alternative != null) {
                System.out.println(indent + "  Alternative:");
                printAST(cost.alternative, depth + 2);
            }
        } else if (expr instanceof Expr.ConditionalEffect) {
            Expr.ConditionalEffect ce = (Expr.ConditionalEffect) expr;
            System.out.println(indent + "ConditionalEffect:");
            System.out.println(indent + "  Condition:");
            printAST(ce.condition, depth + 2);
            System.out.println(indent + "  True Effect:");
            printAST(ce.trueEffect, depth + 2);
            if (ce.falseEffect != null) {
                System.out.println(indent + "  False Effect:");
                printAST(ce.falseEffect, depth + 2);
            }
        } else {
            System.out.println(indent + "Unknown expression type: " + expr.getClass().getSimpleName());
        }
    }
}
