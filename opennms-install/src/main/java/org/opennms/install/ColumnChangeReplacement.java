package org.opennms.install;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

public interface ColumnChangeReplacement {
    public Object getColumnReplacement(ResultSet rs, Map<String, ColumnChange> columnChanges) throws SQLException;
    public boolean addColumnIfColumnIsNew();
}
