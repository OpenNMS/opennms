/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
    TimeseriesSearcher searcher;

    @Before
    public void setUp() {
        TimeseriesStorageManager storageManager = Mockito.mock(TimeseriesStorageManager.class);
        when(storageManager.get()).thenReturn(storage);
        CacheConfig cacheConfig = new CacheConfigBuilder().withName(TimeseriesSearcherTest.class.getSimpleName()).build();
        // for test shouldBuildCacheOnce
        cacheConfig.setExpireAfterRead(1L);
        searcher = spy(new TimeseriesSearcher(storageManager, cacheConfig));
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
    public void shouldOnlyBuildCacheOnce() throws StorageException, InterruptedException {
        Metric n2_loadavg1m = createAndAddMetric("snmp/2/node-stats", "loadavg1m");
        Metric n2_loadavg5m = createAndAddMetric("snmp/2/node-stats", "loadavg5m");

        test("snmp/2", n2_loadavg1m, n2_loadavg5m );
        verify(searcher, times(1)).buildCache(any());
        test("snmp/2/notExist", new Metric[0]);
        // it should still 1
        verify(searcher, times(1)).buildCache(any());

        Thread.sleep(1000L);
        test("snmp/2/notExist", new Metric[0]);
        // should be 2 now because of read expiry
        verify(searcher, times(2)).buildCache(any());
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
