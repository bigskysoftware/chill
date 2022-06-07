package chill.config;

import chill.utils.ChillLogs;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SLF4JSupport {

    public static void init() {
        ChillLogs.setAdapter(new Slf4JAdapter());
    }

    private static class Slf4JAdapter implements ChillLogs.Adapter {
        // TODO - pass context info through
        @Override
        public void log(String logName, ChillLogs.Level level, Map<String, Object> ctx, Object log) {
            if (level == ChillLogs.Level.ERROR) {
                LoggerFactory.getLogger(logName).error(String.valueOf(log));
            } else if (level == ChillLogs.Level.WARN) {
                LoggerFactory.getLogger(logName).warn(String.valueOf(log));
            } else if (level == ChillLogs.Level.INFO) {
                LoggerFactory.getLogger(logName).info(String.valueOf(log));
            } else if (level == ChillLogs.Level.DEBUG) {
                LoggerFactory.getLogger(logName).debug(String.valueOf(log));
            } else if (level == ChillLogs.Level.TRACE) {
                LoggerFactory.getLogger(logName).trace(String.valueOf(log));
            }
        }

        @Override
        public void formatAndLog(String logName, ChillLogs.Level level, Map<String, Object> ctx, String format, Object... arguments) {
            if (level == ChillLogs.Level.ERROR) {
                LoggerFactory.getLogger(logName).error(format, arguments);
            } else if (level == ChillLogs.Level.WARN) {
                LoggerFactory.getLogger(logName).warn(format, arguments);
            } else if (level == ChillLogs.Level.INFO) {
                LoggerFactory.getLogger(logName).info(format, arguments);
            } else if (level == ChillLogs.Level.DEBUG) {
                LoggerFactory.getLogger(logName).debug(format, arguments);
            } else if (level == ChillLogs.Level.TRACE) {
                LoggerFactory.getLogger(logName).trace(format, arguments);
            }
        }

        @Override
        public void logError(String logName, Map<String, Object> ctx, Throwable t) {
            LoggerFactory.getLogger(logName).error(t.getMessage(), t);
        }
    }
}
