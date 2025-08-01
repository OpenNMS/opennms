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
package org.opennms.netmgt.config.api;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

public interface SnmpAgentConfigFactory {

    /**
     * <p>saveCurrent</p>
     * Saves current configuration in memory to file system.
     */
    void saveCurrent() throws IOException;

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
    default SnmpAgentConfig getAgentConfigFromProfile(SnmpProfile snmpProfile, InetAddress address) {
        return getAgentConfigFromProfile(snmpProfile, address, true);
    }

    SnmpAgentConfig getAgentConfigFromProfile(SnmpProfile snmpProfile, InetAddress address, boolean metaDataInterpolation);

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

    public SnmpConfig getSnmpConfig();

}
