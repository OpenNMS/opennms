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

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Level;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;

/*
 * This class computes the response time of making a connection to 
 * the remote server.  If the connection is successful the reponse time 
 * RRD is updated.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

@Distributable
public abstract class JMXMonitor extends IPv4Monitor {

    public abstract ConnectionWrapper getMBeanServerConnection(Map parameterMap, InetAddress address);
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.monitors.ServiceMonitor#poll(org.opennms.netmgt.poller.monitors.NetworkInterface, java.util.Map, org.opennms.netmgt.config.poller.Package)
     */
    public PollStatus poll(MonitoredService svc, Map map) {
        
        NetworkInterface iface = svc.getNetInterface();

        PollStatus     serviceStatus = PollStatus.unavailable();
        String         dsName        = null;
        InetAddress    ipv4Addr      = (InetAddress)iface.getAddress();
        
        ConnectionWrapper connection = null;


        try {
            
            int    retry     = ParameterMap.getKeyedInteger(map, "retry",            3);

            long t0 = 0;
            for (int attempts=0; attempts <= retry && !serviceStatus.isAvailable(); attempts++)    {

                try {
                    
                     t0 = System.nanoTime();
                    
                     connection = getMBeanServerConnection(map, ipv4Addr);
                     if (connection != null) {

                         connection.getMBeanServer().getMBeanCount();
                       
                         long nanoResponseTime = System.nanoTime() - t0;

                         serviceStatus = PollStatus.available(nanoResponseTime / 1000000.0);
                    
                         break;

                     }
                }      
                catch(IOException e) {
                	serviceStatus = logDown(Level.DEBUG, dsName+": IOException while polling address: " + ipv4Addr);
                    break;
                }
            }  // of for
         } catch (Exception e) {
         	serviceStatus = logDown(Level.DEBUG, dsName+" Monitor - failed! " + ipv4Addr.getHostAddress());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        
        return serviceStatus;
    }
}
