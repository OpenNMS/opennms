/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007-2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2009 Aug 28: Restore search and display capabilities for non-ip interfaces
 * Created: February 9, 2007
 *
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.web.svclayer.support;

import java.util.List;

import org.opennms.netmgt.model.OnmsArpInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;

/**
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class NodeListModel {
    private List<NodeModel> m_nodes;
    private int m_interfaceCount;
    
    public NodeListModel(List<NodeModel> nodes, int interfaceCount) {
        m_nodes = nodes;
        m_interfaceCount = interfaceCount;
    }
    
    public List<NodeModel> getNodes() {
        return m_nodes;
    }

    public List<NodeModel> getNodesLeft() {
        return m_nodes.subList(0, getLastInLeftColumn());
    }
    
    public List<NodeModel> getNodesRight() {
        return m_nodes.subList(getLastInLeftColumn(), m_nodes.size());
    }

    public int getLastInLeftColumn() {
        return (int) Math.ceil(m_nodes.size()/2.0);
    }
    
    public int getNodeCount() {
        return m_nodes.size();
    }
    
    public int getInterfaceCount() {
        return m_interfaceCount;
    }
    
    public static class NodeModel {
        private OnmsNode m_node;
        private List<OnmsIpInterface> m_interfaces;
        private List<OnmsArpInterface> m_arpinterfaces;
        private List<OnmsSnmpInterface> m_snmpinterfaces;
        
        
        public NodeModel(OnmsNode node, List<OnmsIpInterface> interfaces, List<OnmsArpInterface> arpinterfaces, List<OnmsSnmpInterface> snmpinterfaces) {
            m_node = node;
            m_interfaces = interfaces;
            m_arpinterfaces = arpinterfaces;
            m_snmpinterfaces = snmpinterfaces;
        }
        
        public OnmsNode getNode() {
            return m_node;
        }
        
        public List<OnmsIpInterface> getInterfaces() {
            return m_interfaces;
        }
        
        public List<OnmsArpInterface> getArpInterfaces() {
            return m_arpinterfaces;
        }
        
        public List<OnmsSnmpInterface> getSnmpInterfaces() {
            return m_snmpinterfaces;
        }
    }

}
