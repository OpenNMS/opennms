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

package org.opennms.netmgt.timeseries.impl.influxdb;

import static org.opennms.netmgt.timeseries.impl.influxdb.TransformUtil.metricKeyToInflux;
import static org.opennms.netmgt.timeseries.impl.influxdb.TransformUtil.tagValueFromInflux;
import static org.opennms.netmgt.timeseries.impl.influxdb.TransformUtil.tagValueToInflux;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.netmgt.timeseries.api.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.api.domain.Metric;
import org.opennms.netmgt.timeseries.api.domain.Sample;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.opennms.netmgt.timeseries.api.domain.Tag;
import org.opennms.netmgt.timeseries.api.domain.TimeSeriesFetchRequest;
import org.opennms.netmgt.timeseries.integration.CommonTagNames;
import org.springframework.stereotype.Service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.InfluxDBClientOptions;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

/**
 * Implementation of TimeSeriesStorage that uses InfluxStorage.
 *
 * Design choices:
 * - we fill the _measurement column with the Metrics key
 * - we prefix the tag key with the tag type ('intrinsic' or 'meta')
 */
@Service
public class InfluxStorage implements TimeSeriesStorage {

    private InfluxDBClient influxDBClient;

    // TODO Patrick: externalize config
    private String configBucket = "opennms";
    private String configOrg = "opennms";
    private String configToken = "5DOQmVNBf1olXG2Iba0WSPfEqD-mt1dhoJctvX4HjLPRufNqjpb4UJsDyVCPJUkDzEXTnN0QrGOaeXGwIdTUxQ==";
    private String configUrl = "http://localhost:9999";

    public InfluxStorage() {
        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .bucket(configBucket)
                .connectionString(configUrl)
                .org(configOrg)
                .url(configUrl)
                .authenticateToken(configToken.toCharArray())
                .build();
        influxDBClient = InfluxDBClientFactory.create(options);
        // TODO Patrick: do we need to enable batch? How?
    }

    @Override
    public void store(List<Sample> samples) {
        for(Sample sample: samples) {
            Point point = Point
                    .measurement(metricKeyToInflux(sample.getMetric().getKey())) // make sure the measurement has only allowed characters
                    .addField("value", sample.getValue())
                    .time(sample.getTime().toEpochMilli(), WritePrecision.MS);
            storeTags(point, Metric.TagType.intrinsic, sample.getMetric().getTags());
            storeTags(point, Metric.TagType.meta, sample.getMetric().getMetaTags());
            influxDBClient.getWriteApi().writePoint(configBucket, configOrg, point);
        }
    }

    private void storeTags(final Point point, final Metric.TagType tagType, final Collection<Tag> tags) {
        for(final Tag tag : tags) {
            String value = tag.getValue();
            value = tagValueToInflux(value); // Influx has a problem with a colon in a tag value if we query for it
            point.addTag(toClassifiedTagKey(tagType, tag), value);
        }
    }

    private String toClassifiedTagKey(final Metric.TagType tagType, final Tag tag) {
         return tagType.name() + "_" + tag.getKey();
    }

    @Override
    public List<Metric> getMetrics(Collection<Tag> tags) throws StorageException {

        // TODO: Patrick: The code works but is probaply not efficient enough, we should optimize this query since
        // it gets way too much (redundant) data.
        // I am not sure how - the influx documentation doesn't seem to be up to date / correct:
        // https://www.influxdata.com/blog/schema-queries-in-ifql/

        String query = "from(bucket:\"opennms\")\n" +
                "  |> range(start:-5y)\n" +
                "  |> keys()";

        final List<FluxTable> keys = influxDBClient.getQueryApi().query(query);
        List<Metric> allMetrics = keys.stream()
                .map(FluxTable::getRecords)
                .flatMap(Collection::stream)
                .map(FluxRecord::getValues)
                .filter(m -> m.get("_measurement").toString().contains(CommonTagNames.resourceId))
                .map(this::createMetricFromMap)
                .filter(metric -> metric.getTags().containsAll(tags))
                .distinct()
                .collect(Collectors.toList());

        return allMetrics;
    }

    /** Restore the metric from the tags we get out of InfluxDb */
    private Metric createMetricFromMap(final Map<String, Object> map) {
        Metric.MetricBuilder metric = Metric.builder();
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            getIfMatching(Metric.TagType.intrinsic, entry).ifPresent(metric::tag);
            getIfMatching(Metric.TagType.meta, entry).ifPresent(metric::metaTag);
        }
        return metric.build();
    }

    private Optional<Tag> getIfMatching(final Metric.TagType tagType, final Map.Entry<String, Object> entry) {
        // Check if the key starts with the prefix. If so it is an opennms key, if not something InfluxDb specific and
        // we can ignore it.
        final String prefix = tagType.name() + '_';
        String key = entry.getKey();

        if(key.startsWith(prefix)) {
            key = key.substring(prefix.length());
            String value = tagValueFromInflux(entry.getValue().toString()); // convert
            return Optional.of(new Tag(key, value));
        }
        return Optional.empty();
    }

    @Override
    public List<Sample> getTimeseries(TimeSeriesFetchRequest request) throws StorageException {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneId.of("UTC"));

        // TODO: Patrick add aggregation function to query
        String query = "from(bucket:\"" + this.configBucket + "\")\n" +
                " |> range(start:" + format.format(request.getStart()) + ", stop:" + format.format(request.getEnd()) + ")\n" +
                " |> filter(fn:(r) => r._measurement == \"" + metricKeyToInflux(request.getMetric().getKey()) + "\")\n" +
                " |> filter(fn: (r) => r._field == \"value\")";
        List<FluxTable> tables = influxDBClient.getQueryApi().query(query);

        final List<Sample> samples = new ArrayList<>();
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord record : records) {
                Sample sample = Sample.builder()
                        .metric(request.getMetric())
                        .time(record.getTime())
                        .value((Double)record.getValue())
                        .build();
                samples.add(sample);
            }
        }
        return samples;
    }

    @Override
    public void delete(Metric metric) throws StorageException {
        DeletePredicateRequest predicate = new DeletePredicateRequest().predicate("_measurement=\"" + metricKeyToInflux(metric.getKey()) + "\"");
        influxDBClient.getDeleteApi().delete(predicate, configBucket, configOrg);
    }
}
