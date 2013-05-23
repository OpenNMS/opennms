/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
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

package org.opennms.netmgt.provision.detector.jmx.client;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.opennms.netmgt.provision.support.Client;
import org.opennms.netmgt.provision.support.jmx.connectors.ConnectionWrapper;

/**
 * <p>Abstract JMXClient class.</p>
 *
 * @author Donald Desloge
 * @version $Id: $
 */
public abstract class JMXClient implements Client<ConnectionWrapper, Integer> {
    
    private ConnectionWrapper m_connection;
    
    /**
     * <p>close</p>
     */
    @Override
    public void close() {
        if(m_connection != null) {
            m_connection.close();
        }
        
    }
    
    /**
     * <p>getMBeanServerConnection</p>
     *
     * @param parameterMap a {@link java.util.Map} object.
     * @param address a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.provision.support.jmx.connectors.ConnectionWrapper} object.
     */
    protected abstract ConnectionWrapper getMBeanServerConnection(Map<String, Object> parameterMap, InetAddress address);
    
    /**
     * <p>generateMap</p>
     *
     * @param port a int.
     * @param timeout a int.
     * @return a {@link java.util.Map} object.
     */
    protected abstract Map<String, Object> generateMap(int port, int timeout);
    
    /** {@inheritDoc} */
    @Override
    public void connect(InetAddress address, int port, int timeout) throws IOException, Exception {
        m_connection = getMBeanServerConnection(generateMap(port, timeout), address);
    }

    /**
     * <p>receiveBanner</p>
     *
     * @return a {@link java.lang.Integer} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public Integer receiveBanner() throws IOException, Exception {
        if(m_connection != null) {
            return m_connection.getMBeanServer().getMBeanCount();
        }else {
            return -1;
        }
    }

    /**
     * <p>sendRequest</p>
     *
     * @param request a {@link org.opennms.netmgt.provision.support.jmx.connectors.ConnectionWrapper} object.
     * @return a {@link java.lang.Integer} object.
     * @throws java.io.IOException if any.
     * @throws java.lang.Exception if any.
     */
    @Override
    public Integer sendRequest(ConnectionWrapper request) throws IOException, Exception {
        return receiveResponse();
    }
    
    private Integer receiveResponse() throws IOException {
        return null;
    }

}
