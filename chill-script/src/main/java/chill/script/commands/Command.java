package chill.script.commands;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.parser.ParseElement;

public abstract class Command extends ParseElement {

    public void execute() {
        execute(new ChillScriptRuntime());
    }

    public abstract void execute(ChillScriptRuntime runtime);
}
