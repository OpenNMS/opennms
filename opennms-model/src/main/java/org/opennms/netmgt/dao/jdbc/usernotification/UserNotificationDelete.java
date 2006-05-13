/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.usernotification;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsUserNotification;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class UserNotificationDelete extends SqlUpdate {
    
   public UserNotificationDelete(DataSource ds) {
       super(ds, "delete from userNotified where alarmid = ?");
       declareParameter(new SqlParameter(Types.INTEGER));
       compile();
   }

   public int doDelete(OnmsUserNotification userNotification) throws DataAccessException {
       return super.update(new Object[] { userNotification.getNotification().getNotifyId() }); //TODO: This is wrong and needs to be fixed
   }
}
