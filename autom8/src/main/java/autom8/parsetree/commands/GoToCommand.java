package autom8.parsetree.commands;

import autom8.runtime.Autom8Runtime;
import chill.script.commands.Command;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import org.openqa.selenium.WebDriver;

public class GoToCommand extends Command {

    private Expression destination;

    public GoToCommand() {
    }

    private void setDestination(Expression expression) {
        this.destination = addChild(expression);
    }

    @Override
    public void execInternal(ChillScriptRuntime runtime) {
        Autom8Runtime chillTests = (Autom8Runtime) runtime;
        WebDriver driver = chillTests.getDriver();
        driver.get(String.valueOf(destination.evaluate(runtime)));
    }

    public static Command parse(ChillScriptParser parser) {
        if (parser.match("go")) {
            GoToCommand goToCommand = new GoToCommand();
            goToCommand.setStart(parser.consumeToken());
            parser.matchAndConsume("to"); // optional to
            goToCommand.setDestination(parser.requireExpression(goToCommand, "expression"));
            return goToCommand;
        }
        return null;
    }
}
