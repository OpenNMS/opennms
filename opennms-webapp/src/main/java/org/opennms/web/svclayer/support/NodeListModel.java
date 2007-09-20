package org.opennms.web.svclayer.support;

import java.util.List;

import org.opennms.netmgt.model.OnmsArpInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

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
        
        public NodeModel(OnmsNode node, List<OnmsIpInterface> interfaces, List<OnmsArpInterface> arpinterfaces) {
            m_node = node;
            m_interfaces = interfaces;
            m_arpinterfaces = arpinterfaces;
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
    }

}
