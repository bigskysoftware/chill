package chill.script.pattern;

import chill.script.parser.ChillScriptParser;
import chill.script.parser.ParseElement;
import chill.script.runtime.ChillScriptRuntime;

/*
[a, b, c] = [1, 2, 3]

let [a, ..5, b] = [1, 2, 3, 4, 3, 2, 1]
assert a equals b

let [a, b, c, ..] = [1, 2, 3, 4, 3, 2, 1]
 */
public abstract class Pattern extends ParseElement {

    public void evaluate(ChillScriptRuntime runtime) {
        throw new UnsupportedOperationException("evaluate needs to be implemented for " + this.getClass().getName());
    }

    public void bind(ChillScriptRuntime runtime, Object value) {
        throw new UnsupportedOperationException("bind needs to be implemented for " + this.getClass().getName());
    }

    public static Pattern parsePattern(ChillScriptParser parser) {
        Pattern pattern;
        if ((pattern = StringPattern.parse(parser)) != null) return pattern;
        if ((pattern = NumberPattern.parse(parser)) != null) return pattern;
        if ((pattern = BooleanPattern.parse(parser)) != null) return pattern;
        if ((pattern = SymbolPattern.parse(parser)) != null) return pattern;
        if ((pattern = GroupPattern.parse(parser)) != null) return pattern;
        if ((pattern = ListPattern.parse(parser)) != null) return pattern;
        return null;
    }
}
