/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.usernotification;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsUserNotification;


public class UserNotificationSave extends UserNotificationSaveOrUpdate {
    
    // DO NOT CHANGE THIS STATEMENT UNLESS YOU CHANGE THE UPDATE STATEMENT AND THE
    // PARAMETERS IN UserNotificationSaveOrUpdate
    private static final String insertStmt = "insert into usersNotified (" +
    "notifyTime, media, contactinfo, autonotify, " + 
    "userID, notifyID " + 
    "values (" + 
    "?, ?, ?, ?, " + 
    "?, ?)";
    

    public UserNotificationSave(DataSource ds) {
        super(ds, insertStmt);
    }
    
    public int doInsert(OnmsUserNotification userNotification) {
        return persist(userNotification);
    }

    
}
