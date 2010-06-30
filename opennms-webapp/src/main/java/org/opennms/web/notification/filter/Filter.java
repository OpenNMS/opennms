package org.opennms.web.notification.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Convenience class to determine what sort of notices to include in a
 * query.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface Filter {
    /**
     * <p>getSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSql();
    
    /**
     * <p>getParamSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getParamSql();
    
    /**
     * <p>bindParams</p>
     *
     * @param ps a {@link java.sql.PreparedStatement} object.
     * @param parameterIndex a int.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public int bindParams(PreparedStatement ps, int parameterIndex) throws SQLException;

    /**
     * <p>getDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getDescription();

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTextDescription();
}
