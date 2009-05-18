/**
 * 
 */
package org.opennms.netmgt.dao.db.columnchanges;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.opennms.netmgt.dao.db.ColumnChange;
import org.opennms.netmgt.dao.db.ColumnChangeReplacement;

public class VarCharReplacement implements ColumnChangeReplacement {
    private final String m_replacement;
    
    public VarCharReplacement(String value) {
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