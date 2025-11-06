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

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.FilterWatcher;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.model.ImmutableEvent;
import org.opennms.netmgt.filter.api.FilterDao;

public class DefaultFilterWatcherTest {

    private DefaultFilterWatcher filterWatcher;
    private FilterDao filterDao;
    private MockSessionUtils sessionUtils = new MockSessionUtils();

    private List<FilterWatcher.FilterResults> filterResultsList = new LinkedList<>();
    private FilterWatcher.FilterResults lastFilterResults;
    private int numCallbacks = 0;

    @Before
    public void setUp() {
        filterWatcher  = new DefaultFilterWatcher();
        // Use a short refresh limit for testing
        filterWatcher.setRefreshRateLimitMs(50);

        NodeDao nodeDao = mock(NodeDao.class);
        when(nodeDao.getLocationForId(anyInt())).thenReturn("test-location");
        filterWatcher.setNodeDao(nodeDao);

        filterDao = mock(FilterDao.class);
        filterWatcher.setFilterDao(filterDao);
        sessionUtils = new MockSessionUtils();
        filterWatcher.setSessionUtils(sessionUtils);
        filterWatcher.afterPropertiesSet();
    }

    @After
    public void tearDown() {
        filterWatcher.destroy();
    }

    private void handleFilterResults(FilterWatcher.FilterResults results) {
        lastFilterResults = results;
        filterResultsList.add(results);
        numCallbacks++;
    }

    @Test
    public void canWatchFilter() throws IOException, InterruptedException {
        Closeable session = filterWatcher.watch("catincProduction", this::handleFilterResults);

        assertThat(numCallbacks, equalTo(1));
        assertThat(lastFilterResults.getServicesNamed("any"), hasSize(0));

        // Sleep for a while
        Thread.sleep(5 * filterWatcher.getRefreshRateLimitMs());

        // No callbacks should have been made
        assertThat(numCallbacks, equalTo(1));

        // Mock some new results
        when(filterDao.getNodeIPAddressServiceMap(any(String.class))).thenReturn(getNodeIPAddressServiceMap(1,1,"ICMP"));
        // Trigger some event
        filterWatcher.inventoryChangeEventHandler(ImmutableEvent.newBuilder()
                .setUei(EventConstants.NODE_CATEGORY_MEMBERSHIP_CHANGED_EVENT_UEI)
                .setSource("test")
                .build());
        // Wait for another callback
        await().atMost(5, TimeUnit.SECONDS).until(() -> numCallbacks, equalTo(2));

        session.close();
    }

    public Map<Integer, Map<InetAddress, Set<String>>> getNodeIPAddressServiceMap(int numNodes, int numInterfaces, String... services) {
        Map<Integer, Map<InetAddress, Set<String>>> nodeIpServiceMap = new LinkedHashMap<>();
        for (int i = 1; i <= numNodes; i++) {
            Map<InetAddress, Set<String>> ipServiceMap = new LinkedHashMap<>();
            for (int j = 1; j <= numInterfaces; j++) {
                Set<String> ipServices = new HashSet<>(Arrays.asList(services));
                ipServiceMap.put(InetAddressUtils.convertCidrToInetAddressV4(j), ipServices);
            }
            nodeIpServiceMap.put(i, ipServiceMap);
        }
        return nodeIpServiceMap;
    }

    private static class MockSessionUtils implements SessionUtils {
        @Override
        public <V> V withTransaction(Supplier<V> supplier) {
            return supplier.get();
        }

        @Override
        public <V> V withReadOnlyTransaction(Supplier<V> supplier) {
            return supplier.get();
        }

        @Override
        public <V> V withManualFlush(Supplier<V> supplier) {
            return supplier.get();
        }
    }
}
