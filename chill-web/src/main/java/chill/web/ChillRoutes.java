package chill.web;

import chill.utils.ChillLogs;
import chill.utils.TheMissingUtils;
import chill.utils.TheMissingUtils.DangerousRunnable;
import io.javalin.core.JavalinConfig;
import org.jetbrains.annotations.NotNull;

import static chill.utils.TheMissingUtils.safely;
import static chill.web.WebServer.Utils.getJavalin;

public abstract class ChillRoutes {

    public abstract void init();
    private ChillLogs.LogCategory log = ChillLogs.get(this.getClass());

    protected void include(Class<? extends ChillRoutes> otherRoutesFile) {
        ChillRoutes chillRoutes = TheMissingUtils.newInstance(otherRoutesFile);
        chillRoutes.init();
    }

    //===============================================
    //  No Args
    //===============================================

    public void all(@NotNull String path, @NotNull DangerousRunnable handler) {
        get(path, handler);
        post(path, handler);
        put(path, handler);
        patch(path, handler);
        delete(path, handler);
        head(path, handler);
        options(path, handler);
    }

    public void get(@NotNull String path, @NotNull DangerousRunnable handler) {
        getJavalin().get(path, new ChillHandler(handler));
    }

    public void post(@NotNull String path, @NotNull DangerousRunnable handler) {
        getJavalin().post(path, new ChillHandler(handler));
    }

    public void put(@NotNull String path, @NotNull DangerousRunnable handler) {
        getJavalin().put(path, new ChillHandler(handler));
    }

    public void patch(@NotNull String path, @NotNull DangerousRunnable handler) {
        getJavalin().patch(path, new ChillHandler(handler));
    }

    public void delete(@NotNull String path, @NotNull DangerousRunnable handler) {
        getJavalin().delete(path, new ChillHandler(handler));
    }

    public void head(@NotNull String path, @NotNull DangerousRunnable handler) {
        getJavalin().head(path, new ChillHandler(handler));
    }

    public void options(@NotNull String path, @NotNull DangerousRunnable handler) {
        getJavalin().options(path, ctx -> executeHandler(handler));
    }

    private void executeHandler(DangerousRunnable handler) {
        TheMissingUtils.safely(() -> handler.run());
    }

    public void configJavalin(JavalinConfig config) {
        // for subclass override
    }

    //=====================================
    // Log delegation
    //=====================================
    protected void trace(TheMissingUtils.ToString msg) {
        log.trace(msg);
    }
    protected void trace(Object msg) {
        log.trace(msg);
    }
    protected void trace(String format, Object... argArray) {
        log.trace(format, argArray);
    }

    protected void debug(TheMissingUtils.ToString msg) {
        log.debug(msg);
    }
    protected void debug(Object msg) {
        log.debug(msg);
    }
    protected void debug(String format, Object... argArray) {
        log.debug(format, argArray);
    }

    protected void info(TheMissingUtils.ToString msg) {
        log.info(msg);
    }
    protected void info(Object msg) {
        log.info(msg);
    }
    protected void info(String format, Object... argArray) {
        log.info(format, argArray);
    }

    protected void warn(TheMissingUtils.ToString msg) {
        log.warn(msg);
    }
    protected void warn(Object msg) {
        log.warn(msg);
    }
    protected void warn(String format, Object... argArray) {
        log.warn(format, argArray);
    }

    protected void error(Throwable throwable) {
        log.error(throwable);
    }
    protected void error(TheMissingUtils.ToString msg) {
        log.error(msg);
    }
    protected void error(Object msg) {
        log.error(msg);
    }
    protected void error(String format, Object... argArray) {
        log.error(format, argArray);
    }

}
