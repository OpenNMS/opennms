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
package org.opennms.netmgt.alarmd.northbounder.syslog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration for Syslog NBI implementation.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@XmlRootElement(name = "syslog-northbounder-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class SyslogNorthbounderConfig implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The enabled. */
    @XmlElement(name = "enabled", required = false, defaultValue = "false")
    private Boolean m_enabled;

    /** The nagles delay. */
    @XmlElement(name = "nagles-delay", required = false, defaultValue = "1000")
    private Integer m_naglesDelay;

    /** The batch size. */
    @XmlElement(name = "batch-size", required = false, defaultValue = "100")
    private Integer m_batchSize;

    /** The queue size. */
    @XmlElement(name = "queue-size", required = false, defaultValue = "300000")
    private Integer m_queueSize;

    /** The message format. */
    @XmlElement(name = "message-format", required = false, defaultValue = "ALARM ID:${alarmId} NODE:${nodeLabel} ${logMsg}")
    private String m_messageFormat;
    
    /** The date format. */
    @XmlElement(name = "date-format", required = false)
    private String m_dateFormat;

    /** The destinations. */
    @XmlElement(name = "destination")
    private List<SyslogDestination> m_destinations = new ArrayList<>();

    /** The UEIs. */
    @XmlElement(name = "uei", required = false)
    private List<String> m_ueis;

    /**
     * Gets the destinations.
     *
     * @return the destinations
     */
    public List<SyslogDestination> getDestinations() {
        return m_destinations;
    }

    /**
     * Sets the destinations.
     *
     * @param destinations the new destinations
     */
    public void setDestinations(List<SyslogDestination> destinations) {
        m_destinations = destinations;
    }

    /**
     * Gets the UEIs.
     *
     * @return the UEIs
     */
    public List<String> getUeis() {
        return m_ueis;
    }

    /**
     * Sets the UEIs.
     *
     * @param ueis the new UEIs
     */
    public void setUeis(List<String> ueis) {
        m_ueis = ueis;
    }

    /**
     * Gets the message format.
     *
     * @return the message format
     */
    public String getMessageFormat() {
        return m_messageFormat == null ? "ALARM ID:${alarmId} NODE:${nodeLabel} ${logMsg}" : m_messageFormat;
    }

    /**
     * Sets the message format.
     *
     * @param messageFormat the new message format
     */
    public void setMessageFormat(String messageFormat) {
        m_messageFormat = messageFormat;
    }

    /**
     * Gets the date format.
     *
     * @return the date format
     */
    public String getDateFormat() {
        return m_dateFormat;
    }

    /**
     * Sets the date format.
     *
     * @param dateFormat sets the date format
     */
    public void setDateFormat(String dateFormat) {
        this.m_dateFormat = dateFormat;
    }

    /**
     * Gets the nagles delay.
     *
     * @return the nagles delay
     */
    public Integer getNaglesDelay() {
        return m_naglesDelay == null ? 1000 : m_naglesDelay;
    }

    /**
     * Sets the nagles delay.
     *
     * @param naglesDelay the new nagles delay
     */
    public void setNaglesDelay(Integer naglesDelay) {
        m_naglesDelay = naglesDelay;
    }

    /**
     * Gets the batch size.
     *
     * @return the batch size
     */
    public Integer getBatchSize() {
        return m_batchSize == null ? 100 : m_batchSize;
    }

    /**
     * Sets the batch size.
     *
     * @param batchSize the new batch size
     */
    public void setBatchSize(Integer batchSize) {
        m_batchSize = batchSize;
    }

    /**
     * Gets the queue size.
     *
     * @return the queue size
     */
    public Integer getQueueSize() {
        return m_queueSize == null ? 300000 : m_queueSize;
    }

    /**
     * Sets the queue size.
     *
     * @param alarmQueueSize the new queue size
     */
    public void setQueueSize(Integer alarmQueueSize) {
        m_queueSize = alarmQueueSize;
    }

    /**
     * Checks if is enabled.
     *
     * @return the boolean
     */
    public Boolean isEnabled() {
        return m_enabled == null ? Boolean.FALSE : m_enabled;
    }

    /**
     * Sets the enabled.
     *
     * @param enabled the new enabled
     */
    public void setEnabled(Boolean enabled) {
        m_enabled = enabled;
    }

    /**
     * Gets a specific Syslog destination.
     *
     * @param syslogDestinationName the Syslog destination name
     * @return the Syslog destination
     */
    public SyslogDestination getSyslogDestination(String syslogDestinationName) {
        for (SyslogDestination dest : m_destinations) {
            if (dest.getName().equals(syslogDestinationName)) {
                return dest;
            }
        }
        return null;
    }

    /**
     * Adds a specific Syslog destination.
     * <p>If there is a destination with the same name, the existing one will be overridden.</p>
     *
     * @param syslogDestination the Syslog destination object
     */
    public void addSyslogDestination(SyslogDestination syslogDestination) {
        int index = -1;
        for (int i = 0; i < m_destinations.size(); i++) {
            if (m_destinations.get(i).getName().equals(syslogDestination.getName())) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            m_destinations.remove(index);
            m_destinations.add(index, syslogDestination);
        } else {
            m_destinations.add(syslogDestination);
        }
    }

    /**
     * Removes a specific syslog destination.
     *
     * @param syslogDestinationName the Syslog destination name
     * @return true, if successful
     */
    public boolean removeSyslogDestination(String syslogDestinationName) {
        int index = -1;
        for (int i = 0; i < m_destinations.size(); i++) {
            if (m_destinations.get(i).getName().equals(syslogDestinationName)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            m_destinations.remove(index);
            return true;
        }
        return false;
    }

}
