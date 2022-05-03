package chill.script.templates.commands;

import chill.script.templates.ChillTemplateRuntime;
import chill.script.tokenizer.Token;

public class ChillTemplateContentElement extends ChillTemplateCommand {

    private final Token token;

    public ChillTemplateContentElement(Token template) {
        this.token = template;
    }

    @Override
    public void render(ChillTemplateRuntime context) {
        context.append(token.getStringValue());
    }
}
