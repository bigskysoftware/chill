package chill.script.pattern;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.tokenizer.TokenType;

public class StringPattern extends Pattern {
    private Token string;

    public StringPattern() {}

    public Token getString() {
        return string;
    }

    public void setString(Token string) {
        this.string = string;
    }

    @Override
    public void bind(ChillScriptRuntime runtime, Object value) {
        if (value instanceof String s && s.equals(string.getStringValue())) {
            return;
        }
        throw new PatternBindingException("String pattern binding failed");
    }

    public static StringPattern parse(ChillScriptParser parser) {
        if (parser.match(TokenType.STRING)) {
            StringPattern rv = new StringPattern();
            rv.setToken(parser.consumeToken());
            rv.setString(rv.getStart());
            return rv;
        } else {
            return null;
        }
    }
}
