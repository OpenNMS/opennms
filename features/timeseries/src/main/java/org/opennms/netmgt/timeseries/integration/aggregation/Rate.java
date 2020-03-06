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
import static org.opennms.newts.api.MetricType.GAUGE;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.opennms.newts.api.Gauge;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Sample;
import org.opennms.newts.api.ValueType;

import com.google.common.collect.Maps;


/**
 * Conditionally calculate rate (per-second) on samples.
 * 
 * Copied from Newts project.
 */
class Rate implements Iterator<Row<Sample>>, Iterable<Row<Sample>> {

    private static final Gauge NAN = new Gauge(Double.NaN);
    private static final EnumSet<MetricType> COUNTERS = EnumSet.of(MetricType.COUNTER, MetricType.ABSOLUTE, MetricType.DERIVE);

    private final Iterator<Row<Sample>> m_input;
    private final Set<String> m_metrics;
    private final Map<String, Sample> m_prevSamples;

    Rate(Iterator<Row<Sample>> input, Set<String> metrics) {
        m_input = checkNotNull(input, "input argument");
        m_metrics = checkNotNull(metrics, "metrics argument");
        m_prevSamples = Maps.newHashMapWithExpectedSize(m_metrics.size());
    }

    @Override
    public boolean hasNext() {
        return m_input.hasNext();
    }

    @Override
    public Row<Sample> next() {

        if (!hasNext()) throw new NoSuchElementException();

        Row<Sample> working = m_input.next();
        Row<Sample> result = new Row<>(working.getTimestamp(), working.getResource());

        for (String metricName : m_metrics) {
            Sample sample = working.getElement(metricName);

            if (sample == null) {
                continue;
            }

            // Use rate as result if one of counter types, else pass through as-is.
            result.addElement(COUNTERS.contains(sample.getType()) ? getRate(sample) : sample);

            m_prevSamples.put(sample.getName(), sample);

        }

        return result;
    }

    private Sample getRate(Sample sample) {

        ValueType<?> value = NAN;
        Sample previous = m_prevSamples.get(sample.getName());

        if (previous != null) {
            long elapsed = sample.getTimestamp().asSeconds() - previous.getTimestamp().asSeconds();
            try {
                value = new Gauge(sample.getValue().delta(previous.getValue()).doubleValue() / elapsed);
            } catch (ArithmeticException e) {
                value = NAN;
            }
        }

        return new Sample(sample.getTimestamp(), sample.getResource(), sample.getName(), GAUGE, value, sample.getAttributes());
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Row<Sample>> iterator() {
        return this;
    }

}
