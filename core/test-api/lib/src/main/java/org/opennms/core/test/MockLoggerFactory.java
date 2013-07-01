package org.opennms.core.test;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.ILoggerFactory;
import org.slf4j.Logger;

public class MockLoggerFactory implements ILoggerFactory {
    final static MockLoggerFactory INSTANCE = new MockLoggerFactory(MockLogAppender.getInstance());
    static MockLogAppender s_appender = null;

    final Map<String,Logger> m_loggerMap;

    public MockLoggerFactory(final MockLogAppender mockLogAppender) {
        m_loggerMap = new HashMap<String,Logger>();
        s_appender = mockLogAppender;
    }

    public static void setAppender(final MockLogAppender appender) {
        s_appender = appender;
    }

    /**
     * Return an appropriate {@link MockLogger} instance by name.
     */
    public Logger getLogger(final String name) {
        if (s_appender == null) {
            System.err.println("WARNING: getLogger(" + name + ") called, but MockLogAppender hasn't been set up yet!");
        }
        Logger slogger = null;
        // protect against concurrent access of the loggerMap
        synchronized (this) {
            slogger = (Logger) m_loggerMap.get(name);
            if (slogger == null) {
                slogger = new MockLogger(name, s_appender);
                m_loggerMap.put(name, slogger);
            }
        }
        return slogger;
    }
}