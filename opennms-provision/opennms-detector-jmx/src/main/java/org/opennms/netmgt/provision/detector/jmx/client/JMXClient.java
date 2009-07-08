/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.provision.detector.jmx.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.jmx.connectors.ConnectionWrapper;

/**
 * @author Donald Desloge
 *
 */
public abstract class JMXClient implements Client<ConnectionWrapper, Integer> {
    
    private ConnectionWrapper m_connection;
    
    public void close() {
        if(m_connection != null) {
            m_connection.close();
        }
        
    }
    
    protected abstract ConnectionWrapper getMBeanServerConnection(Map<String, Object> parameterMap, InetAddress address);
    
    protected abstract Map<String, Object> generateMap(int port, int timeout);
    
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        m_connection = getMBeanServerConnection(generateMap(port, timeout), address);
    }

    public Integer receiveBanner() throws IOException, Exception {
        if(m_connection != null) {
            return m_connection.getMBeanServer().getMBeanCount();
        }else {
            return -1;
        }
    }

    public Integer sendRequest(ConnectionWrapper request) throws IOException, Exception {
        return receiveResponse();
    }
    
    private Integer receiveResponse() throws IOException {
        return null;
    }

}
