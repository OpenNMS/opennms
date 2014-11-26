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

package org.opennms.netmgt.rtc;

/**
 * This class is a repository for constant, static information concerning the
 * RTC.
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Kumaraswamy </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * @version $Id: $
 */
public abstract class RTCConstants {
    /**
     * The value returned by RTC if a nodeid/ip/svc tuple does not belong to a
     * category.
     */
    public static double NODE_NOT_IN_CATEGORY = -1.0;

    /**
     * The base name of the eventd configuration file. This does not include any
     * path information about the location of the file, just the filename
     * itself.
     */
    public static final String RTC_CONF_FNAME = "rtc-configuration.xml";

    /**
     * The SQL statement necessary to read service id and service name into map.
     */
    public static final String SQL_DB_SVC_TABLE_READ = "SELECT serviceID, serviceName FROM service";

    /**
     * The SQL statement necessary to convert the service name into a service id
     * using the 'service' table.
     */
    public static final String SQL_DB_SVCNAME_TO_SVCID = "SELECT serviceID FROM service WHERE serviceName = ?";

    /**
     * The sql statement that is used to get node information for an IP address.
     */
    public static final String DB_GET_INFO_FOR_IP = "SELECT  node.nodeid FROM " + "node, ipInterface WHERE ((ipInterface.ipaddr = ?) AND " + "(ipInterface.nodeid = node.nodeid) AND (node.nodeType = 'A') AND (ipinterface.ismanaged = 'M') )";

    /**
     * The sql statement that is used to get services information for a
     * nodeid/IP address.
     */
    public static final String DB_GET_SVC_ENTRIES = "SELECT service.servicename FROM ifServices, " + "service WHERE ((ifServices.nodeid = ? ) AND (ifServices.ipaddr = ?) AND " + "(ifServices.serviceid = service.serviceid) AND (ifservices.status = 'A'))";

    /**
     * The sql statement that is used to get 'status' for a nodeid/ip/svc.
     */
    public static final String DB_GET_SERVICE_STATUS = "SELECT status from ifservices, service where ((ifservices.nodeid = ?) AND (ifservices.ipaddr = ?) AND (ifservices.serviceid = service.serviceid) AND (service.servicename = ?))";

    /**
     * The sql statement for getting outage entries for a nodeid/ip/serviceid.
     */
    public static final String DB_GET_OUTAGE_ENTRIES = "SELECT ifLostService, ifRegainedService from outages,service " + "where ( (outages.nodeid = ?) AND (outages.ipaddr = ?) AND (outages.serviceid = service.serviceid) AND (service.servicename = ?) AND " + "((ifLostService >= ?) OR (ifRegainedService >= ?) OR (ifRegainedService IS NULL)) ) ORDER BY outageid";

    /**
     * The SQL statement necessary to get the IP addresses associated with a
     * node.
     */
    public static final String SQL_DB_NODE_IPADDRS = "SELECT ipaddr FROM ipinterface WHERE ipaddr != '0.0.0.0' and nodeid = ?";
}
