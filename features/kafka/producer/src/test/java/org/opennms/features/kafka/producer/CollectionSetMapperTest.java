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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.features.kafka.producer.collection.CollectionSetMapper;
import org.opennms.features.kafka.producer.collection.MetricTopicRouter;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.netmgt.collectd.IfInfo;
import org.opennms.netmgt.collectd.IfResourceType;
import org.opennms.netmgt.collectd.MibPropertyAttributeType;
import org.opennms.netmgt.collectd.NumericAttributeType;
import org.opennms.netmgt.collectd.OnmsSnmpCollection;
import org.opennms.netmgt.collectd.SnmpCollectionAgent;
import org.opennms.netmgt.collectd.SnmpCollectionSet;
import org.opennms.netmgt.collectd.SnmpIfData;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.CollectionAttributeWrapper;
import org.opennms.netmgt.collection.support.CollectionSetVisitorWrapper;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.LatencyTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.config.api.DataCollectionConfigDao;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

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

    @Test
    public void testNodeIsResolvedOncePerCollectionSet() throws UnknownHostException {
        final NodeDao nodeDao = Mockito.mock(NodeDao.class);
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("TestNode");
        when(nodeDao.get("1")).thenReturn(node);
        final SessionUtils sessionUtils = Mockito.mock(SessionUtils.class);
        when(sessionUtils.withReadOnlyTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());
        final CollectionSetMapper mapper = new CollectionSetMapper(nodeDao, sessionUtils, Mockito.mock(ResourceDao.class));

        // One device reporting many interfaces, as a gNMI or large-ifTable SNMP collection does.
        final CollectionAgent agent = new MockCollectionAgent(1, "TestNode", InetAddress.getLocalHost());
        final NodeLevelResource nodeResource = new NodeLevelResource(1);
        final CollectionSetBuilder builder = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withGauge(nodeResource, "group1", "cpu", 1.0);
        final int interfaces = 144;
        for (int ifIndex = 1; ifIndex <= interfaces; ifIndex++) {
            builder.withNumericAttribute(new InterfaceLevelResource(nodeResource, Integer.toString(ifIndex)),
                    "mib2-interfaces", "ifInOctets", ifIndex, AttributeType.COUNTER);
        }

        final CollectionSetProtos.CollectionSet proto = mapper.buildCollectionSetProtos(builder.build(),
                new ServiceParameters(Collections.emptyMap()));

        assertThat(proto.getResourceList(), Matchers.hasSize(interfaces + 1));
        assertTrue(proto.getResourceList().stream()
                .allMatch(r -> (r.hasNode() ? r.getNode() : r.getInterface().getNode()).getNodeId() == 1));
        Mockito.verify(nodeDao, Mockito.times(1)).get("1");
        Mockito.verify(sessionUtils, Mockito.times(1)).withReadOnlyTransaction(any(Supplier.class));
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

    private CollectionSetProtos.CollectionSetResource mapInterfaceResource(String agentProtocol,
            java.util.function.BiConsumer<CollectionSetBuilder, InterfaceLevelResource> fill) throws UnknownHostException {
        final CollectionSetMapper mapper = new CollectionSetMapper(Mockito.mock(NodeDao.class), Mockito.mock(SessionUtils.class), Mockito.mock(ResourceDao.class));
        final CollectionAgent agent = new MockCollectionAgent(1, "test", InetAddress.getLocalHost());
        final InterfaceLevelResource r = new InterfaceLevelResource(new NodeLevelResource(1), "25");
        final CollectionSetBuilder builder = new CollectionSetBuilder(agent).withTimestamp(new Date(2));
        fill.accept(builder, r);
        final CollectionSetProtos.CollectionSet proto = mapper.buildCollectionSetProtos(builder.build(),
                ServiceParameters.forAgentProtocol(agentProtocol));
        assertThat(proto.getResourceList(), Matchers.hasSize(1));
        return proto.getResource(0);
    }

    private static List<String> agentProtocols(CollectionSetProtos.CollectionSetResource resource) {
        return resource.getStringList().stream().filter(a -> "agent_protocol".equals(a.getName()))
                .map(CollectionSetProtos.StringAttribute::getValue).collect(Collectors.toList());
    }

    @Test
    public void testSnmpIdentifierIsNormalized() throws UnknownHostException {
        final CollectionSetProtos.CollectionSetResource resource = mapInterfaceResource("SNMP", (b, r) ->
                b.withIdentifiedNumericAttribute(r, "mib2-X-interfaces", "ifHCInOctets", 10, AttributeType.COUNTER,
                        "SNMP_.1.3.6.1.2.1.31.1.1.1.6.512"));
        assertEquals(".1.3.6.1.2.1.31.1.1.1.6.512", resource.getNumeric(0).getMetricId());
        assertEquals(List.of("SNMP"), agentProtocols(resource));
    }

    @Test
    public void testIdentifiedStringAttributeGetsMetricId() throws UnknownHostException {
        final CollectionSetProtos.CollectionSetResource resource = mapInterfaceResource("SNMP", (b, r) ->
                b.withIdentifiedStringAttribute(r, "g", "ifAlias", "uplink", "SNMP_.1.3.6.1.2.1.31.1.1.1.18.512"));
        final CollectionSetProtos.StringAttribute alias = resource.getStringList().stream()
                .filter(a -> "ifAlias".equals(a.getName())).findFirst().get();
        assertEquals(".1.3.6.1.2.1.31.1.1.1.18.512", alias.getMetricId());
        assertEquals(List.of("SNMP"), agentProtocols(resource));
    }

    @Test
    public void testProtocolWithoutIdentifierPublishesOnlyAgentProtocol() throws UnknownHostException {
        final CollectionSetProtos.CollectionSetResource resource = mapInterfaceResource("HTTP", (b, r) ->
                b.withNumericAttribute(r, "group1", "documentCount", 105, AttributeType.GAUGE));
        assertEquals("", resource.getNumeric(0).getMetricId());
        assertEquals(List.of("HTTP"), agentProtocols(resource));
    }

    @Test
    public void testNoProtocolPublishesNeither() throws UnknownHostException {
        final CollectionSetProtos.CollectionSetResource resource = mapInterfaceResource(null, (b, r) ->
                b.withIdentifiedNumericAttribute(r, "g", "a", 1, AttributeType.GAUGE, "SNMP_.1.3.6.1.2.1.1"));
        assertEquals("", resource.getNumeric(0).getMetricId());
        assertEquals(List.of(), agentProtocols(resource));
    }

    @Test
    public void testPrefixedAttributeNameIsNotAnIdentifier() throws UnknownHostException {
        // Without an identifier the attribute name stands in for it; a name that happens
        // to start with the protocol prefix must not become a metric_id.
        final CollectionSetProtos.CollectionSetResource resource = mapInterfaceResource("SNMP", (b, r) ->
                b.withNumericAttribute(r, "group1", "SNMP_session_up", 1, AttributeType.GAUGE));
        assertEquals("", resource.getNumeric(0).getMetricId());
        assertEquals(List.of("SNMP"), agentProtocols(resource));
    }

    @Test
    public void testIdentifierOfAnotherProtocolIsIgnored() throws UnknownHostException {
        final CollectionSetProtos.CollectionSetResource resource = mapInterfaceResource("JMX", (b, r) ->
                b.withIdentifiedNumericAttribute(r, "g", "a", 1, AttributeType.GAUGE, "SNMP_.1.3.6.1.2.1.1"));
        assertEquals("", resource.getNumeric(0).getMetricId());
        assertEquals(List.of("JMX"), agentProtocols(resource));
    }

    @Test
    public void testLowercaseBmpIdentifierIsNotPublished() throws UnknownHostException {
        final CollectionSetProtos.CollectionSetResource resource = mapInterfaceResource("BMP", (b, r) ->
                b.withIdentifiedNumericAttribute(r, "bmp", "rejected", 3, AttributeType.COUNTER, "bmp_10.0.0.1_rejected"));
        assertEquals("", resource.getNumeric(0).getMetricId());
        assertEquals(List.of("BMP"), agentProtocols(resource));
    }

    @Test
    public void testExistingAgentProtocolIsKept() throws UnknownHostException {
        final CollectionSetProtos.CollectionSetResource resource = mapInterfaceResource("SNMP", (b, r) -> b
                .withStringAttribute(r, "g", "agent_protocol", "CUSTOM")
                .withIdentifiedNumericAttribute(r, "g", "a", 1, AttributeType.GAUGE, "SNMP_.1.3.6.1.2.1.1"));
        assertEquals(List.of("CUSTOM"), agentProtocols(resource));
    }

    private static final int IF_INDEX = 25;
    private static final String IF_HC_IN_OCTETS = ".1.3.6.1.2.1.31.1.1.1.6";
    private static final AttributeGroupType IF_GROUP = new AttributeGroupType("mib2-X-interfaces", AttributeGroupType.IF_TYPE_ALL);

    /** Collects ifHCInOctets on one interface with the real SNMP collector classes. */
    private static final class SnmpFixture {
        final SnmpCollectionAgent agent = Mockito.mock(SnmpCollectionAgent.class);
        final ServiceParameters params = ServiceParameters.forAgentProtocol("SNMP");
        final OnmsSnmpCollection collection;
        final SnmpCollectionSet collectionSet;
        final IfResourceType ifResourceType;
        final IfInfo ifInfo;

        SnmpFixture() {
            final OnmsNode node = new OnmsNode();
            node.setId(1);
            final OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, IF_INDEX);
            snmpInterface.setIfName("eth0");
            snmpInterface.setCollectionEnabled(true);
            final SnmpIfData ifData = new SnmpIfData(snmpInterface);

            when(agent.getNodeId()).thenReturn(1);
            when(agent.getStorageResourcePath()).thenReturn(ResourcePath.get("1"));
            when(agent.getSnmpInterfaceInfo(any(IfResourceType.class)))
                    .thenAnswer(invocation -> Set.of(new IfInfo(invocation.getArgument(0), agent, ifData)));

            final LocationAwareSnmpClient client = Mockito.mock(LocationAwareSnmpClient.class);
            collection = new OnmsSnmpCollection(agent, params, Mockito.mock(DataCollectionConfigDao.class), client);
            collectionSet = new SnmpCollectionSet(agent, collection, client);
            collectionSet.setCollectionTimestamp(new Date(2));
            ifResourceType = collection.getIfResourceType(agent);
            ifInfo = (IfInfo) ifResourceType.findResource(new SnmpInstId(IF_INDEX));

            final MibObject mibObject = new MibObject();
            mibObject.setOid(IF_HC_IN_OCTETS);
            mibObject.setInstance(MibObject.INSTANCE_IFINDEX);
            mibObject.setAlias("ifHCInOctets");
            mibObject.setType("counter");
            new NumericAttributeType(ifResourceType, collection.getName(), mibObject, IF_GROUP).storeResult(collectionSet, null,
                    new SnmpResult(SnmpObjId.get(IF_HC_IN_OCTETS), new SnmpInstId(IF_INDEX), SnmpUtils.getValueFactory().getCounter32(10)));
        }

        /** The node resource is always part of the set; return the only interface resource. */
        CollectionSetProtos.CollectionSetResource mapToInterfaceResource() {
            final CollectionSetMapper mapper = new CollectionSetMapper(Mockito.mock(NodeDao.class), Mockito.mock(SessionUtils.class), Mockito.mock(ResourceDao.class));
            final List<CollectionSetProtos.CollectionSetResource> interfaces = mapper.buildCollectionSetProtos(collectionSet, params)
                    .getResourceList().stream().filter(CollectionSetProtos.CollectionSetResource::hasInterface).collect(Collectors.toList());
            assertThat(interfaces, Matchers.hasSize(1));
            return interfaces.get(0);
        }
    }

    @Test
    public void testSnmpPropertyAttributeIsPublishedWithoutMetricId() {
        final SnmpFixture snmp = new SnmpFixture();
        // What the SNMP property extenders add: an attribute whose type has no MIB object.
        final MibObjProperty property = new MibObjProperty();
        property.setAlias("vendorName");
        property.setGroupName(IF_GROUP.getName());
        snmp.ifInfo.setAttributeValue(new MibPropertyAttributeType(snmp.ifResourceType, property, IF_GROUP),
                SnmpUtils.getValueFactory().getOctetString("acme".getBytes()));

        final CollectionSetProtos.CollectionSetResource resource = snmp.mapToInterfaceResource();
        assertEquals(IF_INDEX, resource.getInterface().getIfIndex());
        assertEquals(IF_HC_IN_OCTETS + "." + IF_INDEX, resource.getNumeric(0).getMetricId());
        final CollectionSetProtos.StringAttribute vendorName = resource.getStringList().stream()
                .filter(a -> "vendorName".equals(a.getName())).findFirst().get();
        assertEquals("acme", vendorName.getValue());
        assertEquals("", vendorName.getMetricId());
        assertEquals(List.of("SNMP"), agentProtocols(resource));
    }

    @Test
    public void testSnmpAliasedResourceDoesNotBreakTheCollectionSet() {
        final SnmpFixture snmp = new SnmpFixture();
        // The SNMP collector creates an aliased resource for each interface that returns ifAlias.
        snmp.collection.getAliasAttributeTypes(snmp.agent).get(0).storeResult(snmp.collectionSet, null,
                new SnmpResult(SnmpObjId.get(".1.3.6.1.2.1.31.1.1.1.18"), new SnmpInstId(IF_INDEX),
                        SnmpUtils.getValueFactory().getOctetString("uplink".getBytes())));
        assertThat(snmp.collection.getIfAliasResourceType(snmp.agent).getResources(), Matchers.hasSize(1));

        // The mapper does not publish aliased resources, so only the interface remains.
        final CollectionSetProtos.CollectionSetResource resource = snmp.mapToInterfaceResource();
        assertEquals(IF_INDEX, resource.getInterface().getIfIndex());
        assertEquals(IF_HC_IN_OCTETS + "." + IF_INDEX, resource.getNumeric(0).getMetricId());
        assertEquals(List.of("SNMP"), agentProtocols(resource));
    }

    /** Maps one numeric attribute on an interface whose identifier comes from the given supplier. */
    private static CollectionSetProtos.CollectionSet mapWithIdentifier(String ifInstance, Supplier<String> identifier) throws UnknownHostException {
        final CollectionAgent agent = new MockCollectionAgent(1, "test", InetAddress.getLocalHost());
        final CollectionSet real = new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withNumericAttribute(new InterfaceLevelResource(new NodeLevelResource(1), ifInstance), "g", "a", 1, AttributeType.GAUGE)
                .build();
        final CollectionSet collectionSet = Mockito.mock(CollectionSet.class);
        when(collectionSet.getCollectionTimestamp()).thenReturn(real.getCollectionTimestamp());
        Mockito.doAnswer(invocation -> {
            real.visit(new CollectionSetVisitorWrapper(invocation.<CollectionSetVisitor>getArgument(0)) {
                @Override
                public void visitAttribute(CollectionAttribute attribute) {
                    super.visitAttribute(new CollectionAttributeWrapper(attribute) {
                        @Override
                        public String getMetricIdentifier() {
                            return identifier.get();
                        }
                    });
                }
            });
            return null;
        }).when(collectionSet).visit(any(CollectionSetVisitor.class));
        final CollectionSetMapper mapper = new CollectionSetMapper(Mockito.mock(NodeDao.class), Mockito.mock(SessionUtils.class), Mockito.mock(ResourceDao.class));
        return mapper.buildCollectionSetProtos(collectionSet, ServiceParameters.forAgentProtocol("SNMP"));
    }

    @Test
    public void testFailingIdentifierPublishesAttributeWithoutMetricId() throws UnknownHostException {
        final CollectionSetProtos.CollectionSet proto = mapWithIdentifier("25", () -> {
            throw new IllegalStateException("no identifier");
        });
        assertThat(proto.getResourceList(), Matchers.hasSize(1));
        assertEquals("a", proto.getResource(0).getNumeric(0).getName());
        assertEquals("", proto.getResource(0).getNumeric(0).getMetricId());
    }

    @Test
    public void testIdentifierIsNotComputedForUnpublishedResources() throws UnknownHostException {
        final AtomicInteger calls = new AtomicInteger();
        // An interface resource without an instance is not published.
        final CollectionSetProtos.CollectionSet proto = mapWithIdentifier(null, () -> {
            calls.incrementAndGet();
            return "SNMP_.1.3.6.1.2.1.1";
        });
        assertThat(proto.getResourceList(), Matchers.hasSize(0));
        assertEquals(0, calls.get());
    }
}
