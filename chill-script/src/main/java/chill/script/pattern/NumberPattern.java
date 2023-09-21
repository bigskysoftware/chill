package chill.script.pattern;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.TokenType;

public class NumberPattern extends Pattern {
    private int value;

    public NumberPattern() {
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void bind(ChillScriptRuntime runtime, Object value) {
        if (value instanceof Number n && n.intValue() == this.value) {
            return;
        }
        throw new RuntimeException("Expected the number " + this.value + " but got " + value + " instead.");
    }

    public static NumberPattern parse(ChillScriptParser parser) {
        if (parser.match(TokenType.NUMBER)) {
            NumberPattern rv = new NumberPattern();
            rv.setToken(parser.consumeToken());
            rv.setValue(Integer.parseInt(parser.lastMatch().getStringValue()));
            return rv;
        } else {
            return null;
        }
    }
}
