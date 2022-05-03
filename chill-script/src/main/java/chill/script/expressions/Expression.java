package chill.script.expressions;

import chill.script.parser.ParseElement;
import chill.script.runtime.ChillScriptRuntime;

public abstract class Expression extends ParseElement {
    public Object evaluate(ChillScriptRuntime runtime) {
        throw new UnsupportedOperationException("evaluate needs to be implemented for " + this.getClass().getName());
    }

    public final Object run(Object... args) {
        return evaluate(new ChillScriptRuntime(args));
    }
}