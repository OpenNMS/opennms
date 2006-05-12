/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.usernotification;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsNotification;


public class UserNotificationSave extends UserNotificationSaveOrUpdate {
    
    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE UPDATE STATEMENT AND THE
    // PARAMETERS IN AlarmSaveOrUpdate
    private static final String insertStmt = "insert into alarms (" +
    "textMsg, numericMsg, pageTime, respondTime, answeredBy, " + 
    "nodeID, interfaceID, serviceID, eventID, eventUEI, " + 
    "notifyID " + 
    "values (" + 
    "?, ?, ?, ?, ?, " + 
    "?, ?, ?, ?, ?, " + 
    "?)";
    

    public UserNotificationSave(DataSource ds) {
        super(ds, insertStmt);
    }
    
    public int doInsert(OnmsNotification notification) {
        return persist(notification);
    }

    
}
