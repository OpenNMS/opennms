//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.utils;

import java.util.Date;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.db.Constants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

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

    public EventBuilder(Event event) {
    	m_event = event;
	    Date date = new Date();
	    setTime(date);
	    setCreationTime(date);
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
    
    public EventBuilder setSeverity(String severity) {
    	m_event.setSeverity(Constants.getSeverityString(Constants.getSeverity(severity)));
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


    public EventBuilder setSnmpVersion(String version) {
    	ensureSnmp();
    	m_event.getSnmp().setVersion(version);
		return this;
	}

	private void ensureSnmp() {
		if (m_event.getSnmp() == null) {
			m_event.setSnmp(new Snmp());
		}
		
	}

	public EventBuilder setEnterpriseId(String enterprise) {
		ensureSnmp();
		m_event.getSnmp().setId(enterprise);
		return this;
	}

	public EventBuilder setGeneric(int generic) {
		ensureSnmp();
		m_event.getSnmp().setGeneric(generic);
		return this;
	}

	public EventBuilder setSpecific(int specific) {
		ensureSnmp();
		m_event.getSnmp().setSpecific(specific);
		return this;
	}

	public EventBuilder setSnmpHost(String snmpHost) {
		m_event.setSnmphost(snmpHost);
		return this;
		
	}

    public void setField(String name, String val) {
        BeanWrapper w = new BeanWrapperImpl(m_event);
        w.setPropertyValue(name, val);
    }
    
    private void ensureLogmsg() {
        if (m_event.getLogmsg() == null) {
            m_event.setLogmsg(new Logmsg());
        }
    }

    public void setLogDest(String dest) {
        ensureLogmsg();
        m_event.getLogmsg().setDest(dest);
    }

    public void setLogMessage(String content) {
        ensureLogmsg();
        m_event.getLogmsg().setContent(content);
    }

    public void setDescription(String descr) {
        m_event.setDescr(descr);
    }

}
