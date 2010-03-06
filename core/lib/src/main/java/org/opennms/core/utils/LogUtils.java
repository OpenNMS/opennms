package org.opennms.core.utils;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;

public class LogUtils {
    public static void tracef(Object logee, String format, Object... args) {
        tracef(logee, null, format, args);
    }

    public static void tracef(Object logee, Throwable throwable, String format, Object... args) {
        Logger log = getLogger(logee);
        if (log.isTraceEnabled()) {
            if (throwable == null) {
                log.trace(String.format(format, args));
            } else {
                log.trace(String.format(format, args), throwable);
            }
        }
    }

    public static void debugf(Object logee, String format, Object... args) {
        debugf(logee, null, format, args);
    }

    public static void debugf(Object logee, Throwable throwable, String format, Object... args) {
        Logger log = getLogger(logee);
        if (log.isDebugEnabled()) {
            if (throwable == null) {
                log.debug(String.format(format, args));
            } else {
                log.debug(String.format(format, args), throwable);
            }
        }
    }

    public static void infof(Object logee, String format, Object... args) {
        infof(logee, null, format, args);
    }

    public static void infof(Object logee, Throwable throwable, String format, Object... args) {
        Logger log = getLogger(logee);
        if (log.isInfoEnabled()) {
            if (throwable == null) {
                log.info(String.format(format, args));
            } else {
                log.info(String.format(format, args), throwable);
            }
        }
    }

    public static void warnf(Object logee, String format, Object... args) {
        warnf(logee, null, format, args);
    }

    public static void warnf(Object logee, Throwable throwable, String format, Object... args) {
        Logger log = getLogger(logee);
        if (log.isWarnEnabled()) {
            if (throwable == null) {
                log.warn(String.format(format, args));
            } else {
                log.warn(String.format(format, args), throwable);
            }
        }
    }

    public static void errorf(Object logee, String format, Object... args) {
        errorf(logee, null, format, args);
    }

    public static void errorf(Object logee, Throwable throwable, String format, Object... args) {
        Logger log = getLogger(logee);
        if (log.isErrorEnabled()) {
            if (throwable == null) {
                log.error(String.format(format, args));
            } else {
                log.error(String.format(format, args), throwable);
            }
        }
    }

    public static void fatalf(Object logee, String format, Object... args) {
        errorf(logee, null, format, args);
    }

    public static void fatalf(Object logee, Throwable throwable, String format, Object... args) {
        errorf(logee, null, format, args);
    }

    public static void logToConsole() {
    	Properties logConfig = new Properties();
    	logConfig.setProperty("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
    	logConfig.setProperty("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
    	logConfig.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "%d %-5p [%t] %c: %m%n");
    	PropertyConfigurator.configure(logConfig);
    }

    public static void logToFile(String file) {
    	Properties logConfig = new Properties();
    	logConfig.setProperty("log4j.appender.FILE", "org.apache.log4j.RollingFileAppender");
    	logConfig.setProperty("log4j.appender.FILE.MaxFileSize", "100MB");
    	logConfig.setProperty("log4j.appender.FILE.MaxBackupIndex", "4");
    	logConfig.setProperty("log4j.appender.FILE.File", file);
    	logConfig.setProperty("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
    	logConfig.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "%d %-5p [%t] %c: %m%n");
    	PropertyConfigurator.configure(logConfig);
    }
    
	public static void enableDebugging() {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
	}

	private static Logger getLogger(Object logee) {
        Logger log;
        if (logee instanceof String) {
            log = ThreadCategory.getSlf4jInstance((String)logee);
        } else if (logee instanceof Class<?>) {
            log = ThreadCategory.getSlf4jInstance((Class<?>)logee);
        } else {
            log = ThreadCategory.getSlf4jInstance(logee.getClass());
        }
        return log;
    }

}
