/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.reporting.availability;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.logging.Logging;
import org.opennms.netmgt.config.CategoryFactory;
import org.opennms.netmgt.config.api.CatFactory;
import org.opennms.netmgt.config.categories.Categorygroup;
import org.opennms.netmgt.config.categories.Catinfo;
import org.opennms.reporting.availability.svclayer.AvailabilityDataService;
import org.opennms.reporting.datablock.Node;

/**
 * AvailabilityData collects all the outages for all node/ip/service
 * combination and stores it appropriately in the m_nodes structure.
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 */
public class AvailabilityData {
    private static final Logger LOG = LoggerFactory.getLogger(AvailabilityData.class);
    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "reports";

    /**
     * List of Node objects that satisfy the filter rule for the category.
     */
    private List<Node> m_nodes;

    /**
     * End Time of the report.
     */
    private long m_endTime;

    /**
     * End Time of the report.
     */
    private long m_startTime;

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
    * Availability Data Service
    */
    
    private AvailabilityDataService m_availabilityDataService;
    
    // This version used when end date availalable as strings (from command line?)
    
    /**
     * <p>fillReport</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     * @param report a {@link org.opennms.reporting.availability.Report} object.
     * @param format a {@link java.lang.String} object.
     * @param monthFormat a {@link java.lang.String} object.
     * @param startMonth a {@link java.lang.String} object.
     * @param startDate a {@link java.lang.String} object.
     * @param startYear a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.lang.Exception if any.
     */
    public void fillReport(String categoryName, Report report,
            String format, String monthFormat,
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
        generateData(categoryName, report, format, monthFormat,
                     new Date(cal.getTimeInMillis()));
    }
    
    // This version used when end date availalable as a java Date

    /**
     * <p>fillReport</p>
     *
     * @param categoryName a {@link java.lang.String} object.
     * @param report a {@link org.opennms.reporting.availability.Report} object.
     * @param format a {@link java.lang.String} object.
     * @param monthFormat a {@link java.lang.String} object.
     * @param periodEndDate a {@link java.util.Date} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.lang.Exception if any.
     */
    public void fillReport(String categoryName, Report report,
            String format, String monthFormat, Date periodEndDate)
            throws IOException, MarshalException, ValidationException,
            Exception {
       generateData(categoryName, report, format, monthFormat, periodEndDate);
    }
    

    private void generateData(final String categoryName, final Report report,
            final String format, final String monthFormat,
            final Date periodEndDate)
            throws IOException, MarshalException, ValidationException,
            Exception {
        
        Logging.withPrefix(LOG4J_CATEGORY, new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                LOG.debug("Inside AvailabilityData");
                
                m_nodes = new ArrayList<Node>();
                
                initializeInterval(periodEndDate);
                
                Catinfo config = null;
                try {
                    CategoryFactory.init();
                    m_catFactory = CategoryFactory.getInstance();
                    config = m_catFactory.getConfig();
                } catch (IOException e) {
                    LOG.error("Initializing CategoryFactory", e);
                    throw e;
                } catch (MarshalException e) {
                    LOG.error("Initializing CategoryFactory", e);
                    throw e;
                } catch (ValidationException e) {
                    LOG.error("Initializing CategoryFactory", e);
                    throw e;
                }
                
                // FIXME There's some magic in here regarding multiple categories in a report
                
                LOG.debug("CATEGORY {}", categoryName);
                
                m_catFactory.getReadLock().lock();
                try {
                    if (categoryName.equals("") || categoryName.equals("all")) {
                        int catCount = 0;
                        LOG.debug("catCount {}", catCount);
                        
                        for(final Categorygroup cg : config.getCategorygroupCollection()) {
                        
                            for(org.opennms.netmgt.config.categories.Category cat : cg.getCategories().getCategoryCollection()) {
                
                                LOG.debug("CATEGORY {}", cat.getLabel());
                                catCount++;
                                populateDataStructures(cat, report, format, monthFormat, catCount);
                            }
                        }
                        LOG.debug("catCount {}", catCount);
                    } else {
                        org.opennms.netmgt.config.categories.Category cat = (org.opennms.netmgt.config.categories.Category) m_catFactory.getCategory(categoryName);
                        LOG.debug("CATEGORY - now populating data structures {}", cat.getLabel());
                        populateDataStructures(cat, report, format, monthFormat, 1);
                    }
                
                    final SimpleDateFormat simplePeriod = new SimpleDateFormat("MMMMMMMMMMM dd, yyyy");
                    final String reportPeriod = simplePeriod.format(new java.util.Date(m_startTime)) + " - " + simplePeriod.format(new java.util.Date(m_endTime));
                    Created created = report.getCreated();
                    if (created == null) {
                        created = new Created();
                    }
                    created.setPeriod(reportPeriod);
                    report.setCreated(created);
                } finally {
                    m_catFactory.getReadLock().unlock();
                }
                
                LOG.debug("After availCalculations");
                return null;
            }
        });

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

        LOG.debug("Inside populate data Structures {}", catIndex);
        report.setCatCount(catIndex);
        LOG.debug("Inside populate data Structures");
        try {

            List<String> monitoredServices = new ArrayList<String>(cat.getServiceCollection());

            if (m_availabilityDataService == null) {
                LOG.debug("DATA SERVICE IS NULL");
                throw new IllegalStateException("Data service is null");
            }
            m_nodes = m_availabilityDataService.getNodes(cat, m_startTime, m_endTime);
            
            LOG.debug("Nodes {}", m_nodes);
            
            // remove all the nodes that do not have outages
            
            ListIterator<Node> cleanNodes = m_nodes.listIterator();
            while (cleanNodes.hasNext()) {
                Node node = (Node) cleanNodes.next();
                if (node != null && !node.hasOutages()) {
                    LOG.debug("Removing node: {}", node);
                    cleanNodes.remove();
                }
            }
            LOG.debug("Cleaned Nodes {}", m_nodes);
            
            Map<Double, List<String>> topOffenders = getPercentNode();

            LOG.debug("TOP OFFENDERS {}", topOffenders);
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
                org.opennms.reporting.availability.Category category = new org.opennms.reporting.availability.Category();
                category.setCatComments(cat.getComment());
                category.setCatName(cat.getLabel());
                category.setCatIndex(catIndex);
                category.setNodeCount(0);
                category.setIpaddrCount(0);
                category.setServiceCount(0);
                Section section = new Section();
                section.setSectionIndex(m_sectionIndex);
                org.opennms.reporting.availability.CatSections catSections = new org.opennms.reporting.availability.CatSections();
                catSections.addSection(section);
                category.addCatSections(catSections);
                org.opennms.reporting.availability.Categories categories = report.getCategories();
                categories.addCategory(category);
                report.setCategories(categories);
                report.setSectionCount(m_sectionIndex);
                m_sectionIndex++;
            }
        } catch (Throwable e) {
            LOG.error("Exception has occurred", e);
            throw new Exception(e);
        }
    }

    /**
     * Initialize the endTime, start Time, last Months end time and number of days in the
     * last month.
     */
    
    private void initializeInterval(Date periodEndDate) {
        
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
        
        m_startTime = tempCal.getTimeInMillis();
        
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
     * Returns percent/node combinations for the last month. This is used to
     * get the last months top 20 offenders
     *
     * @return a {@link java.util.TreeMap} object.
     */
    public Map<Double, List<String>> getPercentNode() {
        int days = m_daysInLastMonth;
        long endTime = m_lastMonthEndTime;
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(endTime);
        cal.add(Calendar.DATE, -1 * days);
        long rollingWindow = endTime - cal.getTime().getTime();
        long startTime = cal.getTime().getTime();
        LOG.debug("getPercentNode: Start time {}", new java.util.Date(startTime));
        LOG.debug("getPercentNode: End time {}", new java.util.Date(endTime));
        TreeMap<Double, List<String>> percentNode = new TreeMap<Double, List<String>>();
        
        for(Node node : m_nodes) {
            if (node != null) {
                double percent = node.getPercentAvail(endTime, rollingWindow);
                String nodeName = node.getName();
                LOG.debug("Node {} {} %", nodeName, percent);
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
        LOG.debug("Percent node {}", percentNode);
        return percentNode;
    }


    /**
     * <p>setAvailabilityDataService</p>
     *
     * @param availabilityDataService a {@link org.opennms.reporting.availability.svclayer.AvailabilityDataService} object.
     */
    public void setAvailabilityDataService(
            AvailabilityDataService availabilityDataService) {
        LOG.debug("setting m_availabilityDataService");
        m_availabilityDataService = availabilityDataService;
    }
}
