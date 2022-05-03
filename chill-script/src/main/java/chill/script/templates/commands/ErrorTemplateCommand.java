package chill.script.templates.commands;

import chill.script.templates.ChillTemplateRuntime;
import chill.script.tokenizer.Token;

public class ErrorTemplateCommand extends ChillTemplateCommand {
    public ErrorTemplateCommand(String errorMessage, Token token) {
        addError(token, errorMessage);
    }

    @Override
    public void render(ChillTemplateRuntime context) {
        throw new IllegalStateException("Can't render a template with errors: " + collectAllParseErrors().toString());
    }
}
