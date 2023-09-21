package chill.script.expressions;

import chill.script.parser.ChillScriptParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MethodCallExpressionTest {

    @Test
    public void functionInvocationWorks() {
        assertEquals("basicMethod", eval("x.basicMethod()", "x", new HasMethods()));
        assertEquals("basicMethodWithArg:Foo", eval("x.basicMethodWithArg('Foo')", "x", new HasMethods()));
    }

    @Test
    public void iHateMethodOverloading() {
        assertEquals("2", eval("x.m('foo')", "x", new HasOverloadedMethods()));
        assertEquals("3", eval("x.m(1)", "x", new HasOverloadedMethods()));
    }

    public static Object eval(String src, Object... args) {
        ChillScriptParser parser = new ChillScriptParser();
        Expression expr = parser.parseExpression(src);
        Object value = expr.run(args);
        return value;
    }

    public static class HasMethods {

        public String basicMethod(){
            return "basicMethod";
        }

        public String basicMethodWithArg(Object arg){
            return "basicMethodWithArg:" + arg;
        }
    }

    public static class HasOverloadedMethods {
        public String m(){
            return "1";
        }

        public String m(Object o){
            return "2";
        }

        public String m(Integer i){
            return "3";
        }

        public String m(int i){
            return "4";
        }
    }
}
