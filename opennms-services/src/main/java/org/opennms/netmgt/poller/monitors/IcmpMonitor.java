/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2004-2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/


package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.ping.Pinger;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.NetworkInterfaceNotSupportedException;

/**
 * <P>
 * This class is designed to be used by the service poller framework to test the
 * availability of the ICMP service on remote interfaces. The class implements
 * the ServiceMonitor interface that allows it to be used along with other
 * plug-ins by the service poller framework.
 * </P>
 * 
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * 
 */

// this is marked not distributable because it relies on a shared library
@Distributable(DistributionContext.DAEMON)
final public class IcmpMonitor extends IPv4Monitor {
    /**
     * Constructs a new monitor.
     */
    public IcmpMonitor() throws IOException {
    }

    /**
     * <P>
     * Poll the specified address for ICMP service availability.
     * </P>
     * 
     * <P>
     * The ICMP service monitor relies on Discovery for the actual generation of
     * IMCP 'ping' requests. A JSDT session with two channels (send/recv) is
     * utilized for passing poll requests and receiving poll replies from
     * discovery. All exchanges are SOAP/XML compliant.
     * </P>
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used for
     *            this poll.
     * @param iface
     *            The network interface to test the service on.
     * @return The availability of the interface and if a transition event
     *         should be suppressed.
     * 
     */
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface iface = svc.getNetInterface();

        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_IPV4 currently supported");

        Category log = ThreadCategory.getInstance(this.getClass());
        Long rtt = null;
        InetAddress host = (InetAddress) iface.getAddress();

        try {
            
            // get parameters
            //
            int retries = ParameterMap.getKeyedInteger(parameters, "retry", Pinger.DEFAULT_RETRIES);
            long timeout = ParameterMap.getKeyedLong(parameters, "timeout", Pinger.DEFAULT_TIMEOUT);
            
            rtt = Pinger.ping(host, timeout, retries);
        } catch (Exception e) {
            log.debug("failed to ping " + host, e);
        }
        
        if (rtt != null) {
            return PollStatus.available(rtt.doubleValue());
        } else {
            // TODO add a reason code for unavailability
            return PollStatus.unavailable();
        }

    }

}
