package chill.script.types;

import chill.utils.NiceList;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static chill.utils.TheMissingUtils.forceThrow;

public class ChillJavaMethod implements ChillMethod {

    private final List<Method> javaMethods;
    private final String name;

    public ChillJavaMethod(String methodName, Class backingClass) {
        this.name = methodName;
        this.javaMethods = new ArrayList<>();
        Method[] allMethods = backingClass.getMethods();
        for (Method m : allMethods) {
            if (m.getName().equals(methodName)) {
                javaMethods.add(m);
            }
        }
    }

    @Override
    public Object invoke(Object rootVal, List<Object> args) {
        try {
            Method method = bestMatch(args);
            return method.invoke(rootVal, args.toArray());
        } catch (Exception e) {
            throw forceThrow(e);
        }
    }

    private Method bestMatch(List<Object> argValues) {
        int closestDistance = Integer.MAX_VALUE;
        Method bestMatch = null;
        for (Method javaMethod : javaMethods) {
            int distance = distanceFromValues(javaMethod, argValues);
            if (distance < closestDistance) {
                closestDistance = distance;
                bestMatch = javaMethod;
            }
            // stable resolution by using string comparison
            if (distance == closestDistance) {
                String str1 = new NiceList<>(bestMatch.getParameterTypes()).map(aClass -> aClass.getName()).join("-");
                String str2 = new NiceList<>(javaMethod.getParameterTypes()).map(aClass -> aClass.getName()).join("-");
                if (str1.compareTo(str2) < 0) {
                    bestMatch = javaMethod;
                }
            }
        }
        return bestMatch;
    }

    public int distanceFromValues(Method javaMethod, List<Object> argValues) {
        Class<?>[] parameterTypes = javaMethod.getParameterTypes();
        if(argValues.size() == parameterTypes.length) {
            int distance = 0;
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> parameterType = parameterTypes[i];
                Object o = argValues.get(i);
                if (o != null) {
                    Class<?> runtimeClass = o.getClass();
                    if (!parameterType.isAssignableFrom(runtimeClass)) {
                        return Integer.MAX_VALUE;
                    } else {
                        int paramDistance = distanceTo(runtimeClass, parameterType);
                        if (paramDistance == Integer.MAX_VALUE) {
                            return Integer.MAX_VALUE;
                        }
                        distance += paramDistance;
                    }
                }
            }
            return distance;
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDisplayName() {
        return javaMethods.get(0).getName() + "()";
    }

    @Override
    public boolean isStatic() {
        for (Method javaMethod : javaMethods) {
            boolean isStatic = Modifier.isStatic(javaMethod.getModifiers());
            if (isStatic) {
                return true;
            }
        }
        return false;
    }

    private int distanceTo(Class<?> runtimeClass, Class<?> parameterType) {
        if (runtimeClass == null) {
            return Integer.MAX_VALUE;
        }
        if (runtimeClass.equals(parameterType)) {
            return 0;
        }
        if (Arrays.asList(runtimeClass.getInterfaces()).contains(parameterType)) {
            return 1;
        }
        return 2 + distanceTo(runtimeClass.getSuperclass(), parameterType);
    }

    public boolean isValid() {
        return javaMethods.size() > 0;
    }
}
