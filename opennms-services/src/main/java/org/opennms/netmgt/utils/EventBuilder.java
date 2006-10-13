package org.opennms.netmgt.utils;

import java.util.Date;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

public class EventBuilder {
    
    private Event m_event;

    public EventBuilder(String uei, String source) {
        m_event = new Event();
        m_event.setUei(uei);
        Date date = new Date();
        setTime(date);
        setCreationTime(date);
        setSource(source);
    }

    public Event getEvent() {
        return m_event;
    }
    
    public EventBuilder setTime(Date date) {
       m_event.setTime(EventConstants.formatToString(date));
       return this;
    }
    
    public EventBuilder setCreationTime(Date date) {
        m_event.setCreationTime(EventConstants.formatToString(date));
        return this;
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

    public EventBuilder addParam(String parmName, int val) {
        return addParam(parmName, Integer.toString(val));
    }

    public EventBuilder setNode(OnmsNode node) {
        m_event.setNodeid(node.getId().longValue());
        return this;
    }
    
    public EventBuilder setIpInterface(OnmsIpInterface iface) {
        m_event.setNodeid(iface.getNode().getId().longValue());
        m_event.setInterface(iface.getIpAddress());
        return this;
    }
    
    public EventBuilder setMonitoredService(OnmsMonitoredService monitoredService) {
        m_event.setNodeid(monitoredService.getNodeId().longValue());
        m_event.setInterface(monitoredService.getIpAddress());
        m_event.setService(monitoredService.getServiceName());
        return this;
    }

}
