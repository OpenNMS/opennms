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

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.alarmd.api.Destination;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration for the various SNMP Trap hosts to receive alarms via Traps.
 *
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
@XmlRootElement(name = "snmp-trap-sink")
@XmlAccessorType(XmlAccessType.FIELD)
public class SnmpTrapSink implements Destination {

    /** The Constant LOG. */
    public static final Logger LOG = LoggerFactory.getLogger(SnmpTrapSink.class);

    /** The Constant MAPPINGS_DIRECTORY_NAME. */
    public static final String MAPPINGS_DIRECTORY_NAME = "snmptrap-northbounder-mappings.d";

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The name. */
    @XmlElement(name = "name", required = true)
    private String m_name;

    /** The target IP address. */
    @XmlElement(name = "ip-address", required = true, defaultValue = "127.0.0.1")
    private String m_ipAddress;

    /** The target port. */
    @XmlElement(name = "port", required = false, defaultValue = "162")
    private Integer m_port;

    /** The SPEL expression that returns a string, to obtain the IP address used for the V1 Agent Address field (defaults to the alarm's IP address). */
    @XmlElement(name = "v1-agent-address", required = false)
    private String m_v1AgentAddress;

    /** The SNMP version. */
    @XmlElement(name = "version", required = true, defaultValue = "V1")
    private SnmpVersion m_version;

    /** The SNMP community. */
    @XmlElement(name = "community", required = false, defaultValue = "public")
    private String m_community;

    /** The mapping groups. */
    @XmlElement(name = "mapping-group", required = false)
    private List<SnmpTrapMappingGroup> m_mappings = new ArrayList<>();

    /** The import mappings. */
    @XmlElement(name = "import-mappings", required = false)
    private List<String> m_importMappings = new ArrayList<>();

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
        return m_ipAddress == null ? "127.0.0.1" : m_ipAddress;
    }

    /**
     * Gets the port.
     *
     * @return the port
     */
    public Integer getPort() {
        return m_port == null ? 162 : m_port;
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
        return m_version == null ? SnmpVersion.V1 : m_version;
    }

    /**
     * Gets the community.
     *
     * @return the community
     */
    public String getCommunity() {
        return m_community == null ? "public" : m_community;
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
     * Adds the mapping group.
     * <p>If there is a mapping group with the same name, the existing one will be overridden.</p>
     *
     * @param mappingGroup the mapping group
     */
    public void addMappingGroup(SnmpTrapMappingGroup mappingGroup) {
        int index = -1;
        for (int i = 0; i < m_mappings.size(); i++) {
            if (m_mappings.get(i).getName().equals(mappingGroup.getName())) {
                index = i;
                break;
            }
        }
        if (index > -1) {
            m_mappings.remove(index);
            m_mappings.add(index, mappingGroup);
        } else {
            m_mappings.add(mappingGroup);
        }
    }

    /**
     * Gets the mapping group file name.
     *
     * @param mappingName the mapping name
     * @return the full path for the mapping group file name
     */
    public String getMappingGroupFileName(String mappingName) {
        return MAPPINGS_DIRECTORY_NAME + File.separator + mappingName + ".xml";
    }

    /**
     * Gets the mapping group file.
     *
     * @param mappingFileName the mapping file name
     * @return the mapping group file
     */
    public File getMappingGroupFile(String mappingFileName) {
        return new File(ConfigFileConstants.getHome(), "etc" + File.separator + mappingFileName);
    }

    /**
     * Gets the import mapping.
     *
     * @param mappingName the mapping name
     * @return the import mapping
     * @throws Exception the exception
     */
    public SnmpTrapMappingGroup getImportMapping(String mappingName) throws Exception {
        final String fileName = getMappingGroupFileName(mappingName);
        if (m_importMappings.contains(fileName)) {
            return JaxbUtils.unmarshal(SnmpTrapMappingGroup.class, getMappingGroupFile(fileName));
        }
        return null;
    }

    /**
     * Adds the import mapping.
     *
     * @param mappingGroup the mapping group
     * @param fileName the file name
     * @return true, if successful
     */
    public void addImportMapping(SnmpTrapMappingGroup mappingGroup) throws Exception {
        final String fileName = getMappingGroupFileName(mappingGroup.getName());
        if (!m_importMappings.contains(fileName)) {
            m_importMappings.add(fileName);
        }
        JaxbUtils.marshal(mappingGroup, new FileWriter(getMappingGroupFile(fileName)));
    }

    /**
     * Removes the import mapping.
     *
     * @param mappingName the mapping name
     * @return true, if successful
     */
    public boolean removeImportMapping(String mappingName) {
        final String fileName = getMappingGroupFileName(mappingName);
        if (m_importMappings.contains(fileName)) {
            m_importMappings.remove(fileName);
            final File file = getMappingGroupFile(fileName);
            if (file.exists()) {
                return file.delete();
            }
        }
        return false;
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
        try {
            config.setDestinationAddress(InetAddress.getByName(m_ipAddress));
        } catch (UnknownHostException e) {
            throw new SnmpTrapException("Invalid target IP or FQDN", e);
        }
        config.setDestinationPort(m_port);
        config.setVersion(m_version);
        config.setCommunity(m_community);
        try {
            InetAddress hostAddress = null;
            if (m_v1AgentAddress == null) {
                if (alarm.getIpAddr() == null) {
                    hostAddress = InetAddress.getLocalHost();
                } else {
                    hostAddress = InetAddress.getByName(alarm.getIpAddr());
                }
            } else {
                InetAddress.getByName(m_v1AgentAddress);
            }
            config.setHostAddress(hostAddress);
        } catch (UnknownHostException e) {
            throw new SnmpTrapException("Invalid host address", e);
        }
        config.setEnterpriseId(mapping.getEnterpriseOid());
        config.setGeneric(mapping.getGeneric());
        config.setSpecific(mapping.getSpecific());
        config.setParameters(mapping.getParams(alarm));
        return config;
    }

    /**
     * Clean mapping groups.
     * <p>This is intended to be used when saving the configuration on a file.</p>
     * <p>All the groups that currently exist on the mappings list and also on the import-mappings list will be removed from the mapping list to avoid duplicates.</p>
     */
    public void cleanMappingGroups() {
        for (Iterator<SnmpTrapMappingGroup> it = m_mappings.iterator(); it.hasNext();) {
            SnmpTrapMappingGroup grp = it.next();
            if (m_importMappings.contains(getMappingGroupFileName(grp.getName()))) {
                LOG.debug("cleanMappingGroups: removing {} from {} as the content is managed through an external file", grp.getName(), getName());
                it.remove();
            } else {
                LOG.debug("cleanMappingGroups: keeping {} on {}", grp.getName(), getName());
            }

        }
        LOG.debug("cleanMappingGroups: {} group remains explicitly on {}", m_mappings.size(), getName());
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
