/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.usernotification;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsNotification;

public class UserNotificationUpdate extends UserNotificationSaveOrUpdate {

    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE INSERT STATEMENT AND THE
    // PARAMETERS IN NotificationSaveOrUpdate
    private static final String updateStmt = "update notifications set " +
	    "testMsg = ?, numericMsg = ?, pageTime = ?, respondTime = ?, " + 
	    "answeredBy = ?, nodeID = ?, interfaceID = ?, serviceID = ?, " + 
	    "eventID = ?, eventUEI = ?,  notifyID = ? " +
	    "where alarmid = ?";

    public UserNotificationUpdate(DataSource ds) {
        super(ds, updateStmt);
    }
    
    public int doUpdate(OnmsNotification notification) {
        return persist(notification);
    }
    
}
