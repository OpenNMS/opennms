/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.common;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public abstract class NodeCollector extends Discovery {
    /**
     * The node ID of the system used to collect the SNMP information
     */
    protected final Node m_node;
    private final LocationAwareSnmpClient m_locationAwareSnmpClient;

    /**
     * Constructs a new SNMP collector for a node using the passed interface
     * as the collection point. The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
     * @param nodeid
     * @param config
     *            The SnmpPeer object to collect from.
     */
    public NodeCollector(final LocationAwareSnmpClient locationAwareSnmpClient,
            final long interval,final long initial, final Node node) {
        super(interval, initial);
        m_node = node;
        m_locationAwareSnmpClient=locationAwareSnmpClient;
    }


    public abstract void collect(); 
    /**
     * <p>
     * Performs the collection for the targeted IP address. The success or
     * failure of the collection should be tested via the <code>failed</code>
     * method.
     * </p>
     * <p>
     * No synchronization is performed, so if this is used in a separate
     * thread context synchronization must be added.
     * </p>
     */
    public void runDiscovery() {
            collect();
    }

    /**
     * Returns the target address that the collection occurred for.
     * 
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getPrimaryIpAddress() {
        return m_node.getSnmpPrimaryIpAddr();
    }

    public String getPrimaryIpAddressString() {
    	return str(m_node.getSnmpPrimaryIpAddr());
    }

    @Override
    public String getInfo() {
        return  getName() + " node:[" + getNodeId()
    		+ "] ip:" + str(getPrimaryIpAddress()) + super.getInfo();
    }

    public int getNodeId() {
    	return m_node.getNodeId();
    }
    
    public String getSysoid() {
        return m_node.getSysoid();
    }

    public String getSysname() {
        return m_node.getSysname();
    }

    public String getLocation() {
        return m_node.getLocation();
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((m_node == null) ? 0 : m_node.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        NodeCollector other = (NodeCollector) obj;
        if (m_node == null) {
            if (other.m_node != null)
                return false;
        } else if (!m_node.equals(other.m_node))
            return false;
        return true;
    }
    
    public SnmpAgentConfig getSnmpAgentConfig() {
        return SnmpPeerFactory.getInstance().getAgentConfig(m_node.getSnmpPrimaryIpAddr(), m_node.getLocation());
    }


    public LocationAwareSnmpClient getLocationAwareSnmpClient() {
        return m_locationAwareSnmpClient;
    }


    public Node getNode() {
        return m_node;
    }
    	
}
