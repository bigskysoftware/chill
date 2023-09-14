package chill.script.commands;


import chill.script.testutils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FunctionCommandTest {
    @Test
    public void testParse() {
        var output = TestUtils.programOutput("""
                let x be "hello there"
                
                function foo
                    print x
                end
                
                set x to "hello, world"
                
                foo()""");
        assertEquals("hello, world", output);
    }
}
