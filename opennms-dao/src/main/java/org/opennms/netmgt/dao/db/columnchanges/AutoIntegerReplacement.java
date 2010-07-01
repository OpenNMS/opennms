
/**
 * <p>AutoIntegerReplacement class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
package org.opennms.netmgt.dao.db.columnchanges;

import java.sql.ResultSet;
import java.util.Map;

import org.opennms.netmgt.dao.db.ColumnChange;
import org.opennms.netmgt.dao.db.ColumnChangeReplacement;
public class AutoIntegerReplacement implements ColumnChangeReplacement {
    private int m_value;
    
    /**
     * <p>Constructor for AutoIntegerReplacement.</p>
     *
     * @param initialValue a int.
     */
    public AutoIntegerReplacement(int initialValue) {
        m_value = initialValue;
    }
    
    /**
     * <p>getInt</p>
     *
     * @return a int.
     */
    public int getInt() {
        return m_value++;
    }
    
    /** {@inheritDoc} */
    public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) {
        return getInt();
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
