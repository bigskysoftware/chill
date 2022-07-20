package chill.script.commands;

import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.parser.ParseElement;
import chill.script.runtime.ChillScriptRuntime;

public class PrintCommand extends Command {

    Expression value;

    @Override
    public void execute(ChillScriptRuntime runtime) {
        runtime.print(value.evaluate(runtime));
    }

    public static Command parse(ChillScriptParser parser) {
        if (parser.match("print")) {
            PrintCommand printCommand = new PrintCommand();
            printCommand.setStart(parser.consumeToken());
            printCommand.setValue(parser.requireExpression(printCommand, "expression"));
            printCommand.setEnd(parser.lastMatch());
            return printCommand;
        }
        return null;
    }

    private void setValue(Expression expression) {
        value = addChild(expression);
    }
}
