/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.notification;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsNotification;


public class NotificationSave extends NotificationSaveOrUpdate {
    
    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE UPDATE STATEMENT AND THE
    // PARAMETERS IN NotificationSaveOrUpdate
    private static final String insertStmt = "insert into notifications (" +
    "textMsg, subject, numericMsg, pageTime, respondTime, " + 
    "answeredBy, nodeID, interfaceID, serviceID, queueID, " +
    "eventID, eventUEI, notifyID) " + 
    "values (" + 
    "?, ?, ?, ?, ?, " + 
    "?, ?, ?, ?, ?, " + 
    "?, ?, ?)";
    

    public NotificationSave(DataSource ds) {
        super(ds, insertStmt);
    }
    
    public int doInsert(OnmsNotification notification) {
        return persist(notification);
    }

    
}