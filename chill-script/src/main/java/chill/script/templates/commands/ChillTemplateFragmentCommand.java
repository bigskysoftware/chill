package chill.script.templates.commands;

import chill.script.templates.ChillTemplateRuntime;
import chill.script.tokenizer.Token;

import java.io.IOException;
import java.util.List;

public class ChillTemplateFragmentCommand extends ChillTemplateCommand {

    private List<ChillTemplateCommand> body;
    private Token name;

    public void setBody(List<ChillTemplateCommand> body) {
        this.body = body;
    }

    @Override
    public void render(ChillTemplateRuntime context) {
        for (ChillTemplateCommand elt : body) {
            elt.render(context);
        }
    }

    public void setName(Token name) {
        this.name = name;
    }

    public String getFragmentName() {
        return this.name.getStringValue();
    }
}
