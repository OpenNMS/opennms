/**
 * 
 */
package org.opennms.netmgt.dao.jdbc.node;

import java.sql.Types;

import javax.sql.DataSource;

import org.opennms.netmgt.model.OnmsNode;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.object.SqlUpdate;

public class NodeDelete extends SqlUpdate {
    
   public NodeDelete(DataSource ds) {
       super(ds, "delete from node where nodeid = ?");
       declareParameter(new SqlParameter(Types.INTEGER));
       compile();
   }

   public int doDelete(OnmsNode node) throws DataAccessException {
       return super.update(new Object[] { node.getId() });
   }
}