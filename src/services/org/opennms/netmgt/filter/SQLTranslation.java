//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.sortova.com/
//
package org.opennms.netmgt.filter;

import java.lang.*;
import java.io.IOException;

import java.lang.reflect.UndeclaredThrowableException;

import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Iterator;

import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.MarshalException;

import org.opennms.netmgt.filter.node.*;
import org.opennms.netmgt.filter.analysis.*;

import org.opennms.netmgt.config.DatabaseSchemaConfigFactory;

// castor generated classes - from database-schema.xsd
import org.opennms.netmgt.config.filter.*;

/**
 * This class is responsible for mapping the
 * different parts of the filter expressions into
 * the SQL equivalent. As pieces of the expression are
 * parsed the from and where clauses will be built. This
 * information will be passed on to a SQLConstruct object
 * by the parser when the expression has been fully parsed.
 *
 * @author <A HREF="mailto:jason@opennms.org">Jason Johns</A>
 * @author <A HREF="mailto:weave@opennms.org">Weave</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 *
 */
public class SQLTranslation
	extends DepthFirstAdapter
{
	/**
	 * Constant to identify a virtual column for determining if an
	 * interface supports a service
	 */
	public static final String 		VIRTUAL_COLUMN_PREFIX = "is";

	/**
	 * The singular loaded configuration of the database schema. This is loaded when
	 * the class is initialized.
	 */
	private static final DatabaseSchema	m_schema;

	/**
	 * The list of tables required to create the approriate SQL statement
	 */
	private List		m_tables;

	/**
	 * The select part of the clause
	 */
	private StringBuffer 	m_select;
        
        /**
         *
         */
        private List m_selectList;
        
        /**
         *
         */
        private String m_selectModifier;
    
    	/**
	 * The from part of the clause
	 */
	private StringBuffer 	m_from;
    
    	/**
	 * The where part of the clause
	 */
	private StringBuffer 	m_where;
    
    	/**
	 * The last ip address, this rebuild on a call to inAipaddr
	 */
	private StringBuffer 	m_ipaddr;

        /**
         * The starting node of the parse tree
         */
        private Start m_node;
        
	// Statically load the configuration resource.
	//
	static
	{
		try
		{
			DatabaseSchemaConfigFactory.init();
			m_schema = DatabaseSchemaConfigFactory.getInstance().getDatabaseSchema();
		}
		catch(MarshalException ex)
		{
			throw new UndeclaredThrowableException(ex);
		}
		catch(ValidationException ex)
		{
			throw new UndeclaredThrowableException(ex);
		}
		catch(IOException ex)
		{
			throw new UndeclaredThrowableException(ex);
		}
	}

	/**
	 * This method is used to find the table that should
	 * drive the construction of the join clauses between
         * all table in the from clause. At least one table
         * has to be designated as the driver table.
	 *
         * @return The name of the driver table
         */
	private Table getPrimaryTable()
	{
		Enumeration e = m_schema.enumerateTable();
		while(e.hasMoreElements())
		{
			Table t = (Table)e.nextElement();
			if(t.getVisable() == null || t.getVisable().equalsIgnoreCase("true"))
			{
				if(t.getKey() != null && t.getKey().equals("primary"))
				{
					return t;
				}
			}
		}
		return null;
	}
	 
	/**
	 * This method is used to build the join condtions to be 
         * added to the where clause of a complete select statement.
         * A join condition will only be built between two tables if
         * the table being looked at has a &lt;join&gt; reference indicating
         * a join condition between itself and the driver table.
	 * (NOTE: in the case of the virtual columns in the ifServices
	 * table the join condiontion will be included in the substitution.
	 * The service table will not be joined to the driver table by
	 * this method).
	 *
	 * @return The completed join clause for the WHERE clause
	 */
	private String constructJoin()
	{
		//first we need to find the driver table
		//
		Table primary = getPrimaryTable();

		StringBuffer joinBuf = new StringBuffer();
		Iterator i = m_tables.iterator();
		while(i.hasNext())
		{
			Table t = (Table)i.next();
			if(!t.equals(primary))
			{
				Enumeration ejoin = t.enumerateJoin();
				while(ejoin.hasMoreElements())
				{
					Join j = (Join)ejoin.nextElement();
					if(j.getTable().equals(primary.getName()))
					{
						joinBuf.append(" AND ");
						joinBuf.append(t.getName()).append('.').append(j.getColumn());
						joinBuf.append(" = ");
						joinBuf.append(j.getTable()).append('.').append(j.getTableColumn());
					}
				}
			}
		}
		return joinBuf.toString();
	}

	/**
	 * Find the appropriate table
	 */
    	private Table findTableByVisableColumn(String cname)
	{
		Table table = null;

		Enumeration etbl = m_schema.enumerateTable();
		OUTER: while(etbl.hasMoreElements())
		{
			Table t = (Table)etbl.nextElement();
			Enumeration ecol = t.enumerateColumn();
			while(ecol.hasMoreElements())
			{
				Column col = (Column)ecol.nextElement();
				if(col.getVisable() == null || col.getVisable().equalsIgnoreCase("true"))
				{
					if(col.getName().equalsIgnoreCase(cname))
					{
						table = t;
						break OUTER;
					}
				}
			}
		}

		return table;
	}

	/**
	 * Validate the identifier.
	 */
    	private String validateIdent(String ident)
	{
		StringBuffer buffer = null;
		Enumeration etbl = m_schema.enumerateTable();
		OUTER: while(etbl.hasMoreElements())
		{
			Table t = (Table)etbl.nextElement();
			Enumeration ecol = t.enumerateColumn();
			while(ecol.hasMoreElements())
			{
				Column col = (Column)ecol.nextElement();
				if(col.getVisable() == null || col.getVisable().equalsIgnoreCase("true"))
				{
					if(col.getName().equalsIgnoreCase(ident))
					{
						if(!m_tables.contains(t))
						{
							if(m_tables.size() == 0)
								m_from.append(t.getName());
							else
								m_from.append(", ").append(t.getName());

							m_tables.add(t);
						}
						buffer = new StringBuffer(t.getName()).append('.').append(ident);
						break OUTER;
					}
				}
			}
		}

		if(buffer == null && ident.startsWith(VIRTUAL_COLUMN_PREFIX))
		{
			// should check against some form of 
			// service identifier table, but for now I'm 
			// removing this check since it's just used
			// internally
			//
			buffer = buildVirtualColumnClause(ident);
		}
		else if(buffer == null)
		{
			throw new FilterParseException("The token " + ident + " is an illegal column value.");
		}

		return buffer.toString();
	}
	
	/**
	 * This method is used to build a <em>sub-select</em> clause based
	 * upon the passed virtual identifier. A virtual identifier is
	 * one that starts with <em>is</em>, such as <tt>isHTTP</tt> that
	 * represents the available of HTTP services.
	 *
	 * @return The defined sub-select statement.
	 *
	 */
	private StringBuffer buildVirtualColumnClause(String ident)
	{
		StringBuffer buffer = new StringBuffer();
		
		//buffer.append("ipInterface.ipAddr in (SELECT ipInterface.ipAddr");
                buffer.append(" ( ");
                for (int i = 0; i < m_selectList.size(); i++)
                {
                        buffer.append( (String)m_selectList.get(i)).append( i < m_selectList.size()-1 ? ", " : "");
                }
                buffer.append(") in (SELECT ");
                for (int i = 0; i < m_selectList.size(); i++)
                {
                        buffer.append( (String)m_selectList.get(i)).append( i < m_selectList.size()-1 ? ", " : "");
                }
                
                buffer.append(" FROM ipInterface, ifServices, service");
		buffer.append(" WHERE ifServices.serviceID = service.serviceID");
		buffer.append(" AND service.serviceName = '");
		buffer.append(ident.substring(VIRTUAL_COLUMN_PREFIX.length()));
		buffer.append("' AND    ipInterface.ipAddr = ifServices.ipAddr)");
		
		return buffer;
	}
	
	/**
	 * This method removes any double quote characters from the
	 * start and end of a string and replaces them with single quotes.
	 *
	 * @param String aString, the string to replace quote characters in
	 */
	private String convertString(String string)
	{
		//for a string we need to change any encapsulating double
		//quotes to single quotes
		//
		StringBuffer buffer = new StringBuffer(string);
		buffer.setCharAt(0, '\''); 
		buffer.setCharAt(buffer.length()-1, '\'');
		
		return buffer.toString();
	}
	
	/**
	 * This method checks to ensure that a number appearing 
         * in an IP address is within the 0-255 range.
	 *
	 * @param octet an integer from an ip octet
	 *
	 * @exception java.lang.IndexOutOfBoundsException
         */
	public void checkIPNum(String octet)
	{
		try
		{
			int ipnum = Integer.parseInt(octet);
			if(ipnum < 0 || ipnum > 255) 
				throw new IndexOutOfBoundsException("The specified IP octet is not valid, value = " + octet);
		}
		catch(NumberFormatException e)
		{
			throw new IndexOutOfBoundsException("The specified IP octet is not valid, value = " + octet);
		}
	}

	/**
	 * Default constructor, initalizes the data structures
	 */
	public SQLTranslation(Start node)
	{
		m_node = node;
                //m_select = new StringBuffer("SELECT ");
                m_selectList = new ArrayList();
                
                m_from = new StringBuffer(" FROM ");
                
                m_where = new StringBuffer(" WHERE (");
                
                m_tables = new ArrayList(m_schema.getTableCount());
                setDefaultTranslation();
        }
        
        private String buildSelectClause()
        {
                StringBuffer clause = new StringBuffer("SELECT " );
                clause.append(m_selectModifier).append(" ");
                
                for (int i = 0; i < m_selectList.size(); i++)
                {
                        clause.append( (String)m_selectList.get(i)).append( i < m_selectList.size()-1 ? ", " : "");
                }
                
                return clause.toString();
        }
        
        /**
         * Initializes the pieces of the SQL statement to perform a default query to
         * select distinct ip addresses based the query that is built from the rest of the
         * rule.
         */
        public void setDefaultTranslation()
        {
                //m_select = new StringBuffer("SELECT DISTINCT ipInterface.ipAddr");
		//m_from   = new StringBuffer(" FROM ");
		//m_where  = new StringBuffer(" WHERE (");
                
                m_selectList.add("ipInterface.ipAddr");
                m_selectModifier = "DISTINCT";
                
		Table t = findTableByVisableColumn("ipAddr");
		m_from.append(t.getName());
		m_tables.add(t);
        }
        
        /**
         *
         */
        public void setIPServiceMappingTranslation()
        {
                //m_select = new StringBuffer("SELECT ipinterface.ipaddr, service.serviceName");
                m_selectList.clear();
                m_selectModifier = "";
                m_selectList.add("ipInterface.ipAddr");
                m_selectList.add("service.serviceName");
                
		m_from   = new StringBuffer(" FROM ipinterface, ifservices, service");
		m_where  = new StringBuffer(" WHERE ( ipinterface.ipaddr = ifservices.ipaddr AND ifServices.serviceid = service.serviceid AND "); //ipinterface.ipaddr = ifservices.ipaddr AND ifServices.serviceid = service.serviceid AND ");
        }
        
        /**
	 * This method should be called if you want to put constraints on the node,
	 * interface or service that is returned in the rule. This is useful to see if a 
	 * particular node, interface, or service matches in the rule, and is primarily used
	 * to filter notices. A subselect is built containing joins constrained by node, interface,
	 * and service if they are not null or blank. This select is then anded with the filter rule
	 * to get the complete SQL statement.
	 * @param nodeId, a node id to constrain against
	 * @param ipaddr, an ipaddress to constrain against
	 * @param service, a service name to constrain against
	 */
        public void setConstraintTranslation(long nodeId, String ipaddr, String service)
        {
                //m_select = new StringBuffer("SELECT DISTINCT ipInterface.ipAddr");
		m_selectList.add("ipInterface.ipAddr");
                m_selectModifier = "DISTINCT";
                
                m_from   = new StringBuffer(" FROM ");
		m_where  = new StringBuffer(" WHERE ");
		
		StringBuffer subSelect = new StringBuffer("ipInterface.ipAddr in (SELECT ipInterface.ipAddr");
		StringBuffer subFrom   = new StringBuffer(" FROM ipinterface");
		StringBuffer subWhere  = new StringBuffer(" WHERE ");
		boolean subQueryRequired = false;
		boolean needAnd = false;
		if (nodeId != 0)
		{
			subFrom.append(", node");
			subWhere.append("(node.nodeId = ipInterface.nodeId AND node.nodeid = " + nodeId + ") ");
			subQueryRequired = true;
			needAnd = true;
		}
		
		if (ipaddr != null && !ipaddr.equals(""))
		{
			//already added ipinterface table to subselect
			subWhere.append( (needAnd ? " AND " : " ")).append("ipInterface.ipAddr = '" + ipaddr + "' ");
			subQueryRequired = true;
			needAnd = true;
		}
		
		if (service != null && !service.equals(""))
		{
			subFrom.append(", ifservices, service");
			subWhere.append( (needAnd ? " AND " : " ")).append("(ifServices.serviceID = service.serviceID AND service.serviceName = '" + service + "'");
			subWhere.append(" AND ipInterface.ipAddr = ifServices.ipAddr)");
			subQueryRequired = true;
			needAnd = true;
		}
		
		m_where.append("(");
		
		if (subQueryRequired)
		{
			m_where.append(subSelect.toString()).append(subFrom.toString()).append(subWhere.toString()).append(") AND ");
		}
		
		Table t = findTableByVisableColumn("ipAddr");
		m_from.append(t.getName());
		m_tables.add(t);
        }

	public void addServiceToSelect()
	{
		m_selectList.add("service.serviceName");
		
		m_from = new StringBuffer(" FROM ipinterface, ifservices, service");
		m_where = new StringBuffer(" WHERE ");

		m_where.append("( (ifservices.serviceID = service.serviceID AND ipInterface.ipAddr = ifServices.ipAddr) AND ");
	}

        public void addNodeIDToSelect()
        {
                m_selectList.add("ipinterface.nodeid");
        }

    	public void outStart(Start node)
	{
		//finish the where clause by putting in the join clauses to 
		//the ipinterface table, separating them from the rest of the
		//where clause
		//
		m_where.append(")" + constructJoin());
	}
	
    	public void addJoin()
	{
		//finish the where clause by putting in the join clauses to 
		//the ipinterface table, separating them from the rest of the
		//where clause
		//
		m_where.append(constructJoin());
	}
	
	public void caseAAndRule(AAndRule node)
	{
		node.getRule().apply(this);
		m_where.append(" AND ");
		node.getExpr().apply(this);
	}
	
	public void caseAOrRule(AOrRule node)
	{
		node.getRule().apply(this);
		m_where.append(" OR ");
		node.getExpr().apply(this);
	}
	
	public void inAExprParensExpr(AExprParensExpr node)
	{
		m_where.append("( ");
	}
	
	public void outAExprParensExpr(AExprParensExpr node)
	{
		m_where.append(" )");
	}
	
	public void inAIdentExprPart(AIdentExprPart node)
	{
		m_where.append(validateIdent(node.getIdent().getText()));
	}
	
	public void caseAGtrThanExprPart(AGtrThanExprPart node)
	{
		m_where.append(validateIdent(node.getIdent().getText()));
		m_where.append(" > ");
		node.getCompareRight().apply(this);
	}
	
	public void caseALessThanExprPart(ALessThanExprPart node)
	{
		m_where.append(validateIdent(node.getIdent().getText()));
		m_where.append(" < ");
		node.getCompareRight().apply(this);
	}
	
	public void caseAGtrThanEqualExprPart(AGtrThanEqualExprPart node)
	{
		m_where.append(validateIdent(node.getIdent().getText()));
		m_where.append(" >= ");
		node.getCompareRight().apply(this);
	}
	
	public void caseALessThanEqualExprPart(ALessThanEqualExprPart node)
	{
		m_where.append(validateIdent(node.getIdent().getText()));
		m_where.append(" <= ");
		node.getCompareRight().apply(this);
	}
	
	public void caseACompareExprPart(ACompareExprPart node)
	{
		m_where.append(validateIdent(node.getIdent().getText()));
		m_where.append(" = ");
		node.getCompareRight().apply(this);
	}
	
	public void caseANotEqualExprPart(ANotEqualExprPart node)
	{
		m_where.append(validateIdent(node.getIdent().getText()));
		m_where.append(" <> ");
		node.getCompareRight().apply(this);
	}
	
	public void inANotExprPart(ANotExprPart node)
	{
		m_where.append("NOT ");
	}
	
	public void caseAIntegerCompareRight(AIntegerCompareRight node)
	{
		inAIntegerCompareRight(node);
		m_where.append(node.getInteger().getText());
		outAIntegerCompareRight(node);
	}
	
	public void caseAStringCompareRight(AStringCompareRight node)
	{
		inAStringCompareRight(node);
		if(node.getQuotedString() != null)
		{
			node.getQuotedString().apply(this);
			m_where.append(convertString(node.getQuotedString().getText()));
		}
		outAStringCompareRight(node);
	}
	
	public void caseALikeExprPart(ALikeExprPart node)
	{
		inALikeExprPart(node);
		if(node.getIdent() != null)
		{
			node.getIdent().apply(this);
			m_where.append(validateIdent(node.getIdent().getText()));
		}
		if(node.getLike() != null)
		{
			node.getLike().apply(this);
			m_where.append(" LIKE ");
		}
		if(node.getQuotedString() != null)
		{
			node.getQuotedString().apply(this);
			m_where.append(convertString(node.getQuotedString().getText()));
		}
		outALikeExprPart(node);
	}
	
	public void caseAIplikeExprPart(AIplikeExprPart node)
	{
		StringBuffer iplikeMethodCall = new StringBuffer("iplike(");
		
		inAIplikeExprPart(node);
		if(node.getIdent() != null)
		{
			node.getIdent().apply(this);
			iplikeMethodCall.append(validateIdent(node.getIdent().getText()));
		}
		if(node.getIplike() != null)
		{
			node.getIplike().apply(this);
		}
		if(node.getIpIdent() != null)
		{
			node.getIpIdent().apply(this);
			iplikeMethodCall.append(", '" + m_ipaddr + "')");
		}
		
		m_where.append(iplikeMethodCall.toString());
		
		outAIplikeExprPart(node);
	}
	
	public void caseAIpaddrIpIdent(AIpaddrIpIdent node)
	{
		m_ipaddr = new StringBuffer();
		
		inAIpaddrIpIdent(node);
		if(node.getOct1() != null)
		{
			node.getOct1().apply(this);
		}
		if(node.getDot1() != null)
		{
			node.getDot1().apply(this);
			m_ipaddr.append(node.getDot1().getText());
		}
		if(node.getOct2() != null)
		{
			node.getOct2().apply(this);
		}
		if(node.getDot2() != null)
		{
			node.getDot2().apply(this);
			m_ipaddr.append(node.getDot2().getText());
		}
		if(node.getOct3() != null)
		{
			node.getOct3().apply(this);
		}
		if(node.getDot3() != null)
		{
			node.getDot3().apply(this);
			m_ipaddr.append(node.getDot3().getText());
		}
		if(node.getOct4() != null)
		{
			node.getOct4().apply(this);
		}
		outAIpaddrIpIdent(node);
	}
	
	public void caseAStarOctet(AStarOctet node)
	{
		inAStarOctet(node);
		if(node.getStar() != null)
		{
			node.getStar().apply(this);
			m_ipaddr.append(node.getStar().getText());
		}
		outAStarOctet(node);
	}
	
	public void caseAOctetListOctet(AOctetListOctet node)
	{
		inAOctetListOctet(node);
		if(node.getOctetList() != null)
		{
			node.getOctetList().apply(this);
			
			//validate the list
			//
			StringTokenizer tokens = new StringTokenizer(node.getOctetList().getText(), ",");
			
			while(tokens.hasMoreTokens())
			{
				checkIPNum(tokens.nextToken());
			}
			
			//append it to the address
			//
			m_ipaddr.append(node.getOctetList().getText());
		}
		outAOctetListOctet(node);
	}
	
	public void caseAOctetRangeOctet(AOctetRangeOctet node)
	{
		inAOctetRangeOctet(node);
		if(node.getOctetRange() != null)
		{
			node.getOctetRange().apply(this);
			
			//validate the list
			//
			StringTokenizer tokens = new StringTokenizer(node.getOctetRange().getText(), "-");
			
			while(tokens.hasMoreTokens())
			{
				checkIPNum(tokens.nextToken());
			}
			
			//append it to the address
			//
			m_ipaddr.append(node.getOctetRange().getText());
		}
		outAOctetRangeOctet(node);
	}
	
	public void caseAOctetRangeListOctet(AOctetRangeListOctet node)
	{
		inAOctetRangeListOctet(node);
		if(node.getOctetRangeList() != null)
		{
			node.getOctetRangeList().apply(this);
			
			//validate the list
			//
			StringTokenizer listTokens = new StringTokenizer(node.getOctetRangeList().getText(), ",");
			StringTokenizer rangeTokens = new StringTokenizer(listTokens.nextToken());
			
			//check the range numbers
			//
			while(rangeTokens.hasMoreTokens())
			{
				checkIPNum(rangeTokens.nextToken());
			}
			
			//check the list numbers
			//
			while(listTokens.hasMoreTokens())
			{
				checkIPNum(listTokens.nextToken());
			}
			
			//append it to the address
			//
			m_ipaddr.append(node.getOctetRangeList().getText());
		}
		outAOctetRangeListOctet(node);
	}
	
	public void caseAIntegerOctet(AIntegerOctet node)
	{
		inAIntegerOctet(node);
		if(node.getInteger() != null)
		{
			node.getInteger().apply(this);
			checkIPNum(node.getInteger().toString().trim());
			m_ipaddr.append(node.getInteger().getText());
		}
		outAIntegerOctet(node);
	}
	
	/**
	 * This method returns the complete sql statement for the filter that was parsed.
	 * The SQL statement is the result of the select, from, and where components
	 * assembled from the code.
	 *
	 */
	public String getStatement()
	{
                //don't walk tree if there is no tree to walk
		if (m_node == null)
                        return null;
                
                // this will walk the tree and build the rest of the sql statement
		//
		m_node.apply(this);
                
                return buildSelectClause() + m_from.toString() + m_where.toString();
	}
}
