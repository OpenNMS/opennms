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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.MonitoredServiceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.util.AutoAction;
import org.opennms.netmgt.dao.util.Correlation;
import org.opennms.netmgt.dao.util.Forward;
import org.opennms.netmgt.dao.util.OperatorAction;
import org.opennms.netmgt.dao.util.SnmpInfo;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.events.api.EventDatabaseConstants;
import org.opennms.netmgt.events.api.EventParameterUtils;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;
import org.opennms.netmgt.xml.event.Operaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

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
public final class HibernateEventWriter implements EventWriter {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateEventWriter.class);
    
    @Autowired
    private NodeDao nodeDao;
    
    @Autowired
    private IpInterfaceDao ipInterfaceDao;
    
    @Autowired
    private MonitoredServiceDao monitoredServiceDao;
    
    @Autowired
    private DistPollerDao distPollerDao;
    
    @Autowired
    private EventDao eventDao;

    @Autowired
    private ServiceTypeDao serviceTypeDao;

    @Autowired
    private EventUtil eventUtil;

    /**
     * <p>checkEventSanityAndDoWeProcess</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param logPrefix a {@link java.lang.String} object.
     * @return a boolean.
     */
    private static boolean checkEventSanityAndDoWeProcess(Event event, String logPrefix) {
        Assert.notNull(event, "event argument must not be null");

        /*
         * Check value of <logmsg> attribute 'dest', if set to
         * "donotpersist" or "suppress" then simply return, the UEI is not to be
         * persisted to the database
         */
        Assert.notNull(event.getLogmsg(), "event does not have a logmsg");
        if (
            "donotpersist".equals(event.getLogmsg().getDest()) || 
            "suppress".equals(event.getLogmsg().getDest())
        ) {
            LOG.debug("{}: uei '{}' marked as '{}'; not processing event.", logPrefix, event.getUei(), event.getLogmsg().getDest());
            return false;
        }
        return true;
    }

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

        insertEvent(eventHeader, event);
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
    private void insertEvent(final Header eventHeader, final Event event) {

        OnmsEvent ovent = new OnmsEvent();

        // eventID
        //ovent.setId(event.getDbid());

        // eventUEI
        ovent.setEventUei(EventDatabaseConstants.format(event.getUei(), EVENT_UEI_FIELD_SIZE));

        // nodeID
        if (event.hasNodeid()) {
            ovent.setNode(nodeDao.get(event.getNodeid().intValue()));
        }

        // eventTime
        ovent.setEventTime(event.getTime());

        // eventHost
        // Resolve the event host to a hostname using the ipInterface table
        ovent.setEventHost(EventDatabaseConstants.format(eventUtil.getEventHost(event), EVENT_HOST_FIELD_SIZE));

        // eventSource
        ovent.setEventSource(EventDatabaseConstants.format(event.getSource(), EVENT_SOURCE_FIELD_SIZE));

        // ipAddr
        ovent.setIpAddr(event.getInterfaceAddress());

        // ifindex
        if (event.hasIfIndex()) {
            ovent.setIfIndex(event.getIfIndex());
        } else {
        	ovent.setIfIndex(null);
        }

        // eventDpName
        String dpName = event.getDistPoller();
        if (eventHeader != null && eventHeader.getDpName() != null && !"".equals(eventHeader.getDpName())) {
            dpName = EventDatabaseConstants.format(eventHeader.getDpName(), EVENT_DPNAME_FIELD_SIZE);
        } else if (event.getDistPoller() != null && !"".equals(event.getDistPoller())) {
            dpName = EventDatabaseConstants.format(event.getDistPoller(), EVENT_DPNAME_FIELD_SIZE);
        } else {
            dpName = "localhost";
        }
        ovent.setDistPoller(distPollerDao.get(dpName));

        // eventSnmpHost
        ovent.setEventSnmpHost(EventDatabaseConstants.format(event.getSnmphost(), EVENT_SNMPHOST_FIELD_SIZE));

        // service
        ovent.setServiceType(serviceTypeDao.findByName(event.getService()));

        // eventSnmp
        ovent.setEventSnmp(event.getSnmp() == null ? null : SnmpInfo.format(event.getSnmp(), EVENT_SNMP_FIELD_SIZE));

        // eventParms
        // Replace any null bytes with a space, otherwise postgres will complain about encoding in UNICODE
        final String parametersString = EventParameterUtils.format(event);
        ovent.setEventParms(EventDatabaseConstants.format(parametersString, 0));

        // eventCreateTime
        // TODO: Should we use event.getCreationTime() here?
        ovent.setEventCreateTime(new Date());

        // eventDescr
        ovent.setEventDescr(EventDatabaseConstants.format(event.getDescr(), 0));

        // eventLoggroup
        ovent.setEventLogGroup(event.getLoggroupCount() > 0 ? EventDatabaseConstants.format(event.getLoggroup(), EVENT_LOGGRP_FIELD_SIZE) : null);

        // eventLogMsg
        // eventLog
        // eventDisplay
        if (event.getLogmsg() != null) {
            // set log message
            ovent.setEventLogMsg(EventDatabaseConstants.format(event.getLogmsg().getContent(), 0));
            String logdest = event.getLogmsg().getDest();
            if ("logndisplay".equals(logdest)) {
                // if 'logndisplay' set both log and display column to yes
                ovent.setEventLog(String.valueOf(MSG_YES));
                ovent.setEventDisplay(String.valueOf(MSG_YES));
            } else if ("logonly".equals(logdest)) {
                // if 'logonly' set log column to true
                ovent.setEventLog(String.valueOf(MSG_YES));
                ovent.setEventDisplay(String.valueOf(MSG_NO));
            } else if ("displayonly".equals(logdest)) {
                // if 'displayonly' set display column to true
                ovent.setEventLog(String.valueOf(MSG_NO));
                ovent.setEventDisplay(String.valueOf(MSG_YES));
            } else if ("suppress".equals(logdest)) {
                // if 'suppress' set both log and display to false
                ovent.setEventLog(String.valueOf(MSG_NO));
                ovent.setEventDisplay(String.valueOf(MSG_NO));
            }
        } else {
            ovent.setEventLogMsg(null);
            ovent.setEventLog(String.valueOf(MSG_YES));
            ovent.setEventDisplay(String.valueOf(MSG_YES));
        }

        // eventSeverity
        ovent.setEventSeverity(OnmsSeverity.get(event.getSeverity()).getId());

        // eventPathOutage
        ovent.setEventPathOutage(event.getPathoutage() != null ? EventDatabaseConstants.format(event.getPathoutage(), EVENT_PATHOUTAGE_FIELD_SIZE) : null);

        // eventCorrelation
        ovent.setEventCorrelation(event.getCorrelation() != null ? Correlation.format(event.getCorrelation(), EVENT_CORRELATION_FIELD_SIZE) : null);

        // eventSuppressedCount
        ovent.setEventSuppressedCount(null);

        // eventOperInstruct
        ovent.setEventOperInstruct(EventDatabaseConstants.format(event.getOperinstruct(), 0));

        // eventAutoAction
        ovent.setEventAutoAction(event.getAutoactionCount() > 0 ? AutoAction.format(event.getAutoaction(), EVENT_AUTOACTION_FIELD_SIZE) : null);

        // eventOperAction / eventOperActionMenuText
        if (event.getOperactionCount() > 0) {
            final List<Operaction> a = new ArrayList<Operaction>();
            final List<String> b = new ArrayList<String>();

            for (final Operaction eoa : event.getOperactionCollection()) {
                a.add(eoa);
                b.add(eoa.getMenutext());
            }
            ovent.setEventOperAction(OperatorAction.format(a, EVENT_OPERACTION_FIELD_SIZE));
            ovent.setEventOperActionMenuText(EventDatabaseConstants.format(b, EVENT_OPERACTION_FIELD_SIZE));
        } else {
            ovent.setEventOperAction(null);
            ovent.setEventOperActionMenuText(null);
        }

        // eventNotification, this column no longer needed
        ovent.setEventNotification(null);

        // eventTroubleTicket / eventTroubleTicket state
        if (event.getTticket() != null) {
            ovent.setEventTTicket(EventDatabaseConstants.format(event.getTticket().getContent(), EVENT_TTICKET_FIELD_SIZE));
            ovent.setEventTTicketState("on".equals(event.getTticket().getState()) ? 1 : 0);
        } else {
            ovent.setEventTTicket(null);
            ovent.setEventTTicketState(null);
        }

        // eventForward
        ovent.setEventForward(event.getForwardCount() > 0 ? Forward.format(event.getForward(), EVENT_FORWARD_FIELD_SIZE) : null);

        // eventmouseOverText
        ovent.setEventMouseOverText(EventDatabaseConstants.format(event.getMouseovertext(), EVENT_MOUSEOVERTEXT_FIELD_SIZE));

        // eventAckUser
        if (event.getAutoacknowledge() != null && "on".equals(event.getAutoacknowledge().getState())) {
            ovent.setEventAckUser(EventDatabaseConstants.format(event.getAutoacknowledge().getContent(), EVENT_ACKUSER_FIELD_SIZE));
            // eventAckTime - if autoacknowledge is present,
            // set time to event create time
            ovent.setEventAckTime(ovent.getEventCreateTime());
        } else {
            ovent.setEventAckUser(null);
            ovent.setEventAckTime(null);
        }

        eventDao.save(ovent);
        eventDao.flush();

        // Update the event with the database ID of the event stored in the database
        event.setDbid(ovent.getId());
    }
}
