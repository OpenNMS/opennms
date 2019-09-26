/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologyPersister;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
import org.opennms.netmgt.enlinkd.BridgeOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.CdpOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.IsisOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.LldpOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.NodesOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.OspfOnmsTopologyUpdater;
import org.opennms.netmgt.enlinkd.UserDefinedLinkTopologyUpdater;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-LinkdTopologyProviderTestIT.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class LinkdTopologyProviderTestIT {

    private final static Logger LOG = LoggerFactory.getLogger(LinkdTopologyProviderTestIT.class);

    @Autowired
    GenericPersistenceAccessor genericPersistenceAccessor;

    @Autowired
    LinkdTopologyProvider linkdTopologyProvider;

    @Autowired
    TopologyEntityCache entityCache;
    
    @Autowired
    NodesOnmsTopologyUpdater nodesOnmsTopologyUpdater;

    @Autowired
    CdpOnmsTopologyUpdater cdpOnmsTopologyUpdater;

    @Autowired
    IsisOnmsTopologyUpdater isisOnmsTopologyUpdater;
    
    @Autowired
    LldpOnmsTopologyUpdater lldpOnmsTopologyUpdater;
    
    @Autowired
    OspfOnmsTopologyUpdater ospfOnmsTopologyUpdater;

    @Autowired
    BridgeOnmsTopologyUpdater bridgeOnmsTopologyUpdater;

    @Autowired
    UserDefinedLinkTopologyUpdater userDefinedLinkTopologyUpdater;

    @Autowired
    BridgeTopologyService bridgeTopologyService;

    private TopologyGenerator generator;

    @BeforeTransaction
    public void setUp() {
        nodesOnmsTopologyUpdater.register();
        cdpOnmsTopologyUpdater.register();
        isisOnmsTopologyUpdater.register();
        lldpOnmsTopologyUpdater.register();
        ospfOnmsTopologyUpdater.register();
        bridgeOnmsTopologyUpdater.register();
        userDefinedLinkTopologyUpdater.register();

        TopologyGenerator.ProgressCallback progressCallback = new TopologyGenerator.ProgressCallback(LOG::info);
        TopologyPersister persister = new TopologyPersister(genericPersistenceAccessor, progressCallback);
        generator = TopologyGenerator.builder()
                .persister(persister)
                .progressCallback(progressCallback).build();
    }

    @Test
    @Transactional
    public void testCdp() {
        test(TopologyGenerator.Protocol.cdp);
    }

    @Test
    @Transactional
    public void testLldp() {
        test(TopologyGenerator.Protocol.lldp);
    }

    @Test
    @Transactional
    public void testIsis() {
        test(TopologyGenerator.Protocol.isis);
    }

    @Test
    @Transactional
    public void testOspf() {
        test(TopologyGenerator.Protocol.ospf);
    }

    @Test
    @Transactional
    public void testUserDefined() {
        test(TopologyGenerator.Protocol.userdefined, false);
    }

    /**
     * We expect the following topology:
     *
     *                      bridge0
     *                         |
     *           ------------------------------------------------
     *           |          |          |        |       |       |
     *        bridge1    bridge3    bridge4  bridge5  Macs/Ip bridge10
     *           |          |                         no node
     *        -------    --------
     *        |          |      |
     *     Segment     host8    host9
     *   -----------
     *   |         |
     * Macs/Ip  bridge2
     * no node  -------
     *             |
     *          Segment
     */

    @Test
    @Transactional
    public void testBridge() {
        // The testing of bridge topologies is a bit different than for the other protocols since we have a hierarchical
        // topology with different node types.

        // 1.) Generate Topology
        TopologySettings settings = TopologySettings.builder()
                .protocol(TopologyGenerator.Protocol.bridge)
                .amountNodes(10)
                .amountIpInterfaces(0)
                .amountSnmpInterfaces(0)
                .build();
        generateTopologyAndRefreshCaches(settings);

        // 2.) map the nodes by it's label name.
        final Map<String, Vertex> vertices = new HashMap<>();
        final Map<Integer,Vertex> verticesById = new HashMap<>();
        for(Vertex vertex : linkdTopologyProvider.getVerticesWithoutGroups()) {
            String label = vertex.getLabel();
            if("Segment".equals(label)) { // enhance Segment to make it unique
                label = StringUtils.substringAfter(vertex.getTooltipText(), "nodeid:["); // Shared Segment': nodeid:[13561], bridgeport
                label = StringUtils.substringBefore(label, "]");
                label = verticesById.get(Integer.parseInt(label)).getLabel(); // get label by id of node
                label = "Segment["+label+"]";
            } else if (label.contains("without node")) {
                // shared addresses: ([000000000007, 000000000008]ip:[0.0.0.1 ], mac:[000000000005]ip:[0.0.0.2 ], mac:[000000000006])(Unknown/Not an OpenNMS Node)
                label = StringUtils.substringAfter(vertex.getTooltipText(), "shared addresses: ([");
                label = StringUtils.substringBefore(label, ",");
                label = "NoNode["+label+"]";
            } else {
                verticesById.put(vertex.getNodeID(), vertex);
            }
            vertices.put(label, vertex);
        }


        // 3.) Check linking:
        // Level 0 -> 1
        verifyLinkingBetweenNodes(vertices.get("Node0"), vertices.get("Node1"));
        verifyLinkingBetweenNodes(vertices.get("Node0"), vertices.get("Node3"));
        verifyLinkingBetweenNodes(vertices.get("Node0"), vertices.get("Node4"));
        verifyLinkingBetweenNodes(vertices.get("Node0"), vertices.get("Node5"));
        verifyLinkingBetweenNodes(vertices.get("Node0"), vertices.get("NoNode[000000000007]"));
        verifyLinkingBetweenNodes(vertices.get("Node0"), vertices.get("Node10"));

        // Level 1 -> 2
        verifyLinkingBetweenNodes(vertices.get("Node1"), vertices.get("Segment[Node1]"));
        verifyLinkingBetweenNodes(vertices.get("Node3"), vertices.get("Node8"));
        verifyLinkingBetweenNodes(vertices.get("Node3"), vertices.get("Node9"));

        // Level 2 -> 3
        verifyLinkingBetweenNodes(vertices.get("Segment[Node1]"), vertices.get("NoNode[00000000000e]"));
        verifyLinkingBetweenNodes(vertices.get("Segment[Node1]"), vertices.get("Node2"));

        // Level 3 -> 4
        verifyLinkingBetweenNodes(vertices.get("Node2"), vertices.get("Segment[Node2]"));
        verifyLinkingBetweenNodes(vertices.get("Segment[Node2]"), vertices.get("Node6"));
        verifyLinkingBetweenNodes(vertices.get("Segment[Node2]"), vertices.get("Node7"));

    }

    private void test(TopologyGenerator.Protocol protocol) {
        test(protocol, true);
    }

    private void test(TopologyGenerator.Protocol protocol, boolean linksAreCached) {
        testAmounts(protocol, linksAreCached);
        testLinkingBetweenNodes(protocol);
    }

    private void testAmounts(TopologyGenerator.Protocol protocol, boolean linksAreCached) {

        TopologySettings settings = TopologySettings.builder()
                .protocol(protocol)
                .build();

        // 1.) Generate topology and verify that the TopologyProvider finds it:
        generator.generateTopology(settings);
        verifyAmounts(settings);

        // 2.) Delete the topology. The TopologyProvider should still find it due to the cache:
        generator.deleteTopology();
        if (linksAreCached) {
            verifyAmounts(settings);
        }

        // 3.) Invalidate cache - nothing should be found:
        entityCache.refresh();
        refresh();
        assertEquals(0, linkdTopologyProvider.getVerticesWithoutGroups().size());
    }

    private void refresh() {
        nodesOnmsTopologyUpdater.setTopology(nodesOnmsTopologyUpdater.buildTopology());
        cdpOnmsTopologyUpdater.setTopology(cdpOnmsTopologyUpdater.buildTopology());
        isisOnmsTopologyUpdater.setTopology(isisOnmsTopologyUpdater.buildTopology());
        lldpOnmsTopologyUpdater.setTopology(lldpOnmsTopologyUpdater.buildTopology());
        ospfOnmsTopologyUpdater.setTopology(ospfOnmsTopologyUpdater.buildTopology());
        userDefinedLinkTopologyUpdater.setTopology(userDefinedLinkTopologyUpdater.buildTopology());
        bridgeTopologyService.load();
        bridgeOnmsTopologyUpdater.setTopology(bridgeOnmsTopologyUpdater.buildTopology());
        linkdTopologyProvider.refresh();
    }

    private void verifyAmounts(TopologySettings settings) {
        refresh();
        List<Vertex> vertices = linkdTopologyProvider.getVerticesWithoutGroups();

        // Check amount nodes
        assertEquals(settings.getAmountNodes(), vertices.size());

        // Check amount edges
        Map<VertexRef, Set<EdgeRef>> edgeIds = linkdTopologyProvider.getEdgeIdsForVertices(vertices.toArray(new Vertex[vertices.size()]));
        Set<EdgeRef> allEdges = new HashSet<>();
        edgeIds.values().forEach(allEdges::addAll);
        // 2 links form one edge:
        int expectedAmountOfEdges = settings.getAmountLinks() / 2;
        assertEquals(expectedAmountOfEdges, linkdTopologyProvider.getEdgeTotalCount());
        assertEquals(expectedAmountOfEdges, allEdges.size());
    }

    private void generateTopologyAndRefreshCaches(TopologySettings settings) {
        generator.generateTopology(settings);
        entityCache.refresh();
        
        refresh();
    }

    /**
     * Generates a ring topology and verifies that each Vertex is connected to it's neighbors.
     */
    private void testLinkingBetweenNodes(TopologyGenerator.Protocol protocol) {

        // 1.) Generate Topology
        TopologySettings settings = TopologySettings.builder()
                .protocol(protocol)
                .amountNodes(10) // use 10 so that the label names remain in the single digits => makes sorting easier
                .amountLinks(20) // one edge is composed of 2 links
                .topology(TopologyGenerator.Topology.ring) // deterministic behaviour: each node is connected to its neighbors
                .build();
        generateTopologyAndRefreshCaches(settings);
        assertEquals(settings.getAmountNodes(), linkdTopologyProvider.getVerticesWithoutGroups().size());

        // 2.) sort the nodes by it's label name.
        List<Vertex> vertices = linkdTopologyProvider.getVerticesWithoutGroups();
        Vertex[] verticesArray = vertices.toArray(new Vertex[vertices.size()]);
        Arrays.sort(verticesArray, Comparator.comparing(Vertex::getLabel).thenComparing(Vertex::getNodeID));
        vertices = Arrays.asList(verticesArray);

        // 3.) test the linking between each node and its next neighbor
        for(int i = 0; i < vertices.size(); i++){
            VertexRef left = vertices.get(i);
            VertexRef right = vertices.get(nextIndexInList(i, vertices.size()-1));
            verifyLinkingBetweenNodes(left, right);
        }
    }

    private void verifyLinkingBetweenNodes(VertexRef left, VertexRef right) {

        // 1.) get the EdgeRef that connects the 2 vertices
        List<EdgeRef> leftRefs = Arrays.asList(this.linkdTopologyProvider.getEdgeIdsForVertex(left));
        List<EdgeRef>  rightRefs = Arrays.asList(this.linkdTopologyProvider.getEdgeIdsForVertex(right));
        Set<EdgeRef> intersection = intersect(leftRefs, rightRefs);
        assertEquals(1, intersection.size());
        EdgeRef ref = intersection.iterator().next();

        // 2.) get the Edge and check if it really connects the 2 Vertices
        Edge edge = this.linkdTopologyProvider.getEdge(ref);
        // we don't know the direction it is connected so we have to test both ways:
        assertTrue(
                (edge.getSource().getVertex().equals(left) || edge.getSource().getVertex().equals(right)) // source side
                        && (edge.getTarget().getVertex().equals(left) || edge.getTarget().getVertex().equals(right)) // target side
                        && !edge.getSource().getVertex().equals(edge.getTarget().getVertex())); // make sure it doesn't connect the same node
    }

    /**
     * Gives back the intersection between the 2 collections, as in:
     * - only elements that are contained in both collections will be retained
     * - double elements are removed
     */
    private <E> Set<E> intersect(final Collection<E> left, final Collection<E> right){
        Set<E> set = new HashSet<>(left);
        set.retainAll(new HashSet<>(right));
        return set;
    }

    private int nextIndexInList(int current, int lastIndexInList) {
        if (current == lastIndexInList) {
            return 0;
        }
        return ++current;
    }

}
