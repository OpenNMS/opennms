package org.opennms.secret.model;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class NodeDataSources {
    private Node m_node;
    private List m_dataSources;
    private Set m_interfaces;
    
    public Set getInterfaces() {
        return m_interfaces;
    }
    public void setInterfaces(Set interfaces) {
        m_interfaces = interfaces;
    }
    public Node getNode() {
        return m_node;
    }
    public void setNode(Node node) {
        m_node = node;
    }
    public List getDataSources() {
        return m_dataSources;
    }
    public void setDataSources(List dataSources) {
        m_dataSources = dataSources;
    }

    
}
