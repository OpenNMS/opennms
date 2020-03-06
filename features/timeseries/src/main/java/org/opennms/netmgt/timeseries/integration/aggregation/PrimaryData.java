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

package org.opennms.netmgt.timeseries.integration.aggregation;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.opennms.newts.aggregate.IntervalGenerator;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;
import org.opennms.newts.api.query.Datasource;
import org.opennms.newts.api.query.ResultDescriptor;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;


/**
 * Generate primary data point measurements from a stream of samples.
 *
 * Copied from Newts project.
 */
class PrimaryData implements Iterator<Row<Measurement>>, Iterable<Row<Measurement>> {

    private static class Accumulation {
        private long m_known, m_unknown;
        private ValueType<?> m_value;
        private Map<String, String> m_attributes = Maps.newHashMap();

        private Accumulation() {
            reset();
        }

        private Accumulation accumulateValue(Duration elapsedWithinInterval, Duration elapsedBetweenSamples, Duration heartbeat, ValueType<?> value) {
            if (elapsedBetweenSamples.lt(heartbeat)) {
                m_known += elapsedWithinInterval.asMillis();
                m_value = m_value.plus(value.times(elapsedWithinInterval.asMillis()));
            }
            else {
                m_unknown += elapsedWithinInterval.asMillis();
            }
            return this;
        }

        private Accumulation accumlateAttrs(Map<String, String> attributes) {
            if (attributes != null) m_attributes.putAll(attributes);
            return this;
        }

        private Double getAverage() {
            return isValid() ? m_value.divideBy(m_known).doubleValue() : Double.NaN;
        }

        private long getKnown() {
            return m_known;
        }

        private long getUnknown() {
            return m_unknown;
        }

        private double getElapsed() {
            return getKnown() + getUnknown();
        }

        private boolean isValid() {
            return getUnknown() < (getElapsed() / 2);
        }

        private void reset() {
            m_known = m_unknown = 0;
            m_value = ValueType.compose(0, MetricType.GAUGE);
            m_attributes = Maps.newHashMap();
        }

        private Map<String, String> getAttributes() {
            return m_attributes;
        }

    }

    private final ResultDescriptor m_resultDescriptor;
    private final Resource m_resource;
    private final Iterator<Timestamp> m_timestamps;
    private final Duration m_interval;
    private Timestamp lastIntervalCeiling = null;
    private final ArrayList<Row<Sample>> m_samples = Lists.newArrayList();
    private final Map<String, Integer> m_lastSampleIndex = Maps.newHashMap();
    private final Map<String, Accumulation> m_accumulation = Maps.newHashMap();

    PrimaryData(Resource resource, Timestamp start, Timestamp end, ResultDescriptor resultDescriptor, Iterator<Row<Sample>> input) {
        m_resultDescriptor = checkNotNull(resultDescriptor, "result descriptor argument");
        m_resource = checkNotNull(resource, "resource argument");
        checkNotNull(start, "start argument");
        checkNotNull(end, "end argument");
        m_interval = resultDescriptor.getInterval();

        m_timestamps = new IntervalGenerator(start.stepFloor(m_interval), end.stepCeiling(m_interval), m_interval);

        // Gather the whole collection of rows.
        // We need these since the next sample for a given metric may only appear a few rows ahead
        Iterators.addAll(m_samples, checkNotNull(input, "input argument"));
    }

    @Override
    public boolean hasNext() {
        return m_timestamps.hasNext();
    }

    @Override
    public Row<Measurement> next() {
        if (!hasNext()) throw new NoSuchElementException();

        Timestamp intervalCeiling = m_timestamps.next();
        Row<Measurement> output = new Row<>(intervalCeiling, m_resource);

        for (Datasource ds : m_resultDescriptor.getDatasources().values()) {
            Accumulation accumulation = getOrCreateAccumulation(ds.getSource());
            accumulation.reset();

            int lastSampleIdx = 0;
            if (m_lastSampleIndex.containsKey(ds.getSource())) {
                lastSampleIdx = m_lastSampleIndex.get(ds.getSource());
            }

            Sample last = null;
            for (int sampleIdx = lastSampleIdx; sampleIdx < m_samples.size(); sampleIdx++) {
                Row<Sample> row = m_samples.get(sampleIdx);
                Sample current;

                current = row.getElement(ds.getSource());

                // Skip the row if it does not contain a sample for the current datasource
                if (current == null) {
                    continue;
                }

                if (last == null) {
                    last = current;
                    lastSampleIdx = sampleIdx;
                    continue;
                }

                // Accumulate nothing when samples are beyond this interval
                if (intervalCeiling.lt(last.getTimestamp())) {
                    break;
                }

                Timestamp lowerBound = last.getTimestamp();
                if (lastIntervalCeiling != null && lastIntervalCeiling.gt(lowerBound)) {
                    lowerBound = lastIntervalCeiling;
                }

                Timestamp upperBound = current.getTimestamp();
                if (intervalCeiling.lt(upperBound)) {
                    upperBound = intervalCeiling;
                }
                if (lowerBound.gt(upperBound)) {
                    lowerBound = upperBound;
                }

                Duration elapsedWithinInterval = upperBound.minus(lowerBound);
                Duration elapsedBetweenSamples = current.getTimestamp().minus(last.getTimestamp());

                m_lastSampleIndex.put(ds.getSource(), lastSampleIdx);
                accumulation.accumulateValue(elapsedWithinInterval, elapsedBetweenSamples,
                        ds.getHeartbeat(), current.getValue())
                    .accumlateAttrs(current.getAttributes());

                last = current;
                lastSampleIdx = sampleIdx;
            }

            // Add sample with accumulated value to output row
            output.addElement(new Measurement(
                    output.getTimestamp(),
                    output.getResource(),
                    ds.getSource(),
                    accumulation.getAverage(),
                    accumulation.getAttributes()));
        }

        lastIntervalCeiling = intervalCeiling;
        return output;
    }

    private Accumulation getOrCreateAccumulation(String name) {
        Accumulation result = m_accumulation.get(name);

        if (result == null) {
            result = new Accumulation();
            m_accumulation.put(name, result);
        }

        return result;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Row<Measurement>> iterator() {
        return this;
    }

}
