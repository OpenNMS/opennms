package org.opennms.core.test;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.helpers.Util;

/**
 * <p>Simple implementation of {@link Logger} based on SimpleLogger from SLF4J</p>
 *
 * <p>This implementation is heavily inspired by
 * <a href="http://commons.apache.org/logging/">Apache Commons Logging</a>'s SimpleLog.</p>
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author <a href="mailto:sanders@apache.org">Scott Sanders</a>
 * @author Rod Waldhoff
 * @author Robert Burrell Donkin
 * @author C&eacute;drik LIME
 */
public class MockLogger extends MarkerIgnoringBase {
    private static final long serialVersionUID = 8598934420192647453L;

    private static final String CONFIGURATION_FILE = "mocklogger.properties";

    private static long START_TIME = System.currentTimeMillis();
    private static final Properties SIMPLE_LOGGER_PROPS = new Properties();

    private static boolean INITIALIZED = false;

    private static Level DEFAULT_LOG_LEVEL = Level.INFO;
    private static boolean SHOW_DATE_TIME = true;
    private static String DATE_TIME_FORMAT_STR = "yyyy-MM-dd HH:mm:ss,SSS";
    private static DateFormat DATE_FORMATTER = null;
    private static boolean SHOW_THREAD_NAME = true;
    private static boolean SHOW_LOG_NAME = true;
    private static boolean SHOW_SHORT_LOG_NAME = false;
    private static String LOG_FILE = "System.out";
    private static PrintStream TARGET_STREAM = null;
    private static boolean LEVEL_IN_BRACKETS = false;
    private static String WARN_LEVEL_STRING = "WARN";


    /** All system properties used by <code>MockLogger</code> start with this prefix */
    public static final String SYSTEM_PREFIX = "org.opennms.core.test.mockLogger.";

    public static final String DEFAULT_LOG_LEVEL_KEY = SYSTEM_PREFIX + "defaultLogLevel";
    public static final String SHOW_DATE_TIME_KEY = SYSTEM_PREFIX + "showDateTime";
    public static final String DATE_TIME_FORMAT_KEY = SYSTEM_PREFIX + "dateTimeFormat";
    public static final String SHOW_THREAD_NAME_KEY = SYSTEM_PREFIX + "showThreadName";
    public static final String SHOW_LOG_NAME_KEY = SYSTEM_PREFIX + "showLogName";
    public static final String SHOW_SHORT_LOG_NAME_KEY = SYSTEM_PREFIX + "showShortLogName";
    public static final String LOG_FILE_KEY = SYSTEM_PREFIX + "logFile";
    public static final String LEVEL_IN_BRACKETS_KEY = SYSTEM_PREFIX + "levelInBrackets";
    public static final String WARN_LEVEL_STRING_KEY = SYSTEM_PREFIX + "warnLevelString";


    public static final String LOG_KEY_PREFIX = SYSTEM_PREFIX + "log.";


    private static String getStringProperty(String name) {
        String prop = null;
        try {
            prop = System.getProperty(name);
        } catch (SecurityException e) {
            ; // Ignore
        }
        return (prop == null) ? SIMPLE_LOGGER_PROPS.getProperty(name) : prop;
    }

    private static String getStringProperty(String name, String defaultValue) {
        String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : prop;
    }

    private static boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = getStringProperty(name);
        return (prop == null) ? defaultValue : "true".equalsIgnoreCase(prop);
    }


    // Initialize class attributes.
    // Load properties file, if found.
    // Override with system properties.
    static void init() {
        System.err.println("Initializing MockLogger.");

        INITIALIZED = true;
        loadProperties();

        String defaultLogLevelString = getStringProperty(DEFAULT_LOG_LEVEL_KEY, null);
        if (defaultLogLevelString != null)
            DEFAULT_LOG_LEVEL = stringToLevel(defaultLogLevelString);

        SHOW_LOG_NAME = getBooleanProperty(SHOW_LOG_NAME_KEY, SHOW_LOG_NAME);
        SHOW_SHORT_LOG_NAME = getBooleanProperty(SHOW_SHORT_LOG_NAME_KEY, SHOW_SHORT_LOG_NAME);
        SHOW_DATE_TIME = getBooleanProperty(SHOW_DATE_TIME_KEY, SHOW_DATE_TIME);
        SHOW_THREAD_NAME = getBooleanProperty(SHOW_THREAD_NAME_KEY, SHOW_THREAD_NAME);
        DATE_TIME_FORMAT_STR = getStringProperty(DATE_TIME_FORMAT_KEY, DATE_TIME_FORMAT_STR);
        LEVEL_IN_BRACKETS = getBooleanProperty(LEVEL_IN_BRACKETS_KEY, LEVEL_IN_BRACKETS);
        WARN_LEVEL_STRING = getStringProperty(WARN_LEVEL_STRING_KEY, WARN_LEVEL_STRING);

        LOG_FILE = getStringProperty(LOG_FILE_KEY, LOG_FILE);
        TARGET_STREAM = computeTargetStream(LOG_FILE);

        if (DATE_TIME_FORMAT_STR != null) {
            try {
                DATE_FORMATTER = new SimpleDateFormat(DATE_TIME_FORMAT_STR);
            } catch (IllegalArgumentException e) {
                Util.report("Bad date format in " + CONFIGURATION_FILE + "; will output relative time", e);
            }
        }
    }


    private static PrintStream computeTargetStream(String logFile) {
        if ("System.err".equalsIgnoreCase(logFile))
            return System.err;
        else if ("System.out".equalsIgnoreCase(logFile)) {
            return System.out;
        } else {
            try {
                FileOutputStream fos = new FileOutputStream(logFile);
                PrintStream printStream = new PrintStream(fos);
                return printStream;
            } catch (FileNotFoundException e) {
                Util.report("Could not open [" + logFile + "]. Defaulting to System.err", e);
                return System.err;
            }
        }
    }

    private static void loadProperties() {
        // Add props from the resource mocklogger.properties
        InputStream in = AccessController.doPrivileged(
                                                       new PrivilegedAction<InputStream>() {
                                                           public InputStream run() {
                                                               ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
                                                               if (threadCL != null) {
                                                                   return threadCL.getResourceAsStream(CONFIGURATION_FILE);
                                                               } else {
                                                                   return ClassLoader.getSystemResourceAsStream(CONFIGURATION_FILE);
                                                               }
                                                           }
                                                       });
        if (null != in) {
            try {
                SIMPLE_LOGGER_PROPS.load(in);
                in.close();
            } catch (java.io.IOException e) {
                // ignored
            }
        }
    }

    /** The current log level */
    protected Level currentLogLevel = Level.INFO;
    /** The short name of this simple log instance */
    private transient String shortLogName = null;

    /**
     * Package access allows only {@link MockLoggerFactory} to instantiate
     * MockLogger instances.
     * @param appender 
     */
    MockLogger(String name) {
        if (!INITIALIZED) {
            init();
        }
        this.name = name;

        String levelString = recursivelyComputeLevelString();
        if (levelString != null) {
            this.currentLogLevel = stringToLevel(levelString);
        } else {
            this.currentLogLevel = DEFAULT_LOG_LEVEL;
        }
    }

    String recursivelyComputeLevelString() {
        String tempName = name;
        String levelString = null;
        int indexOfLastDot = tempName.length();
        while ((levelString == null) && (indexOfLastDot > -1)) {
            tempName = tempName.substring(0, indexOfLastDot);
            levelString = getStringProperty(LOG_KEY_PREFIX + tempName, null);
            //System.err.println("tempName = " + tempName + ", levelString = " + levelString);
            indexOfLastDot = String.valueOf(tempName).lastIndexOf(".");
        }
        return levelString;
    }

    private static Level stringToLevel(final String levelStr) {
        Level level = Level.valueOf(levelStr.toUpperCase());
        if (level == null) {
            System.err.println("ERROR: Failed to convert '" + levelStr + "' to a log level!");
            level = Level.INFO;
        }
        return level;
    }


    /**
     * This is our internal implementation for logging regular (non-parameterized)
     * log messages.
     *
     * @param level   One of the Level.XXX constants defining the log level
     * @param message The message itself
     * @param t       The exception whose stack trace should be logged
     */
    private void log(Level level, String message, Throwable t) {
        if (!isLevelEnabled(level)) {
            return;
        }

        MockLogAppender.addEvent(new LoggingEvent(getName(), level, message));

        StringBuffer buf = new StringBuffer(32);

        // Append date-time if so configured
        if (SHOW_DATE_TIME) {
            if (DATE_FORMATTER != null) {
                buf.append(getFormattedDate());
                buf.append(' ');
            } else {
                buf.append(System.currentTimeMillis() - START_TIME);
                buf.append(' ');
            }
        }

        if (LEVEL_IN_BRACKETS) buf.append('[');
        buf.append(level.toString());
        if (LEVEL_IN_BRACKETS) buf.append(']');
        buf.append(' ');

        // Append current thread name if so configured
        if (SHOW_THREAD_NAME) {
            buf.append('[');
            buf.append(Thread.currentThread().getName());
            buf.append("] ");
        }

        // Append the name of the log instance if so configured
        if (SHOW_SHORT_LOG_NAME) {
            if (shortLogName == null) shortLogName = computeShortName();
            buf.append(String.valueOf(shortLogName)).append(" - ");
        } else if (SHOW_LOG_NAME) {
            buf.append(String.valueOf(name)).append(" - ");
        }

        // Append the message
        buf.append(message);

        write(buf, t);

    }

    void write(StringBuffer buf, Throwable t) {
        TARGET_STREAM.println(buf.toString());
        if (t != null) {
            t.printStackTrace(TARGET_STREAM);
        }
        TARGET_STREAM.flush();
    }

    private String getFormattedDate() {
        Date now = new Date();
        String dateText;
        synchronized (DATE_FORMATTER) {
            dateText = DATE_FORMATTER.format(now);
        }
        return dateText;
    }

    private String computeShortName() {
        return name.substring(name.lastIndexOf(".") + 1);
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param level
     * @param format
     * @param arg1
     * @param arg2
     */
    private void formatAndLog(Level level, String format, Object arg1,
            Object arg2) {
        if (!isLevelEnabled(level)) {
            return;
        }
        FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    /**
     * For formatted messages, first substitute arguments and then log.
     *
     * @param level
     * @param format
     * @param arguments a list of 3 ore more arguments
     */
    private void formatAndLog(Level level, String format, Object... arguments) {
        if (!isLevelEnabled(level)) {
            return;
        }
        FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
        log(level, tp.getMessage(), tp.getThrowable());
    }

    /**
     * Is the given log level currently enabled?
     *
     * @param logLevel is this level enabled?
     */
    protected boolean isLevelEnabled(Level logLevel) {
        // log level are numerically ordered so can use simple numeric
        // comparison
        return (logLevel.getCode() >= currentLogLevel.getCode());
    }

    /** Are {@code trace} messages currently enabled? */
    public boolean isTraceEnabled() {
        return isLevelEnabled(Level.TRACE);
    }

    /**
     * A simple implementation which logs messages of level TRACE according
     * to the format outlined above.
     */
    public void trace(final String msg) {
        log(Level.TRACE, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level
     * TRACE according to the format outlined above.
     */
    public void trace(final String format, final Object param1) {
        formatAndLog(Level.TRACE, format, param1, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * TRACE according to the format outlined above.
     */
    public void trace(final String format, final Object param1, final Object param2) {
        formatAndLog(Level.TRACE, format, param1, param2);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * TRACE according to the format outlined above.
     */
    public void trace(String format, Object... argArray) {
        formatAndLog(Level.TRACE, format, argArray);
    }

    /** Log a message of level TRACE, including an exception. */
    public void trace(String msg, Throwable t) {
        log(Level.TRACE, msg, t);
    }

    /** Are {@code debug} messages currently enabled? */
    public boolean isDebugEnabled() {
        return isLevelEnabled(Level.DEBUG);
    }

    /**
     * A simple implementation which logs messages of level DEBUG according
     * to the format outlined above.
     */
    public void debug(String msg) {
        log(Level.DEBUG, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level
     * DEBUG according to the format outlined above.
     */
    public void debug(String format, Object param1) {
        formatAndLog(Level.DEBUG, format, param1, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * DEBUG according to the format outlined above.
     */
    public void debug(String format, Object param1, Object param2) {
        formatAndLog(Level.DEBUG, format, param1, param2);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * DEBUG according to the format outlined above.
     */
    public void debug(String format, Object... argArray) {
        formatAndLog(Level.DEBUG, format, argArray);
    }

    /** Log a message of level DEBUG, including an exception. */
    public void debug(String msg, Throwable t) {
        log(Level.DEBUG, msg, t);
    }

    /** Are {@code info} messages currently enabled? */
    public boolean isInfoEnabled() {
        return isLevelEnabled(Level.INFO);
    }

    /**
     * A simple implementation which logs messages of level INFO according
     * to the format outlined above.
     */
    public void info(String msg) {
        log(Level.INFO, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level
     * INFO according to the format outlined above.
     */
    public void info(String format, Object arg) {
        formatAndLog(Level.INFO, format, arg, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * INFO according to the format outlined above.
     */
    public void info(String format, Object arg1, Object arg2) {
        formatAndLog(Level.INFO, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * INFO according to the format outlined above.
     */
    public void info(String format, Object... argArray) {
        formatAndLog(Level.INFO, format, argArray);
    }

    /** Log a message of level INFO, including an exception. */
    public void info(String msg, Throwable t) {
        log(Level.INFO, msg, t);
    }

    /** Are {@code warn} messages currently enabled? */
    public boolean isWarnEnabled() {
        return isLevelEnabled(Level.WARN);
    }

    /**
     * A simple implementation which always logs messages of level WARN according
     * to the format outlined above.
     */
    public void warn(String msg) {
        log(Level.WARN, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level
     * WARN according to the format outlined above.
     */
    public void warn(String format, Object arg) {
        formatAndLog(Level.WARN, format, arg, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * WARN according to the format outlined above.
     */
    public void warn(String format, Object arg1, Object arg2) {
        formatAndLog(Level.WARN, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * WARN according to the format outlined above.
     */
    public void warn(String format, Object... argArray) {
        formatAndLog(Level.WARN, format, argArray);
    }

    /** Log a message of level WARN, including an exception. */
    public void warn(String msg, Throwable t) {
        log(Level.WARN, msg, t);
    }

    /** Are {@code error} messages currently enabled? */
    public boolean isErrorEnabled() {
        return isLevelEnabled(Level.ERROR);
    }

    /**
     * A simple implementation which always logs messages of level ERROR according
     * to the format outlined above.
     */
    public void error(String msg) {
        log(Level.ERROR, msg, null);
    }

    /**
     * Perform single parameter substitution before logging the message of level
     * ERROR according to the format outlined above.
     */
    public void error(String format, Object arg) {
        formatAndLog(Level.ERROR, format, arg, null);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * ERROR according to the format outlined above.
     */
    public void error(String format, Object arg1, Object arg2) {
        formatAndLog(Level.ERROR, format, arg1, arg2);
    }

    /**
     * Perform double parameter substitution before logging the message of level
     * ERROR according to the format outlined above.
     */
    public void error(String format, Object... argArray) {
        formatAndLog(Level.ERROR, format, argArray);
    }

    /** Log a message of level ERROR, including an exception. */
    public void error(String msg, Throwable t) {
        log(Level.ERROR, msg, t);
    }
}