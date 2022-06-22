package chill.web;

public class FlashMap extends SessionMap {

    public static final String SUCCESS_SLOT = "success";
    public static final String INFO_SLOT = "info";
    public static final String WARN_SLOT = "warn";
    public static final String ERROR_SLOT = "error";

    public boolean has(String str) {
        return containsKey(str);
    }

    public void success(String str) {
        put(SUCCESS_SLOT, str);
    }

    public void info(String str) {
        put(INFO_SLOT, str);
    }

    public void warn(String str) {
        put(WARN_SLOT, str);
    }
    public void error(String str) {
        put(ERROR_SLOT, str);
    }

    public boolean hasSuccessMessage() {
        return has(SUCCESS_SLOT);
    }

    public boolean hasInfoMessage() {
        return has(INFO_SLOT);
    }

    public boolean hasWarningMessage() {
        return has(WARN_SLOT);
    }

    public boolean hasErrorMessage() {
        return has(ERROR_SLOT);
    }

    public Object getSuccessMessage() {
        return get(SUCCESS_SLOT);
    }

    public Object getInfoMessage() {
        return get(INFO_SLOT);
    }

    public Object getWarningMessage() {
        return get(WARN_SLOT);
    }

    public Object getErrorMessage() {
        return get(ERROR_SLOT);
    }

    @Override
    public Object get(Object key) {
        return WebServer.Utils.ctx.get().consumeSessionAttribute(String.valueOf(key));
    }
}
