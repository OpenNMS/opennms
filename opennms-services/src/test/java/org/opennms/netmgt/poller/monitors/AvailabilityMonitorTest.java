//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.ServiceMonitor;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class AvailabilityMonitorTest extends TestCase {

    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
    }

    /* (non-Javadoc)
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test method for {@link org.opennms.netmgt.poller.monitors.AvailabilityMonitor#poll(org.opennms.netmgt.poller.MonitoredService, Map)}.
     */
    public final void testPoll() {
        ServiceMonitor sm = new AvailabilityMonitor();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("timeout", "3000");
        MonitoredService svc = new MonitoredService() {
            protected InetAddress getNetworkAddress() {
                return getAddress();
            }
            public InetAddress getAddress() {
                try {
                    return InetAddress.getByName("127.0.0.1");
                } catch (UnknownHostException e) {
                    throw new IllegalStateException("Error getting localhost address", e);
                }
            }
            public String getIpAddr() {
                return getAddress().getHostAddress();
            }
            public NetworkInterface<InetAddress> getNetInterface() {
                return new NetworkInterface<InetAddress>() {
                    public InetAddress getAddress() {
                        return getNetworkAddress();
                    }
                    public Object getAttribute(String property) {
                        return null;
                    }
                    public int getType() {
                        return 0;
                    }
                    public Object setAttribute(String property, Object value) {
                        return null;
                    }
                };
            }
            public int getNodeId() {
                return 0;
            }
            public String getNodeLabel() {
                return "localhost";
            }
            public String getSvcName() {
                return "ICMP";
            }
            
        };
        PollStatus status = sm.poll(svc, parameters);
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }

}
