/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

    // Standard sink pattern constants.
    private static final int NUM_THREADS = 0;
    private static final int BATCH_SIZE = 1000;
    private static final int BATCH_INTERVAL = 500;
    private static final int QUEUE_SIZE = 10000;

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
     * Whether or not to log a simple event summary. By default, OpenNMS
     *  logs detailed event information at DEBUG level. If this option is enabled,
     *  it will additionally log a simple summary of events received at INFO.
     */
    @XmlAttribute(name = "logEventSummaries")
    private Boolean m_logEventSummaries;

    @XmlValue
    private String m_contents;


    /**
     * Number of threads used for consuming/dispatching messages.
     * Defaults to 2 x the number of available processors.
     */
    @XmlAttribute(name = "sink-threads", required=false)
    private Integer m_threads;

    /**
     * Maximum number of messages to keep in memory while waiting
     to be dispatched.
     */
	@XmlAttribute(name="sink-queue-size", required=false)
    private Integer m_queueSize;

    /**
     * Messages are aggregated in batches before being dispatched.
     * When the batch reaches this size, it will be dispatched.
     */
	@XmlAttribute(name="sink-batch-size", required=false)
    private Integer m_batchSize;

    /**
     * Messages are aggregated in batches before being dispatched.
     * When the batch has been created for longer than this interval (ms)
     * it will be dispatched, regardless of the current size.
     */
	@XmlAttribute(name="sink-batch-interval", required=false)
    private Integer m_batchInterval;

    public EventdConfiguration() {
    }

    public Integer getNumThreads() {
    	return m_threads == null ? NUM_THREADS : m_threads;
    }

    public void setNumThreads(final Integer numThreads) {
        m_threads = numThreads;
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

    public Boolean getLogEventSummaries() {
        return m_logEventSummaries != null ? m_logEventSummaries : true;
    }

    public void setLogEventSummaries(final Boolean logEventSummaries) {
        m_logEventSummaries = logEventSummaries;
    }

    public int getQueueSize() {
        return m_queueSize == null ? QUEUE_SIZE : m_queueSize;
    }

    public void setQueueSize(int _queueSize) {
        this.m_queueSize = _queueSize;
    }

    public int getBatchSize() {
        return m_batchSize == null ? BATCH_SIZE : m_batchSize;
    }

    public void setBatchSize(int _batchSize) {
        this.m_batchSize = _batchSize;
    }

    public int getBatchInterval() {
        return m_batchInterval == null ? BATCH_INTERVAL : m_batchInterval;
    }

    public void setBatchInterval(int _batchInterval) {
        this.m_batchInterval = _batchInterval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                            m_queueLength,
                            m_getNextEventID,
                            m_logEventSummaries,
                            m_threads,
                            m_queueSize,
                            m_batchSize,
                            m_batchInterval);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof EventdConfiguration) {
            final EventdConfiguration that = (EventdConfiguration)obj;
            return Objects.equals(this.m_queueLength, that.m_queueLength)
                    && Objects.equals(this.m_getNextEventID, that.m_getNextEventID)
                    && Objects.equals(this.m_logEventSummaries, that.m_logEventSummaries)
                    && Objects.equals(this.m_threads, that.m_threads)
                    && Objects.equals(this.m_queueSize, that.m_queueSize)
            		&& Objects.equals(this.m_batchSize, that.m_batchSize)
            		&& Objects.equals(this.m_batchInterval, that.m_batchInterval);
        }
        return false;
    }

}
