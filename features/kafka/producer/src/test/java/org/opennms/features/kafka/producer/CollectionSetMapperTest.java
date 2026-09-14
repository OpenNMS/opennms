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
package org.opennms.features.kafka.producer;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.features.kafka.producer.collection.CollectionSetMapper;
import org.opennms.features.kafka.producer.collection.MetricTopicRouter;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.LatencyTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;

public class CollectionSetMapperTest {


    @Test
    public void testCollectionSetForInterfaceResource() throws UnknownHostException {

        ServiceParameters EMPTY_PARAMS = new ServiceParameters(Collections.emptyMap());

        CollectionSetMapper collectionSetMapper = new CollectionSetMapper(Mockito.mock(NodeDao.class), Mockito.mock(SessionUtils.class), Mockito.mock(ResourceDao.class));

        CollectionAgent agent = new MockCollectionAgent(1, "test", InetAddress.getLocalHost());
        NodeLevelResource nodeResource = new NodeLevelResource(1);
        // Null instance should not build any collection set.
        InterfaceLevelResource interfaceLevelResource = new InterfaceLevelResource(nodeResource, null);

        CollectionSet collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withNumericAttribute(interfaceLevelResource, "group1", "interface1", 105, AttributeType.GAUGE)
                .withNumericAttribute(interfaceLevelResource, "group2", "interface2", 1050, AttributeType.GAUGE).build();
        CollectionSetProtos.CollectionSet collectionSetProto = collectionSetMapper.buildCollectionSetProtos(collectionSet, EMPTY_PARAMS);
        assertThat(collectionSetProto.getResourceList(), Matchers.hasSize(0));

        // If Instance is Integer, it is mostly IfIndex.
        interfaceLevelResource = new InterfaceLevelResource(nodeResource, "25");
        collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withNumericAttribute(interfaceLevelResource, "group1", "interface1", 105, AttributeType.GAUGE)
                .withNumericAttribute(interfaceLevelResource, "group2", "interface2", 1050, AttributeType.GAUGE).build();
        collectionSetProto = collectionSetMapper.buildCollectionSetProtos(collectionSet, EMPTY_PARAMS);
        assertThat(collectionSetProto.getResourceList(), Matchers.hasSize(1));
        CollectionSetProtos.CollectionSetResource collectionSetResource = collectionSetProto.getResource(0);
        assertTrue(collectionSetResource.hasInterface());
        assertThat(collectionSetResource.getInterface().getIfIndex(), Matchers.is(25));
        assertThat(collectionSetResource.getInterface().getInstance(), Matchers.is("25"));
    }

    @Test
    public void testNodeLevelResourceForeignIdentity() throws UnknownHostException {
        NodeDao nodeDao = Mockito.mock(NodeDao.class);
        OnmsNode mockNode = new OnmsNode();
        mockNode.setId(1);
        mockNode.setLabel("TestNode");
        mockNode.setForeignId("foo");
        mockNode.setForeignSource("bar");
        when(nodeDao.get("1")).thenReturn(mockNode);

        SessionUtils sessionUtils = Mockito.mock(SessionUtils.class);
        when(sessionUtils.withReadOnlyTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(0);
                    return supplier.get();
                });
        ServiceParameters EMPTY_PARAMS = new ServiceParameters(Collections.emptyMap());

        CollectionSetMapper collectionSetMapper = new CollectionSetMapper(nodeDao, sessionUtils, Mockito.mock(ResourceDao.class));

        CollectionAgent agent = new MockCollectionAgent(1, "TestNode", InetAddress.getLocalHost());

        NodeLevelResource nodeResource = new NodeLevelResource(1);

        CollectionSet collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2)).withGauge(nodeResource, "baz", "baz", 1.0).build();
        CollectionSetProtos.CollectionSet collectionSetProto = collectionSetMapper.buildCollectionSetProtos(collectionSet, EMPTY_PARAMS);
        assertThat(collectionSetProto.getResourceList(), Matchers.hasSize(1));
        CollectionSetProtos.CollectionSetResource collectionSetResource = collectionSetProto.getResource(0);
        assertTrue(collectionSetResource.hasNode());
        assertThat(collectionSetResource.getNode().getForeignId(), Matchers.is("foo"));
        assertThat(collectionSetResource.getNode().getForeignSource(), Matchers.is("bar"));
    }

    @Test
    public void testCollectionSetForResponseResource() throws UnknownHostException {
        NodeDao nodeDao = Mockito.mock(NodeDao.class);
        OnmsNode mockNode = new OnmsNode();
        mockNode.setId(1);
        mockNode.setLabel("TestNode");
        OnmsNode snmpNode = new OnmsNode();
        snmpNode.setId(14);
        snmpNode.setLabel("SnmpNode");
        when(nodeDao.get("1")).thenReturn(mockNode);
        when(nodeDao.get("14")).thenReturn(snmpNode);

        SessionUtils sessionUtils = Mockito.mock(SessionUtils.class);
        when(sessionUtils.withReadOnlyTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(0);
                    return supplier.get();
                });
        ServiceParameters EMPTY_PARAMS = new ServiceParameters(Collections.emptyMap());

        CollectionSetMapper collectionSetMapper = new CollectionSetMapper(nodeDao, sessionUtils, Mockito.mock(ResourceDao.class));

        CollectionAgent agent = new MockCollectionAgent(1, "TestNode", InetAddress.getLocalHost());

        LatencyTypeResource latencyTypeResource = new LatencyTypeResource("ICMP", InetAddress.getLocalHost().getHostAddress(), "Default");
        latencyTypeResource.addTag("node_id", "1");
        latencyTypeResource.addTag("location", "Default");

        CollectionSet collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withGauge(latencyTypeResource, "icmp", "icmp", 1.0)
                .build();
        CollectionSetProtos.CollectionSet collectionSetProto = collectionSetMapper.buildCollectionSetProtos(collectionSet, EMPTY_PARAMS);
        assertThat(collectionSetProto.getResourceList(), Matchers.hasSize(1));
        CollectionSetProtos.CollectionSetResource collectionSetResource = collectionSetProto.getResource(0);
        assertTrue(collectionSetResource.hasResponse());
        assertThat(collectionSetResource.getResponse().getNode().getNodeId(), Matchers.is(1L));
        assertThat(collectionSetResource.getResponse().getNode().getNodeLabel(), Matchers.is("TestNode"));

        agent = new MockCollectionAgent(14, "SnmpNode", InetAddress.getByName("10.11.12.13"));
        latencyTypeResource = new LatencyTypeResource("SNMP", "10.11.12.13", "Default");
        collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withGauge(latencyTypeResource, "snmp", "snmp", 1.0)
                .build();
        collectionSetProto = collectionSetMapper.buildCollectionSetProtos(collectionSet, EMPTY_PARAMS);
        assertThat(collectionSetProto.getResourceList(), Matchers.hasSize(1));
        collectionSetResource = collectionSetProto.getResource(0);
        assertTrue(collectionSetResource.hasResponse());
        assertThat(collectionSetResource.getResponse().getNode().getNodeId(), Matchers.is(14L));
        assertThat(collectionSetResource.getResponse().getNode().getNodeLabel(), Matchers.is("SnmpNode"));
    }

    /**
     * The routing-disabled path has to be indistinguishable from the un-routed behaviour, down to
     * the serialized bytes. Both entry points share one visitor, so this is what actually holds
     * that guarantee - rather than a duplicated code path.
     */
    @Test
    public void testRoutingDisabledIsByteIdenticalToTheUnroutedMapping() throws UnknownHostException {
        final CollectionSetMapper collectionSetMapper = mapperForNodes(1);
        final ServiceParameters params = new ServiceParameters(Collections.emptyMap());
        final CollectionSet collectionSet = mixedCollectionSet();

        final CollectionSetProtos.CollectionSet unrouted =
                collectionSetMapper.buildCollectionSetProtos(collectionSet, params);

        // A null router means "no routing at all"; the default topic is the only group.
        final Map<String, CollectionSetProtos.CollectionSet> byTopic = collectionSetMapper
                .buildCollectionSetProtosByTopic(mixedCollectionSet(), params, null, "metrics");

        assertThat(byTopic.keySet(), Matchers.contains("metrics"));
        assertArrayEquals(unrouted.toByteArray(), byTopic.get("metrics").toByteArray());
        assertThat(unrouted.getResourceList(), Matchers.hasSize(3));
    }

    @Test
    public void testResourcesAreGroupedByResolvedTopicKeepingTimestampAndOrder() throws UnknownHostException {
        final CollectionSetMapper collectionSetMapper = mapperForNodes(1);
        final ServiceParameters params = new ServiceParameters(Collections.emptyMap());

        // Route interface resources elsewhere, everything else to the default topic.
        final MetricTopicRouter router = Mockito.mock(MetricTopicRouter.class);
        when(router.resolveTopic(any(CollectionResource.class), Mockito.anyInt(), any(ServiceParameters.class)))
                .thenAnswer(invocation -> {
                    final CollectionResource resource = invocation.getArgument(0);
                    return CollectionResource.RESOURCE_TYPE_IF.equals(resource.getResourceTypeName())
                            ? "routed" : "metrics";
                });

        final Map<String, CollectionSetProtos.CollectionSet> byTopic = collectionSetMapper
                .buildCollectionSetProtosByTopic(mixedCollectionSet(), params, router, "metrics");

        assertThat(byTopic.keySet(), Matchers.containsInAnyOrder("metrics", "routed"));
        assertThat(byTopic.get("routed").getResourceList(), Matchers.hasSize(1));
        assertTrue(byTopic.get("routed").getResource(0).hasInterface());
        // node level + latency
        assertThat(byTopic.get("metrics").getResourceList(), Matchers.hasSize(2));
        // Every group carries the timestamp of the CollectionSet it came from
        assertEquals(2L, byTopic.get("metrics").getTimestamp());
        assertEquals(2L, byTopic.get("routed").getTimestamp());
    }

    /**
     * Response time resources used to reach the router with node id 0, because the node was
     * resolved inside buildResponseTimeResource and then discarded.
     */
    @Test
    public void testLatencyResourcesAreRoutedWithTheirNodeId() throws UnknownHostException {
        final CollectionSetMapper collectionSetMapper = mapperForNodes(1);
        final ServiceParameters params = new ServiceParameters(Collections.emptyMap());

        final MetricTopicRouter router = Mockito.mock(MetricTopicRouter.class);
        when(router.resolveTopic(any(CollectionResource.class), Mockito.anyInt(), any(ServiceParameters.class)))
                .thenReturn("metrics");

        final CollectionAgent agent = new MockCollectionAgent(1, "TestNode", InetAddress.getLocalHost());
        final LatencyTypeResource latencyTypeResource = new LatencyTypeResource("ICMP",
                InetAddress.getLocalHost().getHostAddress(), "Default");
        latencyTypeResource.addTag("node_id", "1");
        final CollectionSet collectionSet = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withGauge(latencyTypeResource, "icmp", "icmp", 1.0).build();

        collectionSetMapper.buildCollectionSetProtosByTopic(collectionSet, params, router, "metrics");

        // The collecting service reaches the router through the ServiceParameters, and the node id
        // now survives for latency resources as well.
        Mockito.verify(router).resolveTopic(any(CollectionResource.class), Mockito.eq(1), Mockito.same(params));
    }

    /** A node level, an SNMP interface and a response time resource in one CollectionSet. */
    private static CollectionSet mixedCollectionSet() throws UnknownHostException {
        final CollectionAgent agent = new MockCollectionAgent(1, "TestNode", InetAddress.getLocalHost());
        final NodeLevelResource nodeResource = new NodeLevelResource(1);
        final InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeResource, "25");
        final LatencyTypeResource latencyTypeResource = new LatencyTypeResource("ICMP",
                InetAddress.getLocalHost().getHostAddress(), "Default");
        latencyTypeResource.addTag("node_id", "1");

        return new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withGauge(nodeResource, "group1", "cpu", 1.0)
                .withNumericAttribute(interfaceResource, "group2", "ifInOctets", 105, AttributeType.GAUGE)
                .withGauge(latencyTypeResource, "icmp", "icmp", 1.0)
                .build();
    }

    private static CollectionSetMapper mapperForNodes(final int... nodeIds) {
        final NodeDao nodeDao = Mockito.mock(NodeDao.class);
        for (final int nodeId : nodeIds) {
            final OnmsNode node = new OnmsNode();
            node.setId(nodeId);
            node.setLabel("TestNode" + nodeId);
            when(nodeDao.get(Integer.toString(nodeId))).thenReturn(node);
        }

        final SessionUtils sessionUtils = Mockito.mock(SessionUtils.class);
        when(sessionUtils.withReadOnlyTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());

        return new CollectionSetMapper(nodeDao, sessionUtils, Mockito.mock(ResourceDao.class));
    }
}
