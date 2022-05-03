package chill.script.templates;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class BootstrapTemplateTest {

    @Test
    public void bootstrapTest() {
        Assertions.assertEquals("foo\nbar", TestHelpers.renderTemplate("foo\nbar"));
        Assertions.assertEquals("foo\nbar", TestHelpers.renderTemplate("foo\n${\"bar\"}"));
        Assertions.assertEquals("foo\n1", TestHelpers.renderTemplate("foo\n${1}"));
        Assertions.assertEquals("foo\n2", TestHelpers.renderTemplate("foo\n${1 + 1}"));
        Assertions.assertEquals("foo\nfoobar", TestHelpers.renderTemplate("foo\n${\"foo\" + \"bar\"}"));
        Assertions.assertEquals("foo\nbar", TestHelpers.renderTemplate("foo\n${foo}", "foo", "bar"));
        Assertions.assertEquals("foo\n3", TestHelpers.renderTemplate("foo\n${foo.length}", "foo", "bar"));
        Assertions.assertEquals("foo\n3foo", TestHelpers.renderTemplate("foo\n${foo.length()}foo", "foo", "bar"));
        Assertions.assertEquals("foo\nbarfoo", TestHelpers.renderTemplate("foo\n${foo.toString()}foo", "foo", "bar"));
        Assertions.assertEquals("foo\n3foo", TestHelpers.renderTemplate("foo\n${foo.toString().length}foo", "foo", "bar"));
    }

}
