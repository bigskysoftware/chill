package chill.script.runtime;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.function.Function;

public class ChillScriptUtils {

    public static void main(String[] args) throws Throwable {
    }

    public static final class PropAccessor {
        private final Function getterFunction;
        public PropAccessor(Method method) {
            Class targetClass = method.getDeclaringClass();
            Class returnType = method.getReturnType();
            String methodName = method.getName();
            try {
                MethodHandles.Lookup lookup = MethodHandles.lookup();
                CallSite site = LambdaMetafactory.metafactory(lookup,
                        "apply",
                        MethodType.methodType(Function.class),
                        MethodType.methodType(Object.class, Object.class),
                        lookup.findVirtual(targetClass, methodName, MethodType.methodType(returnType)),
                        MethodType.methodType(returnType, targetClass));
                getterFunction = (Function) site.getTarget().invokeExact();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        public Object get(Object bean) {
            return getterFunction.apply(bean);
        }
    }
}
