package chill.script.parser;

import chill.script.runtime.ChillScriptRuntime;
import chill.script.tokenizer.Token;
import chill.script.commands.Command;

import java.util.List;

public class ChillScriptProgram extends Command {
    private List<Command> body;

    public void setBody(List<Command> body) {
        this.body = addChildren(body);
    }

    public void run() {
        ChillScriptRuntime runtime = new ChillScriptRuntime();
        run(runtime);
    }

    public void run(ChillScriptRuntime runtime) {
        runtime.execute(this);
    }

    @Override
    public void execute(ChillScriptRuntime runtime) {
        for (Command command : body) {
            runtime.execute(command);
        }
    }
}
