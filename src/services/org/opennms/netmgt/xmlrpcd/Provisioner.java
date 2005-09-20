//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.xmlrpcd;

import java.net.MalformedURLException;
import java.util.Map;


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
     * @return Always returns true.  Check for XML-RPC exception.
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
     * @return Always returns true.  Check for XML-RPC exception.
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
     * @return Always returns true.  Check for XML-RPC exception.
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
     * @return Always returns true.  Check for XML-RPC exception.
     */
    // TODO: HttpMonitor BackLevel
    boolean addServiceHTTP(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, int port, String responseCode, String contentCheck, String url/*, String auth, String agent*/) throws MalformedURLException;

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
     * @param downTimeDuration <code>int</> the amount of time in milliseconds the downtime polling interval is in effect
     * @return Always returns true.  Check for XML-RPC exception.
     */
    // TODO: HttpMonitor BackLevel
    boolean addServiceHTTPS(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, int port, String responseCode, String contentCheck, String url/*, String auth, String agent*/) throws MalformedURLException;

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
     * @param downTimeDuration <code>int</> the amount of time in milliseconds the downtime polling interval is in effect
     * @return Always returns true.  Check for XML-RPC exception.
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
     * @return Always returns true.  Check for XML-RPC exception.
     */
    Map getServiceConfiguration(String pkName, String serviceId);    

}
