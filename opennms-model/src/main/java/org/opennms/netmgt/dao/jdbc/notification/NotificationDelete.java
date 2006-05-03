/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.notification;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsNotification;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class NotificationDelete extends SqlUpdate {
    
   public NotificationDelete(DataSource ds) {
       super(ds, "delete from alarms where alarmid = ?");
       declareParameter(new SqlParameter(Types.INTEGER));
       compile();
   }

   public int doDelete(OnmsNotification notification) throws DataAccessException {
       return super.update(new Object[] { notification.getNotifyId() });
   }
}