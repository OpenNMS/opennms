/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import junit.framework.AssertionFailedError;

import org.slf4j.LoggerFactory;

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
public class MockLogAppender {
    private static final LoggingEvent[] EMPTY_LOGGING_EVENT = new LoggingEvent[0];

    private static List<LoggingEvent> s_events;
    private static Level s_highestLoggedLevel = Level.TRACE;
    private static String s_defaultLevel = "DEBUG";
    private static MockLogAppender s_instance = null;

    /**
     * <p>Constructor for MockLogAppender.</p>
     */
    protected MockLogAppender() {
        MockLoggerFactory.setAppender(this);
        resetEvents();
        resetLogLevel();
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
     */
    public static LoggingEvent[] getEvents() {
        return (LoggingEvent[]) s_events.toArray(EMPTY_LOGGING_EVENT);
    }

    /**
     * <p>getEventsGreaterOrEqual</p>
     *
     */
    public static LoggingEvent[] getEventsGreaterOrEqual(final Level level) {
        LinkedList<LoggingEvent> matching = new LinkedList<LoggingEvent>();

        synchronized (s_events) {
            for (final LoggingEvent event : s_events) {
                if (event.getLevel().ge(level)) {
                    matching.add(event);
                }
            }
        }

        return matching.toArray(EMPTY_LOGGING_EVENT);
    }

    /**
     * <p>getEventsAtLevel</p>
     *
     * Returns events that were logged at the specified level
     * 
     */
    public static LoggingEvent[] getEventsAtLevel(final Level level) {
        final LinkedList<LoggingEvent> matching = new LinkedList<LoggingEvent>();

        synchronized (s_events) {
            for (final LoggingEvent event : s_events) {
                if (event.getLevel().ge(level)) {
                    matching.add(event);
                }
            }
        }

        return matching.toArray(EMPTY_LOGGING_EVENT);
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
        final String level = System.getProperty(MockLogger.DEFAULT_LOG_LEVEL_KEY, s_defaultLevel);
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
    public static void setupLogging(final boolean toConsole, String level, final Properties config) {
        s_defaultLevel = level;
        resetLogLevel();
        resetEvents();

        setProperty(MockLogger.DEFAULT_LOG_LEVEL_KEY, level);
        setProperty(MockLogger.LOG_KEY_PREFIX + "com.jcraft.jsch", "WARN");
        setProperty(MockLogger.LOG_KEY_PREFIX + "com.mchange", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "com.mchange.v2", "WARN");
        setProperty(MockLogger.LOG_KEY_PREFIX + "httpclient", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.apache.aries.blueprint.container", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.apache.bsf", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.apache.http", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.apache.commons.httpclient.HttpMethodBase", "ERROR");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.apache.http", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.exolab.castor", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.gwtwidgets", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.hibernate", "INFO");
        // One of these is probably unused...
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.hibernate.sql", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.hibernate.SQL", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.hibernate.cfg.AnnotationBinder", "ERROR");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.hibernate.cfg.annotations.EntityBinder", "ERROR");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.quartz", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.snmp4j", "ERROR");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.snmp4j.agent", "ERROR");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.snmp4j.transport", "WARN");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.springframework", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.springframework.beans.factory.support", "WARN");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.springframework.context.support", "WARN");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.springframework.jdbc.datasource", "WARN");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.springframework.test.context.support", "WARN");
        setProperty(MockLogger.LOG_KEY_PREFIX + "org.springframework.security", "INFO");
        setProperty(MockLogger.LOG_KEY_PREFIX + "com.mchange.v2", "WARN");
        setProperty(MockLogger.LOG_KEY_PREFIX + "snaq.db", "INFO");

        for (final Object oKey : config.keySet()) {
            final String key = ((String)oKey).replaceAll("^log4j.logger.", MockLogger.LOG_KEY_PREFIX);
            setProperty(key, config.getProperty((String)oKey));
        }
    }

    private static void setProperty(final String key, final String value) {
        System.setProperty(key, System.getProperty(key, value));
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
     * @param level a {@link Level} object.
     */
    public static void receivedLogLevel(final Level level) {
        if (level.gt(s_highestLoggedLevel)) {
            //System.err.println("MockLogAppender: current level: " + s_highestLoggedLevel + ", new level: " + level);
            s_highestLoggedLevel = level;
        }
    }

    /**
     * <p>resetLogLevel</p>
     */
    public static void resetLogLevel() {
        s_highestLoggedLevel = Level.TRACE;
        System.setProperty(MockLogger.DEFAULT_LOG_LEVEL_KEY, s_defaultLevel);
    }

    /**
     * <p>noWarningsOrHigherLogged</p>
     *
     * @return a boolean.
     */
    public static boolean noWarningsOrHigherLogged() {
        return Level.INFO.ge(s_highestLoggedLevel);
    }

    /**
     * <p>assertNotGreaterOrEqual</p>
     *
     * @throws junit.framework.AssertionFailedError if any.
     */
    public static void assertNotGreaterOrEqual(final Level level) throws AssertionFailedError {
        if (!isLoggingSetup()) {
            throw new AssertionFailedError("MockLogAppender has not been initialized");
        }

/*
        try {
            Thread.sleep(500);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
*/

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

    public static void assertNoErrorOrGreater() throws AssertionFailedError {
        assertNotGreaterOrEqual(Level.ERROR);
    }

    public static void assertNoFatalOrGreater() throws AssertionFailedError {
        assertNotGreaterOrEqual(Level.FATAL);
    }

    /**
     * <p>assertLogAtLevel</p>
     * Asserts that a message was logged at the requested level.
     * 
     * Useful for testing code that *should* have logged an error message 
     * (or a notice or some other special case)
     *
     * @throws junit.framework.AssertionFailedError if any.
     */
    public static void assertLogAtLevel(final Level level) throws AssertionFailedError {
        if (!isLoggingSetup()) {
            throw new AssertionFailedError("MockLogAppender has not been initialized");
        }

/*
        try {
            Thread.sleep(500);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
*/

        final LoggingEvent[] events = getEventsAtLevel(level);
        if (events.length == 0) {
            throw new AssertionFailedError("No messages were received at level " + level);
        }

    }

    public static void addEvent(final LoggingEvent loggingEvent) {
        s_events.add(loggingEvent);
        receivedLogLevel(loggingEvent.getLevel());
    }

    public static MockLogAppender getInstance() {
        setupLogging();

        if (s_instance == null) {
            s_instance = new MockLogAppender();
        }

        return s_instance;
    }

    public static void assertNoLogging() throws AssertionFailedError {
        if (s_events.size() > 0) {
            throw new AssertionFailedError("Unhandled logging occurred.");
        }
    }

    public static void assertLogMatched(final Level level, final String message) {
        for (final LoggingEvent event : s_events) {
            if (event.getLevel().eq(level) && event.getMessage().contains(message)) {
                return;
            }
        }
        throw new AssertionFailedError("No log message matched for log level " + level + ", message '" + message + "'");
    }
}
