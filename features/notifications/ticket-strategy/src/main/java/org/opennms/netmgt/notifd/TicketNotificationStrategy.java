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
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
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
	private EventIpcManager m_eventManager;
	private DefaultEventConfDao m_eventConfDao;
	
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
        
        // Pull the arguments we're interested in from the list.
        for (Argument arg : arguments) {
		LOG.debug("arguments: {} = {}", arg.getSwitch(), arg.getValue());
        	
            if ("eventID".equalsIgnoreCase(arg.getSwitch())) {
            	eventID = arg.getValue();
            } else if ("eventUEI".equalsIgnoreCase(arg.getSwitch())) {
            	eventUEI = arg.getValue();
            } else if ("noticeid".equalsIgnoreCase(arg.getSwitch())) {
            	noticeID = arg.getValue();
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
        if( alarmState.getAlarmID() == 0 ) {
		LOG.error("There is no alarm-id associated with the event-id='{}'. Will not create ticket.", eventID);
        	return 1;
        }
        
        /* Log everything we know so far.
         * The tticketid and tticketstate are only informational.
         */
        LOG.info("Got event-uei='{}' with event-id='{}', notice-id='{}', alarm-type='{}', alarm-id='{}', tticket-id='{}'and tticket-state='{}'", eventUEI, eventID, noticeID, alarmType, alarmState.getAlarmID(), alarmState.getTticketID(), alarmState.getTticketState());
        
        sendCreateTicketEvent(alarmState.getAlarmID(), eventUEI);

        return 0;
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
     * @return 0 if alarmid is null
     */
	protected AlarmType getAlarmTypeFromUEI(final String eventUEI) {
	    final Event event = m_eventConfDao.findByUei(eventUEI);
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
	
    /**
     * <p>Helper function that sends the create ticket event</p>
     *
     * @return
     */
	public void sendCreateTicketEvent(int alarmID, String alarmUEI) {
        LOG.debug("Sending create ticket for alarm '{}' with id={}", alarmUEI, alarmID);
        EventBuilder ebldr = new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, getName());
        ebldr.addParam(EventConstants.PARM_ALARM_ID, alarmID);
        // These fields are required by the trouble ticketer, but not used
        ebldr.addParam(EventConstants.PARM_ALARM_UEI, alarmUEI);
        ebldr.addParam(EventConstants.PARM_USER, "admin");
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
