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
        if (parser.match("print", "println")) {
            PrintCommand printCommand = new PrintCommand();
            printCommand.setStart(parser.consumeToken());
            Expression expression = parser.requireExpression(printCommand, "expression");
            if (printCommand.getStart().getStringValue().equals("println")) {
                expression = new Newline(expression);
            }
            printCommand.setValue(expression);
            printCommand.setEnd(parser.lastMatch());
            return printCommand;
        }
        return null;
    }

    private void setValue(Expression expression) {
        value = addChild(expression);
    }

    private static final class Newline extends Expression {
        Expression value;

        public Newline(Expression value) {
            this.value = addChild(value);
            this.setStart(value.getStart());
            this.setEnd(value.getEnd());
        }

        @Override
        public Object evaluate(ChillScriptRuntime runtime) {
            return String.valueOf(value.evaluate(runtime)) + "\n";
        }
        @Override
        public String toString() {
            return "(" + value + " + '\\n')";
        }
    }
}
