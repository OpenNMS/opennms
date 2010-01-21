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
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.reporting.availability;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.reporting.availability.CatSections;
import org.opennms.reporting.availability.Categories;
import org.opennms.reporting.availability.Category;
import org.opennms.reporting.availability.ClassicTable;
import org.opennms.reporting.availability.Col;
import org.opennms.reporting.availability.Report;
import org.opennms.reporting.availability.Row;
import org.opennms.reporting.availability.Rows;
import org.opennms.reporting.availability.Section;
import org.opennms.reporting.availability.Value;
import org.opennms.reporting.datablock.IfService;
import org.opennms.reporting.datablock.Interface;
import org.opennms.reporting.datablock.Node;
import org.opennms.reporting.datablock.OutageSince;
import org.opennms.reporting.datablock.OutageSvcTimesList;
import org.opennms.reporting.datablock.Service;

/**
 * AvailCalculations does all computations for all reports for a category. The
 * types include Last 30 days daily availability Last 30 days total availability
 * Last 30 days daily service availability Last Months Top 20 offenders Last
 * Months Top 20 Service outages Last N Months Availability Last Months Daily
 * Availability Last Months Total Availability Last Months Daily Service
 * Availability Month To Date Daily Availability Month To Date Total
 * Availability
 * 
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 * @author <A HREF="http://www.oculan.com">Oculan </A>
 */
public class AvailCalculations extends Object {
    /**
     * The log4j category used to log debug messsages and statements.
     */
    private static final String LOG4J_CATEGORY = "OpenNMS.Report";

    /**
     * Castor object that holds all the information required for the generating
     * xml to be translated to the pdf.
     */
    private static Report m_report = null;

    /**
     * End time
     */
    private long m_endTime;

    /**
     * Services map
     */
    private Map<String, Map<IfService, OutageSvcTimesList>> m_services = null;

    /**
     * End time of the last month.
     */
    private long m_endLastMonthTime;

    /**
     * Number of days in the last month.
     */
    private int m_daysInLastMonth;

    /**
     * The time in milliseconds per day.
     */
    private static final long ROLLING_WINDOW = 86400000l;

    /**
     * Constant
     */
    private static final int THIRTY = 30;

    /**
     * Constant (Number of months)
     */
    private static final int NMONTHS = 12;

    /**
     * Nodes that match this category.
     */
    private List<Node> m_nodes;

    /**
     * Monitored Services for the category
     */
    private List<String> m_monitoredServices;

    /**
     * This is used for the PDF Report generation
     */
    private int m_sectionIndex;

    /**
     * Constructor
     * 
     * @param nodes
     *            List of nodes
     * @param endTime
     *            End time ( end of yesterday in milliseconds)
     * @param lastMonthEndTime
     *            Last months end time (end of the last day of last month in
     *            milliseconds)
     * @param monitoredServices
     *            Monitored services belonging to the category.
     * @param report
     *            Castor Report class.
     * @param offenders
     *            Map of all offenders -- percent/(list of node) pairs
     * @param format
     *            Value can be "SVG / all"
     */
    public AvailCalculations(List<Node> nodes, long endTime, long lastMonthEndTime, List<String> monitoredServices, Report report, TreeMap<Double, List<String>> offenders, double warning, double normal, String comments, String name, String format, String monthFormat, int catIndex, int sectionIndex) {
        m_sectionIndex = sectionIndex;
        org.opennms.reporting.availability.Category category = new org.opennms.reporting.availability.Category();
        category.setWarning(warning);
        category.setNormal(normal);
        category.setCatComments(comments);
        category.setCatName(name);
        category.setCatIndex(catIndex);
        category.setNodeCount(nodes.size());
        int ipaddrCount = 0;
        int serviceCount = 0;
        
        for(Node tmpNode : nodes) {
            if (tmpNode != null) {
                ipaddrCount += tmpNode.getInterfaceCount();
                serviceCount += tmpNode.getServiceCount();
            }
        }
        category.setIpaddrCount(ipaddrCount);
        category.setServiceCount(serviceCount);

        org.opennms.reporting.availability.Categories categories = report.getCategories();
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        if (log.isDebugEnabled())
            log.debug("Inside AvailCalculations using endTime " + endTime);

        m_monitoredServices = monitoredServices;
        m_endLastMonthTime = lastMonthEndTime;
        m_daysInLastMonth = getDaysForMonth(m_endLastMonthTime);
        m_report = report;

        m_nodes = nodes;

        m_endTime = endTime;
        String label;
        String descr;


        // Please node the following 4 formats are displayed on the graphical
        // report.
        // (i) last12MoAvail
        // (ii) LastMonthsDailyAvailability
        // (iii) MonthToDateDailyAvailability
        // (iv) lastMoTop20offenders

        if (log.isDebugEnabled())
            log.debug("Now computing last 12 months daily availability ");
        //
        // N Months Availability
        //
        label = AvailabilityConstants.NMONTH_TOTAL_LABEL;
        descr = AvailabilityConstants.NMONTH_TOTAL_DESCR;
        if (label == null || label.length() == 0)
            label = "The last 12 Months Availability";
        if (descr == null || descr.length() == 0)
            descr = "The last 12 Months Availability";
        CatSections catSections = new CatSections();
        lastNMonthsAvailability(NMONTHS, m_endLastMonthTime, catSections, label, descr);
        if (log.isDebugEnabled())
            log.debug("Computed lastNMonthsAvailability");

        //
        // Last Months Daily Availability
        //
        if (log.isDebugEnabled())
            log.debug("Now computing last months daily availability ");
        label = AvailabilityConstants.LAST_MONTH_DAILY_LABEL;
        descr = AvailabilityConstants.LAST_MONTH_DAILY_DESCR;
        if (label == null || label.length() == 0)
            label = "The last Months Daily Availability";
        if (descr == null || descr.length() == 0)
            descr = "Daily Average of svcs monitored and availability of svcs divided by the total svc minutes (last month)";
        if (monthFormat.equalsIgnoreCase("calendar")){
			lastCalMoDailyAvailability(m_daysInLastMonth, m_endLastMonthTime, catSections, label, descr, "LastMonthsDailyAvailability");
        }else {
        	lastMoDailyAvailability(m_daysInLastMonth, m_endLastMonthTime, catSections, label, descr, "LastMonthsDailyAvailability");
        }
		if (log.isDebugEnabled())
        log.debug("Computed lastNDaysDailyAvailability");

        //
        // Month To Date Daily Availability
        //
        if (log.isDebugEnabled())
            log.debug("Now computing  month to date daily availability ");
        label = AvailabilityConstants.LAST_MTD_DAILY_LABEL;
        descr = AvailabilityConstants.LAST_MTD_DAILY_DESCR;
        if (label == null || label.length() == 0)
            label = "Month To Date Daily Availability";
        if (descr == null || descr.length() == 0)
            descr = "Daily Average of svc monitored and availability of svcs div by total svc minutes of month frm 1st till date";
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(m_endTime));
        int numDaysInMonth = calendar.get(Calendar.DAY_OF_MONTH);
		if (monthFormat.equalsIgnoreCase("calendar")){
			lastCalMTDDailyAvailability(numDaysInMonth, m_endTime, catSections, label, descr, "MonthToDateDailyAvailability");
		}else {
			lastMTDDailyAvailability(numDaysInMonth, m_endTime, catSections, label, descr, "MonthToDateDailyAvailability");
		}
        
        if (log.isDebugEnabled())
            log.debug("Computed lastNDaysDailyAvailability");

        //
        // Last Months Top Offenders
        //
        if (log.isDebugEnabled())
            log.debug("Now computing Last Months Top Offenders ");
        label = AvailabilityConstants.NOFFENDERS_LABEL;
        descr = AvailabilityConstants.NOFFENDERS_DESCR;
        if (label == null || label.length() == 0)
            label = "Last Months Top Offenders";
        if (descr == null || descr.length() == 0)
            descr = "This is the list of the worst available devices in the category for the last month";
        lastMoTopNOffenders(offenders, catSections, label, descr);
        if (log.isDebugEnabled())
            log.debug("Computed lastMoTopNOffenders ");

        //
        // Last N days Daily Availability
        //
        if (!format.equals("SVG")) {
            if (log.isDebugEnabled())
                log.debug("Now computing LAST_30_DAYS_DAILY_LABEL ");
            label = AvailabilityConstants.LAST_30_DAYS_DAILY_LABEL;
            descr = AvailabilityConstants.LAST_30_DAYS_DAILY_DESCR;
            if (label == null || label.length() == 0)
                label = "The last 30 Days Daily Availability";
            if (descr == null || descr.length() == 0)
                descr = "Daily average of svcs and dvcs monitored and their availability divided by total mins for 30days";
            lastNDaysDailyAvailability(THIRTY, m_endTime, catSections, label, descr, "Last30DaysDailyAvailability");
            if (log.isDebugEnabled())
                log.debug("Computed lastNDaysDailyAvailability");
        }

        //
        // N days total availability
        //
        if (!format.equals("SVG")) {
            if (log.isDebugEnabled())
                log.debug("Now computing LAST_30_DAYS_TOTAL_LABEL ");
            label = AvailabilityConstants.LAST_30_DAYS_TOTAL_LABEL;
            descr = AvailabilityConstants.LAST_30_DAYS_TOTAL_DESCR;
            if (label == null || label.length() == 0)
                label = "The last 30 Days Total Availability";
            if (descr == null || descr.length() == 0)
                descr = "Average of svcs monitored and availability of svcs divided by total svc minutes of the last 30 days";
            lastNDaysTotalAvailability(THIRTY, m_endTime, catSections, label, descr);
            if (log.isDebugEnabled())
                log.debug("Computed lastNDaysTotalAvailability");
        }

        //
        // Last Months Total Availability
        //
        if (!format.equals("SVG")) {
            if (log.isDebugEnabled())
                log.debug("Now computing LAST_MONTH_TOTAL_LABEL ");
            label = AvailabilityConstants.LAST_MONTH_TOTAL_LABEL;
            descr = AvailabilityConstants.LAST_MONTH_TOTAL_DESCR;
            if (label == null || label.length() == 0)
                label = "The last Months Total Availability";
            if (descr == null || descr.length() == 0)
                descr = "Average of svcs monitored and availability of svcs divided by the total svc minutes of the month";
            lastMoTotalAvailability(m_daysInLastMonth, m_endLastMonthTime, catSections, label, descr);
            if (log.isDebugEnabled())
                log.debug("Computed lastNDaysDailyAvailability");
        }

        //
        // Month To Date Total Availability
        //
        if (!format.equals("SVG")) {
            if (log.isDebugEnabled())
                log.debug("Now computing LAST_MTD_TOTAL_LABEL ");
            label = AvailabilityConstants.LAST_MTD_TOTAL_LABEL;
            descr = AvailabilityConstants.LAST_MTD_TOTAL_DESCR;
            if (label == null || label.length() == 0)
                label = "Month To Date Total Availability";
            if (descr == null || descr.length() == 0)
                descr = "Average of svc monitored and availability of svcs dividedby total svc minutes of month frm 1st till date";
            lastMoTotalAvailability(numDaysInMonth, m_endTime, catSections, label, descr);
            if (log.isDebugEnabled())
                log.debug("Computed MTDTotalAvailability");
        }

        m_services = new HashMap<String, Map<IfService, OutageSvcTimesList>>();
            
        for(Node node : nodes) {
            if (node != null) {
                for(Interface intf : node.getInterfaces()) {
                    if (intf != null) {
                        for(Service svc : intf.getServices()) {
                            if (svc != null) {
                                OutageSvcTimesList outages = svc.getOutages();
                                if (outages != null) {
                                    IfService ifservice = new IfService(node.getNodeID(), intf.getName(), -1, node.getName(), svc.getName());
                                    Map<IfService, OutageSvcTimesList> svcOutages = m_services.get(svc.getName());
                                    if (svcOutages == null)
                                        svcOutages = new HashMap<IfService, OutageSvcTimesList>();
                                    svcOutages.put(ifservice, outages);
                                    m_services.put(svc.getName(), svcOutages);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (log.isDebugEnabled())
            log.debug("Services " + m_services);
        m_nodes = null;
        //
        // N Days Daily Service Availability
        //
        if (!format.equals("SVG")) {
            if (log.isDebugEnabled())
                log.debug("Now computing LAST_30_DAYS_SVC_AVAIL_LABEL ");
            label = AvailabilityConstants.LAST_30_DAYS_SVC_AVAIL_LABEL;
            descr = AvailabilityConstants.LAST_30_DAYS_SVC_AVAIL_DESCR;
            if (label == null || label.length() == 0)
                label = "The last 30 days Daily Service Availability";
            if (descr == null || descr.length() == 0)
                descr = "The last 30 days Daily Service Availability is the daily average of services";
            lastNDaysDailyServiceAvailability(THIRTY, m_endTime, catSections, label, descr);
            if (log.isDebugEnabled())
                log.debug("Computed lastNDaysDailyServiceAvailability");
        }

        //
        // Last Months Daily Service Availability
        //
        if (!format.equals("SVG")) {
            if (log.isDebugEnabled())
                log.debug("Now computing LAST_MONTH_SVC_AVAIL_LABE");
            label = AvailabilityConstants.LAST_MONTH_SVC_AVAIL_LABEL;
            descr = AvailabilityConstants.LAST_MONTH_SVC_AVAIL_DESCR;
            if (label == null || label.length() == 0)
                label = "The last Months Daily Service Availability";
            if (descr == null || descr.length() == 0)
                descr = "The last Months Daily Service Availability is the daily average of services and devices";
            lastNDaysDailyServiceAvailability(m_daysInLastMonth, m_endLastMonthTime, catSections, label, descr);
            if (log.isDebugEnabled())
                log.debug("Computed lastNDaysDailyServiceAvailability");
        }

        //
        // Top N Service Outages
        //
        if (!format.equals("SVG")) {
            if (log.isDebugEnabled())
                log.debug("Now computing TOP20_SVC_OUTAGES_LABEL");
            label = AvailabilityConstants.TOP20_SVC_OUTAGES_LABEL;
            descr = AvailabilityConstants.TOP20_SVC_OUTAGES_DESCR;
            if (label == null || label.length() == 0)
                label = "Last Month Top Service Outages for ";
            if (descr == null || descr.length() == 0)
                descr = "Last Month Top Service Outages for ";
            lastMonTopNServiceOutages(catSections, label, descr);
            if (log.isDebugEnabled())
                log.debug("Computed lastMonTopNServiceOutages");
        }

        m_services = null;
        category.addCatSections(catSections);
        categories.addCategory(category);

        m_report.setCategories(categories);
        report = m_report;

        if (log.isDebugEnabled())
            log.debug("Leaving AvailCalculations");
    }

    public int getSectionIndex() {
        return m_sectionIndex;
    }

    /**
     * Last Months Top N Service Outages.
     * 
     * @param catSections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     */
    private void lastMonTopNServiceOutages(CatSections catSections, String label, String descr) {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        // Result is a map of outage / lost time
        //

        // For each monitored service, get all individual outages.
        //
        TreeMap<Long, List<OutageSince>> treeMap = null;
        
        for(String service : m_services.keySet()) {
            treeMap = new TreeMap<Long, List<OutageSince>>();
            Map<IfService, OutageSvcTimesList> ifSvcOutageList = m_services.get(service);
            
            for(IfService ifservice : ifSvcOutageList.keySet()) {
                if (ifservice != null) {
                    OutageSvcTimesList outageSvcList = (OutageSvcTimesList) ifSvcOutageList.get(ifservice);
                    if (outageSvcList != null) {
                        long rollingWindow = m_daysInLastMonth * ROLLING_WINDOW;
                        List<OutageSince> svcOutages = outageSvcList.getServiceOutages(ifservice.getNodeName(), m_endLastMonthTime, rollingWindow);
                        for(OutageSince outageSince : svcOutages) {
                            if (outageSince != null) {
                                long outage = outageSince.getOutage() / 1000;
                                List<OutageSince> tmpList = treeMap.get(new Long(outage));
                                if (tmpList == null)
                                    tmpList = new ArrayList<OutageSince>();
                                tmpList.add(outageSince);
                                treeMap.put(new Long(-1 * outage), tmpList);
                            }
                        }
                    }
                }
            }
            log.debug("Top 20 service outages from the list " + treeMap);
            
            
            int top20Count = 0;
            Rows rows = new Rows();

            loop : for(Long outage : treeMap.keySet()) {
                List<OutageSince> list = treeMap.get(outage);
                for(OutageSince outageSince : list) {
                    top20Count++;
                    String nodeName = outageSince.getNodeName();

                    Value nodeValue = new Value();
                    nodeValue.setContent(nodeName);
                    nodeValue.setType("title");

                    Value value = new Value();
                    long outtime = outageSince.getOutage() / 1000;
                    int hrs = (new Long(outtime / (60 * 60))).intValue();
                    int remain = (new Long(outtime % (60 * 60))).intValue();
                    int mins = remain / (60);
                    remain = remain % (60);
                    int secs = remain;
                    log.debug("Outage : " + outtime + " in mins " + hrs + " hrs " + mins + " mins " + secs + " secs ");
                    value.setContent(hrs + " hrs " + mins + " mins " + secs + " secs ");
                    value.setType("data");

                    Value datevalue = new Value();
                    SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                    datevalue.setContent(fmt.format(new Date(outageSince.getOutTime())));
                    datevalue.setType("other");

                    Row row = new Row();
                    row.addValue(nodeValue);
                    row.addValue(value);
                    row.addValue(datevalue);
                    rows.addRow(row);

                    if (top20Count >= 20) {
                        break loop;
                    }
                }
            }
            Col col = new Col();
            col.addColTitle(0, "Node Name");
            col.addColTitle(1, "Duration Of Outage");
            col.addColTitle(2, "Service Lost Time");
			ClassicTable table = new ClassicTable();
			table.setCol(col);
			table.setRows(rows);
			Section section = new Section();
			section.setClassicTable(table);
            section.setSectionName(label + " " + service);
            section.setSectionTitle(label + " " + service);
            section.setSectionDescr(descr + " " + service);
            section.setSectionIndex(m_sectionIndex);
            m_sectionIndex++;
            catSections.addSection(section);
        }
    }

    /**
     * Last Month To Date Daily Availability
     * 
     * @param days
     *            Number of days for which the availability computations are
     *            made.
     * @param endTime
     *            End time
     * @param sections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     * @param sectionName
     *            Section name.
     */
    private void lastMTDDailyAvailability(int days, long endTime, CatSections sections, String label, String descr, String sectionName) {
        lastNDaysDailyAvailability(days, endTime, sections, label, descr, sectionName);
    }

    /**
     * Last Month To Date Daily Availability
     * 
     * @param days
     *            Number of days for which the availability computations are
     *            made.
     * @param endTime
     *            End time
     * @param sections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     * @param sectionName
     *            Section name.
     */
    private void lastCalMTDDailyAvailability(int days, long endTime, CatSections sections, String label, String descr, String sectionName) {
        lastNDaysCalDailyAvailability(days, endTime, sections, label, descr, sectionName);
    }
    /**
     * Last N Days Total Availability.
     * 
     * @param days
     *            Number of days for which the availability computations are
     *            made.
     * @param endTime
     *            End time
     * @param catSections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     */
    private void lastMoTotalAvailability(int days, long endTime, CatSections catSections, String label, String descr) {
        lastNDaysTotalAvailability(days, endTime, catSections, label, descr);
    }
	
	
    /**
     * 
     * Last Months Top N offenders.
     * 
     * @param offenders
     *            Top Offenders
     * @param catSections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     * 
     */
    private void lastMoTopNOffenders(TreeMap<Double, List<String>> offenders, CatSections catSections, String label, String descr) {
        // copy this method from the outage data code.
        //
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        if (log.isDebugEnabled()) {
            log.debug("Offenders " + offenders);
            log.debug("Inside lastMoTopNOffenders");
        }
        Set<Double> percentValues = offenders.keySet();
        Iterator<Double> iter = percentValues.iterator();

        Rows rows = new Rows();
        int top20Count = 0;
        loop: while (iter.hasNext()) {
            Double percent = (Double) iter.next();
            if (percent.doubleValue() < 100.0) {
                List<String> nodeNames = offenders.get(percent);
                if (nodeNames != null) {
                    ListIterator<String> lstIter = nodeNames.listIterator();
                    while (lstIter.hasNext()) {
                        top20Count++;
                        String nodeName = lstIter.next();

                        Value dateValue = new Value();
                        dateValue.setContent(nodeName);
                        dateValue.setType("title");

                        Value value = new Value();
                        value.setContent(formatNumber("" + percent.doubleValue()));
                        value.setType("data");

                        Row row = new Row();
                        row.addValue(dateValue);
                        row.addValue(value);
                        rows.addRow(row);

                        if (top20Count >= 20) {
                            break loop;
                        }
                    }
                }
            }
        }
        Col col = new Col();
        col.addColTitle(0, "Node Name");
        col.addColTitle(1, "Percentage Availability");
		ClassicTable table = new ClassicTable();
		table.setCol(col);
		table.setRows(rows);
		Section section = new Section();
		section.setClassicTable(table);
        section.setSectionName("lastMoTop20offenders");
        section.setSectionTitle(label);
        section.setSectionDescr(descr);
        section.setSectionIndex(m_sectionIndex);
        m_sectionIndex++;
        catSections.addSection(section);
        if (log.isDebugEnabled())
            log.debug("Leaving lastMoTopNOffenders");
    }

    /**
     * Last Months Daily availability
     * 
     * @param days
     *            Number of days for which the availability computations are
     *            made
     * @param endTime
     *            End time
     * @param sections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     * @param sectionName
     *            Section name
     */
    private void lastMoDailyAvailability(int days, long endTime, CatSections sections, String label, String descr, String sectionName) {
        lastNDaysDailyAvailability(days, endTime, sections, label, descr, sectionName);
    }
	/**
     * Last Months Daily availability
     * 
     * @param days
     *            Number of days for which the availability computations are
     *            made
     * @param endTime
     *            End time
     * @param sections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     * @param sectionName
     *            Section name
     */
    private void lastCalMoDailyAvailability(int days, long endTime, CatSections sections, String label, String descr, String sectionName) {
        lastNDaysCalDailyAvailability(days, endTime, sections, label, descr, sectionName);
    }
	
	/**
     * Last N days daily availability.
     * 
     * @param days
     *            Number of days for which the availability computations are
     *            made.
     * @param endTime
     *            End time
     * @param catSections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     * @param sectionName
     *            Section name
     */
    private void lastNDaysCalDailyAvailability(int days, long endTime, CatSections catSections, String label, String descr, String sectionName) {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        if (log.isDebugEnabled())
            log.debug("Inside lastNDaysDailyAvailability");
        int numdays = 0;
	    CalendarTableBuilder calBuilder = new CalendarTableBuilder(endTime);
        TreeMap<Date, Double> treeMap = new TreeMap<Date, Double>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM, yyyy");
        String periodEnd = fmt.format(new java.util.Date(endTime));
        String periodFrom = "";
        while (numdays++ < days) {
            if (log.isDebugEnabled())
                log.debug("Computing for " + new Date(endTime));
            int serviceCount = 0;
            long outage = 0;
            //
            // get the outage and service count.
            //
            for(Node node : m_nodes) {
                outage += node.getOutage(endTime, ROLLING_WINDOW);
                serviceCount += node.getServiceCount();
            }
            double percentAvail;
            if (serviceCount > 0){
                
                log.debug("LOOK: calculating percentAvail using outage " + outage + " service count " + serviceCount + " ROLLING_WINODW " + ROLLING_WINDOW + " endTime " + endTime);
                percentAvail = 100.0 * (1 - (outage * 1.0) / (1.0 * serviceCount * ROLLING_WINDOW));
        }
            else
                percentAvail = 100.0;

            //need a double object in here
			
			treeMap.put(new Date(endTime), new Double (percentAvail));
                        Date nicedate = new Date(endTime);
                        log.debug("Inserting " + percentAvail + " into " + nicedate);

            periodFrom = fmt.format(new java.util.Date(endTime));
            endTime -= ROLLING_WINDOW;
        }

        Set<Date> keyDates = treeMap.keySet();
        Iterator<Date> iter = keyDates.iterator();
        int dateSlot = 0;
		while (iter.hasNext()) {
			Date key = iter.next();
            Double percent = treeMap.get(key);
                        log.debug("Inserting value " + percent.doubleValue() + " into date slot " + dateSlot);
			dateSlot++;
                        log.debug("Inserting value " + percent.doubleValue() + " into date slot " + dateSlot);
			calBuilder.setPctValue(dateSlot, percent.doubleValue());
		}
		
		Section section = new Section();
        section.setCalendarTable(calBuilder.getTable());
        section.setSectionName(sectionName); // "LastMonthsDailyAvailability");
        section.setSectionTitle(label);
        section.setSectionDescr(descr);
        section.setPeriod(periodFrom + " to " + periodEnd);
        section.setSectionIndex(m_sectionIndex);
        m_sectionIndex++;
        catSections.addSection(section);
        log.debug("Leaving lastNDaysCalDailyAvailability");
		
		}

	
    /**
     * Last N days daily availability.
     * 
     * @param days
     *            Number of days for which the availability computations are
     *            made.
     * @param endTime
     *            End time
     * @param catSections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     * @param sectionName
     *            Section name
     */
    private void lastNDaysDailyAvailability(int days, long endTime, CatSections catSections, String label, String descr, String sectionName) {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        if (log.isDebugEnabled())
            log.debug("Inside lastNDaysDailyAvailability");
        int numdays = 0;
        Rows rows = new Rows();
        TreeMap<Date, String> treeMap = new TreeMap<Date, String>();
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM, yyyy");
        String periodEnd = fmt.format(new java.util.Date(endTime));
        String periodFrom = "";
        while (numdays++ < days) {
            if (log.isDebugEnabled())
                log.debug("Computing for " + new Date(endTime));
            int serviceCount = 0;
            long outage = 0;
            //
            // get the outage and service count.
            //
            for(Node node : m_nodes) {
                outage += node.getOutage(endTime, ROLLING_WINDOW);
                serviceCount += node.getServiceCount();
            }
            double percentAvail;
            if (serviceCount > 0)
                percentAvail = 100.0 * (1 - (outage * 1.0) / (1.0 * serviceCount * ROLLING_WINDOW));
            else
                percentAvail = 100.0;

            treeMap.put(new Date(endTime), formatNumber(percentAvail + ""));

            periodFrom = fmt.format(new java.util.Date(endTime));
            endTime -= ROLLING_WINDOW;
        }

        Set<Date> keyDates = treeMap.keySet();
        Iterator<Date> iter = keyDates.iterator();
        while (iter.hasNext()) {
            Date key = iter.next();
            Value dateValue = new Value();
            SimpleDateFormat fmtmp = new SimpleDateFormat("dd");
            dateValue.setContent(fmtmp.format(key));
            dateValue.setType("title");

            String percent = treeMap.get(key);
            Value value = new Value();
            value.setContent(percent);
            value.setType("data");

            Row row = new Row();
            row.addValue(dateValue);
            row.addValue(value);
            rows.addRow(row);

        }

        Col col = new Col();
        col.addColTitle(0, "Date");
        col.addColTitle(1, "Percentage Availability");
		ClassicTable table = new ClassicTable();
		table.setCol(col);
		table.setRows(rows);
		Section section = new Section();
		section.setClassicTable(table);
        section.setSectionName(sectionName); // "LastMonthsDailyAvailability");
        section.setSectionTitle(label);
        section.setSectionDescr(descr);
        section.setPeriod(periodFrom + " to " + periodEnd);
        section.setSectionIndex(m_sectionIndex);
        m_sectionIndex++;
        catSections.addSection(section);
        log.debug("Leaving lastNDaysDailyAvailability");
    }

    /**
     * Last N Days Total Availability.
     * 
     * @param days
     *            Number of days for which the availability computations are
     *            made.
     * @param endTime
     *            End time
     * @param catSections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     */
    private void lastNDaysTotalAvailability(int days, long endTime, CatSections catSections, String label, String descr) {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        log.debug("Inside lastNDaysTotalAvailability");
        Rows rows = new Rows();
        int serviceCount = 0;
        long outage = 0;
        int numdays = 0;
        SimpleDateFormat fmt = new SimpleDateFormat("dd MMM, yyyy");
        String periodEnd = fmt.format(new java.util.Date(endTime));
        String periodFrom = "";
        while (numdays++ < days) {
            //
            // get the outage and service count.
            //
            for(Node node : m_nodes) {
                serviceCount += node.getServiceCount();
                outage += node.getOutage(endTime, ROLLING_WINDOW);
            }
            periodFrom = fmt.format(new java.util.Date(endTime)) + " to " + periodEnd;
            endTime -= ROLLING_WINDOW;
        }
        double percentAvail;
        if (serviceCount > 0)
            percentAvail = 100.0 * (1 - (outage * 1.0) / (1.0 * serviceCount * ROLLING_WINDOW));
        else
            percentAvail = 100.0;
        Value dateValue = new Value();
        dateValue.setContent(periodFrom);
        dateValue.setType("title");

        Value value = new Value();
        value.setContent(formatNumber(percentAvail + ""));
        value.setType("data");

        Row row = new Row();
        row.addValue(dateValue);
        row.addValue(value);
        rows.addRow(row);
        Col col = new Col();
        col.addColTitle(0, "Date");
        col.addColTitle(1, "Percentage Availability");
		ClassicTable table = new ClassicTable();
		table.setCol(col);
		table.setRows(rows);
		Section section = new Section();
		section.setClassicTable(table);
        section.setSectionName("Last" + days + "TotalAvailability");
        section.setSectionTitle(label);
        section.setSectionDescr(descr);
        section.setPeriod(periodFrom);
        section.setSectionIndex(m_sectionIndex);
        m_sectionIndex++;
        catSections.addSection(section);
        log.debug("Leaving lastNDaysTotalAvailability");
    }

    /**
     * Last N Months Availability
     * 
     * @param nMonths
     *            Number of months for which the availability computations are
     *            made.
     * @param endTime
     *            End time
     * @param catSections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     */
    private void lastNMonthsAvailability(int nMonths, long endTime, CatSections catSections, String label, String descr) {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        log.debug("Inside lastNMonthsAvailability");
        Rows rows = new Rows();
        int numMonths = 0;

        int numDays = getDaysForMonth(endTime);
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date(endTime));
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        calendar.set(year, month, numDays, 23, 59, 59);
        endTime = calendar.getTime().getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("MMM, yyyy");
        String periodEnd = fmt.format(new java.util.Date(endTime));
        TreeMap<Date, String> treeMap = new TreeMap<Date, String>(); // Holds all the month/percent
                                            // values to be displayed in order
                                            // on pdf.
        String periodFrom = null;

        while (numMonths++ < nMonths) {
            int serviceCount = 0;
            long outage = 0;
            log.debug("Number of days " + numDays + " in month of " + new Date(endTime));
            long rollingWindow = numDays * ROLLING_WINDOW * 1l;
            //
            // get the outage and service count.
            //
            for(Node node : m_nodes) {
                serviceCount += node.getServiceCount();
                outage += node.getOutage(endTime, rollingWindow);
            }
            double percentAvail;
            if (serviceCount > 0)
                percentAvail = 100.0 * (1 - (outage * 1.0) / (1.0 * serviceCount * rollingWindow));
            else
                percentAvail = 100.0;

            treeMap.put(new java.util.Date(endTime), formatNumber(percentAvail + ""));

            periodFrom = fmt.format(new java.util.Date(endTime));
            calendar = new GregorianCalendar();
            calendar.setTime(new Date(endTime));
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);

            calendar.set(year, month - 1, 1, 0, 0, 0);
            endTime = calendar.getTime().getTime();
            month = calendar.get(Calendar.MONTH);
            year = calendar.get(Calendar.YEAR);

            numDays = getDaysForMonth(endTime);
            calendar.set(year, month, numDays, 23, 59, 59);
            endTime = calendar.getTime().getTime();
        }

        Set<Date> keyDates = treeMap.keySet();
        Iterator<Date> iter = keyDates.iterator();
        while (iter.hasNext()) {
            Date key = iter.next();
            Value dateValue = new Value();
            SimpleDateFormat fmtmp = new SimpleDateFormat("MMM");
            dateValue.setContent(fmtmp.format(key) + "");
            dateValue.setType("title");

            Value value = new Value();
            String percent = treeMap.get(key);
            value.setContent(percent);
            value.setType("data");

            Row row = new Row();
            row.addValue(dateValue);
            row.addValue(value);
            rows.addRow(row);
        }

        Col col = new Col();
        col.addColTitle(0, "Date");
        col.addColTitle(1, "Percentage Availability");
		ClassicTable table = new ClassicTable();
		table.setCol(col);
		table.setRows(rows);
		Section section = new Section();
		section.setClassicTable(table);
        section.setSectionName("last12MoAvail");
        section.setSectionTitle(label);
        section.setSectionDescr(descr);
        section.setPeriod(periodFrom + " to " + periodEnd);
        section.setSectionIndex(m_sectionIndex);
        m_sectionIndex++;
        catSections.addSection(section);
        log.debug("Leaving lastNMonthsAvailability");
    }

    /**
     * Returns the number of days in the month, also considers checks for leap
     * year.
     * 
     * @param isLeap
     *            the leap year flag.
     * @param month
     *            The month whose days count is reqd
     */
    private static synchronized int getDays(boolean isLeap, int month) {
        switch (month) {
        case 0:
        case 2:
        case 4:
        case 6:
        case 7:
        case 9:
        case 11:
            return 31;

        case 3:
        case 5:
        case 8:
        case 10:
            return 30;

        case 1:
            if (isLeap)
                return 29;
            else
                return 28;
        }
        return -1;
    }

    /**
     * Returns the number of Days in the month
     * 
     * @param endTime
     *            The end of the month (time in milliseconds)
     */
    private int getDaysForMonth(long endTime) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(new java.util.Date(endTime));
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        return (getDays(calendar.isLeapYear(year), month));
    }

    /**
     * Compute N days daily service availability.
     * 
     * @param endTime
     *            End time
     * @param catSections
     *            Castors sections
     * @param label
     *            Section name in the xml
     * @param descr
     *            Section descr.
     */
    private void lastNDaysDailyServiceAvailability(int days, long endTime, CatSections catSections, String label, String descr) {
        ThreadCategory.setPrefix(LOG4J_CATEGORY);
        org.apache.log4j.Category log = ThreadCategory.getInstance(this.getClass());
        log.debug("Inside lastNDaysDailyServiceAvailability " + days);


        long outage;
        String periodFrom = "";
        SimpleDateFormat fmtmp = new SimpleDateFormat("dd MMM, yyyy");
        String periodTo = "";
        periodTo = fmtmp.format(new java.util.Date(endTime));
        for(String service : m_monitoredServices) {
            TreeMap<Date, Double> treeMap = new TreeMap<Date, Double>();
            Rows rows = new Rows();
            log.debug("SERvice " + service);

            long curTime = endTime;
            Map<IfService, OutageSvcTimesList> svcOutages = null;
            if (m_services != null)
                svcOutages = m_services.get(service);
            if (svcOutages == null || svcOutages.size() <= 0) {
                int daysCnt = 0;
                while (daysCnt++ < days) {
                    log.debug("DAy 100 % : " + daysCnt);
                    periodFrom = fmtmp.format(new java.util.Date(curTime));

                    treeMap.put(new java.util.Date(curTime), new Double(100.0));

                    curTime -= ROLLING_WINDOW;
                }

                Set<Date> keys = treeMap.keySet();
                Iterator<Date> iter = keys.iterator();
                while (iter.hasNext()) {
                    Date tmp = iter.next();
                    Value dateValue = new Value();
                    SimpleDateFormat fmt = new SimpleDateFormat("dd");
                    dateValue.setContent(fmt.format(tmp) + "");
                    dateValue.setType("title");

                    Value value = new Value();
                    value.setContent("100.0");
                    value.setType("data");

                    Row row = new Row();
                    row.addValue(dateValue);
                    row.addValue(value);
                    rows.addRow(row);
                }

                Col col = new Col();
                col.addColTitle(0, "Date");
                col.addColTitle(1, "Percentage Availability");
				ClassicTable table = new ClassicTable();
				table.setCol(col);
				table.setRows(rows);
				Section section = new Section();
				section.setClassicTable(table);
                section.setPeriod(periodFrom + " to " + periodTo);
                section.setSectionName(label + service);
                section.setSectionTitle(label + service);
                section.setSectionDescr(descr + service);
                section.setSectionIndex(m_sectionIndex);
                m_sectionIndex++;
                catSections.addSection(section);
            } else {
                int daysCnt = 0;
                while (daysCnt++ < days) {
                    log.debug("DAy : " + daysCnt + " end time " + new Date(curTime) + " " + " ROLLING_WINDOW " + ROLLING_WINDOW);
                    int serviceCnt = 0;
                    long outageTime = 0;
                    // For each node in the service table.
                    //
                    // Iterate each svc node for getting the ifservice
                    Set<IfService> keys = svcOutages.keySet();
                    Iterator<IfService> iter = keys.iterator();
                    while (iter.hasNext()) {
                        IfService ifservice = (IfService) iter.next();
                        log.debug(ifservice);
                        OutageSvcTimesList outageList = (OutageSvcTimesList) svcOutages.get(ifservice);
                        if (outageList != null) {
                            outage = outageList.getDownTime(curTime, ROLLING_WINDOW);
                            // Keep track of the number of services being
                            // monitored.
                            //
                            outageTime += outage;
                        }
                        serviceCnt++;
                    }
                    log.debug("Outage Time " + outageTime);
                    long den = (ROLLING_WINDOW * serviceCnt);
                    double outag = 1.0 * outageTime;
                    double denom = 1.0 * den;
                    double cal = 0;
                    if (den > 0)
                        cal = 100.0 * (1 - (outag / denom));

                    treeMap.put(new java.util.Date(curTime), new Double(cal));

                    periodFrom = fmtmp.format(new java.util.Date(curTime));
                    log.debug("Added to svc list " + new java.util.Date(curTime));
                    curTime -= ROLLING_WINDOW;
                }

                Set<Date> keys = treeMap.keySet();
                Iterator<Date> iter = keys.iterator();
                while (iter.hasNext()) {
                    Date tmp = iter.next();
                    Value dateValue = new Value();
                    SimpleDateFormat fmt = new SimpleDateFormat("dd");
                    dateValue.setContent(fmt.format(tmp) + "");
                    dateValue.setType("title");

                    Double val = treeMap.get(tmp);

                    Value value = new Value();
                    value.setContent("" + val);
                    value.setType("data");

                    Row row = new Row();
                    row.addValue(dateValue);
                    row.addValue(value);
                    rows.addRow(row);
                }

                Col col = new Col();
                col.addColTitle(0, "Date");
                col.addColTitle(1, "Percentage Availability");
				ClassicTable table = new ClassicTable();
				table.setCol(col);
				table.setRows(rows);
				Section section = new Section();
				section.setClassicTable(table);
                section.setPeriod(periodFrom + " to " + periodTo);
                section.setSectionName(label + service);
                section.setSectionTitle(label + service);
                section.setSectionDescr(descr + service);
                section.setSectionIndex(m_sectionIndex);
                m_sectionIndex++;
                catSections.addSection(section);
            }
        }
        log.debug("Leaving lastNDaysDailyServiceAvailability");
    }

    /**
     * Format the number (String) and return 6 digits of the number
     */
    private String formatNumber(String num) {
        if (num.indexOf(".") == 0) {
            num = "0" + num;
        }
        if (num.indexOf(".") == -1) {
            num = num + ".0";
        }
        num = num + "000000";
        return (num.substring(0, num.indexOf(".") + 6));
    }

}
