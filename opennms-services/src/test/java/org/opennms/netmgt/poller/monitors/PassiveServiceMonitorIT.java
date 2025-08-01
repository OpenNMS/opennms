/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.events.api.model.ImmutableEvent;
import org.opennms.netmgt.events.api.model.ImmutableParm;
import org.opennms.netmgt.events.api.model.ImmutableValue;
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

    /**
     * See NMS-14199, test case event with time set
     */
    @Test
    public void testWithTimestamp() throws UnknownHostException {
        final PassiveStatusKeeper psk = PassiveStatusKeeper.getInstance();
        final List<IParm> parms = new ArrayList<>();
        parms.add(ImmutableParm.newBuilder().setParmName("passiveNodeLabel").setValue(ImmutableValue.newBuilder().setContent("fooBar").build()).build());
        parms.add(ImmutableParm.newBuilder().setParmName("passiveIpAddr").setValue(ImmutableValue.newBuilder().setContent("10.10.10.10").build()).build());
        parms.add(ImmutableParm.newBuilder().setParmName("passiveServiceName").setValue(ImmutableValue.newBuilder().setContent("theService").build()).build());
        parms.add(ImmutableParm.newBuilder().setParmName("passiveStatus").setValue(ImmutableValue.newBuilder().setContent("Up").build()).build());
        parms.add(ImmutableParm.newBuilder().setParmName("passiveReasonCode").setValue(ImmutableValue.newBuilder().setContent("Some reason").build()).build());

        final Date someMinutesAgo = Date.from(Instant.now().minus(Duration.ofMinutes(13)));

        final IEvent e = ImmutableEvent.newBuilder()
                .setUei("uei.opennms.org/services/passiveServiceStatus")
                .setParms(parms)
                .setTime(someMinutesAgo)
                .build();

        psk.onEvent(e);

        final MonitoredService ms = createMonitoredService(1, "fooBar", null, "10.10.10.10", "theService");
        final ServiceMonitor sm = new PassiveServiceMonitor();

        assertEquals(someMinutesAgo, sm.poll(ms, new HashMap<>()).getTimestamp());
    }

    /**
     * See NMS-14199, test case event without time set
     */
    @Test
    public void testWithoutTimestamp() throws UnknownHostException {
        final PassiveStatusKeeper psk = PassiveStatusKeeper.getInstance();
        final List<IParm> parms = new ArrayList<>();
        parms.add(ImmutableParm.newBuilder().setParmName("passiveNodeLabel").setValue(ImmutableValue.newBuilder().setContent("fooBar").build()).build());
        parms.add(ImmutableParm.newBuilder().setParmName("passiveIpAddr").setValue(ImmutableValue.newBuilder().setContent("10.10.10.10").build()).build());
        parms.add(ImmutableParm.newBuilder().setParmName("passiveServiceName").setValue(ImmutableValue.newBuilder().setContent("theService").build()).build());
        parms.add(ImmutableParm.newBuilder().setParmName("passiveStatus").setValue(ImmutableValue.newBuilder().setContent("Up").build()).build());
        parms.add(ImmutableParm.newBuilder().setParmName("passiveReasonCode").setValue(ImmutableValue.newBuilder().setContent("Some reason").build()).build());

        final IEvent e = ImmutableEvent.newBuilder()
                .setUei("uei.opennms.org/services/passiveServiceStatus")
                .setParms(parms)
                .build();

        psk.onEvent(e);

        final MonitoredService ms = createMonitoredService(1, "fooBar", null, "10.10.10.10", "theService");
        final ServiceMonitor sm = new PassiveServiceMonitor();
        final Date now = new Date();

        assertTrue(sm.poll(ms, new HashMap<>()).getTimestamp().getTime() - now.getTime() < 1000);
    }
}
