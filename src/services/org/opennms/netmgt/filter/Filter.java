//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Aug 01: Created a proper Join for rules. Bug #752
// 2003 Apr 24: Allowed for null rules.
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Oct 30: Changed some filter code for notifications.
// 2002 Oct 15: Corrected filters on services.
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

package org.opennms.netmgt.filter;

import java.beans.PropertyVetoException;
import java.io.IOException;
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
import java.util.TreeMap;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.filter.lexer.Lexer;
import org.opennms.netmgt.filter.lexer.LexerException;
import org.opennms.netmgt.filter.node.Start;
import org.opennms.netmgt.filter.parser.Parser;
import org.opennms.netmgt.filter.parser.ParserException;

/**
 * This class is the main entry point for filtering the rules expressions. By
 * creating a Filter object the application can parse a rules expression and get
 * back the list of applicable ip addresses or verify if an ip address adhers to
 * a certain rule.
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public class Filter {
    /**
     * This is the parse tree produced by the parser
     */
    private Start m_parseTree;

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
    public void parseRule(String rule) throws FilterParseException {
        if (rule != null && rule.length() > 0) {
            try {
                // Create a Parser instance.
                Parser p = new Parser(new Lexer(new PushbackReader(new StringReader(rule))));

                // Parse the input.
                m_parseTree = p.parse();
            } catch (ParserException e) {
                Category log = ThreadCategory.getInstance(getClass());
                log.error("Failed to parse the filter rule: " + rule, e);
                throw new FilterParseException("Parse error in " + rule, e);
            } catch (LexerException e) {
                Category log = ThreadCategory.getInstance(getClass());
                log.error("Failed to parse filter rule: " + rule, e);
                throw new FilterParseException("Parse error in " + rule, e);
            } catch (IOException e) {
                Category log = ThreadCategory.getInstance(getClass());
                log.error("Failed to parse filter rule: " + rule, e);
                throw new FilterParseException("Parse error in " + rule, e);
            }
        } else {
            throw new FilterParseException("Parse error, rule is null or empty");
        }
    }

    /**
     * The default constructor. Initializes the filter rule to accept new input.
     */
    public Filter() {
        m_parseTree = null;
    }

    /**
     * Constructs a new filter with the pre-defined rule as the comparision
     * method.
     * 
     * @param rule
     *            The filter rule.
     * 
     * @exception FilterParseException,
     *                if a rule is syntactically incorrect or failed in
     *                executing the SQL statement.
     */
    public Filter(String rule) throws FilterParseException {
        m_parseTree = null;
        parseRule(rule);
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
        if (rule.length() == 0)
            return true;
        else
            // see if the ip address is contained in the list that the
            // rule returns
            //
            return getIPList(rule).contains(addr);
    }

    public Map getIPServiceMap(String rule) {
        Map ipServices = new TreeMap();

        // parse the rule
        parseRule(rule);
        // return getIPServiceMappingStatement();

        // get the database connection
        Connection conn = null;
        try {
            DatabaseConnectionFactory.init();
            conn = DatabaseConnectionFactory.getInstance().getConnection();

            // execute query and return the list of ip addresses
            //
            Statement stmt = conn.createStatement();
            ResultSet rset = stmt.executeQuery(getIPServiceMappingStatement());

            // fill up the array list if the result set has values
            //
            if (rset != null) {
                // Iterate through the result and build the array list
                while (rset.next()) {
                    String ipaddr = rset.getString(1);

                    if (ipServices.containsKey(ipaddr)) {
                        Map services = (Map) ipServices.get(ipaddr);
                        services.put(rset.getString(2), null);
                    } else {
                        Map services = new TreeMap();
                        services.put(rset.getString(2), null);
                        ipServices.put(ipaddr, services);
                    }
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
            Category log = ThreadCategory.getInstance(getClass());
            log.info("SQL Exception occured getting IP List", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException ie) {
            Category log = ThreadCategory.getInstance(getClass());
            log.fatal("IOException getting database connection", ie);
            throw new UndeclaredThrowableException(ie);
        } catch (MarshalException me) {
            Category log = ThreadCategory.getInstance(getClass());
            log.fatal("Marshall Exception getting database connection", me);
            throw new UndeclaredThrowableException(me);
        } catch (ValidationException ve) {
            Category log = ThreadCategory.getInstance(getClass());
            log.fatal("Validation Exception getting database connection", ve);
            throw new UndeclaredThrowableException(ve);
        } catch (PropertyVetoException ve) {
            Category log = ThreadCategory.getInstance(getClass());
            log.fatal("Property Veto Exception getting database connection", ve);
            throw new UndeclaredThrowableException(ve);
        } catch (ClassNotFoundException e) {
            Category log = ThreadCategory.getInstance(getClass());
            log.fatal("Class Not Found Exception getting database connection", e);
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
    public List getIPList(String rule) throws FilterParseException {
        Category log = ThreadCategory.getInstance(getClass());
        List resultList = new ArrayList();
        String sqlString = null;

        // parse the rule
        parseRule(rule);

        // get the database connection
        Connection conn = null;
        try {
            DatabaseConnectionFactory.init();
            conn = DatabaseConnectionFactory.getInstance().getConnection();

            // parse the rule and get the sql select statement
            //
            sqlString = getSQLStatement();
            if (log.isDebugEnabled())
                log.debug("Filter: SQL statement: \n" + sqlString);

            // execute query and return the list of ip addresses
            //
            Statement stmt = conn.createStatement();
            ResultSet rset = stmt.executeQuery(sqlString);

            // fill up the array list if the result set has values
            //
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

        } catch (ClassNotFoundException e) {
            log.info("Class Not Found Exception occured getting IP List", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            log.info("SQL Exception occured getting IP List", e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException ie) {
            log.fatal("IOException getting database connection", ie);
            throw new UndeclaredThrowableException(ie);
        } catch (MarshalException me) {
            log.fatal("Marshall Exception getting database connection", me);
            throw new UndeclaredThrowableException(me);
        } catch (ValidationException ve) {
            log.fatal("Validation Exception getting database connection", ve);
            throw new UndeclaredThrowableException(ve);
        } catch (PropertyVetoException ve) {
            log.fatal("Property Veto Exception getting database connection", ve);
            throw new UndeclaredThrowableException(ve);
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
     * This method parses a rule and returns the SQL select statement equivalent
     * of the rule.
     * 
     * @return the sql select statement
     */
    public String getSQLStatement() {
        SQLTranslation translation = new SQLTranslation(m_parseTree);
        return translation.getStatement();
    }

    public String getSQLStatement(long nodeId, String ipaddr, String service) {
        SQLTranslation translation = new SQLTranslation(m_parseTree);
        translation.setConstraintTranslation(nodeId, ipaddr, service);
        return translation.getStatement();
    }

    public String getIPServiceMappingStatement() {
        SQLTranslation translation = new SQLTranslation(m_parseTree);
        translation.setIPServiceMappingTranslation();
        return translation.getStatement();
    }

    public String getInterfaceWithServiceStatement() {
        SQLTranslation translation = new SQLTranslation(m_parseTree);
        translation.setInterfaceWithServiceTranslation();
        return translation.getStatement();
    }

    /**
     * This method is used to validate that a rule is syntactically correct. The
     * method will throw an exception on an invalid parse and will not do
     * anything in the case of a good parse. The FilterParseException holds the
     * error message of the failure.
     * 
     * @param rule
     *            The expression rule to validate
     * 
     * @exception FilterParseException
     */
    public void validateRule(String rule) throws FilterParseException {
        parseRule(rule);
        getSQLStatement();
    }
}