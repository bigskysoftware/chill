package chill.script.templates.commands;

import org.junit.jupiter.api.Test;

import java.util.List;

import static chill.script.templates.TestHelpers.renderTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ForCommandTest {

    @Test
    public void basicForCommand() {
        assertEquals("""
                1
                2
                3
                """, renderTemplate(
                """
                        #for x in lst
                        ${x}
                        #end""", "lst", List.of(1, 2, 3)));
    }

    @Test
    public void basicForCommandWListLiteral() {
        assertEquals("""
                1
                2
                3
                """, renderTemplate(
                """
                        #for x in [1, 2, 3]
                        ${x}
                        #end"""));
    }

    @Test
    public void doesNotPolluteGlobals() {
        assertEquals("""
                Hello
                1
                2
                3
                Hello\
                """, renderTemplate(
                """
                        ${x}
                        #for x in [1, 2, 3]
                        ${x}
                        #end
                        ${x}""", "x", "Hello"));
    }

}
