/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created February 22, 2007
 *
 * Copyright (C) 2007 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.support;

import java.lang.reflect.UndeclaredThrowableException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.filter.Table;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.Assert;

/**
 * <p>JdbcFilterDao class.</p>
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @version $Id: $
 */
public class JdbcFilterDao implements FilterDao, InitializingBean {
    private DataSource m_dataSource;
    private DatabaseSchemaConfigFactory m_databaseSchemaConfigFactory;
    private NodeDao m_nodeDao;

    /**
     * <p>setDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    /**
     * <p>getDataSource</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public DataSource getDataSource() {
        return m_dataSource;
    }

    /**
     * <p>setDatabaseSchemaConfigFactory</p>
     *
     * @param factory a {@link org.opennms.netmgt.config.DatabaseSchemaConfigFactory} object.
     */
    public void setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory factory) {
        m_databaseSchemaConfigFactory = factory;
    }

    /**
     * <p>getDatabaseSchemaConfigFactory</p>
     *
     * @return a {@link org.opennms.netmgt.config.DatabaseSchemaConfigFactory} object.
     */
    public DatabaseSchemaConfigFactory getDatabaseSchemaConfigFactory() {
        return m_databaseSchemaConfigFactory;
    }

    /**
     * <p>afterPropertiesSet</p>
     */
    public void afterPropertiesSet() {
        Assert.state(m_dataSource != null, "property dataSource cannot be null");
        Assert.state(m_databaseSchemaConfigFactory != null, "property databaseSchemaConfigFactory cannot be null");
    }

    /**
     * <p>setNodeDao</p>
     *
     * @param nodeDao a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * <p>getNodeDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /** {@inheritDoc} */
    public void walkMatchingNodes(String rule, EntityVisitor visitor) {
        Assert.state(m_nodeDao != null, "property nodeDao cannot be null");

        SortedMap<Integer, String> map;
        try {
            map = getNodeMap(rule);
        } catch (FilterParseException e) {
            throw new DataRetrievalFailureException("Could not parse rule '" + rule + "': " + e, e);
        }
        if (log().isDebugEnabled()) {
            log().debug("got " + map.size() + " results");
        }

        for (Integer nodeId : map.keySet()) {
            OnmsNode node = getNodeDao().load(nodeId);
            visitor.visitNode(node);
        }
    }

    /**
     * {@inheritDoc}
     *
     * This method returns a map of all nodeids and nodelabels that match
     * the rule that is passed in, sorted by nodeid.
     * @exception FilterParseException
     *                if a rule is syntactically incorrect or failed in
     *                executing the SQL statement
     */
    public SortedMap<Integer, String> getNodeMap(String rule) throws FilterParseException {
        SortedMap<Integer, String> resultMap = new TreeMap<Integer, String>();
        String sqlString;

        if (log().isDebugEnabled()) {
            log().debug("Filter: rule: " + rule);
        }

        // get the database connection
        Connection conn = null;
        DBUtils d = new DBUtils(getClass());
        try {
            conn = getDataSource().getConnection();
            d.watch(conn);

            // parse the rule and get the sql select statement
            sqlString = getNodeMappingStatement(rule);
            if (log().isDebugEnabled()) {
                log().debug("Filter: SQL statement: " + sqlString);
            }

            // execute query
            Statement stmt = conn.createStatement();
            d.watch(stmt);
            ResultSet rset = stmt.executeQuery(sqlString);
            d.watch(rset);

            if (rset != null) {
                // Iterate through the result and build the map
                while (rset.next()) {
                    resultMap.put(new Integer(rset.getInt(1)), rset.getString(2));
                }
            }
        } catch (FilterParseException e) {
            log().info("Filter Parse Exception occurred getting node map: " + e, e);
            throw new FilterParseException("Filter Parse Exception occurred getting node map: " + e);
        } catch (SQLException e) {
            log().info("SQL Exception occurred getting node map: " + e, e);
            throw new FilterParseException("SQL Exception occurred getting node map: " + e);
        } catch (Exception e) {
            log().fatal("Exception getting database connection: " + e, e);
            throw new UndeclaredThrowableException(e);
        } finally {
            d.cleanUp();
        }

        return resultMap;
    }

    /** {@inheritDoc} */
    public Map<String, Set<String>> getIPServiceMap(String rule) throws FilterParseException {
        Map<String, Set<String>> ipServices = new TreeMap<String, Set<String>>();
        String sqlString;

        if (log().isDebugEnabled()) {
            log().debug("Filter: rule: " + rule);
        }

        // get the database connection
        Connection conn = null;
        DBUtils d = new DBUtils(getClass());
        try {
            conn = getDataSource().getConnection();
            d.watch(conn);

            // parse the rule and get the sql select statement
            sqlString = getIPServiceMappingStatement(rule);
            if (log().isDebugEnabled()) {
                log().debug("Filter: SQL statement: " + sqlString);
            }

            // execute query
            Statement stmt = conn.createStatement();
            d.watch(stmt);
            ResultSet rset = stmt.executeQuery(sqlString);
            d.watch(rset);

            // fill up the array list if the result set has values
            if (rset != null) {
                // Iterate through the result and build the array list
                while (rset.next()) {
                    String ipaddr = rset.getString(1);

                    if (!ipServices.containsKey(ipaddr)) {
                        ipServices.put(ipaddr, new TreeSet<String>());
                    }

                    ipServices.get(ipaddr).add(rset.getString(2));
                }
            }

        } catch (FilterParseException e) {
            log().info("Filter Parse Exception occurred getting IP Service List: " + e, e);
            throw new FilterParseException("Filter Parse Exception occurred getting IP Service List: " + e);
        } catch (SQLException e) {
            log().info("SQL Exception occurred getting IP Service List: " + e, e);
            throw new FilterParseException("SQL Exception occurred getting IP Service List: " + e);
        } catch (Exception e) {
            log().fatal("Exception getting database connection: " + e, e);
            throw new UndeclaredThrowableException(e);
        } finally {
            d.cleanUp();
        }

        return ipServices;
    }

    /**
     * {@inheritDoc}
     *
     * This method returns a list of all ip addresses that match the rule that
     * is passed in.
     * @exception FilterParseException
     *                if a rule is syntactically incorrect or failed in
     *                executing the SQL statement
     */
    public List<String> getIPList(String rule) throws FilterParseException {
        List<String> resultList = new ArrayList<String>();
        String sqlString;

        if (log().isDebugEnabled()) {
            log().debug("Filter: rule: " + rule);
        }

        // get the database connection
        Connection conn = null;
        DBUtils d = new DBUtils(getClass());
        try {
            conn = getDataSource().getConnection();
            d.watch(conn);

            // parse the rule and get the sql select statement
            sqlString = getSQLStatement(rule);
            if (log().isDebugEnabled()) {
                log().debug("Filter: SQL statement: \n" + sqlString);
            }

            // execute query and return the list of ip addresses
            Statement stmt = conn.createStatement();
            d.watch(stmt);
            ResultSet rset = stmt.executeQuery(sqlString);
            d.watch(rset);

            // fill up the array list if the result set has values
            if (rset != null) {
                // Iterate through the result and build the array list
                while (rset.next()) {
                    resultList.add(rset.getString(1));
                }
            }

        } catch (FilterParseException e) {
            log().info("Filter Parse Exception occurred getting IP List: " + e, e);
            throw new FilterParseException("Filter Parse Exception occurred getting IP List: " + e);
        } catch (SQLException e) {
            log().info("SQL Exception occurred getting IP List: " + e, e);
            throw new FilterParseException("SQL Exception occurred getting IP List: " + e);
        } catch (Exception e) {
            log().fatal("Exception getting database connection: " + e, e);
            throw new UndeclaredThrowableException(e);
        } finally {
            d.cleanUp();
        }

        return resultList;
    }

    /**
     * {@inheritDoc}
     *
     * This method verifies if an ip address adheres to a given rule.
     * @exception FilterParseException
     *                if a rule is syntactically incorrect or failed in
     *                executing the SQL statement.
     */
    public boolean isValid(String addr, String rule) throws FilterParseException {
        if (rule.length() == 0) {
            return true;
        } else {
            /*
             * see if the ip address is contained in the list that the
             * rule returns
             */
            return getIPList(rule).contains(addr);
        }
    }

    /** {@inheritDoc} */
    public boolean isRuleMatching(String rule) throws FilterParseException {
        boolean matches = false;
        String sqlString;

        if (log().isDebugEnabled()) {
            log().debug("Filter: rule: " + rule);
        }

        DBUtils d = new DBUtils(getClass());

        // get the database connection
        Connection conn = null;
        try {
            conn = getDataSource().getConnection();
            d.watch(conn);

            // parse the rule and get the sql select statement
            sqlString = getSQLStatement(rule) + " LIMIT 1";
            if (log().isDebugEnabled()) {
                log().debug("Filter: SQL statement: \n" + sqlString);
            }

            // execute query and return the list of ip addresses
            Statement stmt = conn.createStatement();
            d.watch(stmt);
            ResultSet rset = stmt.executeQuery(sqlString);
            d.watch(rset);

            // we only want to check if zero or one rows were fetched, so just
            // return the output from rset.next()
            matches = rset.next();
            if (log().isDebugEnabled()) {
                log().debug("isRuleMatching: rule \"" + rule + "\" " + (matches ? "matches" : "does not match") + " an entry in the database");
            }
        } catch (FilterParseException e) {
            log().info("Filter Parse Exception occurred testing rule \"" + rule + "\" for matching results: " + e, e);
            throw new FilterParseException("Filter Parse Exception occurred testing rule \"" + rule + "\" for matching results: " + e);
        } catch (SQLException e) {
            log().warn("SQL Exception occurred testing rule \""+ rule + "\" for matching results: " + e, e);
            throw new FilterParseException("SQL Exception occurred testing rule \""+ rule + "\" for matching results: " + e);
        } catch (Exception e) {
            log().fatal("Exception getting database connection: " + e, e);
            throw new UndeclaredThrowableException(e);
        } finally {
            d.cleanUp();
        }

        return matches;
    }

    /** {@inheritDoc} */
    public void validateRule(String rule) throws FilterParseException {
        // Since parseRule does not do complete syntax checking,
        // we need to call a function that will actually execute the generated SQL
        isRuleMatching(rule);
    }

    private final Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    /**
     * <p>getNodeMappingStatement</p>
     *
     * @param rule a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.netmgt.filter.FilterParseException if any.
     */
    protected String getNodeMappingStatement(String rule) throws FilterParseException {
        List<Table> tables = new ArrayList<Table>();

        StringBuffer columns = new StringBuffer();
        columns.append(addColumn(tables, "nodeID"));
        columns.append(", " + addColumn(tables, "nodeLabel"));

        String where = parseRule(tables, rule);

        String from = m_databaseSchemaConfigFactory.constructJoinExprForTables(tables);

        return "SELECT DISTINCT " + columns.toString() + " " + from + " " + where;
    }

    /**
     * <p>getIPServiceMappingStatement</p>
     *
     * @param rule a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.netmgt.filter.FilterParseException if any.
     */
    protected String getIPServiceMappingStatement(String rule) throws FilterParseException {
        List<Table> tables = new ArrayList<Table>();

        StringBuffer columns = new StringBuffer();
        columns.append(addColumn(tables, "ipAddr"));
        columns.append(", " + addColumn(tables, "serviceName"));

        String where = parseRule(tables, rule);

        String from = m_databaseSchemaConfigFactory.constructJoinExprForTables(tables);

        return "SELECT " + columns.toString() + " " + from + " " + where;
    }

    /**
     * <p>getInterfaceWithServiceStatement</p>
     *
     * @param rule a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.netmgt.filter.FilterParseException if any.
     */
    protected String getInterfaceWithServiceStatement(String rule) throws FilterParseException {
        List<Table> tables = new ArrayList<Table>();

        StringBuffer columns = new StringBuffer();
        columns.append(addColumn(tables, "ipAddr"));
        columns.append(", " + addColumn(tables, "serviceName"));
        columns.append(", " + addColumn(tables, "nodeID"));

        String where = parseRule(tables, rule);

        String from = m_databaseSchemaConfigFactory.constructJoinExprForTables(tables);

        return "SELECT DISTINCT " + columns.toString() + " " + from + " " + where;
    }

    /**
     * This method parses a rule and returns the SQL select statement equivalent
     * of the rule.
     *
     * @return the SQL select statement
     * @param rule a {@link java.lang.String} object.
     * @throws org.opennms.netmgt.filter.FilterParseException if any.
     */
    protected String getSQLStatement(String rule) throws FilterParseException {
        List<Table> tables = new ArrayList<Table>();

        StringBuffer columns = new StringBuffer();
        columns.append(addColumn(tables, "ipAddr"));

        String where = parseRule(tables, rule);

        String from = m_databaseSchemaConfigFactory.constructJoinExprForTables(tables);

        return "SELECT DISTINCT " + columns.toString() + " " + from + " " + where;
    }

    /**
     * This method should be called if you want to put constraints on the node,
     * interface or service that is returned in the rule. This is useful to see
     * if a particular node, interface, or service matches in the rule, and is
     * primarily used to filter notices. A sub-select is built containing joins
     * constrained by node, interface, and service if they are not null or
     * blank. This select is then ANDed with the filter rule to get the complete
     * SQL statement.
     *
     * @param nodeId
     *            a node id to constrain against
     * @param ipaddr
     *            an ipaddress to constrain against
     * @param service
     *            a service name to constrain against
     * @param rule a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     * @throws org.opennms.netmgt.filter.FilterParseException if any.
     */
    protected String getSQLStatement(String rule, long nodeId, String ipaddr, String service) throws FilterParseException {
        List<Table> tables = new ArrayList<Table>();

        StringBuffer columns = new StringBuffer();
        columns.append(addColumn(tables, "ipAddr"));

        StringBuffer where = new StringBuffer(parseRule(tables, rule));
        if (nodeId != 0)
            where.append(" AND " + addColumn(tables, "nodeID") + " = " + nodeId);
        if (ipaddr != null && !ipaddr.equals(""))
            where.append(" AND " + addColumn(tables, "ipAddr") + " = '" + ipaddr + "'");
        if (service != null && !service.equals(""))
            where.append(" AND " + addColumn(tables, "serviceName") + " = '" + service + "'");

        String from = m_databaseSchemaConfigFactory.constructJoinExprForTables(tables);

        return "SELECT DISTINCT " + columns.toString() + " " + from + " " + where;
    }

    /**
     * SQL Key Word regex
     *
     * Binary Logic / Operators - \\s+(?:AND|OR|(?:NOT )?(?:LIKE|IN)|IS (?:NOT )?DISTINCT FROM)\\s+
     * Unary Operators - \\s+IS (?:NOT )?NULL(?!\\w)
     * Typecasts - ::(?:TIMESTAMP|INET)(?!\\w)
     * Unary Logic - (?&lt;!\\w)NOT\\s+
     * Functions - (?&lt;!\\w)IPLIKE(?=\\()
     *
     */
    private static String sqlKeyWordRegex = "\\s+(?:AND|OR|(?:NOT )?(?:LIKE|IN)|IS (?:NOT )?DISTINCT FROM)\\s+|(?:\\s+IS (?:NOT )?NULL|::(?:TIMESTAMP|INET))(?!\\w)|(?<!\\w)(?:NOT\\s+|IPLIKE(?=\\())";

    /**
     * Generic method to parse and translate a rule into SQL.
     *
     * Only columns listed in database-schema.xml may be used in a filter
     * (explicit "table.column" specification is not supported in filters)
     *
     * To differentiate column names from SQL key words (operators, functions, typecasts, etc)
     * sqlKeyWordRegex must match any SQL key words that may be used in filters,
     * and must not match any column names or prefixed values
     *
     * To make filter syntax more simple and intuitive than SQL
     * - Filters support some aliases for common SQL key words / operators
     *    "&amp;" or "&amp;&amp;" = "AND"
     *    "|" or "||" = "OR"
     *    "!" = "NOT"
     *    "==" = "="
     * - "IPLIKE" may be used as an operator instead of a function in filters ("ipAddr IPLIKE '*.*.*.*'")
     *   When using "IPLIKE" as an operator, the value does not have to be quoted ("ipAddr IPLIKE *.*.*.*" is ok)
     * - Some common SQL expressions may be generated by adding a (lower-case) prefix to an unquoted value in the filter
     *    "isVALUE" = "serviceName = VALUE"
     *    "notisVALUE" = interface does not support the specified service
     *    "catincVALUE" = node is in the specified category
     * - Double-quoted (") strings in filters are converted to single-quoted (') strings in SQL
     *   SQL treats single-quoted strings as constants (values) and double-quoted strings as identifiers (columns, tables, etc)
     *   So, all quoted strings in filters are treated as constants, and filters don't support quoted identifiers
     *
     * This function does not do complete syntax/grammar checking - that is left to the database itself - do not assume the output is valid SQL
     *
     * @param tables
     *            a list to be populated with any tables referenced by the returned SQL
     * @param rule
     *            the rule to parse
     *
     * @return an SQL WHERE clause
     *
     * @throws FilterParseException
     *             if any errors occur during parsing
     */
    private String parseRule(final List<Table> tables, final String rule) throws FilterParseException {
        if (rule != null && rule.length() > 0) {
            String sqlRule = new String(rule);
            Matcher regex;
            List<String> extractedStrings = new ArrayList<String>();
            StringBuffer tempStringBuff;

            // Extract quoted strings from rule and convert double-quoted strings to single-quoted strings
            // Quoted strings need to be extracted first to avoid accidentally matching/modifying anything within them
            // As in SQL, pairs of quotes within a quoted string are treated as an escaped quote character:
            //  'a''b' = a'b ; "a""b" = a"b ; 'a"b' = a"b ; "a'b" = a'b
            regex = Pattern.compile("'(?:[^']|'')*'|\"(?:[^\"]|\"\")*\"").matcher(sqlRule);
            tempStringBuff = new StringBuffer();
            while (regex.find()) {
                String tempString = regex.group();
                if (tempString.charAt(0) == '"') {
                    extractedStrings.add("'" + tempString.substring(1, tempString.length() - 1).replaceAll("\"\"", "\"").replaceAll("'", "''") + "'");
                } else {
                    extractedStrings.add(regex.group());
                }
                regex.appendReplacement(tempStringBuff, "###@" + (extractedStrings.size() - 1) + "@###");
            }
            int tempIndex = tempStringBuff.length();
            regex.appendTail(tempStringBuff);
            if (tempStringBuff.substring(tempIndex).indexOf('\'') > -1) {
                log().error("Unmatched ' in filter rule '" + rule + "'");
                throw new FilterParseException("Unmatched ' in filter rule '" + rule + "'");
            }
            if (tempStringBuff.substring(tempIndex).indexOf('"') > -1) {
                log().error("Unmatched \" in filter rule '" + rule + "'");
                throw new FilterParseException("Unmatched \" in filter rule '" + rule + "'");
            }
            sqlRule = tempStringBuff.toString();

            // Translate filter-specific operators to SQL operators
            sqlRule = sqlRule.replaceAll("\\s*(?:&|&&)\\s*", " AND ");
            sqlRule = sqlRule.replaceAll("\\s*(?:\\||\\|\\|)\\s*", " OR ");
            sqlRule = sqlRule.replaceAll("\\s*!(?!=)\\s*", " NOT ");
            sqlRule = sqlRule.replaceAll("==", "=");

            // Translate IPLIKE operators to IPLIKE() functions
            // If IPLIKE is already used as a function in the filter, this regex should not match it
            regex = Pattern.compile("(\\w+)\\s+IPLIKE\\s+([0-9.*,-]+|###@\\d+@###)", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(sqlRule);
            tempStringBuff = new StringBuffer();
            while (regex.find()) {
                // Is the second argument already a quoted string?
                if (regex.group().charAt(0) == '#') {
                    regex.appendReplacement(tempStringBuff, "IPLIKE($1, $2)");
                } else {
                    regex.appendReplacement(tempStringBuff, "IPLIKE($1, '$2')");
                }
            }
            regex.appendTail(tempStringBuff);
            sqlRule = tempStringBuff.toString();

            // Extract SQL key words to avoid identifying them as columns or prefixed values
            regex = Pattern.compile(sqlKeyWordRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE).matcher(sqlRule);
            tempStringBuff = new StringBuffer();
            while (regex.find()) {
                extractedStrings.add(regex.group().toUpperCase());
                regex.appendReplacement(tempStringBuff, "###@" + (extractedStrings.size() - 1) + "@###");
            }
            regex.appendTail(tempStringBuff);
            sqlRule = tempStringBuff.toString();

            // Identify prefixed values and columns
            regex = Pattern.compile("[a-zA-Z0-9_\\-]*[a-zA-Z][a-zA-Z0-9_\\-]*").matcher(sqlRule);
            tempStringBuff = new StringBuffer();
            while (regex.find()) {
                // Convert prefixed values to SQL expressions
                if (regex.group().startsWith("is")) {
                    regex.appendReplacement(tempStringBuff,
                        addColumn(tables, "serviceName") + " = '" + regex.group().substring(2) + "'");
                } else if (regex.group().startsWith("notis")) {
                    regex.appendReplacement(tempStringBuff,
                        addColumn(tables, "ipAddr") + " NOT IN (SELECT ifServices.ipAddr FROM ifServices, service WHERE service.serviceName ='" + regex.group().substring(5) + "' AND service.serviceID = ifServices.serviceID)");
                } else if (regex.group().startsWith("catinc")) {
                    regex.appendReplacement(tempStringBuff,
                        addColumn(tables, "nodeID") + " IN (SELECT category_node.nodeID FROM category_node, categories WHERE categories.categoryID = category_node.categoryID AND categories.categoryName = '" + regex.group().substring(6) + "')");
                } else {
                    // Call addColumn() on each column
                    regex.appendReplacement(tempStringBuff, addColumn(tables, regex.group()));
                }
            }
            regex.appendTail(tempStringBuff);
            sqlRule = tempStringBuff.toString();

            // Merge extracted strings back into expression
            regex = Pattern.compile("###@(\\d+)@###").matcher(sqlRule);
            tempStringBuff = new StringBuffer();
            while (regex.find())
                regex.appendReplacement(tempStringBuff, Matcher.quoteReplacement(extractedStrings.get(Integer.parseInt(regex.group(1)))));
            regex.appendTail(tempStringBuff);
            sqlRule = tempStringBuff.toString();
            return "WHERE " + sqlRule;
        }
        return "";
    }

    /**
     * Validate that a column is in the schema, add it's table to a list of tables,
     * and return the full table.column name of the column.
     *
     * @param tables
     *            a list of tables to add the column's table to
     * @param column
     *            the column to add
     *
     * @return table.column string
     *
     * @exception FilterParseException
     *                if the column is not found in the schema
     */
    private String addColumn(List<Table> tables, String column) throws FilterParseException {
        Table table = m_databaseSchemaConfigFactory.findTableByVisibleColumn(column);
        if(table == null) {
            log().error("Could not find the column '" + column +"' in filter rule");
            throw new FilterParseException("Could not find the column '" + column +"' in filter rule");
        }
        if (!tables.contains(table))
            tables.add(table);
        return table.getName() + "." + column;
    }

}
