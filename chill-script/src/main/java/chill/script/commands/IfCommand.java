package chill.script.commands;

import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class IfCommand extends Command {
    Expression condition;
    List<Command> trueBranch;
    List<Command> falseBranch;

    public static Command parse(ChillScriptParser parser) {
        if (parser.match("if")) {
            var rv = new IfCommand();
            rv.setStart(parser.consumeToken());

            rv.setCondition(parser.requireExpression(rv, "expression"));

            var trueBranch = parser.parseCommandList("else", "end");
            rv.setTrueBranch(trueBranch);

            if (parser.matchAndConsume("else")) {
                var falseBranch = parser.parseCommandList("end");
                rv.setFalseBranch(falseBranch);
            }

            parser.require("end", rv, "'if' statement does not end");

            rv.setEnd(parser.consumeToken());
            return rv;
        }
        return null;
    }

    @Override
    public void execute(ChillScriptRuntime context) {
        var conditionValue = condition.evaluate(context);
        if (context.isTruthy(conditionValue)) {
            for (var cmd : trueBranch) cmd.execute(context);
        } else {
            if (falseBranch != null) for (var cmd : falseBranch) cmd.execute(context);
        }
    }

    private void setCondition(Expression cond) { condition = addChild(cond); }

    private void setTrueBranch(List<Command> branch) { trueBranch = addChildren(branch); }

    private void setFalseBranch(List<Command> branch) { falseBranch = addChildren(branch); }
}
