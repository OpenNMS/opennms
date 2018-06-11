/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.syslog;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.opennms.netmgt.alarmd.api.Destination;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;

/**
 * Configuration for the various Syslog hosts to receive alarms via Syslog.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@XmlRootElement(name = "syslog-destination")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogDestination implements Destination {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * The Enumeration SyslogProtocol.
     */
    @XmlType
    @XmlEnum(String.class)
    public static enum SyslogProtocol {

        /** The UDP Syslog Protocol. */
        UDP("udp"),
        /** The TCP Syslog Protocol. */
        TCP("tcp");

        /** The m_id. */
        private String m_id;

        /**
         * Instantiates a new Syslog protocol.
         *
         * @param id the id
         */
        SyslogProtocol(String id) {
            m_id = id;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public String getId() {
            return m_id;
        }
    }

    /**
     * The Enumeration SyslogFacility.
     */
    @XmlType
    @XmlEnum(String.class)
    public static enum SyslogFacility {

        /** The Kernel Syslog Facility. */
        KERN("KERN"), 
        /** The User Syslog Facility. */
        USER("USER"), 
        /** The Mail Syslog Facility. */
        MAIL("MAIL"), 
        /** The Daemon Syslog Facility. */
        DAEMON("DAEMON"), 
        /** The Authentication Syslog Facility. */
        AUTH("AUTH"), 
        /** The Syslog Syslog Facility. */
        SYSLOG("SYSLOG"), 
        /** The LPR Syslog Facility. */
        LPR("LPR"), 
        /** The News Syslog Facility. */
        NEWS("NEWS"), 
        /** The UUCP Syslog Facility. */
        UUCP("UUCP"), 
        /** The CRON Syslog Facility. */
        CRON("CRON"), 
        /** The Authpriv Syslog Facility. */
        AUTHPRIV("AUTHPRIV"), 
        /** The FTP Syslog Facility. */
        FTP("FTP"), 
        /** The LOCAL0 Syslog Facility. */
        LOCAL0("LOCAL0"), 
        /** The LOCAL1 Syslog Facility. */
        LOCAL1("LOCAL1"), 
        /** The LOCAL2 Syslog Facility. */
        LOCAL2("LOCAL2"), 
        /** The LOCAL3 Syslog Facility. */
        LOCAL3("LOCAL3"), 
        /** The LOCAL4 Syslog Facility. */
        LOCAL4("LOCAL4"), 
        /** The LOCAL5 Syslog Facility. */
        LOCAL5("LOCAL5"), 
        /** The LOCAL6 Syslog Facility. */
        LOCAL6("LOCAL6"), 
        /** The LOCAL7 Syslog Facility. */
        LOCAL7("LOCAL7"), ;

        /** The ID. */
        private String m_id;

        /**
         * Instantiates a new Syslog facility.
         *
         * @param facility the facility
         */
        SyslogFacility(String facility) {
            m_id = facility;
        }

        /**
         * Gets the id.
         *
         * @return the id
         */
        public String getId() {
            return m_id;
        }
    }

    /** The destination name. */
    @XmlElement(name = "destination-name", required = true)
    private String m_destinationName;

    /** The target Syslog receiver host. */
    @XmlElement(name = "host", defaultValue = "localhost", required = false)
    private String m_host;

    /** The target Syslog receiver port. */
    @XmlElement(name = "port", defaultValue = "514", required = false)
    private Integer m_port;

    /** The target Syslog receiver protocol. */
    @XmlElement(name = "ip-protocol", defaultValue = "udp", required = false)
    private SyslogProtocol m_protocol;

    /** The target Syslog receiver facility. */
    @XmlElement(name = "facility", defaultValue = "USER", required = false)
    private SyslogFacility m_facility;

    /** The message char set. */
    @XmlElement(name = "char-set", defaultValue = "UTF-8", required = false)
    private String m_charSet;

    /** The max message length. */
    @XmlElement(name = "max-message-length", defaultValue = "1024", required = false)
    private Integer m_maxMessageLength = 1024;

    /** The send local name flag. */
    @XmlElement(name = "send-local-name", defaultValue = "true", required = false)
    private Boolean m_sendLocalName;

    /** The send local time flag. */
    @XmlElement(name = "send-local-time", defaultValue = "true", required = false)
    private Boolean m_sendLocalTime;

    /** The truncate message flag. */
    @XmlElement(name = "truncate-message", defaultValue = "false", required = false)
    private Boolean m_truncateMessage;

    /** The first occurrence only flag. */
    @XmlElement(name = "first-occurrence-only", defaultValue = "false", required = false)
    private Boolean m_firstOccurrenceOnly;

    /** The filters. */
    @XmlElement(name = "filter", required = false)
    private List<SyslogFilter> m_filters = new ArrayList<>();

    /**
     * Instantiates a new Syslog destination.
     */
    public SyslogDestination() {
    }

    /**
     * Instantiates a new Syslog destination.
     *
     * @param name the name
     * @param protocol the protocol
     * @param facility the facility
     */
    public SyslogDestination(String name, SyslogProtocol protocol, SyslogFacility facility) {
        m_destinationName = name;
        m_protocol = protocol;
        m_facility = facility;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#getName()
     */
    public String getName() {
        return m_destinationName;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        m_destinationName = name;
    }

    /**
     * Gets the host.
     *
     * @return the host
     */
    public String getHost() {
        return m_host == null ? "localhost" : m_host;
    }

    /**
     * Sets the host.
     *
     * @param m_host the new host
     */
    public void setHost(String m_host) {
        this.m_host = m_host;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public Integer getPort() {
        return m_port == null ? 514 : m_port;
    }

    /**
     * Sets the port.
     *
     * @param m_port the new port
     */
    public void setPort(Integer m_port) {
        this.m_port = m_port;
    }

    /**
     * Gets the protocol.
     *
     * @return the protocol
     */
    public SyslogProtocol getProtocol() {
        return m_protocol == null ? SyslogProtocol.UDP : m_protocol;
    }

    /**
     * Sets the protocol.
     *
     * @param m_protocol the new protocol
     */
    public void setProtocol(SyslogProtocol m_protocol) {
        this.m_protocol = m_protocol;
    }

    /**
     * Gets the facility.
     *
     * @return the facility
     */
    public SyslogFacility getFacility() {
        return m_facility == null ? SyslogFacility.USER : m_facility;
    }

    /**
     * Gets the char set.
     *
     * @return the char set
     */
    public String getCharSet() {
        return m_charSet == null ? StandardCharsets.UTF_8.name() : m_charSet;
    }

    /**
     * Sets the char set.
     *
     * @param charSet the new char set
     */
    public void setCharSet(String charSet) {
        m_charSet = charSet;
    }

    /**
     * Gets the max message length.
     *
     * @return the max message length
     */
    public Integer getMaxMessageLength() {
        return m_maxMessageLength == null ? 1024 : m_maxMessageLength;
    }

    /**
     * Sets the max message length.
     *
     * @param maxMessageLength the new max message length
     */
    public void setMaxMessageLength(Integer maxMessageLength) {
        m_maxMessageLength = maxMessageLength;
    }

    /**
     * Checks if is send local name flag.
     *
     * @return true, if is send local name flag
     */
    public Boolean isSendLocalName() {
        return m_sendLocalName == null ? Boolean.TRUE : m_sendLocalName;
    }

    /**
     * Sets the send local name flag.
     *
     * @param sendLocalName the new send local name flag
     */
    public void setSendLocalName(Boolean sendLocalName) {
        m_sendLocalName = sendLocalName;
    }

    /**
     * Checks if is send local time flag.
     *
     * @return true, if is send local time flag
     */
    public Boolean isSendLocalTime() {
        return m_sendLocalTime == null ? Boolean.TRUE : m_sendLocalTime;
    }

    /**
     * Sets the send local time flag.
     *
     * @param sendLocalTime the new send local time flag
     */
    public void setSendLocalTime(Boolean sendLocalTime) {
        m_sendLocalTime = sendLocalTime;
    }

    /**
     * Checks if is truncate message flag.
     *
     * @return true, if is truncate message flag
     */
    public Boolean isTruncateMessage() {
        return m_truncateMessage == null ? Boolean.FALSE : m_truncateMessage;
    }

    /**
     * Sets the truncate message flag.
     *
     * @param truncateMessage the new truncate message flag
     */
    public void setTruncateMessage(Boolean truncateMessage) {
        m_truncateMessage = truncateMessage;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#isFirstOccurrenceOnly()
     */
    public boolean isFirstOccurrenceOnly() {
        return m_firstOccurrenceOnly == null ? Boolean.FALSE : m_firstOccurrenceOnly;
    }

    /**
     * Sets the first occurrence only.
     *
     * @param firstOccurrenceOnly the new first occurrence only
     */
    public void setFirstOccurrenceOnly(Boolean firstOccurrenceOnly) {
        m_firstOccurrenceOnly = firstOccurrenceOnly;
    }

    /**
     * Gets the filters.
     *
     * @return the filters
     */
    public List<SyslogFilter> getFilters() {
        return m_filters;
    }

    /**
     * Sets the filters.
     *
     * @param filters the new filters
     */
    public void setFilters(List<SyslogFilter> filters) {
        this.m_filters = filters;
    }

    /**
     * Pass filter.
     * <p>If the destination doesn't have filter, the method will return true.</p>
     * <p>If the method has filters, they will be evaluated. If no filters are satisfied, the method will return false.
     * Otherwise, the method will return true as soon as one filter is satisfied.</p>
     *
     * @param alarm the alarm
     * @return true, if successful
     */
    public boolean passFilter(NorthboundAlarm alarm) {
        if (m_filters != null && m_filters.isEmpty() == false) {
            for (SyslogFilter filter : m_filters) {
                if (filter.passFilter(alarm)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Gets the custom message format.
     *
     * @param alarm the alarm
     * @return the custom message format
     */
    public String getCustomMessageFormat(NorthboundAlarm alarm) {
        if (m_filters != null) {
            for (SyslogFilter filter : m_filters) {
                if (filter.getMessageFormat() != null && filter.passFilter(alarm)) {
                    return filter.getMessageFormat();
                }
            }
        }
        return null;
    }
}
