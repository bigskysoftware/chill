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
                programOutput("set i to 0   repeat while i is not 2 set i to i + 1 end   print i")
        );
    }

    @Test
    public void untilLoopWorks() {
        assertEquals(
                "",
                programOutput("repeat until 1 is 1 print 1 end")
        );

        assertEquals(
                "2",
                programOutput("set i to 0   repeat until i is 2 set i to i + 1 end   print i")
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

    @Test
    public void forLoopWorks() {
        assertEquals(
                "",
                programOutput("repeat for i in [] print i end")
        );

        assertEquals(
                "123",
                programOutput("repeat for i in [1, 2, 3] print i end")
        );
    }

    @Test
    public void indexIdentifierWorks() {
        assertEquals(
                "01",
                programOutput("""
                        set i to 0
                        repeat while i is not 2 index j
                            set i to i + 1
                            print j
                        end""")
        );

        assertEquals(
                "01",
                programOutput("""
                        set i to 0
                        repeat until i is 2 index j
                            set i to i + 1
                            print j
                        end""")
        );

        assertEquals(
                "0123",
                programOutput("set i to 2 repeat i + i times index j print j end")
        );

        assertEquals(
                "012",
                programOutput("repeat for i in [1, 2, 3] index j print j end")
        );
    }
}
