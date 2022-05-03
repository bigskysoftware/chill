package chill.script.templates.commands;

import org.junit.jupiter.api.Test;

import java.util.List;

import static chill.script.templates.TestHelpers.renderTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CombinedCommandTest {

    @Test
    public void ifThenFor() {
        assertEquals("""
                1
                2
                3
                """, renderTemplate(
                """
                        #if lst
                          #for x in lst
                        ${x}
                          #end
                        #end""", "lst", List.of(1, 2, 3)));
    }

}
