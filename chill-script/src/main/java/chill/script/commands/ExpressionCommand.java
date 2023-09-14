package chill.script.commands;

import chill.script.expressions.Expression;
import chill.script.runtime.ChillScriptRuntime;

public class ExpressionCommand extends Command {
    final Expression expression;

    public ExpressionCommand(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(ChillScriptRuntime runtime) {
        expression.evaluate(runtime);
    }
}
