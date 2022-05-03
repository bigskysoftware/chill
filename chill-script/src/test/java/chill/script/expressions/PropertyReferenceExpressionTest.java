package chill.script.expressions;

import chill.script.runtime.BoundChillMethod;
import chill.script.runtime.BoundChillProperty;
import org.junit.jupiter.api.Test;

import static chill.script.expressions.ChillParserExpressionTest.eval;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PropertyReferenceExpressionTest {

    @Test
    public void simplePropWorks() {
        BoundChillProperty eval = (BoundChillProperty) eval("str#length", "str", "foo");
        assertEquals(3, eval.get());
    }

    @Test
    public void canGetAFunction() {
        BoundChillMethod eval = (BoundChillMethod) eval("str#toString", "str", "foo");
        Object o = eval.invoke();
        assertEquals("foo", o);
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
