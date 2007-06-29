//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 29: Add the ability to set a limit on the number of rows returned. - dj@opennms.org
// 2006 Aug 15: Throw more specific exceptions in the static initializer - dj@opennms.org
// 2006 Apr 25: Added setNodeMappingTranslation()
// 2003 Aug 01: Created a proper Join for rules. Bug #752
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;
import org.opennms.netmgt.config.filter.Table;
import org.opennms.netmgt.filter.analysis.DepthFirstAdapter;
import org.opennms.netmgt.filter.node.AAndRule;
import org.opennms.netmgt.filter.node.ACompareExprPart;
import org.opennms.netmgt.filter.node.AExprParensExpr;
import org.opennms.netmgt.filter.node.AGtrThanEqualExprPart;
import org.opennms.netmgt.filter.node.AGtrThanExprPart;
import org.opennms.netmgt.filter.node.AIdentExprPart;
import org.opennms.netmgt.filter.node.AIntegerCompareRight;
import org.opennms.netmgt.filter.node.AIntegerOctet;
import org.opennms.netmgt.filter.node.AIpaddrIpIdent;
import org.opennms.netmgt.filter.node.AIplikeExprPart;
import org.opennms.netmgt.filter.node.ALessThanEqualExprPart;
import org.opennms.netmgt.filter.node.ALessThanExprPart;
import org.opennms.netmgt.filter.node.ALikeExprPart;
import org.opennms.netmgt.filter.node.ANotEqualExprPart;
import org.opennms.netmgt.filter.node.ANotExprPart;
import org.opennms.netmgt.filter.node.AOctetListOctet;
import org.opennms.netmgt.filter.node.AOctetRangeListOctet;
import org.opennms.netmgt.filter.node.AOctetRangeOctet;
import org.opennms.netmgt.filter.node.AOrRule;
import org.opennms.netmgt.filter.node.AStarOctet;
import org.opennms.netmgt.filter.node.AStringCompareRight;
import org.opennms.netmgt.filter.node.ATildelikeExprPart;
import org.opennms.netmgt.filter.node.Start;

/**
 * This class is responsible for mapping the different parts of the filter
 * expressions into the SQL equivalent. As pieces of the expression are parsed
 * the from and where clauses will be built. This information will be passed on
 * to a SQLConstruct object by the parser when the expression has been fully
 * parsed.
 * 
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public class SQLTranslation extends DepthFirstAdapter {
    private DatabaseSchemaConfigFactory m_schemaFactory;

    /**
     * Constant to identify a virtual column for determining if an interface
     * supports a service
     */
    public static final String VIRTUAL_COLUMN_PREFIX = "is";

    /**
     * Constant to identify a virtual column for determining if an interface
     * supports a service
     */
    public static final String VIRTUAL_NOT_COLUMN_PREFIX = "notis";

    /**
     * Constant to identify a virtual column for determining if an interface
     * is in a particular category.
     */
    public static final String VIRTUAL_CATINC_PREFIX = "catinc";

    /**
     * The list of tables required to create the approriate SQL statement
     */
    private List<Table> m_tables;

    /**
     * The list of columns to be returned by the SQL.
     */
    private List<String> m_selectList;

    /**
     * A modifier on the selectList (like 'DISTINCT')
     */
    private String m_selectModifier;

    /**
     * The from part of the clause
     */
    private StringBuffer m_from;

    /**
     * The where part of the clause
     */
    private StringBuffer m_where;

    /**
     * The last ip address, this rebuild on a call to inAipaddr
     */
    private StringBuffer m_ipaddr;

    /**
     * The starting node of the parse tree
     */
    private Start m_node;
    
    /**
     * The limit count for the filter, or null if there is no limit.
     */
    private Integer m_limitCount = null;

    /**
     * This method is used to build the join condtions to be added to the where
     * clause of a complete select statement. A join condition will only be
     * built between two tables if the table being looked at has a &lt;join&gt;
     * reference indicating a join condition between itself and the driver
     * table.
     * 
     * @return The completed join clause for the WHERE clause
     */
    private String constructJoin() {
        StringBuffer joinBuf = new StringBuffer();
        Iterator<Table> i = m_tables.iterator();
        while (i.hasNext()) {
            Table t = (Table) i.next();
            String expr = m_schemaFactory.constructJoinExprForTable(t);
            if (expr != null && !"".equals(expr)) {
                joinBuf.append(" AND ");
                joinBuf.append(expr);
            }
        }
        return joinBuf.toString();
    }

    /**
     * Validate the identifier by ensuring it is references in the schema. Also
     * checks for 'virtual columns' be checking the prefix. If it is this turns
     * into a reference to the 'serviceName' column of the service table and the
     * appropriate join.
     */
    private String validateIdent(String ident) {
        String expr = null;
        Table tableForIdent = m_schemaFactory.findTableByVisableColumn(ident);
        if (tableForIdent != null)
            expr = addColumnToStatement(tableForIdent, ident);

        if (expr == null && ident.startsWith(VIRTUAL_COLUMN_PREFIX)) {
            String serviceName = ident.substring(VIRTUAL_COLUMN_PREFIX.length());
            // should check against some form of
            // service identifier table, but for now I'm
            // removing this check since it's just used
            // internally
            //
            tableForIdent = m_schemaFactory.findTableByVisableColumn("serviceName");
            if (tableForIdent != null)
                expr = addColumnToStatement(tableForIdent, "serviceName");
            if (expr != null)
                expr = expr + " = '" + serviceName + '\'';
        }

        if (expr == null && ident.startsWith(VIRTUAL_NOT_COLUMN_PREFIX)) {
            String serviceName = ident.substring(VIRTUAL_NOT_COLUMN_PREFIX.length());
            // should check against some form of
            // service identifier table, but for now I'm
            // removing this check since it's just used
            // internally
            //
            tableForIdent = m_schemaFactory.findTableByVisableColumn("ipAddr");
            if (tableForIdent != null)
                expr = addColumnToStatement(tableForIdent, "ipAddr");
            if (expr != null)
                expr = expr + " not in (select ipaddr from ifservices,service where service.serviceName ='"+ serviceName + "' and service.serviceID = ifServices.serviceid)";
        }

        if (expr == null && ident.startsWith(VIRTUAL_CATINC_PREFIX)) {
            String categoryName = ident.substring(VIRTUAL_CATINC_PREFIX.length());
            // 
            // This is a kludge to get Alex's categories working
            //
            tableForIdent = m_schemaFactory.findTableByVisableColumn("nodeID");
            if (tableForIdent != null)
                expr = addColumnToStatement(tableForIdent, "nodeID");
            if (expr != null)
                expr = expr + " in (select nodeid from category_node, categories where categories.categoryID = category_node.categoryID AND categories.categoryName = '"+ categoryName + "')";
        }

        if (expr == null) {
            throw new FilterParseException("The token " + ident + " is an illegal column value.");
        }

        return expr;
    }

    /**
     * Adds a column to the statement. This means insuring that this column is
     * valid, its table is listed in the m_tables list as well as any
     * intermediate tables necessary to join this table to the primary table. It
     * returns the tablename.colname used to reference this column in the SQL.
     * 
     * @param colName
     *            the name of the column to add
     * @return the 'tablename.column' expression used to reference the column in
     *         SQL.
     */
    private String addColumnToStatement(String colName) {
        Table t = m_schemaFactory.findTableByVisableColumn(colName);
        if (t == null)
            throw new FilterParseException("Could not find the column '" + colName + "' in the database schema");
        return addColumnToStatement(t, colName).toString();
    }

    /**
     * Adds a column to the statement. The column is assumed to be in the passed
     * in table. This means insuring that this column is valid, its table is
     * listed in the m_tables list as well as any intermediate tables necessary
     * to join this table to the primary table. It returns the tablename.colname
     * used to reference this column in the SQL.
     * 
     * @param t
     *            A Table that must contain the column colName
     * @param colName
     *            the name of the column to add
     * @return the 'tablename.column' expression used to reference the column in
     *         SQL.
     * 
     * @param t
     * @param colName
     * @return a string represtenting the part of the where portion of the
     *         clause for this column
     */
    private String addColumnToStatement(Table t, String colName) {
        String[] joinTableNames = m_schemaFactory.getJoinTablesForTable(t);
        for (int i = 0; i < joinTableNames.length; i++) {
            Table joinTable = m_schemaFactory.getTableByName(joinTableNames[i]);
            if (joinTable == null)
                throw new FilterParseException("Unable to locate visable table for " + joinTableNames[i] + " referrenced in join for table " + t.getName());
            if (!m_tables.contains(joinTable)) {
                if (m_tables.size() == 0)
                    m_from.append(joinTable.getName());
                else
                    m_from.append(", ").append(joinTable.getName());

                m_tables.add(joinTable);
            }
        }
        return t.getName() + "." + colName;
    }

    /**
     * This method removes any double quote characters from the start and end of
     * a string and replaces them with single quotes.
     * 
     * @param string
     *            the string to replace quote characters in
     * 
     */
    private String convertString(String string) {
        // for a string we need to change any encapsulating double
        // quotes to single quotes
        //
        StringBuffer buffer = new StringBuffer(string);
        buffer.setCharAt(0, '\'');
        buffer.setCharAt(buffer.length() - 1, '\'');

        return buffer.toString();
    }

    /**
     * This method checks to ensure that a number appearing in an IP address is
     * within the 0-255 range.
     * 
     * @param octet
     *            an integer from an ip octet
     * 
     * @exception java.lang.IndexOutOfBoundsException
     */
    public void checkIPNum(String octet) {
        try {
            int ipnum = Integer.parseInt(octet);
            if (ipnum < 0 || ipnum > 255)
                throw new IndexOutOfBoundsException("The specified IP octet is not valid, value = " + octet);
        } catch (NumberFormatException e) {
            throw new IndexOutOfBoundsException("The specified IP octet is not valid, value = " + octet);
        }
    }

    public SQLTranslation(Start node, DatabaseSchemaConfigFactory databaseSchemaConfigFactory) {
        m_schemaFactory = databaseSchemaConfigFactory;
        
        m_node = node;

        m_selectList = new ArrayList<String>();

        m_from = new StringBuffer(" FROM ");

        m_where = new StringBuffer(" WHERE (");

        m_tables = new ArrayList<Table>(m_schemaFactory.getTableCount());
        setDefaultTranslation();
    }

    private String buildSelectClause() {
        StringBuffer clause = new StringBuffer("SELECT ");
        clause.append(m_selectModifier).append(" ");

        for (int i = 0; i < m_selectList.size(); i++) {
            clause.append((String) m_selectList.get(i)).append(i < m_selectList.size() - 1 ? ", " : "");
        }

        return clause.toString();
    }

    /**
     * Initializes the pieces of the SQL statement to perform a default query to
     * select distinct ip addresses based the query that is built from the rest
     * of the rule.
     */
    public void setDefaultTranslation() {

        m_selectModifier = "DISTINCT";

        m_selectList.clear();
        m_selectList.add(addColumnToStatement("ipAddr"));
    }

    public void setIPServiceMappingTranslation() {
        m_selectModifier = "";

        m_selectList.clear();
        m_selectList.add(addColumnToStatement("ipAddr"));
        m_selectList.add(addColumnToStatement("serviceName"));

    }

    public void setNodeMappingTranslation() {
        m_selectModifier = "DISTINCT";

        m_selectList.clear();
        m_selectList.add(addColumnToStatement("nodeid"));
        m_selectList.add(addColumnToStatement("nodelabel"));

    }

    /**
     * This method should be called if you want to put constraints on the node,
     * interface or service that is returned in the rule. This is useful to see
     * if a particular node, interface, or service matches in the rule, and is
     * primarily used to filter notices. A subselect is built containing joins
     * constrained by node, interface, and service if they are not null or
     * blank. This select is then anded with the filter rule to get the complete
     * SQL statement.
     * 
     * @param nodeId
     *            a node id to constrain against
     * @param ipaddr
     *            an ipaddress to constrain against
     * @param service
     *            a service name to constrain against
     */
    public void setConstraintTranslation(long nodeId, String ipaddr, String service) {
        m_selectModifier = "DISTINCT";

        m_selectList.clear();

        String ipAddrColumn = addColumnToStatement("ipAddr");
        m_selectList.add(ipAddrColumn);

        StringBuffer constraint = new StringBuffer();
        boolean needAnd = false;
        if (nodeId != 0) {
            if (needAnd)
                constraint.append(" AND ");
            String nodeIDColumn = addColumnToStatement("nodeID");
            constraint.append(nodeIDColumn).append(" = ").append(nodeId);
            needAnd = true;
        }

        if (ipaddr != null && !ipaddr.equals("")) {
            if (needAnd)
                constraint.append(" AND ");
            constraint.append(ipAddrColumn).append(" = '").append(ipaddr).append('\'');
            needAnd = true;
        }

        if (service != null && !service.equals("")) {
            String serviceColumn = addColumnToStatement("serviceName");
            if (needAnd)
                constraint.append(" AND ");
            constraint.append(serviceColumn).append(" = '").append(service).append('\'');
            needAnd = true;
        }

        m_where.append(constraint).append(") AND (");

    }

    public void setInterfaceWithServiceTranslation() {
        m_selectModifier = "DISTINCT";

        m_selectList.clear();
        m_selectList.add(addColumnToStatement("ipAddr"));
        m_selectList.add(addColumnToStatement("serviceName"));
        m_selectList.add(addColumnToStatement("nodeID"));
    }
    
    /**
     * Set a limit on the number of rows returned.
     */
    public void setLimitCount(Integer count) {
        m_limitCount = count;
    }

    public void outStart(Start node) {
        // finish the where clause by putting in the join clauses to
        // the ipinterface table, separating them from the rest of the
        // where clause
        //
        m_where.append(")" + constructJoin());
    }

    public void caseAAndRule(AAndRule node) {
        node.getRule().apply(this);
        m_where.append(" AND ");
        node.getExpr().apply(this);
    }

    public void caseAOrRule(AOrRule node) {
        node.getRule().apply(this);
        m_where.append(" OR ");
        node.getExpr().apply(this);
    }

    public void inAExprParensExpr(AExprParensExpr node) {
        m_where.append("( ");
    }

    public void outAExprParensExpr(AExprParensExpr node) {
        m_where.append(" )");
    }

    public void inAIdentExprPart(AIdentExprPart node) {
        m_where.append(validateIdent(node.getIdent().getText()));
    }

    public void caseAGtrThanExprPart(AGtrThanExprPart node) {
        m_where.append(validateIdent(node.getIdent().getText()));
        m_where.append(" > ");
        node.getCompareRight().apply(this);
    }

    public void caseALessThanExprPart(ALessThanExprPart node) {
        m_where.append(validateIdent(node.getIdent().getText()));
        m_where.append(" < ");
        node.getCompareRight().apply(this);
    }

    public void caseAGtrThanEqualExprPart(AGtrThanEqualExprPart node) {
        m_where.append(validateIdent(node.getIdent().getText()));
        m_where.append(" >= ");
        node.getCompareRight().apply(this);
    }

    public void caseALessThanEqualExprPart(ALessThanEqualExprPart node) {
        m_where.append(validateIdent(node.getIdent().getText()));
        m_where.append(" <= ");
        node.getCompareRight().apply(this);
    }

    public void caseACompareExprPart(ACompareExprPart node) {
        m_where.append(validateIdent(node.getIdent().getText()));
        m_where.append(" = ");
        node.getCompareRight().apply(this);
    }

    public void caseANotEqualExprPart(ANotEqualExprPart node) {
        m_where.append(validateIdent(node.getIdent().getText()));
        m_where.append(" <> ");
        node.getCompareRight().apply(this);
    }

    public void inANotExprPart(ANotExprPart node) {
        m_where.append("NOT ");
    }

    public void caseAIntegerCompareRight(AIntegerCompareRight node) {
        inAIntegerCompareRight(node);
        m_where.append(node.getInteger().getText());
        outAIntegerCompareRight(node);
    }

    public void caseAStringCompareRight(AStringCompareRight node) {
        inAStringCompareRight(node);
        if (node.getQuotedString() != null) {
            node.getQuotedString().apply(this);
            m_where.append(convertString(node.getQuotedString().getText()));
        }
        outAStringCompareRight(node);
    }

    public void caseALikeExprPart(ALikeExprPart node) {
        inALikeExprPart(node);
        if (node.getIdent() != null) {
            node.getIdent().apply(this);
            m_where.append(validateIdent(node.getIdent().getText()));
        }
        if (node.getLike() != null) {
            node.getLike().apply(this);
            m_where.append(" LIKE ");
        }
        if (node.getQuotedString() != null) {
            node.getQuotedString().apply(this);
            m_where.append(convertString(node.getQuotedString().getText()));
        }
        outALikeExprPart(node);
    }

    public void caseATildelikeExprPart(ATildelikeExprPart node) {
        inATildelikeExprPart(node);
        if (node.getIdent() != null) {
            node.getIdent().apply(this);
            m_where.append(validateIdent(node.getIdent().getText()));
        }
        if (node.getTildelike() != null) {
            node.getTildelike().apply(this);
            m_where.append(" ~ ");
        }
        if (node.getQuotedString() != null) {
            node.getQuotedString().apply(this);
            m_where.append(convertString(node.getQuotedString().getText()));
        }
        outATildelikeExprPart(node);
    }

    public void caseAIplikeExprPart(AIplikeExprPart node) {
        StringBuffer iplikeMethodCall = new StringBuffer("iplike(");

        inAIplikeExprPart(node);
        if (node.getIdent() != null) {
            node.getIdent().apply(this);
            iplikeMethodCall.append(validateIdent(node.getIdent().getText()));
        }
        if (node.getIplike() != null) {
            node.getIplike().apply(this);
        }
        if (node.getIpIdent() != null) {
            node.getIpIdent().apply(this);
            iplikeMethodCall.append(", '" + m_ipaddr + "')");
        }

        m_where.append(iplikeMethodCall.toString());

        outAIplikeExprPart(node);
    }

    public void caseAIpaddrIpIdent(AIpaddrIpIdent node) {
        m_ipaddr = new StringBuffer();

        inAIpaddrIpIdent(node);
        if (node.getOct1() != null) {
            node.getOct1().apply(this);
        }
        if (node.getDot1() != null) {
            node.getDot1().apply(this);
            m_ipaddr.append(node.getDot1().getText());
        }
        if (node.getOct2() != null) {
            node.getOct2().apply(this);
        }
        if (node.getDot2() != null) {
            node.getDot2().apply(this);
            m_ipaddr.append(node.getDot2().getText());
        }
        if (node.getOct3() != null) {
            node.getOct3().apply(this);
        }
        if (node.getDot3() != null) {
            node.getDot3().apply(this);
            m_ipaddr.append(node.getDot3().getText());
        }
        if (node.getOct4() != null) {
            node.getOct4().apply(this);
        }
        outAIpaddrIpIdent(node);
    }

    public void caseAStarOctet(AStarOctet node) {
        inAStarOctet(node);
        if (node.getStar() != null) {
            node.getStar().apply(this);
            m_ipaddr.append(node.getStar().getText());
        }
        outAStarOctet(node);
    }

    public void caseAOctetListOctet(AOctetListOctet node) {
        inAOctetListOctet(node);
        if (node.getOctetList() != null) {
            node.getOctetList().apply(this);

            // validate the list
            //
            StringTokenizer tokens = new StringTokenizer(node.getOctetList().getText(), ",");

            while (tokens.hasMoreTokens()) {
                checkIPNum(tokens.nextToken());
            }

            // append it to the address
            //
            m_ipaddr.append(node.getOctetList().getText());
        }
        outAOctetListOctet(node);
    }

    public void caseAOctetRangeOctet(AOctetRangeOctet node) {
        inAOctetRangeOctet(node);
        if (node.getOctetRange() != null) {
            node.getOctetRange().apply(this);

            // validate the list
            //
            StringTokenizer tokens = new StringTokenizer(node.getOctetRange().getText(), "-");

            while (tokens.hasMoreTokens()) {
                checkIPNum(tokens.nextToken());
            }

            // append it to the address
            //
            m_ipaddr.append(node.getOctetRange().getText());
        }
        outAOctetRangeOctet(node);
    }

    public void caseAOctetRangeListOctet(AOctetRangeListOctet node) {
        inAOctetRangeListOctet(node);
        if (node.getOctetRangeList() != null) {
            node.getOctetRangeList().apply(this);

            // validate the list
            //
            StringTokenizer listTokens = new StringTokenizer(node.getOctetRangeList().getText(), ",");
            StringTokenizer rangeTokens = new StringTokenizer(listTokens.nextToken());

            // check the range numbers
            //
            while (rangeTokens.hasMoreTokens()) {
                checkIPNum(rangeTokens.nextToken());
            }

            // check the list numbers
            //
            while (listTokens.hasMoreTokens()) {
                checkIPNum(listTokens.nextToken());
            }

            // append it to the address
            //
            m_ipaddr.append(node.getOctetRangeList().getText());
        }
        outAOctetRangeListOctet(node);
    }

    public void caseAIntegerOctet(AIntegerOctet node) {
        inAIntegerOctet(node);
        if (node.getInteger() != null) {
            node.getInteger().apply(this);
            checkIPNum(node.getInteger().toString().trim());
            m_ipaddr.append(node.getInteger().getText());
        }
        outAIntegerOctet(node);
    }

    /**
     * This method returns the complete sql statement for the filter that was
     * parsed. The SQL statement is the result of the select, from, and where
     * components assembled from the code.
     * 
     */
    public String getStatement() {
        // don't walk tree if there is no tree to walk
        if (m_node == null)
            return null;

        // this will walk the tree and build the rest of the sql statement
        //
        m_node.apply(this);

        return buildSelectClause() + m_from.toString() + m_where.toString()
            + buildLimitClause();
    }

    private String buildLimitClause() {
        if (m_limitCount != null) {
            return " LIMIT " + m_limitCount;
        } else {
            return "";
        }
    }
}
