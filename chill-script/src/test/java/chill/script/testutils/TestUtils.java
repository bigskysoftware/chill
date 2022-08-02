package chill.script.testutils;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;

public class TestUtils {
    public static class TestRuntime extends ChillScriptRuntime {
        StringBuilder terminal;

        @Override
        public void print(Object value) {
            terminal.append(value);
        }

        public String logs() {
            return terminal.toString();
        }
    }

    public static String programOutput(String programSrc) {
        var rt = new TestRuntime();
        var parser = new ChillScriptParser();

        parser.parseProgram(programSrc).execute(rt);

        return rt.logs();
    }
}
