package autom8.parsetree.commands;

import chill.script.commands.Command;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import org.openqa.selenium.WebElement;


public class ClickCommand extends Command {

    private Expression target;

    public void setTarget(Expression target) {
        this.target = addChild(target);
    }

    @Override
    public void execInternal(ChillScriptRuntime runtime) {
        Object target = this.target.evaluate(runtime);
        if (target instanceof WebElement) {
            ((WebElement) target).click();
        } else {
            throw new RuntimeException("Don't know how to click value " + target);
        }
    }

    public static Command parse(ChillScriptParser parser) {
        if (parser.match("click")) {
            ClickCommand clickCommand = new ClickCommand();
            clickCommand.setStart(parser.consumeToken());
            clickCommand.setTarget(parser.requireExpression(clickCommand, "expression"));
            clickCommand.setEnd(parser.lastMatch());
            return clickCommand;
        }
        return null;
    }
}
