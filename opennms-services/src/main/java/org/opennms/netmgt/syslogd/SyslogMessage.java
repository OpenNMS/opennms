package org.opennms.netmgt.syslogd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.core.utils.LogUtils;

public class SyslogMessage {
    // Priorities.
    public static final int LOG_EMERG = 0; // system is unusable
    public static final int LOG_ALERT = 1; // action must be taken immediately
    public static final int LOG_CRIT = 2; // critical conditions
    public static final int LOG_ERR = 3; // error conditions
    public static final int LOG_WARNING = 4; // warning conditions
    public static final int LOG_NOTICE = 5; // normal but significant condition
    public static final int LOG_INFO = 6; // informational
    public static final int LOG_DEBUG = 7; // debug-level messages
    public static final int LOG_PRIMASK = 0x0007; // mask to extract priority

    // Facilities.
    public static final int LOG_KERN = (0 << 3); // kernel messages
    public static final int LOG_USER = (1 << 3); // random user-level messages
    public static final int LOG_MAIL = (2 << 3); // mail system
    public static final int LOG_DAEMON = (3 << 3); // system daemons
    public static final int LOG_AUTH = (4 << 3); // security/authorization
    public static final int LOG_SYSLOG = (5 << 3); // internal syslogd use
    public static final int LOG_LPR = (6 << 3); // line printer subsystem
    public static final int LOG_NEWS = (7 << 3); // network news subsystem
    public static final int LOG_UUCP = (8 << 3); // UUCP subsystem
    public static final int LOG_CRON = (15 << 3); // clock daemon
    // Other codes through 15 reserved for system use.
    public static final int LOG_LOCAL0 = (16 << 3); // reserved for local use
    public static final int LOG_LOCAL1 = (17 << 3); // reserved for local use
    public static final int LOG_LOCAL2 = (18 << 3); // reserved for local use
    public static final int LOG_LOCAL3 = (19 << 3); // reserved for local use
    public static final int LOG_LOCAL4 = (20 << 3); // reserved for local use
    public static final int LOG_LOCAL5 = (21 << 3); // reserved for local use
    public static final int LOG_LOCAL6 = (22 << 3); // reserved for local use
    public static final int LOG_LOCAL7 = (23 << 3); // reserved for local use
    public static final int LOG_FACMASK = 0x03F8; // mask to extract facility

    // Option flags.
    public static final int LOG_PID = 0x01; // log the pid with each message
    public static final int LOG_CONS = 0x02; // log on the console if errors
    public static final int LOG_NDELAY = 0x08; // don't delay open
    public static final int LOG_NOWAIT = 0x10; // don't wait for console forks

    private static final DateFormat m_dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss");
    private static final DateFormat m_rfc3339Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    static {
        m_dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        m_rfc3339Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    private int m_facility;
    private int m_severity;
    private Integer m_version;
    private Date m_date;
    private String m_hostname;
    private String m_processName;
    private Integer m_processId;
    private String m_messageId;
    private String m_message;
    private String m_fullText;
    
    public SyslogMessage() {
    }

    public SyslogMessage(final int facility, final int severity, final Date date, String hostname, final String processName, final Integer processId, final String message) {
        this();

        m_facility = facility;
        m_severity = severity;
        m_date = date;
        m_processName = processName;
        m_processId = processId;
        m_message = message;
    }

    public int getFacility() {
        return m_facility;
    }

    public void setFacility(final int facility) {
        m_fullText = null;
        m_facility = facility;
    }

    public int getSeverity() {
        return m_severity;
    }

    public void setSeverity(final int severity) {
        m_fullText = null;
        m_severity = severity;
    }

    public Integer getVersion() {
        return m_version;
    }

    public void setVersion(final Integer version) {
        m_fullText = null;
        m_version = version;
    }

    public Date getDate() {
        return m_date;
    }
    
    public void setDate(final Date date) {
        m_fullText = null;
        m_date = date;
    }

    public String getHostName() {
        return m_hostname;
    }
    
    public void setHostName(final String hostname) {
        m_fullText = null;
        m_hostname = hostname;
    }

    public String getHostAddress() {
        if (m_hostname != null) {
            try {
                final InetAddress address = InetAddress.getByName(m_hostname);
                return address.getHostAddress().replace("/", "");
            } catch (final UnknownHostException e) {
                LogUtils.warnf(ConvertToEvent.class, e, "Could not parse the hostname: %s", m_hostname);
            }
        }
        return null;
    }

    public String getProcessName() {
        return m_processName;
    }
    
    public void setProcessName(final String processName) {
        m_fullText = null;
        m_processName = processName;
    }

    public Integer getProcessId() {
        return m_processId;
    }
    
    public void setProcessId(final Integer processId) {
        m_fullText = null;
        m_processId = processId;
    }

    public String getMessageID() {
        return m_messageId;
    }

    public void setMessageID(final String messageId) {
        m_fullText = null;
        m_messageId = messageId;
    }

    public String getMessage() {
        return m_message;
    }

    public void setMessage(final String message) {
        m_fullText = null;
        m_message = message;
    }

    public int getPriorityField() {
        return ((m_facility & LOG_FACMASK) | m_severity);
    }

    public String getSyslogFormattedDate() {
        if (m_date == null) return null;
        return m_dateFormat.format(m_date);
    }

    public String getRfc3339FormattedDate() {
        if (m_date == null) return null;
        return m_rfc3339Format.format(m_date);
    }

    public String getFullText() {
        if (m_fullText == null) {
            if (m_processId != null && m_processName != null) {
                m_fullText = String.format("<%d>%s %s %s[%d]: %s", getPriorityField(), getSyslogFormattedDate(), getHostName(), getProcessName(), getProcessId(), getMessage());
            } else if (m_processName != null) {
                m_fullText = String.format("<%d>%s %s %s: %s", getPriorityField(), getSyslogFormattedDate(), getHostName(), getProcessName(), getMessage());
            } else {
                m_fullText = String.format("<%d>%s %s %s", getPriorityField(), getSyslogFormattedDate(), getHostName(), getMessage());
            }
        }
        return m_fullText;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("facility", m_facility)
            .append("severity", m_severity)
            .append("version", m_version)
            .append("date", m_date)
            .append("hostname", m_hostname)
            .append("message ID", m_messageId)
            .append("process name", m_processName)
            .append("process ID", m_processId)
            .append("message", m_message)
            .toString();
    }
}
