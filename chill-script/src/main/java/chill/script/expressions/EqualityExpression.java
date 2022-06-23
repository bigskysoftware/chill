package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

import java.util.Objects;

public class EqualityExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public EqualityExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
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
        return operator.getType().equals(TokenType.EQUAL_EQUAL) || operator.getStringValue().equals("is");
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
        Expression expression = parser.parse("logicalExpression");
        while (parser.match(TokenType.EQUAL_EQUAL, TokenType.BANG_EQUAL) || parser.match("is")) {
            Token operator = parser.consumeToken();
            final Expression rightHandSide = parser.parse("logicalExpression");
            EqualityExpression equalityExpression = new EqualityExpression(operator, expression, rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }
}
