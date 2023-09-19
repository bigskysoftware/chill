package autom8.parsetree.commands;

import autom8.runtime.AssertionException;
import chill.script.commands.Command;
import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;


public class ExpectCommand extends Command {

    private Expression condition;

    public void setCondition(Expression target) {
        this.condition = addChild(target);
    }

    @Override
    public void execute(ChillScriptRuntime runtime) {
        Object condition = this.condition.evaluate(runtime);

        if (!(boolean) condition) {
            throw new AssertionException("Expectation failed");
        }
    }

    public static Command parse(ChillScriptParser parser) {
        if (parser.match("expect")) {
            ExpectCommand expectCommand = new ExpectCommand();
            expectCommand.setStart(parser.consumeToken());
            parser.matchAndConsume("that"); // optional that
            Expression expression = parser.parse("expression");
            expectCommand.addChild(expression);
            expectCommand.setCondition(expression);
            expectCommand.setEnd(expression.getEnd());
            return expectCommand;
        }
        return null;
    }
}
