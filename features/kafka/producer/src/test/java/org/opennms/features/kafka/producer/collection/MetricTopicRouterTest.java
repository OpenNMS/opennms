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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.MapScope;
import org.opennms.core.mate.api.Scope;
import org.opennms.netmgt.collection.api.AttributeType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionSetVisitor;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.api.LatencyCollectionResource;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.collection.support.builder.InterfaceLevelResource;
import org.opennms.netmgt.collection.support.builder.LatencyTypeResource;
import org.opennms.netmgt.collection.support.builder.NodeLevelResource;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.core.collection.test.MockCollectionAgent;

import com.codahale.metrics.MetricRegistry;

public class MetricTopicRouterTest {

    private static final String DEFAULT_TOPIC = "metrics";
    private static final ContextKey CONTEXT_KEY = new ContextKey("requisition", "metricRouting.package");

    /** What every path other than collectd passes: no collecting service. */
    private static final ServiceParameters EMPTY_PARAMS = new ServiceParameters(Collections.emptyMap());

    /** What collectd passes: CollectionSpecification puts the service name under SERVICE. */
    private static ServiceParameters snmpParams() {
        return new ServiceParameters(Map.of(
                ServiceParameters.ParameterName.SERVICE.toString(), "SNMP"));
    }

    private EntityScopeProvider entityScopeProvider;
    private SessionUtils sessionUtils;
    private MetricRegistry metricRegistry;

    @Before
    public void setUp() {
        entityScopeProvider = mock(EntityScopeProvider.class);
        metricRegistry = new MetricRegistry();

        sessionUtils = mock(SessionUtils.class);
        when(sessionUtils.withReadOnlyTransaction(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArgument(0)).get());

        // Default: nothing carries the routing meta-data
        when(entityScopeProvider.getScopeForNode(anyInt())).thenReturn(EmptyScope.EMPTY);
        when(entityScopeProvider.getScopeForInterface(anyInt(), anyString())).thenReturn(EmptyScope.EMPTY);
        when(entityScopeProvider.getScopeForInterfaceByIfIndex(anyInt(), anyInt())).thenReturn(EmptyScope.EMPTY);
        when(entityScopeProvider.getScopeForInterfaceByIfName(anyInt(), anyString())).thenReturn(EmptyScope.EMPTY);
        when(entityScopeProvider.getScopeForService(anyInt(), any(InetAddress.class), anyString()))
                .thenReturn(EmptyScope.EMPTY);
    }

    private MetricTopicRouter router(final boolean enabled) {
        return new MetricTopicRouter(entityScopeProvider, sessionUtils, metricRegistry, DEFAULT_TOPIC,
                CONTEXT_KEY, enabled, 300000L);
    }

    private static Scope scopeWith(final Scope.ScopeName scopeName, final String value) {
        return new MapScope(scopeName, Map.of(CONTEXT_KEY, value));
    }

    private long counter(final String name) {
        return metricRegistry.counter(name).getCount();
    }

    // --- enable flag ------------------------------------------------------

    @Test
    public void disabledRouterAlwaysReturnsTheDefaultTopicAndTouchesNothing() {
        final MetricTopicRouter router = router(false);
        assertFalse(router.isEnabled());

        assertEquals(DEFAULT_TOPIC, router.resolve(new MetricTopicRouter.RoutingKey(1, null, null)).topic);
        assertEquals(MetricTopicRouter.Status.DISABLED,
                router.resolve(new MetricTopicRouter.RoutingKey(1, null, null)).status);
        verify(entityScopeProvider, never()).getScopeForNode(anyInt());
    }

    @Test
    public void routerIsDisabledWhenItsCollaboratorsAreMissing() {
        assertFalse(new MetricTopicRouter(null, sessionUtils, metricRegistry, DEFAULT_TOPIC, CONTEXT_KEY,
                true, 300000L).isEnabled());
        assertFalse(new MetricTopicRouter(entityScopeProvider, null, metricRegistry, DEFAULT_TOPIC, CONTEXT_KEY,
                true, 300000L).isEnabled());
        // A null context key is how the activator signals that the configured value was unusable
        assertFalse(new MetricTopicRouter(entityScopeProvider, sessionUtils, metricRegistry, DEFAULT_TOPIC, null,
                true, 300000L).isEnabled());
    }

    // --- precedence -------------------------------------------------------

    @Test
    public void serviceMetaDataWinsOverInterfaceAndNode() {
        when(entityScopeProvider.getScopeForNode(1)).thenReturn(scopeWith(Scope.ScopeName.NODE, "from-node"));
        when(entityScopeProvider.getScopeForInterface(1, "10.0.0.1"))
                .thenReturn(scopeWith(Scope.ScopeName.INTERFACE, "from-interface"));
        when(entityScopeProvider.getScopeForService(eq(1), any(InetAddress.class), eq("ICMP")))
                .thenReturn(scopeWith(Scope.ScopeName.SERVICE, "from-service"));

        final MetricTopicRouter.Resolution resolution = router(true)
                .resolve(new MetricTopicRouter.RoutingKey(1, "10.0.0.1", "ICMP"));
        assertEquals("from-service", resolution.topic);
        assertEquals(Scope.ScopeName.SERVICE, resolution.scopeName);
        assertEquals(MetricTopicRouter.Status.ROUTED, resolution.status);
    }

    @Test
    public void interfaceMetaDataWinsOverNode() {
        when(entityScopeProvider.getScopeForNode(1)).thenReturn(scopeWith(Scope.ScopeName.NODE, "from-node"));
        when(entityScopeProvider.getScopeForInterface(1, "10.0.0.1"))
                .thenReturn(scopeWith(Scope.ScopeName.INTERFACE, "from-interface"));

        final MetricTopicRouter.Resolution resolution = router(true)
                .resolve(new MetricTopicRouter.RoutingKey(1, "10.0.0.1", null));
        assertEquals("from-interface", resolution.topic);
        assertEquals(Scope.ScopeName.INTERFACE, resolution.scopeName);
    }

    @Test
    public void nodeMetaDataIsUsedWhenNothingMoreSpecificApplies() {
        when(entityScopeProvider.getScopeForNode(1)).thenReturn(scopeWith(Scope.ScopeName.NODE, "from-node"));

        final MetricTopicRouter.Resolution resolution = router(true)
                .resolve(new MetricTopicRouter.RoutingKey(1, "10.0.0.1", "SNMP"));
        assertEquals("from-node", resolution.topic);
        assertEquals(Scope.ScopeName.NODE, resolution.scopeName);
    }

    // --- scope selection --------------------------------------------------

    @Test
    public void theServiceScopeIsOnlyConsultedWhenBothTheAddressAndServiceAreKnown() {
        final MetricTopicRouter router = router(true);

        router.resolve(new MetricTopicRouter.RoutingKey(1, "10.0.0.1", null));
        verify(entityScopeProvider, never()).getScopeForService(anyInt(), any(InetAddress.class), anyString());

        router.resolve(new MetricTopicRouter.RoutingKey(1, "10.0.0.1", "ICMP"));
        verify(entityScopeProvider).getScopeForService(eq(1), any(InetAddress.class), eq("ICMP"));
    }

    @Test
    public void anUnresolvedNodeFallsBackWithoutAnyLookup() {
        final MetricTopicRouter router = router(true);
        final MetricTopicRouter.RoutingKey key = new MetricTopicRouter.RoutingKey(0, null, null);

        assertEquals(DEFAULT_TOPIC, router.resolveTopic(key));
        assertEquals(MetricTopicRouter.Status.UNRESOLVED_NODE, router.resolve(key).status);
        assertEquals(1, counter(MetricTopicRouter.METRIC_UNRESOLVED_NODE));
        verify(entityScopeProvider, never()).getScopeForNode(anyInt());
    }

    // --- unmapped / sanitization -----------------------------------------

    @Test
    public void aMissingMetaDataKeyFallsBackToTheDefaultTopic() {
        final MetricTopicRouter router = router(true);
        final MetricTopicRouter.RoutingKey key = new MetricTopicRouter.RoutingKey(1, null, null);
        final MetricTopicRouter.Resolution resolution = router.resolve(key);

        assertEquals(DEFAULT_TOPIC, resolution.topic);
        assertEquals(MetricTopicRouter.Status.UNMAPPED, resolution.status);
        assertNull(resolution.value);

        router.resolveTopic(key);
        assertEquals(1, counter(MetricTopicRouter.METRIC_UNMAPPED));
    }

    @Test
    public void aBlankMetaDataValueCountsAsUnmappedRatherThanRejected() {
        when(entityScopeProvider.getScopeForNode(1)).thenReturn(scopeWith(Scope.ScopeName.NODE, "   "));

        final MetricTopicRouter router = router(true);
        final MetricTopicRouter.RoutingKey key = new MetricTopicRouter.RoutingKey(1, null, null);

        assertEquals(DEFAULT_TOPIC, router.resolveTopic(key));
        assertEquals(MetricTopicRouter.Status.UNMAPPED, router.resolve(key).status);
        assertEquals(1, counter(MetricTopicRouter.METRIC_UNMAPPED));
        assertEquals(0, counter(MetricTopicRouter.METRIC_SANITIZE_REJECTED));
    }

    @Test
    public void aSurroundingWhitespaceIsTrimmedFromAnOtherwiseValidValue() {
        when(entityScopeProvider.getScopeForNode(1)).thenReturn(scopeWith(Scope.ScopeName.NODE, "  foobar  "));

        assertEquals("foobar", router(true)
                .resolve(new MetricTopicRouter.RoutingKey(1, null, null)).topic);
    }

    @Test
    public void legalTopicNamesAreAccepted() {
        assertTrue(MetricTopicRouter.isLegalTopicName("foobar"));
        assertTrue(MetricTopicRouter.isLegalTopicName("tenant-a"));
        assertTrue(MetricTopicRouter.isLegalTopicName("tenant_a"));
        assertTrue(MetricTopicRouter.isLegalTopicName("tenant.a"));
        assertTrue(MetricTopicRouter.isLegalTopicName("A1"));
        assertTrue(MetricTopicRouter.isLegalTopicName("a".repeat(MetricTopicRouter.MAX_TOPIC_LENGTH)));
    }

    @Test
    public void illegalTopicNamesAreRejected() {
        assertFalse(MetricTopicRouter.isLegalTopicName(null));
        assertFalse(MetricTopicRouter.isLegalTopicName(""));
        assertFalse(MetricTopicRouter.isLegalTopicName("."));
        assertFalse(MetricTopicRouter.isLegalTopicName(".."));
        assertFalse(MetricTopicRouter.isLegalTopicName("a b"));
        assertFalse(MetricTopicRouter.isLegalTopicName("a/b"));
        assertFalse(MetricTopicRouter.isLegalTopicName("a:b"));
        assertFalse(MetricTopicRouter.isLegalTopicName("${requisition:foo}"));
        assertFalse(MetricTopicRouter.isLegalTopicName("umlaut-ä"));
        assertFalse(MetricTopicRouter.isLegalTopicName("a".repeat(MetricTopicRouter.MAX_TOPIC_LENGTH + 1)));
    }

    @Test
    public void anIllegalMetaDataValueIsRejectedAndFallsBackToTheDefaultTopic() {
        when(entityScopeProvider.getScopeForNode(1)).thenReturn(scopeWith(Scope.ScopeName.NODE, "not a topic"));

        final MetricTopicRouter router = router(true);
        final MetricTopicRouter.RoutingKey key = new MetricTopicRouter.RoutingKey(1, null, null);
        final MetricTopicRouter.Resolution resolution = router.resolve(key);

        assertEquals(DEFAULT_TOPIC, resolution.topic);
        assertEquals(MetricTopicRouter.Status.SANITIZE_REJECTED, resolution.status);
        assertEquals("not a topic", resolution.value);

        router.resolveTopic(key);
        assertEquals(1, counter(MetricTopicRouter.METRIC_SANITIZE_REJECTED));
    }

    // --- caching ----------------------------------------------------------

    @Test
    public void repeatedResolutionsForTheSameResourceHitTheCache() throws Exception {
        when(entityScopeProvider.getScopeForNode(1)).thenReturn(scopeWith(Scope.ScopeName.NODE, "foobar"));
        final MetricTopicRouter router = router(true);
        final CollectionResource resource = nodeResource(1);

        assertEquals("foobar", router.resolveTopic(resource, 1, EMPTY_PARAMS));
        assertEquals("foobar", router.resolveTopic(resource, 1, EMPTY_PARAMS));
        assertEquals("foobar", router.resolveTopic(resource, 1, EMPTY_PARAMS));
        verify(entityScopeProvider, times(1)).getScopeForNode(1);

        router.invalidateCache();
        assertEquals("foobar", router.resolveTopic(resource, 1, EMPTY_PARAMS));
        verify(entityScopeProvider, times(2)).getScopeForNode(1);
    }

    // --- routing key derivation ------------------------------------------

    @Test
    public void aLatencyResourceYieldsItsAddressAndServiceName() throws Exception {
        final MetricTopicRouter router = router(true);

        // A real LatencyCollectionResource exposes both directly
        final MetricTopicRouter.RoutingKey fromLatencyResource = router.routingKeyFor(
                new LatencyCollectionResource("ICMP", "10.0.0.1", "Default"), 1, EMPTY_PARAMS);
        assertEquals("10.0.0.1", fromLatencyResource.ipAddress);
        assertEquals("ICMP", fromLatencyResource.serviceName);

        // Builder-created latency resources - which is what arrives from a Minion - are plain
        // AbstractCollectionResources, so the service name has to come from the instance string.
        final MetricTopicRouter.RoutingKey fromBuilderResource =
                router.routingKeyFor(latencyResource("ICMP", "10.0.0.1"), 1, EMPTY_PARAMS);
        assertEquals("10.0.0.1", fromBuilderResource.ipAddress);
        assertEquals("ICMP", fromBuilderResource.serviceName);
    }

    @Test
    public void aNodeResourceYieldsTheCollectingInterfaceAndService() throws Exception {
        // The agent address is the interface the collecting service is assigned to, and collectd
        // puts the service name into the ServiceParameters.
        final MetricTopicRouter.RoutingKey key =
                router(true).routingKeyFor(nodeResource(1), 1, snmpParams());
        assertEquals(1, key.nodeId);
        assertEquals("10.0.0.1", key.ipAddress);
        assertEquals("SNMP", key.serviceName);
    }

    @Test
    public void withoutAnyServiceParametersThereIsNoCollectingService() throws Exception {
        // Streaming telemetry, flows and integration-API collections are not triggered by a
        // monitored service, and pass empty parameters.
        final MetricTopicRouter.RoutingKey key =
                router(true).routingKeyFor(nodeResource(1), 1, EMPTY_PARAMS);
        assertEquals("10.0.0.1", key.ipAddress);
        assertNull(key.serviceName);
    }

    @Test
    public void anInterfaceResourceIsRoutedByItsCollectionRatherThanTheInterfaceItDescribes() throws Exception {
        // The ifIndex the resource reports is deliberately not part of the routing key, so every
        // resource of a collection resolves identically.
        final MetricTopicRouter router = router(true);

        final MetricTopicRouter.RoutingKey fromInterfaceResource =
                router.routingKeyFor(interfaceResource(1, "25"), 1, snmpParams());
        final MetricTopicRouter.RoutingKey fromNodeResource =
                router.routingKeyFor(nodeResource(1), 1, snmpParams());

        assertEquals("10.0.0.1", fromInterfaceResource.ipAddress);
        assertEquals("SNMP", fromInterfaceResource.serviceName);
        assertEquals(fromNodeResource, fromInterfaceResource);
    }

    @Test
    public void metaDataOnTheInterfaceAMetricDescribesIsNotConsulted() throws Exception {
        // A tag on the SNMP interface the metrics are about must not override the interface the
        // collecting service is assigned to.
        when(entityScopeProvider.getScopeForInterfaceByIfIndex(anyInt(), anyInt()))
                .thenReturn(scopeWith(Scope.ScopeName.INTERFACE, "from-described-interface"));
        when(entityScopeProvider.getScopeForInterfaceByIfName(anyInt(), anyString()))
                .thenReturn(scopeWith(Scope.ScopeName.INTERFACE, "from-described-interface"));
        when(entityScopeProvider.getScopeForInterface(1, "10.0.0.1"))
                .thenReturn(scopeWith(Scope.ScopeName.INTERFACE, "from-collecting-interface"));

        final MetricTopicRouter router = router(true);
        assertEquals("from-collecting-interface",
                router.resolveTopic(interfaceResource(1, "25"), 1, snmpParams()));

        verify(entityScopeProvider, never()).getScopeForInterfaceByIfIndex(anyInt(), anyInt());
        verify(entityScopeProvider, never()).getScopeForInterfaceByIfName(anyInt(), anyString());
    }

    @Test
    public void aPerspectiveResponseTimeInstanceYieldsItsAddressAndService() throws Exception {
        // PerspectiveResponseTimeResource reports RESOURCE_TYPE_IF with an
        // 'address[service]@location' instance, and the perspective poller passes empty
        // parameters, so the instance is the only source of the service name.
        final MetricTopicRouter.RoutingKey key = router(true)
                .routingKeyFor(interfaceResource(1, "10.0.0.9[ICMP]@Raleigh"), 1, EMPTY_PARAMS);

        assertEquals("10.0.0.9", key.ipAddress);
        assertEquals("ICMP", key.serviceName);
    }

    @Test
    public void aMissingOwnerAddressIsToleratedRatherThanThrowing() {
        final CollectionResource resource = mock(CollectionResource.class);
        when(resource.getResourceTypeName()).thenReturn(CollectionResource.RESOURCE_TYPE_NODE);
        when(resource.getOwnerName()).thenReturn(null);

        final MetricTopicRouter.RoutingKey key = router(true).routingKeyFor(resource, 1, EMPTY_PARAMS);
        assertNull(key.ipAddress);
        assertNull(key.serviceName);
    }

    @Test
    public void aMalformedLatencyInstanceIsIgnoredRatherThanThrowing() {
        assertNull(MetricTopicRouter.parseResponseTimeInstance(null));
        assertNull(MetricTopicRouter.parseResponseTimeInstance("10.0.0.1"));
        assertNull(MetricTopicRouter.parseResponseTimeInstance("10.0.0.1[ICMP"));
        assertNull(MetricTopicRouter.parseResponseTimeInstance("[ICMP]"));
        assertNull(MetricTopicRouter.parseResponseTimeInstance("10.0.0.1[]"));
    }

    // --- helpers ----------------------------------------------------------

    private static CollectionResource nodeResource(final int nodeId) throws Exception {
        return firstResourceOf(new CollectionSetBuilder(agent(nodeId))
                .withTimestamp(new Date(0))
                .withGauge(new NodeLevelResource(nodeId), "group", "name", 1.0)
                .build());
    }

    private static CollectionResource interfaceResource(final int nodeId, final String instance) throws Exception {
        return firstResourceOf(new CollectionSetBuilder(agent(nodeId))
                .withTimestamp(new Date(0))
                .withNumericAttribute(new InterfaceLevelResource(new NodeLevelResource(nodeId), instance),
                        "group", "name", 1.0, AttributeType.GAUGE)
                .build());
    }

    private static CollectionResource latencyResource(final String service, final String ipAddress) throws Exception {
        return firstResourceOf(new CollectionSetBuilder(agent(1))
                .withTimestamp(new Date(0))
                .withGauge(new LatencyTypeResource(service, ipAddress, "Default"), "group", "name", 1.0)
                .build());
    }

    private static CollectionAgent agent(final int nodeId) throws Exception {
        return new MockCollectionAgent(nodeId, "test", InetAddress.getByName("10.0.0.1"));
    }

    /** Runs the visitor just far enough to get hold of the built CollectionResource. */
    private static CollectionResource firstResourceOf(final CollectionSet collectionSet) {
        final CollectionResource[] holder = new CollectionResource[1];
        collectionSet.visit(new CollectionSetVisitor() {
            @Override
            public void visitResource(final CollectionResource resource) {
                if (holder[0] == null) {
                    holder[0] = resource;
                }
            }

            @Override
            public void visitCollectionSet(final CollectionSet set) {
            }

            @Override
            public void visitGroup(final org.opennms.netmgt.collection.api.AttributeGroup group) {
            }

            @Override
            public void visitAttribute(final org.opennms.netmgt.collection.api.CollectionAttribute attribute) {
            }

            @Override
            public void completeAttribute(final org.opennms.netmgt.collection.api.CollectionAttribute attribute) {
            }

            @Override
            public void completeGroup(final org.opennms.netmgt.collection.api.AttributeGroup group) {
            }

            @Override
            public void completeResource(final CollectionResource resource) {
            }

            @Override
            public void completeCollectionSet(final CollectionSet set) {
            }
        });
        return holder[0];
    }

    @Test
    public void previewingAResolutionLeavesTheCountersAlone() {
        final MetricTopicRouter router = router(true);
        final MetricTopicRouter.RoutingKey key = new MetricTopicRouter.RoutingKey(1, null, null);

        // The shell command previews with resolve(), which must not disturb what it reports on
        assertEquals(MetricTopicRouter.Status.UNMAPPED, router.resolve(key).status);
        assertEquals(MetricTopicRouter.Status.UNMAPPED, router.resolve(key).status);
        assertTrue(metricRegistry.getCounters().isEmpty());

        // ...whereas the real path counts every resource
        router.resolveTopic(key);
        assertEquals(1, counter(MetricTopicRouter.METRIC_UNMAPPED));
    }

    // --- the collecting service and interface ----------------------------

    @Test
    public void aServiceTagRoutesTheWholeCollection() {
        // The user-facing point of this: tagging the SNMP service moves everything that SNMP
        // collection produces, including node level resources.
        when(entityScopeProvider.getScopeForService(eq(1), any(InetAddress.class), eq("SNMP")))
                .thenReturn(scopeWith(Scope.ScopeName.SERVICE, "from-service"));

        final MetricTopicRouter router = router(true);

        // Every resource of the collection shares the key, so all of them route here.
        assertEquals("from-service",
                router.resolve(new MetricTopicRouter.RoutingKey(1, "10.0.0.1", "SNMP")).topic);
    }

    @Test
    public void theCollectingInterfaceRoutesTheWholeCollection() {
        // Tagging the interface a service is assigned to routes every resource of that
        // collection, not only the resources about that interface.
        when(entityScopeProvider.getScopeForInterface(1, "10.0.0.1"))
                .thenReturn(scopeWith(Scope.ScopeName.INTERFACE, "from-collecting-interface"));

        final MetricTopicRouter router = router(true);

        assertEquals("from-collecting-interface",
                router.resolve(new MetricTopicRouter.RoutingKey(1, "10.0.0.1", "SNMP")).topic);
    }

    @Test
    public void theCollectingServiceBeatsTheInterfaceAndTheNode() {
        // The full chain: service > interface > node > default topic.
        when(entityScopeProvider.getScopeForNode(1)).thenReturn(scopeWith(Scope.ScopeName.NODE, "from-node"));
        when(entityScopeProvider.getScopeForInterface(1, "10.0.0.1"))
                .thenReturn(scopeWith(Scope.ScopeName.INTERFACE, "from-interface"));
        when(entityScopeProvider.getScopeForService(eq(1), any(InetAddress.class), eq("SNMP")))
                .thenReturn(scopeWith(Scope.ScopeName.SERVICE, "from-service"));

        final MetricTopicRouter.Resolution resolution = router(true)
                .resolve(new MetricTopicRouter.RoutingKey(1, "10.0.0.1", "SNMP"));
        assertEquals("from-service", resolution.topic);
        assertEquals(Scope.ScopeName.SERVICE, resolution.scopeName);
    }

    @Test
    public void withoutACollectingServiceOnlyTheInterfaceAndNodeApply() {
        when(entityScopeProvider.getScopeForNode(1)).thenReturn(scopeWith(Scope.ScopeName.NODE, "from-node"));
        final MetricTopicRouter router = router(true);

        assertEquals("from-node",
                router.resolve(new MetricTopicRouter.RoutingKey(1, "10.0.0.1", null)).topic);
        verify(entityScopeProvider, never()).getScopeForService(anyInt(), any(InetAddress.class), anyString());
    }

    private static ServiceParameters serviceParams(String serviceName) {
        return new ServiceParameters(Map.of(
                ServiceParameters.ParameterName.SERVICE.toString(), serviceName));
    }
}
