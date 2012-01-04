package org.opennms.netmgt.jasper.resource;

public class ResourceQuery {
    public String m_rrdDir;
    public String m_node;
    public String m_resourceName;
    public String[] m_filters;

    public ResourceQuery() {
    }
    
    public String getRrdDir() {
        return m_rrdDir;
    }
    public void setRrdDir(String rrdDir) {
        m_rrdDir = rrdDir;
    }
    public String getNodeId() {
        return m_node;
    }
    public void setNodeId(String node) {
        m_node = node;
    }
    public String getResourceName() {
        return m_resourceName;
    }
    public void setResourceName(String resourceName) {
        m_resourceName = resourceName;
    }
    public String[] getFilters() {
        return m_filters;
    }
    public void setFilters(String[] filters) {
        m_filters = filters;
    }
}