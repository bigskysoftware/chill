package chill.script.commands;

import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

import java.util.ArrayList;
import java.util.List;

public class ForCommand extends Command {

    Token identifier;

    Token indexIdentifier;

    Expression expr;

    List<Command> body;

    public static Command parse(ChillScriptParser parser) {
        if (!parser.match("for")) return null;

        var rv = new ForCommand();
        rv.setStart(parser.consumeToken());

        rv.setIdentifier(parser.require(TokenType.SYMBOL, rv, "Loop variable expected"));

        parser.require("in", rv, "'in' expected");

        rv.setExpr(parser.requireExpression(rv, "expression"));

        if (parser.matchAndConsume("index")) {
            rv.setIndexIdentifier(parser.require(TokenType.SYMBOL, rv, "Name expected for index"));
        }

        rv.setBody(parser.parseCommandList("end"));

        rv.setEnd(parser.consumeToken());

        return rv;
    }

    @Override
    public void execute(ChillScriptRuntime context) {
        Object iterable = expr.evaluate(context);
        if (iterable != null) {
            if (iterable instanceof Object[]) {
                Object[] objArray = (Object[]) iterable;
                iterable = List.of(objArray);
            }
            Iterable iter = (Iterable) iterable;
            int index = 0;

            context.pushScope();
            {
                for (Object value : iter) {
                    context.declareSymbol(identifier.getStringValue(), value);
                    if (indexIdentifier != null) {
                        context.declareSymbol(indexIdentifier.getStringValue(), index);
                    }
                    for (Command elt : body) {
                        elt.execute(context);
                    }
                    index++;
                }
            }
            context.popScope();
        }
    }

    public void setIdentifier(Token identifier) {
        this.identifier = identifier;
    }

    public void setIndexIdentifier(Token indexIdentifier) {
        this.indexIdentifier = indexIdentifier;
    }

    private void setExpr(Expression expr) {
        this.expr = addChild(expr);
    }

    public void setBody(List<Command> body) {
        this.body = addChildren(body);
    }
}
