/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.notifd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.config.api.EventConfDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.model.TroubleTicketState;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.opennms.netmgt.xml.eventconf.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;

/**
 * Invoke the trouble ticketer using notifd instead of automations.
 * This allows tickets to be used in conjunction with path-outages and esclation paths.
 *  
 * @author <a href="mailto:jwhite@datavlaet.com">Jesse White</a>
 * @version $Id: $
 */
public class TicketNotificationStrategy implements NotificationStrategy {

	private static final Logger LOG = LoggerFactory.getLogger(TicketNotificationStrategy.class);
	/** Used as the create-ticket user unless the command supplies a ticketUser argument. */
	public static final String DEFAULT_TICKET_USER = "admin";
	/** Ticket states that no longer block creating a new ticket for the alarm. */
	private static final Set<TroubleTicketState> INACTIVE_TICKET_STATES = EnumSet.of(
			TroubleTicketState.CREATE_FAILED,
			TroubleTicketState.CLOSED,
			TroubleTicketState.RESOLVED,
			TroubleTicketState.CANCELLED);
	private EventIpcManager m_eventManager;
	private EventConfDao m_eventConfDao;
	
	enum AlarmType {
		NOT_AN_ALARM,
		PROBLEM,
		RESULTION
	};
	
	public static class AlarmState {
		int m_alarmID;
		String m_tticketID;
		int m_tticketState;
		
		AlarmState(int alarmID) {
			m_alarmID = alarmID;
			m_tticketID = "";
			m_tticketState = 0;
		}
		
		AlarmState(int alarmID, String tticketID, int tticketState) {
			m_alarmID = alarmID;
			m_tticketID = tticketID;
			m_tticketState = tticketState;
		}
		
		public int getAlarmID() {
			return m_alarmID;
		}
		
		public String getTticketID() {
			return m_tticketID;
		}
		
		public int getTticketState() {
			return m_tticketState;
		}
	}
	
	protected static class AlarmStateRowCallbackHandler implements RowCallbackHandler {
		AlarmState m_alarmState;
		public AlarmStateRowCallbackHandler() {
			m_alarmState = null;
		}
                @Override
        public void processRow(ResultSet rs) throws SQLException {
        	m_alarmState = new AlarmState(rs.getInt(1), rs.getString(2), rs.getInt(3));
        }
        public AlarmState getAlarmState() {
        	return m_alarmState;
        }
	}
	
	public TicketNotificationStrategy() {
		m_eventManager = EventIpcManagerFactory.getIpcManager();
	}

    /** {@inheritDoc} */
        @Override
	public int send(List<Argument> arguments) {
        String eventID = null;
        String eventUEI = null;
        String noticeID = null;
        String ticketUser = DEFAULT_TICKET_USER;

        // Pull the arguments we're interested in from the list.
        for (Argument arg : arguments) {
		LOG.debug("arguments: {} = {}", arg.getSwitch(), arg.getValue());

            if ("eventID".equalsIgnoreCase(arg.getSwitch())) {
            	eventID = arg.getValue();
            } else if ("eventUEI".equalsIgnoreCase(arg.getSwitch())) {
            	eventUEI = arg.getValue();
            } else if ("noticeid".equalsIgnoreCase(arg.getSwitch())) {
            	noticeID = arg.getValue();
            } else if ("ticketUser".equalsIgnoreCase(arg.getSwitch())) {
                if (StringUtils.isNotBlank(arg.getValue())) {
                    ticketUser = arg.getValue();
                }
            }
        }
        
        // Make sure we have the arguments we need.
        if( StringUtils.isBlank(eventID) ) {
		LOG.error("There is no event-id associated with the notice-id='{}'. Cannot create ticket.", noticeID);
        	return 1;
        } else if( StringUtils.isBlank(eventUEI) ) {
		LOG.error("There is no event-uei associated with the notice-id='{}'. Cannot create ticket.", noticeID);
        	return 1;
        }
        
        // Determine the type of alarm based on the UEI.
        AlarmType alarmType = getAlarmTypeFromUEI(eventUEI);
        if( alarmType == AlarmType.NOT_AN_ALARM ) {
		LOG.warn("The event type associated with the notice-id='{}' is not an alarm. Will not create ticket.", noticeID);
        	return 0;
        }
        
        // We know the event is an alarm, pull the alarm and current ticket details from the database
        AlarmState alarmState = getAlarmStateFromEvent(Integer.parseInt(eventID));
        if( alarmState == null ) {
		LOG.error("There is no event with event-id='{}' in the database. Will not create ticket.", eventID);
        	return 1;
        }
        if( alarmState.getAlarmID() == 0 ) {
		LOG.error("There is no alarm-id associated with the event-id='{}'. Will not create ticket.", eventID);
        	return 1;
        }

        /* Guard against duplicate tickets: a previous notification, an escalation,
         * or another member of a group target may already have created one.
         * Tickets in a terminal state (or whose creation failed) do not block a
         * new ticket, so a re-fired problem can open a fresh one.
         * Near-simultaneous deliveries can still race the asynchronous ticket
         * creation, so single-user targets remain the recommended configuration.
         */
        if( StringUtils.isNotBlank(alarmState.getTticketID()) && isTicketActive(alarmState.getTticketState()) ) {
		LOG.info("Alarm-id='{}' already has an active ticket-id='{}' in state '{}'. Will not create another ticket.", alarmState.getAlarmID(), alarmState.getTticketID(), alarmState.getTticketState());
        	return 0;
        }

        // Log everything we know so far.
        LOG.info("Got event-uei='{}' with event-id='{}', notice-id='{}', alarm-type='{}', alarm-id='{}', tticket-id='{}' and tticket-state='{}'", eventUEI, eventID, noticeID, alarmType, alarmState.getAlarmID(), alarmState.getTticketID(), alarmState.getTticketState());
        
        sendCreateTicketEvent(alarmState.getAlarmID(), eventUEI, ticketUser);

        return 0;
	}

    /**
     * A ticket still blocks creating a new one unless it reached a terminal
     * state or its creation failed. Unknown state values are treated as active
     * to stay on the no-duplicate side; a NULL tticketstate column also lands
     * there, because JDBC getInt() maps it to 0 (OPEN).
     */
	protected static boolean isTicketActive(int tticketStateValue) {
		for (TroubleTicketState state : INACTIVE_TICKET_STATES) {
			if (state.getValue() == tticketStateValue) {
				return false;
			}
		}
		return true;
	}

    /**
     * <p>Helper function that gets the alarmid from the eventid</p>
     *
     * @return 0 if alarmid is null
     */
	protected AlarmState getAlarmStateFromEvent(int eventID) {
		AlarmStateRowCallbackHandler callbackHandler = new AlarmStateRowCallbackHandler();

        JdbcTemplate template = new JdbcTemplate(DataSourceFactory.getInstance());
        template.query("SELECT a.alarmid, a.tticketid, a.tticketstate FROM events AS e " +
				       "LEFT JOIN alarms AS a ON a.alarmid = e.alarmid " +
				       "WHERE e.eventid = ?", new Object[] {eventID}, callbackHandler);
        
        return callbackHandler.getAlarmState();
	}
	
    /**
     * <p>Helper function that determines the alarm type for a given UEI.</p>
     *
     * @return NOT_AN_ALARM if the event definition has no alarm-data
     */
	protected AlarmType getAlarmTypeFromUEI(final String eventUEI) {
	    final Event event = getEventConfDao().findByUei(eventUEI);
	    if( event == null ) {
	        return AlarmType.NOT_AN_ALARM;
	    }

	    if (event.getAlarmData() != null && event.getAlarmData().getAlarmType() != null) {
	        if( event.getAlarmData().getAlarmType() == 2) {
	            return AlarmType.RESULTION;
	        } else {
	            return AlarmType.PROBLEM;
	        }
	    }
	    return AlarmType.NOT_AN_ALARM;
	}
	
	protected EventConfDao getEventConfDao() {
		if (m_eventConfDao == null) {
			m_eventConfDao = BeanUtils.getBean("notifdContext", "eventConfDao", EventConfDao.class);
		}
		return m_eventConfDao;
	}

	public void setEventConfDao(EventConfDao eventConfDao) {
		m_eventConfDao = eventConfDao;
	}

    /**
     * <p>Helper function that sends the create ticket event</p>
     *
     * @return
     */
	public void sendCreateTicketEvent(int alarmID, String alarmUEI) {
		sendCreateTicketEvent(alarmID, alarmUEI, DEFAULT_TICKET_USER);
	}

	public void sendCreateTicketEvent(int alarmID, String alarmUEI, String ticketUser) {
        LOG.debug("Sending create ticket for alarm '{}' with id={} as user '{}'", alarmUEI, alarmID, ticketUser);
        EventBuilder ebldr = new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, getName());
        ebldr.addParam(EventConstants.PARM_ALARM_ID, alarmID);
        // These fields are required by the trouble ticketer, but not used
        ebldr.addParam(EventConstants.PARM_ALARM_UEI, alarmUEI);
        ebldr.addParam(EventConstants.PARM_USER, ticketUser);
        m_eventManager.sendNow(ebldr.getEvent());
	}
	
    /**
     * <p>Return an id for this notification strategy</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return "Notifd:TicketNotificationStrategy";
    }
}
