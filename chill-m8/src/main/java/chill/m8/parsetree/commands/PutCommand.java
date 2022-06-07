package chill.m8.parsetree.commands;

import chill.script.commands.Command;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import org.openqa.selenium.WebElement;


public class PutCommand extends Command {

    private Expression value;
    private Expression target;

    public void setValue(Expression value) {
        this.value = addChild(value);
    }

    public void setTarget(Expression target) {
        this.target = addChild(target);
    }

    @Override
    public void execInternal(ChillScriptRuntime runtime) {
        Object value = this.value.evaluate(runtime);
        Object target = this.target.evaluate(runtime);
        if (target instanceof WebElement) {
            ((WebElement) target).sendKeys(String.valueOf(value));
        } else {
            throw new RuntimeException("Don't know how to put into value " + target);
        }
    }

    public static Command parse(ChillScriptParser parser) {
        if (parser.match("put")) {
            PutCommand putCommand = new PutCommand();
            putCommand.setStart(parser.consumeToken());
            putCommand.setValue(parser.requireExpression(putCommand, "expression"));
            parser.require("into", putCommand, "Expected an 'into'");
            putCommand.setTarget(parser.requireExpression(putCommand, "expression"));
            return putCommand;
        }
        return null;
    }
}
