/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.northbounder.jms;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Configuration for the JMS NBI.
 *
 * @author <a mailto:david@opennms.org>David Hustace</a>
 * @author <a mailto:dschlenk@converge-one.com>David Schlenk</a>
 */
@XmlRootElement(name = "jms-northbounder-config")
@XmlType(namespace = "http://xmlns.opennms.org/xsd/config/jms-northbounder")
@XmlAccessorType(XmlAccessType.FIELD)
public class JmsNorthbounderConfig {

    /** The enabled flag. */
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

    /** The destinations. */
    @XmlElement(name = "destination")
    private List<JmsDestination> m_destinations;

    /** The UEIs. */
    @XmlElement(name = "uei", required = false)
    private List<String> m_ueis;

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
     * Gets the destinations.
     *
     * @return the destinations
     */
    public List<JmsDestination> getDestinations() {
        return m_destinations;
    }

    /**
     * Sets the destinations.
     *
     * @param destinations the new destinations
     */
    public void setDestinations(List<JmsDestination> destinations) {
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
     * Gets the destination.
     *
     * @param destinationName the name to match
     * @return destination in destinations list who's toString equals destinationName.
     */
    public JmsDestination getDestination(String destinationName) {
        JmsDestination ret = null;
        for (JmsDestination dest : m_destinations) {
            if (dest.getName().equals(destinationName)) {
                ret = dest;
                break;
            }
        }
        return ret;
    }
}
