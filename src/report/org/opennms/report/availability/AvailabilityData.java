//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 Blast Internet Services, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of Blast Internet Services, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.blast.com/
//
// Tab Size = 8
//

package org.opennms.report.availability;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

import org.apache.log4j.Priority;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.filter.Filter;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.report.datablock.Node;
import org.opennms.report.datablock.Outage;
import org.opennms.report.datablock.OutageSvcTimesList;

/**
 * AvailabilityData collects all the outages for all node/ip/service combination
 * and stores it appropriately in the m_nodes structure.
 * 
 * @author      <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios</A>
 * @author      <A HREF="http://www.oculan.com">Oculan</A>
 */
public class AvailabilityData extends Object
{
        /**
         * The log4j category used to log debug messsages
         * and statements.
         */
        private static final String LOG4J_CATEGORY = "OpenNMS.Report";

	/**
	 * Database connection handle.
	 */
	static java.sql.Connection m_availConn;

	/**
	 * List of Node objects that satisfy the filter rule for the category.
	 */
	private List m_nodes;

	/**
	 * Common Rule for the category group.
	 */
	private String m_commonRule;

	/**
	 * Category Name
	 */
	private String m_categoryName;

	/**
	 * Category Comments
	 */
	private String m_catComment;

	/**
	 * End Time of the report.
	 */
	private long m_endTime;

	/**
	 * End Time of the report.
	 */
	private long m_12MonthsBack;

	/**
	 * End Time of the last month.
	 */
	private long m_lastMonthEndTime;

	/**
	 * Number of days in the last month
	 */
	private int m_daysInLastMonth;

	/**
 	 * Category Factory
	 */
	CategoryFactory m_catFactory;

	/**
	 * Rolling window of the last year.
	 */
	private static long LAST_YEAR_ROLLING_WINDOW;

	/**
	 * Section Index
	 */
	private int m_sectionIndex = 0;

	/**
	 * Constructor 
	 *
	 */
	public AvailabilityData(String categoryName, Report report, String format) throws IOException, MarshalException, ValidationException, Exception
	{
		ThreadCategory.setPrefix(LOG4J_CATEGORY);
                org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
		if(log.isDebugEnabled())
	                log.debug("Inside AvailabilityData");

		m_nodes 	= new ArrayList();
		initialiseInterval();
		m_categoryName	= categoryName;
		Catinfo config  = null;
		try
		{
			CategoryFactory.init();
			m_catFactory = CategoryFactory.getInstance();
			config =  m_catFactory.getConfig();
		}
		catch(IOException ioe)
		{
                        if(log.isEnabledFor(Priority.FATAL))
				log.fatal("IOException " + ioe);
			throw ioe;
		}
		catch(MarshalException marshex)
		{
                        if(log.isEnabledFor(Priority.FATAL))
				log.fatal("Exception " + marshex);
			throw marshex;
		}
		catch(ValidationException ex)
		{
                        if(log.isEnabledFor(Priority.FATAL))
				log.fatal("Exception " + ex);
			throw ex;
		}
		
		if(log.isDebugEnabled())
			log.debug("CATEGORY " + categoryName);
		if(categoryName.equals("") || categoryName.equals("all"))
		{
			Enumeration enumCG = config.enumerateCategorygroup();
			int catCount = 0;
			while(enumCG.hasMoreElements())
			{
				Categorygroup cg = (Categorygroup)enumCG.nextElement();

				// go through the categories
				org.opennms.netmgt.config.categories.Categories cats = cg.getCategories();

				Enumeration enumCat = cats.enumerateCategory();
				while(enumCat.hasMoreElements())
				{
					org.opennms.netmgt.config.categories.Category cat = (org.opennms.netmgt.config.categories.Category)enumCat.nextElement();
					Enumeration enumMonitoredSvc = cat.enumerateService();
					List monitoredServices = new ArrayList();
					while(enumMonitoredSvc.hasMoreElements())
					{
						String service = (String)enumMonitoredSvc.nextElement();
						monitoredServices.add(service);
					}
					if(log.isDebugEnabled())
						log.debug("CATEGORY " + cat.getLabel());
					catCount++;
					populateDataStructures(cat, report, format, catCount);
				}
			}
		}
		else
		{
			org.opennms.netmgt.config.categories.Category cat = (org.opennms.netmgt.config.categories.Category)m_catFactory.getCategory(categoryName);
			if(log.isDebugEnabled())
				log.debug("CATEGORY " + cat.getLabel());
			populateDataStructures(cat, report, format, 1);
		}
		
		SimpleDateFormat simplePeriod = new SimpleDateFormat("MMMMMMMMMMM dd, yyyy");
		String reportPeriod = simplePeriod.format(new java.util.Date(m_12MonthsBack)) + " - " + simplePeriod.format(new java.util.Date(m_endTime));
		Created created = report.getCreated();
		if(created == null)
			created = new Created();
		created.setPeriod(reportPeriod);
		report.setCreated(created);
		
		if(log.isDebugEnabled())
			log.debug("After availCalculations");
	}

	/**
	 * Populates the data structure for this category. This method only computes for monitored services in this 
	 * category.
	 *
	 * @param cat Category
	 * @param report Report Castor class
	 * @param format SVG-specific/all reports
	 */
	private void populateDataStructures(org.opennms.netmgt.config.categories.Category cat, Report report, String format, int catIndex) throws Exception
	{
		report.setCatCount(catIndex);
                org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
                try
                {
			String categoryName = cat.getLabel();
			m_commonRule = m_catFactory.getEffectiveRule(categoryName);
			Enumeration enumMonitoredSvc = cat.enumerateService();
                        List monitoredServices = new ArrayList();
                        while(enumMonitoredSvc.hasMoreElements())
                        {
                	        String service = (String)enumMonitoredSvc.nextElement();
                                monitoredServices.add(service);
                        }
                        populateNodesFromDB(cat, monitoredServices);
			ViewInfo viewInfo = report.getViewInfo();
                        if(log.isDebugEnabled())
                        {
                                log.debug("Nodes " + m_nodes );
                        }
			ListIterator cleanNodes = m_nodes.listIterator();
			while(cleanNodes.hasNext())
			{
				Node node = (Node)cleanNodes.next();
				if( node != null && !node.hasOutages())
				{
					cleanNodes.remove();
				}
			}
                        if(log.isDebugEnabled())
                        {
                                log.debug("Cleaned Nodes " + m_nodes );
                        }
                        TreeMap topOffenders = new TreeMap();
                        topOffenders = getPercentNode();
			
                        if(log.isDebugEnabled())
                                log.debug("TOP OFFENDERS " + topOffenders);
			if(m_nodes.size() <= 0)
				m_nodes = null;
			if(m_nodes != null)
			{
                        	AvailCalculations availCalculations = new AvailCalculations (   m_nodes,
                                	                                                        m_endTime,
                                        	                                                m_lastMonthEndTime,
                                                	                                        monitoredServices,
                                                        	                                report,
                                                                	                        topOffenders,
												cat.getWarning(), 
												cat.getNormal(),
												cat.getComment(),
												cat.getLabel(),
												format,
												catIndex,
												m_sectionIndex);
				m_sectionIndex = availCalculations.getSectionIndex();
				report.setSectionCount(m_sectionIndex-1);
			}
			else
			{
				org.opennms.report.availability.Category category =
								new org.opennms.report.availability.Category();
				category.setCatComments(cat.getComment());
				category.setCatName(cat.getLabel());
				category.setCatIndex(catIndex);
				category.setNodeCount(0);
				category.setIpaddrCount(0);
				category.setServiceCount(0);
				Section section = new Section();
				section.setSectionIndex(m_sectionIndex);
				org.opennms.report.availability.CatSections catSections = new org.opennms.report.availability.CatSections();
				catSections.addSection(section);
				category.addCatSections(catSections);
				org.opennms.report.availability.Categories categories = report.getCategories();
				categories.addCategory(category);
				report.setCategories(categories);
				report.setSectionCount(m_sectionIndex);
				m_sectionIndex++;
			}
                }
                catch(Exception e)
                {
                        if(log.isEnabledFor(Priority.FATAL))
				log.fatal("Exception has occured " + e);
                        e.printStackTrace();
			throw e;
                }		
	}

	/**
	 * Initialise the endTime, last Months end time and number of days in the last month.
	 */
	private void initialiseInterval()
	{
                org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());

                Calendar calendar = new GregorianCalendar();
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int year = calendar.get(Calendar.YEAR);
                calendar.set(year, month, day-1, 23, 59, 59);                                   // Set the end Time
                m_endTime = calendar.getTime().getTime();
		
		calendar.add(Calendar.YEAR, -1);
		LAST_YEAR_ROLLING_WINDOW = m_endTime -  calendar.getTime().getTime();
		m_12MonthsBack = m_endTime - LAST_YEAR_ROLLING_WINDOW;
		
                calendar = new GregorianCalendar();
		calendar.setTime(new java.util.Date(m_12MonthsBack));
                month = calendar.get(Calendar.MONTH);
                day = calendar.get(Calendar.DAY_OF_MONTH);
                year = calendar.get(Calendar.YEAR);
                calendar.set(year, month, 1, 0, 0, 0);                                   // Set the end Time
                m_12MonthsBack = calendar.getTime().getTime();

		if (log.isDebugEnabled())
		{
			log.debug("last Year " + new java.util.Date(m_12MonthsBack));
			log.debug("End Year " + new java.util.Date(m_endTime));
			log.debug("Rolling window of the last year " + LAST_YEAR_ROLLING_WINDOW);
		}
                Calendar lastMonthCalendar = new GregorianCalendar();
                lastMonthCalendar.setTime(new java.util.Date((new Double(m_endTime)).longValue()));
                month = lastMonthCalendar.get(Calendar.MONTH) - 1;
                year = lastMonthCalendar.get(Calendar.YEAR);
                lastMonthCalendar.set(year, month, 1, 0, 0, 0);
                m_daysInLastMonth = getDaysForMonth(lastMonthCalendar.getTime().getTime());     //Number of days in the last month

                lastMonthCalendar.set(year, month, m_daysInLastMonth, 23, 59, 59);              // Set the end Time of the last month
                m_lastMonthEndTime = lastMonthCalendar.getTime().getTime();
	}

        /**
         * Returns the number of days in the month, also considers checks for leap year.
         * @param isLeap the leap year flag.
         * @param month The month whose days count is reqd
         */
        private static synchronized int getDays(boolean isLeap, int month)
        {
                switch(month)
                {
                case 0:
                case 2:
                case 4:
                case 6:
                case 7:
                case 9:
                case 11:        return 31;

                case 3:
                case 5:
                case 8:
                case 10:        return 30;

                case 1:
                                if(isLeap)
                                        return 29;
                                else
                                        return 28;
                }
                return -1;
        }

        /**
         * Returns the number of Days in the month
         * @param endTime The end of the month (time in milliseconds)
         */
        private int getDaysForMonth(long endTime)
        {
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.setTime(new java.util.Date(endTime));
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);
                int days = getDays(calendar.isLeapYear(year), month);
                return (getDays(calendar.isLeapYear(year), month));
        }


	/**
	 * Returns the nodes.
	 */
	public List getNodes()
	{
		return m_nodes;
	}

        /**
         * Initialises the database connection.
         */
        public void initialiseConnection()  throws IOException, MarshalException, ValidationException, ClassNotFoundException, SQLException
        {
                org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
                //
                // Initialize the DataCollectionConfigFactory
                //
                try
                {
                        DatabaseConnectionFactory.init();
                        m_availConn = DatabaseConnectionFactory.getInstance().getConnection();
                }
                catch(MarshalException ex)
                {
                        if(log.isEnabledFor(Priority.FATAL))
                                log.fatal("initialize: Failed to load data collection configuration", ex);
                        throw new UndeclaredThrowableException(ex);
                }
                catch(ValidationException ex)
                {
                        if(log.isEnabledFor(Priority.FATAL))
                                log.fatal("initialize: Failed to load data collection configuration", ex);
                        throw new UndeclaredThrowableException(ex);
                }
                catch(IOException ex)
                {
                        if(log.isEnabledFor(Priority.FATAL))
                                log.fatal("initialize: Failed to load data collection configuration", ex);
                        throw new UndeclaredThrowableException(ex);
                }
                catch (ClassNotFoundException cnfE)
                {
                        if(log.isEnabledFor(Priority.FATAL))
                                log.fatal("initialize: Failed loading database driver.", cnfE);
                        throw new UndeclaredThrowableException(cnfE);
                }
                catch (SQLException sqlE)
                {
                        if(log.isEnabledFor(Priority.FATAL))
                                log.fatal("initialize: Failed getting connection to the database.", sqlE);
                        throw new UndeclaredThrowableException(sqlE);
                }
        }

        /**
         * Closes the database connection.
         */
        public void closeConnection() 
        {
                org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
                if(m_availConn != null)
                {
                        try
                        {
                                m_availConn.close();
				m_availConn = null;
                        }
                        catch(Throwable t)
                        {
                                if(log.isEnabledFor(Priority.WARN))
                                        log.warn("initialize: an exception occured while closing the JDBC connection", t);
                        }
                }
        }

        /**
         * Returns percent/node combinations for the last month.
	 * This is used to get the last months top 20 offenders
         */
        public TreeMap getPercentNode()
        {
		org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
		int days = m_daysInLastMonth;
		long endTime = m_lastMonthEndTime;
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(endTime);
		cal.add(Calendar.DATE, -1 * days);
		long rollingWindow = endTime - cal.getTime().getTime();
		long startTime = cal.getTime().getTime();
		if(log.isDebugEnabled())
		{
			log.debug("getPercentNode: Start time " + new java.util.Date(startTime));
			log.debug("getPercentNode: End time " + new java.util.Date(endTime));
		}
                TreeMap percentNode = new TreeMap();
		Iterator nodeIter = m_nodes.iterator();

		while(nodeIter.hasNext())
		{
			Node node = (Node)nodeIter.next();
			if(node != null)
			{
				double percent = node.getPercentAvail(endTime, rollingWindow);
				String nodeName = node.getName();
				if(log.isDebugEnabled())
					log.debug("Node " + nodeName + " " + percent + "%");
				if(percent < 100.0)
				{
					List tmp = (List)percentNode.get(new Double(percent));
					if(tmp == null)
						tmp = new ArrayList();
					tmp.add(nodeName);
					percentNode.put(new Double(percent), tmp);
				}
			}
		}
	        if(log.isDebugEnabled())
        	        log.debug("Percent node " + percentNode );
		return percentNode;
        }

	/**
	 * For each category in the categories list, this reads the 
	 * services and outage tables to get the initial data, creates
	 * objects that are added to the map and and to the
	 * appropriate category
	 *
	 * @throws SQLException if the database read fails due to an SQL error
	 * @throws FilterParseException if filtering the data against the category rule fails due to the rule being incorrect
	 */
	private void populateNodesFromDB(org.opennms.netmgt.config.categories.Category cat, List monitoredServices)
			throws SQLException, FilterParseException, Exception
	{
		m_nodes 	= new ArrayList();
		// Create the filter
		Filter filter =  new Filter();

		initialiseConnection();
		// Prepare the statement to get service entries for each IP
		PreparedStatement servicesGetStmt = m_availConn.prepareStatement(AvailabilityConstants.DB_GET_SVC_ENTRIES);
	 	// Prepared statement to get node info for an ip
		PreparedStatement ipInfoGetStmt	=  m_availConn.prepareStatement(AvailabilityConstants.DB_GET_INFO_FOR_IP);
	 	// Prepared statement to get outages entries
		PreparedStatement outagesGetStmt = m_availConn.prepareStatement(AvailabilityConstants.DB_GET_OUTAGE_ENTRIES);

		org.apache.log4j.Category log = ThreadCategory.getInstance(AvailabilityData.class);

		// get the rule for this category, get the list of nodes that satisfy this rule
		m_catComment = cat.getComment();
		String filterRule = m_commonRule;

		if(log.isDebugEnabled())
			log.debug("Category: " + filterRule);

		String ip = null;
		ResultSet ipRS=null;
		try
		{
			List nodeIPs = filter.getIPList(filterRule);

			if (log.isDebugEnabled())
				log.debug("Number of IPs satisfying rule: " + nodeIPs.size());
			
			// For each of these IP addresses, get the details from the ifServices and services tables
			Iterator ipIter = nodeIPs.iterator();
			while(ipIter.hasNext())
			{
				ip = (String)ipIter.next();

				// get node info for this ip
				ipInfoGetStmt.setString(1, ip);

				ipRS = ipInfoGetStmt.executeQuery();
				while(ipRS.next())
				{
					int nodeid = ipRS.getInt(1);
					String nodeName = ipRS.getString(2);

//					if(log.isDebugEnabled())
//						log.debug("IP->node info lookup result: " + nodeid);

					//
					// get the services for this IP address
					//
					ResultSet  svcRS=null;
					servicesGetStmt.setLong(1, nodeid);
					servicesGetStmt.setString(2, ip);
					servicesGetStmt.setString(3, ip);
					servicesGetStmt.setLong(4, nodeid);
					svcRS = servicesGetStmt.executeQuery();

					// create node objects for this nodeID/IP/service
					while(svcRS.next())
					{
						// read data from the resultset
						int svcid = svcRS.getInt(1);
						String svcname = svcRS.getString(2);
						// If the list is empty, we assume all services are monitored.  If it has any, we use it as a filter
						if(monitoredServices.isEmpty() || monitoredServices.contains(svcname))
						{
//							if(log.isDebugEnabled())
//								log.debug("services result: " + nodeid + "\t" + ip + "\t" +  svcname);

							OutageSvcTimesList outageSvcTimesList = new OutageSvcTimesList();
							getOutagesNodeIpSvc(nodeid, 
									    nodeName, 
									    ip, 
									    svcid, 
									    svcname, 
									    outageSvcTimesList,
									    outagesGetStmt);

/*							IfService ifservice = new IfService(nodeid, 
											    ip, 
											    svcid, 
											    nodeName, 	
											    svcname);
							Map svcOutages = (Map)m_services.get(svcname);
							if(svcOutages == null)
								svcOutages = new HashMap();
							svcOutages.put(ifservice, outageSvcTimesList);
							m_services.put(svcname, svcOutages);
*/
						}
					}	

					// finally close the result set
					try
					{
						if (svcRS != null)
							svcRS.close();
					}
					catch(Exception e)
					{
						if(log.isEnabledFor(Priority.FATAL))
							log.fatal("Exception while closing the services result set", e);
						throw e;
					}
				}
			}
		}
		catch(SQLException e)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("Unable to get node list for category \'" + cat.getLabel(), e);
			throw e;
		}
		catch(FilterParseException e)
		{
			// if we get here, the error was most likely in
			// getting the nodelist from the filters
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("Unable to get node list for category \'" + cat.getLabel(), e);

			// throw exception
			throw e;
		}
		catch(Exception e)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("Unable to get node list for category \'" + cat.getLabel(), e);
 
			// throw exception
			throw new Exception("Unable to get node list for category \'" + cat.getLabel() + "\':\n\t" + e);
		}
		finally
		{
			try
			{
				if(ipRS != null)
					ipRS.close();
				if (servicesGetStmt != null)
                                	servicesGetStmt.close();

	                        if (ipInfoGetStmt != null)
        	                        ipInfoGetStmt.close();

                	        if (outagesGetStmt != null)
                        	        outagesGetStmt.close();
				
				if(m_availConn != null)
					closeConnection();
			}
			catch(Exception e)
			{
				if(log.isEnabledFor(Priority.FATAL))
					log.fatal("Exception while closing the ip get node info result set - ip: " + ip, e);
				throw e;
			}
		}
	}

	/**
	 * Get all outages for this nodeid/ipaddr/service combination and add it to m_nodes.
	 */
	private void getOutagesNodeIpSvc(int nodeid, 
					 String nodeName, 
					 String ipaddr, 
					 int serviceid, 
					 String serviceName, 
					 OutageSvcTimesList outageSvcTimesList,
					 PreparedStatement outagesGetStmt) throws SQLException
	{
		org.apache.log4j.Category log = ThreadCategory.getInstance(AvailabilityData.class);
		// Get outages for this node/ip/svc pair
		try
		{
//			if (log.isDebugEnabled())
//	                        log.debug("Node " + nodeid + " ipaddr " + ipaddr + " serviceid " + serviceid);
			outagesGetStmt.setInt(1, nodeid );
			outagesGetStmt.setString(2, ipaddr);
			outagesGetStmt.setInt(3, serviceid);
			
			ResultSet rs = outagesGetStmt.executeQuery();
			if(m_nodes != null && m_nodes.size() > 0)
			{
				ListIterator lstIter = m_nodes.listIterator();
				boolean foundFlag = false;
				Node oldNode = null;
				while(lstIter.hasNext())
				{
					oldNode = (Node)lstIter.next();
					if(     oldNode != null &&
						oldNode.getNodeID() == nodeid)
					{
						foundFlag = true;
						break;
					}
				}
				if(!foundFlag)
				{
					Node newNode = new Node(nodeName, nodeid);
					newNode.addInterface(ipaddr, serviceName);
					m_nodes.add(newNode);
				}
				else
				{
					oldNode.addInterface(ipaddr, serviceName);
				}
			}
			else
			{
				Node newNode = new Node(nodeName, nodeid);
				newNode.addInterface(ipaddr, serviceName);
				m_nodes.add(newNode);
			}
			rs.beforeFirst();
			while(rs.next())
			{
				Timestamp lost = rs.getTimestamp(1);
				Timestamp regained = rs.getTimestamp(2);
				long losttime = lost.getTime();
				long regainedtime = 0;
				if(regained != null)
					regainedtime = regained.getTime();

				if(regainedtime > 0)
				{
					if( regainedtime <= m_12MonthsBack || losttime >= m_endTime)
						continue;
				}
				else
				{
					if( losttime >= m_endTime)
						continue;
				}
				Outage outage = new Outage(losttime, regainedtime);
				outageSvcTimesList.add(outage);
				addNode(nodeName, 
					nodeid,
					ipaddr,
					serviceName,
					losttime,
					regainedtime);
			}
			if(rs != null)
				rs.close();

		}
		catch(SQLException e)
		{
			if(log.isEnabledFor(Priority.FATAL))
				log.fatal("Error has occured while getting the outages " , e);
			throw e;
		}
	}

        /**
         * This method adds a unique tuple to the list of nodes m_nodes.
         */
        public void addNode(String nodeName, int nodeid, String ipaddr, String serviceid, long losttime, long regainedtime)
        {
		org.apache.log4j.Category log = ThreadCategory.getInstance(AvailabilityData.class);
                if(m_nodes == null)
                        m_nodes = new ArrayList();
                else
                {
                        if(m_nodes.size() <= 0)
                        {
                                Node newNode = new Node(nodeName, nodeid);
//                                if(log.isDebugEnabled())
//                                        log.debug("Created the new node.");
                                if(losttime > 0)
                                {
                                        if(regainedtime > 0)
                                                newNode.addInterface(ipaddr, serviceid, losttime, regainedtime);
                                        else
                                                newNode.addInterface(ipaddr, serviceid, losttime);
                                }
                                else
                                {
                                        newNode.addInterface(ipaddr, serviceid);
                                }
                                m_nodes.add(newNode);
                                return;
                        }
                        else    // look for the node with the nodeName
                        {
                                Node newNode = null;
                                boolean foundFlag = false;
                                ListIterator lstIter = m_nodes.listIterator();
                                while(lstIter.hasNext())
                                {
                                        newNode = (Node)lstIter.next();
                                        if(newNode.getNodeID() == nodeid )
                                        {
                                                foundFlag = true;
                                                break;
                                        }
                                }
                                if( !foundFlag )
                                {
                                        newNode = new Node(nodeName, nodeid);
                                        if(losttime > 0)
                                        {
                                                if(regainedtime > 0)
                                                        newNode.addInterface(ipaddr, serviceid, losttime, regainedtime);
                                                else
                                                        newNode.addInterface(ipaddr, serviceid, losttime);
                                        }
                                        else
                                        {
                                                newNode.addInterface(ipaddr, serviceid);
                                        }
                                        m_nodes.add(newNode);
                                        return;
                                }
                                else
                                {
                                        if(losttime > 0)
                                        {
                                                if(regainedtime > 0)
                                                        newNode.addInterface(ipaddr, serviceid, losttime, regainedtime);
                                                else
                                                        newNode.addInterface(ipaddr, serviceid, losttime);
                                        }
                                        else
                                        {
                                                newNode.addInterface(ipaddr, serviceid);
                                        }
                                        return;
                                }
                        }
                }
        }
}
