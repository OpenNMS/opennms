/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao.support;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * ProxySnmpAgentConfigFactory
 *
 * @author brozow
 */
public class ProxySnmpAgentConfigFactory implements SnmpAgentConfigFactory {

    public SnmpAgentConfig getAgentConfig(InetAddress address) {
        SnmpAgentConfig config = new SnmpAgentConfig(getLocalHost());
        config.setProxyFor(address);
        config.setPort(9161);
        return config;
    }

    private InetAddress getLocalHost() {
        try {
            return InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            throw new IllegalStateException("Unable to resolve 127.0.0.1");
        }
    }

}
