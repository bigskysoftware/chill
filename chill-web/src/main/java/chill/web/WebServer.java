package chill.web;

import chill.env.ChillEnv;
import chill.utils.ChillLogs;
import chill.utils.TheMissingUtils;
import io.javalin.Javalin;
import io.javalin.core.util.JavalinException;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.plugin.rendering.JavalinRenderer;
import org.eclipse.jetty.server.session.DefaultSessionCache;
import org.eclipse.jetty.server.session.SessionCache;
import org.eclipse.jetty.server.session.SessionHandler;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class WebServer {

    static public ChillLogs.LogCategory LOG = ChillLogs.get(WebServer.class);

    private Javalin javalin;

    public static final WebServer INSTANCE = new WebServer();

    private WebServer(){
        javalin = initJavalin();
    }

    @NotNull
    private Javalin initJavalin() {
        final Javalin javalin;
        javalin = Javalin.create(config -> {

            config.showJavalinBanner = false;

            // store sessions in redis
            config.sessionHandler(() -> {
                SessionHandler sessionHandler = new SessionHandler();
                SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
                sessionCache.setSessionDataStore(new RedisSessionStore());
                sessionHandler.setSessionCache(sessionCache);
                sessionHandler.setHttpOnly(true);
                return sessionHandler;
            });

            //TODO webjars?
            //  config.enableWebjars();

            // static files
            try {
                config.addStaticFiles(staticFiles -> {
                    staticFiles.hostedPath = "/";                   // change to host files on a subpath, like '/assets'
                    staticFiles.directory = "/static";              // the directory where your files are located
                    staticFiles.location = Location.CLASSPATH;
                });
            } catch (JavalinException javalinException) {
                LOG.warn("Could not configure static resources: " + javalinException.getMessage());
            }

            // Register ChillTemplates
            // TODO enable caching in production, etc.
            JavalinRenderer.register(ChillTemplatesRenderer.INSTANCE, ".html");

            ChillRoutes routes = TheMissingUtils.newInstance("web.Routes");
            routes.configJavalin(config);
        });

        //===================================================
        //  Make the context available thread-wide
        //===================================================
        javalin.before(Utils::establishContext);
        javalin.after(ctx -> Utils.clearContext());


        if (ChillEnv.isDevMode()) {
            javalin.exception(Exception.class, (e, ctx) -> {
                e.printStackTrace();
                Utils.render("/system/errors.html", "exception", e);
            });
        }
        return javalin;
    }

    public void restart() {
        LOG.info("Restarting Web Server");
        javalin.stop();
        javalin = initJavalin();
        start();
    }
    public void start() {
        try {
            ChillRoutes routes = TheMissingUtils.newInstance("web.Routes");
            routes.init();
        } catch (Exception e) {
            LOG.error("Unable to load routes for the Web Application!", e);
        }

        javalin.start(ChillEnv.getPort());
    }

    public Javalin getJavalin() {
        return javalin;
    }

    public static class Utils {

        //====================================================
        // Web helper methods
        //====================================================
        private static ThreadLocal<Context> CTX = new ThreadLocal<>();

        public static final ContextGetter ctx = new ContextGetter();
        public static final FlashMap flash = new FlashMap();
        public static final SessionMap session = new SessionMap();
        public static final HeaderMap headers = new HeaderMap();
        public static final UnifiedParams params = new UnifiedParams();

        public static Javalin getJavalin() {
            return INSTANCE.getJavalin();
        }

        public static void redirect(String location) {
            ctx.get().redirect(location);
        }

        public static void raw(String content) {
            ctx.get().result(content);
        }

        @NotNull
        public static Context render(String filePath, Object... args) {
            Context context = ctx.get();
            String content = ChillTemplatesRenderer.INSTANCE.render(filePath,
                    TheMissingUtils.mapFrom(args),
                    context);
            return context.html(content);
        }

        public static void clearContext() {
            CTX.remove();
        }

        public static void establishContext(Context ctx) {
            CTX.set(ctx);
        }

        public static void flash(String key, String value) {
            flash.put(key, value);
        }

        public static boolean isGet() {
            return "GET".equals(ctx.get().method());
        }

        public static boolean isPost() {
            return "POST".equals(ctx.get().method());
        }

        public static boolean isPut() {
            return "PUT".equals(ctx.get().method());
        }

        public static boolean isDelete() {
            return "Delete".equals(ctx.get().method());
        }

        public static final class ContextGetter {
            public Context get() {
                return CTX.get();
            }
        }
    }
}
