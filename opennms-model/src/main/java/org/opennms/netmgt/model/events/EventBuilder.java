/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model.events;

import java.net.InetAddress;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.opennms.core.time.ZonedDateTimeBuilder;
import org.opennms.core.utils.StringUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Header;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Snmp;
import org.opennms.netmgt.xml.event.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyAccessorFactory;

/**
 * <p>EventBuilder class.</p>
 */
public class EventBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(EventBuilder.class);

    private Event m_event;

    private ZonedDateTimeBuilder zonedDateTimeBuilder = null;

    /**
     * <p>Constructor for EventBuilder.</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @param source a {@link java.lang.String} object.
     */
    public EventBuilder() {
        m_event = new Event();
    }

    /**
     * <p>Constructor for EventBuilder.</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @param source a {@link java.lang.String} object.
     */
    public EventBuilder(final String uei, final String source) {
        this(uei, source, new Date());
    }

    /**
     * <p>Constructor for EventBuilder.</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @param source a {@link java.lang.String} object.
     * @param date a {@link java.util.Date} object.
     */
    public EventBuilder(final String uei, final String source, final Date date) {
        m_event = new Event();
        setUei(uei);
        setTime(date);
        setSource(source);
        checkForIllegalUei();
    }

    /**
     * <p>Constructor for EventBuilder.</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public EventBuilder(final Event event) {
        m_event = event;
        Date now = new Date();
        setTime(now);
        checkForIllegalUei();
    }


    protected void checkForIllegalUei(){
        if(EventConstants.NODE_LABEL_CHANGED_EVENT_UEI.equals(this.m_event.getUei())){
            LOG.warn("The use of EventBuilder is deprecated for UEI="+EventConstants.NODE_LABEL_CHANGED_EVENT_UEI
                    +", use NodeLabelChangedEventBuilder instead");
        }
    }

    public Date currentEventTime() {
        if (m_event.getTime() == null && zonedDateTimeBuilder != null) {
            ZonedDateTime time = zonedDateTimeBuilder.build();
            return Date.from(time.toInstant());
        } else {
            return m_event.getTime();
        }
    }

    /**
     * <p>getEvent</p>
     *
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event getEvent() {
        if (m_event.getTime() == null && zonedDateTimeBuilder != null) {
            ZonedDateTime time = zonedDateTimeBuilder.build();
            m_event.setTime(Date.from(time.toInstant()));
        }

        if (m_event.getCreationTime() == null) {
            // The creation time has been used as the time when the event
            // is stored in the database so update it right before we return
            // the event object.
            m_event.setCreationTime(new Date());
        }
        return m_event;
    }

    public Log getLog() {
        Event event = getEvent();

        Events events = new Events();
        events.setEvent(new Event[]{event});

        Header header = new Header();
        header.setCreated(StringUtils.toStringEfficiently(event.getCreationTime()));

        Log log = new Log();
        log.setHeader(header);
        log.setEvents(events);
        return log;
    }

    public EventBuilder setUei(final String uei) {
        m_event.setUei(uei);
        checkForIllegalUei();
        return this;
    }


    /**
     * <p>setTime</p>
     *
     * @param date a {@link java.util.Date} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setTime(final Date date) {
       m_event.setTime(date);
       return this;
    }

    protected ZonedDateTimeBuilder getZonedDateTimeBuilder() {
        if (zonedDateTimeBuilder == null) {
            zonedDateTimeBuilder = new ZonedDateTimeBuilder();
        }
        return zonedDateTimeBuilder;
    }

    public EventBuilder setYear(final int value) {
        getZonedDateTimeBuilder().setYear(value);
        return this;
    }

    public EventBuilder setMonth(final int value) {
        // Note that java.time.Month values are 1-based
        // unlike java.util.Calendar.MONTH values which
        // are zero-based
        getZonedDateTimeBuilder().setMonth(value);
        return this;
    }

    public EventBuilder setDayOfMonth(final int value) {
        getZonedDateTimeBuilder().setDayOfMonth(value);
        return this;
    }

    public EventBuilder setHourOfDay(final int value) {
        getZonedDateTimeBuilder().setHourOfDay(value);
        return this;
    }

    public EventBuilder setMinute(final int value) {
        getZonedDateTimeBuilder().setMinute(value);
        return this;
    }

    public EventBuilder setSecond(final int value) {
        getZonedDateTimeBuilder().setSecond(value);
        return this;
    }

    public EventBuilder setMillisecond(final int value) {
        getZonedDateTimeBuilder().setNanosecond(value * 1000);
        return this;
    }

    public EventBuilder setZoneId(final ZoneId value) {
        getZonedDateTimeBuilder().setZoneId(value);
        return this;
    }

    /**
     * <p>setSource</p>
     *
     * @param source a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setSource(final String source) {
        m_event.setSource(source);
        return this;
        
    }
    
    /**
     * <p>setSeverity</p>
     *
     * @param severity a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setSeverity(final String severity) {
    	m_event.setSeverity(OnmsSeverity.get(severity).getLabel());
    	return this;
    }

    /**
     * <p>setNodeid</p>
     *
     * @param nodeid a long.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setNodeid(long nodeid) {
        m_event.setNodeid(nodeid);
        return this;
    }

    /**
     * <p>setHost</p>
     *
     * @param hostname a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setHost(final String hostname) {
        m_event.setHost(hostname);
        return this;
    }
    
    /**
     * <p>setInterface</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setInterface(final InetAddress ipAddress) {
        if (ipAddress != null) m_event.setInterfaceAddress(ipAddress);
        return this;
    }
    /**
     * <p>setInterface</p>
     *
     * @param ipAddress a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setIfIndex(final int ifIndex) {
        m_event.setIfIndex(ifIndex);
        return this;
    }

    /**
     * <p>setService</p>
     *
     * @param serviceName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setService(final String serviceName) {
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
    public EventBuilder addParam(final String parmName, final String val) {
        return addParam(parmName, val, null, null);
    }
    
    public EventBuilder addParam(final String parmName, final String val, final String type, final String encoding) {
        if (parmName != null) {
            Value value = new Value();
            value.setContent(val);
            
            if (type != null) {
                value.setType(type);
            }
            
            if (encoding != null) {
                value.setEncoding(encoding);
            }
            
            Parm parm = new Parm();
            parm.setParmName(parmName);
            parm.setValue(value);
            
            addParam(parm);
        }
        
        return this;
    }

    
    public EventBuilder addParam(final Parm parm) {
        m_event.addParm(parm);

        return this;
    }
    
    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param val a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(final String parmName, final boolean val) {
        if (parmName != null) {
            final Value value = new Value();
            value.setContent(val ? "true" : "false");

            final Parm parm = new Parm();
            parm.setParmName(parmName);
            parm.setValue(value);

            this.addParam(parm);
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
    public EventBuilder setParam(final String parmName, final String val) {
        if (m_event.getParmCollection().size() < 1) {
            return addParam(parmName, val);
        }

        for(final Parm parm : m_event.getParmCollection()) {
            if (parm.getParmName().equals(parmName)) {
            	final Value value = new Value();
                value.setContent(val);
                parm.setValue(value);
                return this;
            }
        }

        return addParam(parmName, val);
    }

    /**
     * <p>setParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param val a int.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setParam(final String parmName, final int val) {
        return setParam(parmName, Integer.toString(val));
    }

    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param val a long.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(final String parmName, final double val) {
        return addParam(parmName, Double.toString(val));
    }
    
    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param val a long.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(final String parmName, final long val) {
        return addParam(parmName, Long.toString(val));
    }
    
    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param val a int.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(final String parmName, final int val) {
        return addParam(parmName, Integer.toString(val));
    }
    
    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param ch a char.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(final String parmName, final char ch) {
        return addParam(parmName, Character.toString(ch));
    }
    
    /**
     * <p>addParam</p>
     *
     * @param parmName a {@link java.lang.String} object.
     * @param vals a {@link java.util.Collection} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder addParam(final String parmName, final Collection<String> vals) {
        final String val = org.springframework.util.StringUtils.collectionToCommaDelimitedString(vals);
        return addParam(parmName, val);
        
    }

    /**
     * <p>setAlarmData</p>
     *
     * @param alarmData a {@link org.opennms.netmgt.xml.event.AlarmData} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setAlarmData(final AlarmData alarmData) {
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
    public EventBuilder setNode(final OnmsNode node) {
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
    public EventBuilder setIpInterface(final OnmsIpInterface iface) {
        if (iface != null) {
            if (iface.getNode() != null) {
                m_event.setNodeid(iface.getNode().getId().longValue());
            }
            m_event.setInterfaceAddress(iface.getIpAddress());
        }
        return this;
    }
    
    /**
     * <p>setMonitoredService</p>
     *
     * @param monitoredService a {@link org.opennms.netmgt.model.OnmsMonitoredService} object.
     * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
     */
    public EventBuilder setMonitoredService(final OnmsMonitoredService monitoredService) {
        if (monitoredService != null) {
            m_event.setNodeid(monitoredService.getNodeId().longValue());
            m_event.setInterfaceAddress(monitoredService.getIpAddress());
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
    public EventBuilder setSnmpVersion(final String version) {
    	ensureSnmp();
    	m_event.getSnmp().setVersion(version);
		return this;
	}

	private void ensureSnmp() {
		if (m_event.getSnmp() == null) {
			m_event.setSnmp(new Snmp());
		}
		
	}
	
	public EventBuilder setCommunity(final String community) {
	    ensureSnmp();
	    m_event.getSnmp().setCommunity(community);
	    return this;
	}

	/**
	 * <p>setEnterpriseId</p>
	 *
	 * @param enterprise a {@link java.lang.String} object.
	 * @return a {@link org.opennms.netmgt.model.events.EventBuilder} object.
	 */
	public EventBuilder setEnterpriseId(final String enterprise) {
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
	public EventBuilder setGeneric(final int generic) {
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
	public EventBuilder setSpecific(final int specific) {
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
	public EventBuilder setSnmpHost(final String snmpHost) {
		m_event.setSnmphost(snmpHost);
		return this;
		
	}
	
    public EventBuilder setSnmpTimeStamp(final long timeStamp) {
        ensureSnmp();
        m_event.getSnmp().setTimeStamp(timeStamp);
        return this;
    }



    /**
     * <p>setField</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param val a {@link java.lang.String} object.
     */
    public void setField(final String name, final String val) {
        if (name.equals("eventparms")) {
            String[] parts = val.split(";");
            for (String part : parts) {
                String[] pair = part.split("=");
                addParam(pair[0], pair[1].replaceFirst("[(]\\w+,\\w+[)]", ""));
            }
        } else {
            final BeanWrapper w = PropertyAccessorFactory.forBeanPropertyAccess(m_event);
            try {
                w.setPropertyValue(name, val);
            } catch (final BeansException e) {
                LOG.warn("Could not set field on event: {}", name, e);
            }
        }
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
    public EventBuilder setLogDest(final String dest) {
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
    public EventBuilder setLogMessage(final String content) {
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
    public EventBuilder setDescription(final String descr) {
        m_event.setDescr(descr);
        return this;
    }

    /**
     * <p>setParms</p>
     *
     * @param parms a list of parameters.
     * @return the event builder
     */
    public EventBuilder setParms(final List<Parm> parms) {
        m_event.setParmCollection(parms);
        return this;
    }

	public EventBuilder setUuid(final String uuid) {
		m_event.setUuid(uuid);
		return this;
	}

	public EventBuilder setDistPoller(final String distPoller) {
		m_event.setDistPoller(distPoller);
		return this;
	}

	public EventBuilder setMasterStation(final String masterStation) {
		m_event.setMasterStation(masterStation);
		return this;
	}



}
