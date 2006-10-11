package org.opennms.netmgt.model;

public class OnmsMonitoringLocationDefinition {

    private String m_area;

    private String m_name;

    private String m_pollingPackageName;
    
    public OnmsMonitoringLocationDefinition() {
        
    }
    
    public OnmsMonitoringLocationDefinition(String name, String pollingPackageName) {
        m_name = name;
        m_pollingPackageName = pollingPackageName;
    }
    
    public OnmsMonitoringLocationDefinition(String name, String pollingPackageName, String area) {
        m_name = name;
        m_pollingPackageName = pollingPackageName;
        m_area = area;
    }

    public String getArea() {
        return m_area;
    }

    public void setArea(String area) {
        m_area = area;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public String getPollingPackageName() {
        return m_pollingPackageName;
    }

    public void setPollingPackageName(String pollingPackageName) {
        m_pollingPackageName = pollingPackageName;
    }
    
    @Override
    public String toString() {
        return "OnmsMonitoringLocationDefinition@" + Integer.toHexString(hashCode()) + ": Name \"" + m_name + "\", polling package name \"" + m_pollingPackageName + "\", area \"" + m_area + "\"";
    }
}
