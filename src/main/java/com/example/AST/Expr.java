package com.example.AST;

import com.example.Token;

public abstract class Expr {
    
    public static class KeywordAbility extends Expr {
        public final String keyword;
        public final Expr modifier;
        
        public KeywordAbility(String keyword, Expr modifier) {
            this.keyword = keyword;
            this.modifier = modifier;
        }
    }
    
    public static class TriggeredAbility extends Expr {
        public final Expr trigger;
        public final Expr effect;
        
        public TriggeredAbility(Expr trigger, Expr effect) {
            this.trigger = trigger;
            this.effect = effect;
        }
    }
    
    public static class ActivatedAbility extends Expr {
        public final Expr cost;
        public final Expr effect;
        
        public ActivatedAbility(Expr cost, Expr effect) {
            this.cost = cost;
            this.effect = effect;
        }
    }
    
    public static class StaticAbility extends Expr {
        public final Expr condition;
        public final Expr effect;
        
        public StaticAbility(Expr condition, Expr effect) {
            this.condition = condition;
            this.effect = effect;
        }
    }
    
    public static class ConditionalEffect extends Expr {
        public final Expr condition;
        public final Expr trueEffect;
        public final Expr falseEffect;
        
        public ConditionalEffect(Expr condition, Expr trueEffect, Expr falseEffect) {
            this.condition = condition;
            this.trueEffect = trueEffect;
            this.falseEffect = falseEffect;
        }
    }
    
    public static class Action extends Expr {
        public final String action;
        public final Expr target;
        public final Expr modifier;
        
        public Action(String action, Expr target, Expr modifier) {
            this.action = action;
            this.target = target;
            this.modifier = modifier;
        }
    }
    
    public static class Target extends Expr {
        public final Expr qualifier;
        public final Expr type;
        public final Expr controller;
        
        public Target(Expr qualifier, Expr type, Expr controller) {
            this.qualifier = qualifier;
            this.type = type;
            this.controller = controller;
        }
    }
    
    public static class Type extends Expr {
        public final String supertype;
        public final String subtype;
        public final String color;
        
        public Type(String supertype, String subtype, String color) {
            this.supertype = supertype;
            this.subtype = subtype;
            this.color = color;
        }
    }
    
    public static class Cost extends Expr {
        public final String costType;
        public final String value;
        public final Expr alternative;
        
        public Cost(String costType, String value, Expr alternative) {
            this.costType = costType;
            this.value = value;
            this.alternative = alternative;
        }
    }
    
    public static class StatBlock extends Expr {
        public final String power;
        public final String toughness;
        
        public StatBlock(String power, String toughness) {
            this.power = power;
            this.toughness = toughness;
        }
    }
    
    public static class StatChange extends Expr {
        public final String powerChange;
        public final String toughnessChange;
        
        public StatChange(String powerChange, String toughnessChange) {
            this.powerChange = powerChange;
            this.toughnessChange = toughnessChange;
        }
    }
    
    public static class Literal extends Expr {
        public final Object value;
        
        public Literal(Object value) {
            this.value = value;
        }
    }
    
    public static class Sequence extends Expr {
        public final Expr[] expressions;
        
        public Sequence(Expr[] expressions) {
            this.expressions = expressions;
        }
    }
    
    public static class Choice extends Expr {
        public final Expr[] options;
        
        public Choice(Expr[] options) {
            this.options = options;
        }
    }
    
    public static class SagaChapter extends Expr {
        public final int chapterNumber;
        public final Expr effect;
        
        public SagaChapter(int chapterNumber, Expr effect) {
            this.chapterNumber = chapterNumber;
            this.effect = effect;
        }
    }
}
