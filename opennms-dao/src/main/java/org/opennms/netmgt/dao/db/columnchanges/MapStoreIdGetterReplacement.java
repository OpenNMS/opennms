/**
 * 
 */
package org.opennms.netmgt.dao.db.columnchanges;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.opennms.netmgt.dao.db.ColumnChange;
import org.opennms.netmgt.dao.db.ColumnChangeReplacement;

public class MapStoreIdGetterReplacement implements ColumnChangeReplacement {
    private final AutoIntegerIdMapStoreReplacement m_storeFoo;
    private final String[] m_indexColumns;
    private final boolean m_noMatchOkay;
    
    public MapStoreIdGetterReplacement(AutoIntegerIdMapStoreReplacement storeFoo,
            String[] columns, boolean noMatchOkay) {
        m_storeFoo = storeFoo;
        m_indexColumns = columns;
        m_noMatchOkay = noMatchOkay;
    }

    public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
        return m_storeFoo.getIntegerForColumns(rs, columnChanges, m_indexColumns, m_noMatchOkay);
    }
    
    public boolean addColumnIfColumnIsNew() {
        return true;
    }
    
    public void close() {
    }
}