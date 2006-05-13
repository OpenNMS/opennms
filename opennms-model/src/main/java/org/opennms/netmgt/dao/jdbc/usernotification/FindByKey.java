package org.opennms.netmgt.dao.jdbc.usernotification;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsUserNotification;
import org.springframework.jdbc.core.SqlParameter;



public class FindByKey extends UserNotificationMappingQuery {
    static String s_clause = "FROM usersNotified as u where u.notifyID = ? and u.userID = ?";

    public static FindByKey get(DataSource ds, UserNotificationId id) {
        return null;
    }
       
    public FindByKey(DataSource ds) {
        super(ds, s_clause);
        declareParameter(new SqlParameter("notifyID", Types.INTEGER));
        declareParameter(new SqlParameter("userID", Types.VARCHAR));
        compile();
    }

    public OnmsUserNotification find(UserNotificationId key) {
        
        return new OnmsUserNotification();
    }

}
