package chill.m8.parsetree.commands;

import chill.script.commands.Command;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;

import java.math.BigDecimal;

public class WaitCommand extends Command {

    private Expression value;

    @Override
    public void execInternal(ChillScriptRuntime runtime) {
        try {
            BigDecimal val = (BigDecimal) value.evaluate(runtime);
            Thread.sleep(val.intValue());
        } catch (InterruptedException e) {
            runtime.handleException(this, e);
        }
    }

    private void setValue(Expression value) {
        this.value = addChild(value);
    }

    public static Command parse(ChillScriptParser parser) {
        if (parser.match("wait")) {
            WaitCommand waitCommand = new WaitCommand();
            waitCommand.setStart(parser.consumeToken());
            waitCommand.setValue(parser.parse("expression"));
            parser.require("seconds", waitCommand, "Expected an 'seconds'");
            waitCommand.setEnd(parser.lastMatch());
            return waitCommand;
        }
        return null;
    }
}
