package chill.script.commands;

import static chill.script.testutils.TestUtils.programOutput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RepeatCommandTest {
    @Test
    public void whileLoopWorks() {
        assertEquals(
                "",
                programOutput("repeat while null print 1 end")
        );

        assertEquals(
                "2",
                programOutput("set i to 0   repeat while i != 2 set i to i + 1 end   print i")
        );
    }

    @Test
    public void untilLoopWorks() {
        assertEquals(
                "",
                programOutput("repeat until 1 == 1 print 1 end")
        );

        assertEquals(
                "2",
                programOutput("set i to 0   repeat until i == 2 set i to i + 1 end   print i")
        );
    }

    @Test
    public void nTimesLoopWorks() {
        assertEquals(
                "111",
                programOutput("repeat 3 times print 1 end")
        );


        assertEquals(
                "1111",
                programOutput("set i to 2 repeat i + i times print 1 end")
        );
    }
}
