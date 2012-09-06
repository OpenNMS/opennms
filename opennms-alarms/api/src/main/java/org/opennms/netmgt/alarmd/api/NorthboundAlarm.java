/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.alarmd.api;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.TroubleTicketState;

/**
 * Wraps the OnmsAlarm into a more generic Alarm instance
 * 
 * FIXME: Improve this alarm to support TIP and 3GPP collaboration.
 * FIXME: Most of these fields are not implemented waiting on above fix to be completed.
 * 
 * @author <a mailto:david@opennms.org>David Hustace</a>
 */
public class NorthboundAlarm implements Preservable {
	
	public static final NorthboundAlarm SYNC_LOST_ALARM = new NorthboundAlarm(-1, "uei.opennms.org/alarmd/northbounderSyncLost");

	public enum AlarmType {
		PROBLEM,
		RESOLUTION,
		NOTIFICATION;
		
		static AlarmType toAlarmType(final int alarmType) {
			if (alarmType == OnmsAlarm.PROBLEM_TYPE) {
				return PROBLEM;
			} else if (alarmType == OnmsAlarm.RESOLUTION_TYPE) {
				return RESOLUTION;
			} else {
				return NOTIFICATION;
			}
		}
	}
	
	
    private final Integer m_id;
    private final String m_uei;
    private final Date m_ackTime;
    private final String m_ackUser;
    private final AlarmType m_alarmType;
    private final String m_appDn;
    private final String m_clearKey;
    private final Integer m_count;
    private final String m_desc;
    private final OnmsDistPoller m_poller;
    private final Date m_firstOccurrence;
    private final InetAddress m_ipAddr;
    private final Date m_lastOccurrence;
    private final String m_logMsg;
    private final String m_objectInstance;
    private final String m_objectType;
    private final String m_operInst;
    private final String m_ossKey;
    private final String m_ossState;
    private final String m_alarmKey;
    private final OnmsServiceType m_service;
    private final OnmsSeverity m_severity;
    private final Date m_suppressed;
    private final Date m_suppressedUntil;
    private final String m_suppressedBy;
    private final String m_ticketId;
    private final TroubleTicketState m_ticketState;
    private final String m_x733Type;
    private final int m_x733Cause;

	private final String m_eventParms;
	
	private volatile boolean m_preserved = false;
    
    private NorthboundAlarm(int id, String uei) {
    	// I only set these for the 'special event'
    	m_id = id;
    	m_uei = uei;
    	
        m_ackTime = null;
        m_ackUser = null;
        m_alarmType = null;
        m_appDn = null;
        m_clearKey = null;
        m_count = null;
        m_desc = null;
        m_poller = null;
        m_eventParms = null;
        //alarm.getFirstAutomationTime();
        m_firstOccurrence = null;
        //alarm.getIfIndex();
        m_ipAddr = null;
        //alarm.getLastAutomationTime();
        //alarm.getLastEvent();
        m_lastOccurrence = null;
        m_logMsg = null;
        m_objectInstance = null;
        m_objectType = null;
        //alarm.getNode();
        m_operInst = null;
        m_ossKey = null;
        m_ossState = null;
        m_alarmKey = null;
        m_service = null;
        m_severity = null;
        //alarm.getSeverityId();
        //alarm.getSeverityLabel();
        m_suppressed = null;
        m_suppressedUntil = null;
        m_suppressedBy = null;
        m_ticketId = null;
        m_ticketState = null;
        //alarm.getType();
        m_x733Type = null;
        m_x733Cause = -1;

	}

    public NorthboundAlarm(OnmsAlarm alarm) {
        //alarm.getAckId();
        //alarm.getAckTime();
        //alarm.getAckUser();
        
        m_ackTime = alarm.getAlarmAckTime();
        m_ackUser = alarm.getAlarmAckUser();
        m_alarmType = alarm.getAlarmType() == null ? null : AlarmType.toAlarmType(alarm.getAlarmType());
        m_appDn = alarm.getApplicationDN();
        m_clearKey = alarm.getClearKey();
        m_count = alarm.getCounter();
        m_desc = alarm.getDescription();
        m_poller = alarm.getDistPoller();
        m_eventParms = alarm.getEventParms();
        //alarm.getFirstAutomationTime();
        m_firstOccurrence = alarm.getFirstEventTime();
        m_id = alarm.getId();
        //alarm.getIfIndex();
        m_ipAddr = alarm.getIpAddr();
        //alarm.getLastAutomationTime();
        //alarm.getLastEvent();
        m_lastOccurrence = alarm.getLastEventTime();
        m_logMsg = alarm.getLogMsg();
        m_objectInstance = alarm.getManagedObjectInstance();
        m_objectType = alarm.getManagedObjectType();
        //alarm.getNode();
        m_operInst = alarm.getOperInstruct();
        m_ossKey = alarm.getOssPrimaryKey();
        m_ossState = alarm.getQosAlarmState();
        m_alarmKey = alarm.getReductionKey();
        m_service = alarm.getServiceType();
        m_severity = alarm.getSeverity();
        //alarm.getSeverityId();
        //alarm.getSeverityLabel();
        m_suppressed = alarm.getSuppressedTime();
        m_suppressedUntil = alarm.getSuppressedUntil();
        m_suppressedBy = alarm.getSuppressedUser();
        m_ticketId = alarm.getTTicketId();
        m_ticketState = alarm.getTTicketState();
        //alarm.getType();
        m_uei = alarm.getUei();
        m_x733Type = alarm.getX733AlarmType();
        m_x733Cause = alarm.getX733ProbableCause();
    }
    
    public Integer getId() {
		return m_id;
	}

	public String getUei() {
		return m_uei;
	}

	public Date getAckTime() {
		return m_ackTime;
	}

	public String getAckUser() {
		return m_ackUser;
	}

	public AlarmType getAlarmType() {
		return m_alarmType;
	}

	public String getAppDn() {
		return m_appDn;
	}

	public String getClearKey() {
		return m_clearKey;
	}

	public Integer getCount() {
		return m_count;
	}

	public String getDesc() {
		return m_desc;
	}

	public OnmsDistPoller getPoller() {
		return m_poller;
	}

	public Date getFirstOccurrence() {
		return m_firstOccurrence;
	}

	public InetAddress getIpAddr() {
		return m_ipAddr;
	}

	public Date getLastOccurrence() {
		return m_lastOccurrence;
	}

	public String getLogMsg() {
		return m_logMsg;
	}

	public String getObjectInstance() {
		return m_objectInstance;
	}

	public String getObjectType() {
		return m_objectType;
	}

	public String getOperInst() {
		return m_operInst;
	}

	public String getOssKey() {
		return m_ossKey;
	}

	public String getOssState() {
		return m_ossState;
	}

	public String getAlarmKey() {
		return m_alarmKey;
	}

	public OnmsServiceType getService() {
		return m_service;
	}

	public OnmsSeverity getSeverity() {
		return m_severity;
	}

	public Date getSuppressed() {
		return m_suppressed;
	}

	public Date getSuppressedUntil() {
		return m_suppressedUntil;
	}

	public String getSuppressedBy() {
		return m_suppressedBy;
	}

	public String getTicketId() {
		return m_ticketId;
	}

	public TroubleTicketState getTicketState() {
		return m_ticketState;
	}

	public String getX733Type() {
		return m_x733Type;
	}

	public int getX733Cause() {
		return m_x733Cause;
	}

	public String getEventParms() {
		return m_eventParms;
	}

	public boolean isPreserved() {
    	return m_preserved;
    }

    public void setPreserved(boolean preserved) {
    	m_preserved = preserved;
    }

}
