package chill.script.expressions;

import chill.script.runtime.ChillScriptRuntime;

import java.util.Objects;

public class EqualityExpression extends Expression {
    public enum Operator {
        Equals,
        NotEquals;

        public String getStringValue() {
            if (this == Equals) {
                return "==";
            } else if (this == NotEquals) {
                return "!=";
            } else {
                throw new RuntimeException("Unknown op: " + this);
            }
        }
    }

    private Operator operator;
    private Expression leftHandSide;
    private Expression rightHandSide;

    public EqualityExpression() {}

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

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(Operator operator) {
        this.operator = operator;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    public boolean isEqual() {
        return operator == Operator.Equals;
    }


    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Object lhsValue = leftHandSide.evaluate(runtime);
        Object rhsValue = rightHandSide.evaluate(runtime);
        boolean isEqual = Objects.equals(lhsValue, rhsValue);
        if (isEqual()) {
            return isEqual;
        } else {
            return !isEqual;
        }
    }
}
