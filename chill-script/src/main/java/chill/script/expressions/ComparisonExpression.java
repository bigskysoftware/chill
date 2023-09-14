package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.parser.ErrorType;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.runtime.Container;
import org.bouncycastle.crypto.modes.OpenPGPCFBBlockCipher;

import java.util.Objects;
import java.util.Set;

public class ComparisonExpression extends Expression {
    public static class OrdinalExpression extends ComparisonExpression {
        public enum Operator {
            LessThan,
            LessThanOrEqual,
            GreaterThan,
            GreaterThanOrEqual;

            public String getStringValue() {
                if (this == LessThan) {
                    return "less than";
                } else if (this == LessThanOrEqual) {
                    return "less than or equal to";
                } else if (this == GreaterThan) {
                    return "greater than";
                } else if (this == GreaterThanOrEqual) {
                    return "greater than or equal to";
                } else {
                    throw new RuntimeException("Unknown op: " + this);
                }
            }
        }

        private Operator operator;

        public OrdinalExpression() {
            super();
        }

        public Operator getOperator() {
            return operator;
        }

        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        @Override
        public Object evaluate(ChillScriptRuntime runtime) {
            Object lhs = leftHandSide.evaluate(runtime);
            Object rhs = rightHandSide.evaluate(runtime);

            int comparison;
            if (lhs instanceof Comparable) {
                comparison = ((Comparable) lhs).compareTo(rhs);
            } else {
                throw new RuntimeException("Cannot compare " + lhs + " to " + rhs);
            }

            if (operator == Operator.LessThan) {
                return comparison < 0;
            } else if (operator == Operator.LessThanOrEqual) {
                return comparison <= 0;
            } else if (operator == Operator.GreaterThan) {
                return comparison > 0;
            } else if (operator == Operator.GreaterThanOrEqual) {
                return comparison >= 0;
            } else {
                throw new RuntimeException("Unknown op: " + operator);
            }
        }

        @Override
        public String toString() {
            return super.toString() + "[" + operator.getStringValue() + "]";
        }
    }

    public static class SetExpression extends Expression {
        public enum Operator {
            Intersection,
            Union,
            Difference,
            Distinct,
            Differs,
            SymmetricDifference,
            NotContains,
            Contains,
            ContainsAll,
            NotContainsAll, // if left contains all in right
            ContainsAny, // if left contains any in right
            Intersects, // if right contains any in left
            ContainsNone;

            public String getStringValue() {
                if (this == Intersection) return "intersection";
                if (this == Union) return "union";
                if (this == Difference) return "difference";
                if (this == Distinct) return "distinct";
                if (this == Differs) return "differs";
                if (this == SymmetricDifference) return "symmetric-difference";
                if (this == NotContains) return "not-contains";
                if (this == Contains) return "contains";
                if (this == ContainsAll) return "contains-all";
                if (this == ContainsAny) return "contains-any";
                if (this == ContainsNone) return "contains-none";
                throw new RuntimeException("Unknown op: " + this);
            }
        }

        private Expression leftHandleSide;
        private Operator operator;
        private Expression rightHandeSide;

        public SetExpression() {
        }

        public Expression getLeftHandleSide() {
            return leftHandleSide;
        }

        public void setLeftHandleSide(Expression leftHandleSide) {
            this.leftHandleSide = leftHandleSide;
        }

        public Operator getOperator() {
            return operator;
        }

        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        public Expression getRightHandeSide() {
            return rightHandeSide;
        }

        public void setRightHandeSide(Expression rightHandeSide) {
            this.rightHandeSide = rightHandeSide;
        }

        @Override
        public Object evaluate(ChillScriptRuntime runtime) {
            Object lhs = leftHandleSide.evaluate(runtime);
            Object rhs = rightHandeSide.evaluate(runtime);

            if (operator == Operator.Intersects) {
                Iterable<?> left = (Iterable<?>) lhs;
                Iterable<?> right = (Iterable<?>) rhs;

                for (Object leftItem : left) {
                    for (Object rightItem : right) {
                        if (leftItem.equals(rightItem)) {
                            return true;
                        }
                    }
                }
                return false;
            } else if (operator == Operator.Contains) {
                if (rhs instanceof Container right) {
                    return right.contains(lhs);
                } else if (rhs instanceof Iterable<?> right) {
                    for (Object rightItem : right) {
                        if (lhs.equals(rightItem)) {
                            return true;
                        }
                    }
                } else if (rhs instanceof String right) {
                    return right.contains((CharSequence) lhs);
                } else {
                    throw new RuntimeException("Unknown type: " + rhs.getClass());
                }
                return false;
            } else if (operator == Operator.NotContains) {
                Iterable<?> right = (Iterable<?>) rhs;

                for (Object rightItem : right) {
                    if (lhs.equals(rightItem)) {
                        return false;
                    }
                }
                return true;
            } else if (operator == Operator.ContainsAll) {
                Iterable<?> left = (Iterable<?>) lhs;
                Iterable<?> right = (Iterable<?>) rhs;

                // if right contains every item in left
                for (Object leftItem : left) {
                    boolean found = false;
                    for (Object rightItem : right) {
                        if (leftItem.equals(rightItem)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return false;
                    }
                }
                return true;
            } else if (operator == Operator.NotContainsAll) {
                Iterable<?> left = (Iterable<?>) lhs;
                Iterable<?> right = (Iterable<?>) rhs;

                // if right does not contain every item in left
                for (Object leftItem : left) {
                    boolean found = false;
                    for (Object rightItem : right) {
                        if (leftItem.equals(rightItem)) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        return true;
                    }
                }
                return false;
            } else {
                throw new RuntimeException("Unknown op: " + operator);
            }
        }

        @Override
        public String toString() {
            return "SetExpression{" + operator.getStringValue() + '}';
        }
    }

    public static class TraitExpression extends Expression {
        public enum Operator {
            NotDistinct,
            IsDistinct;

            public String getStringValue() {
                if (this == IsDistinct) return "is-distinct";
                if (this == NotDistinct) return "not-distinct";
                throw new RuntimeException("Unknown op: " + this);
            }
        }
        private Expression value;
        private Operator operator;

        public TraitExpression() {}

        public Expression getValue() {
            return value;
        }

        public void setValue(Expression value) {
            this.value = value;
        }

        public Operator getOperator() {
            return operator;
        }

        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        @Override
        public Object evaluate(ChillScriptRuntime runtime) {
            Object value = this.value.evaluate(runtime);

            if (operator == Operator.IsDistinct) {
                Iterable<?> iterable = (Iterable<?>) value;
                Set<Object> set = new java.util.HashSet<>();
                for (Object item : iterable) {
                    if (set.contains(item)) {
                        return false;
                    }
                    set.add(item);
                }
                return true;
            } else {
                throw new RuntimeException("Unknown op: " + operator);
            }
        }

        @Override
        public String toString() {
            return "TraitExpression[" + operator.getStringValue() + "]";
        }
    }

    public static class LogicalExpression extends Expression {
        public enum Operator {
            And,
            Or;

            public String getStringValue() {
                if (this == And) return "and";
                if (this == Or) return "or";
                throw new RuntimeException("Unknown op: " + this);
            }
        }

        private Expression leftHandSide;
        private Operator operator;
        private Expression rightHandSide;

        public LogicalExpression() {}

        public Expression getLeftHandSide() {
            return leftHandSide;
        }

        public void setLeftHandSide(Expression leftHandSide) {
            this.leftHandSide = leftHandSide;
        }

        public Operator getOperator() {
            return operator;
        }

        public void setOperator(Operator operator) {
            this.operator = operator;
        }

        public Expression getRightHandSide() {
            return rightHandSide;
        }

        public void setRightHandSide(Expression rightHandSide) {
            this.rightHandSide = rightHandSide;
        }

        @Override
        public Object evaluate(ChillScriptRuntime runtime) {
            if (operator == Operator.And) {
                return (Boolean) leftHandSide.evaluate(runtime) && (Boolean) rightHandSide.evaluate(runtime);
            } else if (operator == Operator.Or) {
                return (Boolean) leftHandSide.evaluate(runtime) || (Boolean) rightHandSide.evaluate(runtime);
            } else {
                throw new RuntimeException("Unknown op: " + operator);
            }
        }

        @Override
        public String toString() {
            return "LogicalExpression[" + operator.getStringValue() + "]";
        }
    }

    protected Expression leftHandSide;
    protected Expression rightHandSide;

    public ComparisonExpression() {
    }

    public Expression getLeftHandSide() {
        return leftHandSide;
    }

    public void setLeftHandSide(Expression leftHandSide) {
        this.leftHandSide = leftHandSide;
    }

    public Expression getRightHandSide() {
        return rightHandSide;
    }

    public void setRightHandSide(Expression rightHandSide) {
        this.rightHandSide = rightHandSide;
    }


    //==============================================================
    // Implementation
    //==============================================================

    public enum Level {
        Equality,
        Logical,
        Ordinal,
        Collection,
    }

    public static Expression parse(ChillScriptParser parser) {
        return parse(parser, Level.Equality);
    }

    public static Expression parse(ChillScriptParser parser, Level level) {
        Expression expression;
        if (level == Level.Equality) {
            expression = parser.parse("logicalExpression");
        } else if (level == Level.Logical) {
            expression = parser.parse("ordinalExpression");
        } else if (level == Level.Ordinal) {
            expression = parser.parse("collectionExpression");
        } else if (level == Level.Collection) {
            expression = parser.parse("additiveExpression");
        } else {
            throw new RuntimeException("Unknown level: " + level);
        }

        while (true) {
            Expression initial = expression;
            if (parser.matchAndConsume("starts")) {
                StartsWithExpression startsWithExpression = new StartsWithExpression();
                startsWithExpression.setStart(expression.getStart());
                startsWithExpression.setTarget(expression);

                parser.require("with", startsWithExpression, "Expected 'with'");

                Expression content = parser.requireExpression(startsWithExpression, "expression");
                startsWithExpression.setContent(content);
                startsWithExpression.setEnd(content.getEnd());

                return startsWithExpression;
            } else if (parser.matchAndConsume("ends")) {
                // todo: implement
            } else if (parser.matchAndConsume("is")) {
                boolean not = parser.matchAndConsume("not");
                if (level == Level.Collection) {
                    if (parser.matchAndConsume("in")) {
                        SetExpression.Operator operator;

                        if (not)
                            operator = SetExpression.Operator.NotContains;
                        else
                            operator = SetExpression.Operator.Contains;

                        expression = parseCollectionExpression(parser, level, expression, new OrdinalExpression.SetExpression(),
                                operator);
                    } else if (parser.matchAndConsume("distinct")) {
                        TraitExpression traitExpression = new TraitExpression();
                        if (not)
                            traitExpression.setOperator(TraitExpression.Operator.NotDistinct);
                        else
                            traitExpression.setOperator(TraitExpression.Operator.IsDistinct);
                        traitExpression.setValue(expression);
                        return traitExpression;
                    }
                } else if (level == Level.Ordinal) {
                    if (parser.match("less", "smaller")) {
                        parser.consumeToken();
                        OrdinalExpression ordinalExpression = new OrdinalExpression();
                        parser.require("than", ordinalExpression, "Expected 'than'");

                        OrdinalExpression.Operator op;
                        if (parser.matchAndConsume("or")) {
                            if (!(parser.matchAndConsume("equal") || parser.matchAndConsume("equivalent"))) {
                                ordinalExpression.addError(ErrorType.UNEXPECTED_TOKEN, "Expected 'equal to'");
                            }
                            parser.require("to", ordinalExpression, "Expected 'equal to'");
                            op = OrdinalExpression.Operator.LessThanOrEqual;
                        } else {
                            op = OrdinalExpression.Operator.LessThan;
                        }

                        expression = parseOrdinalExpression(parser, level, expression, ordinalExpression, op);
                    } else if (parser.match("greater", "larger")) {
                        parser.consumeToken();
                        OrdinalExpression ordinalExpression = new OrdinalExpression();
                        parser.require("than", ordinalExpression, "Expected 'than'");

                        OrdinalExpression.Operator op;
                        if (parser.matchAndConsume("or")) {
                            if (!(parser.matchAndConsume("equal") || parser.matchAndConsume("equivalent"))) {
                                ordinalExpression.addError(ErrorType.UNEXPECTED_TOKEN, "Expected 'equal to'");
                            }
                            parser.require("to", ordinalExpression, "Expected 'equal to'");
                            op = OrdinalExpression.Operator.GreaterThanOrEqual;
                        } else {
                            op = OrdinalExpression.Operator.GreaterThan;
                        }

                        expression = parseOrdinalExpression(parser, level, expression, ordinalExpression, op);
                    }
                } else if (level == Level.Logical) {
                } else if (level == Level.Equality) {
                    if (parser.match("equal", "similar")) {
                        parser.consumeToken();

                        EqualityExpression equalityExpression = new EqualityExpression();
                        parser.require("to", equalityExpression, "Expected 'to'");

                        EqualityExpression.Operator operator;
                        if (not)
                            operator = EqualityExpression.Operator.NotEquals;
                        else
                            operator = EqualityExpression.Operator.Equals;

                        expression = parseEqualityExpression(parser, level, expression, equalityExpression, operator);
                    } else if (parser.matchAndConsume("like")) {
                        EqualityExpression equalityExpression = new EqualityExpression();

                        EqualityExpression.Operator operator;
                        if (not)
                            operator = EqualityExpression.Operator.NotEquals;
                        else
                            operator = EqualityExpression.Operator.Equals;

                        expression = parseEqualityExpression(parser, level, expression, equalityExpression, operator);
                    } else if (parser.matchAndConsume("unlike")) {
                        EqualityExpression equalityExpression = new EqualityExpression();

                        EqualityExpression.Operator operator;
                        if (not)
                            operator = EqualityExpression.Operator.Equals;
                        else
                            operator = EqualityExpression.Operator.NotEquals;

                        expression = parseEqualityExpression(parser, level, expression, equalityExpression, operator);
                    } else {
                        EqualityExpression.Operator operator;
                        if (not)
                            operator = EqualityExpression.Operator.NotEquals;
                        else
                            operator = EqualityExpression.Operator.Equals;

                        expression = parseEqualityExpression(parser, level, expression, new EqualityExpression(), operator);
                    }
                } else {
                    throw new RuntimeException("unreachable");
                }

                if (System.identityHashCode(initial) == System.identityHashCode(expression)) {
                    if (not) parser.produceToken();
                    parser.produceToken();
                    return expression;
                }
            } else if (parser.matchAndConsume("are")) {
                boolean not = parser.matchAndConsume("not");

                if (parser.matchAndConsume("all")) {
                    SetExpression setExpression = new SetExpression();
                    parser.require("in", setExpression, "Expected 'of'");

                    SetExpression.Operator operator;
                    if (not)
                        operator = SetExpression.Operator.NotContainsAll;
                    else
                        operator = SetExpression.Operator.ContainsAll;

                    expression = parseCollectionExpression(parser, level, expression, setExpression, operator);
                } else {
                    throw new RuntimeException("Not implemented yet: are [not] " + parser.currentToken().getStringValue() + " ...");
                }
            } else if (parser.match("equals", "like")) {
                if (level != Level.Equality) {
                    return expression;
                }
                parser.consumeToken();

                expression = parseEqualityExpression(parser, level, expression, new EqualityExpression(), EqualityExpression.Operator.Equals);
            } else if (parser.match("less", "smaller")) {
                if (level != Level.Ordinal) {
                    return expression;
                }
                parser.consumeToken();
                OrdinalExpression ordinalExpression = new OrdinalExpression();
                parser.require("than", ordinalExpression, "Expected 'than'");

                OrdinalExpression.Operator op;
                if (parser.matchAndConsume("or")) {
                    if (!(parser.matchAndConsume("equal") || parser.matchAndConsume("equivalent"))) {
                        ordinalExpression.addError(ErrorType.UNEXPECTED_TOKEN, "Expected 'equal to'");
                    }
                    parser.require("to", ordinalExpression, "Expected 'equal to'");
                    op = OrdinalExpression.Operator.LessThanOrEqual;
                } else {
                    op = OrdinalExpression.Operator.LessThan;
                }

                expression = parseOrdinalExpression(parser, level, expression, ordinalExpression, op);
            } else if (parser.match("greater") || parser.match("larger")) {
                if (level != Level.Ordinal) {
                    return expression;
                }
                parser.consumeToken();
                OrdinalExpression ordinalExpression = new OrdinalExpression();
                parser.require("than", ordinalExpression, "Expected 'than'");
                OrdinalExpression.Operator op;
                if (parser.matchAndConsume("or")) {
                    if (!(parser.matchAndConsume("equal") || parser.matchAndConsume("equivalent"))) {
                        ordinalExpression.addError(ErrorType.UNEXPECTED_TOKEN, "Expected 'equal to'");
                    }
                    parser.require("to", ordinalExpression, "Expected 'equal to'");
                    op = OrdinalExpression.Operator.GreaterThanOrEqual;
                } else {
                    op = OrdinalExpression.Operator.GreaterThan;
                }

                expression = parseOrdinalExpression(parser, level, expression, ordinalExpression, op);
            } else if (parser.matchAndConsume("intersects")) {
                expression = parseCollectionExpression(parser, level, expression, new OrdinalExpression.SetExpression(),
                        SetExpression.Operator.Intersects);
            } else if (parser.matchAndConsume("does")) {
                if (level == Level.Equality) {
                    boolean not = parser.matchAndConsume("not");

                    if (parser.matchAndConsume("equal")) {
                        EqualityExpression.Operator operator;
                        if (not)
                            operator = EqualityExpression.Operator.NotEquals;
                        else
                            operator = EqualityExpression.Operator.Equals;

                        expression = parseEqualityExpression(parser, level, expression, new EqualityExpression(), operator);
                    } else {
                        throw new RuntimeException("Not implemented yet: does [not] " + parser.currentToken().getStringValue() + " ...");
                    }
                }

                if (System.identityHashCode(initial) == System.identityHashCode(expression)) {
                    parser.produceToken();
                    return expression;
                }
            } else {
                boolean not = parser.matchAndConsume("not");

                if (parser.matchAndConsume("equal")) {
                    EqualityExpression equalityExpression = new EqualityExpression();
                    parser.require("to", equalityExpression, "Expected 'to'");
                    equalityExpression.setStart(expression.getStart());
                    equalityExpression.setLeftHandSide(expression);
                    if (not)
                        equalityExpression.setOperator(EqualityExpression.Operator.NotEquals);
                    else
                        equalityExpression.setOperator(EqualityExpression.Operator.Equals);
                    expression = equalityExpression;
                } else if (parser.matchAndConsume("in")) {
                    OrdinalExpression.SetExpression setExpression = new OrdinalExpression.SetExpression();

                    SetExpression.Operator operator;
                    if (not)
                        operator = SetExpression.Operator.NotContains;
                    else
                        operator = SetExpression.Operator.Contains;

                    expression = parseCollectionExpression(parser, level, expression, setExpression, operator);
                } else {
                    return expression;
                }
            }
        }
    }

    private static Expression parseCollectionExpression(ChillScriptParser parser, Level level, Expression expression, OrdinalExpression.SetExpression setExpression, OrdinalExpression.SetExpression.Operator operator) {
        if (level.ordinal() != Level.Collection.ordinal()) {
            throw new RuntimeException("collection expressions cannot exist at this level!");
        }

        setExpression.setStart(expression.getStart());
        setExpression.setLeftHandleSide(expression);
        setExpression.setOperator(operator);

        Expression rhs = parser.parse("additiveExpression");
        setExpression.setRightHandeSide(rhs);
        setExpression.setEnd(rhs.getEnd());
        return setExpression;
    }

    private static Expression parseOrdinalExpression(ChillScriptParser parser, Level level, Expression expression, OrdinalExpression ordinalExpression, OrdinalExpression.Operator op) {
        if (level.ordinal() != Level.Ordinal.ordinal()) {
            throw new RuntimeException("ordinal expressions (<=, <, >=, >) cannot exist at this level!");
        }

        ordinalExpression.setStart(expression.getStart());
        ordinalExpression.setLeftHandSide(expression);
        ordinalExpression.setOperator(op);

        Expression rhs = parse(parser, Level.Logical);
        ordinalExpression.setRightHandSide(rhs);
        ordinalExpression.setEnd(rhs.getEnd());
        return ordinalExpression;
    }

    private static Expression parseEqualityExpression(ChillScriptParser parser, Level level, Expression expression, EqualityExpression equalityExpression, EqualityExpression.Operator operator) {
        if (level != Level.Equality) {
            throw new RuntimeException("equality expressions cannot exist at this level!");
        }

        equalityExpression.setStart(expression.getStart());
        equalityExpression.setLeftHandSide(expression);
        equalityExpression.setOperator(operator);

        Expression rhs = parse(parser, Level.Logical);
        equalityExpression.setRightHandSide(rhs);
        equalityExpression.setEnd(rhs.getEnd());
        return equalityExpression;
    }

    public static class StartsWithExpression extends Expression {

        private Expression target;
        private Expression content;

        public void setTarget(Expression expression) {
            this.target = expression;
        }

        public void setContent(Expression expression) {
            this.content = expression;
        }

        @Override
        public Object evaluate(ChillScriptRuntime runtime) {
            Object targetVal = target.evaluate(runtime);
            if (targetVal != null) {
                Object contentVal = content.evaluate(runtime);
                if (contentVal != null) {
                    String targetValAsString = String.valueOf(targetVal);
                    return targetValAsString.startsWith(String.valueOf(contentVal));
                }
            }
            return false;
        }
    }
}
