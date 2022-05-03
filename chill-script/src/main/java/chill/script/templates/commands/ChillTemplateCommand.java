package chill.script.templates.commands;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.templates.ChillTemplateParseElement;
import chill.script.templates.ChillTemplateRuntime;

import java.io.IOException;

public abstract class ChillTemplateCommand extends ChillTemplateParseElement {
    public abstract void render(ChillTemplateRuntime context);
}
