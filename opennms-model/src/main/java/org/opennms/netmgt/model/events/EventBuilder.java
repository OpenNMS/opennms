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
package org.opennms.netmgt.model.events;

import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.util.StringUtils;

/**
 * <p>EventBuilder class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class EventBuilder {
    
    private Event m_event;
    
    /**
     * <p>Constructor for EventBuilder.</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @param source a {@link java.lang.String} object.
     */
    public EventBuilder(String uei, String source) {
        this(uei, source, new Date());
    }

    /**
     * <p>Constructor for EventBuilder.</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @param source a {@link java.lang.String} object.
     * @param date a {@link java.util.Date} object.
     */
    public EventBuilder(String uei, String source, Date date) {
        m_event = new Event();
        m_event.setUei(uei);
        setTime(date);
        setCreationTime(date);
        setSource(source);
    }
    
    /**
     * <p>Constructor for EventBuilder.</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public EventBuilder(Event event) {
        this(event, new Date());
    }

    /**
     * <p>Constructor for EventBuilder.</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param source a {@link java.lang.String} object.
     */
    public EventBuilder(Event event, String source) {
        this(event);
        setSource(source);
    }

    /**
     * <p>Constructor for EventBuilder.</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param date a {@link java.util.Date} object.
     */
    public EventBuilder(Event event, Date date) {
    	m_event = event;
    	setSource(event.getSource());
	    setTime(date);
	    setCreationTime(date);
	}

	/**
	 * <p>getEvent</p>
	 *
	 * @return a {@link org.opennms.netmgt.xml.event.Event} object.
	 */
	public Event getEvent() {
        return m_event;
    }

    /**
     * <p>setTime</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setTime(Date date) {
       m_event.setTime(EventConstants.formatToString(date));
       return this;
    }
    
    /**
     * <p>setCreationTime</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setCreationTime(Date date) {
        m_event.setCreationTime(EventConstants.formatToString(date));
        return this;
    }

    /**
     * <p>setSource</p>
     *
     * @param source a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setSource(String source) {
        m_event.setSource(source);
        return this;
        
    }
    
    /**
     * <p>setSeverity</p>
     *
     * @param severity a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setSeverity(String severity) {
    	m_event.setSeverity(EventConstants.getSeverityString(EventConstants.getSeverity(severity)));
    	return this;
    }

    /**
     * <p>setNodeid</p>
     *
     * @param nodeid a int.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setNodeid(int nodeid) {
        m_event.setNodeid(nodeid);
        return this;
    }

    /**
     * <p>setHost</p>
     *
     * @param hostname a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setHost(String hostname) {
        m_event.setHost(hostname);
        return this;
    }
    
    /**
     * <p>setInterface</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setInterface(String ipAddress) {
        m_event.setInterface(ipAddress);
        return this;
    }
    
    /**
     * <p>setService</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setService(String serviceName) {
        m_event.setService(serviceName);
        return this;
    }

    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param val a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(String parmName, String val) {
        if (parmName != null) {
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
        }
        
        return this;
    }
    
    /**
     * <p>setParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param val a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setParam(String parmName, String val) {
        Parms parms = m_event.getParms();
        if (parms == null) {
            return addParam(parmName, val);
        }

        for(Parm parm : parms.getParmCollection()) {
            if (parm.getParmName().equals(val)) {
                Value value = new Value();
                value.setContent(val);
                parm.setValue(value);
                return this;
            }
        }

        return addParam(parmName, val);
    }

    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param val a long.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(String parmName, long val) {
        return addParam(parmName, Long.toString(val));
    }
    
    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param val a int.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(String parmName, int val) {
        return addParam(parmName, Integer.toString(val));
    }
    
    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param ch a char.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(String parmName, char ch) {
        return addParam(parmName, Character.toString(ch));
    }
    
    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param vals a {@link java.util.Collection} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(String parmName, Collection<String> vals) {
        String val = StringUtils.collectionToCommaDelimitedString(vals);
        return addParam(parmName, val);
        
    }

    /**
     * <p>setAlarmData</p>
     *
     * @param alarmData a {@link org.opennms.netmgt.xml.event.AlarmData} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setAlarmData(AlarmData alarmData) {
        if (alarmData != null) {
            m_event.setAlarmData(alarmData);
        }
        return this;
    }
    
    /**
     * <p>setNode</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setNode(OnmsNode node) {
        if (node != null) {
            m_event.setNodeid(node.getId().longValue());
        }
        return this;
    }
    
    /**
     * <p>setIpInterface</p>
     *
     * @param iface a {@link org.opennms.netmgt.model.OnmsIpInterface} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setIpInterface(OnmsIpInterface iface) {
        if (iface != null) {
            if (iface.getNode() != null) {
                m_event.setNodeid(iface.getNode().getId().longValue());
            }
            m_event.setInterface(iface.getIpAddress());
        }
        return this;
    }
    
    /**
     * <p>setMonitoredService</p>
     *
     * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setMonitoredService(OnmsMonitoredService monitoredService) {
        if (monitoredService != null) {
            m_event.setNodeid(monitoredService.getNodeId().longValue());
            m_event.setInterface(monitoredService.getIpAddress());
            m_event.setService(monitoredService.getServiceName());
        }
        return this;
    }


    /**
     * <p>setSnmpVersion</p>
     *
     * @param version a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
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

	/**
	 * <p>setEnterpriseId</p>
	 *
	 * @param enterprise a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
	 */
	public EventBuilder setEnterpriseId(String enterprise) {
		ensureSnmp();
		m_event.getSnmp().setId(enterprise);
		return this;
	}

	/**
	 * <p>setGeneric</p>
	 *
	 * @param generic a int.
	 * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
	 */
	public EventBuilder setGeneric(int generic) {
		ensureSnmp();
		m_event.getSnmp().setGeneric(generic);
		return this;
	}

	/**
	 * <p>setSpecific</p>
	 *
	 * @param specific a int.
	 * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
	 */
	public EventBuilder setSpecific(int specific) {
		ensureSnmp();
		m_event.getSnmp().setSpecific(specific);
		return this;
	}

	/**
	 * <p>setSnmpHost</p>
	 *
	 * @param snmpHost a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
	 */
	public EventBuilder setSnmpHost(String snmpHost) {
		m_event.setSnmphost(snmpHost);
		return this;
		
	}

    /**
     * <p>setField</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param val a {@link java.lang.String} object.
     */
    public void setField(String name, String val) {
        BeanWrapper w = new BeanWrapperImpl(m_event);
        w.setPropertyValue(name, val);
    }
    
    private void ensureLogmsg() {
        if (m_event.getLogmsg() == null) {
            m_event.setLogmsg(new Logmsg());
        }
    }

    /**
     * <p>setLogDest</p>
     *
     * @param dest a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setLogDest(String dest) {
        ensureLogmsg();
        m_event.getLogmsg().setDest(dest);
        return this;
    }

    /**
     * <p>setLogMessage</p>
     *
     * @param content a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setLogMessage(String content) {
        ensureLogmsg();
        m_event.getLogmsg().setContent(content);
        return this;
    }

    /**
     * <p>setDescription</p>
     *
     * @param descr a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setDescription(String descr) {
        m_event.setDescr(descr);
        return this;
    }

    /**
     * <p>addParms</p>
     *
     * @param parms a {@link org.opennms.netmgt.xml.event.Parms} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParms(final Parms parms) {
        m_event.setParms(parms);
        return this;
    }

}
