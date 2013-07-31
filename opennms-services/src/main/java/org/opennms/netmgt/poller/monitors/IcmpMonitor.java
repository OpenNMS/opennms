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

import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.icmp.PingConstants;
import org.opennms.netmgt.icmp.PingerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
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
 */

@Distributable
final public class IcmpMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(IcmpMonitor.class);
    /**
     * Constructs a new monitor.
     *
     * @throws java.io.IOException if any.
     */
    public IcmpMonitor() throws IOException {
    }

    /**
     * {@inheritDoc}
     *
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
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        NetworkInterface<InetAddress> iface = svc.getNetInterface();

        // Get interface address from NetworkInterface
        //
        if (iface.getType() != NetworkInterface.TYPE_INET)
            throw new NetworkInterfaceNotSupportedException("Unsupported interface type, only TYPE_INET currently supported");

        Number rtt = null;
        InetAddress host = (InetAddress) iface.getAddress();

        try {
            
            // get parameters
            //
            int retries = ParameterMap.getKeyedInteger(parameters, "retry", PingConstants.DEFAULT_RETRIES);
            long timeout = ParameterMap.getKeyedLong(parameters, "timeout", PingConstants.DEFAULT_TIMEOUT);
            int packetSize = ParameterMap.getKeyedInteger(parameters, "packet-size", PingConstants.DEFAULT_PACKET_SIZE);
            
            rtt = PingerFactory.getInstance().ping(host, timeout, retries,packetSize);
        } catch (Throwable e) {
            LOG.debug("failed to ping {}", host, e);
        }
        
        if (rtt != null) {
            return PollStatus.available(rtt.doubleValue());
        } else {
            // TODO add a reason code for unavailability
            return PollStatus.unavailable();
        }

    }

}
