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
package org.opennms.netmgt.dao.support;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.fail;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.FilterWatcher;
import org.opennms.netmgt.dao.api.ServiceRef;
import org.opennms.netmgt.dao.api.ServiceTracker;

public class DefaultServiceTrackerTest implements ServiceTracker.ServiceListener {
    private  static  final  String DEFAULT_LOCATION = "Default";
    private static final String OPENCONFIG = "OpenConfig";
    private static final String ICMP = "ICMP";

    private DefaultServiceTracker serviceTracker;
    private MockFilterWatcher mockFilterWatcher = new MockFilterWatcher();

    private List<ServiceRef> activeServices = new LinkedList<>();
    private int numServicesAdded;
    private int numServicesRemoved;

    @Before
    public void setUp() {
        serviceTracker = new DefaultServiceTracker();
        serviceTracker.setFilterWatcher(mockFilterWatcher);
    }

    @Test
    public void canTrackService() throws IOException {
        // There are no services in the database, we start a session, we expect no callbacks
        Closeable session = serviceTracker.trackService(OPENCONFIG, this);
        assertThat(activeServices, hasSize(0));
        assertThat(numServicesAdded, equalTo(0));
        assertThat(numServicesRemoved, equalTo(0));

        // We add a service with the corresponding name, we expect a callback
        mockFilterWatcher.addService(1, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG);

        // Verify
        assertThat(activeServices, hasSize(1));
        assertThat(numServicesAdded, equalTo(1));
        assertThat(numServicesRemoved, equalTo(0));

        // We add the same service again, we expect no callbacks
        mockFilterWatcher.addService(1, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG);

        // Verify
        assertThat(activeServices, hasSize(1));
        assertThat(numServicesAdded, equalTo(1));
        assertThat(numServicesRemoved, equalTo(0));

        // We add a service that does not match the tracked name, we expect no callbacks
        mockFilterWatcher.addService(1, InetAddressUtils.ONE_TWENTY_SEVEN, ICMP);

        // Verify
        assertThat(activeServices, hasSize(1));
        assertThat(numServicesAdded, equalTo(1));
        assertThat(numServicesRemoved, equalTo(0));

        // We remove the original service, we expect a callback
        mockFilterWatcher.deleteService(1, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG);

        // Verify
        assertThat(activeServices, hasSize(0));
        assertThat(numServicesAdded, equalTo(1));
        assertThat(numServicesRemoved, equalTo(1));

        // We remove the other service, we expect no callbacks
        mockFilterWatcher.deleteService(1, InetAddressUtils.ONE_TWENTY_SEVEN, ICMP);

        // Verify
        assertThat(activeServices, hasSize(0));
        assertThat(numServicesAdded, equalTo(1));
        assertThat(numServicesRemoved, equalTo(1));

        // Cleanup our session
        session.close();
    }

    @Test
    public void deliversEachFilterChangeAsOneBatch() throws IOException {
        final List<Set<ServiceRef>> matchedBatches = new LinkedList<>();
        final List<Set<ServiceRef>> stoppedBatches = new LinkedList<>();
        Closeable session = serviceTracker.trackService(OPENCONFIG, (ServiceTracker.BatchServiceListener) (matched, stoppedMatching) -> {
            matchedBatches.add(matched);
            stoppedBatches.add(stoppedMatching);
        });

        mockFilterWatcher.addServices(Arrays.asList(
                new ServiceRef(1, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG, DEFAULT_LOCATION),
                new ServiceRef(2, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG, DEFAULT_LOCATION),
                new ServiceRef(3, InetAddressUtils.ONE_TWENTY_SEVEN, ICMP, DEFAULT_LOCATION)));

        assertThat(matchedBatches, hasSize(1));
        assertThat(matchedBatches.get(0), hasSize(2));
        assertThat(stoppedBatches.get(0), hasSize(0));

        // Same results again, no callback
        mockFilterWatcher.refresh();
        assertThat(matchedBatches, hasSize(1));

        mockFilterWatcher.deleteService(1, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG);

        assertThat(matchedBatches, hasSize(2));
        assertThat(matchedBatches.get(1), hasSize(0));
        assertThat(stoppedBatches.get(1), hasSize(1));

        session.close();
    }

    @Test
    public void redeliversBatchAfterListenerFailure() throws IOException {
        final List<Set<ServiceRef>> matchedBatches = new LinkedList<>();
        final boolean[] failNext = {true};
        Closeable session = serviceTracker.trackService(OPENCONFIG, (ServiceTracker.BatchServiceListener) (matched, stoppedMatching) -> {
            matchedBatches.add(matched);
            if (failNext[0]) {
                failNext[0] = false;
                throw new IllegalStateException("listener failure");
            }
        });

        try {
            mockFilterWatcher.addServices(Arrays.asList(
                    new ServiceRef(1, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG, DEFAULT_LOCATION),
                    new ServiceRef(2, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG, DEFAULT_LOCATION)));
            fail("Expected the listener failure to propagate");
        } catch (IllegalStateException expected) {
            // the filter watcher logs callback failures
        }

        mockFilterWatcher.refresh();

        assertThat(matchedBatches, hasSize(2));
        assertThat(matchedBatches.get(1), hasSize(2));

        // Accepted batch is not delivered again
        mockFilterWatcher.refresh();
        assertThat(matchedBatches, hasSize(2));

        session.close();
    }

    @Test
    public void deliversRemainingServicesAfterListenerFailure() throws IOException {
        final List<ServiceRef> matchedServices = new LinkedList<>();
        final boolean[] failNext = {true};
        Closeable session = serviceTracker.trackService(OPENCONFIG, new ServiceTracker.ServiceListener() {
            @Override
            public void onServiceMatched(ServiceRef serviceRef) {
                matchedServices.add(serviceRef);
                if (failNext[0]) {
                    failNext[0] = false;
                    throw new IllegalStateException("listener failure");
                }
            }

            @Override
            public void onServiceStoppedMatching(ServiceRef serviceRef) {
            }
        });

        try {
            mockFilterWatcher.addServices(Arrays.asList(
                    new ServiceRef(1, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG, DEFAULT_LOCATION),
                    new ServiceRef(2, InetAddressUtils.ONE_TWENTY_SEVEN, OPENCONFIG, DEFAULT_LOCATION)));
            fail("Expected the listener failure to propagate");
        } catch (IllegalStateException expected) {
            // the filter watcher logs callback failures
        }

        mockFilterWatcher.refresh();

        assertThat(matchedServices, hasSize(2));
        assertThat(matchedServices.get(0), not(equalTo(matchedServices.get(1))));

        session.close();
    }

    @Override
    public void onServiceMatched(ServiceRef serviceRef) {
        activeServices.add(serviceRef);
        numServicesAdded++;
    }

    @Override
    public void onServiceStoppedMatching(ServiceRef serviceRef) {
        activeServices.remove(serviceRef);
        numServicesRemoved++;
    }

    public static class MockFilterWatcher implements FilterWatcher {
        private final List<ServiceRef> exposedServices = new LinkedList<>();
        private final List<Consumer<FilterResults>> callbacks = new LinkedList<>();

        @Override
        public Closeable watch(String filterRule, Consumer<FilterResults> callback) {
            callbacks.add(callback);
            return () -> {
                callbacks.remove(callback);
            };
        }

        public void addService(int nodeId, InetAddress interfaceAddress, String serviceName) {
            exposedServices.add(new ServiceRef(nodeId, interfaceAddress, serviceName,DEFAULT_LOCATION));
            rebuildResultsAndNotify();
        }

        public void addServices(List<ServiceRef> services) {
            exposedServices.addAll(services);
            rebuildResultsAndNotify();
        }

        public void refresh() {
            rebuildResultsAndNotify();
        }

        public void deleteService(int nodeId, InetAddress interfaceAddress, String serviceName) {
            exposedServices.removeIf(s -> Objects.equals(s, new ServiceRef(nodeId, interfaceAddress, serviceName,DEFAULT_LOCATION)));
            rebuildResultsAndNotify();
        }

        private void rebuildResultsAndNotify() {
            FilterResults filterResults = new FilterResults() {
                @Override
                public Map<Integer, Map<InetAddress, Set<String>>> getNodeIpServiceMap() {
                    return Collections.emptyMap();
                }

                @Override
                public Set<ServiceRef> getServicesNamed(String serviceName) {
                    return exposedServices.stream()
                            .filter(s -> s.getServiceName().equals(serviceName))
                            .collect(Collectors.toSet());
                }
            };
            callbacks.forEach(c -> c.accept(filterResults));
        }
    }
}
