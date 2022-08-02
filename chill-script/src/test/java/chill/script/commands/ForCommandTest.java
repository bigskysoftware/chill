package chill.script.commands;

import chill.script.testutils.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForCommandTest {
    @Test
    public void loopsOverSimpleLists() {
        assertEquals(
                "123",
                TestUtils.programOutput("for n in [1, 2, 3] print n end")
        );

        assertEquals(
                "",
                TestUtils.programOutput("for n in [] print n end")
        );
    }

    @Test
    public void canExposeIndex() {
        assertEquals(
                "a0b1c2",
                TestUtils.programOutput("for n in ['a', 'b', 'c'] index i print n print i end")
        );
    }
}
