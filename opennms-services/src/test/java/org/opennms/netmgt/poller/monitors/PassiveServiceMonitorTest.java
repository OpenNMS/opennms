//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.passive.PassiveStatusKeeper;
import org.opennms.netmgt.passive.PassiveStatusKeeperTest;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockPollContext;
import org.opennms.netmgt.poller.pollables.PollableInterface;
import org.opennms.netmgt.poller.pollables.PollableNetwork;
import org.opennms.netmgt.poller.pollables.PollableNode;
import org.opennms.netmgt.poller.pollables.PollableService;

public class PassiveServiceMonitorTest extends PassiveStatusKeeperTest {

    // inherit from PassiveStatusKeeperTest so we can inherit all the proper initialization
    public void testPoll() throws UnknownHostException {
        
        PassiveStatusKeeper psk = PassiveStatusKeeper.getInstance();
        psk.setStatus("localhost", "127.0.0.1", "my-passive-service", PollStatus.get(PollStatus.SERVICE_UNAVAILABLE, "testing failure"));
        
        ServiceMonitor sm = new PassiveServiceMonitor();
        
        MonitoredService ms = createMonitoredService(1, "localhost", "127.0.0.1", "my-passive-service");
        PollStatus ps = sm.poll(ms, new HashMap<String, Object>());
        assertEquals(PollStatus.down(), ps);

        psk.setStatus("localhost", "127.0.0.1", "my-passive-service", PollStatus.get(PollStatus.SERVICE_AVAILABLE, "testing failure"));
        ps = sm.poll(ms, new HashMap<String, Object>());
        assertEquals(PollStatus.up(), ps);
    }

    private PollableService createMonitoredService(int nodeId, String nodeLabel, String ipAddr, String serviceName) throws UnknownHostException {
        return new PollableService(new PollableInterface(new PollableNode(new PollableNetwork(new MockPollContext()), nodeId, nodeLabel), InetAddress.getByName(ipAddr)), serviceName);
    }

}
