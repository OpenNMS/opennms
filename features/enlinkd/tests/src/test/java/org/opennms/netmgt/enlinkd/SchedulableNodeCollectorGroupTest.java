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
package org.opennms.netmgt.enlinkd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.opennms.netmgt.enlinkd.common.NodeCollector;
import org.opennms.netmgt.enlinkd.common.SchedulableNodeCollectorGroup;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.SubNetwork;
import org.opennms.netmgt.scheduler.LegacyPriorityExecutor;
import org.opennms.netmgt.scheduler.PriorityReadyRunnable;

/**
 * The collector group must submit one collector per node per cycle: the
 * identity-based NodeCollector.equals() used to defeat the group's
 * deduplication, so the executable set grew by one collector per node on
 * every cycle and re-submitted the whole backlog each time.
 */
public class SchedulableNodeCollectorGroupTest {

    private static Node node(int id) {
        try {
            return new Node(id, "node" + id, InetAddress.getByName("192.0.2." + id), ".1.1", "node" + id, "Default");
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static class RecordingExecutor extends LegacyPriorityExecutor {
        final List<PriorityReadyRunnable> submitted = new ArrayList<>();

        RecordingExecutor() {
            super("test", 1, 10);
        }

        @Override
        public synchronized void addPriorityReadyRunnable(PriorityReadyRunnable job) {
            submitted.add(job);
        }
    }

    private static class StubNodeTopologyService implements NodeTopologyService {
        final List<Node> nodes;

        StubNodeTopologyService(List<Node> nodes) {
            this.nodes = nodes;
        }

        @Override public List<Node> findAllSnmpNode() { return nodes; }
        @Override public Set<SubNetwork> findAllSubNetwork() { return Set.of(); }
        @Override public Set<SubNetwork> findAllLegalSubNetwork() { return Set.of(); }
        @Override public Set<SubNetwork> findSubNetworkByNetworkPrefixLessThen(int ipv4prefix, int ipv6prefix) { return Set.of(); }
        @Override public Set<SubNetwork> findAllPointToPointSubNetwork() { return Set.of(); }
        @Override public Set<SubNetwork> findAllLegalPointToPointSubNetwork() { return Set.of(); }
        @Override public Set<SubNetwork> findAllLoopbacks() { return Set.of(); }
        @Override public Set<SubNetwork> findAllLegalLoopbacks() { return Set.of(); }
        @Override public Map<Integer, Integer> getNodeidPriorityMap(ProtocolSupported protocol) { return new HashMap<>(); }
        @Override public Node getSnmpNode(String nodeCriteria) { return null; }
        @Override public Node getSnmpNode(int nodeid) { return null; }
        @Override public Set<SubNetwork> getSubNetworks(int nodeid) { return Set.of(); }
        @Override public Set<SubNetwork> getLegalSubNetworks(int nodeid) { return Set.of(); }
        @Override public List<NodeTopologyEntity> findAllNode() { return List.of(); }
        @Override public List<IpInterfaceTopologyEntity> findAllIp() { return List.of(); }
        @Override public List<SnmpInterfaceTopologyEntity> findAllSnmp() { return List.of(); }
        @Override public NodeTopologyEntity getDefaultFocusPoint() { return null; }
        @Override public boolean parseUpdates() { return false; }
        @Override public void updatesAvailable() { }
        @Override public boolean hasUpdates() { return false; }
        @Override public void refresh() { }
    }

    private static class TestCollector extends NodeCollector {
        TestCollector(Node node, int priority) {
            super(null, node, priority);
        }

        @Override
        public void collect() {
        }

        @Override
        public String getName() {
            return "TestCollector";
        }
    }

    private static class TestGroup extends SchedulableNodeCollectorGroup {
        TestGroup(LegacyPriorityExecutor executor, NodeTopologyService nodeTopologyService) {
            super(60000, 0, executor, 0, ProtocolSupported.LLDP, nodeTopologyService, null);
        }

        @Override
        public NodeCollector getNodeCollector(Node node, int priority) {
            return new TestCollector(node, priority);
        }
    }

    @Test
    public void collectorEqualityIsByNode() {
        final Node one = node(1);
        assertEquals(new TestCollector(one, 0), new TestCollector(one, 5));
        assertEquals(new TestCollector(one, 0).hashCode(), new TestCollector(one, 5).hashCode());
        assertNotEquals(new TestCollector(one, 0), new TestCollector(node(2), 0));
    }

    @Test
    public void groupSubmitsOneCollectorPerNodePerCycle() {
        final RecordingExecutor executor = new RecordingExecutor();
        final TestGroup group = new TestGroup(executor, new StubNodeTopologyService(Arrays.asList(node(1), node(2))));

        group.runSchedulable();
        group.runSchedulable();
        group.runSchedulable();

        assertEquals(6, executor.submitted.size());
        assertEquals(2, group.getExecutables().size());
    }

    @Test
    public void suspendedNodesAreSkipped() {
        final RecordingExecutor executor = new RecordingExecutor();
        final TestGroup group = new TestGroup(executor, new StubNodeTopologyService(Arrays.asList(node(1), node(2))));

        group.suspend(1);
        group.runSchedulable();

        assertEquals(1, executor.submitted.size());
        assertEquals(1, group.getExecutables().size());

        group.wakeUp(1);
        group.runSchedulable();

        assertEquals(3, executor.submitted.size());
        assertEquals(2, group.getExecutables().size());
    }
}
