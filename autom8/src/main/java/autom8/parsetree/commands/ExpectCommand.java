package autom8.parsetree.commands;

import autom8.runtime.AssertionException;
import chill.script.commands.Command;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import org.openqa.selenium.WebElement;


public class ExpectCommand extends Command {

    private Expression target;
    private Expression value;

    public void setTarget(Expression target) {
        this.target = addChild(target);
    }

    public void setValue(Expression value) {
        this.value = addChild(value);
    }

    @Override
    public void execute(ChillScriptRuntime runtime) {
        Object value = this.value.evaluate(runtime);
        Object target = this.target.evaluate(runtime);
        if (target instanceof WebElement) {
            String strValue = String.valueOf(value);
            boolean contains = ((WebElement) target).getText().contains(strValue);
            if (!contains) {
                throw new AssertionException("Could not find " + strValue + " in " + target);
            }
        } else {
            throw new RuntimeException("Don't know how to get text of value " + target);
        }
    }

    public static Command parse(ChillScriptParser parser) {
        if (parser.matchAndConsume("expect")) {
            ExpectCommand expectCommand = new ExpectCommand();
            expectCommand.setStart(parser.consumeToken());
            parser.matchAndConsume("that"); // optional that
            expectCommand.setValue(parser.requireExpression(expectCommand,"expression"));
            parser.require("is", expectCommand, "Expected an 'is'");
            parser.require("in", expectCommand, "Expected an 'in'");
            expectCommand.setTarget(parser.requireExpression(expectCommand, "expression"));
            return expectCommand;
        }
        return null;
    }
}
