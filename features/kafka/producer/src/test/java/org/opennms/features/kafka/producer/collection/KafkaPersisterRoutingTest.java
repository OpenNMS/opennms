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
package org.opennms.features.kafka.producer.collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.MockProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.features.kafka.producer.model.CollectionSetProtos;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

import static org.junit.Assert.assertThat;

/**
 * Exercises the routing behaviour of {@link KafkaPersister} against a {@link MockProducer}, so the
 * topic each record lands on can be asserted without an embedded broker.
 */
public class KafkaPersisterRoutingTest {

    private static final String DEFAULT_TOPIC = "test-metrics";

    private MockProducer<String, byte[]> producer;
    private CollectionSetMapper collectionSetMapper;
    private MetricRegistry metricRegistry;

    @Before
    public void setUp() {
        producer = new MockProducer<>(true, new StringSerializer(), new ByteArraySerializer());
        metricRegistry = new MetricRegistry();

        final NodeDao nodeDao = Mockito.mock(NodeDao.class);
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.setLabel("TestNode");
        when(nodeDao.get("1")).thenReturn(node);

        final SessionUtils sessionUtils = Mockito.mock(SessionUtils.class);
        when(sessionUtils.withReadOnlyTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());

        collectionSetMapper = new CollectionSetMapper(nodeDao, sessionUtils, Mockito.mock(ResourceDao.class));
    }

    private KafkaPersister persister(final MetricTopicRouter router) {
        final KafkaPersister persister = new KafkaPersister(new ServiceParameters(Collections.emptyMap()));
        persister.setCollectionSetMapper(collectionSetMapper);
        persister.setProducer(producer);
        persister.setTopicName(DEFAULT_TOPIC);
        persister.setMetricTopicRouter(router);
        return persister;
    }

    /** A router that routes interface resources to {@code routedTopic} and everything else to the default. */
    private MetricTopicRouter routerRoutingInterfacesTo(final String routedTopic) {
        final MetricTopicRouter router = Mockito.mock(MetricTopicRouter.class);
        when(router.isEnabled()).thenReturn(true);
        when(router.getMetricRegistry()).thenReturn(metricRegistry);
        when(router.resolveTopic(any(CollectionResource.class), anyInt(), any(ServiceParameters.class)))
                .thenAnswer(invocation -> {
            final CollectionResource resource = invocation.getArgument(0);
            return CollectionResource.RESOURCE_TYPE_IF.equals(resource.getResourceTypeName())
                    ? routedTopic : DEFAULT_TOPIC;
        });
        return router;
    }

    @Test
    public void aNullRouterSendsEverythingToTheConfiguredTopic() throws UnknownHostException {
        persister(null).visitCollectionSet(mixedCollectionSet());

        assertThat(topicsOf(producer.history()), Matchers.contains(DEFAULT_TOPIC));
        assertEquals(1, producer.history().size());
    }

    @Test
    public void aDisabledRouterSendsEverythingToTheConfiguredTopic() throws UnknownHostException {
        final MetricTopicRouter router = Mockito.mock(MetricTopicRouter.class);
        when(router.isEnabled()).thenReturn(false);
        when(router.getMetricRegistry()).thenReturn(metricRegistry);
        // A disabled router resolves everything to the default topic
        when(router.resolveTopic(any(CollectionResource.class), anyInt(), any(ServiceParameters.class)))
                .thenReturn(DEFAULT_TOPIC);

        persister(router).visitCollectionSet(mixedCollectionSet());

        assertThat(topicsOf(producer.history()), Matchers.contains(DEFAULT_TOPIC));
        // No routed counters when routing is off
        assertTrue(metricRegistry.getCounters().isEmpty());
    }

    @Test
    public void aCollectionSetSpanningTopicsIsSplitIntoOneRecordPerTopic()
            throws UnknownHostException, InvalidProtocolBufferException {
        persister(routerRoutingInterfacesTo("routed")).visitCollectionSet(mixedCollectionSet());

        final List<ProducerRecord<String, byte[]>> history = producer.history();
        assertEquals(2, history.size());
        assertThat(topicsOf(history), Matchers.containsInAnyOrder(DEFAULT_TOPIC, "routed"));

        final CollectionSetProtos.CollectionSet routed = parse(recordFor(history, "routed"));
        assertEquals(1, routed.getResourceCount());
        assertTrue(routed.getResource(0).hasInterface());

        final CollectionSetProtos.CollectionSet defaulted = parse(recordFor(history, DEFAULT_TOPIC));
        assertEquals(1, defaulted.getResourceCount());
        assertTrue(defaulted.getResource(0).hasNode());
    }

    @Test
    public void everyGroupKeepsTheNodeIdAsItsRecordKey() throws UnknownHostException {
        persister(routerRoutingInterfacesTo("routed")).visitCollectionSet(mixedCollectionSet());

        // A CollectionSet belongs to a single agent, so splitting it by topic cannot change the
        // key any group derives - both groups stay on the same node.
        for (final ProducerRecord<String, byte[]> record : producer.history()) {
            assertEquals("1", record.key());
        }
    }

    @Test
    public void routedResourcesAreCountedPerTopic() throws UnknownHostException {
        persister(routerRoutingInterfacesTo("routed")).visitCollectionSet(mixedCollectionSet());

        assertEquals(1, metricRegistry.counter(MetricTopicRouter.METRIC_ROUTED_PREFIX + ".routed").getCount());
        assertEquals(1, metricRegistry.counter(MetricTopicRouter.METRIC_ROUTED_PREFIX + "." + DEFAULT_TOPIC).getCount());
    }

    @Test
    public void theMetricFilterIsAppliedWithinEachGroup() throws UnknownHostException {
        final KafkaPersister persister = persister(routerRoutingInterfacesTo("routed"));
        // Keep only interface resources, which all route to 'routed'
        persister.setMetricFilter("ifIndex != null");

        persister.visitCollectionSet(mixedCollectionSet());

        assertThat(topicsOf(producer.history()), Matchers.contains("routed"));
    }

    @Test
    public void aGroupLeftEmptyByTheFilterIsNotSent() throws UnknownHostException {
        final KafkaPersister persister = persister(routerRoutingInterfacesTo("routed"));
        persister.setMetricFilter("nodeId == 999");

        persister.visitCollectionSet(mixedCollectionSet());

        assertTrue(producer.history().isEmpty());
    }

    @Test
    public void theSingleArgumentBisectOverloadStillUsesTheConfiguredTopic() throws UnknownHostException {
        final KafkaPersister persister = persister(null);
        persister.setDisableMetricsSplitting(true);

        persister.bisectAndSendMessageToKafka(protoFor(mixedCollectionSet()));

        assertThat(topicsOf(producer.history()), Matchers.contains(DEFAULT_TOPIC));
    }

    @Test
    public void bisectingPreservesTheRoutedTopic() throws UnknownHostException {
        // Four interface resources all route to 'routed', so splitting happens within one group.
        final CollectionSet collectionSet = interfaceCollectionSet(4);
        final CollectionSetProtos.CollectionSet proto = protoFor(collectionSet);
        // Big enough to split the whole group once, small enough to stop at the halves.
        final int threshold = proto.getSerializedSize() * 3 / 4;

        final KafkaPersister persister = Mockito.spy(persister(routerRoutingInterfacesTo("routed")));
        Mockito.doAnswer(invocation -> (int) invocation.getArgument(0) > threshold)
                .when(persister).checkForMaxSize(anyInt());

        persister.visitCollectionSet(collectionSet);

        assertEquals(2, producer.history().size());
        assertThat(topicsOf(producer.history()), Matchers.contains("routed"));
    }

    // --- helpers ----------------------------------------------------------

    /** A node level and an SNMP interface resource on the same node. */
    private static CollectionSet mixedCollectionSet() throws UnknownHostException {
        final CollectionAgent agent = new MockCollectionAgent(1, "TestNode", InetAddress.getByName("10.0.0.1"));
        final NodeLevelResource nodeResource = new NodeLevelResource(1);
        final InterfaceLevelResource interfaceResource = new InterfaceLevelResource(nodeResource, "25");

        return new CollectionSetBuilder(agent).withTimestamp(new Date(2))
                .withGauge(nodeResource, "group1", "cpu", 1.0)
                .withNumericAttribute(interfaceResource, "group2", "ifInOctets", 105, AttributeType.GAUGE)
                .build();
    }

    private static List<String> topicsOf(final List<ProducerRecord<String, byte[]>> history) {
        return history.stream().map(ProducerRecord::topic).distinct().collect(Collectors.toList());
    }

    private static ProducerRecord<String, byte[]> recordFor(final List<ProducerRecord<String, byte[]>> history,
                                                            final String topic) {
        return history.stream().filter(r -> topic.equals(r.topic())).findFirst()
                .orElseThrow(() -> new AssertionError("no record for topic " + topic));
    }

    private static CollectionSetProtos.CollectionSet parse(final ProducerRecord<String, byte[]> record)
            throws InvalidProtocolBufferException {
        return CollectionSetProtos.CollectionSet.parseFrom(record.value());
    }

    /** {@code n} SNMP interface resources on the same node. */
    private static CollectionSet interfaceCollectionSet(final int n) throws UnknownHostException {
        final CollectionAgent agent = new MockCollectionAgent(1, "TestNode", InetAddress.getByName("10.0.0.1"));
        final NodeLevelResource nodeResource = new NodeLevelResource(1);
        final CollectionSetBuilder builder = new CollectionSetBuilder(agent).withTimestamp(new Date(2));
        for (int i = 1; i <= n; i++) {
            builder.withNumericAttribute(new InterfaceLevelResource(nodeResource, Integer.toString(i)),
                    "group", "ifInOctets", 100 + i, AttributeType.GAUGE);
        }
        return builder.build();
    }

    private CollectionSetProtos.CollectionSet protoFor(final CollectionSet collectionSet) {
        return collectionSetMapper.buildCollectionSetProtos(collectionSet, new ServiceParameters(Collections.emptyMap()));
    }
}
