package org.opennms.netmgt.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OnmsLocationAvailDefinition {
    
    private String m_availability;
    private String m_application;
    
    public OnmsLocationAvailDefinition() {}
    
    public OnmsLocationAvailDefinition(String applicationName, String calculatePercentageUptime) {
        setApplication(applicationName);
        setAvailability(calculatePercentageUptime);
    }

    public void setAvailability(String availability) {
        this.m_availability = availability;
    }
    
    @XmlElement(name="availability")
    public String getAvailability() {
        return m_availability;
    }

    public void setApplication(String applicationName) {
        m_application = applicationName;
    }
    
    @XmlElement(name="application")
    public String getApplication() {
        return m_application;
    }

    
}
