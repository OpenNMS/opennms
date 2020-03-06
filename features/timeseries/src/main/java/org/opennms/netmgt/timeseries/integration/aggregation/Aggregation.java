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


import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Map;

import org.opennms.newts.aggregate.IntervalGenerator;
import org.opennms.newts.api.Duration;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results.Row;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.query.Datasource;
import org.opennms.newts.api.query.ResultDescriptor;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;


/**
 * Apply aggregate functions to measurements.
 *
 * Copied from Newts project.
 */
class Aggregation implements Iterable<Row<Measurement>>, Iterator<Row<Measurement>> {

    private final ResultDescriptor m_resultDescriptor;
    private final Resource m_resource;
    private final Iterator<Timestamp> m_timestamps;
    private final Duration m_resolution;
    private final Iterator<Row<Measurement>> m_input;
    private final double m_intervalsPer;

    private Row<Measurement> m_working;
    private Row<Measurement> m_nextOut;

    Aggregation(Resource resource, Timestamp start, Timestamp end, ResultDescriptor resultDescriptor, Duration resolution, Iterator<Row<Measurement>> input) {
        m_resultDescriptor = checkNotNull(resultDescriptor, "result descriptor argument");
        m_resource = checkNotNull(resource, "resource argument");
        checkNotNull(start, "start argument");
        checkNotNull(end, "end argument");
        m_resolution = checkNotNull(resolution, "resolution argument");
        m_input = checkNotNull(input, "input argument");

        Duration interval = resultDescriptor.getInterval();
        checkArgument(resolution.isMultiple(interval), "resolution must be a multiple of interval");

        m_timestamps = new IntervalGenerator(start.stepFloor(m_resolution), end.stepCeiling(m_resolution), m_resolution);
        m_intervalsPer = (double) resolution.divideBy(interval);

        m_working = m_input.hasNext() ? m_input.next() : null;
        m_nextOut = m_timestamps.hasNext() ? new Row<Measurement>(m_timestamps.next(), m_resource) : null;

        // If the input stream contains any Samples earlier than what's relevant, iterate past them.
        if (m_nextOut != null) {
            while (m_working != null && m_working.getTimestamp().lte(m_nextOut.getTimestamp().minus(m_resolution))) {
                m_working = nextWorking();
            }
        }

    }

    @Override
    public boolean hasNext() {
        return m_nextOut != null;
    }

    @Override
    public Row<Measurement> next() {

        if (!hasNext()) throw new NoSuchElementException();

        Multimap<String, Double> values = ArrayListMultimap.create();
        Map<String, Map<String, String>> aggregatedAttrs = Maps.newHashMap();

        while (inRange()) {
            // accumulate
            for (Datasource ds : getDatasources()) {
                Measurement metric = m_working.getElement(ds.getSource());
                values.put(ds.getLabel(), metric != null ? metric.getValue() : Double.NaN);

                Map<String, String> metricAttrs = aggregatedAttrs.get(ds.getLabel());
                if (metricAttrs == null) {
                    metricAttrs = Maps.newHashMap();
                    aggregatedAttrs.put(ds.getLabel(), metricAttrs);
                }

                if (metric != null && metric.getAttributes() != null) {
                    metricAttrs.putAll(metric.getAttributes());
                }

            }

            m_working = nextWorking();
        }

        for (Datasource ds : getDatasources()) {
            Double v = aggregate(ds, values.get(ds.getLabel()));
            Map<String, String> attrs = aggregatedAttrs.get(ds.getLabel());
            m_nextOut.addElement(new Measurement(m_nextOut.getTimestamp(), m_resource, ds.getLabel(), v, attrs));
        }

        try {
            return m_nextOut;
        }
        finally {
            m_nextOut = m_timestamps.hasNext() ? new Row<Measurement>(m_timestamps.next(), m_resource) : null;
        }
    }

    // Return the result of this Datasource's aggregation function if the number of values
    // is within XFF, otherwise return NaN.
    private Double aggregate(Datasource ds, Collection<Double> values) {
        return ((values.size() / m_intervalsPer) > ds.getXff()) ? ds.getAggregationFuction().apply(values) : Double.NaN;
    }

    // true if the working input Row is within the Range of the next output Row; false otherwise
    private boolean inRange() {
        if (m_working == null || m_nextOut == null) {
            return false;
        }

        Timestamp rangeUpper = m_nextOut.getTimestamp();
        Timestamp rangeLower = m_nextOut.getTimestamp().minus(m_resolution);

        return m_working.getTimestamp().lte(rangeUpper) && m_working.getTimestamp().gt(rangeLower);
    }

    private Row<Measurement> nextWorking() {
        return m_input.hasNext() ? m_input.next() : null;
    }

    private Collection<Datasource> getDatasources() {
        return m_resultDescriptor.getDatasources().values();
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
