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
package org.opennms.netmgt.dao.jdbc.alarm;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsAlarm;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class AlarmSaveOrUpdate extends SqlUpdate {

    public AlarmSaveOrUpdate(DataSource ds, String updateStmt) {
        setDataSource(ds);
        setSql(updateStmt);
        
        // assumes that the update and insert statements have the same parms in the same order
        declareParameter(new SqlParameter(Types.VARCHAR));  //eventUei
        declareParameter(new SqlParameter(Types.VARCHAR));  //dpName
        declareParameter(new SqlParameter(Types.INTEGER));  //nodeID
        declareParameter(new SqlParameter(Types.VARCHAR));  //ipaddr
        declareParameter(new SqlParameter(Types.INTEGER));  //serviceID
        declareParameter(new SqlParameter(Types.VARCHAR));  //reductionKey
        declareParameter(new SqlParameter(Types.INTEGER));  //alarmType
        declareParameter(new SqlParameter(Types.INTEGER));  //counter
        declareParameter(new SqlParameter(Types.INTEGER));  //severity
        declareParameter(new SqlParameter(Types.INTEGER));  //lastEventID
        declareParameter(new SqlParameter(Types.TIMESTAMP));  //firstEventTime
        declareParameter(new SqlParameter(Types.TIMESTAMP));  //lastEventTime
        declareParameter(new SqlParameter(Types.VARCHAR));  //description
        declareParameter(new SqlParameter(Types.VARCHAR));  //logMsg
        declareParameter(new SqlParameter(Types.VARCHAR));  //operInstruct
        declareParameter(new SqlParameter(Types.VARCHAR));  //tticketID
        declareParameter(new SqlParameter(Types.INTEGER));  //tticketState
        declareParameter(new SqlParameter(Types.VARCHAR));  //mouseOverText
        declareParameter(new SqlParameter(Types.TIMESTAMP));  //suppressedUntil
        declareParameter(new SqlParameter(Types.VARCHAR));  //suppressedUser
        declareParameter(new SqlParameter(Types.TIMESTAMP));  //suppressedTime
        declareParameter(new SqlParameter(Types.VARCHAR));  //alarmAckUser
        declareParameter(new SqlParameter(Types.TIMESTAMP));  //alarmAckTime
        declareParameter(new SqlParameter(Types.VARCHAR));  //clearUei
        declareParameter(new SqlParameter(Types.VARCHAR));  //managedObjectInstance
        declareParameter(new SqlParameter(Types.VARCHAR));  //managedObjectType
        declareParameter(new SqlParameter(Types.VARCHAR));  //applicationDN
        declareParameter(new SqlParameter(Types.VARCHAR));  //ossPrimaryKey
        declareParameter(new SqlParameter(Types.INTEGER));  //alarmID
        compile();
    }
    
    public int persist(OnmsAlarm alarm) {
        Object[] parms = new Object[] {
        		alarm.getUei(), //eventUei
        		(alarm.getDistPoller() == null ? null : alarm.getDistPoller().getName()), //dpName
        		(alarm.getNode() == null ? null : alarm.getNode().getId()), //nodeID
        		alarm.getIpAddr(), //ipaddr
        		(alarm.getServiceType() == null ? null : alarm.getServiceType().getId()), //serviceID
        		alarm.getReductionKey(), //reductionKey
        		alarm.getAlarmType(), //alarmType
        		alarm.getCounter(), //counter
        		alarm.getSeverity(), //severity
        		alarm.getLastEvent().getId(), //lastEventID
        		alarm.getFirstEventTime(), //firstEventTime
        		alarm.getLastEventTime(), //lastEventTime
        		alarm.getDescription(), //description
        		alarm.getLogMsg(), //logMsg
        		alarm.getOperInstruct(), //operInstruct
        		alarm.getTTicketId(), //tticketID
        		alarm.getTTicketState(), //tticketState
        		alarm.getMouseOverText(), //mouseOverText
        		alarm.getSuppressedUntil(), //suppressedUntil
        		alarm.getSuppressedUser(), //suppressedUser
        		alarm.getSuppressedTime(), //suppressedTime
        		alarm.getAlarmAckUser(), //alarmAckUser
        		alarm.getAlarmAckTime(), //alarmAckTime
        		alarm.getClearUei(), //clearUei        
                alarm.getManagedObjectInstance(), //managedObjectInstance        
                alarm.getManagedObjectType(), //managedObjectType
                alarm.getApplicationDN(), //applicationDN
                alarm.getOssPrimaryKey(), //ossPrimaryKey
        		alarm.getId()}; //alarmID
        return update(parms);
    }    

}