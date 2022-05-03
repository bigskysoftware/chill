package chill.web;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class HeaderMap implements Map<String, String> {

    /**
     * This is the only thing about a flash map: a get consumes the value from the session
     */
    @Override
    public String get(Object key) {
        return WebServer.Utils.ctx.get().header(String.valueOf(key));
    }

    @Override
    public int size() {
        return WebServer.Utils.ctx.get().headerMap().size();
    }

    @Override
    public boolean isEmpty() {
        return WebServer.Utils.ctx.get().headerMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return WebServer.Utils.ctx.get().headerMap().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return WebServer.Utils.ctx.get().headerMap().containsValue(value);
    }

    @Nullable
    @Override
    public String put(String key, String value) {
        String old = WebServer.Utils.ctx.get().header(key);
        WebServer.Utils.ctx.get().header(key, value);
        return old;
    }

    @Override
    public String remove(Object key) {
        // no op
        return null;
    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends String> m) {
        for (String s : m.keySet()) {
            put(s, m.get(s));
        }
    }

    @Override
    public void clear() {
        // no op
    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return WebServer.Utils.ctx.get().headerMap().keySet();
    }

    @NotNull
    @Override
    public Collection<String> values() {
        return WebServer.Utils.ctx.get().headerMap().values();
    }

    @NotNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return WebServer.Utils.ctx.get().headerMap().entrySet();
    }

}
