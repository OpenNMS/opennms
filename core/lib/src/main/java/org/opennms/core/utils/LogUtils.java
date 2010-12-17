package org.opennms.core.utils;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;

/**
 * <p>LogUtils class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class LogUtils {
    /**
     * <p>tracef</p>
     *
     * @param logee a {@link java.lang.Object} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void tracef(final Object logee, final String format, final Object... args) {
        tracef(logee, null, format, args);
    }

    /**
     * <p>tracef</p>
     *
     * @param logee a {@link java.lang.Object} object.
     * @param throwable a {@link java.lang.Throwable} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void tracef(final Object logee, final Throwable throwable, final String format, final Object... args) {
        Logger log = getLogger(logee);
        if (log.isTraceEnabled()) {
            String logMessage = ((args == null || args.length < 1) ? format : String.format(format, args));
            if (throwable == null) {
                log.trace(logMessage);
            } else {
                log.trace(logMessage, throwable);
            }
        }
    }

    /**
     * <p>debugf</p>
     *
     * @param logee a {@link java.lang.Object} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void debugf(final Object logee, final String format, final Object... args) {
        debugf(logee, null, format, args);
    }

    /**
     * <p>debugf</p>
     *
     * @param logee a {@link java.lang.Object} object.
     * @param throwable a {@link java.lang.Throwable} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void debugf(final Object logee, final Throwable throwable, final String format, final Object... args) {
        Logger log = getLogger(logee);
        if (log.isDebugEnabled()) {
            String logMessage = ((args == null || args.length < 1) ? format : String.format(format, args));
            if (throwable == null) {
                log.debug(logMessage);
            } else {
                log.debug(logMessage, throwable);
            }
        }
    }

    /**
     * <p>infof</p>
     *
     * @param logee a {@link java.lang.Object} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void infof(final Object logee, final String format, final Object... args) {
        infof(logee, null, format, args);
    }

    /**
     * <p>infof</p>
     *
     * @param logee a {@link java.lang.Object} object.
     * @param throwable a {@link java.lang.Throwable} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void infof(final Object logee, final Throwable throwable, final String format, final Object... args) {
        Logger log = getLogger(logee);
        if (log.isInfoEnabled()) {
            String logMessage = ((args == null || args.length < 1) ? format : String.format(format, args));
            if (throwable == null) {
                log.info(logMessage);
            } else {
                log.info(logMessage, throwable);
            }
        }
    }

    /**
     * <p>warnf</p>
     *
     * @param logee a {@link java.lang.Object} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void warnf(final Object logee, final String format, final Object... args) {
        warnf(logee, null, format, args);
    }

    /**
     * <p>warnf</p>
     *
     * @param logee a {@link java.lang.Object} object.
     * @param throwable a {@link java.lang.Throwable} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void warnf(final Object logee, final Throwable throwable, final String format, final Object... args) {
        Logger log = getLogger(logee);
        if (log.isWarnEnabled()) {
            String logMessage = ((args == null || args.length < 1) ? format : String.format(format, args));
            if (throwable == null) {
                log.warn(logMessage);
            } else {
                log.warn(logMessage, throwable);
            }
        }
    }

    /**
     * <p>errorf</p>
     *
     * @param logee a {@link java.lang.Object} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void errorf(final Object logee, final String format, final Object... args) {
        errorf(logee, null, format, args);
    }

    /**
     * <p>errorf</p>
     *
     * @param logee a {@link java.lang.Object} object.
     * @param throwable a {@link java.lang.Throwable} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void errorf(final Object logee, final Throwable throwable, final String format, final Object... args) {
        Logger log = getLogger(logee);
        if (log.isErrorEnabled()) {
            String logMessage = ((args == null || args.length < 1) ? format : String.format(format, args));
            if (throwable == null) {
                log.error(logMessage);
            } else {
                log.error(logMessage, throwable);
            }
        }
    }

    /**
     * <p>fatalf</p>
     *
     * @deprecated SLF4J doesn't support fatal, so this just goes to {@link #errorf} anyways.
     * @param logee a {@link java.lang.Object} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void fatalf(final Object logee, final String format, final Object... args) {
        errorf(logee, null, format, args);
    }

    /**
     * <p>fatalf</p>
     *
     * @deprecated SLF4J doesn't support fatal, so this just goes to {@link #errorf} anyways.
     * @param logee a {@link java.lang.Object} object.
     * @param throwable a {@link java.lang.Throwable} object.
     * @param format a {@link java.lang.String} object.
     * @param args a {@link java.lang.Object} object.
     */
    public static void fatalf(final Object logee, final Throwable throwable, final String format, final Object... args) {
        errorf(logee, throwable, format, args);
    }

    /**
     * <p>logToConsole</p>
     */
    public static void logToConsole() {
    	final Properties logConfig = new Properties();
    	logConfig.setProperty("log4j.reset", "true");
    	logConfig.setProperty("log4j.rootCategory", "INFO, CONSOLE");
    	logConfig.setProperty("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
    	logConfig.setProperty("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
    	logConfig.setProperty("log4j.appender.CONSOLE.layout.ConversionPattern", "%d %-5p [%t] %c: %m%n");
    	PropertyConfigurator.configure(logConfig);
    }

    /**
     * <p>logToFile</p>
     *
     * @param file a {@link java.lang.String} object.
     */
    public static void logToFile(final String file) {
    	final Properties logConfig = new Properties();
    	logConfig.setProperty("log4j.reset", "true");
    	logConfig.setProperty("log4j.rootCategory", "INFO, FILE");
    	logConfig.setProperty("log4j.appender.FILE", "org.apache.log4j.RollingFileAppender");
    	logConfig.setProperty("log4j.appender.FILE.MaxFileSize", "100MB");
    	logConfig.setProperty("log4j.appender.FILE.MaxBackupIndex", "4");
    	logConfig.setProperty("log4j.appender.FILE.File", file);
    	logConfig.setProperty("log4j.appender.FILE.layout", "org.apache.log4j.PatternLayout");
    	logConfig.setProperty("log4j.appender.FILE.layout.ConversionPattern", "%d %-5p [%t] %c: %m%n");
    	PropertyConfigurator.configure(logConfig);
    }
    
	/**
	 * <p>enableDebugging</p>
	 */
	public static void enableDebugging() {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.ALL);
	}

    public static boolean isTraceEnabled(final Object logee) {
        return getLogger(logee).isTraceEnabled();
    }

    private static Logger getLogger(final Object logee) {
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
