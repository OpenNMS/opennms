/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.eventd.processor;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.util.AutoAction;
import org.opennms.netmgt.dao.util.OperatorAction;
import org.opennms.netmgt.dao.util.SnmpInfo;
import org.opennms.netmgt.eventd.EventdConstants;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.events.Constants;
import org.opennms.netmgt.model.events.EventProcessor;
import org.opennms.netmgt.model.events.EventProcessorException;
import org.opennms.netmgt.model.events.Parameter;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;
import org.opennms.netmgt.xml.event.Operaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * EventWriter loads the information in each 'Event' into the database.
 *
 * While loading multiple values of the same element into a single DB column, the
 * multiple values are delimited by MULTIPLE_VAL_DELIM.
 *
 * When an element and its attribute are loaded into a single DB column, the
 * value and the attribute are separated by a DB_ATTRIB_DELIM.
 *
 * When using delimiters to append values, if the values already have the
 * delimiter, the delimiter in the value is escaped as in URLs.
 *
 * Values for the ' <parms>' block are loaded with each parm name and parm value
 * delimited with the NAME_VAL_DELIM.
 * 
 * @see org.opennms.netmgt.model.events.Constants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.model.events.Constants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.model.events.Constants#NAME_VAL_DELIM
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public final class HibernateEventWriter extends AbstractJdbcPersister implements EventProcessor, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateEventWriter.class);
    
    @Autowired
    private IpInterfaceDao ipInterfaceDao;
    
    @Autowired
    private MonitoredServiceDao monitoredServiceDao;
    
    @Autowired
    private DistPollerDao distPollerDao;
    
    @Autowired
    private EventDao eventDao;
    /**
     * {@inheritDoc}
     *
     * The method that inserts the event into the database
     */
    @Override
    public void process(final Header eventHeader, final Event event) throws EventProcessorException {
        if (!checkEventSanityAndDoWeProcess(event, "HibernateEventWriter")) {
            return;
        }

        LOG.debug("HibernateEventWriter: processing {} nodeid: {} ipaddr: {} serviceid: {} time: {}", event.getUei(), event.getNodeid(), event.getInterface(), event.getService(), event.getTime());

       insertEvent(eventHeader, event, null);
	
}

    /**
     * Insert values into the EVENTS table
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error adding the event to the
     *                database.
     * @exception java.lang.NullPointerException
     *                Thrown if a required resource cannot be found in the
     *                properties file.
     */
    private void insertEvent(final Header eventHeader, final Event event, final Connection connection) {
        OnmsDistPoller poll = distPollerDao.get(event.getDistPoller());
        OnmsServiceType serve = new OnmsServiceType(event.getService());
        
        
        OnmsEvent ovent = new OnmsEvent();
        ovent.setId(event.getDbid());
        ovent.setEventUei(event.getUei());
        ovent.setEventTime(Date.valueOf(event.getTime()));
        ovent.setEventHost(event.getHost());
        ovent.setEventSource(event.getSource());
        ovent.setIpAddr(event.getInterfaceAddress());
        ovent.setDistPoller(poll);
        ovent.setEventSnmpHost(event.getSnmphost());
        ovent.setServiceType(serve);
        ovent.setEventSnmp(SnmpInfo.format(event.getSnmp(), EVENT_SNMP_FIELD_SIZE));
        final String parms = Parameter.format(event);
        ovent.setEventParms(parms);
        ovent.setEventCreateTime(Date.valueOf(event.getCreationTime()));
        ovent.setEventDescr(event.getDescr());
        ovent.setEventLogGroup(Integer.toString(event.getLoggroupCount()));
        
        ovent.setEventSeverity(Integer.valueOf(event.getSeverity()));
        ovent.setEventPathOutage(event.getPathoutage());
        ovent.setEventCorrelation((event.getCorrelation() != null) ? org.opennms.netmgt.dao.util.Correlation.format(event.getCorrelation(), EVENT_CORRELATION_FIELD_SIZE) : null);
        ovent.setEventSuppressedCount(null);
        ovent.setEventOperInstruct(event.getOperinstruct());
        ovent.setEventAutoAction((event.getAutoactionCount() > 0) ? AutoAction.format(event.getAutoaction(), EVENT_AUTOACTION_FIELD_SIZE) : null);
  
        if (event.getOperactionCount() > 0) {
        	final List<Operaction> a = new ArrayList<Operaction>();
        	final List<String> b = new ArrayList<String>();
        	
        	for (final Operaction eoa : event.getOperactionCollection()) {
        		a.add(eoa);
        		b.add(eoa.getMenutext());
        	}
        	ovent.setEventOperAction(OperatorAction.format(a, EVENT_OPERACTION_FIELD_SIZE));
        	ovent.setEventOperActionMenuText(Constants.format(b, EVENT_OPERACTION_FIELD_SIZE));
        } else {
        	ovent.setEventOperAction(null);
        	ovent.setEventOperActionMenuText(null);
        }
        ovent.setEventNotification(null);
        if (event.getTticket() != null) {
        	ovent.setEventTTicket(event.getTticket().getContent());
        	ovent.setEventTTicketState(Integer.valueOf(event.getTticket().getState()));
        } else {
        	ovent.setEventTTicket(null);
        	ovent.setEventTTicketState(null);
        }
        ovent.setEventForward(org.opennms.netmgt.dao.util.Forward.format(event.getForward(), EVENT_FORWARD_FIELD_SIZE));
        ovent.setEventMouseOverText(event.getMouseovertext());
        
        if (event.getLogmsg() != null) {
            // set log message
        	ovent.setEventLogMsg(event.getLogmsg().getContent());
            String logdest = event.getLogmsg().getDest();
            if (logdest.equals("logndisplay")) {
                // if 'logndisplay' set both log and display column to yes
                ovent.setEventLog("" + MSG_YES + "");
                ovent.setEventDisplay("" + MSG_YES + "");
            } else if (logdest.equals("logonly")) {
                // if 'logonly' set log column to true
            	ovent.setEventLog("" + MSG_YES + "");
                ovent.setEventDisplay("" + MSG_NO + "");
            } else if (logdest.equals("displayonly")) {
                // if 'displayonly' set display column to true
            	ovent.setEventLog("" + MSG_NO + "");
                ovent.setEventDisplay("" + MSG_YES + "");
            } else if (logdest.equals("suppress")) {
                // if 'suppress' set both log and display to false
            	ovent.setEventLog("" + MSG_NO + "");
                ovent.setEventDisplay("" + MSG_NO + "");
            }
        } else {
            ovent.setEventLogMsg(null);
            ovent.setEventLog("" + MSG_YES + "");
            ovent.setEventDisplay("" + MSG_YES + "");
        }
        
        if (event.getAutoacknowledge() != null && event.getAutoacknowledge().getState().equals("on")) {
            ovent.setEventAckUser(event.getAutoacknowledge().getContent());
            // eventAckTime - if autoacknowledge is present,
            // set time to event create time
            ovent.setEventAckTime(Date.valueOf(event.getCreationTime()));
        } else {
            ovent.setEventAckUser(null);
            ovent.setEventAckTime(null);
        }
        
        eventDao.update(ovent);
    }


    /**
     * This method is used to convert the event host into a hostname id by
     * performing a lookup in the database. If the conversion is successful then
     * the corresponding hosname will be returned to the caller.
     * @param nodeId 
     * @param hostip
     *            The event host
     * 
     * @return The hostname
     * 
     * @exception java.sql.SQLException
     *                Thrown if there is an error accessing the stored data or
     *                the SQL text is malformed.
     * 
     * @see EventdConstants#SQL_DB_HOSTIP_TO_HOSTNAME
     * 
     */
    
    String getHostName(final int nodeId, final String hostip) throws SQLException {
    	
    	OnmsIpInterface ints = ipInterfaceDao.findByNodeIdAndIpAddress(nodeId, hostip);
        
        final String hostname = ints.getIpHostName();
        return (hostname != null) ? hostname : hostip;
        
    }

    /**
     * @param event
     * @param log
     * @return
     */
    private int getEventServiceId(final Event event) {
    	if (event.getService() == null) {
            return -1;
        } else {
        	return Integer.valueOf(event.getService());
        }
    }

    /**
     * <p>getEventHost</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param connection a {@link java.sql.Connection} object.
     * @return a {@link java.lang.String} object.
     */
    protected String getEventHost(final Event event) {
        if (event.getHost() == null) {
            return null;
        }
        
        // If the event doesn't have a node ID, we can't lookup the IP address and be sure we have the right one since we don't know what node it is on
        if (!event.hasNodeid()) {
            return event.getHost();
        }
        
        try {
            return getHostName(event.getNodeid().intValue(), event.getHost());
        } catch (final Throwable t) {
            LOG.warn("Error converting host IP \"{}\" to a hostname, storing the IP.", event.getHost(), t);
            return event.getHost();
        }
    }
}
