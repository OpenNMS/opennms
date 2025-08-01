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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class Index {

    private String m_name;
    private String m_table;
    private String m_using;
    private List<String> m_columns;
    private boolean m_unique;
    private String m_where;

    private static Pattern m_pattern =
            Pattern.compile("(?i)create"
                            + "(\\s+unique)?"
                            + "\\s+index\\s+(\\S+)"
                            + "\\s+on\\s+(\\S+)"
                            + "(?:\\s+USING\\s+(\\S+))?"
                            + "\\s*\\(([^)]+)\\)"
                            + "(?:\\s+WHERE\\s+(.*?))?"
                            + "\\s*(?:;|$)");

    /**
     * <p>Constructor for Index.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param table a {@link java.lang.String} object.
     * @param using a {@link java.lang.String} object.
     * @param columns a {@link java.util.List} object.
     * @param unique a boolean.
     * @param where a {@link java.lang.String} object.
     */
    public Index(String name, String table, String using, List<String> columns,
            boolean unique, String where) {
        m_name = name;
        m_table = table;
        m_using = using;
        m_columns = columns;
        m_unique = unique;
        m_where = where;
    }
    
    /**
     * <p>findIndexInString</p>
     *
     * @param create a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.dao.db.Index} object.
     */
    public static Index findIndexInString(String create) {
        Matcher m = m_pattern.matcher(create);
        if (!m.find()) {
            return null;
        }
        
        boolean unique = (m.group(1) != null);
        String name = m.group(2);
        String table = m.group(3);
        String using = m.group(4);
        String columnList = m.group(5);
        String where = m.group(6);
        
        String[] columns = columnList.split("\\s*,\\s*");

        return new Index(name, table, using, Arrays.asList(columns), unique,
                         where);
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

    /**
     * <p>removeFromDatabase</p>
     *
     * @param connection a {@link java.sql.Connection} object.
     * @throws java.sql.SQLException if any.
     */
    public void removeFromDatabase(Connection connection) throws SQLException {
        Statement st = connection.createStatement();
        try {
            st.execute("DROP INDEX " + getName());
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
     * <p>getSql</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getSql() {
        final StringBuilder sql = new StringBuilder();
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
        sql.append(StringUtils.collectionToDelimitedString(m_columns, ", "));
        sql.append(" )");
        if (m_where != null) {
            sql.append(" WHERE ");
            sql.append(m_where);
        }
        
        return sql.toString();
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
     * <p>getTable</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTable() {
        return m_table;
    }
    
    /**
     * <p>isUnique</p>
     *
     * @return a boolean.
     */
    public boolean isUnique() {
        return m_unique;
    }
    
    /**
     * <p>getColumns</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<String> getColumns() {
        return m_columns;
    }

    /**
     * <p>getIndexUniquenessQuery</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws java.lang.Exception if any.
     */
    public String getIndexUniquenessQuery() {
        String firstColumn = getColumns().get(0);
        String columnList = StringUtils.collectionToDelimitedString(getColumns(), ", ");
        
        /*
         * E.g. select * from foo where (a, b) in (select a, b from foo
         *      group by a, b having count(a) > 1 order by a, b);
         */
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM " + getTable() + " WHERE ( "
                   + columnList + " ) IN ( SELECT "  + columnList + " FROM "
                   + getTable() + " GROUP BY " + columnList + " HAVING count("
                   + firstColumn + ") > 1");
        if (m_where != null) {
            sql.append(" AND ( " + m_where + " )");
        }
        sql.append(" ORDER BY " + columnList + " ) "
                   + "ORDER BY " + columnList);
        
        return sql.toString();

        /*
        List<String> whereComponents =
            new ArrayList<String>(getColumns().size() + 2);
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
        }
        
        if (m_where != null) {
            whereComponents.add("( " + m_where + " )");
        }
        return "SELECT DISTINCT a.* FROM "
            + getTable() + " a, " + getTable() + " b WHERE "
            + Installer.join(" AND ", whereComponents);
            */
    }

}
