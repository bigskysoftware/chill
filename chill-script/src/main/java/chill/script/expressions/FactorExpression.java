package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.runtime.op.Div;
import chill.script.runtime.op.Mul;
import chill.script.runtime.op.Rem;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

import java.math.BigDecimal;

public class FactorExpression extends Expression {
    private Expression leftHandeSide;
    private Token operator;
    private Expression rightHandeSide;

    public FactorExpression() {}

    public Expression getLeftHandeSide() {
        return leftHandeSide;
    }

    public void setLeftHandSide(Expression leftHandeSide) {
        this.leftHandeSide = leftHandeSide;
    }

    public Token getOperator() {
        return operator;
    }

    public void setOperator(Token operator) {
        this.operator = operator;
    }

    public Expression getRightHandeSide() {
        return rightHandeSide;
    }

    public void setRightHandSide(Expression rightHandeSide) {
        this.rightHandeSide = rightHandeSide;
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Object lhs = leftHandeSide.evaluate(runtime);
        Object rhs = rightHandeSide.evaluate(runtime);
        if (operator.getType() == TokenType.STAR) {
            if (lhs instanceof BigDecimal left && rhs instanceof BigDecimal right) {
                return left.multiply(right);
            } else if (lhs instanceof Mul left) {
                return left.mul(runtime, rhs);
            } else {
                throw new RuntimeException("Cannot perform multiplication on non-numbers");
            }
        } else if (operator.getType() == TokenType.SLASH) {
            if (lhs instanceof BigDecimal left && rhs instanceof BigDecimal right) {
                return left.divide(right);
            } else if (lhs instanceof Div left) {
                return left.div(runtime, rhs);
            } else {
                throw new RuntimeException("Cannot perform division on non-numbers");
            }
        } else if (operator.getType() == TokenType.PERCENT) {
            if (lhs instanceof BigDecimal left && rhs instanceof BigDecimal right) {
                return left.remainder(right);
            } else if (lhs instanceof Rem left) {
                return left.rem(runtime, rhs);
            } else {
                throw new RuntimeException("Cannot perform modulo on non-numbers");
            }
        } else {
            throw new RuntimeException("Unknown operator: " + operator);
        }
    }

    public static Expression parse(ChillScriptParser parser) {
        Expression expression = parser.parse("unaryExpression");
        while (parser.match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token operator = parser.consumeToken();
            final Expression rightHandSide = parser.parse("unaryExpression");
            final FactorExpression factorExpression = new FactorExpression();
            factorExpression.setLeftHandSide(expression);
            factorExpression.setOperator(operator);
            factorExpression.setRightHandSide(rightHandSide);
            expression = factorExpression;
        }
        return expression;
    }
}
