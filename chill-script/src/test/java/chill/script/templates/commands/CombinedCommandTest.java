package chill.script.templates.commands;

import org.junit.jupiter.api.Test;

import java.util.List;

import static chill.script.templates.TestHelpers.renderTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CombinedCommandTest {

    @Test
    public void ifThenFor() {
        assertEquals("\n1\n2\n3\n", renderTemplate(
                "\n" +
                        "#if lst\n" +
                        "#for x in lst\n" +
                        "${x}\n" +
                        "#end\n" +
                        "#end", "lst", List.of(1, 2, 3)));
    }

}
