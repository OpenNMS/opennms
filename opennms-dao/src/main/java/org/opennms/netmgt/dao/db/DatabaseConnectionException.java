package org.opennms.netmgt.dao.db;

import java.sql.SQLException;

public class DatabaseConnectionException extends SQLException {
    private static final long serialVersionUID = 1L;

    public DatabaseConnectionException() {
        super();
    }

    public DatabaseConnectionException(String reason) {
        super(reason);
    }

    public DatabaseConnectionException(Throwable cause) {
        super(cause);
    }

    public DatabaseConnectionException(String reason, String SQLState) {
        super(reason, SQLState);
    }

    public DatabaseConnectionException(String reason, Throwable cause) {
        super(reason, cause);
    }

    public DatabaseConnectionException(String reason, String SQLState, int vendorCode) {
        super(reason, SQLState, vendorCode);
    }

    public DatabaseConnectionException(String reason, String sqlState, Throwable cause) {
        super(reason, sqlState, cause);
    }

    public DatabaseConnectionException(String reason, String sqlState, int vendorCode, Throwable cause) {
        super(reason, sqlState, vendorCode, cause);
    }

}
