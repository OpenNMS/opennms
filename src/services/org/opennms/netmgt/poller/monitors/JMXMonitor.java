//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//  
//For more information contact: 
// OpenNMS Licensing       <license@opennms.org>
// http://www.opennms.org/
// http://www.opennms.com/
//

package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.net.URL;
import java.util.Map;

import javax.naming.InitialContext;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;

/*
 * This class computes the response time of making a connection to 
 * the remote server.  If the connection is successful the reponse time 
 * RRD is updated.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
public abstract class JMXMonitor extends IPv4LatencyMonitor {

    public abstract ConnectionWrapper getMBeanServerConnection(Map parameterMap, InetAddress address);
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.monitors.ServiceMonitor#poll(org.opennms.netmgt.poller.monitors.NetworkInterface, java.util.Map, org.opennms.netmgt.config.poller.Package)
     */
    public int poll(NetworkInterface iface, Map map, Package pkg) {

        Category       log           = ThreadCategory.getInstance(getClass());
        boolean        res           = false;
        InitialContext ctx           = null;
        int            serviceStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
        String         dsName        = null;
        InetAddress    ipv4Addr      = (InetAddress)iface.getAddress();
        
        ConnectionWrapper connection = null;


        try {
            
            int    retry     = ParameterMap.getKeyedInteger(map, "retry",            3);
            String rrdPath   = ParameterMap.getKeyedString(map,  "rrd-repository",   null);
                   dsName    = ParameterMap.getKeyedString(map,  "ds-name",          "jmx");

            long t0 = 0;
            for (int attempts=0; attempts <= retry && serviceStatus != ServiceMonitor.SERVICE_AVAILABLE; attempts++)    {
                URL jmxLink = null;
                try {
                    
                     t0 = System.currentTimeMillis();
                    
                     connection = getMBeanServerConnection(map, ipv4Addr);
                     if (connection != null) {
                         Integer result = connection.getMBeanServer().getMBeanCount();
                         serviceStatus = ServiceMonitor.SERVICE_AVAILABLE;
                       
                         long responseTime = System.currentTimeMillis() - t0;
                        
                         if (responseTime >= 0 && rrdPath != null) {
                             this.updateRRD(rrdPath, ipv4Addr, dsName, responseTime, pkg);
                         }
                     }
                    
                     break;
                }      
                catch(Exception e) {
                    log.debug(dsName + "poll: IOException while polling address: " + ipv4Addr);
                    break;
                }
            }  // of for
         } catch (Exception e) {
            log.debug(dsName + " Monitor - failed! " + ipv4Addr.getHostAddress());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        
        return serviceStatus;
    }
}
