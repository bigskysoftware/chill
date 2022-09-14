package chill.script.types;

import java.util.concurrent.ConcurrentHashMap;

public class TypeSystem {

    private static final ConcurrentHashMap<String, ChillType> TYPESYSTEM_CACHE = new ConcurrentHashMap<>();

    public static ChillType getRuntimeType(Object rootVal) {
        if (rootVal instanceof HasCustomChillType) {
            HasCustomChillType hasChillType = (HasCustomChillType) rootVal;
            return hasChillType.getChillType();
        } else {
            Class<?> aClass = rootVal.getClass();
            return getType(aClass);
        }
    }

    public static ChillType getType(Class<?> aClass) {
        return TYPESYSTEM_CACHE.computeIfAbsent(aClass.getSimpleName(), (name) -> getRuntimeTypeNoCache(aClass));
    }

    private static ChillType getRuntimeTypeNoCache(Class aClass) {
        return new JavaChillType(aClass);
    }
}
