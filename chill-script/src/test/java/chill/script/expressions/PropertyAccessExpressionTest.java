package chill.script.expressions;

import chill.script.runtime.Gettable;
import chill.utils.DynaProperties;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static chill.script.expressions.ChillParserExpressionTest.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyAccessExpressionTest {

    @Test
    public void simpleAccessWorks() {
        assertEquals(3, eval("str.length", "str", "foo"));
    }

    @Test
    public void simpleAccessViaLowercasedGetterWorks() {
        assertEquals("example", eval("x.example", "x", new TestBean()));
    }

    @Test
    public void simpleAccessViaGetterWorks() {
        assertEquals("example", eval("x.Example", "x", new TestBean()));
    }

    @Test
    public void simpleAccessViaSnakeCase() {
        assertEquals(true, eval("x.example_bean", "x", new TestBean()));
    }

    @Test
    public void simpleAccessViaIsWorks() {
        assertEquals(true, eval("x.ExampleBean", "x", new TestBean()));
    }

    @Test
    public void simpleToNonGetterWorks() {
        assertEquals("demo", eval("x.demo", "x", new TestBean()));
    }

    @Test
    public void simpleDynaPropertiesWork() {
        TestBean testBean = new TestBean();
        Map<String, Object> dynaProperties = DynaProperties.forObject(testBean);
        dynaProperties.put("foo", "demo");
        assertEquals("demo", eval("x.foo", "x", testBean));
    }

    @Test
    public void complexDynaPropertiesWork() {
        TestBean testBean = new TestBean();
        Map<String, Object> dynaProperties = DynaProperties.forObject(testBean);
        dynaProperties.put("foo", (Gettable) () -> "demo");
        assertEquals("demo", eval("x.foo", "x", testBean));
    }

    public static class TestBean {

        public String getExample() {
            return "example";
        }

        public boolean isExampleBean() {
            return true;
        }

        public String demo() {
            return "demo";
        }

    }

}
