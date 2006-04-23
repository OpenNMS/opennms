/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.outage;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsOutage;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class OutageDelete extends SqlUpdate {
    
   public OutageDelete(DataSource ds) {
       super(ds, "delete from outages where outageid = ?");
       declareParameter(new SqlParameter(Types.INTEGER));
       compile();
   }

   public int doDelete(OnmsOutage alarm) throws DataAccessException {
       return super.update(new Object[] { alarm.getId() });
   }
}