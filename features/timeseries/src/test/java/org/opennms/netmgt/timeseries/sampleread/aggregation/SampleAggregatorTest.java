/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.sampleread.aggregation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.opennms.core.test.OnmsAssert.assertThrowsException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.integration.api.v1.timeseries.Aggregation;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableMetric;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;

public class SampleAggregatorTest {

    private List<Sample> samples;
    private List<Sample> results;

    @Before
    public void setUp() {
        samples = new ArrayList<>();
    }

    private final ImmutableMetric metric = ImmutableMetric.builder()
            .intrinsicTag(IntrinsicTagNames.resourceId, "123").build();

    @Test
    public void shouldAggregateToAverage() {
        sample(10, 10);
        sample(12, 15);
        sample(13, 20);
        sample(16, 13);
        compute(Aggregation.AVERAGE, 10, 19, 5);
        assertEquals(2, results.size());
        expect(0, 10, 15);
        expect(1, 15, 13);
    }

    @Test
    public void shouldAggregateToMax() {
        sample(10, 10);
        sample(12, 15);
        sample(13, 20);
        sample(16, 13);
        compute(Aggregation.MAX, 10, 19, 5);
        assertEquals(2, results.size());
        expect(0, 10, 20);
        expect(1, 15, 13);
    }

    @Test
    public void shouldAggregateToMin() {
        sample(10, 10);
        sample(12, 15);
        sample(13, 20);
        sample(16, 13);
        compute(Aggregation.MIN, 10, 19,5);
        assertEquals(2, results.size());
        expect(0, 10, 10);
        expect(1, 15, 13);
    }

    @Test
    public void shouldAggregateToNone() {
        sample(10, 10);
        sample(12, 15);
        sample(13, 20);
        sample(16, 13);
        compute(Aggregation.NONE, 10, 19,5);
        assertEquals(4, results.size());
        expect(0, 10, 10);
        expect(1, 12, 15);
        expect(2, 13, 20);
        expect(3, 16, 13);
    }

    @Test
    public void shouldFillGaps() {
        sample(10, 10);
        sample(12, 15);
        sample(13, 20);
        sample(21, 13);
        compute(Aggregation.AVERAGE, 10, 24, 5);
        assertEquals(3, results.size());
        expect(0, 10, 15);
        expect(1, 15, Double.NaN);
        expect(2, 20, 13);
    }

    @Test
    public void shouldRejectWrongMetric() {
        ImmutableMetric wrongMetric = ImmutableMetric.builder()
                .intrinsicTag(IntrinsicTagNames.resourceId, "otherResource").build();

        sample(10, 10);
        samples.add(ImmutableSample.builder().metric(wrongMetric).time(Instant.ofEpochMilli(11)).value(11d).build());
        assertThrowsException(IllegalArgumentException.class, () -> compute(Aggregation.AVERAGE, 10, 24, 5));
    }

    @Test
    public void shouldRejectNullValues() {
        assertThrowsException(NullPointerException.class, () -> compute(null, 10, 24, 5));
    }

    private void sample(long timeInMillis, double value) {
        samples.add(ImmutableSample.builder().metric(metric).time(Instant.ofEpochMilli(timeInMillis)).value(value).build());
    }

    private void compute(final Aggregation aggregation, final long startTime, final long endTime, final long bucketSize) {
        this.results = SampleAggregator.builder()
                .samples(this.samples)
                .aggregation(aggregation)
                .startTime(Instant.ofEpochMilli(startTime))
                .endTime(Instant.ofEpochMilli(endTime))
                .bucketSize(Duration.ofMillis(bucketSize))
                .expectedMetric(this.metric).build().computeAggregatedSamples();
    }

    private void expect(int index, long timeInMillis, double value) {
        if (results.size() <= index) {
            fail(String.format("Bucket with index %s not found", index));
        }
        Sample bucket = this.results.get(index);
        assertEquals(this.metric, bucket.getMetric());
        assertEquals(timeInMillis, bucket.getTime().toEpochMilli());
        assertEquals(Double.valueOf(value), bucket.getValue());
    }

}
