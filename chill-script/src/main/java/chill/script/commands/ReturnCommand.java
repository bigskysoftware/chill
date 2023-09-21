package chill.script.commands;

import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.runtime.ReturnInterrupt;
import chill.script.tokenizer.Token;

public class ReturnCommand extends Command {
    private Expression expression;

    public ReturnCommand() {}

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    @Override
    public void execute(ChillScriptRuntime runtime) {
        if (expression != null) {
            Object value = expression.evaluate(runtime);
            throw new ReturnInterrupt(value);
        } else {
            throw new ReturnInterrupt();
        }
    }

    public static ReturnCommand parse(ChillScriptParser parser) {
        if (!parser.match("return")) return null;

        var rv = new ReturnCommand();
        Token token = parser.consumeToken();

        if (parser.match("end")) { // todo: parser.closingDelimiters()
            rv.setToken(token);
        } else {
            rv.setStart(token);
            rv.setExpression(parser.requireExpression(rv, "expression"));
            rv.setEnd(parser.lastMatch());
        }

        return rv;
    }
}
