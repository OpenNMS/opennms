package org.opennms.netmgt.model;

public class OnmsMonitoringLocationDefinition {

    private String m_area;

    private String m_name;

    private String m_pollingPackageName;

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
}
