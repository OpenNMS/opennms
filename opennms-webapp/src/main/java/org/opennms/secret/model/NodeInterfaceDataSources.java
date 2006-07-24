package org.opennms.secret.model;

import java.util.List;
import java.util.Set;

public class NodeInterfaceDataSources {
    NodeInterface m_nodeInterface;
    List m_dataSources;
    Set m_services;
    
    public List getDataSources() {
        return m_dataSources;
    }
    
    public void setDataSources(List dataSources) {
        m_dataSources = dataSources;
    }
    
    public Set getServices() {
        return m_services;
    }
    
    public void setServices(Set services) {
        m_services = services;
    }

    public NodeInterface getNodeInterface() {
        return m_nodeInterface;
    }

    public void setNodeInterface(NodeInterface nodeInterface) {
        m_nodeInterface = nodeInterface;
    }
}
