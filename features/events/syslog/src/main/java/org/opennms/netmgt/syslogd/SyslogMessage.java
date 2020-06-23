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
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SyslogMessage implements Cloneable {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogMessage.class);
    private static final ThreadLocal<DateFormat> m_rfc3164Format = new ThreadLocal<DateFormat>() {
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

    private Integer m_year;
    private Integer m_month;
    private Integer m_dayOfMonth;
    private Integer m_hourOfDay;
    private Integer m_minute;
    private Integer m_second;
    private Integer m_millisecond;
    private ZoneId m_zoneId;

    private String m_hostname;
    private String m_processName;
    private String m_processId;
    private String m_messageId;
    private String m_message;

    /**
     * A map to store generic syslog message parameters that are not otherwise handled by the above specific fields.
     */
    private final Map<String, String> m_parameters = new HashMap<>();

    public SyslogMessage() {
    }

    /**
     * Copy constructor used by {@link #clone()}.
     * 
     * @param facility
     * @param severity
     * @param version
     * @param date
     * @param year
     * @param month
     * @param dayOfMonth
     * @param hourOfDay
     * @param minute
     * @param second
     * @param millisecond
     * @param zoneId
     * @param hostname
     * @param processName
     * @param processId
     * @param messageId
     * @param message
     * @param parameters
     */
    protected SyslogMessage(
        final SyslogFacility facility,
        final SyslogSeverity severity,
        final Integer version,
        final Date date,
        final Integer year,
        final Integer month,
        final Integer dayOfMonth,
        final Integer hourOfDay,
        final Integer minute,
        final Integer second,
        final Integer millisecond,
        final ZoneId zoneId,
        final String hostname,
        final String processName,
        final String processId,
        final String messageId,
        final String message,
        final Map<String, String> parameters
    ) {
        m_facility = facility;
        m_severity = severity;
        m_version = version;
        m_date = date;
        m_year = year;
        m_month = month;
        m_dayOfMonth = dayOfMonth;
        m_hourOfDay = hourOfDay;
        m_minute = minute;
        m_second = second;
        m_millisecond = millisecond;
        m_zoneId = zoneId;
        m_hostname = hostname;
        m_processName = processName;
        m_processId = processId;
        m_messageId = messageId;
        m_message = message;
        m_parameters.putAll(parameters);
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
        m_facility = facility;
    }

    public SyslogSeverity getSeverity() {
        return m_severity;
    }

    public void setSeverity(final SyslogSeverity severity) {
        m_severity = severity;
    }

    public Integer getVersion() {
        return m_version;
    }

    public void setVersion(final Integer version) {
        m_version = version;
    }

    public Date getDate() {
        return m_date;
    }

    public void setDate(final Date date) {
        m_date = date;
    }

    public Integer getYear() {
        return m_year;
    }

    public void setYear(Integer year) {
        m_year = year;
    }

    public Integer getMonth() {
        return m_month;
    }

    public void setMonth(Integer month) {
        m_month = month;
    }

    public Integer getDayOfMonth() {
        return m_dayOfMonth;
    }

    public void setDayOfMonth(Integer dayOfMonth) {
        m_dayOfMonth = dayOfMonth;
    }

    public Integer getHourOfDay() {
        return m_hourOfDay;
    }

    public void setHourOfDay(Integer hourOfDay) {
        m_hourOfDay = hourOfDay;
    }

    public Integer getMinute() {
        return m_minute;
    }

    public void setMinute(Integer minute) {
        m_minute = minute;
    }

    public Integer getSecond() {
        return m_second;
    }

    public void setSecond(Integer second) {
        m_second = second;
    }

    public Integer getMillisecond() {
        return m_millisecond;
    }

    public void setMillisecond(Integer millisecond) {
        m_millisecond = millisecond;
    }

    public ZoneId getZoneId() {
        return m_zoneId;
    }

    public void setZoneId(ZoneId zoneId) {
        m_zoneId = zoneId;
    }

    public String getHostName() {
        return m_hostname;
    }
    
    public void setHostName(final String hostname) {
        m_hostname = hostname;
    }

    public InetAddress getHostAddress() {
        if (m_hostname != null) {
            try {
                return InetAddress.getByName(m_hostname);
            } catch (UnknownHostException e) {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Unable to resolve hostname '{}' in syslog message", m_hostname, e);
                } else {
                    LOG.debug("Unable to resolve hostname '{}' in syslog message", m_hostname);
                }
                return null;
            } catch (final IllegalArgumentException e) {
                LOG.debug("Illegal argument when trying to resolve hostname '{}' in syslog message", m_hostname, e);
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
        m_processName = processName;
    }

    public String getProcessId() {
        return m_processId;
    }
    
    public void setProcessId(final String processId) {
        m_processId = processId;
    }

    public String getMessageID() {
        return m_messageId;
    }

    public void setMessageID(final String messageId) {
        m_messageId = messageId;
    }

    public String getMessage() {
        return m_message;
    }

    public void setMessage(final String message) {
        m_message = message;
    }

    public void addParameter(String key, String value) {
        m_parameters.put(key, value);
    }

    public Map<String, String> getParameters() {
        return Collections.unmodifiableMap(m_parameters);
    }

    private int getPriorityField() {
        if (m_severity != null && m_facility != null) {
            return m_severity.getPriority(m_facility);
        }
        return 0;
    }

    public String getRfc3164FormattedDate() {
        if (m_date == null) return null;
        return m_rfc3164Format.get().format(m_date);
    }

    public static String getRfc3164FormattedDate(Date date) {
        if (date == null) return null;
        return m_rfc3164Format.get().format(date);
    }

    public String getRfc3339FormattedDate() {
        if (m_date == null) return null;
        return m_rfc3339Format.get().format(m_date);
    }

    public String asRfc3164Message() {
        if (m_processName != null) {
            if (m_processId != null) {
                return String.format("<%d>%s %s %s[%s]: %s", getPriorityField(), getRfc3164FormattedDate(), getHostName(), getProcessName(), getProcessId(), getMessage());
            } else  {
                return String.format("<%d>%s %s %s: %s", getPriorityField(), getRfc3164FormattedDate(), getHostName(), getProcessName(), getMessage());
            }
        } else {
            return String.format("<%d>%s %s %s", getPriorityField(), getRfc3164FormattedDate(), getHostName(), getMessage());
        }
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("parser", m_parserClass == null ? "Unknown" : m_parserClass.getName())
            .append("facility", m_facility)
            .append("severity", m_severity)
            .append("version", m_version)
            .append("date", m_date)
            .append("year", m_year)
            .append("month", m_month)
            .append("dayOfMonth", m_dayOfMonth)
            .append("hourOfDay", m_hourOfDay)
            .append("minute", m_minute)
            .append("second", m_second)
            .append("millisecond", m_millisecond)
            .append("zoneId", m_zoneId == null ? null : m_zoneId.getId())
            .append("hostname", m_hostname)
            .append("message ID", m_messageId)
            .append("process name", m_processName)
            .append("process ID", m_processId)
            .append("message", m_message)
            .append("parameters", m_parameters)
            .toString();
    }

    public void setParam(String key, String value) {
        throw new UnsupportedOperationException(String.format("Cannot process param %s -> %s, setting arbitrary params is not supported yet", key, value));
    }

    public void setParam(String key, Integer value) {
        throw new UnsupportedOperationException(String.format("Cannot process param %s -> %d, setting arbitrary params is not supported yet", key, value));
    }

    @Override
    public SyslogMessage clone() {
        return new SyslogMessage(
            m_facility,
            m_severity,
            m_version,
            m_date,
            m_year,
            m_month,
            m_dayOfMonth,
            m_hourOfDay,
            m_minute,
            m_second,
            m_millisecond,
            m_zoneId,
            m_hostname,
            m_processName,
            m_processId,
            m_messageId,
            m_message,
            m_parameters
        );
    }
}
