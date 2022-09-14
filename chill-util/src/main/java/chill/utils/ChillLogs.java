package chill.utils;

import chill.utils.TheMissingUtils.ToString;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

import static chill.utils.ChillLogs.Level.*;
import static chill.utils.TheMissingUtils.lazyStr;

public class ChillLogs {

    private static final String DEFAULT_LOG_CATEGORY = "chill.System";
    private static ThreadLocal<TreeMap<String, Object>> CTX = new ThreadLocal<>();

    public static TheMissingUtils.SafeAutoCloseable establishCtx() {
        CTX.set(new TreeMap<>());
        return () -> closeContext();
    }

    public static void addContext(String str, Object val) {
        TreeMap<String, Object> stringObjectTreeMap = CTX.get();
        if (stringObjectTreeMap != null) {
            stringObjectTreeMap.put(str, val);
        }
    }

    public static Map<String, Object> getCurrentContext() {
        return CTX.get();
    }

    public static void closeContext() {
        CTX.set(null);
    }

    public interface Adapter {
        void log(String logName, Level level, Map<String, Object> ctx, Object log);
        void formatAndLog(String logName, Level level, Map<String, Object> ctx, String format, Object... arguments);
        void logError(String logName, Map<String, Object> ctx, Throwable t);
    }

    private static Adapter ADAPTER = new StdOutAdapter();

    public static Adapter getAdapter() {
        return ADAPTER;
    }

    public static void setAdapter(Adapter adapter) {
        ChillLogs.ADAPTER = adapter;
    }

    public enum Level {
        TRACE(0),
        DEBUG(10),
        INFO(20),
        WARN(30),
        ERROR(40),
        OFF(40);
        private final int level;
        Level(int level) {
            this.level = level;
        }
        public boolean logs(Level level) {
            return this.level <= level.level;
        }
    }

    public static final LogCategory DEFAULT_CATEGORY = get(DEFAULT_LOG_CATEGORY);

    public static LogCategory get(Object category) {
        if (category instanceof String) {
            String  str = (String) category;
            return new LogCategory(str);
        } else if (category instanceof Class) {
            Class cls = (Class) category;
            return new LogCategory(cls.getName());
        } else {
            return new LogCategory(String.valueOf(category));
        }
    }

    public static void trace(ToString msg) {
        DEFAULT_CATEGORY.trace(msg);
    }
    public static void trace(Object msg) {
        DEFAULT_CATEGORY.trace(msg);
    }
    public static void trace(String format, Object... argArray) {
        DEFAULT_CATEGORY.trace(format, argArray);
    }

    public static void debug(ToString msg) {
        DEFAULT_CATEGORY.debug(msg);
    }
    public static void debug(Object msg) {
        DEFAULT_CATEGORY.debug(msg);
    }
    public static void debug(String format, Object... argArray) {
        DEFAULT_CATEGORY.debug(format, argArray);
    }

    public static void info(ToString msg) {
        DEFAULT_CATEGORY.info(msg);
    }
    public static void info(Object msg) {
        DEFAULT_CATEGORY.info(msg);
    }
    public static void info(String format, Object... argArray) {
        DEFAULT_CATEGORY.info(format, argArray);
    }

    public static void warn(ToString msg) {
        DEFAULT_CATEGORY.warn(msg);
    }
    public static void warn(Object msg) {
        DEFAULT_CATEGORY.warn(msg);
    }
    public static void warn(String format, Object... argArray) {
        DEFAULT_CATEGORY.warn(format, argArray);
    }

    public static void error(Throwable throwable) {
        DEFAULT_CATEGORY.error(throwable);
    }
    public static void error(ToString msg) {
        DEFAULT_CATEGORY.error(msg);
    }
    public static void error(Object msg) {
        DEFAULT_CATEGORY.error(msg);
    }
    public static void error(String format, Object... argArray) {
        DEFAULT_CATEGORY.error(format, argArray);
    }

    public static class LogCategory {

        public String categoryName;

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        private LogCategory(){}

        public LogCategory(String categoryName) {
            this.categoryName = categoryName;
        }

        private void logError(Throwable throwable) {
            ADAPTER.logError(categoryName, getCurrentContext(), throwable);
        }

        private void log(Level level, Object log) {
            ADAPTER.log(categoryName, level, getCurrentContext(), log);
        }

        private  void formatAndLog(Level level, String format, Object... arguments) {
            ADAPTER.formatAndLog(categoryName, level, getCurrentContext(), format, arguments);
        }

        public void trace(ToString msg) {
            log(TRACE, lazyStr(msg));
        }
        public void trace(Object msg) {
            log(TRACE, msg);
        }
        public void trace(String format, Object... argArray) {
            formatAndLog(TRACE, format, argArray);
        }

        public void debug(ToString msg) {
            log(DEBUG, lazyStr(msg));
        }
        public void debug(Object msg) {
            log(DEBUG, msg);
        }
        public void debug(String format, Object... argArray) {
            formatAndLog(DEBUG, format, argArray);
        }

        public void info(ToString msg) {
            log(INFO, lazyStr(msg));
        }
        public void info(Object msg) {
            log(INFO, msg);
        }
        public void info(String format, Object... argArray) {
            formatAndLog(INFO, format, argArray);
        }

        public void warn(ToString msg) {
            log(WARN, lazyStr(msg));
        }
        public void warn(Object msg) {
            log(WARN, msg);
        }
        public void warn(String format, Object... argArray) {
            formatAndLog(WARN, format, argArray);
        }

        public void error(Throwable throwable) {
            logError(throwable);
        }
        public void error(ToString msg) {
            log(ERROR, lazyStr(msg));
        }
        public void error(Object msg) {
            log(ERROR, msg);
        }
        public void error(String format, Object... argArray) {
            formatAndLog(ERROR, format, argArray);
        }

    }

    public static class StdOutAdapter implements Adapter {
        private Level adapterLevel = INFO;

        @Override
        public void log(String logName, Level messageLevel, Map<String, Object> ctx, Object log) {
            formatAndLog(logName, messageLevel, ctx,"%s", log);
        }

        @Override
        public void formatAndLog(String logName, Level messageLevel, Map<String, Object> ctx, String format, Object... arguments) {
            if (adapterLevel.logs(messageLevel)) {
                format = format.replace("{}", "%s"); // support slf4j log style formatting
                String formattedLogMessage = String.format(format, arguments);
                String finalLogMessage;
                if (ctx != null) {
                    finalLogMessage = String.format("%-5s [%s] - %s %s", messageLevel, LocalDateTime.now(), ctx, formattedLogMessage);
                } else {
                    finalLogMessage = String.format("%-5s [%s] - %s", messageLevel, LocalDateTime.now(), formattedLogMessage);
                }
                print(finalLogMessage);
            }
        }

        @Override
        public void logError(String logName, Map<String, Object> ctx, Throwable t) {
            formatAndLog(logName, ERROR, ctx, "An exception occurred: %s", t.getMessage());
            t.printStackTrace();
        }

        public void print(String finalLogMessage) {
            System.out.println(finalLogMessage);
        }

        public void setLevel(Level level) {
            this.adapterLevel = level;
        }
    }
}
