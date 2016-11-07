/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;

import java.net.UnknownHostException;
import java.util.HashMap;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.passive.PassiveStatusKeeper;
import org.opennms.netmgt.passive.PassiveStatusKeeperIT;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockPollContext;
import org.opennms.netmgt.poller.pollables.PollableInterface;
import org.opennms.netmgt.poller.pollables.PollableNetwork;
import org.opennms.netmgt.poller.pollables.PollableNode;
import org.opennms.netmgt.poller.pollables.PollableService;

public class PassiveServiceMonitorIT extends PassiveStatusKeeperIT {

    // inherit from PassiveStatusKeeperTest so we can inherit all the proper initialization
    public void testPoll() throws UnknownHostException {
        
        PassiveStatusKeeper psk = PassiveStatusKeeper.getInstance();
        psk.setStatus("localhost", "127.0.0.1", "my-passive-service", PollStatus.get(PollStatus.SERVICE_UNAVAILABLE, "testing failure"));
        
        ServiceMonitor sm = new PassiveServiceMonitor();
        
        MonitoredService ms = createMonitoredService(1, "localhost", null, "127.0.0.1", "my-passive-service");
        PollStatus ps = sm.poll(ms, new HashMap<String, Object>());
        assertEquals(PollStatus.down("fail."), ps);

        psk.setStatus("localhost", "127.0.0.1", "my-passive-service", PollStatus.get(PollStatus.SERVICE_AVAILABLE, "testing failure"));
        ps = sm.poll(ms, new HashMap<String, Object>());
        assertEquals(PollStatus.up(), ps);
    }

    private PollableService createMonitoredService(int nodeId, String nodeLabel, String nodeLocation, String ipAddr, String serviceName) throws UnknownHostException {
        return new PollableService(new PollableInterface(new PollableNode(new PollableNetwork(new MockPollContext()), nodeId, nodeLabel, nodeLocation), InetAddressUtils.addr(ipAddr)), serviceName);
    }

}
