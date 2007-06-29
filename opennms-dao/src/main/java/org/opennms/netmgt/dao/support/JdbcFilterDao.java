package org.opennms.netmgt.dao.support;

import java.io.PushbackReader;
import java.io.StringReader;
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

import javax.sql.DataSource;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.dao.FilterDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.netmgt.filter.SQLTranslation;
import org.opennms.netmgt.filter.lexer.Lexer;
import org.opennms.netmgt.filter.node.Start;
import org.opennms.netmgt.filter.parser.Parser;
import org.opennms.netmgt.model.EntityVisitor;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.Assert;

public class JdbcFilterDao implements FilterDao, InitializingBean {
    private NodeDao m_nodeDao;
    private DataSource m_dataSource;
    private DatabaseSchemaConfigFactory m_databaseSchemaConfigFactory;
    
    /**
     * This method returns a map of all nodeids and nodelabels that match
     * the rule that is passed in, sorted by nodeid.
     * 
     * @param rule
     *            an expression rule to be parsed and executed.
     * 
     * @return SortedMap containing all nodeids/nodelabels selected by the rule.
     * 
     * @exception FilterParseException
     *                if a rule is syntactically incorrect or failed in
     *                executing the SQL statement
     */
    public SortedMap<Integer, String> getNodeMap(String rule) throws FilterParseException {
        SortedMap<Integer, String> resultMap = new TreeMap<Integer, String>();
        String sqlString = null;

        if (log().isDebugEnabled()) {
            log().debug("Filter: rule: " + rule);
        }

        // get the database connection
        Connection conn = null;
        try {
            conn = getDataSource().getConnection();

            // parse the rule and get the sql select statement
            sqlString = getNodeMappingStatement(rule);
            if (log().isDebugEnabled()) {
                log().debug("Filter: SQL statement: " + sqlString);
            }

            // execute query
            Statement stmt = conn.createStatement();
            ResultSet rset = stmt.executeQuery(sqlString);

            if (rset != null) {
                // Iterate through the result and build the map
                while (rset.next()) {
                    resultMap.put(new Integer(rset.getInt(1)), rset.getString(2));
                }
            }

            try {
                rset.close();
            } catch (SQLException e) {
            }

            try {
                stmt.close();
            } catch (SQLException e) {
            }
        } catch (SQLException e) {
            log().info("SQL Exception occured getting node map: " + e, e);
            throw new UndeclaredThrowableException(e);
        } catch (Exception e) {
            log().fatal("Exception getting database connection: " + e, e);
            throw new UndeclaredThrowableException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }

        return resultMap;
    }
    
    private final Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void afterPropertiesSet() {
        Assert.state(m_dataSource != null, "property dataSource cannot be null");
        Assert.state(m_databaseSchemaConfigFactory != null, "property databaseSchemaConfigFactory cannot be null");
    }
    
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public DataSource getDataSource() {
        return m_dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }
    
    public void setDatabaseSchemaConfigFactory(DatabaseSchemaConfigFactory factory) {
        m_databaseSchemaConfigFactory = factory;
    }

    public DatabaseSchemaConfigFactory getDatabaseSchemaConfigFactory() {
        return m_databaseSchemaConfigFactory;
    }
    
    public Map<String, Set<String>> getIPServiceMap(String rule) {
        Map<String, Set<String>> ipServices = new TreeMap<String, Set<String>>();

        // get the database connection
        Connection conn = null;
        try {
            conn = getDataSource().getConnection();

            // execute query and return the list of ip addresses
            Statement stmt = conn.createStatement();
            ResultSet rset = stmt.executeQuery(getIPServiceMappingStatement(rule));

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

            try {
                rset.close();
            } catch (SQLException e) {
            }

            try {
                stmt.close();
            } catch (SQLException e) {
            }
        } catch (SQLException e) {
            log().info("SQL Exception occured getting IP List: " + e, e);
            throw new UndeclaredThrowableException(e);
        } catch (Exception e) {
            log().fatal("Exception getting database connection: " + e, e);
            throw new UndeclaredThrowableException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }

        return ipServices;
    }

    /**
     * This method returns a list of all ip addresses that match the rule that
     * is passed in.
     * 
     * @param rule
     *            an expression rule to be parsed and executed.
     * 
     * @return interface containing all ip addresses selected by the rule.
     * 
     * @exception FilterParseException
     *                if a rule is syntactically incorrect or failed in
     *                executing the SQL statement
     */
    public List<String> getIPList(String rule) throws FilterParseException {
        List<String> resultList = new ArrayList<String>();
        String sqlString = null;

        // get the database connection
        Connection conn = null;
        try {
            conn = getDataSource().getConnection();

            // parse the rule and get the sql select statement
            sqlString = getSQLStatement(rule);
            if (log().isDebugEnabled()) {
                log().debug("Filter: SQL statement: \n" + sqlString);
            }

            // execute query and return the list of ip addresses
            Statement stmt = conn.createStatement();
            ResultSet rset = stmt.executeQuery(sqlString);

            // fill up the array list if the result set has values
            if (rset != null) {
                // Iterate through the result and build the array list
                while (rset.next()) {
                    resultList.add(rset.getString(1));
                }
            }

            try {
                rset.close();
            } catch (SQLException e) {
            }

            try {
                stmt.close();
            } catch (SQLException e) {
            }
        } catch (SQLException e) {
            log().info("SQL Exception occured getting IP List: " + e, e);
            throw new UndeclaredThrowableException(e);
        } catch (Exception e) {
            log().fatal("Exception getting database connection: " + e, e);
            throw new UndeclaredThrowableException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }

        return resultList;
    }

    /**
     * This method verifies if an ip address adhers to a given rule.
     * 
     * @param addr
     *            an octet string ip to be validated against a rule.
     * @param rule
     *            an expression rule to be parsed and executed.
     * 
     * @return indicating that the ip is included in the rule
     * 
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

    public void validateRule(String rule) throws FilterParseException {
        getSQLStatement(rule);
    }
    
    /**
     * This method is used to parse and valiate a rule into its graph tree. If
     * the parser cannot validate the rule then an exception is generated.
     * 
     * @param rule
     *            The rule to parse.
     * 
     * @throws FilterParseException
     *             Thrown if the rule cannot be parsed.
     */
    private Start parseRule(String rule) throws FilterParseException {
        if (rule != null && rule.length() > 0) {
            try {
                // Create a Parser instance.
                Parser p = new Parser(new Lexer(new PushbackReader(new StringReader(rule))));

                // Parse the input.
                return p.parse();
            } catch (Exception e) {
                log().error("Failed to parse the filter rule '" + rule + "': " + e, e);
                throw new FilterParseException("Parse error in rule '" + rule + "': " + e, e);
            }
        } else {
            throw new FilterParseException("Parse error: rule is null or empty");
        }
    }
    
    /**
     * This method parses a rule and returns the SQL select statement equivalent
     * of the rule.
     * 
     * @return the sql select statement
     */
    protected String getSQLStatement(String rule) {
        Start parseTree = parseRule(rule);
        SQLTranslation translation = new SQLTranslation(parseTree, getDatabaseSchemaConfigFactory());
        return translation.getStatement();
    }

    protected String getSQLStatement(String rule, long nodeId, String ipaddr, String service) {
        Start parseTree = parseRule(rule);
        SQLTranslation translation = new SQLTranslation(parseTree, getDatabaseSchemaConfigFactory());
        translation.setConstraintTranslation(nodeId, ipaddr, service);
        return translation.getStatement();
    }

    protected String getIPServiceMappingStatement(String rule) {
        Start parseTree = parseRule(rule);
        SQLTranslation translation = new SQLTranslation(parseTree, getDatabaseSchemaConfigFactory());
        translation.setIPServiceMappingTranslation();
        return translation.getStatement();
    }

    protected String getNodeMappingStatement(String rule) {
        Start parseTree = parseRule(rule);
        SQLTranslation translation = new SQLTranslation(parseTree, getDatabaseSchemaConfigFactory());
        translation.setNodeMappingTranslation();
        return translation.getStatement();
    }

    protected String getInterfaceWithServiceStatement(String rule) {
        Start parseTree = parseRule(rule);
        SQLTranslation translation = new SQLTranslation(parseTree, getDatabaseSchemaConfigFactory());
        translation.setInterfaceWithServiceTranslation();
        return translation.getStatement();
    }
    
    public boolean isRuleMatching(String rule) {
        Start parseTree = parseRule(rule);
        SQLTranslation translation = new SQLTranslation(parseTree, getDatabaseSchemaConfigFactory());
        translation.setLimitCount(1);
        
        String sql = translation.getStatement();
        
        Connection conn = null;
        
        try {
            conn = getDataSource().getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rows = stmt.executeQuery(sql);
            
            /**
             * We only want to check if zero or one rows were fetched, so just
             * return the output from rows.next(); 
             */
            boolean matches = rows.next();
    
            try {
                rows.close();
            } catch (SQLException e) {
            }
    
            try {
                stmt.close();
            } catch (SQLException e) {
            }
            
            if (log().isDebugEnabled()) {
                log().debug("isRuleMatching: rule \"" + rule + "\" does " + (matches ? "" : "not ") + "match an entry in the database; converted to SQL: " + sql);
            }

            return matches;
        } catch (SQLException e) {
            log().info("SQL Exception occured query results: " + e, e);
            throw new UndeclaredThrowableException(e);
        } catch (Exception e) {
            log().fatal("Exception getting database connection: " + e, e);
            throw new UndeclaredThrowableException(e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                }
            }
        }
    }

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
}
