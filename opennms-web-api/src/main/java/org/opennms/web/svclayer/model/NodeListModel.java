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
package org.opennms.web.svclayer.model;

import java.util.List;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

/**
 * <p>NodeListModel class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 * @since 1.8.1
 */
public class NodeListModel {
    private List<NodeModel> m_nodes;
    private int m_interfaceCount;
    
    /**
     * <p>Constructor for NodeListModel.</p>
     *
     * @param nodes a {@link java.util.List} object.
     * @param interfaceCount a int.
     */
    public NodeListModel(List<NodeModel> nodes, int interfaceCount) {
        m_nodes = nodes;
        m_interfaceCount = interfaceCount;
    }
    
    /**
     * <p>getNodes</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<NodeModel> getNodes() {
        return m_nodes;
    }

    /**
     * <p>getNodesLeft</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<NodeModel> getNodesLeft() {
        return m_nodes.subList(0, getLastInLeftColumn());
    }
    
    /**
     * <p>getNodesRight</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<NodeModel> getNodesRight() {
        return m_nodes.subList(getLastInLeftColumn(), m_nodes.size());
    }

    /**
     * <p>getLastInLeftColumn</p>
     *
     * @return a int.
     */
    public int getLastInLeftColumn() {
        return (int) Math.ceil(m_nodes.size()/2.0);
    }
    
    /**
     * <p>getNodeCount</p>
     *
     * @return a int.
     */
    public int getNodeCount() {
        return m_nodes.size();
    }
    
    /**
     * <p>getInterfaceCount</p>
     *
     * @return a int.
     */
    public int getInterfaceCount() {
        return m_interfaceCount;
    }
    
    public static class NodeModel {
        private OnmsNode m_node;
        private List<OnmsIpInterface> m_interfaces;
        private List<OnmsSnmpInterface> m_snmpinterfaces;
        
        
        public NodeModel(OnmsNode node, List<OnmsIpInterface> interfaces, List<OnmsSnmpInterface> snmpinterfaces) {
            m_node = node;
            m_interfaces = interfaces;
            m_snmpinterfaces = snmpinterfaces;
        }
        
        public OnmsNode getNode() {
            return m_node;
        }
        
        public List<OnmsIpInterface> getInterfaces() {
            return m_interfaces;
        }
        
        public List<OnmsSnmpInterface> getSnmpInterfaces() {
            return m_snmpinterfaces;
        }
    }

}
