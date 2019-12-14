/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.ts;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.Period;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.joda.time.Duration;
import org.opennms.netmgt.measurements.api.FetchResults;
import org.opennms.netmgt.measurements.model.QueryMetadata;
import org.opennms.netmgt.measurements.model.QueryResource;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timescale.support.TimescaleUtils;
import org.opennms.netmgt.timeseries.api.TimeSeriesStorage;
import org.opennms.netmgt.timeseries.api.domain.Metric;
import org.opennms.netmgt.timeseries.api.domain.Sample;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.swrve.ratelimitedlogger.RateLimitedLog;

@Service
public class TimescaleStorage implements TimeSeriesStorage {

    private static final Logger LOG = LoggerFactory.getLogger(TimescaleStorage.class);

    private static final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.standardSeconds(30))
            .build();


    private DataSource dataSource;

    private Connection connection;

    private int maxBatchSize = 100;

    @Autowired
    public TimescaleStorage(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void store(List<Sample> entries) throws StorageException {
        String sql = "INSERT INTO timeseries(time, key, value)  values (?, ?, ?)";

        try {
            if(this.connection == null) {
                this.connection = this.dataSource.getConnection();
            }
            PreparedStatement ps = connection.prepareStatement(sql);
            // Partition the samples into collections smaller then max_batch_size
            for (List<Sample> batch : Lists.partition(entries, maxBatchSize)) {
                try {
                        LOG.debug("Inserting {} samples", batch.size());
                        // m_sampleRepository.insert(batch);
                        for (Sample sample: batch) {
                            ps.setTimestamp(1, new Timestamp(sample.getTime().toEpochMilli()));
                            ps.setString(2, sample.getMetric().getTagsByKey("resourceId").iterator().next().getValue()); // TODO Patrick: this should be getKey instead
                            ps.setDouble(3, sample.getValue());
                            ps.addBatch();
                        }
                        ps.executeBatch();

                    if (LOG.isDebugEnabled()) {
                        String keys = batch.stream()
                                .map(s -> s.getMetric().getKey())
                                .distinct()
                                .collect(Collectors.joining(", "));
                        LOG.debug("Successfully inserted samples for resources with ids {}", keys);
                    }
                } catch (Throwable t) {
                    RATE_LIMITED_LOGGER.error("An error occurred while inserting samples. Some sample may be lost.", t);
                }
            }
            ps.close();
        } catch(SQLException e) {
            throw new StorageException(e);
        }
    }

    public List<Metric> getAllMetrics() throws StorageException {
        try {
        // TODO: Patrick: do db stuff properly
        final String sql = "select distinct key from timeseries";
        if(connection == null) {
            this.connection = this.dataSource.getConnection();

        }
        PreparedStatement statement = connection.prepareStatement(sql);

        ResultSet rs = statement.executeQuery();
        List<Metric> metrics = new ArrayList<>(rs.getFetchSize());
        while(rs.next()){
            String resourceString = rs.getString("key");
            Metric metric = Metric.builder()
                    .tag("resourceId", resourceString)
                    .tag(Metric.MandatoryTag.unit.name(), "ms") // TODO: Patrick: remove hard coded value
                    .tag(Metric.MandatoryTag.mtype.name(), "gauge").build(); // TODO: Patrick: remove hard coded value
            metrics.add(metric);
        }
        rs.close();
        return metrics;
        } catch(SQLException e) {
            throw new StorageException(e);
        }
    }

    @Override
    public List<Sample> getTimeseries(Metric metric, Instant start, Instant end, java.time.Duration step) {

        // TODO: Patrick: do db stuff properly
        ArrayList<Sample> samples;
        try {
            long stepInSeconds = step.getSeconds() ;
            String resourceId = "response:127.0.0.1:response-time"; // TODO Patrick: deduct from sources
            String sql = "SELECT time_bucket_gapfill('" + stepInSeconds + " Seconds', time) AS step, min(value), avg(value), max(value) FROM timeseries where " +
                    "key=? AND time > ? AND time < ? GROUP BY step ORDER BY step ASC";
//            if(maxrows>0) {
//                sql = sql + " LIMIT " + maxrows;
//            }
            if(connection == null) {
                this.connection = this.dataSource.getConnection();

            }
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, resourceId);
            statement.setTimestamp(2, new java.sql.Timestamp(start.toEpochMilli()));
            statement.setTimestamp(3, new java.sql.Timestamp(end.toEpochMilli()));
            ResultSet rs = statement.executeQuery();

            samples = new ArrayList<>();
            while(rs.next()) {
                long timestamp = rs.getTimestamp("step").getTime();
                samples.add(new Sample(metric, Instant.ofEpochMilli(timestamp), rs.getDouble("avg")));
            }

            rs.close();
        } catch (SQLException e) {
            LOG.error("Could not retrieve FetchResults", e);
            throw new RuntimeException(e);
        }
        return samples;
    }
}
