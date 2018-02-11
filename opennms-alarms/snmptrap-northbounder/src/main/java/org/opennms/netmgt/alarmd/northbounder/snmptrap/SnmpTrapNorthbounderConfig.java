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

package org.opennms.netmgt.alarmd.northbounder.snmptrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Configuration for SNMP Trap NBI implementation.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "snmptrap-northbounder-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class SnmpTrapNorthbounderConfig implements Serializable {

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

    /** The SNMP trap sinks. */
    @XmlElement(name = "snmp-trap-sink")
    private List<SnmpTrapSink> m_snmpTrapSinks = new ArrayList<>();

    /** The UEIs. */
    @XmlElement(name = "uei", required = false)
    private List<String> m_ueis;

    /**
     * Gets the SNMP Trap sinks.
     *
     * @return the SNMP Trap sinks
     */
    public List<SnmpTrapSink> getSnmpTrapSinks() {
        return m_snmpTrapSinks;
    }

    /**
     * Sets the SNMP Trap sinks.
     *
     * @param snmpTrapSinks the new SNMP Trap sinks
     */
    public void setDestinations(List<SnmpTrapSink> snmpTrapSinks) {
        m_snmpTrapSinks = snmpTrapSinks;
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
     * Gets a specific trap sink.
     *
     * @param trapSinkName the trap sink name
     * @return the trap sink object
     */
    public SnmpTrapSink getSnmpTrapSink(String trapSinkName) {
        for (SnmpTrapSink sink : m_snmpTrapSinks) {
            if (sink.getName().equals(trapSinkName)) {
                return sink;
            }
        }
        return null;
    }

    /**
     * Adds the SNMP trap sink.
     * <p>If there is a trap sink with the same name, the existing one will be overridden.</p>
     *
     * @param snmpTrapSink the SNMP trap sink
     */
    public void addSnmpTrapSink(SnmpTrapSink snmpTrapSink) {
        int index = -1;
        for (int i = 0; i < m_snmpTrapSinks.size(); i++) {
            if (m_snmpTrapSinks.get(i).getName().equals(snmpTrapSink.getName())) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            m_snmpTrapSinks.remove(index);
            m_snmpTrapSinks.add(index, snmpTrapSink);
        } else {
            m_snmpTrapSinks.add(snmpTrapSink);
        }
    }

    /**
     * Removes a specific SNMP trap sink.
     *
     * @param trapSinkName the trap sink name
     * @return true, if successful
     */
    public boolean removeSnmpTrapSink(String trapSinkName) {
        int index = -1;
        for (int i = 0; i < m_snmpTrapSinks.size(); i++) {
            if (m_snmpTrapSinks.get(i).getName().equals(trapSinkName)) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            m_snmpTrapSinks.remove(index);
            return true;
        }
        return false;
    }

}