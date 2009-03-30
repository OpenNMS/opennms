package org.opennms.web.notification.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Convenience class to determine what sort of notices to include in a
 * query.
 */
public interface Filter {
    public String getSql();
    
    public String getParamSql();
    
    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException;

    public String getDescription();

    public String getTextDescription();
}