package org.opennms.netmgt.dao.jdbc.usernotification;

import javax.sql.DataSource;



public class FindByKey extends UserNotificationMappingQuery {


    public static FindByKey get(DataSource ds, UserNotificationId id) {
        return null;
    }
       
    public FindByKey(DataSource ds, String clause) {
        super(ds, clause);
    }

    public void find(UserNotificationId key) {
        // TODO Auto-generated method stub
        
    }

}
