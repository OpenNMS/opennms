//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
// Copyright (C) 2001 Oculan Corp.  All rights reserved.
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
//	Brian Weaver	<weave@opennms.org>
//	http://www.opennms.org/
//
// Tab Size = 8
//
package org.opennms.netmgt.eventd;

import java.lang.*;

/**
 * <P>This class is a repository for constant, static information
 * concerning Eventd.</P>
 *
 * @author	<A HREF="mailto:weave@opennms.org">Sowmya Nataraj</A>
 * @author	<A HREF="http://www.opennms.org">OpenNMS.org</A>
 */
public final class EventdConstants
{
	/**
	 * <P>The SQL statement necessary to read service id
	 * and service name into map </P>
	 */
	public final static String 	SQL_DB_SVC_TABLE_READ		= "SELECT serviceID, serviceName FROM service";

	/**
	 * <P>The SQL insertion string used
	 * by eventd to store the event information into the
	 * database.</P>
	 */
	public final static String	SQL_DB_INS_EVENT			= "INSERT into events (eventID, eventUei, nodeID, eventTime, eventHost, ipAddr, eventDpName, eventSnmpHost, serviceID, eventSnmp, eventParms, eventCreateTime, eventDescr, eventLoggroup, eventLogmsg, eventLog, eventDisplay, eventSeverity, eventPathOutage, eventCorrelation, eventSuppressedCount, eventOperInstruct, eventAutoAction, eventOperAction, eventOperActionMenuText, eventNotification, eventTticket, eventTticketState, eventForward, eventMouseOverText, eventAckUser, eventAckTime, eventSource) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	
	/**
	 * <P>The SQL string used by eventd to update number of duplicate
	 * events in case of duplicate event suppression.</P>
	 */
	public final static String	SQL_DB_UPDATE_EVENT_COUNT		= "UPDATE events set eventSuppressedCount=? where (eventID=?)";
	
	/**
	 * <P>The SQL statement necessary to convert
	 * the service name into a service id using the distributed
	 * poller database.</P>
	 */
	public final static String 	SQL_DB_SVCNAME_TO_SVCID			= "SELECT serviceID FROM service WHERE serviceName = ?";

	/**
	 * <P>The SQL statement necessary to convert
	 * the event host into a hostname using the 'ipinterface' table</p>
	 */
	public final static String 	SQL_DB_HOSTIP_TO_HOSTNAME		= "SELECT ipHostname FROM ipinterface WHERE ipAddr = ?";

}

