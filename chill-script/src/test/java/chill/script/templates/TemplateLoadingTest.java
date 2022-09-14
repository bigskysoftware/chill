package chill.script.templates;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateLoadingTest {

    static ChillTemplates engine = new ChillTemplates()
            .withCaching()
            .addToPath("file:src/test/resources/view")
            .addToPath("file:src/test/resources/templates");

    @Test
    public void basics(){
        assertTrue(renderTemplate("/index.html").contains("Hello Templates"));
        assertEquals("bar", renderTemplate("/view-template.html", "foo", "bar"));
    }

    @Test
    public void forLoop() {
        assertEquals("-a\n-b\n-c\n", renderTemplate("/for-loop.html", "lst", List.of("a", "b", "c")));
    }

    @Test
    public void ifStmt() {
        assertEquals("x\n", renderTemplate("/if-stmt.html", "x", true));
        assertEquals("y\n", renderTemplate("/if-stmt.html", "x", false));
    }

    @Test
    public void includeStmt() {
        assertEquals("foo\ndoh\nbar", renderTemplate("/include.html"));
    }

    @Test
    public void basicLayout() {
        assertEquals("foo\nbar\nfoo", renderTemplate("/has-layout.html"));
    }

    @Test
    public void nestedLayout() {
        assertEquals("foo\ndoh\nbar\ndoh\nfoo", renderTemplate("/has-nested-layout.html"));
    }

    @Test
    public void basicFragment() {
        assertEquals("foo\n", renderTemplateFragment("/has-fragment.html", "frag1", "myVar", "foo"));
        assertEquals("myVar is foo\n", renderTemplateFragment("/has-fragment.html", "frag2", "myVar", "foo"));
    }

    private String renderTemplate(String name, Object... args) {
        ChillTemplate template = engine.get(name);
        String content = template.render(args);
        return content;
    }

    private String renderTemplateFragment(String name, String fragment, Object... args) {
        ChillTemplate template = engine.get(name);
        String content = template.renderFragment(fragment, args);
        return content;
    }

}
