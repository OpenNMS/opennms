
/**
 * <p>NullReplacement class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.dao.db.columnchanges;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.opennms.netmgt.dao.db.ColumnChange;
import org.opennms.netmgt.dao.db.ColumnChangeReplacement;
public class NullReplacement implements ColumnChangeReplacement {
    /**
     * <p>Constructor for NullReplacement.</p>
     */
    public NullReplacement() {
        // do nothing
    }
    
    /** {@inheritDoc} */
    public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
        return null;
    }
    
    /**
     * <p>addColumnIfColumnIsNew</p>
     *
     * @return a boolean.
     */
    public boolean addColumnIfColumnIsNew() {
        return true;
    }
    
    /**
     * <p>close</p>
     */
    public void close() {
    }
}
