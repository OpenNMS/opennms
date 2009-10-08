package org.opennms.server;

import java.util.*;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/** 
 * Log4j {@link org.apache.log4j.Appender Appender} that will capture log 
 * events in a list so that they can be relayed to the UI.
 */
public class ListAppender extends AppenderSkeleton implements Appender {
    private List<LoggingEvent> m_list = new ArrayList<LoggingEvent>();

    private int m_counter = 0;

    public void resetCounter() {
        // Integer operation, sync not necessary
        m_counter = 0;
    }

    public List<LoggingEvent> getEvents(int offset, int count) {
        List<LoggingEvent> retval = null;

        synchronized(m_list) {
            int toIndex = (count > 0) ? (offset + count) : m_list.size();
            // Wrap the collection in an unmodifiable facade
            // retval = Collections.unmodifiableList(m_list.subList((offset > 0) ? offset : 0, toIndex));
            retval = Collections.unmodifiableList(m_list.subList(0, m_list.size()));
        }

        return retval;
    }

    protected void append(LoggingEvent event) {
        synchronized(m_list) {
            m_list.add(event);
        }
    }

    public void clear() {
        synchronized(m_list){
            m_list.clear();
        }
    }

    public void close() {
        this.clear();
    }

    public boolean requiresLayout() {
        return false;
    }
}
