package org.opennms.client;

import java.io.Serializable;

import com.extjs.gxt.ui.client.data.BeanModelTag;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *  This class is a thin facade for {@link org.apache.log4j.spi.LoggingEvent}
 *  log4j messages on the server side.
 *
 * @author ranger
 * @version $Id: $
 */
public class LoggingEvent implements BeanModelTag, Serializable, IsSerializable {
    private static final long serialVersionUID = -701344411706799165L;

    /**
     * Enum that facades the {@link org.apache.log4j.Level} of the original log4j log message.
     */
    public enum LogLevel { TRACE, DEBUG, INFO, WARN, ERROR, FATAL }

    private String m_category;
    private long m_timestamp;
    private LogLevel m_level;
    private String m_message;

    /**
     * Zero-argument constructor is necessary for GWT serialization.
     *
     * @deprecated Only for use by GWT serialization.
     */
    public LoggingEvent() {}

    /**
     * <p>Constructor for LoggingEvent.</p>
     *
     * @param category a {@link java.lang.String} object.
     * @param timestamp a long.
     * @param level a {@link org.opennms.client.LoggingEvent.LogLevel} object.
     * @param message a {@link java.lang.String} object.
     */
    public LoggingEvent(String category, long timestamp, LogLevel level, String message) {
        m_category = category;
        m_timestamp = timestamp;
        m_level = level;
        m_message = message;
    }

    /**
     * <p>getCategory</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCategory() { return m_category; }
    /**
     * <p>getTimeStamp</p>
     *
     * @return a long.
     */
    public long getTimeStamp() { return m_timestamp; }
    /**
     * <p>getLevel</p>
     *
     * @return a {@link org.opennms.client.LoggingEvent.LogLevel} object.
     */
    public LogLevel getLevel() { return m_level; }
    /**
     * <p>getMessage</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getMessage() { return m_message; }
}
