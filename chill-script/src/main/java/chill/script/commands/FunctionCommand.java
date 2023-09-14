package chill.script.commands;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.runtime.ReturnInterrupt;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;
import chill.utils.NiceList;

import java.util.LinkedList;
import java.util.List;

public class FunctionCommand extends Command {
    private Token name;
    private NiceList<Token> argNames = new NiceList<>();
    private List<Command> body;

    @Override
    public void execute(ChillScriptRuntime runtime) {
        var capture = new Capture();
        runtime.setSymbol(name.getStringValue(), capture); // we do this first so that the function can call itself recursively
        capture.setCapturedScope(runtime.getCurrentScope());
    }

    public Token getName() {
        return name;
    }

    public void setName(Token name) {
        this.name = name;
    }

    public List<Command> getBody() {
        return body;
    }

    public void setBody(List<Command> body) {
        this.body = body;
    }

    public void addArg(Token name) {
        argNames.add(name);
    }

    public static FunctionCommand parse(ChillScriptParser parser) {
        if (!parser.match("function")) return null;

        var rv = new FunctionCommand();
        rv.setStart(parser.consumeToken());

        rv.setName(parser.require(TokenType.SYMBOL, rv, "Function name expected"));

        if (parser.matchAndConsume(TokenType.LEFT_PAREN)) {
            while (parser.moreTokens() && !parser.match(TokenType.RIGHT_PAREN)) {
                var name = parser.require(TokenType.SYMBOL, rv, "Parameter name expected");
                rv.addArg(name);
            }
            parser.require(TokenType.RIGHT_PAREN, rv, "Expected ')'");
        }

        var body = parser.parseCommandList("end");
        for (var cmd : body) {
            rv.addChild(cmd);
        }
        rv.setBody(body);

        parser.require("end", rv, "'end' expected");

        return rv;
    }

    public Capture capture(ChillScriptRuntime chillScriptRuntime) {
        var out = new Capture();
        out.setCapturedScope(chillScriptRuntime.getCurrentScope());
        return out;
    }

    public class Capture {
        LinkedList<ChillScriptRuntime.ScopeFrame> scope;

        public FunctionCommand getFunction() {
            return FunctionCommand.this; // what the flying f* is this semantic nonsense?
        }

        public LinkedList<ChillScriptRuntime.ScopeFrame> getScope() {
            return scope;
        }

        void setCapturedScope(LinkedList<ChillScriptRuntime.ScopeFrame> scope) {
            this.scope = new LinkedList<>();
            for (var frame : scope) {
                this.scope.add(frame.shallowCopy());
            }
        }

        public Object invoke(ChillScriptRuntime runtime, List<Object> argValues) {
            if (argValues.size() != argNames.size()) {
                throw new RuntimeException("Expected " + argNames.size() + " arguments but got " + argValues.size());
            }
            runtime.pushFrame(this);
            for (int i = 0; i < argNames.size(); i++) {
                runtime.setSymbol(argNames.get(i).getStringValue(), argValues.get(i));
            }
            try {
                for (var cmd : body) {
                    cmd.execute(runtime);
                }
            } catch (ReturnInterrupt ri) {
                return ri.getValue();
            }
            runtime.popFrame();
            return null;
        }
    }
}
