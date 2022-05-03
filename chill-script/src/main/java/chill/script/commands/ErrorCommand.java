package chill.script.commands;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;

public class ErrorCommand extends Command {
    public ErrorCommand(String errorMessage, Token token) {
        setStart(token);
        setEnd(token);
        addError(token, errorMessage);
    }

    @Override
    public void execInternal(ChillScriptRuntime runtime) {
        throw new UnsupportedOperationException("Error command");
    }
}
