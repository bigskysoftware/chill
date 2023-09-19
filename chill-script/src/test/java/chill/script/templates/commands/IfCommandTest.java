package chill.script.templates.commands;

import chill.script.templates.TestHelpers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IfCommandTest {

    @Test
    public void basicIfCommand() {
        Assertions.assertEquals("foo\nbar\n", TestHelpers.renderTemplate(
                """
                        foo
                        #if 1 is 1
                        bar
                        #end"""));
        Assertions.assertEquals("foo\n", TestHelpers.renderTemplate(
                """
                        foo
                        #if 1 is not 1
                        bar
                        #end"""));
    }

    @Test
    public void elseCommand() {
        Assertions.assertEquals("foo\nbar\n", TestHelpers.renderTemplate(
                """
                        foo
                        #if 1 is 1
                        bar
                        #else
                        foo
                        #end
                        """
        ));
        Assertions.assertEquals("foo\nfoo\n", TestHelpers.renderTemplate(
                """
                        foo
                        #if 1 is not 1
                        bar
                        #else
                        foo
                        #end
                        """
        ));
    }

    @Test
    public void elseIfCommand() {
        Assertions.assertEquals("foo\nbar\n", TestHelpers.renderTemplate(
                """
                        foo
                        #if 1 is 1
                        bar
                        #elseif 1 is not 1
                        foo
                        #end
                        """
        ));
        Assertions.assertEquals("foo\nfoo\n", TestHelpers.renderTemplate(
                        "foo\n" +
                        "#if 1 != 1\n" +
                        "bar\n" +
                        "#elseif 1 == 1\n" +
                        "foo\n" +
                        "#end\n" +
                        ""
        ));
    }

}
