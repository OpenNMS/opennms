package org.opennms.netmgt.dao.db;

import java.sql.SQLException;

/**
 * <p>DatabaseConnectionException class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class DatabaseConnectionException extends SQLException {

    /**
     * 
     */
    private static final long serialVersionUID = -6548231456647908279L;

    /**
     * <p>Constructor for DatabaseConnectionException.</p>
     *
     * @param reason a {@link java.lang.String} object.
     */
    public DatabaseConnectionException(String reason) {
        super(reason);
    }
}
