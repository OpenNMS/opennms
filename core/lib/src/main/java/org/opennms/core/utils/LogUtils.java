package org.opennms.core.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class LogUtils {
    
    public static void tracef(Object logee, String format, Object... args) {
        logf(Level.TRACE, logee, null, format, args);
    }

    public static void tracef(Object logee, Throwable throwable, String format, Object... args) {
        logf(Level.TRACE, logee, throwable, format, args);
    }

    public static void debugf(Object logee, String format, Object... args) {
        logf(Level.DEBUG, logee, null, format, args);
    }

    public static void debugf(Object logee, Throwable throwable, String format, Object... args) {
        logf(Level.DEBUG, logee, throwable, format, args);
    }

    public static void infof(Object logee, String format, Object... args) {
        logf(Level.INFO, logee, null, format, args);
    }

    public static void infof(Object logee, Throwable throwable, String format, Object... args) {
        logf(Level.INFO, logee, throwable, format, args);
    }

    public static void warnf(Object logee, String format, Object... args) {
        logf(Level.WARN, logee, null, format, args);
    }

    public static void warnf(Object logee, Throwable throwable, String format, Object... args) {
        logf(Level.WARN, logee, throwable, format, args);
    }

    public static void errorf(Object logee, String format, Object... args) {
        logf(Level.ERROR, logee, null, format, args);
    }

    public static void errorf(Object logee, Throwable throwable, String format, Object... args) {
        logf(Level.ERROR, logee, throwable, format, args);
    }

    public static void fatalf(Object logee, String format, Object... args) {
        logf(Level.FATAL, logee, null, format, args);
    }

    public static void fatalf(Object logee, Throwable throwable, String format, Object... args) {
        logf(Level.FATAL, logee, throwable, format, args);
    }

    public static void logf(Level level, Object logee, Throwable throwable, String format, Object... args) {
        Logger log = getLogger(logee);
        if (log.isEnabledFor(level)) {
            if (throwable == null) {
                log.log(level, String.format(format, args));
            } else {
                log.log(level, String.format(format, args), throwable);
            }
        }
    }

    private static Logger getLogger(Object logee) {
        Logger log;
        if (logee instanceof String) {
            log = ThreadCategory.getInstance((String)logee);
        } else if (logee instanceof Class<?>) {
            log = ThreadCategory.getInstance((Class<?>)logee);
        } else {
            log = ThreadCategory.getInstance(logee.getClass());
        }
        return log;
    }
}
