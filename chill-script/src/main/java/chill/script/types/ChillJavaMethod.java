package chill.script.types;

import chill.script.types.coercions.Coercion;
import chill.utils.NiceList;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import static chill.utils.TheMissingUtils.forceThrow;
import static chill.utils.TheMissingUtils.nice;
import static java.lang.Integer.MAX_VALUE;

public class ChillJavaMethod implements ChillMethod {

    private final NiceList<Method> javaMethods;
    private final String name;
    private final Class backingClass;

    public ChillJavaMethod(String methodName, Class backingClass) {
        this.name = methodName;
        this.backingClass = backingClass;
        this.javaMethods = new NiceList<>();
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
            if (javaMethods.isEmpty()) {
                throw new NoSuchMethodException("No methods named " + this.name + " were found on " + backingClass.getName());
            }
            Method method = bestMatch(args);
            if (method == null) {
                String argTypes = nice(args).map(o -> o.getClass().getName()).join(",");
                throw new IllegalArgumentException("Could not find compatible method with args (" + argTypes + ")");
            }
            for (int i = 0; i < args.size(); i++) {
                Object arg = args.get(i);
                Class<?> parameterType = method.getParameterTypes()[i];
                Class<?> runtimeType = arg.getClass();
                if (!parameterType.isAssignableFrom(runtimeType)) {
                    Coercion coercer = Coercion.resolve(runtimeType, parameterType);
                    if (coercer == null) {
                        throw new IllegalArgumentException("Could not figure out how to convert argument " + i + " of type " + runtimeType.getName() + " to " + parameterType.getName());
                    } else {
                        args.set(i, coercer.coerce(arg));
                    }
                }
            }
            return method.invoke(rootVal, args.toArray());
        } catch (Exception e) {
            throw forceThrow(e);
        }
    }

    private Method bestMatch(List<Object> argValues) {
        int closestDistance = MAX_VALUE;
        Method bestMatch = null;
        for (Method javaMethod : javaMethods) {
            int distance = distanceFromValues(javaMethod, argValues);
            if (distance == MAX_VALUE) {
                continue;
            } else if (distance < closestDistance) {
                closestDistance = distance;
                bestMatch = javaMethod;
            } else if (distance == closestDistance) {
                // stable resolution by using string comparison
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
                    int paramDistance = distanceTo(runtimeClass, parameterType);
                    if (paramDistance == MAX_VALUE) {
                        return MAX_VALUE;
                    }
                    distance += paramDistance;
                }
            }
            return distance;
        }
        return MAX_VALUE;
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
        return javaMethods.anyMatch((m) -> Modifier.isStatic(m.getModifiers()));
    }

    @Override
    public boolean isPublic() {
        return javaMethods.anyMatch((m) -> Modifier.isPublic(m.getModifiers()));
    }

    @Override
    public boolean canInvokeWith(List<Object> args) {
        Method method = bestMatch(args);
        return method != null;
    }

    private int distanceTo(Class<?> from, Class<?> to) {
        if (from == null) {
            return MAX_VALUE;
        }
        if (from.equals(to)) {
            return 0;
        }
        if (Arrays.asList(from.getInterfaces()).contains(to)) {
            return 1;
        }
        Coercion coercion = Coercion.resolve(from, to);
        if (coercion != null) {
            return coercion.getRank();
        }
        int distanceToSuperclass = distanceTo(from.getSuperclass(), to);
        if (distanceToSuperclass == MAX_VALUE) {
            return MAX_VALUE;
        } else {
            return 2 + distanceToSuperclass;
        }
    }

    public boolean isValid() {
        return javaMethods.size() > 0;
    }
}
