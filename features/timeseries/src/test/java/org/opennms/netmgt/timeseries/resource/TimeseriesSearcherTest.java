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
package org.opennms.netmgt.timeseries.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.cache.CacheConfig;
import org.opennms.core.cache.CacheConfigBuilder;
import org.opennms.integration.api.v1.timeseries.InMemoryStorage;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.TimeSeriesStorage;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;

public class TimeseriesSearcherTest {
    TimeSeriesStorage storage = spy(new InMemoryStorage());
    TimeseriesStorageManager storageManager;
    CacheConfig cacheConfig;
    TimeseriesSearcher searcher;

    @Before
    public void setUp() throws Exception {
        storageManager = Mockito.mock(TimeseriesStorageManager.class);
        when(storageManager.get()).thenReturn(storage);
        cacheConfig = new CacheConfigBuilder().withName(TimeseriesSearcherTest.class.getSimpleName()).build();
        searcher = new TimeseriesSearcher(storageManager, cacheConfig);
    }

    @Test
    public void shouldFindAllMetrics() throws StorageException {
        // Node with FS/FID
        Metric n1_loadavg1m = createAndAddMetric("snmp/fs/FOREIGN_SOURCE/FOREIGN_ID/node-stats", "loadavg1m");
        Metric n1_loadavg5m = createAndAddMetric("snmp/fs/FOREIGN_SOURCE/FOREIGN_ID/node-stats", "loadavg5m");
        Metric n1_ifHcInOctects = createAndAddMetric("snmp/fs/FOREIGN_SOURCE/FOREIGN_ID/eth0/mib2-stats", "ifHcInOctects");
        Metric n1_ifHcOutOctects = createAndAddMetric("snmp/fs/FOREIGN_SOURCE/FOREIGN_ID/eth0/mib2-stats", "ifHcOutOctects");
        Metric n1_dskUsage = createAndAddMetric("snmp/fs/FOREIGN_SOURCE/FOREIGN_ID/dskIndex/C_/disk-stats", "dskUsage");

        // Node without FS/FID
        Metric n2_loadavg1m = createAndAddMetric("snmp/2/node-stats", "loadavg1m");
        Metric n2_loadavg5m = createAndAddMetric("snmp/2/node-stats", "loadavg5m");
        Metric n2_ifHcInOctects = createAndAddMetric("snmp/2/eth0/mib2-stats", "ifHcInOctects");
        Metric n2_ifHcOutOctects = createAndAddMetric("snmp/2/eth0/mib2-stats", "ifHcOutOctects");
        Metric n2_dskUsage = createAndAddMetric("snmp/2/dskIndex/C_/disk-stats", "dskUsage");

        test("snmp/fs/FOREIGN_SOURCE/FOREIGN_ID",  n1_loadavg1m, n1_loadavg5m);
        test("snmp/fs/FOREIGN_SOURCE/FOREIGN_ID",  n1_loadavg1m, n1_loadavg5m);
        test("snmp/fs/FOREIGN_SOURCE/FOREIGN_ID/eth0", n1_ifHcInOctects, n1_ifHcOutOctects);
        test("snmp/fs/FOREIGN_SOURCE/FOREIGN_ID/dskIndex/C_", n1_dskUsage);
        test("snmp/fs/FOREIGN_SOURCE/FOREIGN_ID/someNonExistantType");

        // verify wildcard cache: we expect only one getMetrics() invocation for the 5 calls above
        // since all resources are under the same node path: snmp/fs/FOREIGN_SOURCE/FOREIGN_ID
        verify(storage, times(1)).findMetrics(any());
        clearInvocations(storage);

        // search another provisioned node => we should get another call since resources from that node should not be cached
        searcher.search(ResourcePath.fromString("snmp/fs/FOREIGN_SOURCE/SOME_OTHER_FOREIGN_ID"),  0);
        verify(storage, times(1)).findMetrics(any());
        clearInvocations(storage);

        test("snmp/2/eth0", n2_ifHcInOctects, n2_ifHcOutOctects);
        test("snmp/2/dskIndex/C_", n2_dskUsage);
        test("snmp/2/someNonExistantType");

        // verify wildcard cache: we expect only 1 more getMetrics() invocation for the 3 calls above
        // since all resources are under the same node path: snmp/2
        verify(storage, times(1)).findMetrics(any());
        clearInvocations(storage);

        // search another auto-detect node => we should get another call since resources from that node should not be cached
        searcher.search(ResourcePath.fromString("snmp/5/eth0"),  0);
        verify(storage, times(1)).findMetrics(any());
        clearInvocations(storage);
    }

    @Test
    public void shouldReloadAfterReadExpiry() throws StorageException, InterruptedException {
        final CacheConfig expiringConfig = new CacheConfigBuilder().withName("expiring").build();
        expiringConfig.setExpireAfterRead(1L);
        searcher = new TimeseriesSearcher(storageManager, expiringConfig);

        Metric n2_loadavg1m = createAndAddMetric("snmp/2/node-stats", "loadavg1m");
        Metric n2_loadavg5m = createAndAddMetric("snmp/2/node-stats", "loadavg5m");

        test("snmp/2", n2_loadavg1m, n2_loadavg5m);
        verify(storage, times(1)).findMetrics(any());

        // a miss below the node is answered from the node-level entry, without a new load
        // and without leaving a placeholder behind
        test("snmp/2/notExist");
        test("snmp/2", n2_loadavg1m, n2_loadavg5m);
        verify(storage, times(1)).findMetrics(any());

        Thread.sleep(1100L);
        test("snmp/2/notExist");
        // the node-level entry expired, so storage is consulted again
        verify(storage, times(2)).findMetrics(any());
    }

    @Test
    public void newResourcesShouldAppearDespiteConstantReads() throws Exception {
        final CacheConfig config = new CacheConfigBuilder().withName("writeExpiry").build();
        config.setExpireAfterRead(1L);
        config.setExpireAfterWrite(2L);
        searcher = new TimeseriesSearcher(storageManager, config);

        Metric existing = createAndAddMetric("snmp/2/eth0/mib2-stats", "ifInOctets");
        final ResourcePath existingPath = ResourcePath.fromString("snmp/2/eth0");
        final ResourcePath newPath = ResourcePath.fromString("snmp/2/eth1");

        // read one resource at an interval well below the read expiry, for longer than the read
        // expiry but shorter than the write expiry: the reads keep the node entry alive
        final long until = System.currentTimeMillis() + 1500L;
        while (System.currentTimeMillis() < until) {
            assertEquals(Collections.singleton(existing), searcher.search(existingPath, 0));
            Thread.sleep(100L);
        }
        verify(storage, times(1)).findMetrics(any());

        // a resource collected after the entry was built ...
        Metric added = createAndAddMetric("snmp/2/eth1/mib2-stats", "ifInOctets");

        // ... shows up once the write expiry passes, while the constant reads go on
        final long deadline = System.currentTimeMillis() + 4000L;
        Set<Metric> found = searcher.search(newPath, 0);
        while (found.isEmpty() && System.currentTimeMillis() < deadline) {
            assertEquals(Collections.singleton(existing), searcher.search(existingPath, 0));
            Thread.sleep(100L);
            found = searcher.search(newPath, 0);
        }
        assertEquals(Collections.singleton(added), found);
        verify(storage, times(2)).findMetrics(any());
    }

    @Test
    public void removeFromCacheShouldHideDeletedMetricsWithoutReloading() throws StorageException {
        Metric eth0In = createAndAddMetric("snmp/2/eth0/mib2-stats", "ifInOctets");
        Metric eth0Out = createAndAddMetric("snmp/2/eth0/mib2-stats", "ifOutOctets");
        Metric eth1In = createAndAddMetric("snmp/2/eth1/mib2-stats", "ifInOctets");
        final ResourcePath eth0 = ResourcePath.fromString("snmp/2/eth0");

        test("snmp/2/eth0", eth0In, eth0Out);
        assertEquals(new HashSet<>(Arrays.asList(eth0In, eth0Out, eth1In)), searcher.search(ResourcePath.fromString("snmp/2"), 1));

        // the DAO deleted eth0 from storage and tells the searcher about it
        storage.delete(eth0In);
        storage.delete(eth0Out);
        searcher.removeFromCache(eth0, Arrays.asList(eth0In, eth0Out));

        test("snmp/2/eth0");
        assertEquals(Collections.singleton(eth1In), searcher.search(ResourcePath.fromString("snmp/2"), 1));
        test("snmp/2/eth1", eth1In);
        // all of the above was answered from the cached node entry
        verify(storage, times(1)).findMetrics(any());

        // removing something that is not cached is a no-op
        searcher.removeFromCache(ResourcePath.fromString("snmp/3/eth0"), Collections.singletonList(eth1In));
        verify(storage, times(1)).findMetrics(any());
    }

    @Test
    public void searchResultsShouldBeImmutable() throws StorageException {
        Metric metric = createAndAddMetric("snmp/2/node-stats", "loadavg1m");
        Set<Metric> found = searcher.search(ResourcePath.fromString("snmp/2"), 0);
        assertEquals(Collections.singleton(metric), found);
        assertThrows(UnsupportedOperationException.class, () -> found.add(metric));
        assertThrows(UnsupportedOperationException.class, found::clear);
    }

    @Test
    public void shouldIndexEveryAncestorBelowTheNode() throws StorageException {
        // Below node level every ancestor is searchable, whether or not an element contains a
        // dot: generic index instances such as "192.168.1.1" or "data.old" keep their dots.
        Metric plain = createAndAddMetric("snmp/2/eth0/mib2-stats", "ifInOctets");
        Metric dotted = createAndAddMetric("snmp/2/bgpPeerEntry/192.168.1.1/bgp-stats", "bgpPeerInUpdates");
        Metric deeper = createAndAddMetric("snmp/2/dskIndex/C_/disk-stats", "dskUsage");

        test("snmp/2/eth0", plain);
        test("snmp/2/bgpPeerEntry/192.168.1.1", dotted);
        test("snmp/2");                          // node-stats only, there are none here
        assertEquals(Collections.singleton(plain), searcher.search(ResourcePath.fromString("snmp/2"), 1));
        assertEquals(new HashSet<>(Arrays.asList(dotted, deeper)), searcher.search(ResourcePath.fromString("snmp/2"), 2));
        assertEquals(Collections.singleton(dotted), searcher.search(ResourcePath.fromString("snmp/2/bgpPeerEntry"), 1));
        assertEquals(Collections.singleton(deeper), searcher.search(ResourcePath.fromString("snmp/2/dskIndex"), 1));
        verify(storage, times(1)).findMetrics(any());
    }

    @Test
    public void depthMinusOneShouldMatchTheResourceIdExactly() throws StorageException {
        Metric loadavg1m = createAndAddMetric("snmp/2/node-stats", "loadavg1m");
        Metric loadavg5m = createAndAddMetric("snmp/2/node-stats", "loadavg5m");
        Metric icmp = createAndAddMetric("response/10.0.0.1/icmp", "icmp");

        // below node level
        assertEquals(new HashSet<>(Arrays.asList(loadavg1m, loadavg5m)), searcher.search(ResourcePath.fromString("snmp/2/node-stats"), -1));
        assertEquals(Collections.emptySet(), searcher.search(ResourcePath.fromString("snmp/2"), -1));
        verify(storage, times(1)).findMetrics(any());

        // above node level
        assertEquals(Collections.singleton(icmp), searcher.search(ResourcePath.fromString("response/10.0.0.1/icmp"), -1));
        verify(storage, times(2)).findMetrics(any());
    }

    @Test
    public void shouldNotIndexParentsAboveTheQueriedOne() throws StorageException {
        Metric metric = createAndAddMetric("snmp/fs/FS/FID/eth0/mib2-stats", "ifInOctets");
        TimeseriesSearcher.MetricIndex index = TimeseriesSearcher.MetricIndex.of(Collections.singletonList(metric), 4);
        assertEquals(Collections.singleton(metric), index.get("snmp/fs/FS/FID", 2));
        assertEquals(Collections.singleton(metric), index.get("snmp/fs/FS/FID/eth0", 1));
        assertEquals(Collections.singleton(metric), index.get("snmp/fs/FS/FID/eth0/mib2-stats", 0));
        assertEquals(Collections.emptySet(), index.get("snmp/fs/FS", 3));
        assertEquals(Collections.emptySet(), index.get("snmp", 5));
    }

    @Test
    public void shouldQueryStorageDirectlyAboveNodeLevel() throws StorageException {
        Metric icmp = createAndAddMetric("response/10.0.0.1/icmp", "icmp");
        Metric node2 = createAndAddMetric("snmp/2/node-stats", "loadavg1m");

        assertEquals(Collections.singleton(icmp), searcher.search(ResourcePath.fromString("response/10.0.0.1"), 0));
        assertEquals(Collections.singleton(icmp), searcher.search(ResourcePath.fromString("response/10.0.0.1"), 0));
        // one exact query, served from cache the second time
        verify(storage, times(1)).findMetrics(any());

        assertEquals(Collections.singleton(node2), searcher.search(ResourcePath.fromString("snmp/2"), 0));
        verify(storage, times(2)).findMetrics(any());
    }

    /**
     * Many threads search different resources of the same node while that node is being
     * loaded for the first time. Every search must return exactly the metrics stored for
     * its path. Before the fix, threads that lost the race to load the node-level entry
     * fell through to a placeholder empty set, or saw a set that was still being filled,
     * and returned incomplete results.
     */
    @Test
    public void concurrentSearchesShouldNeverObserveIncompleteResults() throws Exception {
        final int numInterfaces = 300;
        final int numThreads = 16;
        final int searchesPerThread = 40;
        final int rounds = 25;

        final String nodePath = "snmp/fs/FOREIGN_SOURCE/FOREIGN_ID";
        final Map<String, Set<Metric>> expectedByPath = new HashMap<>();
        expectedByPath.put(nodePath, new HashSet<>(Arrays.asList(
                createAndAddMetric(nodePath + "/node-stats", "loadavg1m"),
                createAndAddMetric(nodePath + "/node-stats", "loadavg5m"))));
        for (int i = 0; i < numInterfaces; i++) {
            final String ifPath = nodePath + "/eth" + i;
            expectedByPath.put(ifPath, new HashSet<>(Arrays.asList(
                    createAndAddMetric(ifPath + "/mib2-stats", "ifHcInOctets"),
                    createAndAddMetric(ifPath + "/mib2-stats", "ifHcOutOctets"),
                    createAndAddMetric(ifPath + "/mib2-stats", "ifInErrors"))));
        }
        expectedByPath.put(nodePath + "/doesNotExist", Collections.emptySet());
        final List<String> paths = new ArrayList<>(expectedByPath.keySet());

        // Slow the storage down so that every thread of a round piles up on the same
        // node-level load and wakes up at the same time.
        doAnswer(invocation -> {
            Thread.sleep(25L);
            return invocation.callRealMethod();
        }).when(storage).findMetrics(any());
        clearInvocations(storage);

        final ConcurrentLinkedQueue<String> mismatches = new ConcurrentLinkedQueue<>();
        final ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        try {
            for (int round = 0; round < rounds; round++) {
                // a fresh searcher per round so every round starts with a cold cache
                final TimeseriesSearcher coldSearcher = new TimeseriesSearcher(storageManager, cacheConfig);
                final CyclicBarrier startLine = new CyclicBarrier(numThreads);
                final List<Future<?>> futures = new ArrayList<>();
                for (int t = 0; t < numThreads; t++) {
                    final Random random = new Random(round * 1000L + t);
                    futures.add(executor.submit(() -> {
                        startLine.await();
                        for (int i = 0; i < searchesPerThread; i++) {
                            final String path = paths.get(random.nextInt(paths.size()));
                            final Set<Metric> expected = expectedByPath.get(path);
                            final Set<Metric> actual = coldSearcher.search(ResourcePath.fromString(path), 0);
                            if (!expected.equals(actual)) {
                                mismatches.add(String.format("%s: expected %d metrics but got %d", path, expected.size(), actual.size()));
                            }
                        }
                        return null;
                    }));
                }
                for (Future<?> future : futures) {
                    future.get(30, TimeUnit.SECONDS);
                }
            }
        } finally {
            executor.shutdownNow();
        }

        assertTrue("Concurrent searches returned incomplete results: " + mismatches.size() + " mismatches, e.g. "
                + new ArrayList<>(mismatches).subList(0, Math.min(5, mismatches.size())), mismatches.isEmpty());
        // concurrent loads of the same node must be coalesced into one storage query per round
        verify(storage, times(rounds)).findMetrics(any());
    }

    private void test(String path, Metric...expectedMetrics) throws StorageException {
        Set<Metric> foundMetrics = searcher.search(ResourcePath.fromString(path), 0);
        Set<Metric> expectedMetricsSet = new HashSet<>(Arrays.asList(expectedMetrics));
        assertEquals(expectedMetricsSet, foundMetrics);
    }

    private Metric createAndAddMetric(final String resourceId, final String name) throws StorageException {
        ImmutableMetric.MetricBuilder metricBuilder = ImmutableMetric.builder()
                .intrinsicTag(IntrinsicTagNames.resourceId, resourceId)
                .intrinsicTag(IntrinsicTagNames.name, name);
        Metric metric = metricBuilder.build();

        storage.store(Collections.singletonList(ImmutableSample.builder().metric(metric).time(Instant.now()).value(3.0).build()));
        return metric;
    }

}
