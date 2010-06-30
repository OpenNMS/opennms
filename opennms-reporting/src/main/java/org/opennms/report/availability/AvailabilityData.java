//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// 2006 May 30: Added a way to choose the date to run the availability
// reports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.report.availability;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.CatFactory;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.FilterParseException;
import org.opennms.report.datablock.Node;
import org.opennms.report.datablock.Outage;
import org.opennms.report.datablock.OutageSvcTimesList;

/**
 * AvailabilityData collects all the outages for all node/ip/service
 * combination and stores it appropriately in the m_nodes structure.
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 * @author <A HREF="http://www.oculan.com">Oculan </A>
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 * @author <A HREF="http://www.oculan.com">Oculan </A>
 * @version $Id: $
 */
public class AvailabilityData extends Object {
    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    private static final String DEFAULT_FORMAT = "PDF";

    /**
     * Database connection handle.
     */
    static java.sql.Connection m_availConn;

    /**
     * List of Node objects that satisfy the filter rule for the category.
     */
    private List<Node> m_nodes;

    /**
     * Common Rule for the category group.
     */
    private String m_commonRule;

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
    CatFactory m_catFactory;

    /**
     * Section Index
     */
    private int m_sectionIndex = 0;

    /**
     * <p>Constructor for AvailabilityData.</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     * @param report a {@link org.opennms.report.availability.Report} object.
     * @param monthFormat a {@link java.lang.String} object.
     * @param calendar a {@link java.util.Calendar} object.
     * @param startMonth a {@link java.lang.String} object.
     * @param startDate a {@link java.lang.String} object.
     * @param startYear a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.lang.Exception if any.
     */
    public AvailabilityData(String categoryName, Report report,
            String monthFormat, Calendar calendar, String startMonth,
            String startDate, String startYear) throws IOException,
            MarshalException, ValidationException, Exception {
        
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(startDate));
        cal.set(Calendar.MONTH, Integer.parseInt(startMonth));
        cal.set(Calendar.YEAR, Integer.parseInt(startYear));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        generateData(categoryName, report, DEFAULT_FORMAT, monthFormat,
                     calendar, new Date(cal.getTimeInMillis()));
    }

    /**
     * <p>Constructor for AvailabilityData.</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     * @param report a {@link org.opennms.report.availability.Report} object.
     * @param format a {@link java.lang.String} object.
     * @param monthFormat a {@link java.lang.String} object.
     * @param calendar a {@link java.util.Calendar} object.
     * @param startMonth a {@link java.lang.String} object.
     * @param startDate a {@link java.lang.String} object.
     * @param startYear a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.lang.Exception if any.
     */
    public AvailabilityData(String categoryName, Report report,
            String format, String monthFormat, Calendar calendar,
            String startMonth, String startDate, String startYear)
            throws IOException, MarshalException, ValidationException,
            Exception {
      
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(startDate));
        cal.set(Calendar.MONTH, Integer.parseInt(startMonth));
        cal.set(Calendar.YEAR, Integer.parseInt(startYear));
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        generateData(categoryName, report, format, monthFormat, calendar,
                     new Date(cal.getTimeInMillis()));
    }

    /**
     * <p>Constructor for AvailabilityData.</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     * @param report a {@link org.opennms.report.availability.Report} object.
     * @param format a {@link java.lang.String} object.
     * @param monthFormat a {@link java.lang.String} object.
     * @param calendar a {@link java.util.Calendar} object.
     * @param periodEndDate a {@link java.util.Date} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.lang.Exception if any.
     */
    public AvailabilityData(String categoryName, Report report,
            String format, String monthFormat, Calendar calendar,
            Date periodEndDate)
            throws IOException, MarshalException, ValidationException,
            Exception {
       generateData(categoryName, report, format, monthFormat, calendar, periodEndDate);
    }

    /**
     * Original constructor, now called by new version
     */

    private void generateData(String categoryName, Report report,
            String format, String monthFormat, Calendar calendar,
            Date periodEndDate)
            throws IOException, MarshalException, ValidationException,
            Exception {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        log.debug("Inside AvailabilityData");

        m_nodes = new ArrayList<Node>();
        initializeInterval(calendar, periodEndDate);

        Catinfo config = null;
        try {
            CategoryFactory.init();
            m_catFactory = CategoryFactory.getInstance();
            config = m_catFactory.getConfig();
        } catch (IOException e) {
            log.fatal("Initializing CategoryFactory", e);
            throw e;
        } catch (MarshalException e) {
            log.fatal("Initializing CategoryFactory", e);
            throw e;
        } catch (ValidationException e) {
            log.fatal("Initializing CategoryFactory", e);
            throw e;
        }

        if (log.isDebugEnabled()) {
            log.debug("CATEGORY " + categoryName);
        }
        if (categoryName.equals("") || categoryName.equals("all")) {
            int catCount = 0;
            if (log.isDebugEnabled()) {
                log.debug("catCount " + catCount);
            }
            
            for(Categorygroup cg : config.getCategorygroupCollection()) {
            
                // go through the categories
                org.opennms.netmgt.config.categories.Categories cats = cg.getCategories();
            
                for(org.opennms.netmgt.config.categories.Category cat : cats.getCategoryCollection()) {

                    if (log.isDebugEnabled()) {
                        log.debug("CATEGORY " + cat.getLabel());
                    }
                    catCount++;
                    populateDataStructures(cat, report, format, monthFormat, catCount);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("catCount " + catCount);
            }
        } else {
            org.opennms.netmgt.config.categories.Category cat = (org.opennms.netmgt.config.categories.Category) m_catFactory.getCategory(categoryName);
            if (log.isDebugEnabled()) {
                log.debug("CATEGORY - now populating data structures "
                        + cat.getLabel());
            }
            populateDataStructures(cat, report, format, monthFormat, 1);
        }

        SimpleDateFormat simplePeriod = new SimpleDateFormat("MMMMMMMMMMM dd, yyyy");
        String reportPeriod = simplePeriod.format(new java.util.Date(
                                                                     m_12MonthsBack))
                + " - " + simplePeriod.format(new java.util.Date(m_endTime));
        Created created = report.getCreated();
        if (created == null) {
            created = new Created();
        }
        created.setPeriod(reportPeriod);
        report.setCreated(created);

        if (log.isDebugEnabled()) {
            log.debug("After availCalculations");
        }
    }

    /**
     * Populates the data structure for this category. This method only
     * computes for monitored services in this category.
     * 
     * @param cat
     *            Category
     * @param report
     *            Report Castor class
     * @param format
     *            SVG-specific/all reports
     */
    private void populateDataStructures(
            org.opennms.netmgt.config.categories.Category cat, Report report,
            String format, String monthFormat, int catIndex) throws Exception {
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        if (log.isDebugEnabled())
            log.debug("Inside populate data Structures" + catIndex);
        report.setCatCount(catIndex);
        log.debug("Inside populate data Structures");
        try {
            String categoryName = cat.getLabel();
            m_commonRule = m_catFactory.getEffectiveRule(categoryName);

            List<String> monitoredServices = new ArrayList<String>(cat.getServiceCollection());

            populateNodesFromDB(cat, monitoredServices);
            
            if (log.isDebugEnabled()) {
                log.debug("Nodes " + m_nodes);
            }
            ListIterator<Node> cleanNodes = m_nodes.listIterator();
            while (cleanNodes.hasNext()) {
                Node node = (Node) cleanNodes.next();
                if (node != null && !node.hasOutages()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Removing node: " + node);
                    }
                    cleanNodes.remove();
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("Cleaned Nodes " + m_nodes);
            }
            TreeMap<Double, List<String>> topOffenders = getPercentNode();

            if (log.isDebugEnabled()) {
                log.debug("TOP OFFENDERS " + topOffenders);
            }
            if (m_nodes.size() <= 0) {
                m_nodes = null;
            }
            if (m_nodes != null) {
                AvailCalculations availCalculations = new AvailCalculations(
                                                                            m_nodes,
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
                                                                            monthFormat,
                                                                            catIndex,
                                                                            m_sectionIndex);
                m_sectionIndex = availCalculations.getSectionIndex();
                report.setSectionCount(m_sectionIndex - 1);
            } else {
                org.opennms.report.availability.Category category = new org.opennms.report.availability.Category();
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
        } catch (Exception e) {
            log.fatal("Exception has occured", e);
            throw e;
        }
    }

    /**
     * Initialize the endTime, start Time, last Months end time and number of days in the
     * last month.
     *
     * @param calendar a {@link java.util.Calendar} object.
     * @param periodEndDate a {@link java.util.Date} object.
     */
    public void initializeInterval(Calendar calendar, Date periodEndDate) {
        
        Calendar tempCal = new GregorianCalendar();
        tempCal.setTime(periodEndDate);

        // This used to be the day prior to the report being run, which is confusing
        // tempCal.add(Calendar.DAY_OF_MONTH, -1);
        tempCal.set(Calendar.HOUR_OF_DAY, 23);
        tempCal.set(Calendar.MINUTE, 59);
        tempCal.set(Calendar.SECOND, 59);
        tempCal.set(Calendar.MILLISECOND, 999);
        m_endTime = tempCal.getTimeInMillis();
        
        // Calculate first of the month, 12 months ago.
        
        tempCal.add(Calendar.YEAR, -1);
        tempCal.set(Calendar.DAY_OF_MONTH, 1);
        tempCal.set(Calendar.HOUR_OF_DAY, 0);
        tempCal.set(Calendar.MINUTE, 0);
        tempCal.set(Calendar.SECOND, 0);
        tempCal.set(Calendar.MILLISECOND, 0);
        
        m_12MonthsBack = tempCal.getTimeInMillis();
        
        // Reset tempCal to m_end time and calculate last month calendar details
        
        tempCal.setTimeInMillis(m_endTime);
        tempCal.add(Calendar.MONTH, -1);
        
        m_daysInLastMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        // Not entirely sure if this is needed
        
        tempCal.set(Calendar.DAY_OF_MONTH, m_daysInLastMonth);
        tempCal.set(Calendar.HOUR_OF_DAY, 23);
        tempCal.set(Calendar.MINUTE, 59);
        tempCal.set(Calendar.SECOND, 59);
        tempCal.set(Calendar.MILLISECOND, 999);
        
        m_lastMonthEndTime = tempCal.getTimeInMillis();
        
    }
    
    /**
     * Returns the nodes.
     *
     * @return a {@link java.util.List} object.
     */
    public List<Node> getNodes() {
        return m_nodes;
    }

    /**
     * Initializes the database connection.
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.lang.ClassNotFoundException if any.
     * @throws java.sql.SQLException if any.
     */
    public void initialiseConnection() throws IOException, MarshalException,
            ValidationException, ClassNotFoundException, SQLException {
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        //
        // Initialize the DataCollectionConfigFactory
        //
        try {
            DataSourceFactory.init();
            m_availConn = DataSourceFactory.getInstance().getConnection();
        } catch (MarshalException e) {
            log.fatal(
                      "initialize: Failed to load data collection configuration",
                      e);
            throw new UndeclaredThrowableException(e);
        } catch (ValidationException e) {
            log.fatal(
                      "initialize: Failed to load data collection configuration",
                      e);
            throw new UndeclaredThrowableException(e);
        } catch (IOException e) {
            log.fatal(
                      "initialize: Failed to load data collection configuration",
                      e);
            throw new UndeclaredThrowableException(e);
        } catch (ClassNotFoundException e) {
            log.fatal("initialize: Failed loading database driver.", e);
            throw new UndeclaredThrowableException(e);
        } catch (SQLException e) {
            log.fatal(
                      "initialize: Failed getting connection to the database.",
                      e);
            throw new UndeclaredThrowableException(e);
        } catch (PropertyVetoException e) {
            log.fatal(
                      "initialize: Failed getting connection to the database.",
                      e);
            throw new UndeclaredThrowableException(e);
        }
    }

    /**
     * Closes the database connection.
     */
    public void closeConnection() {
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        if (m_availConn != null) {
            try {
                m_availConn.close();
                m_availConn = null;
            } catch (Throwable t) {
                log.warn(
                         "initialize: an exception occured while closing the "
                                 + "JDBC connection", t);
            }
        }
    }

    /**
     * Returns percent/node combinations for the last month. This is used to
     * get the last months top 20 offenders
     *
     * @return a {@link java.util.TreeMap} object.
     */
    public TreeMap<Double, List<String>> getPercentNode() {
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        int days = m_daysInLastMonth;
        long endTime = m_lastMonthEndTime;
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(endTime);
        cal.add(Calendar.DATE, -1 * days);
        long rollingWindow = endTime - cal.getTime().getTime();
        long startTime = cal.getTime().getTime();
        if (log.isDebugEnabled()) {
            log.debug("getPercentNode: Start time "
                    + new java.util.Date(startTime));
            log.debug("getPercentNode: End time "
                    + new java.util.Date(endTime));
        }
        TreeMap<Double, List<String>> percentNode = new TreeMap<Double, List<String>>();
        
        for(Node node : m_nodes) {
            if (node != null) {
                double percent = node.getPercentAvail(endTime, rollingWindow);
                String nodeName = node.getName();
                if (log.isDebugEnabled()) {
                    log.debug("Node " + nodeName + " " + percent + "%");
                }
                if (percent < 100.0) {
                    List<String> nodeNames = percentNode.get(new Double(percent));
                    if (nodeNames == null) {
                        nodeNames = new ArrayList<String>();
                    }
                    nodeNames.add(nodeName);
                    percentNode.put(new Double(percent), nodeNames);
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Percent node " + percentNode);
        }
        return percentNode;
    }

    /**
     * For each category in the categories list, this reads the services and
     * outage tables to get the initial data, creates objects that are added
     * to the map and and to the appropriate category
     * 
     * @throws SQLException
     *             if the database read fails due to an SQL error
     * @throws FilterParseException
     *             if filtering the data against the category rule fails due
     *             to the rule being incorrect
     */
    private void populateNodesFromDB(
            org.opennms.netmgt.config.categories.Category cat,
            List<String> monitoredServices) throws SQLException,
            FilterParseException, Exception {
        m_nodes = new ArrayList<Node>();
        org.apache.log4j.Category log = ThreadCategory.getInstance(AvailabilityData.class);

        log.debug("in populateNodesFromDB");

        final DBUtils d = new DBUtils(getClass());
        initialiseConnection();
        d.watch(m_availConn);
            
        /*
         * Get the rule for this category, get the list of nodes that satisfy
         * this rule.
         */

        String filterRule = m_commonRule;

        if (log.isDebugEnabled()) {
            log.debug("Category: " + filterRule);
        }

        String ip = null;
        ResultSet ipRS = null;
        try {
            // Prepare the statement to get service entries for each IP
            PreparedStatement servicesGetStmt = m_availConn.prepareStatement(AvailabilityConstants.DB_GET_SVC_ENTRIES);
            d.watch(servicesGetStmt);
            // Prepared statement to get node info for an IP
            PreparedStatement ipInfoGetStmt = m_availConn.prepareStatement(AvailabilityConstants.DB_GET_INFO_FOR_IP);
            d.watch(ipInfoGetStmt);
            // Prepared statement to get outages entries
            PreparedStatement outagesGetStmt = m_availConn.prepareStatement(AvailabilityConstants.DB_GET_OUTAGE_ENTRIES);
            d.watch(outagesGetStmt);

            List<String> nodeIPs = FilterDaoFactory.getInstance().getIPList(filterRule);

            if (log.isDebugEnabled()) {
                log.debug("Number of IPs satisfying rule: " + nodeIPs.size());
            }

            /*
             * For each of these IP addresses, get the details from the
             * ifServices and services tables.
             */
            Iterator<String> ipIter = nodeIPs.iterator();
            while (ipIter.hasNext()) {
                ip = (String) ipIter.next();

                // get node info for this ip
                ipInfoGetStmt.setString(1, ip);

                ipRS = ipInfoGetStmt.executeQuery();
                d.watch(ipRS);
                while (ipRS.next()) {
                    int nodeid = ipRS.getInt(1);
                    String nodeName = ipRS.getString(2);

                    // get the services for this IP address
                    ResultSet svcRS = null;
                    servicesGetStmt.setLong(1, nodeid);
                    servicesGetStmt.setString(2, ip);
                    servicesGetStmt.setString(3, ip);
                    servicesGetStmt.setLong(4, nodeid);
                    svcRS = servicesGetStmt.executeQuery();
                    d.watch(svcRS);
                    
                    // create node objects for this nodeID/IP/service
                    while (svcRS.next()) {
                        // read data from the resultSet
                        int svcid = svcRS.getInt(1);
                        String svcname = svcRS.getString(2);

                        /*
                         * If the list is empty, we assume all services are
                         * monitored. If it has any, we use it as a filter
                         */
                        if (monitoredServices.isEmpty() || monitoredServices.contains(svcname)) {

                            OutageSvcTimesList outageSvcTimesList = new OutageSvcTimesList();
                            getOutagesNodeIpSvc(nodeid, nodeName, ip, svcid,
                                                svcname, outageSvcTimesList,
                                                outagesGetStmt);

                            /*
                             * IfService ifservice = new IfService(nodeid, ip,
                             * svcid, nodeName, svcname); Map svcOutages =
                             * (Map)m_services.get(svcname); if(svcOutages ==
                             * null) svcOutages = new HashMap();
                             * svcOutages.put(ifservice, outageSvcTimesList);
                             * m_services.put(svcname, svcOutages);
                             */
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.fatal("Unable to get node list for category '"
                    + cat.getLabel() + "'", e);
            throw e;
        } catch (FilterParseException e) {
            /*
             * If we get here, the error was most likely in getting the
             * nodelist from the filters.
             */
            log.fatal("Unable to get node list for category '"
                    + cat.getLabel() + "'", e);
            throw e;
        } catch (Exception e) {
            log.fatal("Unable to get node list for category '"
                    + cat.getLabel() + "'", e);

            // re-throw exception
            throw new Exception("Unable to get node list for category \'"
                    + cat.getLabel() + "\': " + e.getMessage(), e);
        } finally {
            d.cleanUp();
        }

    }

    /**
     * Get all outages for this nodeid/ipaddr/service combination and add it
     * to m_nodes.
     */
    private void getOutagesNodeIpSvc(int nodeid, String nodeName,
            String ipaddr, int serviceid, String serviceName,
            OutageSvcTimesList outageSvcTimesList,
            PreparedStatement outagesGetStmt) throws SQLException {
        org.apache.log4j.Category log = ThreadCategory.getInstance(AvailabilityData.class);
        // Get outages for this node/ip/svc pair
        final DBUtils d = new DBUtils(getClass());
        try {

            outagesGetStmt.setInt(1, nodeid);
            outagesGetStmt.setString(2, ipaddr);
            outagesGetStmt.setInt(3, serviceid);

            ResultSet rs = outagesGetStmt.executeQuery();
            d.watch(rs);

            if (m_nodes != null && m_nodes.size() > 0) {
                ListIterator<Node> lstIter = m_nodes.listIterator();
                boolean foundFlag = false;
                Node oldNode = null;
                while (lstIter.hasNext()) {
                    oldNode = (Node) lstIter.next();
                    if (oldNode != null && oldNode.getNodeID() == nodeid) {
                        foundFlag = true;
                        break;
                    }
                }
                if (!foundFlag) {
                    Node newNode = new Node(nodeName, nodeid);
                    newNode.addInterface(ipaddr, serviceName);
                    m_nodes.add(newNode);
                } else {
                    oldNode.addInterface(ipaddr, serviceName);
                }
            } else {
                Node newNode = new Node(nodeName, nodeid);
                newNode.addInterface(ipaddr, serviceName);
                m_nodes.add(newNode);
            }

            while (rs.next()) {
                Timestamp lost = rs.getTimestamp(1);
                Timestamp regained = rs.getTimestamp(2);
                long losttime = lost.getTime();
                long regainedtime = 0;

                if (regained != null) {
                    regainedtime = regained.getTime();
                }

                if (regainedtime > 0) {
                    if (regainedtime <= m_12MonthsBack
                            || losttime >= m_endTime) {
                        continue;
                    }
                } else {
                    if (losttime >= m_endTime) {
                        continue;
                    }
                }
                Outage outage = new Outage(losttime, regainedtime);
                outageSvcTimesList.add(outage);
                addNode(nodeName, nodeid, ipaddr, serviceName, losttime,
                        regainedtime);
            }

        } catch (SQLException e) {
            log.fatal("Error has occured while getting the outages ", e);
            throw e;
        } finally {
            d.cleanUp();
        }
    }

    /**
     * This method adds a unique tuple to the list of nodes m_nodes.
     *
     * @param nodeName a {@link java.lang.String} object.
     * @param nodeid a int.
     * @param ipaddr a {@link java.lang.String} object.
     * @param serviceid a {@link java.lang.String} object.
     * @param losttime a long.
     * @param regainedtime a long.
     */
    public void addNode(String nodeName, int nodeid, String ipaddr,
            String serviceid, long losttime, long regainedtime) {
        if (m_nodes == null) {
            m_nodes = new ArrayList<Node>();
        } else {
            if (m_nodes.size() <= 0) {
                Node newNode = new Node(nodeName, nodeid);
                // if(log.isDebugEnabled())
                // log.debug("Created the new node.");
                if (losttime > 0) {
                    if (regainedtime > 0) {
                        newNode.addInterface(ipaddr, serviceid, losttime,
                                             regainedtime);
                    } else {
                        newNode.addInterface(ipaddr, serviceid, losttime);
                    }
                } else {
                    newNode.addInterface(ipaddr, serviceid);
                }
                m_nodes.add(newNode);
                return;
            } else // look for the node with the nodeName
            {
                Node newNode = null;
                boolean foundFlag = false;
                ListIterator<Node> lstIter = m_nodes.listIterator();
                while (lstIter.hasNext()) {
                    newNode = lstIter.next();
                    if (newNode.getNodeID() == nodeid) {
                        foundFlag = true;
                        break;
                    }
                }
                if (!foundFlag) {
                    newNode = new Node(nodeName, nodeid);
                    if (losttime > 0) {
                        if (regainedtime > 0) {
                            newNode.addInterface(ipaddr, serviceid, losttime,
                                                 regainedtime);
                        } else {
                            newNode.addInterface(ipaddr, serviceid, losttime);
                        }
                    } else {
                        newNode.addInterface(ipaddr, serviceid);
                    }
                    m_nodes.add(newNode);
                    return;
                } else {
                    if (losttime > 0) {
                        if (regainedtime > 0) {
                            newNode.addInterface(ipaddr, serviceid, losttime,
                                                 regainedtime);
                        } else {
                            newNode.addInterface(ipaddr, serviceid, losttime);
                        }
                    } else {
                        newNode.addInterface(ipaddr, serviceid);
                    }
                    return;
                }
            }
        }
    }
}
