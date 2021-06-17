/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.resource;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
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
        searcher = new TimeseriesSearcher(storageManager, cacheConfig);
    }

    @Test
    public void shouldFindAllMetrics() throws StorageException {

        Metric abc = createAndAddMetric("a/b", "c");
        Metric abcd1 = createAndAddMetric("a/b/c", "d1");
        Metric abcd1e = createAndAddMetric("a/b/c/d1", "e");
        Metric abcd2 = createAndAddMetric("a/b/c", "d2");
        test("a",  abc);
        test("a",  abc); // test cache: we should not have another invocation
        verify(storage, times(1)).findMetrics(any());

        test("a/b", abcd1, abcd2);
        test("a/b/c", abcd1e);
        test("a/b/not-existing");

        // verify wildcard cache: we expect only 1 more getMetrics() invocation for the 3 calls above
        verify(storage, times(2)).findMetrics(any());
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
