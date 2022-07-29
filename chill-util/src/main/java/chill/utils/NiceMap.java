package chill.utils;

import java.util.*;

public class NiceMap<K, T> implements Map<K, T> {

    private final Map<K, T> delegate;

    public NiceMap() {
        delegate = new LinkedHashMap<>();
    }

    public NiceMap(Map<K, T> c) {
        delegate = c;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    @Override
    public T get(Object key) {
        return delegate.get(key);
    }

    @Override
    public T put(K key, T value) {
        return delegate.put(key, value);
    }

    @Override
    public T remove(Object key) {
        return delegate.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends T> m) {
        delegate.putAll(m);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public Set<K> keySet() {
        return delegate.keySet();
    }

    @Override
    public Collection<T> values() {
        return delegate.values();
    }

    @Override
    public Set<Entry<K, T>> entrySet() {
        return delegate.entrySet();
    }
}
