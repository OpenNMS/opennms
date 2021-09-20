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

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Sample;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableSample;
import org.opennms.newts.api.Context;
import org.opennms.newts.api.Counter;
import org.opennms.newts.api.Gauge;
import org.opennms.newts.api.Measurement;
import org.opennms.newts.api.MetricType;
import org.opennms.newts.api.Resource;
import org.opennms.newts.api.Results;
import org.opennms.newts.api.Timestamp;
import org.opennms.newts.api.ValueType;

import com.google.common.base.Optional;

/** Provides methods to convert between the Timeseries API world and Newts. */
public class NewtsConverterUtils {

    public static Iterator<Results.Row<org.opennms.newts.api.Sample>> samplesToNewtsRowIterator(List<Sample> allSamples) {
        return allSamples
                .stream()
                .map(NewtsConverterUtils::sampleToRow)
                .collect(Collectors.toList())
                .iterator();
    }

    private static Results.Row<org.opennms.newts.api.Sample> sampleToRow(final Sample sample) {

        Optional<Map<String, String>> resourceAttributes = sample.getMetric().getExternalTags().isEmpty() ?
                Optional.absent() : Optional.of(asMap(sample.getMetric().getMetaTags()));

        final Timestamp timestamp = Timestamp.fromEpochMillis(sample.getTime().toEpochMilli());
        final Context context = new Context("not relevant");
        final Resource resource = new Resource(sample.getMetric().getFirstTagByKey(IntrinsicTagNames.resourceId).getValue(), resourceAttributes);
        final String name = sample.getMetric().getFirstTagByKey(IntrinsicTagNames.name).getValue();
        final MetricType type = toNewts(Metric.Mtype.valueOf(sample.getMetric().getFirstTagByKey(IntrinsicTagNames.mtype).getValue()));
        final ValueType<?> value = toNewtsValue(sample);
        final Map<String, String> attributes = new HashMap<>();

        org.opennms.newts.api.Sample newtsSample = new org.opennms.newts.api.Sample(
                timestamp, context, resource, name, type, value, attributes);

        final Results.Row<org.opennms.newts.api.Sample> row = new Results.Row<>(timestamp, resource);
        row.addElement(newtsSample);
        return row;
    }

    private static MetricType toNewts(Metric.Mtype type) {
        if(Metric.Mtype.count == type) {
            return MetricType.COUNTER;
        } else if(Metric.Mtype.gauge == type) {
            return MetricType.GAUGE;
        } else  {
            throw new IllegalArgumentException(String.format("I don't know how to map %s to MetricType", type));
        }
    }

    private static ValueType<?> toNewtsValue(final Sample sample) {
        final Metric.Mtype mtype = Metric.Mtype.valueOf(sample.getMetric().getFirstTagByKey(IntrinsicTagNames.mtype).getValue());
        if(Metric.Mtype.count == mtype) {
            return new Counter(sample.getValue().longValue());
        } else if(Metric.Mtype.gauge == mtype) {
            return new Gauge(sample.getValue());
        } else  {
            throw new IllegalArgumentException(String.format("I don't know how to map %s to ValueType", mtype));
        }
    }

    // this only works if we have exactly one column in a row. */
    public static Sample toTimeseriesSample(final Results.Row<Measurement> row, final Metric metric) {

        // check preconditions
        final int sizeOfRow = row.getElements().size();
        if(row.getElements().size() != 1) {
            throw new IllegalArgumentException(String.format("expected exactly one column in row but was %s", sizeOfRow));
        }

        // and convert
        Measurement measurement = row.getElements().iterator().next();
        return ImmutableSample.builder()
                .metric(metric)
                .time(toInstant(measurement.getTimestamp()))
                .value(measurement.getValue())
                .build();
    }

    public static Instant toInstant(final Timestamp timestamp) {
        return Instant.ofEpochMilli(timestamp.asMillis());
    }

    public static Map<String, String> asMap (Collection<Tag> tags) {
        Map<String, String> map = new HashMap<>();
        for (Tag tag : tags) {
            map.put(tag.getKey(), tag.getValue());
        }
        return map;
    }
}
