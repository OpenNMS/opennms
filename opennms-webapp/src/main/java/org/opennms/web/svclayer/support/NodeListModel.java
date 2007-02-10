package org.opennms.web.svclayer.support;

import java.util.List;

import org.opennms.web.element.Interface;
import org.opennms.web.element.Node;

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
        private Node m_node;
        private List<Interface> m_interfaces;
        
        public NodeModel(Node node, List<Interface> interfaces) {
            m_node = node;
            m_interfaces = interfaces;
        }
        
        public Node getNode() {
            return m_node;
        }
        
        public List<Interface> getInterfaces() {
            return m_interfaces;
        }
    }

}
