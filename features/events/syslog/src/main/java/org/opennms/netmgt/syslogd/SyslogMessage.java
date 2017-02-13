/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogMessage {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogMessage.class);
    private static final ThreadLocal<DateFormat> m_dateFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            final DateFormat dateFormat = new SimpleDateFormat("MMM dd HH:mm:ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat;
        }
    };

    private static final ThreadLocal<DateFormat> m_rfc3339Format = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            return dateFormat;
        }
    };

    /**
     * This field is for informational purposes only.
     */
    private Class<? extends SyslogParser> m_parserClass;

    private SyslogFacility m_facility = SyslogFacility.UNKNOWN;
    private SyslogSeverity m_severity = SyslogSeverity.UNKNOWN;
    private Integer m_version;
    private Date m_date;
    private String m_hostname;
    private String m_processName;
    private Integer m_processId;
    private String m_messageId;
    private String m_message;
    private String m_matchedMessage;
    private String m_fullText;
    
    public SyslogMessage() {
    }

    public SyslogMessage(final int facility, final int severity, final Date date, String hostname, final String processName, final Integer processId, final String message) {
        this();

        m_facility = SyslogFacility.getFacility(facility);
        m_severity = SyslogSeverity.getSeverity(severity);
        m_date = date;
        m_processName = processName;
        m_processId = processId;
        m_message = message;
    }

    public Class<? extends SyslogParser> getParserClass() {
        return m_parserClass;
    }

    public void setParserClass(final Class<? extends SyslogParser> parser) {
        m_parserClass = parser;
    }

    public SyslogFacility getFacility() {
        return m_facility;
    }

    public void setFacility(final SyslogFacility facility) {
        m_fullText = null;
        m_facility = facility;
    }

    public SyslogSeverity getSeverity() {
        return m_severity;
    }

    public void setSeverity(final SyslogSeverity severity) {
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

    public InetAddress getHostAddress() {
        if (m_hostname != null) {
            try {
                return InetAddress.getByName(m_hostname);
            } catch (UnknownHostException e) {
                LOG.debug("Unable to resolve hostname '" + m_hostname + "' in syslog message.", e);
                return null;
            } catch (final IllegalArgumentException e) {
                LOG.debug("Illegal argument when trying to resolve hostname '" + m_hostname + "' in syslog message.", e);
                return null;
            }
        } else {
            return null;
        }
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

    public String getMatchedMessage() {
        return m_matchedMessage == null? m_message : m_matchedMessage;
    }

    public void setMatchedMessage(final String matchedMessage) {
        m_fullText = null;
        m_matchedMessage = matchedMessage;
    }

    public int getPriorityField() {
        if (m_severity != null && m_facility != null) {
            return m_severity.getPriority(m_facility);
        }
        return 0;
    }

    public String getSyslogFormattedDate() {
        if (m_date == null) return null;
        return m_dateFormat.get().format(m_date);
    }

    public String getRfc3339FormattedDate() {
        if (m_date == null) return null;
        return m_rfc3339Format.get().format(m_date);
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

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("parser", m_parserClass == null ? "Unknown" : m_parserClass.getName())
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
