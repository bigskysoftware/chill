package chill.web;

public class FlashMap extends SessionMap {
    public boolean has(String str) {
        return containsKey(str);
    }

    @Override
    public Object get(Object key) {
        return WebServer.Utils.ctx.get().consumeSessionAttribute(String.valueOf(key));
    }
}
