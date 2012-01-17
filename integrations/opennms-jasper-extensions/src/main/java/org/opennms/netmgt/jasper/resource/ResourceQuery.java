package org.opennms.netmgt.jasper.resource;

import java.io.File;

public class ResourceQuery {
    private String m_rrdDir;
    private String m_node;
    private String m_resourceName;
    private String[] m_filters;
    private String[] m_strProperties;

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
    
    public String constructBasePath() {
        return getRrdDir() + File.separator + getNodeId() + File.separator + getResourceName();
    }

    public String[] getStringProperties() {
        return m_strProperties;
    }
    
    public void setStringProperties(String[] strProperties) {
        m_strProperties = strProperties;
    }
}