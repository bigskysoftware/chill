package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

import java.util.Objects;

public class ComparisonExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public ComparisonExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
        this.leftHandSide = addChild(leftHandSide);
        this.rightHandSide = addChild(rightHandSide);
        this.operator = operator;
    }

    public Expression getLeftHandSide() {
        return leftHandSide;
    }

    public Expression getRightHandSide() {
        return rightHandSide;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    public boolean isEqual() {
        return operator.getType().equals(TokenType.EQUAL_EQUAL);
    }


    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Object lhsValue = leftHandSide.evaluate(runtime);
        Object rhsValue = rightHandSide.evaluate(runtime);
        if (isEqual()) {
            return Objects.equals(lhsValue, rhsValue);
        } else {
            return !Objects.equals(lhsValue, rhsValue);
        }
    }

    public static Expression parse(ChillScriptParser parser) {
        Expression expression = parser.parse("additiveExpression");
        if (parser.matchAndConsume("starts")) {
            StartsWithExpression startsWithExpression = new StartsWithExpression();
            startsWithExpression.setStart(expression.getStart());
            startsWithExpression.setTarget(expression);
            parser.require("with", startsWithExpression, "Expected 'with'");
            startsWithExpression.setContent(parser.requireExpression(startsWithExpression, "expression"));
            return startsWithExpression;
        }
        // TODO implement
//        while (parser.match(TokenType.LESS, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.GREATER_EQUAL)) {
//            Token operator = parser.consumeToken();
//            final Expression rightHandSide = parser.parse("additiveExpression");
//            ComparisonExpression equalityExpression = new ComparisonExpression(operator, expression, rightHandSide);
//            equalityExpression.setStart(expression.getStart());
//            equalityExpression.setEnd(rightHandSide.getEnd());
//            expression = equalityExpression;
//        }
        return expression;
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
