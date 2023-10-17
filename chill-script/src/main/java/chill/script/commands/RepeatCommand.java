package chill.script.commands;

import chill.script.expressions.Expression;
import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

import java.util.Iterator;
import java.util.List;

public class RepeatCommand extends Command {
    private Loop loop;
    private List<Command> body;

    private Token identifier, indexIdentifier;

    public static Command parse(ChillScriptParser parser) {
        if (!parser.match("repeat")) return null;

        var rv = new RepeatCommand();
        rv.setStart(parser.consumeToken());

        if (parser.matchAndConsume("while")) {

            var cond = parser.requireExpression(rv, "expression");
            var loop = new WhileLoop(cond);
            rv.setLoop(loop);

        } else if (parser.matchAndConsume("until")) {

            var cond = parser.requireExpression(rv, "expression");
            var loop = new UntilLoop(cond);
            rv.setLoop(loop);

        } else if (parser.matchAndConsume("for")) {
            rv.setIdentifier(parser.require(TokenType.SYMBOL, rv, "Loop variable expected"));

            parser.require("in", rv, "'in' expected");

            var it = parser.requireExpression(rv, "expression");
            var loop = new ForLoop(it);
            rv.setLoop(loop);
        } else {

            var num = parser.requireExpression(rv, "expression");
            parser.require("times", rv, "Expected 'times' after expression");
            var loop = new NTimesLoop(num);
            rv.setLoop(loop);

        }

        if (parser.matchAndConsume("index")) {
            rv.setIndexIdentifier(parser.require(TokenType.SYMBOL, rv, "Name expected for index"));
        }

        rv.setBody(parser.parseCommandList("end"));
        rv.setEnd(parser.require("end", rv, "Expected 'end'"));

        return rv;
    }
    @Override
    public void execute(ChillScriptRuntime runtime) {
        var iter = this.loop.iterator(runtime);

        int i = 0;
        while (iter.hasNext()) {
            runtime.pushScope();
            {
                var value = iter.next();
                if (identifier != null) runtime.setSymbol(identifier.getStringValue(), value);
                if (indexIdentifier != null) runtime.setSymbol(indexIdentifier.getStringValue(), i++);

                for (Command elt : body) {
                    elt.execute(runtime);
                }
            }
            runtime.popScope();
        }
    }

    public void setLoop(Loop loop) {
        this.loop = loop;
    }

    public void setBody(List<Command> body) {
        this.body = body;
    }

    public void setIdentifier(Token identifier) {
        this.identifier = identifier;
    }

    public void setIndexIdentifier(Token indexIdentifier) {
        this.indexIdentifier = indexIdentifier;
    }

    public interface Loop {
        Iterator<Object> iterator(ChillScriptRuntime rt);
    }

    private static class WhileLoop implements Loop {
        Expression condition;

        public WhileLoop(Expression condition) {
            this.condition = condition;
        }

        @Override
        public Iterator<Object> iterator(ChillScriptRuntime rt) {
            return new Iterator<Object>() {
                @Override
                public boolean hasNext() {
                    return rt.isTruthy(condition.evaluate(rt));
                }

                @Override
                public Object next() {
                    return null;
                }
            };
        }
    }

    private static class UntilLoop implements Loop {
        Expression condition;

        public UntilLoop(Expression condition) {
            this.condition = condition;
        }

        @Override
        public Iterator<Object> iterator(ChillScriptRuntime rt) {
            return new Iterator<Object>() {
                @Override
                public boolean hasNext() {
                    return !rt.isTruthy(condition.evaluate(rt));
                }

                @Override
                public Object next() {
                    return null;
                }
            };
        }
    }

    private static class NTimesLoop implements Loop {
        Expression n;

        public NTimesLoop(Expression n) {
            this.n = n;
        }

        @Override
        public Iterator<Object> iterator(ChillScriptRuntime rt) {
            var n_ = ((Number) n.evaluate(rt)).intValue();
            return new Iterator<Object>() {
                int i = 0;

                @Override
                public boolean hasNext() {
                    return i < n_;
                }

                @Override
                public Object next() {
                    return i++;
                }
            };
        }
    }

    private static class ForLoop implements Loop {
        Expression it;

        public ForLoop(Expression it) {
            this.it = it;
        }

        @Override
        public Iterator<Object> iterator(ChillScriptRuntime rt) {
            var iterable = (Iterable) it.evaluate(rt);
            return iterable.iterator();
        }
    }
}
