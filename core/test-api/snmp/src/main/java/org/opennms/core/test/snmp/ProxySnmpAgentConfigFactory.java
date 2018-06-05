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

package org.opennms.core.test.snmp;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.InetAddress;

import org.opennms.netmgt.config.SnmpPeerFactory;
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

}
