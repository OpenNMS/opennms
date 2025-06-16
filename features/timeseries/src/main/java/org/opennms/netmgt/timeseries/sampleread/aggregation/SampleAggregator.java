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
package org.opennms.netmgt.timeseries.sampleread.aggregation;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.timeseries.Aggregation;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;

public class SampleAggregator {

    final Metric expectedMetric;
    final List<Sample> samples;
    final Aggregation aggregation;
    final Instant startTime;
    final Instant endTime;
    final Duration bucketSize;

    private SampleAggregator(final Metric expectedMetric,
                             final List<Sample> samples,
                             final Aggregation aggregation,
                             final Instant startTime,
                             final Instant endTime,
                             final Duration bucketSize) {
        this.expectedMetric = Objects.requireNonNull(expectedMetric);
        this.samples = Objects.requireNonNull(samples);
        this.aggregation = Objects.requireNonNull(aggregation);
        this.startTime = Objects.requireNonNull(startTime);
        this.endTime = Objects.requireNonNull(endTime);
        this.bucketSize = Objects.requireNonNull(bucketSize);
    }

    public static SampleAggregatorBuilder builder() {
        return new SampleAggregatorBuilder();
    }

    public List<Sample> computeAggregatedSamples() {

        // make sure we have only the expected metric
        Optional<Sample> missmatchedSample = this.samples.stream().filter(s -> !this.expectedMetric.equals(s.getMetric())).findAny();
        if(missmatchedSample.isPresent()) {
            throw new IllegalArgumentException(String.format("Expected Metric %s but found %s", expectedMetric,missmatchedSample.get().getMetric()));
        }

        if(this.aggregation == Aggregation.NONE) {
            return this.samples; // nothing to do
        }

        // bucket all samples by time
        final Map<Instant, List<Sample>> buckets = new HashMap<>();
        for (Sample sample : this.samples) {
            List<Sample> bucket = getBucket(buckets, sample.getTime());
            bucket.add(sample);
        }

        // fill gaps
        for(long l=this.startTime.toEpochMilli(); l<=this.endTime.toEpochMilli(); l=l+this.bucketSize.toMillis()) {
            Instant currentInstant = Instant.ofEpochMilli(l);
            List<Sample> bucket = getBucket(buckets, currentInstant); // will create bucket automatically
            if(bucket.isEmpty()) {
                bucket.add(ImmutableSample.builder().metric(expectedMetric).time(currentInstant).value(Double.NaN).build());
            }
        }

        // aggregate all samples in bucket and sort by time
        return buckets.entrySet().stream()
                .map(entry -> aggregate(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(i -> i.getTime().toEpochMilli()))
                .collect(Collectors.toList());
    }

    private Sample aggregate(final Instant startOfBucket, final List<Sample> samples) {
        if (samples == null || samples.isEmpty()) {
            return null;
        }
        List<Double> values = samples.stream().map(Sample::getValue).collect(Collectors.toList());
        Double value = getAggregation().apply(values);
        return ImmutableSample.builder()
                .metric(this.expectedMetric)
                .time(startOfBucket)
                .value(value).build();
    }

    private Function<Collection<Double>, Double> getAggregation() {
        if (Aggregation.MIN == this.aggregation) {
            return StandardAggregationFunctions.MIN;
        } else if (Aggregation.MAX == this.aggregation) {
            return StandardAggregationFunctions.MAX;
        } else {
            return StandardAggregationFunctions.AVERAGE;
        }
    }

    private List<Sample> getBucket(Map<Instant, List<Sample>> buckets, Instant instant) {
        long offset = instant.toEpochMilli() - startTime.toEpochMilli();
        long startOfBucket = startTime.toEpochMilli() + (offset / this.bucketSize.toMillis()) * this.bucketSize.toMillis();
        return buckets.computeIfAbsent(Instant.ofEpochMilli(startOfBucket), inst -> new ArrayList<>());
    }


    public static class SampleAggregatorBuilder {
        private Metric expectedMetric;
        private List<Sample> samples;
        private Aggregation aggregation;
        private Instant startTime;
        private Instant endTime;
        private Duration bucketSize;

        SampleAggregatorBuilder() {
        }

        public SampleAggregatorBuilder expectedMetric(Metric expectedMetric) {
            this.expectedMetric = expectedMetric;
            return this;
        }

        public SampleAggregatorBuilder samples(List<Sample> samples) {
            this.samples = samples;
            return this;
        }

        public SampleAggregatorBuilder aggregation(Aggregation aggregation) {
            this.aggregation = aggregation;
            return this;
        }

        public SampleAggregatorBuilder startTime(Instant startTime) {
            this.startTime = startTime;
            return this;
        }

        public SampleAggregatorBuilder endTime(Instant endTime) {
            this.endTime = endTime;
            return this;
        }

        public SampleAggregatorBuilder bucketSize(Duration bucketSize) {
            this.bucketSize = bucketSize;
            return this;
        }

        public SampleAggregator build() {
            return new SampleAggregator(expectedMetric, samples, aggregation, startTime, endTime, bucketSize);
        }

        public String toString() {
            return "SampleAggregator.SampleAggregatorBuilder(expectedMetric=" + this.expectedMetric + ", samples=" + this.samples + ", aggregation=" + this.aggregation + ", startTime=" + this.startTime + ", endTime=" + this.endTime + ", bucketSize=" + this.bucketSize + ")";
        }
    }
}
