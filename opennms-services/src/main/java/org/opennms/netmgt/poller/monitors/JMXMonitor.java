/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * This class computes the response time of making a connection to 
 * the remote server.  If the connection is successful the reponse time 
 * RRD is updated.
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Jamison </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

@Distributable
/**
 * <p>Abstract JMXMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public abstract class JMXMonitor extends AbstractServiceMonitor {

    
    public static final Logger LOG = LoggerFactory.getLogger(JMXMonitor.class);
    
    /**
     * <p>getMBeanServerConnection</p>
     *
     * @param parameterMap a {@link java.util.Map} object.
     * @param address a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.protocols.jmx.connectors.ConnectionWrapper} object.
     */
    public abstract ConnectionWrapper getMBeanServerConnection(Map<String, Object> parameterMap, InetAddress address);
    
    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.monitors.ServiceMonitor#poll(org.opennms.netmgt.poller.monitors.NetworkInterface, java.util.Map, org.opennms.netmgt.config.poller.Package)
     */
    /** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> map) {

        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        PollStatus     serviceStatus = PollStatus.unavailable();
        String         dsName        = null;
        InetAddress    ipv4Addr      = (InetAddress)iface.getAddress();
        
        ConnectionWrapper connection = null;


        try {
            
            int retry = ParameterMap.getKeyedInteger(map, "retry", 3);

            long t0 = 0;
            for (int attempts=0; attempts <= retry && !serviceStatus.isAvailable(); attempts++) {
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
                    String reason = dsName+": IOException while polling address: " + ipv4Addr;
                    LOG.debug(reason);
                    serviceStatus = PollStatus.unavailable(reason);
                    break;
                }
            }
        } catch (Throwable e) {
            String reason = dsName+" Monitor - failed! " + InetAddressUtils.str(ipv4Addr);
            LOG.debug(reason);
            serviceStatus = PollStatus.unavailable(reason);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        
        return serviceStatus;
    }
}
