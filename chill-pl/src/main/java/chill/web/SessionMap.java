package chill.web;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import static chill.web.WebServer.Utils.*;

public class SessionMap implements Map<String, Object> {

    /**
     * This is the only thing about a flash map: a get consumes the value from the session
     */
    @Override
    public Object get(Object key) {
        return ctx.get().sessionAttribute(String.valueOf(key));
    }

    @Override
    public int size() {
        return ctx.get().sessionAttributeMap().size();
    }

    @Override
    public boolean isEmpty() {
        return ctx.get().sessionAttributeMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return ctx.get().sessionAttributeMap().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return ctx.get().sessionAttributeMap().containsValue(value);
    }

    @Nullable
    @Override
    public Object put(String key, Object value) {
        Object old = ctx.get().sessionAttribute(key);
        ctx.get().sessionAttribute(key, value);
        return old;
    }

    @Override
    public Object remove(Object key) {
        return ctx.get().consumeSessionAttribute(String.valueOf(key));
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ?> m) {
        for (String s : m.keySet()) {
            put(s, m.get(s));
        }
    }

    @Override
    public void clear() {
        Map<String, Object> stringObjectMap = ctx.get().sessionAttributeMap();
        for (String s : stringObjectMap.keySet()) {
            ctx.get().consumeSessionAttribute(s);
        }
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return ctx.get().sessionAttributeMap().keySet();
    }

    @NotNull
    @Override
    public Collection<Object> values() {
        return ctx.get().sessionAttributeMap().values();
    }

    @NotNull
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return ctx.get().sessionAttributeMap().entrySet();
    }

}
