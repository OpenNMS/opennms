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
package org.opennms.core.test.snmp;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.config.snmp.SnmpConfig;
import org.opennms.netmgt.config.snmp.SnmpProfile;
import org.opennms.netmgt.dao.api.SnmpConfigDao;
import org.opennms.netmgt.snmp.SnmpAgentAddress;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;

public class ProxySnmpAgentConfigFactory extends SnmpPeerFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(ProxySnmpAgentConfigFactory.class);

    public ProxySnmpAgentConfigFactory(final InputStream config) throws FileNotFoundException {
       super(new InputStreamResource(config));
    }

    @Override
    public SnmpAgentConfig getAgentConfig(final InetAddress address) {
        return getAgentConfig(address, null);
    }

    @Override
    public SnmpAgentConfig getAgentConfig(final InetAddress address, String location) {
        final SnmpAgentConfigProxyMapper mapper = SnmpAgentConfigProxyMapper.getInstance();
        final SnmpAgentAddress agentAddress = mapper.getAddress(address);

        final String addressString = str(address);
        if (agentAddress == null) {
            LOG.debug(
                    "No agent address mapping found for {}!  Try adding a @JUnitSnmpAgent(host=\"{}\", resource=\"...\" entry...",
                    addressString, addressString);

            return super.getAgentConfig(address, location);

        }

        final SnmpAgentConfig config = new SnmpAgentConfig(agentAddress.getAddress());
        config.setProxyFor(address);
        config.setPort(agentAddress.getPort());

        LOG.debug("proxying {} -> {}", addressString, agentAddress);
        return config;
    }

    @Override
    public SnmpAgentConfig getAgentConfigFromProfile(SnmpProfile snmpProfile, InetAddress address, boolean metaDataInterpolation) {
        final SnmpAgentConfigProxyMapper mapper = SnmpAgentConfigProxyMapper.getInstance();
        final SnmpAgentAddress agentAddress = mapper.getAddress(address);

        final String addressString = str(address);
        if (agentAddress == null) {
            LOG.debug(
                    "No agent address mapping found for {}!  Try adding a @JUnitSnmpAgent(host=\"{}\", resource=\"...\" entry...",
                    addressString, addressString);

            return super.getAgentConfig(address, null, metaDataInterpolation);

        }
        SnmpAgentConfig config = super.getAgentConfigFromProfile(snmpProfile, agentAddress.getAddress(), metaDataInterpolation);
        config.setProxyFor(address);
        config.setPort(agentAddress.getPort());

        LOG.debug("proxying {} -> {}", addressString, agentAddress);
        return config;
    }

}
