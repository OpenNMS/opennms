/**
 * 
 */
package org.opennms.netmgt.dao.db.columnchanges;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.dao.db.ColumnChange;
import org.opennms.netmgt.dao.db.ColumnChangeReplacement;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class AutoIntegerIdMapStoreReplacement implements ColumnChangeReplacement {
    private int m_value;
    private final String[] m_indexColumns;
    private final Map<MultiColumnKey, Integer> m_idMap =
        new HashMap<MultiColumnKey, Integer>();
    
    public AutoIntegerIdMapStoreReplacement(int initialValue, String[] indexColumns) {
        m_value = initialValue;
        m_indexColumns = indexColumns;
    }
    
    public Integer getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException {
        MultiColumnKey key = getKeyForColumns(rs, columnChanges, m_indexColumns);
        Integer newInteger = m_value++;
        m_idMap.put(key, newInteger);
        return newInteger;
    }
    
    public boolean addColumnIfColumnIsNew() {
        return true;
    }
    
    public Integer getIntegerForColumns(ResultSet rs, Map<String, ColumnChange> columnChanges, String[] columns, boolean noMatchOkay) throws SQLException {
        MultiColumnKey key = getKeyForColumns(rs, columnChanges, columns);

        Integer oldInteger = m_idMap.get(key);
        Assert.isTrue(oldInteger != null || noMatchOkay, "No entry in the map for " + key);
        
        return oldInteger;
    }
    
    private MultiColumnKey getKeyForColumns(ResultSet rs, Map<String, ColumnChange> columnChanges, String[] columns) throws SQLException {
        Object[] objects = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) { 
            String indexColumn = columns[i];
            
            ColumnChange columnChange = columnChanges.get(indexColumn);
            Assert.notNull(columnChange, "No ColumnChange entry for '" + indexColumn + "'");
            
            int index = columnChange.getSelectIndex();
            Assert.isTrue(index > 0, "ColumnChange entry for '" + indexColumn + "' has no select index");
            
            objects[i] = rs.getObject(index);
        }

        return new MultiColumnKey(objects);
    }
    
    public class MultiColumnKey {
        private final Object[] m_keys;
        
        public MultiColumnKey(Object[] keys) {
            m_keys = keys;
        }
        
        @Override
        public boolean equals(Object otherObject) {
            if (!(otherObject instanceof MultiColumnKey)) {
                return false;
            }
            MultiColumnKey other = (MultiColumnKey) otherObject;
            
            if (m_keys.length != other.m_keys.length) {
                return false;
            }
            
            for (int i = 0; i < m_keys.length; i++) {
                if (m_keys[i] == null && other.m_keys[i] == null) {
                    continue;
                }
                if (m_keys[i] == null || other.m_keys[i] == null) {
                    return false;
                }
                if (!m_keys[i].equals(other.m_keys[i])) {
                    return false;
                }
            }
            
            return true;
        }
        
        @Override
        public String toString() {
            return StringUtils.arrayToDelimitedString(m_keys, ", ");
        }
        
        @Override
        public int hashCode() {
            int value = 1;
            for (Object o : m_keys) {
                if (o != null) {
                    // not the other way around, since 1 ^ anything == 1
                    value = o.hashCode() ^ value;
                }
            }
            return value;
        }
    }
    
    public void close() {
    }
}