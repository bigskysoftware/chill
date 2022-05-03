package chill.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DynaProperties {

    private static final ConcurrentReferenceHashMap<Object, ConcurrentHashMap<String, Object>> DYNA_PROPERTIES = new ConcurrentReferenceHashMap<>();

    public static boolean hasDynaProps(Object o) {
        return DYNA_PROPERTIES.containsKey(o);
    }

    public static Map<String, Object> forObject(Object o) {
        ConcurrentHashMap<String, Object> propertyMap =  DYNA_PROPERTIES.computeIfAbsent(o,
                obj -> new ConcurrentHashMap<>());
        return propertyMap;
    }

}
