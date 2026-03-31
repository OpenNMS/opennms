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

import org.opennms.netmgt.config.snmp.Configuration;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.Range;
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
     * <p>setAndSaveConfig</p>
     * Sets and then saves the given configuration to file system.
     * Use caution, this will overwrite the configuration.
     */
    void setAndSaveConfig(SnmpConfig snmpConfig) throws IOException;

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
     * @param save If true, save to the backing store or DAO.
     */
    void saveDefinition(Definition definition, boolean save);

    /**
     * Remove an address from the definitions.
     * @param ipAddress IP address that needs to be removed from definition.
     * @param location  location at which this ipaddress belongs.
     * @param module    module from which the definition is getting removed.
     */
    boolean removeFromDefinition(InetAddress ipAddress, String location, String module);

    /**
     * Remove the given IP address ranges and/or specific IP addresses from the definitions.
     * Must supply at least one range or specific.
     * @param ranges List of ranges to remove. Set to null or empty list to ignore.
     * @param specifics List of individual IP addresses to remove. Set to null or empty list to ignore.
     * @param ipMatches List of IP match expressions to remove. Set to null or empty list to ignore.
     * @param location  location at which this ipaddress belongs.
     * @param module    module from which the definition is getting removed.
     */
    boolean removeRangesFromDefinition(List<Range> ranges, List<String> specifics,
                                       List<String> ipMatches, String location, String module);

    /**
     * Create definition and merge this definition into Current SNMP Config.
     * @param snmpAgentConfig agentConfig that might have succeeded in SNMP walk/get.
     * @param location the location that this agent config belongs.
     * @param module   module from which the definition is getting saved.
     */
    void saveAgentConfigAsDefinition(SnmpAgentConfig snmpAgentConfig, String location, String module);

    /**
     * Save default configuration overrides into current SNMP config.
     * *NOTE*: Null values will NOT be ignored, they will replace values in the current config, meaning that
     * default values will be used for any parameters that are set to null in the given config.
     */
    void saveDefaultOverrides(Configuration config);

    /**
     * Save an SnmpProfile. The given profile must include a label.
     * If a profile with the label exists, replace it, otherwise add a new profile.
     * Only fields that differ from defaults (for new profiles), or override any existing values (for
     * updated profiles) should be non-null.
     */
    void saveProfile(SnmpProfile profile);

    /**
     * Remove the SnmpProfile with the given label.
     */
    boolean removeProfile(String label);

    /**
     * Get all the SNMP profiles from SNMP Config.
     * @return a List of snmp profiles.
     */
    List<SnmpProfile> getProfiles();

    public SnmpConfig getSnmpConfig();
}
