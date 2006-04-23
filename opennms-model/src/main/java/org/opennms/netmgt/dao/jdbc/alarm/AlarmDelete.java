/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.alarm;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsAlarm;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class AlarmDelete extends SqlUpdate {
    
   public AlarmDelete(DataSource ds) {
       super(ds, "delete from alarm where alarmid = ?");
       declareParameter(new SqlParameter(Types.INTEGER));
       compile();
   }

   public int doDelete(OnmsAlarm alarm) throws DataAccessException {
       return super.update(new Object[] { alarm.getId() });
   }
}