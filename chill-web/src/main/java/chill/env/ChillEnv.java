package chill.env;

import chill.config.ChillApp;
import chill.utils.ChillLogs;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Path;

import static chill.utils.TheMissingUtils.newInstance;
import static chill.utils.TheMissingUtils.safely;

public class ChillEnv {

    public static ChillLogs.LogCategory LOG = ChillLogs.get(ChillEnv.class);
    public static final String FILE_PREFIX = "file:";
    public static final String RESOURCE_PREFIX = "resource:";
    public static final String[] CONFIG_PATH = {
            "file:./chill.local.toml",
            "file:./chill.toml",
            "resource:/config/chill.toml",
    };

    public static final String DEFAULT_REDIS_URL = "redis://localhost:6379";
    public static final Integer DEFAULT_PORT = 8800;

    public static final ChillMode MODE = new ChillMode();
    public static final ChillEnv INSTANCE = new ChillEnv();

    private final TomlParseResult tomlFile;

    public ChillEnv() {
        LOG.info("Creating The Chill Environment");
        this.tomlFile = resolveTOMLFile();
    }

    public static final ChillEnvVar<Integer> PORT = envVar("PORT", Integer.class).withDefault(DEFAULT_PORT);
    public static final ChillEnvVar<String> DB_URL = envVar("DB_URL", String.class);
    public static final ChillEnvVar<String> DB_DRIVER = envVar("DB_DRIVER", String.class);
    public static final ChillEnvVar<String> DB_USERNAME = envVar("DB_USERNAME", String.class);
    public static final ChillEnvVar<String> DB_PASSWORD = envVar("DB_PASSWORD", String.class);
    public static final ChillEnvVar<String> DB_CONNECTION_POOL_CONFIG = envVar("DB_CONNECTION_POOL_CONFIG", String.class);
    public static final ChillEnvVar<String> REDIS_URL = envVar("REDIS_URL", String.class).withDefault(DEFAULT_REDIS_URL);
    public static final ChillEnvVar<String> S3_REGION = envVar("S3.REGION", String.class);
    public static final ChillEnvVar<String> S3_ACCESS_KEY = envVar("S3.ACCESS_KEY", String.class);
    public static final ChillEnvVar<String> S3_SECRET_KEY = envVar("S3.SECRET_KEY", String.class);

    public static String getRedisURL() {
        return REDIS_URL.get();
    }

    public static int getPort() {
        return PORT.get();
    }

    public static boolean isDevMode() {
        return MODE.isDev();
    }

    public static void setMode(ChillMode.Modes mode, String comment) {
        MODE.setManualValue(mode, comment);
    }

    public static String getS3Region() {
        return S3_REGION.get();
    }

    public static String getS3AccessKey() {
        return S3_ACCESS_KEY.get();
    }

    public static String getS3SecretKey() {
        return S3_SECRET_KEY.require();
    }

    public static int getS3MockPort() {
        return getPort() + 2;
    }

    public static int getJobDashboardPort() {
        return getPort() + 1;
    }

    public static void init(ChillApp cmdLine) {
        LOG.info("Initializing The Chill Environment");

        Field[] fields = ChillEnv.class.getFields();
        for (Field field : fields) {
            if (ChillEnvVar.class.isAssignableFrom(field.getType())) {
                var envVar = (ChillEnvVar) safely(()->field.get(null));
                if (envVar != null) {
                    envVar.initialize(cmdLine);
                }
            }
        }

        LOG.info("Chill Environment: ");
        for (Field field : fields) {
            if (ChillEnvVar.class.isAssignableFrom(field.getType())) {
                var envVar = (ChillEnvVar) safely(()->field.get(null));
                if (envVar != null) {
                    LOG.info("    " + envVar);
                }
            }
        }
    }

    public static void init() {
        init(new ChillApp());
    }

    public TomlParseResult getToml() {
        return tomlFile;
    }

    public String getMode() {
        return MODE.get() == null ? null : MODE.stringValue();
    }

    protected static <T> ChillEnvVar<T> envVar(String name, Class<T> type) {
        return new ChillEnvVar(name, type);
    }

    private static TomlParseResult resolveTOMLFile() {

        for (String path : CONFIG_PATH) {
            if (path.startsWith(FILE_PREFIX)) {
                Path of = Path.of(path.substring(FILE_PREFIX.length()));
                if (of.toFile().exists()) {
                    LOG.info("Found chill config file " + of.toAbsolutePath() + ", loading config...");
                    return safely(() -> Toml.parse(of));
                }
            } else {
                String resourcePath = path.substring(RESOURCE_PREFIX.length());
                InputStream resourceAsStream = ChillEnv.class.getResourceAsStream(resourcePath);
                if (resourceAsStream != null) {
                    LOG.info("Found chill config file " + path + ", loading config...");
                    return safely(() -> Toml.parse(resourceAsStream));
                }
            }
        }

        LOG.info("No chill config file found.  We looked here:");
        for (String path : CONFIG_PATH) {
            if (path.startsWith(FILE_PREFIX)) {
                Path of = Path.of(path.substring(FILE_PREFIX.length()));
                LOG.info(" - " + of.toAbsolutePath());
            } else {
                LOG.info(" - " + path);
            }
        }
        return null;
    }
}
