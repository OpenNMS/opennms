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

package org.opennms.netmgt.config.syslogd;


import java.io.Serializable;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.TimeZone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

import com.google.common.base.Strings;

/**
 * Top-level element for the syslogd-configuration.xml configuration file.
 */
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("syslog.xsd")
public class Configuration implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_PARSER = "org.opennms.netmgt.syslogd.CustomSyslogParser";
    private static final String DEFAULT_DISCARD_UEI = "DISCARD-MATCHING-MESSAGES";

    /**
     * The address on which Syslogd listens for SYSLOG Messages. The
     *  default is to listen on all addresses.
     *  
     */
    @XmlAttribute(name = "listen-address")
    private String m_listenAddress;

    /**
     * The port on which Syslogd listens for SYSLOG Messages. The
     *  standard port is 514.
     */
    @XmlAttribute(name = "syslog-port", required = true)
    private Integer m_syslogPort;

    /**
     * Whether messages from devices unknown to OpenNMS should
     *  generate newSuspect events.
     */
    @XmlAttribute(name = "new-suspect-on-message")
    private Boolean m_newSuspectOnMessage;

    /**
     * The regular expression used to separate message and host.
     */
    @XmlAttribute(name = "forwarding-regexp")
    private String m_forwardingRegexp;

    /**
     * The matching group for the host
     */
    @XmlAttribute(name = "matching-group-host")
    private Integer m_matchingGroupHost;

    /**
     * The matching group for the message
     */
    @XmlAttribute(name = "matching-group-message")
    private Integer m_matchingGroupMessage;

    /**
     * A string which defines the class to use when parsing syslog messages.
     *  The default is the "CustomSyslogParser", which honors the forwarding-regexp,
     *  matching-group-host, and matching-group-message attributes, and can parse
     *  most BSD-style Syslog messages, including Syslog-NG's default format.
     *  Other options include "org.opennms.netmgt.syslogd.SyslogNGParser" which is a
     *  slightly more strict version of the CustomSyslogParser, and
     *  "org.opennms.netmgt.syslogd.Rfc5424SyslogParser" which can handle the
     * recent (2009) RFC for syslog messages.
     *  
     */
    @XmlAttribute(name = "parser")
    private String m_parser;

    /**
     * A string which, when used as the value of a "uei"
     *  element inside a "ueiMatch" element, results in all
     *  matching messages to be discarded without an event
     *  ever being created
     */
    @XmlAttribute(name = "discard-uei")
    private String m_discardUei;

    /**
     * Number of threads used for consuming/dispatching messages.
     *  Defaults to 2 x the number of available processors.
     */
    @XmlAttribute(name = "threads")
    private Integer m_threads;

    /**
     * Maximum number of messages to keep in memory while waiting
     *  to be dispatched.
     */
    @XmlAttribute(name = "queue-size")
    private Integer m_queueSize;

    /**
     * Messages are aggregated in batches before being dispatched.
     *  When the batch reaches this size, it will be dispatched.
     */
    @XmlAttribute(name = "batch-size")
    private Integer m_batchSize;

    /**
     * Messages are aggregated in batches before being dispatched.
     *  When the batch has been created for longer than this interval (ms)
     *  it will be dispatched, regardless of the current size.
     */
    @XmlAttribute(name = "batch-interval")
    private Integer m_batchInterval;

    @XmlAttribute(name = "timezone")
    private String timeZone;

    public Optional<String> getListenAddress() {
        return Optional.ofNullable(m_listenAddress);
    }

    public void setListenAddress(final String listenAddress) {
        m_listenAddress = ConfigUtils.normalizeString(listenAddress);
    }

    public Integer getSyslogPort() {
        return m_syslogPort;
    }

    public void setSyslogPort(final Integer syslogPort) {
        m_syslogPort = ConfigUtils.assertMinimumInclusive(ConfigUtils.assertNotNull(syslogPort, "syslog-port"), 1, "syslog-port");
    }

    public Boolean getNewSuspectOnMessage() {
        return m_newSuspectOnMessage != null ? m_newSuspectOnMessage : Boolean.FALSE;
    }

    public void setNewSuspectOnMessage(final Boolean newSuspectOnMessage) {
        m_newSuspectOnMessage = newSuspectOnMessage;
    }

    public Optional<String> getForwardingRegexp() {
        return Optional.ofNullable(m_forwardingRegexp);
    }

    public void setForwardingRegexp(final String forwardingRegexp) {
        m_forwardingRegexp = ConfigUtils.normalizeString(forwardingRegexp);
    }

    public Optional<Integer> getMatchingGroupHost() {
        return Optional.ofNullable(m_matchingGroupHost);
    }

    public void setMatchingGroupHost(final Integer matchingGroupHost) {
        m_matchingGroupHost = ConfigUtils.assertMinimumInclusive(matchingGroupHost, 1, "matching-group-host");
    }

    public Optional<Integer> getMatchingGroupMessage() {
        return Optional.ofNullable(m_matchingGroupMessage);
    }

    public void setMatchingGroupMessage(final Integer matchingGroupMessage) {
        m_matchingGroupMessage = ConfigUtils.assertMinimumInclusive(matchingGroupMessage, 1, "matching-group-message");
    }

    public String getParser() {
        return m_parser != null ? m_parser : DEFAULT_PARSER;
    }

    public void setParser(final String parser) {
        m_parser = ConfigUtils.normalizeString(parser);
    }

    public String getDiscardUei() {
        return m_discardUei != null ? m_discardUei : DEFAULT_DISCARD_UEI;
    }

    public void setDiscardUei(final String discardUei) {
        m_discardUei = ConfigUtils.normalizeString(discardUei);
    }

    public Optional<Integer> getThreads() {
        return Optional.ofNullable(m_threads);
    }

    public void setThreads(final Integer threads) {
        m_threads = ConfigUtils.assertMinimumInclusive(threads, 1, "threads");
    }

    public Integer getQueueSize() {
        return m_queueSize != null ? m_queueSize : 10000;
    }

    public void setQueueSize(final Integer queueSize) {
        m_queueSize = ConfigUtils.assertMinimumInclusive(queueSize, 1, "queue-size");
    }

    public Integer getBatchSize() {
        return m_batchSize != null ? m_batchSize : 1000;
    }

    public void setBatchSize(final Integer batchSize) {
        m_batchSize = ConfigUtils.assertMinimumInclusive(batchSize, 1, "batch-size");
    }

    public Integer getBatchInterval() {
        return m_batchInterval != null ? m_batchInterval : 500;
    }

    public void setBatchInterval(final Integer batchInterval) {
        m_batchInterval = ConfigUtils.assertMinimumInclusive(batchInterval, 1, "batch-interval");
    }

    public Optional<TimeZone> getTimeZone(){
        if(Strings.emptyToNull(this.timeZone) ==null){
            return Optional.empty();
        }
        return Optional.of(TimeZone.getTimeZone(ZoneId.of(timeZone)));
    }

    public void setTimeZone(String timeZone){
        if(Strings.emptyToNull(timeZone) == null ){
            this.timeZone = null;
        }
        // test if zone is valid:
        ZoneId.of(timeZone);
        this.timeZone = timeZone;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_listenAddress, 
                            m_syslogPort, 
                            m_newSuspectOnMessage, 
                            m_forwardingRegexp, 
                            m_matchingGroupHost, 
                            m_matchingGroupMessage, 
                            m_parser, 
                            m_discardUei, 
                            m_threads, 
                            m_queueSize, 
                            m_batchSize, 
                            m_batchInterval,
                            timeZone);
    }

    /**
     * Overrides the Object.equals method.
     * 
     * @param obj
     * @return true if the objects are equal.
     */
    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Configuration) {
            final Configuration that = (Configuration)obj;
            return Objects.equals(this.m_listenAddress, that.m_listenAddress)
                    && Objects.equals(this.m_syslogPort, that.m_syslogPort)
                    && Objects.equals(this.m_newSuspectOnMessage, that.m_newSuspectOnMessage)
                    && Objects.equals(this.m_forwardingRegexp, that.m_forwardingRegexp)
                    && Objects.equals(this.m_matchingGroupHost, that.m_matchingGroupHost)
                    && Objects.equals(this.m_matchingGroupMessage, that.m_matchingGroupMessage)
                    && Objects.equals(this.m_parser, that.m_parser)
                    && Objects.equals(this.m_discardUei, that.m_discardUei)
                    && Objects.equals(this.m_threads, that.m_threads)
                    && Objects.equals(this.m_queueSize, that.m_queueSize)
                    && Objects.equals(this.m_batchSize, that.m_batchSize)
                    && Objects.equals(this.m_batchInterval, that.m_batchInterval)
                    && Objects.equals(this.timeZone, that.timeZone);
        }
        return false;
    }

}
