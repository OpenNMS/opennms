/**
 * 
 */
package org.opennms.netmgt.dao.db.columnchanges;

import java.sql.ResultSet;
import java.util.Map;

import org.opennms.netmgt.dao.db.ColumnChange;
import org.opennms.netmgt.dao.db.ColumnChangeReplacement;

public class AutoIntegerReplacement implements ColumnChangeReplacement {
    private int m_value;
    
    public AutoIntegerReplacement(int initialValue) {
        m_value = initialValue;
    }
    
    public int getInt() {
        return m_value++;
    }
    
    public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) {
        return getInt();
    }

    public boolean addColumnIfColumnIsNew() {
        return true;
    }
    
    public void close() {
    }
}