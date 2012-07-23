/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.AssertionFailedError;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggingEvent;

/**
 * <p>MockLogAppender class. If you do not specify the log level specifically, the level
 * will default to DEBUG and you can control the level by setting the <code>mock.logLevel</code>
 * system property.</p>
 * 
 * Used in unit tests to check that the level of logging produced by a test was suitable.
 * In some cases, the test will be that no messages were logged at a higher priority than
 * specified, e.g. Error messages logged when only Notice were expected.
 * 
 * Some other tests may wish to ensure that an Error or Warn message was indeed logged as expected
 * 
 * Remember: "Greater" in regards to level relates to priority; the higher the level, the less should
 * usually be logged (e.g. Errors (highest) should be only the highest priority messages about really bad things, 
 * as compared to Debug (lowest) which is any old rubbish that might be interesting to a developer)
 *
 * @author brozow
 * @version $Id: $
 */
public class MockLogAppender extends AppenderSkeleton {
    private static List<LoggingEvent> s_events = null;

    private static Level s_logLevel = Level.ALL;

    /**
     * <p>Constructor for MockLogAppender.</p>
     */
    public MockLogAppender() {
        super();
        resetEvents();
        resetLogLevel();
    }

    /** {@inheritDoc} */
    public synchronized void doAppend(final LoggingEvent event) {
        super.doAppend(event);
        receivedLogLevel(event.getLevel());
    }

    /** {@inheritDoc} */
    protected void append(final LoggingEvent event) {
        s_events.add(event);
    }

    /**
     * <p>close</p>
     */
    public void close() {
    }

    /**
     * <p>requiresLayout</p>
     *
     * @return a boolean.
     */
    public boolean requiresLayout() {
        return false;
    }

    /**
     * <p>resetEvents</p>
     */
    public static void resetEvents() {
        s_events = Collections.synchronizedList(new LinkedList<LoggingEvent>());
    }

    /**
     * <p>getEvents</p>
     *
     * @return an array of {@link org.apache.log4j.spi.LoggingEvent} objects.
     */
    public static LoggingEvent[] getEvents() {
        return (LoggingEvent[]) s_events.toArray(new LoggingEvent[0]);
    }

    /**
     * <p>getEventsGreaterOrEqual</p>
     *
     * @param level a {@link org.apache.log4j.Level} object.
     * @return an array of {@link org.apache.log4j.spi.LoggingEvent} objects.
     */
    public static LoggingEvent[] getEventsGreaterOrEqual(final Level level) {
        LinkedList<LoggingEvent> matching = new LinkedList<LoggingEvent>();

        synchronized (s_events) {
            for (final LoggingEvent event : s_events) {
                if (event.getLevel().isGreaterOrEqual(level)) {
                    matching.add(event);
                }
            }
        }

        return matching.toArray(new LoggingEvent[0]);
    }

    /**
     * <p>getEventsAtLevel</p>
     *
     * Returns events that were logged at the specified level
     * 
     * @param level a {@link org.apache.log4j.Level} object.
     * @return an array of {@link org.apache.log4j.spi.LoggingEvent} objects.
     */
    public static LoggingEvent[] getEventsAtLevel(final Level level) {
        LinkedList<LoggingEvent> matching = new LinkedList<LoggingEvent>();

        synchronized (s_events) {
            for (final LoggingEvent event : s_events) {
                if (event.getLevel().isGreaterOrEqual(level)) {
                    matching.add(event);
                }
            }
        }

        return matching.toArray(new LoggingEvent[0]);
    }

    /**
     * <p>setupLogging</p>
     */
    public static void setupLogging() {
        setupLogging(new Properties());
    }

    /**
     * <p>setupLogging</p>
     *
     * @param config a {@link java.util.Properties} object.
     */
    public static void setupLogging(final Properties config) {
        setupLogging(true, config);
    }

    /**
     * <p>setupLogging</p>
     *
     * @param toConsole a boolean.
     */
    public static void setupLogging(final boolean toConsole) {
        setupLogging(toConsole, new Properties());
    }

    /**
     * <p>setupLogging</p>
     *
     * @param toConsole a boolean.
     * @param props a {@link java.util.Properties} object.
     */
    public static void setupLogging(final boolean toConsole, final Properties props) {
    	final String level = System.getProperty("mock.logLevel", "DEBUG");
        setupLogging(toConsole, level, props);
    }
    
    /**
     * <p>setupLogging</p>
     *
     * @param toConsole a boolean.
     * @param level a {@link java.lang.String} object.
     */
    public static void setupLogging(final boolean toConsole, final String level) {
        setupLogging(toConsole, level, new Properties());
    }
    
    /**
     * <p>setupLogging</p>
     *
     * @param toConsole a boolean.
     * @param level a {@link java.lang.String} object.
     * @param config a {@link java.util.Properties} object.
     */
    public static void setupLogging(final boolean toConsole, final String level, final Properties config) {
        resetLogLevel();
        
        final Properties logConfig = new Properties(config);
        final String consoleAppender = (toConsole ? ", CONSOLE" : "");
        
        setProperty(logConfig, "log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
        setProperty(logConfig, "log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        setProperty(logConfig, "log4j.appender.CONSOLE.layout.ConversionPattern", "%d %-5p [%t] %c: %m%n");
        setProperty(logConfig, "log4j.appender.MOCK", MockLogAppender.class.getName());
        setProperty(logConfig, "log4j.appender.MOCK.layout", "org.apache.log4j.PatternLayout");
        setProperty(logConfig, "log4j.appender.MOCK.layout.ConversionPattern", "%-5p [%t] %c: %m%n");

        setProperty(logConfig, "log4j.rootCategory", level + consoleAppender + ", MOCK");
        setProperty(logConfig, "log4j.logger.org.apache.commons.httpclient.HttpMethodBase", "ERROR");
        setProperty(logConfig, "log4j.logger.org.exolab.castor", "INFO");
        setProperty(logConfig, "log4j.logger.org.snmp4j", "ERROR");
        setProperty(logConfig, "log4j.logger.org.snmp4j.agent", "ERROR");
        setProperty(logConfig, "log4j.logger.com.mchange.v2.c3p0.impl", "WARN");
        setProperty(logConfig, "log4j.logger.org.hibernate.cfg.AnnotationBinder", "ERROR" + consoleAppender + ", MOCK");
        
        PropertyConfigurator.configure(logConfig);
    }
    
    private static void setProperty(final Properties logConfig, final String key, final String value) {
        if (!logConfig.containsKey(key)) {
            logConfig.put(key, value);
        }
    }

    /**
     * <p>isLoggingSetup</p>
     *
     * @return a boolean.
     */
    public static boolean isLoggingSetup() {
        return s_events != null;
    }

    /**
     * <p>receivedLogLevel</p>
     *
     * @param level a {@link org.apache.log4j.Level} object.
     */
    public static void receivedLogLevel(final Level level) {
        if (level.isGreaterOrEqual(s_logLevel)) {
            s_logLevel = level;
        }
    }

    /**
     * <p>resetLogLevel</p>
     */
    public static void resetLogLevel() {
        s_logLevel = Level.ALL;
    }

    /**
     * <p>noWarningsOrHigherLogged</p>
     *
     * @return a boolean.
     */
    public static boolean noWarningsOrHigherLogged() {
        return Level.INFO.isGreaterOrEqual(s_logLevel);
    }

    /**
     * <p>assertNotGreaterOrEqual</p>
     *
     * @param level a {@link org.apache.log4j.Level} object.
     * @throws junit.framework.AssertionFailedError if any.
     */
    public static void assertNotGreaterOrEqual(final Level level) throws AssertionFailedError {
        if (!isLoggingSetup()) {
            throw new AssertionFailedError("MockLogAppender has not been initialized");
        }

        try {
            Thread.sleep(500);
        } catch (final InterruptedException e) {
        	Thread.currentThread().interrupt();
        }
        final LoggingEvent[] events = getEventsGreaterOrEqual(level);
        if (events.length == 0) {
            return;
        }

        StringBuffer message = new StringBuffer("Log messages at or greater than the log level ").append(level).append(" received:");

        for (final LoggingEvent event : events) {
            message.append("\n\t[").append(event.getLevel()).append("] ")
            	.append(event.getLoggerName()).append(": ")
            	.append(event.getMessage());
        }

        throw new AssertionFailedError(message.toString());
    }

    /**
     * <p>assertNoWarningsOrGreater</p>
     *
     * @throws junit.framework.AssertionFailedError if any.
     */
    public static void assertNoWarningsOrGreater() throws AssertionFailedError {
        assertNotGreaterOrEqual(Level.WARN);
    }
    
    /**
     * <p>assertLogAtLevel</p>
     * Asserts that a message was logged at the requested level.
     * 
     * Useful for testing code that *should* have logged an error message 
     * (or a notice or some other special case)
     *
     * @param level a {@link org.apache.log4j.Level} object.
     * @throws junit.framework.AssertionFailedError if any.
     */
    public static void assertLogAtLevel(final Level level) throws AssertionFailedError {
        if (!isLoggingSetup()) {
            throw new AssertionFailedError("MockLogAppender has not been initialized");
        }

        try {
            Thread.sleep(500);
        } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
        }
        final LoggingEvent[] events = getEventsAtLevel(level);
        if (events.length == 0) {
            throw new AssertionFailedError("No messages were received at level " + level);
        }

    }

}
