package org.opennms.netmgt.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OnmsLocationAvailDataPoint {
    
    private Date m_time;
    private List<OnmsLocationAvailDefinition> m_definitions = new ArrayList<OnmsLocationAvailDefinition>();
    
    public void setTime(Date time) {
        m_time = time;
    }
    
    @XmlElement(name="time")
    public long getTime() {
        return m_time.getTime();
    }
    
    public void addAvailDefinition(OnmsLocationAvailDefinition definition) {
        m_definitions.add(definition);
    }
    
    @XmlElement(name="values")
    public List<OnmsLocationAvailDefinition> getDefininitions(){
        return m_definitions;
    }

}
