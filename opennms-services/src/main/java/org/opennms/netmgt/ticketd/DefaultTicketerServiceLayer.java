/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ticketd;

import org.opennms.api.integration.ticketing.*;
import org.opennms.api.integration.ticketing.Ticket.State;
import org.opennms.core.utils.ThreadCategory;

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;

/**
 * OpenNMS Trouble Ticket API implementation.
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class DefaultTicketerServiceLayer implements TicketerServiceLayer, InitializingBean {
	
	private AlarmDao m_alarmDao;
    private Plugin m_ticketerPlugin;
    private EventIpcManager m_eventIpcManager;
    
    static final String COMMS_ERROR_UEI = "uei.opennms.org/troubleTicket/communicationError";
    
    /**
     * <p>Constructor for DefaultTicketerServiceLayer.</p>
     */
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
     * Needs access to the ticketer Plugin API implementation for
     * communication with the HelpDesk.
     *
     * @param ticketerPlugin a {@link org.opennms.api.integration.ticketing.Plugin} object.
     */
    public void setTicketerPlugin(Plugin ticketerPlugin) {
        m_ticketerPlugin = ticketerPlugin;
    }
    
    /**
     * Spring functionality implemented to validate the state of the trouble ticket
     * plugin API.
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.state(m_alarmDao != null, "alarmDao property must be set");
        Assert.state(m_ticketerPlugin != null, "ticketPlugin property must be set");
    }

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.ticketd.TicketerServiceLayer#cancelTicketForAlarm(int, java.lang.String)
     */
	/** {@inheritDoc} */
	public void cancelTicketForAlarm(int alarmId, String ticketId) {
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
		if (alarm == null) {
			throw new ObjectRetrievalFailureException("Unable to locate Alarm with ID: "+alarmId, null);
		}

	
		try {
            setTicketState(ticketId, Ticket.State.CANCELLED);
            alarm.setTTicketState(TroubleTicketState.CANCELLED);
        } catch (PluginException e) {
            alarm.setTTicketState(TroubleTicketState.CANCEL_FAILED);
            log().error("Unable to cancel ticket for alarm: " + e.getMessage());
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
            log().error("Unable to set ticket state");
            throw e;
        }
    }
    

    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.ticketd.TicketerServiceLayer#closeTicketForAlarm(int, java.lang.String)
     */
	/** {@inheritDoc} */
	public void closeTicketForAlarm(int alarmId, String ticketId) {
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
        
        try {
            setTicketState(ticketId, State.CLOSED);
            alarm.setTTicketState(TroubleTicketState.CLOSED);
        } catch (PluginException e) {
            alarm.setTTicketState(TroubleTicketState.CLOSE_FAILED);
            log().error("Unable to close ticket for alarm: " + e.getMessage());
            m_eventIpcManager.sendNow(createEvent(e.getMessage()));
        }
        
		m_alarmDao.saveOrUpdate(alarm);
	}

	/*
	 * (non-Javadoc)
	 * @see org.opennms.netmgt.ticketd.TicketerServiceLayer#createTicketForAlarm(int)
	 */
	/** {@inheritDoc} */
	public void createTicketForAlarm(int alarmId) {
	    
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
        
        Ticket ticket = createTicketFromAlarm(alarm);
        
        try {
            m_ticketerPlugin.saveOrUpdate(ticket);
            alarm.setTTicketId(ticket.getId());
            alarm.setTTicketState(TroubleTicketState.OPEN);
        } catch (PluginException e) {
            alarm.setTTicketState(TroubleTicketState.CREATE_FAILED);
            log().error("Unable to create ticket for alarm: "  + e.getMessage());
            m_eventIpcManager.sendNow(createEvent(e.getMessage()));
        }
        
		m_alarmDao.saveOrUpdate(alarm);
		
	}

	/**
	 * Called from API implemented method after successful retrieval of Alarm.
	 * 
	 * @param alarm OpenNMS Model class alarm
	 * @return OpenNMS Ticket with contents of alarm.
	 * TODO: Add alarm attributes to Ticket.
	 * TODO: Add alarmid to Ticket class for ability to reference back to Alarm (waffling on this
	 * since ticket isn't a persisted object and other reasons)
	 */
    private Ticket createTicketFromAlarm(OnmsAlarm alarm) {
        Ticket ticket = new Ticket();
        ticket.setSummary(alarm.getLogMsg());
        ticket.setDetails(alarm.getDescription());
        ticket.setId(alarm.getTTicketId());
        return ticket;
    }

    
    /*
     * (non-Javadoc)
     * @see org.opennms.netmgt.ticketd.TicketerServiceLayer#updateTicketForAlarm(int, java.lang.String)
     */
	/** {@inheritDoc} */
	public void updateTicketForAlarm(int alarmId, String ticketId) {
		
//      ticket.setState(State.OPEN);
//      ticket.setDetails(alarm.getDescription());
//      m_ticketerPlugin.saveOrUpdate(ticket);
		
		OnmsAlarm alarm = m_alarmDao.get(alarmId);
        
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
            log().error("Unable to update ticket for alarm: " + e.getMessage());
            m_eventIpcManager.sendNow(createEvent(e.getMessage()));
        }
        
		m_alarmDao.saveOrUpdate(alarm);
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
	 * @return a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
	 */
	public EventIpcManager getEventIpcManager() {
        return m_eventIpcManager;
    }

    /**
     * <p>setEventIpcManager</p>
     *
     * @param ipcManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public void setEventIpcManager(EventIpcManager ipcManager) {
        m_eventIpcManager = ipcManager;
    }
	
	 /**
     * Covenience logging.
     * 
     * @return a log4j Category for this class
     */
    ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }
    
}
