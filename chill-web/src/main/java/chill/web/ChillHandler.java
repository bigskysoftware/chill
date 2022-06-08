package chill.web;

import chill.utils.ChillLogs;
import chill.utils.TheMissingUtils;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

class ChillHandler implements Handler {
    private final TheMissingUtils.DangerousRunnable wrapped;
    private final String definedAt;

    public ChillHandler(TheMissingUtils.DangerousRunnable wrappedHandler) {
        this.wrapped = wrappedHandler;
        this.definedAt = getDefinitionLocation();
    }

    private String getDefinitionLocation() {
        StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            if (!stackTraceElement.getClassName().startsWith("chill.")) {
                return stackTraceElement.getFileName() + ":" + stackTraceElement.getLineNumber();
            }
        }
        return "<unknown handler>";
    }

    @Override
    public void handle(@NotNull Context ctx) throws Exception {
        UUID uuid = UUID.randomUUID();
        ChillLogs.establishCtx();
        ChillLogs.addContext("request-id", uuid);
        long requestStartTime = System.currentTimeMillis();
        WebServer.LOG.info("Handling  `" + ctx.method() + " " + ctx.path() + "` with handler defined at " + definedAt);
        wrapped.run();
        long totalTime = System.currentTimeMillis() - requestStartTime;
        WebServer.LOG.info("  Finished `" + ctx.method() + " : " + ctx.path() + "` (" + totalTime + " ms)");
        ChillLogs.closeContext();
    }
}
