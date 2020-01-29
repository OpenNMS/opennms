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

package org.opennms.netmgt.timeseries.impl.influx;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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

import okhttp3.OkHttpClient;

/**
 * Implementation of TimeSeriesStorage that uses InfluxStorage.
 * Design choices:
 * - we fill the _measurement column with the Metrics key
 * - we prepend the tag values with the tag type (intrinsic or meta)
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
        // TODO Patrick: externalize configuration
        // OkHttpClient okHttpClient = new OkHttpClient.Builder().

        InfluxDBClientOptions options = InfluxDBClientOptions.builder()
                .bucket(configBucket)
                .connectionString(configUrl)
                .org(configOrg)
                .url(configUrl)
                .authenticateToken(configToken.toCharArray())
                .build();
        influxDBClient = InfluxDBClientFactory.create(options);
        // TODO Patrick: enable batch?
    }

    @Override
    public void store(List<Sample> samples) throws StorageException {
        for(Sample sample: samples) {
            Point point = Point.measurement(sample.getMetric().getKey())
                    .addField("value", sample.getValue())
                    .time(sample.getTime().toEpochMilli(), WritePrecision.MS);
            storeTags(point, Metric.TagType.intrinsic, sample.getMetric().getTags());
            storeTags(point, Metric.TagType.meta, sample.getMetric().getMetaTags());
            influxDBClient.getWriteApi().writePoint(configBucket, configOrg, point);
        }
    }

    private void storeTags(final Point point, final Metric.TagType tagType, final Collection<Tag> tags) {
        for(final Tag tag : tags) {
            point.addTag(toClassifiedTagKey(tagType, tag), tag.getValue());
        }
    }

    private String toClassifiedTagKey(final Metric.TagType tagType, final Tag tag) {
         return tagType.name() + "_" + tag.getKey();
    }

    @Override
    public List<Metric> getMetrics(Collection<Tag> tags) throws StorageException {

        String query = "from(bucket:\"opennms\")\n" +
                "  |> range(start:-24h)\n" +
                "  |> group(columns:[\"_measurement\"])\n" +
                "  |> distinct(column:\"_measurement\")";

        // TODO Patrick: Find a way to restore a metric
        // https://www.influxdata.com/blog/schema-queries-in-ifql/

        final List<FluxTable> tables = influxDBClient.getQueryApi().query(query);
        List<?> allMetricKeys = tables.stream()
                .map(FluxTable::getRecords)
                .filter(l -> !l.isEmpty()) // just to be sure
                .map(t -> t.get(0))
                .map(FluxRecord::getMeasurement)
                .filter(Objects::nonNull)
                .filter(s -> s.contains(CommonTagNames.resourceId))
                .collect(Collectors.toList());

        query = "from(bucket:\"opennms\")\n" +
                "  |> range(start:-24h)\n" +
//                "  |> group(columns:[\"_measurement\"])\n" +
//                "  |> distinct(column:\"_measurement\"\n)" +
                // "  |> filter(fn:(r) => r._measurement == \"go_gc_duration_seconds\")\n" +
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

    private Metric createMetricFromMap(final Map<String, Object> map) {
        Metric.MetricBuilder metric = Metric.builder();
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            getIfMatching(Metric.TagType.intrinsic, entry).ifPresent(metric::tag);
            getIfMatching(Metric.TagType.meta, entry).ifPresent(metric::metaTag);
        }
        return metric.build();
    }

    private Optional<Tag> getIfMatching(final Metric.TagType tagType, final Map.Entry<String, Object> entry) {
        // Check if the key starts with the prefix. If so it is an opennms key, if not something InfluxDb specific
        final String prefix = tagType.name() + '_';
        if(entry.getKey().startsWith(prefix)) {
            return Optional.of(new Tag(entry.getKey().substring(prefix.length()), entry.getValue().toString()));
        }
        return Optional.empty();
    }

    @Override
    public List<Sample> getTimeseries(TimeSeriesFetchRequest request) throws StorageException {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.systemDefault());

        String query = "from(bucket:\"" + this.configBucket + "\")\n" +
                " |> filter(fn: (r) => (r[\"_measurement\"] == \"" + request.getMetric().getKey() + "\"))" +
                " |> range(start:" + format.format(request.getStart()) + ", stop:" + format.format(request.getEnd()) + ")\n";
        List<FluxTable> tables = influxDBClient.getQueryApi().query(query);
        for (FluxTable fluxTable : tables) {
            List<FluxRecord> records = fluxTable.getRecords();
            for (FluxRecord fluxRecord : records) {
                System.out.println(fluxRecord.getTime() + ": " + fluxRecord.getValueByKey("_value"));
            }
        }
        return new ArrayList<>();
    }

    @Override
    public void delete(Metric metric) throws StorageException {
        DeletePredicateRequest predicate = new DeletePredicateRequest().predicate("_measurement=\"" + metric.getKey() + "\"");
        influxDBClient.getDeleteApi().delete(predicate, configBucket, configOrg);
    }
}
