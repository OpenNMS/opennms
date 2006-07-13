//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.dao.jdbc.event;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsEvent;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class EventSaveOrUpdate extends SqlUpdate {

    public EventSaveOrUpdate(DataSource ds, String updateStmt) {
        setDataSource(ds);
        setSql(updateStmt);
        
        // assumes that the update and insert statements have the same parms in the same order
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventUei
        declareParameter(new SqlParameter(Types.INTEGER));   //nodeID
        declareParameter(new SqlParameter(Types.TIMESTAMP));   //eventTime
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventHost
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventSource
        declareParameter(new SqlParameter(Types.VARCHAR));   //ipAddr
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventDpName
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventSnmphost
        declareParameter(new SqlParameter(Types.INTEGER));   //serviceID
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventSnmp
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventParms
        declareParameter(new SqlParameter(Types.TIMESTAMP));   //eventCreateTime
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventDescr
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventLoggroup
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventLogmsg
        declareParameter(new SqlParameter(Types.INTEGER));   //eventSeverity
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventPathOutage
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventCorrelation
        declareParameter(new SqlParameter(Types.INTEGER));   //eventSuppressedCount
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventOperInstruct
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventAutoAction
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventOperAction
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventOperActionMenuText
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventNotification
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventTticket
        declareParameter(new SqlParameter(Types.INTEGER));   //eventTticketState
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventForward
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventMouseOverText
        declareParameter(new SqlParameter(Types.CHAR));   //eventLog
        declareParameter(new SqlParameter(Types.CHAR));   //eventDisplay
        declareParameter(new SqlParameter(Types.VARCHAR));   //eventAckUser
        declareParameter(new SqlParameter(Types.TIMESTAMP));   //eventAckTime
        declareParameter(new SqlParameter(Types.INTEGER));   //alarmID
        declareParameter(new SqlParameter(Types.INTEGER));   //eventID

        compile();
    }
    
    public int persist(OnmsEvent event) {
        Object[] parms = new Object[] {
                event.getEventUei(),   //eventUei
                (event.getNode() == null ? null : event.getNode().getId()),   //nodeID
                event.getEventTime(),   //eventTime
                event.getEventHost(),   //eventHost
                event.getEventSource(),   //eventSource
                event.getIpAddr(),   //ipAddr
                event.getDistPoller().getName(),   //eventDpName
                event.getEventSnmpHost(),   //eventSnmphost
                (event.getServiceType() == null ? null : event.getServiceType().getId()),   //serviceID
                event.getEventSnmp(),   //eventSnmp
                event.getEventParms(),   //eventParms
                event.getEventCreateTime(),   //eventCreateTime
                event.getEventDescr(),   //eventDescr
                event.getEventLogGroup(),   //eventLoggroup
                event.getEventLogMsg(),   //eventLogmsg
                event.getEventSeverity(),   //eventSeverity
                event.getEventPathOutage(),   //eventPathOutage
                event.getEventCorrelation(),   //eventCorrelation
                event.getEventSuppressedCount(),   //eventSuppressedCount
                event.getEventOperInstruct(),   //eventOperInstruct
                event.getEventAutoAction(),   //eventAutoAction
                event.getEventOperAction(),   //eventOperAction
                event.getEventOperActionMenuText(),   //eventOperActionMenuText
                event.getEventNotification(),   //eventNotification
                event.getEventTTicket(),   //eventTticket
                event.getEventTTicketState(),   //eventTticketState
                event.getEventForward(),   //eventForward
                event.getEventMouseOverText(),   //eventMouseOverText
                event.getEventLog(),   //eventLog
                event.getEventDisplay(),   //eventDisplay
                event.getEventAckUser(),   //eventAckUser
                event.getEventAckTime(),   //eventAckTime
                (event.getAlarm() == null ? null : event.getAlarm().getId()),   //alarmID
                event.getId()				//eventID
        };   
        
        return update(parms);
    }    

}