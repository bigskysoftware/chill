package chill.web;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static chill.utils.TheMissingUtils.first;

public class UnifiedParams implements Map<String, String> {

    private Map<String, String> getUnifiedParamMap(){
        HashMap<String, String> map = new HashMap<>();
        map.putAll(getPathParameters());
        var queryParameters = getQueryParameters();
        for (var entry : queryParameters.entrySet()) {
            map.put(entry.getKey(), first(entry.getValue()));
        }
        var formParameters = getFormParameters();
        for (var entry : formParameters.entrySet()) {
            map.put(entry.getKey(), first(entry.getValue()));
        }
        return map;
    }
    public Map<String, String> getPathParameters() {
        return WebServer.Utils.ctx.get().pathParamMap();
    }

    public Map<String, List<String>> getQueryParameters() {
        return WebServer.Utils.ctx.get().queryParamMap();
    }

    public Map<String, List<String>> getFormParameters() {
        return WebServer.Utils.ctx.get().formParamMap();
    }

    @Override
    public int size() {
        return getUnifiedParamMap().size();
    }

    @Override
    public boolean isEmpty() {
        return getUnifiedParamMap().isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return getUnifiedParamMap().containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return getUnifiedParamMap().containsValue(value);
    }

    @Override
    public String get(Object key) {
        Context webContext = WebServer.Utils.ctx.get();
        String strKey = String.valueOf(key);
        if (webContext.pathParamMap().containsKey(strKey)) {
            return webContext.pathParamMap().get(strKey);
        }
        if (webContext.queryParamMap().containsKey(strKey)) {
            return first(webContext.queryParamMap().get(strKey));
        }
        if (webContext.formParamMap().containsKey(strKey)) {
            return first(webContext.formParamMap().get(strKey));
        }
        return null;
    }

    public List<String> getAll(Object key) {
        Context webContext = WebServer.Utils.ctx.get();
        String strKey = String.valueOf(key);
        if (webContext.pathParamMap().containsKey(strKey)) {
            return List.of(webContext.pathParamMap().get(strKey));
        }
        if (webContext.queryParamMap().containsKey(strKey)) {
            return webContext.queryParamMap().get(strKey);
        }
        if (webContext.formParamMap().containsKey(strKey)) {
            return webContext.formParamMap().get(strKey);
        }
        return null;
    }

    @Nullable
    @Override
    public String put(String key, String value) {
        throw new UnsupportedOperationException("Param Maps are immutable");
    }

    @Override
    public String remove(Object key) {
        throw new UnsupportedOperationException("Param Maps are immutable");

    }

    @Override
    public void putAll(@NotNull Map<? extends String, ? extends String> m) {
        throw new UnsupportedOperationException("Param Maps are immutable");

    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Param Maps are immutable");

    }

    @NotNull
    @Override
    public Set<String> keySet() {
        return getUnifiedParamMap().keySet();
    }

    @NotNull
    @Override
    public Collection<String> values() {
        return getUnifiedParamMap().values();
    }

    @NotNull
    @Override
    public Set<Entry<String, String>> entrySet() {
        return getUnifiedParamMap().entrySet();
    }
}
