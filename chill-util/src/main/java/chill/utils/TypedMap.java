package chill.utils;

import java.util.HashMap;

public class TypedMap {

    HashMap map = new HashMap();
    public static class Key<T> {}

    public <T> T get(Key<T> key) {
        return (T) map.get(key);
    }

    public <T> void set(Key<T> key, T value) {
        map.put(key, value);
    }

}
