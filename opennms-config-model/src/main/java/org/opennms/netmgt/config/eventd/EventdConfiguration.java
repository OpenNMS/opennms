/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 * 
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.config.eventd;


import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

@XmlRootElement(name = "EventdConfiguration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("eventd-configuration.xsd")
public class EventdConfiguration implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * The IP address on which eventd listens for TCP connections.
     *  If "" is specified, eventd will bind to all addresses. The default
     *  address is 127.0.0.1.
     */
    @XmlAttribute(name = "TCPAddress")
    private String m_tcpAddress = "127.0.0.1";

    /**
     * The port on which eventd listens for TCP connections.
     *  The default port is 5817.
     */
    @XmlAttribute(name = "TCPPort", required = true)
    private Integer m_tcpPort;

    /**
     * The IP address on which eventd listens for UDP packets.
     *  If "" is specified, eventd will bind to all addresses. The default
     *  address is 127.0.0.1.
     */
    @XmlAttribute(name = "UDPAddress")
    private String m_udpAddress = "127.0.0.1";

    /**
     * The port on which eventd listens for UDP packets. The
     *  default port is 5817.
     */
    @XmlAttribute(name = "UDPPort", required = true)
    private Integer m_udpPort;

    /**
     * The maximum number of threads used for reading and
     *  processing of incoming events.
     */
    @XmlAttribute(name = "receivers", required = true)
    private Integer m_receivers;

    /**
     * The maximum number of incoming events that can be 
     *  queued inside eventd for processing.
     */
    @XmlAttribute(name = "queueLength")
    private Integer m_queueLength;

    /**
     * SQL query to get next value of the 'nodeNxtId'
     *  database sequence. This is used to make the access database
     *  independent.
     */
    @XmlAttribute(name = "getNextEventID")
    private String m_getNextEventID;

    /**
     * Set the socket timeout value. In Linux, the close()
     *  system call is not preemptive. Setting this attribute to to 'yes'
     *  causes the socketSoTimeoutPeriod attribute to be set on sockets to
     *  mimic non-blocking socket I/O.
     */
    @XmlAttribute(name = "socketSoTimeoutRequired", required = true)
    private String m_socketSoTimeoutRequired;

    /**
     * Socket timeout, in milliseconds. This is only set on
     *  eventd's sockets if socketSoTimeoutRequired is set to
     *  'yes'.
     */
    @XmlAttribute(name = "socketSoTimeoutPeriod")
    private Integer m_socketSoTimeoutPeriod;

    /**
     * Whether or not to log a simple event summary. By default, OpenNMS
     *  logs detailed event information at DEBUG level. If this option is enabled,
     *  it will additionally log a simple summary of events received at INFO.
     */
    @XmlAttribute(name = "logEventSummaries")
    private Boolean m_logEventSummaries;

    @XmlValue
    private String m_contents;

    public EventdConfiguration() {
    }

    public Optional<String> getTCPAddress() {
        return Optional.ofNullable(m_tcpAddress);
    }

    public void setTCPAddress(final String TCPAddress) {
        m_tcpAddress = ConfigUtils.normalizeString(TCPAddress);
    }

    public Integer getTCPPort() {
        return m_tcpPort;
    }

    public void setTCPPort(final Integer TCPPort) {
        m_tcpPort = ConfigUtils.assertNotNull(TCPPort, "TCPPort");
    }

    public Optional<String> getUDPAddress() {
        return Optional.ofNullable(m_udpAddress);
    }

    public void setUDPAddress(final String UDPAddress) {
        m_udpAddress = ConfigUtils.normalizeString(UDPAddress);
    }

    public Integer getUDPPort() {
        return m_udpPort;
    }

    public void setUDPPort(final Integer UDPPort) {
        m_udpPort = ConfigUtils.assertNotNull(UDPPort, "UDPPort");
    }

    public Integer getReceivers() {
        return m_receivers;
    }

    public void setReceivers(final Integer receivers) {
        m_receivers = ConfigUtils.assertNotNull(receivers, "receivers");
    }

    public Optional<Integer> getQueueLength() {
        return Optional.ofNullable(m_queueLength);
    }

    public void setQueueLength(final Integer queueLength) {
        m_queueLength = queueLength;
    }

    public Optional<String> getGetNextEventID() {
        return Optional.ofNullable(m_getNextEventID);
    }

    public void setGetNextEventID(final String getNextEventID) {
        m_getNextEventID = ConfigUtils.normalizeString(getNextEventID);
    }

    public String getSocketSoTimeoutRequired() {
        return m_socketSoTimeoutRequired;
    }

    public void setSocketSoTimeoutRequired(final String socketSoTimeoutRequired) {
        m_socketSoTimeoutRequired = ConfigUtils.assertNotNull(socketSoTimeoutRequired, "socketSoTimeoutRequired");
    }

    public Optional<Integer> getSocketSoTimeoutPeriod() {
        return Optional.ofNullable(m_socketSoTimeoutPeriod);
    }

    public void setSocketSoTimeoutPeriod(final Integer socketSoTimeoutPeriod) {
        m_socketSoTimeoutPeriod = socketSoTimeoutPeriod;
    }

    public Boolean getLogEventSummaries() {
        return m_logEventSummaries != null ? m_logEventSummaries : true;
    }

    public void setLogEventSummaries(final Boolean logEventSummaries) {
        m_logEventSummaries = logEventSummaries;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_tcpAddress, 
                            m_tcpPort, 
                            m_udpAddress, 
                            m_udpPort, 
                            m_receivers, 
                            m_queueLength, 
                            m_getNextEventID, 
                            m_socketSoTimeoutRequired, 
                            m_socketSoTimeoutPeriod, 
                            m_logEventSummaries);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof EventdConfiguration) {
            final EventdConfiguration that = (EventdConfiguration)obj;
            return Objects.equals(this.m_tcpAddress, that.m_tcpAddress)
                    && Objects.equals(this.m_tcpPort, that.m_tcpPort)
                    && Objects.equals(this.m_udpAddress, that.m_udpAddress)
                    && Objects.equals(this.m_udpPort, that.m_udpPort)
                    && Objects.equals(this.m_receivers, that.m_receivers)
                    && Objects.equals(this.m_queueLength, that.m_queueLength)
                    && Objects.equals(this.m_getNextEventID, that.m_getNextEventID)
                    && Objects.equals(this.m_socketSoTimeoutRequired, that.m_socketSoTimeoutRequired)
                    && Objects.equals(this.m_socketSoTimeoutPeriod, that.m_socketSoTimeoutPeriod)
                    && Objects.equals(this.m_logEventSummaries, that.m_logEventSummaries);
        }
        return false;
    }

}
