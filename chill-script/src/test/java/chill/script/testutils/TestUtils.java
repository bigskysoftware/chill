package chill.script.testutils;

import chill.script.parser.ChillScriptParser;
import chill.script.runtime.ChillScriptRuntime;

public class TestUtils {
    public static class TestRuntime extends ChillScriptRuntime {
        StringBuilder terminal = new StringBuilder();

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
        var program = parser.parseProgram(programSrc);
        rt.beforeExecute(program);
        program.execute(rt);
        rt.afterExecute(program);

        return rt.logs();
    }
}
