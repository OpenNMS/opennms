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
}
