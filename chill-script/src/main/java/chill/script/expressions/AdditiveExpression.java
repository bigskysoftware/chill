package chill.script.expressions;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.runtime.op.Add;
import chill.script.runtime.op.Sub;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;
import chill.script.parser.ChillScriptParser;

import java.math.BigDecimal;

public class AdditiveExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public AdditiveExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
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

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Object lhsValue = leftHandSide.evaluate(runtime);
        Object rhsValue = rightHandSide.evaluate(runtime);
        if (operator.getType() == TokenType.PLUS) {
            if (lhsValue instanceof String || rhsValue instanceof String) {
                return lhsValue.toString() + rhsValue.toString();
            } else if (lhsValue instanceof BigDecimal || rhsValue instanceof BigDecimal) {
                return new BigDecimal(lhsValue.toString()).add(new BigDecimal(rhsValue.toString()));
            } else if (lhsValue instanceof Integer || rhsValue instanceof Integer) {
                return Integer.parseInt(lhsValue.toString()) + Integer.parseInt(rhsValue.toString());
            } else if (lhsValue instanceof Double || rhsValue instanceof Double) {
                return Double.parseDouble(lhsValue.toString()) + Double.parseDouble(rhsValue.toString());
            } else if (lhsValue instanceof Add add) {
                return add.add(runtime, rhsValue);
            } else {
                throw new UnsupportedOperationException("Cannot add " + lhsValue.getClass().getName() + " and " + rhsValue.getClass().getName());
            }
        } else {
            if (lhsValue instanceof BigDecimal || rhsValue instanceof BigDecimal) {
                return new BigDecimal(lhsValue.toString()).subtract(new BigDecimal(rhsValue.toString()));
            } else if (lhsValue instanceof Integer || rhsValue instanceof Integer) {
                return Integer.parseInt(lhsValue.toString()) - Integer.parseInt(rhsValue.toString());
            } else if (lhsValue instanceof Double || rhsValue instanceof Double) {
                return Double.parseDouble(lhsValue.toString()) - Double.parseDouble(rhsValue.toString());
            } else if (lhsValue instanceof Sub sub) {
                return sub.sub(runtime, rhsValue);
            } else {
                throw new UnsupportedOperationException("Cannot subtract " + lhsValue.getClass().getName() + " and " + rhsValue.getClass().getName());
            }
        }
    }

    public static Expression parse(ChillScriptParser chillScriptParser) {
        Expression leftHandSide = chillScriptParser.parse("factorExpression");
        while (chillScriptParser.match(TokenType.PLUS, TokenType.MINUS)) {
            Token operator = chillScriptParser.consumeToken();
            final Expression rightHandSide = chillScriptParser.parse("factorExpression");
            var additiveExpression = new AdditiveExpression(operator, leftHandSide, rightHandSide);
            additiveExpression.setStart(leftHandSide.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            leftHandSide = additiveExpression;
        }
        return leftHandSide;
    }
}
