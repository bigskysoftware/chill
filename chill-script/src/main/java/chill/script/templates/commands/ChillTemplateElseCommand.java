package chill.script.templates.commands;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.templates.ChillTemplateRuntime;

import java.io.IOException;
import java.util.List;

public class ChillTemplateElseCommand extends ChillTemplateCommand {

    private List<ChillTemplateCommand> body;

    @Override
    public void render(ChillTemplateRuntime context) {
        for (ChillTemplateCommand elt : body) {
            elt.render(context);
        }
    }

    public void setBody(List<ChillTemplateCommand> cmdList) {
        this.body = cmdList;
    }
}
