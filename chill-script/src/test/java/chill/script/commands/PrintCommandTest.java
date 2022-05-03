package chill.script.commands;

import chill.script.parser.ChillScriptParser;
import chill.script.parser.ChillScriptProgram;
import chill.script.runtime.ChillScriptRuntime;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrintCommandTest {

    @Test
    void basics() {
        var parser = new ChillScriptParser();
        var program = parser.parseProgram("print 1 + 1");
        var sb = new StringBuilder();
        program.run(new ChillScriptRuntime(){
            @Override
            public void print(Object value) {
                sb.append(value);
            }
        });
        assertEquals("2", sb.toString());
    }

}
