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
import org.opennms.integration.api.v1.timeseries.StorageException;
import org.opennms.integration.api.v1.timeseries.TagMatcher;
import org.opennms.integration.api.v1.timeseries.TimeSeriesData;
import org.opennms.integration.api.v1.timeseries.TimeSeriesFetchRequest;
import org.opennms.integration.api.v1.timeseries.TimeSeriesStorage;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.netmgt.timeseries.TimeseriesStorageManager;
import org.opennms.newts.api.Resource;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.junit.Assert.assertEquals;
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
