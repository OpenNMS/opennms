/**
 * 
 */
package org.opennms.netmgt.dao.db.columnchanges;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.opennms.netmgt.dao.db.ColumnChange;
import org.opennms.netmgt.dao.db.ColumnChangeReplacement;

public class FixedIntegerReplacement implements ColumnChangeReplacement {
    private final Integer m_replacement;
    
    public FixedIntegerReplacement(int value) {
        m_replacement = value;
    }

    public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
        return m_replacement;
    }
    
    public boolean addColumnIfColumnIsNew() {
        return true;
    }
    
    public void close() {
    }
}