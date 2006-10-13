package org.opennms.netmgt.utils;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

public class EventBuilder {
    
    private Event m_event;

    public EventBuilder(String uei) {
        m_event = new Event();
        m_event.setUei(uei);
    }

    public Event getEvent() {
        return m_event;
    }

    public EventBuilder setSource(String source) {
        m_event.setSource(source);
        return this;
        
    }

    public EventBuilder setNodeid(int nodeid) {
        m_event.setNodeid(nodeid);
        return this;
    }

    public EventBuilder setInterface(String ipAddress) {
        m_event.setInterface(ipAddress);
        return this;
    }
    
    public EventBuilder setService(String serviceName) {
        m_event.setService(serviceName);
        return this;
    }

    public EventBuilder addParam(String parmName, String val) {
        Value value = new Value();
        value.setContent(val);
        
        Parm parm = new Parm();
        parm.setParmName(parmName);
        parm.setValue(value);
        
        if (m_event.getParms() == null) {
            Parms parms = new Parms();
            m_event.setParms(parms);
        }
        
        m_event.getParms().addParm(parm);
        
        return this;
    }

}
