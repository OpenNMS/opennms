/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.newts.converter.rrd.converter;

import java.util.Date;

/**
 * This LogUtils class is a simple log4j emulator that prints output to {@link System#err}.
 */
public abstract class LogUtils {
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
            final String className = clazz instanceof Class<?> ? ((Class<?>)clazz).getSimpleName() : clazz.getClass().getSimpleName();
            System.err.println(String.format(new Date() + ": " + level + ": " + className + ": " + format, args));
            if (t != null) t.printStackTrace();
        }
    }

    public static boolean isTraceEnabled(final Object clazz) {
        return false;
    }

}
