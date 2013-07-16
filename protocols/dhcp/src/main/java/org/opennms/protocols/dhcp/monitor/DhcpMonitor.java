/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.dhcp.monitor;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.dhcpd.Dhcpd;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;
import org.opennms.netmgt.poller.monitors.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to be used by the service poller framework to test the
 * availability of the DHCP service on remote interfaces as defined by RFC 2131.
 *
 * This class relies on the DHCP API provided by JDHCP v1.1.1 (please refer to
 * <A HREF="http://www.dhcp.org/javadhcp">http://www.dhcp.org/javadhcp </A>).
 *
 * The class implements the ServiceMonitor interface that allows it to be used
 * along with other plug-ins by the service poller framework.
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */
@Distributable(DistributionContext.DAEMON)
final public class DhcpMonitor extends AbstractServiceMonitor {
	
	private static final Logger LOG = LoggerFactory.getLogger(DhcpMonitor.class);

    /**
     * Default retries.
     */
    private static final int DEFAULT_RETRY = 0;

    /**
     * Default timeout. Specifies how long (in milliseconds) to block waiting
     * for data from the monitored interface.
     */
    private static final int DEFAULT_TIMEOUT = 3000; // 3 second timeout on
                                                        // read()

    /**
     * {@inheritDoc}
     *
     * Poll the specified address for DHCP service availability.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_INET)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");

        // Process parameters
        //

        // Retries
        //
        int retry = ParameterMap.getKeyedInteger(parameters, "retry", DEFAULT_RETRY);
        int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", DEFAULT_TIMEOUT);

        // Get interface address from NetworkInterface
        //
        InetAddress ipv4Addr = (InetAddress) iface.getAddress();

            LOG.debug("DhcpMonitor.poll: address: {} timeout: {} retry: {}", ipv4Addr, timeout,  retry);

        PollStatus serviceStatus = PollStatus.unavailable();
        long responseTime = -1;
        try {
            // Dhcpd.isServer() returns the response time in milliseconds
            // if the remote box is a DHCP server or -1 if the remote
            // box is NOT a DHCP server.
            // 
            responseTime = Dhcpd.isServer(ipv4Addr, (long) timeout, retry);
            if (responseTime >= 0) {
                serviceStatus = PollStatus.available((double)responseTime);
            }
        } catch (IOException e) {
            e.fillInStackTrace();
            DhcpMonitor.LOG.debug("An I/O exception occured during DHCP polling", e);
            serviceStatus = PollStatus.unavailable("An I/O exception occured during DHCP polling");
        } catch (Throwable e) {
            e.fillInStackTrace();
            DhcpMonitor.LOG.debug("An unexpected exception occured during DHCP polling", e);
            serviceStatus = PollStatus.unavailable("An unexpected exception occured during DHCP polling");
        }

        //
        // return the status of the service
        //
        return serviceStatus;
    }
    
}
