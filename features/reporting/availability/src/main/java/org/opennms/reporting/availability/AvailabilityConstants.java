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

/**
 * This class is a repository for constant, static information concerning the
 * Availability Reporter Module.
 *
 * @author <A HREF="mailto:jacinta@oculan.com">Jacinta Remedios </A>
 */
public abstract class AvailabilityConstants {
    /**
     * The SQL statement that is used to get node information for an IP address.
     */
    public static final String DB_GET_INFO_FOR_IP = "SELECT node.nodeid, node.nodelabel FROM node, ipInterface WHERE ipInterface.ipaddr = ? AND ipInterface.nodeid = node.nodeid AND node.nodeType = 'A' AND ipinterface.ismanaged = 'M'";

    /**
     * The SQL statement that is used to get services information for a
     * nodeid/IP address.
     */
    public static final String DB_GET_SVC_ENTRIES = "SELECT ifServices.serviceid, service.servicename FROM ifServices, ipInterface, node, service WHERE ifServices.ipInterfaceId = ipInterface.id AND ipInterface.nodeId = node.nodeId AND ipInterface.ipaddr = ? AND ipinterface.isManaged ='M' AND ifServices.serviceid = service.serviceid AND ifservices.status = 'A' AND node.nodeid = ? AND node.nodetype = 'A'";

    /**
     * The SQL statement for getting outage entries for a nodeid/ip/serviceid
     */
    public static final String DB_GET_OUTAGE_ENTRIES = "SELECT ifLostService, ifRegainedService FROM outages, ifServices, ipInterface, node WHERE outages.ifServiceId = ifServices.id AND ifServices.ipInterfaceId = ipInterface.id AND ipInterface.nodeId = node.nodeId AND node.nodeid = ? AND ipInterface.ipaddr = ? AND ifServices.serviceid = ?"; // and
                                                                                                                                                                                                        // ((ifRegainedService
                                                                                                                                                                                                        // IS
                                                                                                                                                                                                        // null)
                                                                                                                                                                                                        // OR
                                                                                                                                                                                                        // (ifRegainedService
                                                                                                                                                                                                        // IS
                                                                                                                                                                                                        // NOT
                                                                                                                                                                                                        // NULL
                                                                                                                                                                                                        // AND
                                                                                                                                                                                                        // ifRegainedService
                                                                                                                                                                                                        // > ?)
                                                                                                                                                                                                        // )";

    /**
     * The list of Availability Report Constants that are needed to display
     * appropriate messages on the report.
     */
    public final static String LAST_30_DAYS_DAILY_LABEL = "The last 30 Days Daily Availability";

    /** Constant <code>LAST_30_DAYS_DAILY_DESCR="Daily average of svcs and dvcs monitore"{trunked}</code> */
    public final static String LAST_30_DAYS_DAILY_DESCR = "Daily average of svcs and dvcs monitored and their availability divided by total mins for 30days";

    /** Constant <code>LAST_30_DAYS_TOTAL_LABEL="The last 30 Days Total Availability"</code> */
    public final static String LAST_30_DAYS_TOTAL_LABEL = "The last 30 Days Total Availability";

    /** Constant <code>LAST_30_DAYS_TOTAL_DESCR="Average of svcs monitored and availabil"{trunked}</code> */
    public final static String LAST_30_DAYS_TOTAL_DESCR = "Average of svcs monitored and availability of svcs divided by total svc minutes of the last 30 days";

    /** Constant <code>LAST_30_DAYS_SVC_AVAIL_LABEL="The last 30 days Daily Service Availabi"{trunked}</code> */
    public final static String LAST_30_DAYS_SVC_AVAIL_LABEL = "The last 30 days Daily Service Availability";

    /** Constant <code>LAST_30_DAYS_SVC_AVAIL_DESCR="The last 30 days Daily Service Availabi"{trunked}</code> */
    public final static String LAST_30_DAYS_SVC_AVAIL_DESCR = "The last 30 days Daily Service Availability is the daily average of services";

    /** Constant <code>LAST_MONTH_SVC_AVAIL_LABEL="The last Months Daily Service Availabil"{trunked}</code> */
    public final static String LAST_MONTH_SVC_AVAIL_LABEL = "The last Months Daily Service Availability";

    /** Constant <code>LAST_MONTH_SVC_AVAIL_DESCR="The last Months Daily Service Availabil"{trunked}</code> */
    public final static String LAST_MONTH_SVC_AVAIL_DESCR = "The last Months Daily Service Availability is the daily average of services and devices";

    /** Constant <code>LAST_MTD_DAILY_LABEL="Month To Date Daily Availability"</code> */
    public final static String LAST_MTD_DAILY_LABEL = "Month To Date Daily Availability";

    /** Constant <code>LAST_MTD_DAILY_DESCR="Daily Average of svc monitored and avai"{trunked}</code> */
    public final static String LAST_MTD_DAILY_DESCR = "Daily Average of svc monitored and availability of svcs div by total svc minutes of month frm 1st till date";

    /** Constant <code>LAST_MTD_TOTAL_LABEL="Month To Date Total Availability"</code> */
    public final static String LAST_MTD_TOTAL_LABEL = "Month To Date Total Availability";

    /** Constant <code>LAST_MTD_TOTAL_DESCR="Average of svc monitored and availabili"{trunked}</code> */
    public final static String LAST_MTD_TOTAL_DESCR = "Average of svc monitored and availability of svcs dividedby total svc minutes of month frm 1st till date";

    /** Constant <code>LAST_MONTH_DAILY_LABEL="The last Months Daily Availability"</code> */
    public final static String LAST_MONTH_DAILY_LABEL = "The last Months Daily Availability";

    /** Constant <code>LAST_MONTH_DAILY_DESCR="Daily Average of svcs monitored and ava"{trunked}</code> */
    public final static String LAST_MONTH_DAILY_DESCR = "Daily Average of svcs monitored and availability of svcs divided by the total svc minutes (last month)";

    /** Constant <code>LAST_MONTH_TOTAL_LABEL="The last Months Total Availability"</code> */
    public final static String LAST_MONTH_TOTAL_LABEL = "The last Months Total Availability";

    /** Constant <code>LAST_MONTH_TOTAL_DESCR="Average of svcs monitored and availabil"{trunked}</code> */
    public final static String LAST_MONTH_TOTAL_DESCR = "Average of svcs monitored and availability of svcs divided by the total svc minutes of the month";

    /** Constant <code>NOFFENDERS_LABEL="Last Months Top Offenders"</code> */
    public final static String NOFFENDERS_LABEL = "Last Months Top Offenders";

    /** Constant <code>NOFFENDERS_DESCR="This is the list of the worst available"{trunked}</code> */
    public final static String NOFFENDERS_DESCR = "This is the list of the worst available devices in the category for the last month";

    /** Constant <code>TOP20_SVC_OUTAGES_LABEL="Last Month Top Service Outages for"</code> */
    public final static String TOP20_SVC_OUTAGES_LABEL = "Last Month Top Service Outages for";

    /** Constant <code>TOP20_SVC_OUTAGES_DESCR="Last Month Top Service Outages for"</code> */
    public final static String TOP20_SVC_OUTAGES_DESCR = "Last Month Top Service Outages for";

    /** Constant <code>NMONTH_TOTAL_LABEL="The last 12 Months Availability"</code> */
    public final static String NMONTH_TOTAL_LABEL = "The last 12 Months Availability";

    /** Constant <code>NMONTH_TOTAL_DESCR="The last 12 Months Availability"</code> */
    public final static String NMONTH_TOTAL_DESCR = "The last 12 Months Availability";

}
