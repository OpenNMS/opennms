//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.dao.db;

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

    public Trigger(String name, String table, String storedProcedure, String sql) {
        m_name = name;
        m_table = table;
        m_storedProcedure = storedProcedure;
        m_sql = sql;
    }
    
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

    public void removeFromDatabase(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute("DROP TRIGGER " + getName() + " ON " + getTable());
        } finally{
            st.close();
        }
    }
    
    public void addToDatabase(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute(getSql());
        } finally{
            st.close();
        }
    }


    public String getName() {
        return m_name;
    }

    public String getSql() {
        return m_sql;
    }

    public String getStoredProcedure() {
        return m_storedProcedure;
    }

    public String getTable() {
        return m_table;
    }

}
