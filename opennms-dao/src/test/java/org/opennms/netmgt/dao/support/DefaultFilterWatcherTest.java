/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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
