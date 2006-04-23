/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.alarm;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsAlarm;

public class AlarmSave extends AlarmSaveOrUpdate {
    
    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE UPDATE STATEMENT AND THE
    // PARAMETERS IN AlarmSaveOrUpdate
    private static final String insertStmt = "insert into alarms (" +
    
    "alarmID, eventUei, dpName, nodeID, ipaddr,\n" + 
    "serviceID, reductionKey, alarmType, counter, severity,\n" + 
    "lastEventID, firstEventTime, lastEventTime, description, logMsg,\n" + 
    "operInstruct, tticketID, tticketState, mouseOverText, suppressedUntil,\n" + 
    "suppressedUser, suppressedTime, alarmAckUser, alarmAckTime, clearUei\n" + 
    "values (" + 
    "?, ?, ?, ?, ?,\n" + 
    "?, ?, ?, ?, ?,\n" + 
    "?, ?, ?, ?, ?,\n" + 
    "?, ?, ?, ?, ?,\n" + 
    "?, ?, ?, ?, ?)";
    

    public AlarmSave(DataSource ds) {
        super(ds, insertStmt);
    }
    
    public int doInsert(OnmsAlarm alarm) {
        return persist(alarm);
    }

    
}