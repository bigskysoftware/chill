package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.runtime.op.Negate;
import chill.script.runtime.op.Not;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

import java.math.BigDecimal;

public class UnaryExpression extends Expression {

    public static final BigDecimal NEGATIVE_ONE = new BigDecimal("-1");
    private final Token operator;
    private final Expression rightHandSide;

    public UnaryExpression(Token operator, Expression rightHandSide) {
        this.rightHandSide = addChild(rightHandSide);
        this.operator = operator;
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
        Object rhsValue = rightHandSide.evaluate(runtime);
        if (operator.getStringValue().equals("-")) {
            if (rhsValue instanceof BigDecimal rvalue) {
                return rvalue.multiply(NEGATIVE_ONE);
            } else if (rhsValue instanceof Negate negate) {
                return negate.negate(runtime);
            } else {
                var bigDecimal = new BigDecimal(rhsValue.toString());
                return bigDecimal.multiply(NEGATIVE_ONE);
            }
        } else if (operator.getStringValue().equals("not")) {
            if (rhsValue instanceof Boolean rvalue) {
                return !rvalue;
            } else if (rhsValue instanceof Not rvalue) {
                return rvalue.not(runtime);
            } else {
                throw new UnsupportedOperationException("not operator only works on booleans");
            }
        }
        throw new UnsupportedOperationException(operator.getStringValue() + " not implemented for this type yet");
    }

    public static Expression parse(ChillScriptParser parser) {
        parser.matchAndConsume("the"); // optional 'the'
        if (parser.match(TokenType.MINUS) || parser.match("not")) {
            Token operator = parser.consumeToken();
            final Expression rightHandSide = parser.parse("unaryExpression");
            var unaryExpr = new UnaryExpression(operator, rightHandSide);
            unaryExpr.setEnd(rightHandSide.getEnd());
            return unaryExpr;
        } else {
            return parser.parse("indirectExpression");
        }
    }
}
