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
    

//    def AddServiceICMP( self,
//                        serviceid,
//                        retries,
//                        timeout,
//                        interval,
//                        downtime_interval,
//                        downtime_duration ):
//       """
//       AddServiceICMP function
//       """
//       return True
    
    boolean addServiceICMP(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration);

//    def AddServiceDNS( self,
//                       serviceid,
//                       retries,
//                       timeout,
//                       interval, 
//                       downtime_interval,
//                       downtime_duration,
//                       port,
//                       hostname ):
//        """
//        AddServiceDNS function
//        """
//        return True

    boolean addServiceDNS(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, int port, String hostname);

    
//    def AddServiceTCP( self,
//                       serviceid, 
//                       retries, 
//                       timeout, 
//                       interval, 
//                       downtime_interval,
//                       downtime_duration, 
//                       port, 
//                       content_check ):
//        """
//        AddServiceTCP function
//        """
//        return True

    boolean addServiceTCP(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, int port, String contentCheck);

//    def AddServiceHTTP( self, 
//                        serviceid, 
//                        retries, 
//                        timeout, 
//                        interval, 
//                        downtime_interval,
//                        downtime_dutation, 
//                        port, 
//                        response_code, 
//                        content_check, 
//                        url ):
//        """
//        AddServiceHTTP function
//        """
//        return True

    // TODO: HttpMonitor BackLevel
    boolean addServiceHTTP(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, int port, String responseCode, String contentCheck, String url/*, String auth, String agent*/) throws MalformedURLException;

//    def AddServiceHTTPS( self,
//                         serviceid, 
//                         retries, 
//                         timeout, 
//                         interval,
//                         downtime_interval,
//                         downtime_dutation, 
//                         port, 
//                         response_code, 
//                         content_check, 
//                         url ):
//        """
//        AddServiceHTTPS function
//        """
//        return True

    // TODO: HttpMonitor BackLevel
    boolean addServiceHTTPS(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, int port, String responseCode, String contentCheck, String url/*, String auth, String agent*/) throws MalformedURLException;

//    def AddServiceDatabse( self, 
//                           serviceid, 
//                           retries, 
//                           timeout, 
//                           interval,
//                           downtime_interval,
//                           downtime_duration, 
//                           username, 
//                           password,
//                           driver, 
//                           url ):
//        """
//        AddServiceDatabase function
//        """
//        return True

    boolean addServiceDatabase(String serviceId, int retries, int timeout, int interval, int downTimeInterval, int downTimeDuration, String username, String password, String driver, String url);

    
    
    Map getServiceConfiguration(String pkName, String serviceId);
    
    

}
