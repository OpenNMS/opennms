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

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.timeseries.DataPoint;
import org.opennms.integration.api.v1.timeseries.IntrinsicTagNames;
import org.opennms.integration.api.v1.timeseries.MetaTagNames;
import org.opennms.integration.api.v1.timeseries.Metric;
import org.opennms.integration.api.v1.timeseries.Tag;
import org.opennms.integration.api.v1.timeseries.TimeSeriesData;
import org.opennms.integration.api.v1.timeseries.immutables.ImmutableDataPoint;
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

    public static Iterator<Results.Row<org.opennms.newts.api.Sample>> samplesToNewtsRowIterator(TimeSeriesData allDataPoints) {
        return allDataPoints
                .getDataPoints()
                .stream()
                .map(d -> dataPointToRow(allDataPoints.getMetric(), d))
                .collect(Collectors.toList())
                .iterator();
    }

    private static Results.Row<org.opennms.newts.api.Sample> dataPointToRow(final Metric metric, final DataPoint dataPoint) {

        Optional<Map<String, String>> resourceAttributes = metric.getExternalTags().isEmpty() ?
                Optional.absent() : Optional.of(asMap(metric.getMetaTags()));

        final Timestamp timestamp = Timestamp.fromEpochMillis(dataPoint.getTime().toEpochMilli());
        final Context context = new Context("not relevant");
        final Resource resource = new Resource(metric.getFirstTagByKey(IntrinsicTagNames.resourceId).getValue(), resourceAttributes);
        final String name = metric.getFirstTagByKey(IntrinsicTagNames.name).getValue();
        final MetricType type = toNewts(Metric.Mtype.valueOf(metric.getFirstTagByKey(MetaTagNames.mtype).getValue()));
        final ValueType<?> value = toNewtsValue(metric, dataPoint);
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

    private static ValueType<?> toNewtsValue(final Metric metric, final DataPoint dataPoint) {
        final Metric.Mtype mtype = Metric.Mtype.valueOf(metric.getFirstTagByKey(MetaTagNames.mtype).getValue());
        if(Metric.Mtype.count == mtype) {
            return new Counter(dataPoint.getValue().longValue());
        } else if(Metric.Mtype.gauge == mtype) {
            return new Gauge(dataPoint.getValue());
        } else  {
            throw new IllegalArgumentException(String.format("I don't know how to map %s to ValueType", mtype));
        }
    }

    // this only works if we have exactly one column in a row. */
    public static DataPoint toTimeSeriesDataPoint(final Results.Row<Measurement> row) {

        // check preconditions
        final int sizeOfRow = row.getElements().size();
        if(row.getElements().size() != 1) {
            throw new IllegalArgumentException(String.format("expected exactly one column in row but was %s", sizeOfRow));
        }

        // and convert
        Measurement measurement = row.getElements().iterator().next();
        return ImmutableDataPoint.builder()
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
