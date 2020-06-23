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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.MonitoringSystemDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.util.AutoAction;
import org.opennms.netmgt.dao.util.Correlation;
import org.opennms.netmgt.dao.util.Forward;
import org.opennms.netmgt.dao.util.OperatorAction;
import org.opennms.netmgt.dao.util.SnmpInfo;
import org.opennms.netmgt.eventd.EventUtil;
import org.opennms.netmgt.events.api.EventDatabaseConstants;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Header;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Operaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DeadlockLoserDataAccessException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.util.Assert;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;

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
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#MULTIPLE_VAL_DELIM
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#DB_ATTRIB_DELIM
 * @see org.opennms.netmgt.events.api.EventDatabaseConstants#NAME_VAL_DELIM
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public class HibernateEventWriter implements EventWriter {
    private static final Logger LOG = LoggerFactory.getLogger(HibernateEventWriter.class);

    public static final String LOG_MSG_DEST_DO_NOT_PERSIST = "donotpersist";
    public static final String LOG_MSG_DEST_SUPRRESS = "suppress";
    public static final String LOG_MSG_DEST_LOG_AND_DISPLAY = "logndisplay";
    public static final String LOG_MSG_DEST_LOG_ONLY = "logonly";
    public static final String LOG_MSG_DEST_DISPLAY_ONLY = "displayonly";
    
    @Autowired
    private TransactionOperations m_transactionManager;
    
    @Autowired
    private NodeDao nodeDao;
    
    @Autowired
    private MonitoringSystemDao monitoringSystemDao;

    @Autowired
    private DistPollerDao distPollerDao;
    
    @Autowired
    private EventDao eventDao;

    @Autowired
    private ServiceTypeDao serviceTypeDao;

    @Autowired
    private EventUtil eventUtil;

    private final Timer writeTimer;

    public HibernateEventWriter(MetricRegistry registry) {
        writeTimer = Objects.requireNonNull(registry).timer("eventlogs.process.write");
    }

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
            LOG_MSG_DEST_DO_NOT_PERSIST.equalsIgnoreCase(event.getLogmsg().getDest()) ||
            LOG_MSG_DEST_SUPRRESS.equalsIgnoreCase(event.getLogmsg().getDest())
        ) {
            LOG.debug("{}: uei '{}' marked as '{}'; not processing event.", logPrefix, event.getUei(), event.getLogmsg().getDest());
            return false;
        }
        return true;
    }

    /**
     * Event writing is always synchronous so this method just 
     * delegates to {@link #process(Log)}.
     */
    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        process(eventLog);
    }

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        if (eventLog != null && eventLog.getEvents() != null) {
            final List<Event> eventsInLog = eventLog.getEvents().getEventCollection();
            // This shouldn't happen, but just to be safe...
            if (eventsInLog == null) {
                return;
            }

            // Find the events in the log that need to be persisted
            final List<Event> eventsToPersist = eventsInLog.stream()
                .filter(e -> checkEventSanityAndDoWeProcess(e, "HibernateEventWriter"))
                .collect(Collectors.toList());

            // If there are no events to persist, avoid creating a database transaction
            if (eventsToPersist.size() < 1) {
                return;
            }

            // Time the transaction and insertions
            try (Context context = writeTimer.time()) {
                final AtomicReference<EventProcessorException> exception = new AtomicReference<>();

                m_transactionManager.execute(new TransactionCallbackWithoutResult() {
                    @Override
                    protected void doInTransactionWithoutResult(TransactionStatus status) {
                        for (Event eachEvent : eventsToPersist) {
                            try {
                                process(eventLog.getHeader(), eachEvent);
                            } catch (EventProcessorException e) {
                                exception.set(e);
                                return;
                            }
                        }
                    }
                });

                if (exception.get() != null) {
                    throw exception.get();
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * The method that inserts the event into the database
     */
    private void process(final Header eventHeader, final Event event) throws EventProcessorException {
        LOG.debug("HibernateEventWriter: processing {}, nodeid: {}, ipaddr: {}, serviceid: {}, time: {}", event.getUei(), event.getNodeid(), event.getInterface(), event.getService(), event.getTime());

        try {
            final OnmsEvent ovent = createOnmsEvent(eventHeader, event);
            eventDao.save(ovent);

            // Update the event with the database ID of the event stored in the database
            event.setDbid(ovent.getId());
        } catch (DeadlockLoserDataAccessException e) {
            throw new EventProcessorException("Encountered deadlock when inserting event: " + event.toString(), e);
        } catch (Throwable e) {
            throw new EventProcessorException("Unexpected exception while storing event: " + event.toString(), e);
        }
    }

    /**
     * Creates OnmsEvent to be inserted afterwards.
     * 
     * @exception java.lang.NullPointerException
     *                Thrown if a required resource cannot be found in the
     *                properties file.
     */
    private OnmsEvent createOnmsEvent(final Header eventHeader, final Event event) {

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

        // systemId

        // If available, use the header's distPoller
        if (eventHeader != null && eventHeader.getDpName() != null && !"".equals(eventHeader.getDpName().trim())) {
            // TODO: Should we also try a look up the value in the MinionDao and LocationMonitorDao here?
            ovent.setDistPoller(distPollerDao.get(eventHeader.getDpName()));
        }
        // Otherwise, use the event's distPoller
        if (ovent.getDistPoller() == null && event.getDistPoller() != null && !"".equals(event.getDistPoller().trim())) {
            ovent.setDistPoller(monitoringSystemDao.get(event.getDistPoller()));
        }
        // And if both are unavailable, use the local system as the event's source system
        if (ovent.getDistPoller() == null) {
            ovent.setDistPoller(distPollerDao.whoami());
        }

        // eventSnmpHost
        ovent.setEventSnmpHost(EventDatabaseConstants.format(event.getSnmphost(), EVENT_SNMPHOST_FIELD_SIZE));

        // service
        ovent.setServiceType(serviceTypeDao.findByName(event.getService()));

        // eventSnmp
        ovent.setEventSnmp(event.getSnmp() == null ? null : SnmpInfo.format(event.getSnmp(), EVENT_SNMP_FIELD_SIZE));

        // eventParms
        ovent.setEventParametersFromEvent(event);

        // eventCreateTime
        // TODO: We are overriding the 'eventcreatetime' field of the event with a new Date
        // representing the storage time of the event. 'eventcreatetime' should really be
        // renamed to something like 'eventpersisttime' since that is closer to its meaning.
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
            if (LOG_MSG_DEST_LOG_AND_DISPLAY.equals(logdest)) {
                // if 'logndisplay' set both log and display column to yes
                ovent.setEventLog(String.valueOf(MSG_YES));
                ovent.setEventDisplay(String.valueOf(MSG_YES));
            } else if (LOG_MSG_DEST_LOG_ONLY.equals(logdest)) {
                // if 'logonly' set log column to true
                ovent.setEventLog(String.valueOf(MSG_YES));
                ovent.setEventDisplay(String.valueOf(MSG_NO));
            } else if (LOG_MSG_DEST_DISPLAY_ONLY.equals(logdest)) {
                // if 'displayonly' set display column to true
                ovent.setEventLog(String.valueOf(MSG_NO));
                ovent.setEventDisplay(String.valueOf(MSG_YES));
            } else if (LOG_MSG_DEST_SUPRRESS.equals(logdest)) {
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
            final List<Operaction> a = new ArrayList<>();
            final List<String> b = new ArrayList<>();

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
        return ovent;
    }

    public void setTransactionManager(TransactionOperations transactionManager) {
        m_transactionManager = transactionManager;
    }
}
