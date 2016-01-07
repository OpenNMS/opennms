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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.alarmd.api.Destination;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;

/**
 * Configuration for the various SNMP Trap hosts to receive alarms via Traps.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "snmp-trap-sink")
@XmlAccessorType(XmlAccessType.FIELD)
public class SnmpTrapSink implements Destination {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The name. */
    @XmlElement(name = "name", required = true)
    private String m_name;

    /** The target IP address. */
    @XmlElement(name = "ip-address", required = true)
    private String m_ipAddress = "127.0.0.1";

    /** The target port. */
    @XmlElement(name = "port", required = false)
    private int m_port = 162;

    /** The SPEL expression that returns a string, to obtain the IP address used for the V1 Agent Address field (defaults to the alarm's IP address). */
    @XmlElement(name = "v1-agent-address", required = false)
    private String m_v1AgentAddress = null;

    /** The SNMP version. */
    @XmlElement(name = "version", required = true)
    private SnmpVersion m_version = SnmpVersion.V1;

    /** The SNMP community. */
    @XmlElement(name = "community", required = false)
    private String m_community = "public";

    /** The mapping groups. */
    @XmlElement(name = "mapping-group", required = false)
    private List<SnmpTrapMappingGroup> m_mappings = new ArrayList<SnmpTrapMappingGroup>();

    /** The import mappings. */
    @XmlElement(name = "import-mappings", required = false)
    private List<String> m_importMappings = new ArrayList<String>();

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#isFirstOccurrenceOnly()
     */
    @Override
    public boolean isFirstOccurrenceOnly() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.alarmd.api.Destination#getName()
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * Gets the IP address.
     *
     * @return the IP address
     */
    public String getIpAddress() {
        return m_ipAddress;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public int getPort() {
        return m_port;
    }

    /**
     * Gets the v1 agent IP address.
     *
     * @return the v1 agent IP address
     */
    public String getV1AgentIpAddress() {
        return m_v1AgentAddress;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public SnmpVersion getVersion() {
        return m_version;
    }

    /**
     * Gets the community.
     *
     * @return the community
     */
    public String getCommunity() {
        return m_community;
    }

    /**
     * Gets the mappings.
     *
     * @return the mappings
     */
    public List<SnmpTrapMappingGroup> getMappings() {
        return m_mappings;
    }

    /**
     * Gets the import mappings.
     *
     * @return the import mappings
     */
    public List<String> getImportMappings() {
        return m_importMappings;
    }

    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.m_name = name;
    }

    /**
     * Sets the IP address.
     *
     * @param ipAddress the new IP address
     */
    public void setIpAddress(String ipAddress) {
        this.m_ipAddress = ipAddress;
    }

    /**
     * Sets the port.
     *
     * @param port the new port
     */
    public void setPort(int port) {
        this.m_port = port;
    }

    /**
     * Sets the v1 agent IP address.
     *
     * @param v1AgentIpAddress the new v1 agent IP address
     */
    public void setV1AgentIpAddress(String v1AgentIpAddress) {
        this.m_v1AgentAddress = v1AgentIpAddress;
    }

    /**
     * Sets the version.
     *
     * @param version the new version
     */
    public void setVersion(SnmpVersion version) {
        this.m_version = version;
    }

    /**
     * Sets the community.
     *
     * @param community the new community
     */
    public void setCommunity(String community) {
        this.m_community = community;
    }

    /**
     * Sets the mappings.
     *
     * @param mappings the new mappings
     */
    public void setMappings(List<SnmpTrapMappingGroup> mappings) {
        this.m_mappings = mappings;
    }

    /**
     * Sets the import mappings.
     *
     * @param importMappings the new import mappings
     */
    public void setImportMappings(List<String> importMappings) {
        this.m_importMappings = importMappings;
    }

    /**
     * Verifies if the sink accepts a given northbound alarm.
     *
     * @param alarm the northbound alarm
     * @return true, if the alarm is accepted
     */
    public boolean accepts(NorthboundAlarm alarm) {
        for (SnmpTrapMappingGroup group : m_mappings) {
            if (group.accepts(alarm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates the SNMP trap configuration object.
     *
     * @param alarm the northbound alarm
     * @return the SNMP trap configuration
     * @throws SnmpTrapException the SNMP trap exception
     */
    public SnmpTrapConfig createTrapConfig(NorthboundAlarm alarm) throws SnmpTrapException {
        SnmpTrapMapping mapping = getSnmpTrapMapping(alarm);
        if (mapping == null) {
            return null;
        }
        final SnmpTrapConfig config = new SnmpTrapConfig();
        config.setDestinationAddress(InetAddressUtils.addr(m_ipAddress));
        config.setDestinationPort(m_port);
        config.setVersion(m_version);
        config.setCommunity(m_community);
        config.setHostAddress(m_v1AgentAddress == null ? InetAddressUtils.addr(alarm.getIpAddr()) : InetAddressUtils.addr(m_v1AgentAddress));
        config.setEnterpriseId(mapping.getEnterpriseOid());
        config.setGeneric(mapping.getGeneric());
        config.setSpecific(mapping.getSpecific());
        config.setParameters(mapping.getParams(alarm));
        return config;
    }

    /**
     * Gets the SNMP trap mapping.
     *
     * @param alarm the northbound alarm
     * @return the SNMP trap mapping
     */
    private SnmpTrapMapping getSnmpTrapMapping(NorthboundAlarm alarm) {
        for (SnmpTrapMappingGroup group : m_mappings) {
            if (group.accepts(alarm)) {
                for (SnmpTrapMapping mapping : group.getMappings()) {
                    if (mapping.accepts(alarm)) {
                        return mapping;
                    }
                }
            }
        }
        return null;
    }

}
