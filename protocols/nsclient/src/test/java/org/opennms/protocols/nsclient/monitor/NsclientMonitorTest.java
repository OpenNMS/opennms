/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.protocols.nsclient.monitor;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.protocols.nsclient.AbstractNsclientTest;
import org.opennms.protocols.nsclient.monitor.NsclientMonitor;

/**
 * <p>JUnit Test Class for NsclientMonitor.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class NsclientMonitorTest extends AbstractNsclientTest {

    @Test
    public void testMonitorSuccess() throws Exception {
        startServer("None&1", "NSClient++ 0.3.8.75 2010-05-27");
        NsclientMonitor monitor = new NsclientMonitor();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("port", getServer().getLocalPort());
        PollStatus status = monitor.poll(createMonitoredService(), parameters);
        Assert.assertTrue(status.isAvailable());
        stopServer();
    }

    @Test
    public void testMonitorFail() throws Exception {
        startServer("None&1", "ERROR: I don't know what you mean");
        NsclientMonitor monitor = new NsclientMonitor();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("port", getServer().getLocalPort());
        PollStatus status = monitor.poll(createMonitoredService(), parameters);
        Assert.assertFalse(status.isAvailable());
        stopServer();
    }

    private MonitoredService createMonitoredService() {
        final InetAddress address = getServer().getInetAddress();
        MonitoredService svc = new MonitoredService() {

            @Override
            public String getSvcUrl() {
                // TODO Auto-generated method stub
                return null;
            }

            @Override
            public String getSvcName() {
                return "NSClient";
            }

            @Override
            public String getIpAddr() {
                return address.getHostAddress();
            }

            @Override
            public int getNodeId() {
                return 1;
            }

            @Override
            public String getNodeLabel() {
                return "winsrv";
            }

            @Override
            public NetworkInterface<InetAddress> getNetInterface() {
                return new NetworkInterface<InetAddress>() {
                    @Override
                    public int getType() {
                        return NetworkInterface.TYPE_INET;
                    }
                    @Override
                    public InetAddress getAddress() {
                        return address;
                    }
                    @Override
                    public <V> V getAttribute(String property) {
                        return null;
                    }
                    @Override
                    public Object setAttribute(String property, Object value) {
                        return null;
                    }
                };
            }

            @Override
            public InetAddress getAddress() {
                return address;
            }

        };
        return svc;
    }

}
