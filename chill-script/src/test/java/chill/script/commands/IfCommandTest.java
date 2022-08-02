package chill.script.commands;

import chill.script.testutils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IfCommandTest {
    @Test
    public void handlesFalseyValues() {
        assertEquals(
                "!2",
                TestUtils.programOutput("if false print 2 else print '!2' end")
        );

        assertEquals(
                "",
                TestUtils.programOutput("if null print 3 end")
        );
    }

    @Test
    public void handlesObviouslyTruthyValues() {
        assertEquals(
                "4",
                TestUtils.programOutput("if true print 4 end")
        );

        assertEquals(
                "5",
                TestUtils.programOutput("if 'Hello, World!' print 5 end")
        );

        assertEquals(
                "6",
                TestUtils.programOutput("if 42 print 6 end")
        );

        assertEquals(
                "7",
                TestUtils.programOutput("if [1,2,3] print 7 end")
        );
    }

    @Test
    public void handlesTrickyValues() {
        assertEquals(
                "",
                TestUtils.programOutput("if nonexistentVar_d208715e_c9f2_4d43_a4d1_50c3cbe96def print 8 else print '8!' end")
        );

        assertEquals(
                "9",
                TestUtils.programOutput("if 0 print 9 else print '9!' end")
        );


        assertEquals(
                "10",
                TestUtils.programOutput("if -1 print 10 end")
        );


        assertEquals(
                "11",
                TestUtils.programOutput("if '' print 11 end")
        );


        assertEquals(
                "12",
                TestUtils.programOutput("if [] print 12 end")
        );
    }
}
