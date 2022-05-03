package chill.script.expressions;

import chill.script.runtime.ChillScriptRuntime;
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
        if (lhsValue instanceof BigDecimal && rhsValue instanceof BigDecimal) {
            var lhsBD = (BigDecimal) lhsValue;
            var rhsBD = (BigDecimal) rhsValue;
            return lhsBD.add(rhsBD);
        } else {
            return String.valueOf(lhsValue) + String.valueOf(rhsValue);
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
