/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.plugins.dbnotifier.alarmnotifier;

public interface AlarmChangeEventConstants {
	
	// note this should be aligned with definitions in AlarmChangeNotifierEvents.xml
	
	// stem of all alarm change notification uei's
	public static final String ALARM_NOTIFICATION_UEI_STEM = "uei.opennms.org/plugin/AlarmChangeNotificationEvent";
	
	// uei definitions of alarm change events
	public static final String ALARM_DELETED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmDeleted";
	public static final String ALARM_CREATED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/NewAlarmCreated";
	public static final String ALARM_SEVERITY_CHANGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmSeverityChanged";
	public static final String ALARM_CLEARED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmCleared";
	public static final String ALARM_ACKNOWLEDGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmAcknowledged";
	public static final String ALARM_UNACKNOWLEDGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmUnAcknowledged";
	public static final String ALARM_SUPPRESSED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmSuppressed";
	public static final String ALARM_UNSUPPRESSED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmUnSuppressed";
	public static final String ALARM_TROUBLETICKET_STATE_CHANGE_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/TroubleTicketStateChange";
	public static final String ALARM_CHANGED_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/AlarmChanged";
	public static final String ALARM_STICKYMEMO_ADD_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/StickyMemoAdded";

	// uei definitions of memo change events
	public static final String STICKY_MEMO_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/StickyMemoUpdate";
	public static final String JOURNAL_MEMO_EVENT = "uei.opennms.org/plugin/AlarmChangeNotificationEvent/JournalMemoUpdate";
	
	// param definitions for memo change events
	public static final String MEMO_VALUES_PARAM="memovalues";
	public static final String MEMO_ALARMID_PARAM="alarmid";
	public static final String MEMO_BODY_PARAM="body";
	public static final String MEMO_AUTHOR_PARAM="author";
	public static final String MEMO_REDUCTIONKEY_PARAM="reductionkey";

	// param definitions for alarm change events
	public static final String OLD_ALARM_VALUES_PARAM="oldalarmvalues";
	public static final String NEW_ALARM_VALUES_PARAM="newalarmvalues";
	
	public static final String INITIAL_SEVERITY_PARAM="initialseverity";
	public static final String OLDSEVERITY_PARAM="oldseverity";
	public static final String ALARM_SEVERITY_PARAM = "alarmseverity";
	public static final String ALARMID_PARAM="alarmid";
	public static final String LOGMSG_PARAM="logmsg";
	public static final String CLEARKEY_PARAM="clearkey";
	public static final String ALARMTYPE_PARAM="alarmtype";
	public static final String ALARM_ACK_TIME_PARAM="alarmacktime";
	public static final String ALARM_ACK_USER_PARAM="alarmackuser";
	public static final String SUPPRESSEDTIME_PARAM="suppressedtime";
	public static final String SUPPRESSEDUNTIL_PARAM="suppresseduntil";
	public static final String SUPPRESSEDUSER_PARAM="suppresseduser";
	public static final String EVENTUEI_PARAM="eventuei";
	public static final String REDUCTIONKEY_PARAM="reductionkey";
	public static final String APPLICATIONDN_PARAM="applicationdn";
	public static final String SERVICEID_PARAM="serviceid";
	public static final String SYSTEMID_PARAM="systemid";
	public static final String OLDTICKETID_PARAM="oldtticketid";
	public static final String TTICKETID_PARAM="tticketid";
	public static final String OLDTTICKETSTATE_PARAM="oldtticketstate";
	public static final String TTICKETSTATE_PARAM="tticketstate";
	public static final String STICKYMEMO_PARAM="stickymemo";

}
