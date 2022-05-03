package chill.script.commands;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.parser.ParseElement;

public abstract class Command extends ParseElement {

    public void execute(ChillScriptRuntime runtime) {
        runtime.beforeExecute(this);
        try {
            execInternal(runtime);
        } catch (Exception e) {
            runtime.handleException(this, e);
        }
        runtime.afterExecute(this);
    }

    protected abstract void execInternal(ChillScriptRuntime runtime);
}
