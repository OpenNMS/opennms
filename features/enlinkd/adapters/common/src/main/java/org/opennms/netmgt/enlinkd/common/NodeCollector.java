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
package org.opennms.netmgt.enlinkd.common;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.scheduler.Executable;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public abstract class NodeCollector extends Executable {
    /**
     * The node ID of the system used to collect the SNMP information
     */
    protected final Node m_node;
    private final LocationAwareSnmpClient m_loLocationAwareSnmpClient;

    /**
     * Constructs a new SNMP collector for a node using the passed interface
     * as the collection point. The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
     */
    public NodeCollector(final LocationAwareSnmpClient snmpClient, final Node node, final int priority) {
        super(priority);
        m_node = node;
        m_loLocationAwareSnmpClient = snmpClient;
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
    public void runExecutable() {
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
            return other.m_node == null;
        } else return m_node.equals(other.m_node);
    }
    
    public SnmpAgentConfig getSnmpAgentConfig() {
        return SnmpPeerFactory.getInstance().getAgentConfig(m_node.getSnmpPrimaryIpAddr(), m_node.getLocation());
    }

    public LocationAwareSnmpClient getLocationAwareSnmpClient() {
        return m_loLocationAwareSnmpClient;
    }

    public Node getNode() {
        return m_node;
    }

    @Override
    public boolean isReady() {
        return true;
    }
    	
}
