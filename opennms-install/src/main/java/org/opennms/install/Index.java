package org.opennms.install;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Index {

    private String m_name;
    private String m_table;
    private String m_using;
    private List<String> m_columns;
    private boolean m_unique;

    private static Pattern m_pattern =
            Pattern.compile("(?i)create\\s+(unique\\s+)?"
                            + "index\\s+(\\S+)\\s+"
                            + "on\\s+(\\S+)(?:\\s+USING (\\S+))?\\s*\\(([^)]+)\\)");

    public Index(String name, String table, String using, List<String> columns,
            boolean unique) {
        m_name = name;
        m_table = table;
        m_using = using;
        m_columns = columns;
        m_unique = unique;
    }
    
    public static Index findIndexInString(String create) {
        Matcher m = m_pattern.matcher(create.toString());
        if (!m.find()) {
            return null;
        }
        
        boolean unique = (m.group(1) != null);
        String name = m.group(2);
        String table = m.group(3);
        String using = m.group(4);
        String columnList = m.group(5);
        
        String[] columns = columnList.split("\\s*,\\s*");

        return new Index(name, table, using, Arrays.asList(columns), unique);
    }
    
    public boolean isOnDatabase(Connection connection) throws SQLException {
        boolean exists;
    
        Statement st = connection.createStatement();
        ResultSet rs = null;
        try {
            rs = st.executeQuery("SELECT relname FROM pg_class "
                                 + "WHERE relname = '" + m_name.toLowerCase()
                                 + "'");
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
            st.execute("DROP INDEX " + getName());
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
    
    public String getSql() {
        StringBuffer sql = new StringBuffer();
        sql.append("CREATE ");
        if (m_unique) {
            sql.append("UNIQUE ");
            
        }
        sql.append("INDEX ");
        sql.append(m_name);
        sql.append(" ON ");
        sql.append(m_table);
        if (m_using != null) {
            sql.append(" USING ");
            sql.append(m_using);
        }
        sql.append(" ( ");
        sql.append(Installer.join(", ", m_columns));
        sql.append(" )");
        
        return sql.toString();
    }


    public String getName() {
        return m_name;
    }

    public String getTable() {
        return m_table;
    }
    
    public boolean isUnique() {
        return m_unique;
    }
    
    public List<String> getColumns() {
        return m_columns;
    }

    public String getIndexUniquenessQuery() throws Exception {
        List<String> whereComponents =
            new ArrayList<String>(getColumns().size() + 1);
        for (String column : getColumns()) {
            whereComponents.add("a." + column + " = b." + column);
        }
        
        String lowerTable = getTable().toLowerCase();
        if ("snmpinterface".equals(lowerTable)) {
            whereComponents.add("a.ipAddr != b.ipAddr");
        } else if ("ifservices".equals(lowerTable)) {
            whereComponents.add("a.lastGood != b.lastGood");
        } else {
            return null;
            /*
            throw new Exception("table '" + lowerTable + "' not supported "
                                + "at this time");
                                */
        }
        return "SELECT DISTINCT a.* FROM "
            + getTable() + " a, " + getTable() + " b WHERE "
            + Installer.join(" AND ", whereComponents);
    }

}
