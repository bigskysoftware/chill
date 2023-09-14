package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import chill.script.types.ChillMethod;
import chill.script.types.ChillType;
import chill.script.types.TypeSystem;
import chill.utils.TheMissingUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ChillParserExpressionTest {

    @Test
    public void equality() {
        assertEquals(true, eval("1 is 1"));
        assertEquals(true, eval("1 equals 1"));
        assertEquals(true, eval("1 is equal to 1"));
        assertEquals(true, eval("1 does equal 1"));
        assertEquals(true, eval("1 like 1"));
        assertEquals(true, eval("1 is similar to 1"));

        assertEquals(false, eval("1 equals 2"));
        assertEquals(false, eval("1 is equal to 2"));
        assertEquals(false, eval("1 does equal 2"));
        assertEquals(false, eval("1 like 2"));
        assertEquals(false, eval("1 is similar to 2"));

        assertEquals(false, eval("1 does not equal 1"));
        assertEquals(false, eval("1 is not equal to 1"));
        assertEquals(false, eval("1 is not similar to 1"));
        assertEquals(false, eval("1 is not like 1"));
        assertEquals(false, eval("1 is unlike 1"));

        assertEquals(true, eval("1 does not equal 2"));
        assertEquals(true, eval("1 is not equal to 2"));
        assertEquals(true, eval("1 is not similar to 2"));
        assertEquals(true, eval("1 is not like 2"));
        assertEquals(true, eval("1 is unlike 2"));

        assertEquals(true, eval("1 equals 2 is false"));
        assertEquals(true, eval("1 equals 2 is not true"));
    }

    @Test
    public void ordinal() {
        assertEquals(true, eval("1 less than 2"));
        assertEquals(true, eval("1 less than or equal to 2"));
        assertEquals(true, eval("1 less than or equivalent to 2"));

        assertEquals(true, eval("1 is less than 2"));
        assertEquals(true, eval("1 is less than or equal to 2"));
        assertEquals(true, eval("1 is less than or equivalent to 2"));

        assertEquals(true, eval("1 smaller than 2"));
        assertEquals(true, eval("1 smaller than or equal to 2"));
        assertEquals(true, eval("1 smaller than or equivalent to 2"));

        assertEquals(true, eval("1 is smaller than 2"));
        assertEquals(true, eval("1 is smaller than or equal to 2"));
        assertEquals(true, eval("1 is smaller than or equivalent to 2"));


        assertEquals(false, eval("1 greater than 2"));
        assertEquals(false, eval("1 greater than or equal to 2"));
        assertEquals(false, eval("1 greater than or equivalent to 2"));

        assertEquals(false, eval("1 is greater than 2"));
        assertEquals(false, eval("1 is greater than or equal to 2"));
        assertEquals(false, eval("1 is greater than or equivalent to 2"));

        assertEquals(false, eval("1 larger than 2"));
        assertEquals(false, eval("1 larger than or equal to 2"));
        assertEquals(false, eval("1 larger than or equivalent to 2"));

        assertEquals(false, eval("1 is larger than 2"));
        assertEquals(false, eval("1 is larger than or equal to 2"));
        assertEquals(false, eval("1 is larger than or equivalent to 2"));
    }

    @Test
    public void collections() {
        assertEquals(true, eval("[1, 2, 4] intersects [1, 5, 25]"));

        assertEquals(true, eval("4 in [1, 2, 3, 4]"));
        assertEquals(true, eval("5 not in [1, 2, 3, 4]"));
        assertEquals(true, eval("3 is in [1, 2, 3, 4]"));
        assertEquals(true, eval("0 is not in [1, 2, 3, 4]"));

        assertEquals(true, eval("[1, 2, 3] are all in [1, 2, 3]"));
        assertEquals(true, eval("[1, 2, 3] are not all in [1, 4, 5]"));

        assertEquals(true, eval("[1, 2, 3] is distinct"));
        assertEquals(false, eval("[1, 2, 3] intersects [4, 5, 6]"));
    }

    @Test
    public void addition() {
        assertEquals(new BigDecimal("2"), eval("1 + 1"));
        assertEquals(new BigDecimal("3"), eval("1 + 1 + 1"));
        assertEquals("foo1", eval("'foo' + 1"));
    }

    @Test
    public void strings() {
        assertEquals("foo", eval("'foo'"));
        assertEquals("bar", eval("\"bar\"'"));
        assertEquals("foo\nbar", eval("'foo\nbar'"));
        assertEquals("foo\nbar", eval("'foo\\nbar'"));
        assertEquals("foo\tbar", eval("'foo\tbar'"));
        assertEquals("foo\tbar", eval("'foo\\tbar'"));
    }

    @Test
    public void multiLineStrings() {
        assertEquals("foo\nbar", eval("    'foo\n" +
                                                   "     bar'"));
    }

    @Test
    public void symbols() {
        assertEquals("foo", eval("foo", "foo", "foo"));
    }

    @Test
    public void lists() {
        assertEquals(Arrays.asList(), eval("[]"));
        assertEquals(Arrays.asList("a"), eval("['a']"));
        assertEquals(Arrays.asList("a", "b"), eval("['a', 'b']"));
        assertEquals(Arrays.asList("a", "b", "c"), eval("['a', 'b', 'c']"));
    }

    @Test
    public void properties() {
        assertEquals(3, eval("foo.length", "foo", "foo"));
    }

    @Test
    public void methods() {
        assertEquals(3, eval("foo.length()", "foo", "foo"));
        assertEquals(3, eval("foo.toString().length()", "foo", "foo"));
        var lst = new LinkedList<>();
        eval("lst.add('foo')", "lst", lst);
        assertEquals("foo", lst.get(0));
    }

    @Test
    public void topLevelMethodsWork() {
        ChillType type = TypeSystem.getType(ChillParserExpressionTest.class);
        ChillMethod staticFunc = type.getMethod("staticFunction");
        assertEquals("static function", eval("foo()", "foo", staticFunc));
    }

    @Test
    public void urls() throws Exception {
        assertEquals(new URL("https://google.com"), eval("https://google.com"));
        assertEquals(new URL("http://google.com"), eval("http://google.com"));
        assertEquals(new URL("http://google.com").toString(), eval("(http://google.com).toString()"));
    }

    @Test
    public void paths() {
        assertEquals("/foo/bar.html", eval("/foo/bar.html"));
        assertEquals("./foo/bar.html", eval("./foo/bar.html"));
    }

    @Test
    public void arrayAccessNull() {
        assertNull(eval("foo['bar']"));
    }

    @Test
    public void arrayAccessMap() {
        assertEquals(3, eval("foo['bar']", "foo", TheMissingUtils.mapFrom("bar", 3)));
        assertNull(eval("foo['doh']", "foo", TheMissingUtils.mapFrom("bar", 3)));
    }

    @Test
    public void arrayAccessList() {
        assertNull(eval("foo[-1]", "foo", List.of(1, 2, 3)));
        assertEquals(1, eval("foo[0]", "foo", List.of(1, 2, 3)));
        assertEquals(2, eval("foo[1]", "foo", List.of(1, 2, 3)));
        assertEquals(3, eval("foo[2]", "foo", List.of(1, 2, 3)));
        assertNull(eval("foo[3]", "foo", List.of(1, 2, 3)));
    }

    @Test
    public void arrayAccessProperties() {
        assertEquals(3, eval("foo['length']", "foo", "foo"));
        assertNull(eval("foo['bar']", "foo", "foo"));
    }


    @Test
    public void arrayAccessArray() {
        assertNull(eval("foo[-1]", "foo", new Object[]{1, 2, 3}));
        assertEquals(1, eval("foo[0]", "foo", new Object[]{1, 2, 3}));
        assertEquals(2, eval("foo[1]", "foo", new Object[]{1, 2, 3}));
        assertEquals(3, eval("foo[2]", "foo", new Object[]{1, 2, 3}));
        assertNull(eval("foo[3]", "foo", new Object[]{1, 2, 3}));
    }

    public static Object eval(String src, Object... args) {
        ChillScriptParser parser = new ChillScriptParser();
        Expression expr = parser.parseExpression(src);
        Object value = expr.run(args);
        return value;
    }

    public static String staticFunction(){
        return "static function";
    }
}
