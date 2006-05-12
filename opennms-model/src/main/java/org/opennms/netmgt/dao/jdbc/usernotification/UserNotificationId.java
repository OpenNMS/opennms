package org.opennms.netmgt.dao.jdbc.usernotification;

import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsUserNotification;


public class UserNotificationId {
    private String m_userID;
    private Integer m_notifyID;
    
    public UserNotificationId(String userID, Integer notifyID) {
        m_userID = userID;
        m_notifyID = notifyID;
    }
    
    public UserNotificationId(OnmsUserNotification userNotification) {
        this(userNotification.getUserId(), userNotification.getId());
    }

    
    public boolean equals(Object obj) {
        if (obj instanceof UserNotificationId) {
            UserNotificationId key = (UserNotificationId) obj;
            return (
                    m_userID.equals(key.m_userID)
                    &&  m_notifyID.equals(key.m_notifyID)
                    );
        }
        return false;
    }

    public int hashCode() {
        return m_userID.hashCode() ^ m_notifyID.hashCode();
    }

    public String toSqlClause() {
        return "userID = ? and notifyID = ?";
    }
    
    public Object[] toSqlParmArray() {
        return new Object[] { m_userID, m_notifyID };
    }

    public String getUserID() {
        return m_userID;
    }
    
    public Integer getNotifyID() {
        return m_notifyID;
    }
    
}
