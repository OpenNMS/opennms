/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 2 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.ConfigurationTestUtils;

/**
 * ProxySnmpAgentConfigFactory
 *
 * @author brozow
 */
public class ProxySnmpAgentConfigFactory extends SnmpPeerFactory {

    public ProxySnmpAgentConfigFactory() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        super(ConfigurationTestUtils.getInputStreamForConfigFile("snmp-config.xml"));
    }

    public SnmpAgentConfig getAgentConfig(final InetAddress address) {
    	final SnmpAgentConfig config = new SnmpAgentConfig(InetAddressUtils.getLocalHostAddress());
        config.setProxyFor(address);
        // This port should match the default port inside {@link JUnitSnmpAgent}
        config.setPort(9161);
        return config;
    }

}
