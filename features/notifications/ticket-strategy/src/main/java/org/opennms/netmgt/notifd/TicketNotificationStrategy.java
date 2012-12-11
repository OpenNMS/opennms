/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.Argument;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.eventconf.AlarmData;
import org.opennms.netmgt.xml.eventconf.Event;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.config.DefaultEventConfDao;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
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
	private EventIpcManager m_eventManager;
	private List<Argument> m_arguments;
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
	
	protected class AlarmStateRowCallbackHandler implements RowCallbackHandler {
		AlarmState m_alarmState;
		public AlarmStateRowCallbackHandler() {
			m_alarmState = null;
		}
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
	public int send(List<Argument> arguments) {
        String eventID = null;
        String eventUEI = null;
        String noticeID = null;
        
        m_arguments = arguments;
        
        // Pull the arguments we're interested in from the list.
        for (Argument arg : m_arguments) {
        	log().debug("arguments: "+arg.getSwitch() +" = "+arg.getValue());
        	
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
        	log().error("There is no event-id associated with the notice-id='" + noticeID + "'. Cannot create ticket.");
        	return 1;
        } else if( StringUtils.isBlank(eventUEI) ) {
        	log().error("There is no event-uei associated with the notice-id='" + noticeID + "'. Cannot create ticket.");
        	return 1;
        }
        
        // Determine the type of alarm based on the UEI.
        AlarmType alarmType = getAlarmTypeFromUEI(eventUEI);
        if( alarmType == AlarmType.NOT_AN_ALARM ) {
        	log().warn("The event type associated with the notice-id='" + noticeID + "' is not an alarm. Will not create ticket.");
        	return 0;
        }
        
        // We know the event is an alarm, pull the alarm and current ticket details from the database
        AlarmState alarmState = getAlarmStateFromEvent(Integer.parseInt(eventID));
        if( alarmState.getAlarmID() == 0 ) {
        	log().error("There is no alarm-id associated with the event-id='" + eventID + "'. Will not create ticket.");
        	return 1;
        }
        
        /* Log everything we know so far.
         * The tticketid and tticketstate are only informational.
         */
        log().info("Got event-uei='"+ eventUEI +"' with event-id='" + eventID + 
        			  "', notice-id='" + noticeID + "', alarm-type='" + alarmType +
        			  "', alarm-id='" + alarmState.getAlarmID() + "', tticket-id='" + alarmState.getTticketID() +
        			  "'and tticket-state='" + alarmState.getTticketState() + "'");
        
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
	protected AlarmType getAlarmTypeFromUEI(String eventUEI) {
        Event event = m_eventConfDao.findByUei(eventUEI);
        if( event == null )
        	return AlarmType.NOT_AN_ALARM;
        
        AlarmData alarmData = event.getAlarmData();        
        if( alarmData != null && alarmData.hasAlarmType() ) {
        	if( alarmData.getAlarmType() == 2) {
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
        if (log().isDebugEnabled()) {
            log().debug("Sending create ticket for alarm '" + alarmUEI + "' with id=" + alarmID);
        }
        EventBuilder ebldr = new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, getName());
        ebldr.addParam(EventConstants.PARM_ALARM_ID, alarmID);
        // These fields are required by the trouble ticketer, but not used
        ebldr.addParam(EventConstants.PARM_ALARM_UEI, alarmUEI);
        ebldr.addParam(EventConstants.PARM_USER, "admin");
        m_eventManager.sendNow(ebldr.getEvent());
	}
	
    /**
     * <p>Helper function to log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
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
