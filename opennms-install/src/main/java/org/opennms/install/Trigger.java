package org.opennms.install;

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
