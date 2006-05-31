/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.usernotification;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsUserNotification;

public class UserNotificationUpdate extends UserNotificationSaveOrUpdate {

    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE INSERT STATEMENT AND THE
    // PARAMETERS IN UserNotificationSaveOrUpdate
    private static final String updateStmt = "update usersNotified set " +
	    "notifyTime = ?, media = ?, contactinfo = ?, autonotify = ? " + 
	    "where userID = ? and notifyID = ? " +
        "where id = ?";

    public UserNotificationUpdate(DataSource ds) {
        super(ds, updateStmt);
    }
    
    public int doUpdate(OnmsUserNotification userNotification) {
        return persist(userNotification);
    }
    
}
