/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.api;

import java.net.InetAddress;
import java.util.List;

import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

public interface SnmpAgentConfigFactory {
    /**
     * <p>getAgentConfig</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param location a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public SnmpAgentConfig getAgentConfig(InetAddress address, String location);

    /**
     * <p>getAgentConfig for a given profile </p>
     *
     * @param snmpProfile a @{@link Definition} object.
     * @param address a {@link InetAddress} object.
     * @return a {@link SnmpAgentConfig} object.
     */
    public SnmpAgentConfig getAgentConfigFromProfile(SnmpProfile snmpProfile, InetAddress address);

    /**
     * Merge this definition into current config.
     * @param definition Definition that has SNMP parameters associated with a specific IP address or Range.
     */
    void saveDefinition(Definition definition);

    /**
     * Remove an address from the definitions.
     * @param ipAddress IP address that needs to be removed from definition.
     * @param location  location at which this ipaddress belongs.
     * @param module    module from which the definition is getting removed.
     */
    boolean removeFromDefinition(InetAddress ipAddress, String location, String module);

    /**
     * Create definition and merge this definition into Current SNMP Config.
     * @param snmpAgentConfig agentConfig that might have succeeded in SNMP walk/get.
     * @param location the location that this agent config belongs.
     * @param module   module from which the definition is getting saved.
     */
    void saveAgentConfigAsDefinition(SnmpAgentConfig snmpAgentConfig, String location, String module);

    /**
     * Get all the SNMP profiles from SNMP Config.
     * @return a List of snmp profiles.
     */
    List<SnmpProfile> getProfiles();

}
