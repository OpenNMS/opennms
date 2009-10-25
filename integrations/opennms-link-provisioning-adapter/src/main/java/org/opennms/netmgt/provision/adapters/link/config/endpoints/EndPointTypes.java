package org.opennms.netmgt.provision.adapters.link.config.endpoints;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="end-point-types")
public class EndPointTypes {
    List<EndPointType> m_endPointTypes = Collections.synchronizedList(new ArrayList<EndPointType>());

    public void addEndPointType(EndPointType type) {
        m_endPointTypes.add(type);
    }
    
    public List<EndPointType> getEndPointTypes() {
        return m_endPointTypes;
    }
    
    public void setEndPointTypes(List<EndPointType> types) {
        synchronized(m_endPointTypes) {
            m_endPointTypes.clear();
            m_endPointTypes.addAll(types);
        }
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("end-point-types", m_endPointTypes)
            .toString();
    }
}
