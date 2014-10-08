/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xmlrpcd;

import java.net.MalformedURLException;
import java.util.Map;

/**
 * <p>Provisioner interface.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public interface Provisioner {

    /**
     * This method defines a new package and ICMP service using the <code>serviceId</code> as the name of the
     * polling package and service name within that package.  If the same <code>serviceId</code> is used again,
     * it redefines the service.
     *
     * To add an interface to this newly defined ICMP service,
     * send an <code>EventConstants.UPDATE_SERVICE_EVENT_UEI</code> <code>Event</code>.
     *
     * @param serviceId <code>String</code> is used to define a polling package and a service name in that polling package.
     * @param retries <code>int</code> number of retries for this service
     * @param timeout <code>int</code> time in milliseconds to wait during each <code>retries</code>
     * @param interval <code>int</code> the standard polling interval in milliseconds
     * @param downTimeInterval <code>int</code> the downtime polling interval in milliseconds(downtime model)
     * @param downTimeDuration <code>int</> the amount of time in milliseconds the downtime polling interval is in effect
     * @return Always returns true.
     * @throws java.lang.IllegalArgumentException when arguments are outside of ranges
     */
    boolean addServiceICMP(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration);

    /**
     * This method defines a new package and DNS service using the <code>serviceId</code> as the name of the
     * polling package and service name within that package.  If the same <code>serviceId</code> is used again,
     * it redefines the service.
     *
     * To add an interface to this newly defined DNS service,
     * send an <code>EventConstants.UPDATE_SERVICE_EVENT_UEI</code> <code>Event</code>.
     *
     * @param serviceId <code>String</code> is used to define a polling package and a service name in that polling package.
     * @param retries <code>int</code> number of retries for this service
     * @param timeout <code>int</code> time in milliseconds to wait during each <code>retries</code>
     * @param interval <code>int</code> the standard polling interval in milliseconds
     * @param downTimeInterval <code>int</code> the downtime polling interval in milliseconds(downtime model)
     * @param downTimeDuration <code>int</> the amount of time in milliseconds the downtime polling interval is in effect
     * @param port <code>int</code> the port to poll for DNS service
     * @param hostname <code>String</code> the hostname to resolve with the DNS service
     * @return Always returns true.
     * @throws java.lang.IllegalArgumentException when arguments are outside of ranges
     */
    boolean addServiceDNS(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, int port, String hostname);

    /**
     * This method defines a new package and TCP service using the <code>serviceId</code> as the name of the
     * polling package and service name within that package.  If the same <code>serviceId</code> is used again,
     * it redefines the service.
     *
     * To add an interface to this newly defined TCP service,
     * send an <code>EventConstants.UPDATE_SERVICE_EVENT_UEI</code> <code>Event</code>.
     *
     * @param serviceId <code>String</code> is used to define a polling package and a service name in that polling package.
     * @param retries <code>int</code> number of retries for this service
     * @param timeout <code>int</code> time in milliseconds to wait during each <code>retries</code>
     * @param interval <code>int</code> the standard polling interval in milliseconds
     * @param downTimeInterval <code>int</code> the downtime polling interval in milliseconds(downtime model)
     * @param downTimeDuration <code>int</> the amount of time in milliseconds the downtime polling interval is in effect
     * @param port <code>int</code> the port to attempt to connect to determine if the service is accepting connections
     * @param contentCheck <code>String</code> a string to check for in the returned banner of the TCP service or "" if no check is desired.
     * @return Always returns true.
     * @throws java.lang.IllegalArgumentException when arguments are outside of ranges
     */
    boolean addServiceTCP(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, int port, String contentCheck);

    /**
     * This method defines a new package and HTTP service using the <code>serviceId</code> as the name of the
     * polling package and service name within that package.  If the same <code>serviceId</code> is used again,
     * it redefines the service.
     *
     * To add an interface to this newly defined HTTP service,
     * send an <code>EventConstants.UPDATE_SERVICE_EVENT_UEI</code> <code>Event</code>.
     *
     * @param serviceId <code>String</code> is used to define a polling package and a service name in that polling package.
     * @param retries <code>int</code> number of retries for this service
     * @param timeout <code>int</code> time in milliseconds to wait during each <code>retries</code>
     * @param interval <code>int</code> the standard polling interval in milliseconds
     * @param downTimeInterval <code>int</code> the downtime polling interval in milliseconds(downtime model)
     * @param downTimeDuration <code>int</> the amount of time in milliseconds the downtime polling interval is in effect
     * @param hostName <code>String</code> the virtual host (requires a url other than just "/")
     * @param port <code>int</code> the port to attempt to connect to determine if the service is accepting connections
     * @param responseCode <code>String</code> a set of responseCodes that are considered valid.
     *        This string should consist of one or more ranges ( [range],[range],...) where a range is either a single number or
     *        a pair of number separated by a - ( 200,202-300 ).  If the default range check is desire this can be ""
     *        The default range check is defined by the HttpMonitor plugin and is currently 100-499 for / or 100-399 for other URLs
     * @param contentCheck <code>String</code> a string to check for in the returned web page.  This string is either an exact string
     *        to search for in the page or a regular expression.  To indicate a regular expression prefix the string with a '~' character
     * @param url <code>String</code> the path portion of the URL to check on the server
     * @param user <code>String</code> the user to use with basic authentication or "" if no authentication required
     * @param passwd <code>String</code> the password to use with the user parameter with doing authentication
     * @param agent <code>String</code> the String to use for the User agent field when communicating with the server
     * @return Always returns true.
     * @throws java.lang.IllegalArgumentException when arguments are outside of ranges
     * @throws java.net.MalformedURLException if any.
     */
    boolean addServiceHTTP(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, String hostName, int port, String responseCode, String contentCheck, String url, String user, String passwd, String agent) throws MalformedURLException;

    /**
     * This method defines a new package and HTTPS service using the <code>serviceId</code> as the name of the
     * polling package and service name within that package.  If the same <code>serviceId</code> is used again,
     * it redefines the service.
     *
     * To add an interface to this newly defined HTTPS service,
     * send an <code>EventConstants.UPDATE_SERVICE_EVENT_UEI</code> <code>Event</code>.
     *
     * @param serviceId <code>String</code> is used to define a polling package and a service name in that polling package.
     * @param retries <code>int</code> number of retries for this service
     * @param timeout <code>int</code> time in milliseconds to wait during each <code>retries</code>
     * @param interval <code>int</code> the standard polling interval in milliseconds
     * @param downTimeInterval <code>int</code> the downtime polling interval in milliseconds(downtime model)
     * @param downTimeDuration <code>int</code> the amount of time in milliseconds the downtime polling interval is in effect
     * @param hostName <code>String</code> the virtual host (requires a url other than just "/")
     * @param port <code>int</code> the port to attempt to connect to determine if the service is accepting connections
     * @param responseCode <code>String</code> a set of responseCodes that are considered valid.
     *        This string should consist of one or more ranges ( [range],[range],...) where a range is either a single number or
     *        a pair of number separated by a - ( 200,202-300 ).  If the default range check is desire this can be ""
     *        The default range check is defined by the HttpMonitor plugin and is currently 100-499 for / or 100-399 for other URLs
     * @param contentCheck <code>String</code> a string to check for in the returned web page.  This string is either an exact string
     *        to search for in the page or a regular expression.  To indicate a regular expression prefix the string with a '~' character
     * @param url <code>String</code> the path portion of the URL to check on the server
     * @param user <code>String</code> the user to use with basic authentication or "" if no authentication required
     * @param passwd <code>String</code> the password to use with the user parameter with doing authentication
     * @param agent <code>String</code> the String to use for the User agent field when communicating with the server
     * @return Always returns true.
     * @throws java.lang.IllegalArgumentException when arguments are outside of ranges
     * @throws java.net.MalformedURLException if any.
     */
    boolean addServiceHTTPS(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, String hostName, int port, String responseCode, String contentCheck, String url, String user, String passwd, String agent) throws MalformedURLException;

    /**
     * This method defines a new package and Database(JDBC) service using the <code>serviceId</code> as the name of the
     * polling package and service name within that package.  If the same <code>serviceId</code> is used again,
     * it redefines the service.
     *
     * To add an interface to this newly defined Database(JDBC) service,
     * send an <code>EventConstants.UPDATE_SERVICE_EVENT_UEI</code> <code>Event</code>.
     *
     * @param serviceId <code>String</code> is used to define a polling package and a service name in that polling package.
     * @param retries <code>int</code> number of retries for this service
     * @param timeout <code>int</code> time in milliseconds to wait during each <code>retries</code>
     * @param interval <code>int</code> the standard polling interval in milliseconds
     * @param downTimeInterval <code>int</code> the downtime polling interval in milliseconds(downtime model)
     * @param downTimeDuration <code>int</code> the amount of time in milliseconds the downtime polling interval is in effect
     * @param username <code>String</code> the user to pass to the JDBC connection
     * @param password <code>String</code> the password to pass to the JDBC connection
     * @param driver <code>String</code> the fully qualified name of the JDBC Driver class
     * @param url <code>String</code> the url used to make a JDBC connection to the database
     * @return Always returns true.
     * @throws java.lang.IllegalArgumentException when arguments are outside of ranges
     */
    boolean addServiceDatabase(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, String username, String password, String driver, String url);

    /**
     * Returns an XML-RPC compatible hash map representing the current configuration (see addService params) for
     * the matching <code>String</code> <code>serviceId</code>
     *
     * When a service is defined using XML-RPC, the package name and the service name will be the same.
     *
     * @param pkName Name of the polling package i.e. "default" or custom package "FastHTTP"
     * @param serviceId Name of the service, i.e. "ICMP" or a custom service "FastHTTP"
     * @return Always returns true.
     * @throws java.lang.IllegalArgumentException when arguments are outside of ranges
     */
    Map<?,?> getServiceConfiguration(String pkName, String serviceId);    

}
