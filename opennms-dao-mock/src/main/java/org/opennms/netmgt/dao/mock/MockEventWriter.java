/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.mock;

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.dao.util.AutoAction;
import org.opennms.netmgt.dao.util.OperatorAction;
import org.opennms.netmgt.dao.util.SnmpInfo;
import org.opennms.netmgt.events.api.EventDatabaseConstants;
import org.opennms.netmgt.events.api.EventProcessor;
import org.opennms.netmgt.events.api.EventProcessorException;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.netmgt.xml.event.Operaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataAccessException;
import org.springframework.util.Assert;

public class MockEventWriter implements EventProcessor, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(MockEventWriter.class);

    private static final int EVENT_AUTOACTION_FIELD_SIZE = 256;
    private static final int EVENT_CORRELATION_FIELD_SIZE = 1024;
    private static final int EVENT_FORWARD_FIELD_SIZE = 256;
    private static final int EVENT_OPERACTION_FIELD_SIZE = 256;
    private static final int EVENT_OPERACTION_MENU_FIELD_SIZE = 64;
    private static final int EVENT_SNMPHOST_FIELD_SIZE = 256;
    private static final int EVENT_SNMP_FIELD_SIZE = 256;
    private static final int EVENT_TTICKET_FIELD_SIZE = 128;

    private EventDao m_eventDao;
    private DistPollerDao m_distPollerDao;
    private NodeDao m_nodeDao;
    private ServiceTypeDao m_serviceTypeDao;

    public EventDao getEventDao() {
        return m_eventDao;
    }
    
    public void setEventDao(final EventDao eventDao) {
        m_eventDao = eventDao;
    }

    public DistPollerDao getDistPollerDao() {
        return m_distPollerDao;
    }
    
    public void setDistPollerDao(final DistPollerDao distPollerDao) {
        m_distPollerDao = distPollerDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }
    
    public void setNodeDao(final NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public ServiceTypeDao getServiceTypeDao() {
        return m_serviceTypeDao;
    }
    
    public void setServiceTypeDao(final ServiceTypeDao serviceTypeDao) {
        m_serviceTypeDao = serviceTypeDao;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(m_eventDao);
        Assert.notNull(m_distPollerDao);
        Assert.notNull(m_nodeDao);
        Assert.notNull(m_serviceTypeDao);
    }

    @Override
    public void process(Log eventLog) throws EventProcessorException {
        process(eventLog, false);
    }

    @Override
    public void process(Log eventLog, boolean synchronous) throws EventProcessorException {
        if (eventLog != null && eventLog.getEvents() != null && eventLog.getEvents().getEvent() != null) {
            for (Event event : eventLog.getEvents().getEvent()) {
                process(event);
            }
        }
    }

    private void process(final Event event) throws EventProcessorException {
        LOG.debug("Writing event: {}", event);
        final OnmsEvent oe = new OnmsEvent();
        oe.setEventAutoAction((event.getAutoactionCount() > 0) ? AutoAction.format(event.getAutoaction(), EVENT_AUTOACTION_FIELD_SIZE) : null);
        oe.setEventCorrelation((event.getCorrelation() != null) ? org.opennms.netmgt.dao.util.Correlation.format(event.getCorrelation(), EVENT_CORRELATION_FIELD_SIZE) : null);
        oe.setEventCreateTime(event.getCreationTime());
        oe.setId(event.getDbid());
        oe.setEventDescr(event.getDescr());
        try {
            oe.setDistPoller(m_distPollerDao.get(event.getDistPoller()));
        } catch (final DataAccessException e) {
            throw new EventProcessorException(e);
        }
        oe.setEventHost(event.getHost());
        oe.setEventForward((event.getForwardCount() > 0) ? org.opennms.netmgt.dao.util.Forward.format(event.getForward(), EVENT_FORWARD_FIELD_SIZE) : null);
        oe.setIfIndex(event.getIfIndex());
        oe.setIpAddr(event.getInterfaceAddress());

        if (event.getLogmsg() != null) {
            // set log message
            oe.setEventLogMsg(EventDatabaseConstants.format(event.getLogmsg().getContent(), 0));
            final String logdest = event.getLogmsg().getDest();
            if (logdest.equals("logndisplay")) {
                // if 'logndisplay' set both log and display column to yes
                oe.setEventLog("Y");
                oe.setEventDisplay("Y");
            } else if (logdest.equals("logonly")) {
                // if 'logonly' set log column to true
                oe.setEventLog("Y");
                oe.setEventDisplay("N");
            } else if (logdest.equals("displayonly")) {
                // if 'displayonly' set display column to true
                oe.setEventLog("N");
                oe.setEventDisplay("Y");
            } else if (logdest.equals("suppress")) {
                // if 'suppress' set both log and display to false
                oe.setEventLog("N");
                oe.setEventDisplay("N");
            }
        }

        oe.setEventMouseOverText(event.getMouseovertext());
        try {
            oe.setNode(m_nodeDao.get(event.getNodeid().intValue()));
        } catch (final DataAccessException e) {
            throw new EventProcessorException(e);
        }
        
        if (event.getOperactionCount() > 0) {
            final List<Operaction> a = new ArrayList<>();
            final List<String> b = new ArrayList<>();

            for (final Operaction eoa : event.getOperactionCollection()) {
                a.add(eoa);
                b.add(eoa.getMenutext());
            }

            oe.setEventOperAction(OperatorAction.format(a, EVENT_OPERACTION_FIELD_SIZE));
            oe.setEventOperActionMenuText(EventDatabaseConstants.format(b, EVENT_OPERACTION_MENU_FIELD_SIZE));
        }
        oe.setEventOperInstruct(event.getOperinstruct());
        oe.setEventParametersFromEvent(event);
        oe.setEventPathOutage(event.getPathoutage());
        try {
            oe.setServiceType(m_serviceTypeDao.findByName(event.getService()));
        } catch (final DataAccessException e) {
            throw new EventProcessorException(e);
        }
        oe.setSeverityLabel(event.getSeverity());
        oe.setEventSnmp(SnmpInfo.format(event.getSnmp(), EVENT_SNMP_FIELD_SIZE));
        oe.setEventSnmpHost(EventDatabaseConstants.format(event.getSnmphost(), EVENT_SNMPHOST_FIELD_SIZE));
        oe.setEventSource(event.getSource());
        oe.setEventTime(event.getTime());

        if (event.getTticket() != null) {
            oe.setEventTTicket(EventDatabaseConstants.format(event.getTticket().getContent(), EVENT_TTICKET_FIELD_SIZE));
            oe.setEventTTicketState(event.getTticket().getState().equals("on") ? 1 : 0);
        }
        oe.setEventUei(event.getUei());
        
        m_eventDao.saveOrUpdate(oe);
    }
}
