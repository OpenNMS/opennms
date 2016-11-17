/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ticketd;

import java.util.Map;
import java.util.Objects;

import org.opennms.api.integration.ticketing.Plugin;
import org.opennms.api.integration.ticketing.PluginException;
import org.opennms.api.integration.ticketing.Ticket;
import org.opennms.api.integration.ticketing.Ticket.State;
import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * OpenNMS Trouble Ticket API implementation.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class DefaultTicketerServiceLayer implements TicketerServiceLayer, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultTicketerServiceLayer.class);

    protected static final String COMMS_ERROR_UEI = "uei.opennms.org/troubleTicket/communicationError";
    public static final String SKIP_CREATE_WHEN_CLEARED_SYS_PROP = "opennms.ticketer.skipCreateWhenCleared";
    public static final String SKIP_CLOSE_WHEN_NOT_CLEARED_SYS_PROP = "opennms.ticketer.skipCloseWhenNotCleared";

    private final boolean SKIP_CREATE_WHEN_CLEARED = Boolean.getBoolean(SKIP_CREATE_WHEN_CLEARED_SYS_PROP);
    private final boolean SKIP_CLOSE_WHEN_NOT_CLEARED = Boolean.getBoolean(SKIP_CLOSE_WHEN_NOT_CLEARED_SYS_PROP);

    @Autowired
    private AlarmDao m_alarmDao;

    private Plugin m_ticketerPlugin;
    private EventIpcManager m_eventIpcManager;

    public DefaultTicketerServiceLayer() {
        m_eventIpcManager = EventIpcManagerFactory.getIpcManager();
    }

    /**
     * Needs access to the AlarmDao.
     *
     * @param alarmDao a {@link org.opennms.netmgt.dao.AlarmDao} object.
     */
    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    /**
     * Spring functionality implemented to validate the state of the trouble ticket
     * plugin API.
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(m_alarmDao, "alarmDao property must be set");
        Objects.requireNonNull(m_ticketerPlugin, "ticketerPlugin property must be set");
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.ticketd.TicketerServiceLayer#cancelTicketForAlarm(int, java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public void cancelTicketForAlarm(int alarmId, String ticketId) {
        OnmsAlarm alarm = m_alarmDao.get(alarmId);
        if (alarm == null) {
            LOG.error("No alarm with id {} was found. Ticket with id '{}' will not be canceled.", alarmId, ticketId);
            return;
        }

        try {
            setTicketState(ticketId, Ticket.State.CANCELLED);
            alarm.setTTicketState(TroubleTicketState.CANCELLED);
        } catch (PluginException e) {
            alarm.setTTicketState(TroubleTicketState.CANCEL_FAILED);
            LOG.error("Unable to cancel ticket for alarm: {}", e.getMessage(), e);
            m_eventIpcManager.sendNow(createEvent(e.getMessage()));
        }

        m_alarmDao.saveOrUpdate(alarm);

    }

    private void setTicketState(String ticketId, State state) throws PluginException { 
        try {
            Ticket ticket = m_ticketerPlugin.get(ticketId);
            ticket.setState(state);
            m_ticketerPlugin.saveOrUpdate(ticket);
        } catch (PluginException e) {            
            LOG.error("Unable to set ticket state");
            throw e;
        }
    }


    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.ticketd.TicketerServiceLayer#closeTicketForAlarm(int, java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public void closeTicketForAlarm(int alarmId, String ticketId) {
        OnmsAlarm alarm = m_alarmDao.get(alarmId);
        if (alarm == null) {
            LOG.error("No alarm with id {} was found. Ticket with id '{}' will not be closed.", alarmId, ticketId);
            return;
        }

        if (SKIP_CLOSE_WHEN_NOT_CLEARED) {
            final OnmsSeverity currentSeverity = alarm.getSeverity();
            if (currentSeverity != null && !currentSeverity.equals(OnmsSeverity.CLEARED)) {
                LOG.info("Alarm with id {} is not currently cleared. Ticket with id '{}' will not be closed.", alarmId, ticketId);
                return;
            }
        }

        try {
            setTicketState(ticketId, State.CLOSED);
            alarm.setTTicketState(TroubleTicketState.CLOSED);
        } catch (PluginException e) {
            alarm.setTTicketState(TroubleTicketState.CLOSE_FAILED);
            LOG.error("Unable to close ticket for alarm: {}", e.getMessage(), e);
            m_eventIpcManager.sendNow(createEvent(e.getMessage()));
        }

        m_alarmDao.saveOrUpdate(alarm);
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.ticketd.TicketerServiceLayer#createTicketForAlarm(int)
     */
    /** {@inheritDoc} */
    @Override
    public void createTicketForAlarm(int alarmId, Map<String,String> attributes) {

        OnmsAlarm alarm = m_alarmDao.get(alarmId);
        if (alarm == null) {
            LOG.error("No alarm with id {} was found. No ticket will be created.", alarmId);
            return;
        }

        if (SKIP_CREATE_WHEN_CLEARED) {
            final OnmsSeverity currentSeverity = alarm.getSeverity();
            if (currentSeverity != null && currentSeverity.equals(OnmsSeverity.CLEARED)) {
                LOG.info("Alarm with id {} is currently cleared. No ticket will be created.", alarmId);
                return;
            }
        }

        Ticket ticket = createTicketFromAlarm(alarm, attributes);

        try {
            m_ticketerPlugin.saveOrUpdate(ticket);
            alarm.setTTicketId(ticket.getId());
            alarm.setTTicketState(TroubleTicketState.OPEN);
        } catch (PluginException e) {
            alarm.setTTicketState(TroubleTicketState.CREATE_FAILED);
            LOG.error("Unable to create ticket for alarm: {}", e.getMessage(), e);
            m_eventIpcManager.sendNow(createEvent(e.getMessage()));
        }

        m_alarmDao.saveOrUpdate(alarm);

    }

    /**
     * Called from API implemented method after successful retrieval of Alarm.
     * 
     * @param alarm OpenNMS Model class alarm
     * @param attributes
     * @return OpenNMS Ticket with contents of alarm.
     * TODO: Add alarm attributes to Ticket.
     * TODO: Add alarmid to Ticket class for ability to reference back to Alarm (waffling on this
     * since ticket isn't a persisted object and other reasons)
     */
    protected Ticket createTicketFromAlarm(OnmsAlarm alarm, Map<String, String> attributes) {
        Ticket ticket = new Ticket();
        ticket.setSummary(alarm.getLogMsg());
        ticket.setDetails(alarm.getDescription());
        ticket.setId(alarm.getTTicketId());
        ticket.setAlarmId(alarm.getId());
        ticket.setNodeId(alarm.getNodeId());
        ticket.setIpAddress(alarm.getIpAddr());
        ticket.setAttributes(attributes);
        if (attributes.containsKey("user"))
            ticket.setUser(attributes.get("user"));
        return ticket;
    }


    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.ticketd.TicketerServiceLayer#updateTicketForAlarm(int, java.lang.String)
     */
    /** {@inheritDoc} */
    @Override
    public void updateTicketForAlarm(int alarmId, String ticketId) {
        OnmsAlarm alarm = m_alarmDao.get(alarmId);
        if (alarm == null) {
            LOG.error("No alarm with id {} was found. Ticket with id '{}' will not be updated.", alarmId, ticketId);
            return;
        }

        Ticket ticket = null;

        try {
            ticket = m_ticketerPlugin.get(ticketId);
            if (ticket.getState() == Ticket.State.CANCELLED) {
                alarm.setTTicketState(TroubleTicketState.CANCELLED);
            } else if (ticket.getState() == Ticket.State.CLOSED) {
                alarm.setTTicketState(TroubleTicketState.CLOSED);
            } else if (ticket.getState() == Ticket.State.OPEN) {
                alarm.setTTicketState(TroubleTicketState.OPEN);
            } else {
                alarm.setTTicketState(TroubleTicketState.OPEN);
            }
        } catch (PluginException e) {
            alarm.setTTicketState(TroubleTicketState.UPDATE_FAILED);
            LOG.error("Unable to update ticket for alarm: {}", e.getMessage());
            m_eventIpcManager.sendNow(createEvent(e.getMessage()));
        }

        m_alarmDao.saveOrUpdate(alarm);
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.ticketd.TicketerServiceLayer#reloadTicketer()
     */
    /** {@inheritDoc} */
    @Override
    public void reloadTicketer() {
        // Do nothing
    }

    // TODO what if the alarm doesn't exist?

    private Event createEvent(String reason) {
        EventBuilder bldr = new EventBuilder(COMMS_ERROR_UEI, "Ticketd");
        bldr.addParam("reason", reason);
        return bldr.getEvent();
    }

    /**
     * <p>getEventIpcManager</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public EventIpcManager getEventIpcManager() {
        return m_eventIpcManager;
    }

    /**
     * <p>setEventIpcManager</p>
     *
     * @param ipcManager a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public void setEventIpcManager(EventIpcManager ipcManager) {
        m_eventIpcManager = ipcManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setTicketerPlugin(Plugin plugin) {
        m_ticketerPlugin = Objects.requireNonNull(plugin, "plugin cannot be null");
    }
}
