/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.event;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsEvent;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class EventDelete extends SqlUpdate {
    
   public EventDelete(DataSource ds) {
       super(ds, "delete from event where eventid = ?");
       declareParameter(new SqlParameter(Types.INTEGER));
       compile();
   }

   public int doDelete(OnmsEvent event) throws DataAccessException {
       return super.update(new Object[] { event.getId() });
   }
}