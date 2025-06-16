/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * <p>Abstract JDBCTemplate class.</p>
 * 
 * @deprecated Use Hibernate instead of plain JDBC
 *
 * @author brozow
 * @version $Id: $
 */
public abstract class JDBCTemplate {

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
             throw new IllegalArgumentException("Problem executing statement: "+m_sql+" with values "+vals, e);
         }
     }

    private String argsToString(final Object[] values) {
        final StringBuilder sb = new StringBuilder("[");
         for(int i = 0; i < values.length; i++) {
             if (i != 0)
                 sb.append(", ");
             sb.append(values[i]);
         }
         sb.append("]");
         return sb.toString();
    }

    private void doExecute(final Object[] values) throws SQLException {
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
    public String reproduceStatement(final Object[] values) {
    		return m_sql+": with vals "+argsToString(values);
    }
    
    abstract void executeStmt(PreparedStatement stmt) throws SQLException;

}
