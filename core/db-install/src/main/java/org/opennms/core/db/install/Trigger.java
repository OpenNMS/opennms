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
        Matcher m = m_pattern.matcher(create.toString());
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
