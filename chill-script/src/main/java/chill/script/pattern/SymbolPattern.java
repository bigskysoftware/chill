package chill.script.pattern;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

public class SymbolPattern extends Pattern {
    public Token identifier;

    public SymbolPattern() {
    }

    public Token getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Token identifier) {
        this.identifier = identifier;
    }

    @Override
    public void bind(ChillScriptRuntime runtime, Object value) {
        runtime.setSymbol(identifier.getStringValue(), value);
    }

    public static SymbolPattern parse(ChillScriptParser parser) {
        if (parser.match(TokenType.SYMBOL)) {
            SymbolPattern rv = new SymbolPattern();
            rv.setToken(parser.consumeToken());
            rv.setIdentifier(rv.getStart());
            return rv;
        } else {
            return null;
        }
    }
}
