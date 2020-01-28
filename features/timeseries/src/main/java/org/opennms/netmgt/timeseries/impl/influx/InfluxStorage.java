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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.netmgt.timeseries.api.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.api.domain.Metric;
import org.opennms.netmgt.timeseries.api.domain.Sample;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.opennms.netmgt.timeseries.api.domain.Tag;
import org.opennms.netmgt.timeseries.api.domain.TimeSeriesFetchRequest;
import org.springframework.stereotype.Service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;

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
    private String configBucket = "my-bucket";
    private String configOrg = "my-org";

    public InfluxStorage() {
        // TODO Patrick: externalize configuration
        influxDBClient = InfluxDBClientFactory.create("http://localhost:9999", "my-token".toCharArray());
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
            point.addTag(tag.getKey(), toClassifiedTagValue(tagType, tag));
        }
    }

    private String toClassifiedTagValue(final Metric.TagType tagType, final Tag tag) {
         return tagType.name() + "_" + tag.getValue();
    }

    @Override
    public List<Metric> getMetrics(Collection<Tag> tags) throws StorageException {

        String query = "from(bucket:\"\" + this.configBucket + \"\")" +
                "|> range(start:-24h)" +
                "|> group(by:[\"_measurement\"])" +
                "|> distinct(column:\"_measurement\")" +
                "|> group(none:true)";

        // TODO Patrick: Find a way to restore a metric
        // https://www.influxdata.com/blog/schema-queries-in-ifql/
        return new ArrayList<>();
    }

    @Override
    public List<Sample> getTimeseries(TimeSeriesFetchRequest request) throws StorageException {
        String query = "from(bucket:\"" + this.configBucket + "\")\n" +
                " |> filter(fn: (r) => (r[\"_measurement\"] == \"" + request.getMetric().getKey() + "\"))" +
                " |> range(start: time(v: " + request.getStart().toEpochMilli() * 1000000 + "), stop: time(v: " + request.getEnd().toEpochMilli() * 1000000 + "))\n";
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
