package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.parser.ErrorType;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;

public class LogicalExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public LogicalExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
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

    public boolean isAnd() {
        return operator.getStringValue().equals("and");
    }


    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Object lhsValue = leftHandSide.evaluate(runtime);
        if (isAnd()) {
            return runtime.isTruthy(lhsValue) && runtime.isTruthy(rightHandSide.evaluate(runtime));
        } else {
            return runtime.isTruthy(lhsValue) || runtime.isTruthy(rightHandSide.evaluate(runtime));
        }
    }

    public static Expression parse(ChillScriptParser parser) {
        Expression expression = parser.parse("comparisonExpression");
        Token firstOperator = null;
        while (parser.match("and") || parser.match("or")) {
            Token operator = parser.consumeToken();
            if (firstOperator == null) {
                firstOperator = operator;
            } else if (!firstOperator.getStringValue().equals(operator.getStringValue())) {
                expression.addError(ErrorType.MUST_PARENTHESIZE);
            }
            final Expression rightHandSide = parser.parse("comparisonExpression");
            LogicalExpression equalityExpression = new LogicalExpression(operator, expression, rightHandSide);
            equalityExpression.setStart(expression.getStart());
            equalityExpression.setEnd(rightHandSide.getEnd());
            expression = equalityExpression;
        }
        return expression;
    }
}
