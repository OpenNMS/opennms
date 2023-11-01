/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.samplewrite;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.MetaTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import static org.mockito.Mockito.when;

public class OffheapTimeseriesWriterTest {

    private TimeseriesStorageManager storageManager;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() {
        this.storageManager = Mockito.mock(TimeseriesStorageManager.class);
    }

    /**
     * Uses a latch to verify that multiple that multiple threads
     * are used to concurrently insert samples into the SampleRepository.
     */
    @Test
    public void canWriteToSampleRepositoryUsingMultipleThreads() throws Exception {
        TimeseriesWriterConfig config = new TimeseriesWriterConfig();
        config.setBufferSize(1024);
        config.setNumWriterThreads(8);
        config.setBatchSize(32);
        config.setPath(folder.newFolder().toString());
        LatchedTimeseriesStorage store = new LatchedTimeseriesStorage(config.getNumWriterThreads());
        MetricRegistry registry = new MetricRegistry();

        OffheapTimeSeriesWriter writer = new OffheapTimeSeriesWriter(storageManager, config, registry);
        when(storageManager.get()).thenReturn(store);

        Metric metric = createMetric().build();
        for (int i = 0; i < config.getBufferSize() * 2; i++) {
            Sample s = ImmutableSample.builder()
                    .metric(metric)
                    .time(Instant.now())
                    .value((double) i).build();
            writer.insert(Lists.newArrayList(s));
        }
    }

    private static class LatchedTimeseriesStorage extends RingBufferTimeseriesWriterTest.MockTimeSeriesStorage {
        private final CountDownLatch latch;

        public LatchedTimeseriesStorage(int N) {
            latch = new CountDownLatch(N);
        }

        @Override
        public void store(List<Sample> samples) {
            latch.countDown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }
    }

    private ImmutableMetric.MetricBuilder createMetric() {
        return ImmutableMetric
                .builder()
                .intrinsicTag(IntrinsicTagNames.resourceId, "a/b")
                .intrinsicTag(IntrinsicTagNames.name, "c")
                .intrinsicTag(MetaTagNames.mtype, Metric.Mtype.counter.name());
    }
}
