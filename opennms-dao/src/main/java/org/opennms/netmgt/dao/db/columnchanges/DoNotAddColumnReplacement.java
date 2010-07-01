
/**
 * <p>DoNotAddColumnReplacement class.</p>
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
public class DoNotAddColumnReplacement implements ColumnChangeReplacement {
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
        return false;
    }

    
    /**
     * <p>close</p>
     */
    public void close() {
    }
}
