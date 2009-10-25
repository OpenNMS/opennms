package org.opennms.netmgt.provision.adapters.link.config.endpoints;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name="end-point-type")
@XmlType(propOrder={"name", "deviceType", "criteria"})
public class EndPointType {
    private String m_name;

    @XmlAttribute(name="name")
    public String getName() {
        return m_name;
    }
    
    public void setName(String name) {
        m_name = name;
    }
}
