package chill.script.types;

import chill.utils.NiceList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static chill.script.runtime.ChillScriptRuntime.UNDEFINED;
import static chill.script.types.ChillJavaProperty.propertyNameFor;

public class JavaChillType implements ChillType, PropertyMissing {

    private final Class backingClass;
    private final ConcurrentHashMap<String, ChillJavaProperty> properties = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ChillMethod> methods = new ConcurrentHashMap<>();

    public JavaChillType(Class backingClass) {
        this.backingClass = backingClass;
    }

    @Override
    public ChillProperty getProperty(String propName) {
        return properties.computeIfAbsent(propName, this::resolveProperty);
    }

    @Override
    public String getDisplayName() {
        return backingClass.getName();
    }

    @Override
    public List<ChillMethod> getMethods() {

        NiceList<Method> methods = new NiceList<>(backingClass.getMethods());

        return methods.distinct(Method::getName)
                .map(method -> getMethod(method.getName()));

    }

    @Override
    public List<ChillProperty> getProperties() {

        NiceList<Method> methods = new NiceList<>(backingClass.getMethods());
        NiceList<ChillProperty> methodProperties = methods.filter(ChillJavaProperty::isPropertyMethod)
                .map(method -> getProperty(propertyNameFor(method)));

        NiceList<Field> fields = new NiceList<>(backingClass.getFields());
        NiceList<ChillProperty> fieldProperties = fields.map(field -> getProperty(field.getName()));

        return methodProperties.concat(fieldProperties);
    }

    @Override
    public List<ChillMethod> getDeclaredMethods() {

        NiceList<Method> methods = new NiceList<>(backingClass.getDeclaredMethods());

        return methods.distinct(Method::getName)
                .map(method -> getMethod(method.getName())).removeNulls();

    }

    @Override
    public List<ChillProperty> getDeclaredProperties() {

        NiceList<Method> methods = new NiceList<>(backingClass.getDeclaredMethods());
        NiceList<ChillProperty> methodProperties = methods.filter(ChillJavaProperty::isPropertyMethod)
                .map(method -> getProperty(propertyNameFor(method)));

        NiceList<Field> fields = new NiceList<>(backingClass.getDeclaredFields());
        NiceList<ChillProperty> fieldProperties = fields.map(field -> getProperty(field.getName())).removeNulls();

        return methodProperties.concat(fieldProperties);
    }

    @Override
    public ChillMethod getMethod(String methodName) {
        return methods.computeIfAbsent(methodName, this::resolveMethod);
    }

    public ChillJavaProperty resolveProperty(String propName) {
        var chillJavaProperty = new ChillJavaProperty(backingClass, propName);
        if (chillJavaProperty.isValid()) {
            return chillJavaProperty;
        } else {
            return null;
        }
    }

    public ChillMethod resolveMethod(String methodName) {
        var javaMethod = new ChillJavaMethod(methodName, backingClass);
        if (javaMethod.isValid()) {
            return javaMethod;
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return "JavaChillType: " + backingClass.getName();
    }

    @Override
    public Object propertyMissing(String propName) {
        ChillProperty property = getProperty(propName);
        if (property != null && property.isStatic()) {
            return property.get(null);
        } else {
            return UNDEFINED;
        }
    }
}
