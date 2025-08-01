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
package org.opennms.core.db.install;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Trigger {

    private String m_name;
    private String m_table;
    private String m_storedProcedure;
    private String m_sql;

    private static Pattern m_pattern =
        Pattern.compile("(?i)"
                        + "(CREATE TRIGGER (\\S+)\\s+"
                        + "BEFORE (?:INSERT|UPDATE|INSERT OR UPDATE)\\s+"
                        + "ON (\\S+) FOR EACH ROW\\s+"
                        + "EXECUTE PROCEDURE (\\S+)\\(\\));");

    /**
     * <p>Constructor for Trigger.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param table a {@link java.lang.String} object.
     * @param storedProcedure a {@link java.lang.String} object.
     * @param sql a {@link java.lang.String} object.
     */
    public Trigger(String name, String table, String storedProcedure, String sql) {
        m_name = name;
        m_table = table;
        m_storedProcedure = storedProcedure;
        m_sql = sql;
    }
    
    /**
     * <p>findTriggerInString</p>
     *
     * @param create a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.dao.db.Trigger} object.
     */
    public static Trigger findTriggerInString(String create) {
        Matcher m = m_pattern.matcher(create);
        if (!m.find()) {
            return null;
        }
        
        String sql = m.group(1);
        String name = m.group(2);
        String table = m.group(3);
        String storedProcedure = m.group(4);

        return new Trigger(name, table, storedProcedure, sql);
    }
    
    /**
     * <p>isOnDatabase</p>
     *
     * @param connection a {@link java.sql.Connection} object.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public boolean isOnDatabase(Connection connection) throws SQLException {
        boolean exists;
    
        Statement st = connection.createStatement();
        ResultSet rs = null;
        try {
            rs = st.executeQuery("SELECT oid FROM pg_trigger WHERE tgname = '"
                                           + m_name.toLowerCase()
                                           + "' AND tgrelid = (SELECT oid FROM pg_class WHERE relname = '"
                                           + m_table.toLowerCase()
                                           + "' ) AND tgfoid = (SELECT oid FROM pg_proc WHERE proname = '"
                                           + m_storedProcedure.toLowerCase() + "')");
            exists = rs.next();
        } finally {
            if (rs != null) {
                rs.close();
            }
            st.close();
        }
        
        return exists;
    }

    /**
     * <p>removeFromDatabase</p>
     *
     * @param connection a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public void removeFromDatabase(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute("DROP TRIGGER " + getName() + " ON " + getTable());
        } finally{
            st.close();
        }
    }
    
    /**
     * <p>addToDatabase</p>
     *
     * @param connection a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public void addToDatabase(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute(getSql());
        } finally{
            st.close();
        }
    }


    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>getSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSql() {
        return m_sql;
    }

    /**
     * <p>getStoredProcedure</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStoredProcedure() {
        return m_storedProcedure;
    }

    /**
     * <p>getTable</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTable() {
        return m_table;
    }

}
