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
    "eventUei, dpName, nodeID, ipaddr, serviceID, " + 
    "reductionKey, alarmType, counter, severity, lastEventID, " + 
    "firstEventTime, lastEventTime, description, logMsg, operInstruct, " + 
    "tticketID, tticketState, mouseOverText, suppressedUntil, suppressedUser, " + 
    "suppressedTime, alarmAckUser, alarmAckTime, clearUei, alarmID " + 
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