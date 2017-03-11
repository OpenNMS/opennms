/*******************************************************************************
 * This file is part of OpenNMS(R).
 * 
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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


import java.util.Objects;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Top-level element for the syslogd-configuration.xml
 *  configuration file.
 *  
 * 
 * @version $Revision$ $Date$
 */
@XmlRootElement(name = "configuration")
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_PARSER = "org.opennms.netmgt.syslogd.CustomSyslogParser";
    private static final String DEFAULT_DISCARD_UEI = "DISCARD-MATCHING-MESSAGES";

    /**
     * The address on which Syslogd listens for SYSLOG Messages. The
     *  default is to listen on all addresses.
     *  
     */
    @XmlAttribute(name = "listen-address")
    private String listenAddress;

    /**
     * The port on which Syslogd listens for SYSLOG Messages. The
     *  standard port is 514.
     *  
     */
    @XmlAttribute(name = "syslog-port", required = true)
    private Integer syslogPort;

    /**
     * Whether messages from devices unknown to OpenNMS should
     *  generate newSuspect events.
     *  
     */
    @XmlAttribute(name = "new-suspect-on-message")
    private Boolean newSuspectOnMessage;

    /**
     * The regular expression used to separate message and host.
     *  
     */
    @XmlAttribute(name = "forwarding-regexp")
    private String forwardingRegexp;

    /**
     * The matching group for the host
     */
    @XmlAttribute(name = "matching-group-host")
    private Integer matchingGroupHost;

    /**
     * The matching group for the message
     */
    @XmlAttribute(name = "matching-group-message")
    private Integer matchingGroupMessage;

    /**
     * A string which defines the class to use when parsing syslog messages.
     *  The default is the "CustomSyslogParser", which honors the
     * forwarding-regexp,
     *  matching-group-host, and matching-group-message attributes, and can parse
     *  most BSD-style Syslog messages, including Syslog-NG's default format.
     *  Other options include "org.opennms.netmgt.syslogd.SyslogNGParser" which is
     * a
     *  slightly more strict version of the CustomSyslogParser, and
     *  "org.opennms.netmgt.syslogd.Rfc5424SyslogParser" which can handle the
     * recent
     *  (2009) RFC for syslog messages.
     *  
     */
    @XmlAttribute(name = "parser")
    private String parser;

    /**
     * A string which, when used as the value of a "uei"
     *  element inside a "ueiMatch" element, results in all
     *  matching messages to be discarded without an event
     *  ever being created
     *  
     */
    @XmlAttribute(name = "discard-uei")
    private String discardUei;

    /**
     * Number of threads used for consuming/dispatching messages.
     *  Defaults to 2 x the number of available processors.
     *  
     */
    @XmlAttribute(name = "threads")
    private Integer threads;

    /**
     * Maximum number of messages to keep in memory while waiting
     *  to be dispatched.
     *  
     */
    @XmlAttribute(name = "queue-size")
    private Integer queueSize;

    /**
     * Messages are aggregated in batches before being dispatched.
     *  When the batch reaches this size, it will be dispatched.
     *  
     */
    @XmlAttribute(name = "batch-size")
    private Integer batchSize;

    /**
     * Messages are aggregated in batches before being dispatched.
     *  When the batch has been created for longer than this interval (ms)
     *  it will be dispatched, regardless of the current size.
     *  
     */
    @XmlAttribute(name = "batch-interval")
    private Integer batchInterval;

    /**
     */
    public void deleteBatchInterval() {
        this.batchInterval= null;
    }

    /**
     */
    public void deleteBatchSize() {
        this.batchSize= null;
    }

    /**
     */
    public void deleteMatchingGroupHost() {
        this.matchingGroupHost= null;
    }

    /**
     */
    public void deleteMatchingGroupMessage() {
        this.matchingGroupMessage= null;
    }

    /**
     */
    public void deleteNewSuspectOnMessage() {
        this.newSuspectOnMessage= null;
    }

    /**
     */
    public void deleteQueueSize() {
        this.queueSize= null;
    }

    /**
     */
    public void deleteSyslogPort() {
        this.syslogPort= null;
    }

    /**
     */
    public void deleteThreads() {
        this.threads= null;
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
            Configuration temp = (Configuration)obj;
            boolean equals = Objects.equals(temp.listenAddress, listenAddress)
                && Objects.equals(temp.syslogPort, syslogPort)
                && Objects.equals(temp.newSuspectOnMessage, newSuspectOnMessage)
                && Objects.equals(temp.forwardingRegexp, forwardingRegexp)
                && Objects.equals(temp.matchingGroupHost, matchingGroupHost)
                && Objects.equals(temp.matchingGroupMessage, matchingGroupMessage)
                && Objects.equals(temp.parser, parser)
                && Objects.equals(temp.discardUei, discardUei)
                && Objects.equals(temp.threads, threads)
                && Objects.equals(temp.queueSize, queueSize)
                && Objects.equals(temp.batchSize, batchSize)
                && Objects.equals(temp.batchInterval, batchInterval);
            return equals;
        }
        return false;
    }

    /**
     * Returns the value of field 'batchInterval'. The field 'batchInterval' has
     * the following description: Messages are aggregated in batches before being
     * dispatched.
     *  When the batch has been created for longer than this interval (ms)
     *  it will be dispatched, regardless of the current size.
     *  
     * 
     * @return the value of field 'BatchInterval'.
     */
    public Integer getBatchInterval() {
        return this.batchInterval != null ? this.batchInterval : Integer.valueOf("500");
    }

    /**
     * Returns the value of field 'batchSize'. The field 'batchSize' has the
     * following description: Messages are aggregated in batches before being
     * dispatched.
     *  When the batch reaches this size, it will be dispatched.
     *  
     * 
     * @return the value of field 'BatchSize'.
     */
    public Integer getBatchSize() {
        return this.batchSize != null ? this.batchSize : Integer.valueOf("1000");
    }

    /**
     * Returns the value of field 'discardUei'. The field 'discardUei' has the
     * following description: A string which, when used as the value of a "uei"
     *  element inside a "ueiMatch" element, results in all
     *  matching messages to be discarded without an event
     *  ever being created
     *  
     * 
     * @return the value of field 'DiscardUei'.
     */
    public String getDiscardUei() {
        return this.discardUei != null ? this.discardUei : DEFAULT_DISCARD_UEI;
    }

    /**
     * Returns the value of field 'forwardingRegexp'. The field 'forwardingRegexp'
     * has the following description: The regular expression used to separate
     * message and host.
     *  
     * 
     * @return the value of field 'ForwardingRegexp'.
     */
    public String getForwardingRegexp() {
        return this.forwardingRegexp;
    }

    /**
     * Returns the value of field 'listenAddress'. The field 'listenAddress' has
     * the following description: The address on which Syslogd listens for SYSLOG
     * Messages. The
     *  default is to listen on all addresses.
     *  
     * 
     * @return the value of field 'ListenAddress'.
     */
    public String getListenAddress() {
        return this.listenAddress;
    }

    /**
     * Returns the value of field 'matchingGroupHost'. The field
     * 'matchingGroupHost' has the following description: The matching group for
     * the host
     * 
     * @return the value of field 'MatchingGroupHost'.
     */
    public Integer getMatchingGroupHost() {
        return this.matchingGroupHost;
    }

    /**
     * Returns the value of field 'matchingGroupMessage'. The field
     * 'matchingGroupMessage' has the following description: The matching group
     * for the message
     * 
     * @return the value of field 'MatchingGroupMessage'.
     */
    public Integer getMatchingGroupMessage() {
        return this.matchingGroupMessage;
    }

    /**
     * Returns the value of field 'newSuspectOnMessage'. The field
     * 'newSuspectOnMessage' has the following description: Whether messages from
     * devices unknown to OpenNMS should
     *  generate newSuspect events.
     *  
     * 
     * @return the value of field 'NewSuspectOnMessage'.
     */
    public Boolean getNewSuspectOnMessage() {
        return this.newSuspectOnMessage != null ? this.newSuspectOnMessage : Boolean.valueOf("false");
    }

    /**
     * Returns the value of field 'parser'. The field 'parser' has the following
     * description: A string which defines the class to use when parsing syslog
     * messages.
     *  The default is the "CustomSyslogParser", which honors the
     * forwarding-regexp,
     *  matching-group-host, and matching-group-message attributes, and can parse
     *  most BSD-style Syslog messages, including Syslog-NG's default format.
     *  Other options include "org.opennms.netmgt.syslogd.SyslogNGParser" which is
     * a
     *  slightly more strict version of the CustomSyslogParser, and
     *  "org.opennms.netmgt.syslogd.Rfc5424SyslogParser" which can handle the
     * recent
     *  (2009) RFC for syslog messages.
     *  
     * 
     * @return the value of field 'Parser'.
     */
    public String getParser() {
        return this.parser != null ? this.parser : DEFAULT_PARSER;
    }

    /**
     * Returns the value of field 'queueSize'. The field 'queueSize' has the
     * following description: Maximum number of messages to keep in memory while
     * waiting
     *  to be dispatched.
     *  
     * 
     * @return the value of field 'QueueSize'.
     */
    public Integer getQueueSize() {
        return this.queueSize != null ? this.queueSize : Integer.valueOf("10000");
    }

    /**
     * Returns the value of field 'syslogPort'. The field 'syslogPort' has the
     * following description: The port on which Syslogd listens for SYSLOG
     * Messages. The
     *  standard port is 514.
     *  
     * 
     * @return the value of field 'SyslogPort'.
     */
    public Integer getSyslogPort() {
        return this.syslogPort;
    }

    /**
     * Returns the value of field 'threads'. The field 'threads' has the following
     * description: Number of threads used for consuming/dispatching messages.
     *  Defaults to 2 x the number of available processors.
     *  
     * 
     * @return the value of field 'Threads'.
     */
    public Integer getThreads() {
        return this.threads;
    }

    /**
     * Method hasBatchInterval.
     * 
     * @return true if at least one BatchInterval has been added
     */
    public boolean hasBatchInterval() {
        return this.batchInterval != null;
    }

    /**
     * Method hasBatchSize.
     * 
     * @return true if at least one BatchSize has been added
     */
    public boolean hasBatchSize() {
        return this.batchSize != null;
    }

    /**
     * Method hasMatchingGroupHost.
     * 
     * @return true if at least one MatchingGroupHost has been added
     */
    public boolean hasMatchingGroupHost() {
        return this.matchingGroupHost != null;
    }

    /**
     * Method hasMatchingGroupMessage.
     * 
     * @return true if at least one MatchingGroupMessage has been added
     */
    public boolean hasMatchingGroupMessage() {
        return this.matchingGroupMessage != null;
    }

    /**
     * Method hasNewSuspectOnMessage.
     * 
     * @return true if at least one NewSuspectOnMessage has been added
     */
    public boolean hasNewSuspectOnMessage() {
        return this.newSuspectOnMessage != null;
    }

    /**
     * Method hasQueueSize.
     * 
     * @return true if at least one QueueSize has been added
     */
    public boolean hasQueueSize() {
        return this.queueSize != null;
    }

    /**
     * Method hasSyslogPort.
     * 
     * @return true if at least one SyslogPort has been added
     */
    public boolean hasSyslogPort() {
        return this.syslogPort != null;
    }

    /**
     * Method hasThreads.
     * 
     * @return true if at least one Threads has been added
     */
    public boolean hasThreads() {
        return this.threads != null;
    }

    /**
     * Method hashCode.
     * 
     * @return a hash code value for the object.
     */
    @Override
    public int hashCode() {
        int hash = Objects.hash(
            listenAddress, 
            syslogPort, 
            newSuspectOnMessage, 
            forwardingRegexp, 
            matchingGroupHost, 
            matchingGroupMessage, 
            parser, 
            discardUei, 
            threads, 
            queueSize, 
            batchSize, 
            batchInterval);
        return hash;
    }

    /**
     * Returns the value of field 'newSuspectOnMessage'. The field
     * 'newSuspectOnMessage' has the following description: Whether messages from
     * devices unknown to OpenNMS should
     *  generate newSuspect events.
     *  
     * 
     * @return the value of field 'NewSuspectOnMessage'.
     */
    public Boolean isNewSuspectOnMessage() {
        return this.newSuspectOnMessage != null ? this.newSuspectOnMessage : Boolean.valueOf("false");
    }

    /**
     * Sets the value of field 'batchInterval'. The field 'batchInterval' has the
     * following description: Messages are aggregated in batches before being
     * dispatched.
     *  When the batch has been created for longer than this interval (ms)
     *  it will be dispatched, regardless of the current size.
     *  
     * 
     * @param batchInterval the value of field 'batchInterval'.
     */
    public void setBatchInterval(final Integer batchInterval) {
        this.batchInterval = batchInterval;
    }

    /**
     * Sets the value of field 'batchSize'. The field 'batchSize' has the
     * following description: Messages are aggregated in batches before being
     * dispatched.
     *  When the batch reaches this size, it will be dispatched.
     *  
     * 
     * @param batchSize the value of field 'batchSize'.
     */
    public void setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
    }

    /**
     * Sets the value of field 'discardUei'. The field 'discardUei' has the
     * following description: A string which, when used as the value of a "uei"
     *  element inside a "ueiMatch" element, results in all
     *  matching messages to be discarded without an event
     *  ever being created
     *  
     * 
     * @param discardUei the value of field 'discardUei'.
     */
    public void setDiscardUei(final String discardUei) {
        this.discardUei = discardUei;
    }

    /**
     * Sets the value of field 'forwardingRegexp'. The field 'forwardingRegexp'
     * has the following description: The regular expression used to separate
     * message and host.
     *  
     * 
     * @param forwardingRegexp the value of field 'forwardingRegexp'.
     */
    public void setForwardingRegexp(final String forwardingRegexp) {
        this.forwardingRegexp = forwardingRegexp;
    }

    /**
     * Sets the value of field 'listenAddress'. The field 'listenAddress' has the
     * following description: The address on which Syslogd listens for SYSLOG
     * Messages. The
     *  default is to listen on all addresses.
     *  
     * 
     * @param listenAddress the value of field 'listenAddress'.
     */
    public void setListenAddress(final String listenAddress) {
        this.listenAddress = listenAddress;
    }

    /**
     * Sets the value of field 'matchingGroupHost'. The field 'matchingGroupHost'
     * has the following description: The matching group for the host
     * 
     * @param matchingGroupHost the value of field 'matchingGroupHost'.
     */
    public void setMatchingGroupHost(final Integer matchingGroupHost) {
        this.matchingGroupHost = matchingGroupHost;
    }

    /**
     * Sets the value of field 'matchingGroupMessage'. The field
     * 'matchingGroupMessage' has the following description: The matching group
     * for the message
     * 
     * @param matchingGroupMessage the value of field 'matchingGroupMessage'.
     */
    public void setMatchingGroupMessage(final Integer matchingGroupMessage) {
        this.matchingGroupMessage = matchingGroupMessage;
    }

    /**
     * Sets the value of field 'newSuspectOnMessage'. The field
     * 'newSuspectOnMessage' has the following description: Whether messages from
     * devices unknown to OpenNMS should
     *  generate newSuspect events.
     *  
     * 
     * @param newSuspectOnMessage the value of field 'newSuspectOnMessage'.
     */
    public void setNewSuspectOnMessage(final Boolean newSuspectOnMessage) {
        this.newSuspectOnMessage = newSuspectOnMessage;
    }

    /**
     * Sets the value of field 'parser'. The field 'parser' has the following
     * description: A string which defines the class to use when parsing syslog
     * messages.
     *  The default is the "CustomSyslogParser", which honors the
     * forwarding-regexp,
     *  matching-group-host, and matching-group-message attributes, and can parse
     *  most BSD-style Syslog messages, including Syslog-NG's default format.
     *  Other options include "org.opennms.netmgt.syslogd.SyslogNGParser" which is
     * a
     *  slightly more strict version of the CustomSyslogParser, and
     *  "org.opennms.netmgt.syslogd.Rfc5424SyslogParser" which can handle the
     * recent
     *  (2009) RFC for syslog messages.
     *  
     * 
     * @param parser the value of field 'parser'.
     */
    public void setParser(final String parser) {
        this.parser = parser;
    }

    /**
     * Sets the value of field 'queueSize'. The field 'queueSize' has the
     * following description: Maximum number of messages to keep in memory while
     * waiting
     *  to be dispatched.
     *  
     * 
     * @param queueSize the value of field 'queueSize'.
     */
    public void setQueueSize(final Integer queueSize) {
        this.queueSize = queueSize;
    }

    /**
     * Sets the value of field 'syslogPort'. The field 'syslogPort' has the
     * following description: The port on which Syslogd listens for SYSLOG
     * Messages. The
     *  standard port is 514.
     *  
     * 
     * @param syslogPort the value of field 'syslogPort'.
     */
    public void setSyslogPort(final Integer syslogPort) {
        this.syslogPort = syslogPort;
    }

    /**
     * Sets the value of field 'threads'. The field 'threads' has the following
     * description: Number of threads used for consuming/dispatching messages.
     *  Defaults to 2 x the number of available processors.
     *  
     * 
     * @param threads the value of field 'threads'.
     */
    public void setThreads(final Integer threads) {
        this.threads = threads;
    }

}
