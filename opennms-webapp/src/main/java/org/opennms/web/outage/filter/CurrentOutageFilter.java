package org.opennms.web.outage.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class CurrentOutageFilter implements Filter {

    public int bindParam(PreparedStatement ps, int parameterIndex) throws SQLException {
        return 0;
    }

    public String getDescription() {
        return "regainedafter IS NULL";
    }

    public String getTextDescription() {
        return "service has not regained";
    }

    public String getParamSql() {
        return "ifRegainedService IS NULL";
    }

    public String getSql() {
        return "ifRegainedService IS NULL";
    }

}
