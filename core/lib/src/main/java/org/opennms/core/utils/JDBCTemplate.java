/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.dao.DataRetrievalFailureException;

/**
 * <p>Abstract JDBCTemplate class.</p>
 *
 * @author brozow
 * @version $Id: $
 */
abstract public class JDBCTemplate {

    private DataSource m_db;
    private String m_sql;
    
    /**
     * <p>Constructor for JDBCTemplate.</p>
     *
     * @param db a {@link javax.sql.DataSource} object.
     * @param sql a {@link java.lang.String} object.
     */
    protected JDBCTemplate(final DataSource db, final String sql) {
        m_db = db;
        m_sql = sql;
    }

    /**
     * <p>execute</p>
     *
     * @param values a {@link java.lang.Object} object.
     */
    public void execute(final Object ... values) {
         try {
             doExecute(values);
         } catch (final SQLException e) {
             final String vals = argsToString(values);
             throw new DataRetrievalFailureException("Problem executing statement: "+m_sql+" with values "+vals, e);
         }
     }

    private String argsToString(final Object[] values) {
        final StringBuffer sb = new StringBuffer("[");
         for(int i = 0; i < values.length; i++) {
             if (i != 0)
                 sb.append(", ");
             sb.append(values[i]);
         }
         sb.append("]");
         return sb.toString();
    }

    private void doExecute(final Object values[]) throws SQLException {
        final DBUtils d = new DBUtils(getClass());
        try {
            final Connection conn = m_db.getConnection();
            d.watch(conn);
            final PreparedStatement stmt = conn.prepareStatement(m_sql);
            d.watch(stmt);
            for(int i = 0; i < values.length; i++) {
                stmt.setObject(i+1, values[i]);
            }
            executeStmt(stmt);
        } finally {
            d.cleanUp();
        }
    }
    
    /**
     * <p>reproduceStatement</p>
     *
     * @param values an array of {@link java.lang.Object} objects.
     * @return a {@link java.lang.String} object.
     */
    public String reproduceStatement(final Object values[]) {
    		return m_sql+": with vals "+argsToString(values);
    }
    
    abstract void executeStmt(PreparedStatement stmt) throws SQLException;

}
