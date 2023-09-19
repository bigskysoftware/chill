package chill.script.pattern;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;

public class BooleanPattern extends Pattern {
    private boolean value;

    public BooleanPattern() {}

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void bind(ChillScriptRuntime runtime, Object value) {
        if (value instanceof Boolean val && val == this.value) {
            return;
        }
        throw new PatternBindingException("Cannot bind " + value + " to " + this.value);
    }

    public static BooleanPattern parse(ChillScriptParser parser) {
        if (parser.match("true", "false")) {
            BooleanPattern rv = new BooleanPattern();
            rv.setToken(parser.consumeToken());
            rv.setValue(Boolean.parseBoolean(parser.lastMatch().getStringValue()));
            return rv;
        } else {
            return null;
        }
    }
}
