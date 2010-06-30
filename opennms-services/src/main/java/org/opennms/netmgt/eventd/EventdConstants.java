//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 06: Add nodeId to the WHERE clause for SQL_DB_HOSTIP_TO_HOSTNAME to
//              address the main issue in bug #2247. - dj@opennms.org
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.eventd;

/**
 * This class is a repository for constant, static information concerning
 * Eventd.
 *
 * @author <A HREF="mailto:weave@oculan.com">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @author <A HREF="mailto:weave@oculan.com">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 * @version $Id: $
 */
public final class EventdConstants {
    /**
     * The SQL statement necessary to read service id and service name into map.
     */
    public final static String SQL_DB_SVC_TABLE_READ = "SELECT serviceID, serviceName FROM service";

    /**
     * The SQL insertion string used by eventd to store the event information
     * into the database.
     */
    public final static String SQL_DB_INS_EVENT = "INSERT into events (eventID, eventUei, nodeID, eventTime, eventHost, ipAddr, eventDpName, eventSnmpHost, serviceID, eventSnmp, eventParms, eventCreateTime, eventDescr, eventLoggroup, eventLogmsg, eventLog, eventDisplay, eventSeverity, eventPathOutage, eventCorrelation, eventSuppressedCount, eventOperInstruct, eventAutoAction, eventOperAction, eventOperActionMenuText, eventNotification, eventTticket, eventTticketState, eventForward, eventMouseOverText, eventAckUser, eventAckTime, eventSource) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * The SQL query to test for alarm reduction
     */
    public final static String SQL_DB_ALARM_REDUCTION_QUERY =
            "SELECT alarmid " +
            "  FROM alarms " +
            " WHERE reductionKey = ?";

    /** Constant <code>SQL_DB_UPDATE_EVENT_WITH_ALARM_ID="UPDATE events    SET alarmid = ?  WHERE"{trunked}</code> */
    public static final String SQL_DB_UPDATE_EVENT_WITH_ALARM_ID =
            "UPDATE events "+
            "   SET alarmid = ? " +
            " WHERE eventid = ?";

    /**
     * The SQL insertion string used by eventd to update an event as an alarm
     * in the database.
     */
    public final static String SQL_DB_ALARM_UPDATE_EVENT =
            "UPDATE alarms " +
            "   SET counter = counter+1, lastEventID = ?, lastEventTime = ?, logmsg = ? " +
            " WHERE reductionKey = ?";

    /**
     * The SQL insertion string used by eventd to store the event information as an alarm
     * into the database.
     */
    public final static String SQL_DB_ALARM_INS_EVENT =
            "INSERT" +
            "  INTO alarms (alarmID, eventUei, dpName, nodeID, ipaddr, " +
            "               serviceID, reductionKey, alarmType, counter, severity, " +
            "               lastEventID, firstEventTime, lastEventTime, description, logMsg, " +
            "               operInstruct, tticketID, tticketState, mouseOverText, suppressedUntil, " +
            "               suppressedUser, suppressedTime, alarmAckUser, alarmAckTime, clearUei, " +
            "               x733AlarmType, x733ProbableCause, clearKey) " +
            "VALUES (?,?,?,?,?," +
            "        ?,?,?,?,?," +
            "        ?,?,?," +
            "        ?,?,?,?," +
            "        ?,?,?," +
            "        ?,?,?,?,?," +
            "        ?,?,?)";

    /**
     * The SQL string used by eventd to update number of duplicate events in
     * case of duplicate event suppression.
     */
    public final static String SQL_DB_UPDATE_EVENT_COUNT = "UPDATE events set eventSuppressedCount=? where (eventID=?)";

    /**
     * The SQL statement necessary to convert the service name into a service id
     * using the distributed poller database.
     */
    public final static String SQL_DB_SVCNAME_TO_SVCID = "SELECT serviceID FROM service WHERE serviceName = ?";

    /**
     * The SQL statement necessary to convert the event host into a hostname
     * using the 'ipinterface' table.
     */
    public final static String SQL_DB_HOSTIP_TO_HOSTNAME = "SELECT ipHostname FROM ipinterface WHERE nodeId = ? AND ipAddr = ?";

}
