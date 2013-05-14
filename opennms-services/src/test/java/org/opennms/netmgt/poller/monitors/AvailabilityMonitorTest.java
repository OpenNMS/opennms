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

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.ServiceMonitor;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class AvailabilityMonitorTest {

    /**
     * Test method for {@link org.opennms.netmgt.poller.monitors.AvailabilityMonitor#poll(org.opennms.netmgt.poller.MonitoredService, Map)}.
     */
    @Test
    public final void testPoll() {
        ServiceMonitor sm = new AvailabilityMonitor();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("timeout", "3000");
        MonitoredService svc = new MonitoredService() {
            @Override
            public InetAddress getAddress() {
                final InetAddress addr = InetAddressUtils.addr("127.0.0.1");
                if (addr == null) {
                    throw new IllegalStateException("Error getting localhost address");
                }
                return addr;
            }
            @Override
            public String getIpAddr() {
                return InetAddressUtils.str(getAddress());
            }
            @Override
            public NetworkInterface<InetAddress> getNetInterface() {
                return new InetNetworkInterface(getAddress());
            }
            @Override
            public int getNodeId() {
                return 0;
            }
            @Override
            public String getNodeLabel() {
                return "localhost";
            }
            @Override
            public String getSvcName() {
                return "ICMP";
            }
            @Override
            public String getSvcUrl() {
                return null;
            }
            
        };
        PollStatus status = sm.poll(svc, parameters);
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }

}
