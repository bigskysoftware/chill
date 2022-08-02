package chill.script.commands;

import chill.script.parser.ChillScriptParser;
import chill.script.parser.ChillScriptProgram;
import chill.script.runtime.ChillScriptRuntime;
import chill.script.testutils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrintCommandTest {

    @Test
    void basics() {
        assertEquals("2", TestUtils.programOutput("print 1 + 1"));
    }

}
