package chill.script.expressions;

import chill.script.commands.Command;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;

import java.util.List;

/*
let a be 3
let b be 4

let larger = if a is larger than b then a else b
 */
public class IfExpression extends Expression {
    Expression condition;
    List<Command> trueBranch;
    Expression falseBranch;

    public IfExpression() {}

    public Expression getCondition() {
        return condition;
    }

    public void setCondition(Expression condition) {
        this.condition = condition;
    }

    public List<Command> getTrueBranch() {
        return trueBranch;
    }

    public void setTrueBranch(List<Command> trueBranch) {
        this.trueBranch = trueBranch;
    }

    public Expression getFalseBranch() {
        return falseBranch;
    }

    public void setFalseBranch(Expression falseBranch) {
        this.falseBranch = falseBranch;
    }

    @Override
    public Object evaluate(ChillScriptRuntime runtime) {
        Boolean conditionValue = (Boolean) condition.evaluate(runtime);
        if (conditionValue) {
            Object result;
            for (var cmd : trueBranch) {
                cmd.execute(runtime);
            }
        } else {
            if (falseBranch != null) falseBranch.evaluate(runtime);
        }
    }

    public static IfExpression parse(ChillScriptParser parser) {
        if (!parser.matchAndConsume("if")) {
            return null;
        }

        IfExpression ifExpression = new IfExpression();

        var condition = parser.parse("equalityExpression");
        ifExpression.setCondition(condition);

        parser.require("then", ifExpression, "if statement does not have a then");

        ifExpression.setTrueBranch(parser.parseCommandList("else", "end"));

        if (parser.matchAndConsume("else")) {
            ifExpression.setFalseBranch(parser.parse("equalityExpression"));
        } else {
            parser.require("end", ifExpression, "if statement does not end");
        }

        return ifExpression;
    }
}
