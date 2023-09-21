package chill.script.pattern;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.TokenType;

public class GroupPattern extends Pattern {
    private Pattern inner;

    public GroupPattern() {
    }

    public Pattern getInner() {
        return inner;
    }

    public void setInner(Pattern inner) {
        this.inner = inner;
    }

    @Override
    public void evaluate(ChillScriptRuntime runtime) {
        super.evaluate(runtime);
    }

    @Override
    public void bind(ChillScriptRuntime runtime, Object value) {
        inner.bind(runtime, value);
    }

    public static GroupPattern parse(ChillScriptParser parser) {
        if (parser.matchAndConsume(TokenType.LEFT_PAREN)) {
            GroupPattern rv = new GroupPattern();
            rv.setStart(parser.lastMatch());

            Pattern inner = Pattern.parsePattern(parser);

            if (inner == null) {
                rv.addError(parser.lastMatch(), "Expected pattern");
            } else {
                rv.setInner(inner);
                rv.addChild(inner);
            }

            rv.setEnd(parser.require(TokenType.RIGHT_PAREN, rv, "Expected ')'"));
            return rv;
        } else {
            return null;
        }
    }
}
