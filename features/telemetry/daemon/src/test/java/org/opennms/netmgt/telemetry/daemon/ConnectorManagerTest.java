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
package org.opennms.netmgt.telemetry.daemon;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.rpc.mock.MockEntityScopeProvider;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.dao.api.ServiceTracker;
import org.opennms.netmgt.dao.api.ServiceTracker.BatchServiceListener;
import org.opennms.netmgt.telemetry.config.model.ConnectorConfig;
import org.opennms.netmgt.telemetry.config.model.ConnectorTwinConfig;
import org.opennms.netmgt.telemetry.config.model.PackageConfig;
import org.opennms.netmgt.telemetry.config.model.Parameter;
import org.opennms.netmgt.telemetry.config.model.QueueConfig;
import org.opennms.netmgt.telemetry.config.model.TelemetrydConfig;

public class ConnectorManagerTest {

    private static final String DEFAULT_LOCATION = "Default";
    private static final String SERVICE_NAME = "OpenConfig";
    private static final String LOCATION_A = "loc-a";
    private static final String LOCATION_B = "loc-b";

    private final RecordingTwinPublisher twinPublisher = new RecordingTwinPublisher();
    private final StubServiceTracker serviceTracker = new StubServiceTracker();
    private ConnectorManager connectorManager;
    private TelemetrydConfig telemetrydConfig;

    @Before
    public void setUp() {
        LocationPublisherManager locationPublisherManager = new LocationPublisherManager();
        locationPublisherManager.setTwinPublisher(twinPublisher);

        connectorManager = new ConnectorManager();
        connectorManager.setEntityScopeProvider(new MockEntityScopeProvider());
        connectorManager.setServiceTracker(serviceTracker);
        connectorManager.setOpenConfigTwinPublisher(new OpenConfigTwinPublisherImpl(locationPublisherManager));

        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setName("pkg");
        packageConfig.getParameters().add(new Parameter("port", "50052"));
        QueueConfig queueConfig = new QueueConfig();
        queueConfig.setName(SERVICE_NAME);
        ConnectorConfig connectorConfig = new ConnectorConfig();
        connectorConfig.setName("connector");
        connectorConfig.setServiceName(SERVICE_NAME);
        connectorConfig.setQueue(queueConfig);
        connectorConfig.getPackages().add(packageConfig);
        telemetrydConfig = new TelemetrydConfig();
        telemetrydConfig.getConnectors().add(connectorConfig);
    }

    @Test
    public void startPublishesOncePerLocation() {
        serviceTracker.initialServices.addAll(services(LOCATION_A, 0, 1000));
        serviceTracker.initialServices.addAll(services(LOCATION_B, 1000, 10));

        connectorManager.start(telemetrydConfig);

        Assert.assertEquals(1, twinPublisher.published(LOCATION_A).size());
        Assert.assertEquals(1000, twinPublisher.last(LOCATION_A).getConfigurations().size());
        Assert.assertEquals(1, twinPublisher.published(LOCATION_B).size());
        Assert.assertEquals(10, twinPublisher.last(LOCATION_B).getConfigurations().size());
        Assert.assertEquals(SERVICE_NAME, twinPublisher.last(LOCATION_A).getQueueName());
        Assert.assertEquals(HashMap.class,
                twinPublisher.last(LOCATION_A).getConfigurations().get(0).getParameters().get(0).getClass());
    }

    @Test
    public void laterChangesArePublishedOncePerChange() {
        serviceTracker.initialServices.addAll(services(LOCATION_A, 0, 100));
        connectorManager.start(telemetrydConfig);

        serviceTracker.listener.onServicesChanged(new LinkedHashSet<>(services(LOCATION_A, 100, 50)),
                new LinkedHashSet<>(services(LOCATION_A, 0, 20)));

        Assert.assertEquals(2, twinPublisher.published(LOCATION_A).size());
        Assert.assertEquals(130, twinPublisher.last(LOCATION_A).getConfigurations().size());
    }

    @Test
    public void alreadyMatchedServiceIsNotPublishedAgain() {
        serviceTracker.initialServices.addAll(services(LOCATION_A, 0, 5));
        connectorManager.start(telemetrydConfig);

        serviceTracker.listener.onServiceMatched(services(LOCATION_A, 0, 1).get(0));

        Assert.assertEquals(1, twinPublisher.published(LOCATION_A).size());
    }

    @Test
    public void publishFailingWithIOExceptionIsSentWithNextChange() {
        assertFailedPublishIsSentWithNextChange(new IOException("publish failure"));
    }

    @Test
    public void publishFailingWithRuntimeExceptionIsSentWithNextChange() {
        assertFailedPublishIsSentWithNextChange(new IllegalArgumentException("publish failure"));
    }

    private void assertFailedPublishIsSentWithNextChange(Exception publishFailure) {
        twinPublisher.failures.put(LOCATION_A, publishFailure);
        serviceTracker.initialServices.addAll(services(LOCATION_A, 0, 5));
        connectorManager.start(telemetrydConfig);
        Assert.assertEquals(0, twinPublisher.published(LOCATION_A).size());

        serviceTracker.listener.onServicesChanged(new LinkedHashSet<>(services(LOCATION_A, 5, 1)), Collections.emptySet());

        Assert.assertEquals(1, twinPublisher.published(LOCATION_A).size());
        Assert.assertEquals(6, twinPublisher.last(LOCATION_A).getConfigurations().size());
    }

    @Test
    public void failedRemovalIsSentWithNextChange() {
        serviceTracker.initialServices.addAll(services(LOCATION_A, 0, 3));
        connectorManager.start(telemetrydConfig);

        twinPublisher.failures.put(LOCATION_A, new IOException("publish failure"));
        serviceTracker.listener.onServicesChanged(Collections.emptySet(),
                Collections.singleton(services(LOCATION_A, 0, 1).get(0)));
        serviceTracker.listener.onServicesChanged(Collections.emptySet(),
                Collections.singleton(services(LOCATION_A, 1, 1).get(0)));

        Assert.assertEquals(2, twinPublisher.published(LOCATION_A).size());
        Assert.assertEquals(1, twinPublisher.last(LOCATION_A).getConfigurations().size());
    }

    @Test
    public void removalIsPublishedWhileAnotherLocationFailed() {
        final ServiceRef atA = services(LOCATION_A, 0, 1).get(0);
        serviceTracker.initialServices.add(atA);
        serviceTracker.initialServices.addAll(services(LOCATION_B, 1, 1));
        twinPublisher.failures.put(LOCATION_B, new IOException("publish failure"));
        connectorManager.start(telemetrydConfig);

        serviceTracker.listener.onServicesChanged(new LinkedHashSet<>(services(LOCATION_B, 2, 1)), Collections.singleton(atA));

        Assert.assertEquals(0, twinPublisher.last(LOCATION_A).getConfigurations().size());
        Assert.assertEquals(1, twinPublisher.published(LOCATION_B).size());
        Assert.assertEquals(2, twinPublisher.last(LOCATION_B).getConfigurations().size());
    }

    @Test
    public void failedConfigBuildDoesNotBlockOtherServices() {
        connectorManager.setEntityScopeProvider(scopeProviderFailingOnce());
        serviceTracker.initialServices.addAll(services(LOCATION_A, 0, 3));
        connectorManager.start(telemetrydConfig);

        Assert.assertEquals(1, twinPublisher.published(LOCATION_A).size());
        Assert.assertEquals(2, twinPublisher.last(LOCATION_A).getConfigurations().size());
    }

    @Test
    public void serviceMovedToAnotherLocationIsPublishedToBoth() {
        final ServiceRef atA = services(LOCATION_A, 0, 1).get(0);
        final ServiceRef atB = new ServiceRef(atA.getNodeId(), atA.getIpAddress(), SERVICE_NAME, LOCATION_B);
        serviceTracker.initialServices.add(atA);
        connectorManager.start(telemetrydConfig);

        serviceTracker.listener.onServicesChanged(Collections.singleton(atB), Collections.singleton(atA));

        Assert.assertEquals(0, twinPublisher.last(LOCATION_A).getConfigurations().size());
        Assert.assertEquals(1, twinPublisher.published(LOCATION_B).size());
        Assert.assertEquals(1, twinPublisher.last(LOCATION_B).getConfigurations().size());
    }

    @Test
    public void restartPublishesAllServicesAgain() {
        serviceTracker.initialServices.addAll(services(LOCATION_A, 0, 5));
        connectorManager.start(telemetrydConfig);
        connectorManager.stop();

        connectorManager.start(telemetrydConfig);

        Assert.assertEquals(2, twinPublisher.published(LOCATION_A).size());
        Assert.assertEquals(5, twinPublisher.last(LOCATION_A).getConfigurations().size());
    }

    @Test
    public void testParamsByGroup() {

        PackageConfig connectorPackage = new PackageConfig();
        connectorPackage.getParameters().add(new Parameter("port", "50052"));
        connectorPackage.getParameters().add(new Parameter("group1","paths", "/interfaces"));
        connectorPackage.getParameters().add(new Parameter("group1", "frequency", "5000"));
        connectorPackage.getParameters().add(new Parameter("group2", "frequency", "3000"));
        connectorPackage.getParameters().add(new Parameter("group2", "paths",
                "/network-instances/network-instance[instance-name='master']"));
        connectorPackage.getParameters().add(new Parameter("group3", "paths", "/protocols/protocol/bgp"));
        connectorPackage.getParameters().add(new Parameter("group3", "frequency", "4000"));

        ServiceRef serviceRef = new ServiceRef(1, InetAddressUtils.ONE_TWENTY_SEVEN, "OPENCONFIG",DEFAULT_LOCATION);
        List<Map<String, String>> groupedParams = connectorManager.getGroupedParams(connectorPackage, serviceRef);
        Assert.assertEquals(4, groupedParams.size());
        // Each map should either belongs to different group which has paths or it should be a group with port ( which is global)
        for(Map<String, String> parms : groupedParams) {
            Assert.assertTrue(parms.containsKey("paths") || parms.containsKey("port"));
        }

    }

    private static EntityScopeProvider scopeProviderFailingOnce() {
        final EntityScopeProvider delegate = new MockEntityScopeProvider();
        final EntityScopeProvider provider = mock(EntityScopeProvider.class, delegatesTo(delegate));
        doThrow(new IllegalStateException("scope failure")).doAnswer(delegatesTo(delegate))
                .when(provider).getScopeForNode(anyInt());
        return provider;
    }

    private static List<ServiceRef> services(String location, int firstNodeId, int count) {
        List<ServiceRef> services = new ArrayList<>();
        for (int nodeId = firstNodeId; nodeId < firstNodeId + count; nodeId++) {
            services.add(new ServiceRef(nodeId,
                    InetAddressUtils.addr("10.0." + (nodeId / 256) + "." + (nodeId % 256)), SERVICE_NAME, location));
        }
        return services;
    }

    private static class StubServiceTracker implements ServiceTracker {
        private final Set<ServiceRef> initialServices = new LinkedHashSet<>();
        private BatchServiceListener listener;

        @Override
        public Closeable trackServiceMatchingFilterRule(String serviceName, String filterRule, ServiceListener listener) {
            this.listener = (BatchServiceListener) listener;
            this.listener.onServicesChanged(initialServices, Collections.emptySet());
            return () -> this.listener = null;
        }

        @Override
        public Closeable trackService(String serviceName, ServiceListener listener) {
            return trackServiceMatchingFilterRule(serviceName, null, listener);
        }
    }

    private static class RecordingTwinPublisher implements TwinPublisher {
        private final Map<String, List<ConnectorTwinConfig>> publishedByLocation = new HashMap<>();
        private final Map<String, Exception> failures = new HashMap<>();

        @Override
        public <T> Session<T> register(String key, Class<T> clazz, String location) {
            return new Session<T>() {
                @Override
                public void publish(T obj) throws IOException {
                    final Exception failure = failures.remove(location);
                    if (failure != null) {
                        if (failure instanceof IOException) {
                            throw (IOException) failure;
                        }
                        throw (RuntimeException) failure;
                    }
                    publishedByLocation.computeIfAbsent(location, l -> new ArrayList<>()).add((ConnectorTwinConfig) obj);
                }

                @Override
                public void close() {
                }
            };
        }

        @Override
        public void close() {
        }

        private List<ConnectorTwinConfig> published(String location) {
            return publishedByLocation.getOrDefault(location, Collections.emptyList());
        }

        private ConnectorTwinConfig last(String location) {
            List<ConnectorTwinConfig> published = published(location);
            return published.get(published.size() - 1);
        }
    }
}
