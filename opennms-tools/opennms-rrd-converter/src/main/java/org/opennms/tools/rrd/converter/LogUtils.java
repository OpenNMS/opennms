package org.opennms.tools.rrd.converter;

import java.util.Date;


public class LogUtils {
    public static enum Level {
        ERROR, WARN, INFO, DEBUG, TRACE
    }
    
    private static Level m_level = Level.INFO;
    
    public static void setLevel(final Level level) {
        m_level = level;
    }

    public static void tracef(final Object clazz, final String format, final Object... args) {
        logf(Level.TRACE, clazz, null, format, args);
    }

    public static void tracef(final Object clazz, final Throwable t, final String format, final Object... args) {
        logf(Level.TRACE, clazz, t, format, args);
    }

    public static void debugf(final Object clazz, final String format, final Object... args) {
        logf(Level.DEBUG, clazz, null, format, args);
    }

    public static void debugf(final Object clazz, final Throwable t, final String format, final Object... args) {
        logf(Level.DEBUG, clazz, t, format, args);
    }

    public static void infof(final Object clazz, final String format, final Object... args) {
        logf(Level.INFO, clazz, null, format, args);
    }

    public static void infof(final Object clazz, final Throwable t, final String format, final Object... args) {
        logf(Level.INFO, clazz, t, format, args);
    }

    public static void warnf(final Object clazz, final String format, final Object... args) {
        logf(Level.WARN, clazz, null, format, args);
    }

    public static void warnf(final Object clazz, final Throwable t, final String format, final Object... args) {
        logf(Level.WARN, clazz, t, format, args);
    }

    public static void errorf(final Object clazz, final String format, final Object... args) {
        logf(Level.ERROR, clazz, null, format, args);
    }

    public static void errorf(final Object clazz, final Throwable t, final String format, final Object... args) {
        logf(Level.ERROR, clazz, t, format, args);
    }

    private static void logf(final Level level, final Object clazz, final Throwable t, final String format, final Object... args) {
        if (m_level.compareTo(level) >= 0) {
            final String className = clazz instanceof Class? ((Class<?>)clazz).getSimpleName() : clazz.getClass().getSimpleName();
            System.err.println(String.format(new Date() + ": " + level + ": " + className + ": " + format, args));
            if (t != null) t.printStackTrace();
        }
    }

    public static boolean isTraceEnabled(final Object clazz) {
        return false;
    }

}
