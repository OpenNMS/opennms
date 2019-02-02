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

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.enlinkd.generator.TopologyGenerator;
import org.opennms.enlinkd.generator.TopologyPersister;
import org.opennms.enlinkd.generator.TopologySettings;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.GenericPersistenceAccessor;
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
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
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
    BridgeTopologyService bridgeTopologyService;

    private TopologyGenerator generator;

    @BeforeTransaction
    public void setUp() {
        bridgeTopologyService.load(); // avoid Nullpointer later
        TopologyGenerator.ProgressCallback progressCallback = new TopologyGenerator.ProgressCallback(LOG::info);
        TopologyPersister persister = new TopologyPersister(genericPersistenceAccessor, progressCallback);
        generator = TopologyGenerator.builder()
                .persister(persister)
                .progressCallback(progressCallback).build();
    }

    @Test
    @Transactional
    public void testCdp() {
        testAmounts(TopologyGenerator.Protocol.cdp);
    }

    @Test
    @Transactional
    public void testLldp() {
        testAmounts(TopologyGenerator.Protocol.lldp);
    }

    @Test
    @Transactional
    public void testIsis() {
        testAmounts(TopologyGenerator.Protocol.isis);
    }

    @Test
    @Transactional
    public void testOspf() {
        testAmounts(TopologyGenerator.Protocol.ospf);
    }

    private void testAmounts(TopologyGenerator.Protocol protocol) {

        TopologySettings settings = TopologySettings.builder()
                .protocol(protocol)
                .build();

        // 1.) Generate topology and verify that the TopologyProvider finds it:
        generator.generateTopology(settings);
        verifyAmounts(settings);

        // 2.) Delete the topology. The TopologyProvider should still find it due to the cache:
        generator.deleteTopology();
        verifyAmounts(settings);

        // 3.) Invalidate cache - nothing should be found:
        entityCache.refresh();
        linkdTopologyProvider.refresh();
        assertEquals(0, linkdTopologyProvider.getVerticesWithoutGroups().size());
    }

    private void verifyAmounts(TopologySettings settings) {
        linkdTopologyProvider.refresh();

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

}
