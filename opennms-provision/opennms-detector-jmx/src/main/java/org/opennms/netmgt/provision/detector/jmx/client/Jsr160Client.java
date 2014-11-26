/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.detector.jmx.client;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.provision.support.jmx.connectors.ConnectionWrapper;
import org.opennms.netmgt.provision.support.jmx.connectors.Jsr160ConnectionFactory;

/**
 * <p>Jsr160Client class.</p>
 *
 * @author thedesloge
 * @version $Id: $
 */
public class Jsr160Client extends JMXClient {
    
    private Map<String, Object> m_parameterMap;
    
    /**
     * <p>Constructor for Jsr160Client.</p>
     */
    public Jsr160Client() {
        m_parameterMap = new HashMap<String, Object>();
    }
    
    /** {@inheritDoc} */
    @Override
    protected Map<String, Object> generateMap(int port, int timeout) {
        
        m_parameterMap.put("port",           port);
        m_parameterMap.put("timeout", timeout);
        return Collections.unmodifiableMap(m_parameterMap);
    }

    /** {@inheritDoc} */
    @Override
    protected ConnectionWrapper getMBeanServerConnection(Map<String, Object> parameterMap, InetAddress address) {
        return Jsr160ConnectionFactory.getMBeanServerConnection(parameterMap, address);
    }
    
    /**
     * <p>setFactory</p>
     *
     * @param factory a {@link java.lang.String} object.
     */
    public void setFactory(String factory) {
        m_parameterMap.put("factory", factory);
    }
    
    /**
     * <p>setFriendlyName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setFriendlyName(String name) {
        m_parameterMap.put("friendlyname", name);
    }
    
    /**
     * <p>setUsername</p>
     *
     * @param username a {@link java.lang.String} object.
     */
    public void setUsername(String username) {
        m_parameterMap.put("username", username);
    }
    
    /**
     * <p>setPassword</p>
     *
     * @param password a {@link java.lang.String} object.
     */
    public void setPassword(String password) {
        m_parameterMap.put("password", password);
    }
    
    /**
     * <p>setUrlPath</p>
     *
     * @param urlPath a {@link java.lang.String} object.
     */
    public void setUrlPath(String urlPath) {
        m_parameterMap.put("urlPath", urlPath);
    }
    
    /**
     * <p>setType</p>
     *
     * @param type a {@link java.lang.String} object.
     */
    public void setType(String type) {
        m_parameterMap.put("type", type);
    }

    /**
     * <p>setProtocol</p>
     *
     * @param protocol a {@link java.lang.String} object.
     */
    public void setProtocol(String protocol) {
        m_parameterMap.put("protocol", protocol);
        
    }
    
}
