package org.opennms.netmgt.dao.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface ColumnChangeReplacement {
    public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException;
    public boolean addColumnIfColumnIsNew();
    public void close() throws SQLException;
}
