package chill.script.commands;

import chill.script.expressions.Expression;
import chill.script.expressions.IdentifierExpression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;

public class SetCommand extends Command {

    Expression value;
    private IdentifierExpression symbol;

    @Override
    public void execute(ChillScriptRuntime runtime) {
        runtime.setSymbolValue(symbol.getName(), value.evaluate(runtime));
    }

    public static Command parse(ChillScriptParser parser) {
        if (parser.match("set")) {
            SetCommand setCommand = new SetCommand();
            setCommand.setStart(parser.consumeToken());
            setCommand.setSymbol((IdentifierExpression) parser.requireExpression(setCommand, "identifier"));
            parser.require("to", setCommand, "Expected a 'to'");
            setCommand.setValue(parser.requireExpression(setCommand, "expression"));
            setCommand.setEnd(parser.lastMatch());
            return setCommand;
        }
        return null;
    }

    private void setSymbol(IdentifierExpression identifier) {
        this.symbol = identifier;
    }

    private void setValue(Expression expression) {
        value = addChild(expression);
    }
}
