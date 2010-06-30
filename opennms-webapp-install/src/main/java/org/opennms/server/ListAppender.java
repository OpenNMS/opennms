package org.opennms.server;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Log4j {@link org.apache.log4j.Appender Appender} that will capture log
 * events in a list so that they can be relayed to the UI.
 *
 * @author ranger
 * @version $Id: $
 */
public class ListAppender extends AppenderSkeleton implements Appender {
    private List<LoggingEvent> m_list = new ArrayList<LoggingEvent>();

    // private int m_counter = 0;

    /**
     * <p>resetCounter</p>
     */
    public void resetCounter() {
        // Integer operation, sync not necessary
        // m_counter = 0;
    }

    /**
     * <p>getEvents</p>
     *
     * @param offset a int.
     * @param count a int.
     * @return a {@link java.util.List} object.
     */
    public List<LoggingEvent> getEvents(int offset, int count) {
        List<LoggingEvent> retval = null;

        synchronized(m_list) {
            // int toIndex = (count > 0) ? (offset + count) : m_list.size();

            // Wrap the collection in an unmodifiable facade
            // retval = Collections.unmodifiableList(m_list.subList((offset > 0) ? offset : 0, toIndex));
            retval = Collections.unmodifiableList(m_list.subList(0, m_list.size()));
        }

        return retval;
    }

    /**
     * <p>getEventsAsStrings</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getEventsAsStrings() {
        List<String> retval = new ArrayList<String>();

        synchronized(m_list) {
            for (LoggingEvent event : m_list) {
                StringBuffer buffer = new StringBuffer();
                // Emulates the log4j date format: 2009-11-02 10:57:55,875
                buffer.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date(event.getTimeStamp())));
                if (event.getLevel().equals(Level.TRACE)) {
                    buffer.append(" TRACE ");
                } else if (event.getLevel().equals(Level.DEBUG)) {
                    buffer.append(" DEBUG  ");
                } else if (event.getLevel().equals(Level.INFO)) {
                    buffer.append(" INFO  ");
                } else if (event.getLevel().equals(Level.WARN)) {
                    buffer.append(" WARN  ");
                } else if (event.getLevel().equals(Level.ERROR)) {
                    buffer.append(" ERROR ");
                } else if (event.getLevel().equals(Level.FATAL)) {
                    buffer.append(" FATAL ");
                } else {
                    // Ignore events with any other priority
                    continue;
                }
                buffer.append(event.getRenderedMessage());
                retval.add(buffer.toString());
            }
        }

        return retval;
    }

    /** {@inheritDoc} */
    protected void append(LoggingEvent event) {
        synchronized(m_list) {
            m_list.add(event);
        }
    }

    /**
     * <p>clear</p>
     */
    public void clear() {
        synchronized(m_list){
            m_list.clear();
        }
    }

    /**
     * <p>close</p>
     */
    public void close() {
        this.clear();
    }

    /**
     * <p>requiresLayout</p>
     *
     * @return a boolean.
     */
    public boolean requiresLayout() {
        return false;
    }
}
