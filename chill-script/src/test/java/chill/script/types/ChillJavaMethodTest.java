package chill.script.types;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class ChillJavaMethodTest {

    @Test
    public void testBasicMethodInvocationWorks() {
        ChillJavaMethod len = new ChillJavaMethod("length", String.class);
        assertEquals(3, len.invoke("foo", Collections.emptyList()));
    }

    @Test
    public void testUnknownMethodThrows() {
        ChillJavaMethod len = new ChillJavaMethod("blah", String.class);
        assertThrows(NoSuchMethodException.class, () -> len.invoke("foo", Collections.emptyList()));
    }

    @Test
    public void testNoCompatibleMethodThrows() {
        ChillJavaMethod len = new ChillJavaMethod("foo", HasMethod1.class);
        assertThrows(IllegalArgumentException.class, () -> len.invoke(new HasMethod1(), Collections.emptyList()),
                "Could not find compatible method with ()");
    }

    @Test
    public void testSimpleMethodResolutionWorks() {
        ChillJavaMethod len = new ChillJavaMethod("foo", Overloaded1.class);
        assertEquals(1, len.invoke(new Overloaded1(), Arrays.asList("foo")));
        assertEquals(2, len.invoke(new Overloaded1(), Arrays.asList(2)));
    }

    @Test
    public void testCoerciveResolutionWorks() {
        ChillJavaMethod len = new ChillJavaMethod("foo", Overloaded1.class);
        assertEquals(2, len.invoke(new Overloaded1(), Arrays.asList(new BigDecimal("2"))));
    }

    public static class HasMethod1 {
        public void foo(Date date) {
        }
    }
    public static class Overloaded1 {
        public int foo(String x) {
            return 1;
        }
        public int foo(int x) {
            return 2;
        }
    }

}
