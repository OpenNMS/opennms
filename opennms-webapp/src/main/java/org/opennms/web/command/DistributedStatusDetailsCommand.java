package org.opennms.web.command;

public class DistributedStatusDetailsCommand {
    private String m_location;
    private String m_application;
    
    public String getApplication() {
        return m_application;
    }
    public void setApplication(String application) {
        m_application = application;
    }
    public String getLocation() {
        return m_location;
    }
    public void setLocation(String location) {
        m_location = location;
    }
    
}
