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
package org.opennms.netmgt.provision.detector;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import org.opennms.netmgt.config.SnmpEventInfo;
import org.opennms.netmgt.config.api.SnmpAgentConfigFactory;
import org.opennms.netmgt.config.snmp.Definition;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * @author Donald Desloge
 *
 */
public class AnAgentConfigFactory implements SnmpAgentConfigFactory {

    public void define(final SnmpEventInfo info) {
    }

    @Override
    public void saveCurrent() throws IOException {

    }

    @Override
    public SnmpAgentConfig getAgentConfig(InetAddress address, String location) {
        final SnmpAgentConfig agentConfig = new SnmpAgentConfig(address);
        agentConfig.setVersion(SnmpAgentConfig.DEFAULT_VERSION);
        return agentConfig;
    }

    @Override
    public SnmpAgentConfig getAgentConfigFromProfile(SnmpProfile snmpProfile, InetAddress address, boolean metaDataInterpolation) {
        return null;
    }

    @Override
    public void saveDefinition(Definition definition) {

    }

    @Override
    public boolean removeFromDefinition(InetAddress ipAddress, String location, String module) {
       return true;
    }

    @Override
    public void saveAgentConfigAsDefinition(SnmpAgentConfig snmpAgentConfig, String location, String module) {

    }

    @Override
    public List<SnmpProfile> getProfiles() {
        return null;
    }

    @Override
    public SnmpConfig getSnmpConfig() {
        return null;
    }

}
