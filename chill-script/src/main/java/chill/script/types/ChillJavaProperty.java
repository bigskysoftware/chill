package chill.script.types;

import chill.utils.TheMissingUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ChillJavaProperty implements ChillProperty {

    public static final String GET_PREFIX = "get";
    public static final String SET_PREFIX = "set";
    public static final String IS_PREFIX = "is";

    private final Getter getter;
    private final Setter setter;

    private final Class parentClass;
    private final String name;

    public static boolean isPropertyMethod(Method method) {
        if (method.getParameterTypes().length == 0) {
            String name = method.getName();
            if (name.startsWith("get")) {
                return true;
            } else if (name.startsWith("is")) {
                return true;
            } else if (TheMissingUtils.isLowerCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static String propertyNameFor(Method method) {
        String name = method.getName();
        if (name.startsWith("get")) {
            return name.substring(3);
        } else if (method.getName().startsWith("is")) {
            return name.substring(2);
        } else if (TheMissingUtils.isLowerCase(method.getName())) {
            return name;
        }
        return null;
    }

    public boolean isValid() {
        return getter != null;
    }

    public boolean isStatic() {
        return getter != null && getter.isStatic();
    }

    @Override
    public ChillType getType() {
        return TypeSystem.getType(getter.getType());
    }

    public ChillJavaProperty(Class aClass, String propName) {
        this.getter = resolveGetter(aClass, propName);
        this.setter = getter == null ? null : getter.resolveSetter();
        this.parentClass = aClass;
        this.name = propName;
    }

    private Getter resolveGetter(Class aClass, String propName) {
        String javaPropertyName = getJavaPropertyName(propName);
        String lowercase = propName.toLowerCase();
        String[] methodPossibilities;

        // if the prop name is all lowercase, try to resolve a no prefix version of the getter
        if (lowercase.equals(propName)) {
            methodPossibilities = new String[]{GET_PREFIX + javaPropertyName, IS_PREFIX + javaPropertyName, lowercase};
        } else {
            methodPossibilities = new String[]{GET_PREFIX + javaPropertyName, IS_PREFIX + javaPropertyName};
        }

        for (String methodName : methodPossibilities) {
            try {
                return new MethodGetter(aClass.getMethod(methodName));
            } catch (NoSuchMethodException nsme) {
                // ignore
            }
        }
        String[] fieldPossibilites = {propName, javaPropertyName, TheMissingUtils.decapitalize(propName)};
        for (String fieldName : fieldPossibilites) {
            try {
                return new FieldAccessor(aClass.getField(fieldName));
            } catch (NoSuchFieldException nsfe) {
                // ignore
            }
        }
        return null;
    }

    private String getJavaPropertyName(String propName) {
        String desnaked = TheMissingUtils.desnake(propName);
        return TheMissingUtils.capitalize(desnaked);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCanonicalName() {
        return TheMissingUtils.snake(getName());
    }

    @Override
    public Object get(Object root) {
        if (getter == null) {
            return null;
        }
        try {
            return getter.get(root);
        } catch (Exception e) {
            throw TheMissingUtils.forceThrow(e);
        }
    }

    @Override
    public void set(Object owner, Object val) {
        setter.set(owner, val);
    }

    private interface Getter {
        Object get(Object root);
        Class getType();
        Setter resolveSetter();
        boolean isStatic();
    }

    private interface Setter {
        void set(Object root, Object value);
    }

    private class MethodGetter implements Getter {
        private final Method method;
        public MethodGetter(Method method) {
            this.method = method;
        }

        @Override
        public Object get(Object root) {
            try {
                return method.invoke(root);
            } catch (Exception e) {
                throw TheMissingUtils.forceThrow(e);
            }
        }

        @Override
        public Class getType() {
            return method.getReturnType();
        }

        @Override
        public Setter resolveSetter() {
            try {
                String setterName = method.getName();
                // flip get to set
                if (setterName.startsWith("get")) {
                    setterName = "set" + setterName.substring(3);
                }
                return new MethodSetter(method.getDeclaringClass().getMethod(setterName, method.getReturnType()));
            } catch (NoSuchMethodException e) {
                return null;
            }
        }

        @Override
        public boolean isStatic() {
            return Modifier.isStatic(method.getModifiers());
        }
    }

    private class FieldAccessor implements Getter, Setter {
        private final Field field;

        public FieldAccessor(Field field) {
            this.field = field;
        }

        @Override
        public Object get(Object root) {
            try {
                return field.get(root);
            } catch (IllegalAccessException e) {
                throw TheMissingUtils.forceThrow(e);
            }
        }

        @Override
        public Class getType() {
            return null;
        }

        @Override
        public Setter resolveSetter() {
            return this;
        }

        @Override
        public void set(Object root, Object value) {
            try {
                field.set(root, value);
            }  catch (IllegalAccessException e) {
                throw TheMissingUtils.forceThrow(e);
            }
        }

        @Override
        public boolean isStatic() {
            return Modifier.isStatic(field.getModifiers());
        }
    }

    private class MethodSetter implements Setter {
        private final Method method;

        public MethodSetter(Method method) {
            this.method = method;
        }

        @Override
        public void set(Object root, Object value) {
            try {
                method.invoke(root, value);
            } catch (Exception e) {
                throw TheMissingUtils.forceThrow(e);
            }
        }
    }

    @Override
    public String toString() {
        return getCanonicalName();
    }
}
