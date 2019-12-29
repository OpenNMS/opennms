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

package org.opennms.netmgt.timeseries.meta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.joda.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.swrve.ratelimitedlogger.RateLimitedLog;

@Service
public class TimeSeriesMetaDataDao {

    private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesMetaDataDao.class);


    private static final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.standardSeconds(30))
            .build();

    private final DataSource dataSource;

    private Connection connection;

    private int maxBatchSize = 100; // TODO Patrick: do we need to make value configurable?

    @Autowired
    public TimeSeriesMetaDataDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void store(List<MetaData> metaData) throws SQLException {

        // TODO: Patrick: can we simplify this? Right now it reflects the fields in StringAttribute

        String sql = "INSERT INTO timeseries_meta(id, value)  values (?, ?)";

        if (this.connection == null) {
            this.connection = this.dataSource.getConnection();
        }

        PreparedStatement ps = connection.prepareStatement(sql);
        // Partition the samples into collections smaller then max_batch_size
        for (List<MetaData> batch : Lists.partition(metaData, maxBatchSize)) {
            try {
                LOG.debug("Inserting {} attributes", batch.size());
                // m_sampleRepository.insert(batch);
                for (MetaData data : batch) {
                    ps.setString(1, data.getKey());
                    ps.setString(2, data.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            } catch (Throwable t) {
                RATE_LIMITED_LOGGER.error("An error occurred while inserting samples. Some sample may be lost.", t);
            }
        }
        ps.close();
    }

//    public void store(List<StringAttribute> attributes) throws SQLException {
//
//        // TODO: Patrick: can we simplify this? Right now it reflects the fields in StringAttribute
//
//        String sql = "INSERT INTO timeseries_meta(group, identifier, name, value, type)  values (?, ?, ?, ?, ?)";
//
//        if (this.connection == null) {
//            this.connection = this.dataSource.getConnection();
//        }
//
//        PreparedStatement ps = connection.prepareStatement(sql);
//        // Partition the samples into collections smaller then max_batch_size
//        for (List<StringAttribute> batch : Lists.partition(attributes, maxBatchSize)) {
//            try {
//                LOG.debug("Inserting {} attributes", batch.size());
//                // m_sampleRepository.insert(batch);
//                for (StringAttribute attribute : batch) {
//                    ps.setString(1, attribute.getGroup());
//                    ps.setString(2, attribute.getIdentifier()); // TODO Patrick: this should be getKey instead
//                    ps.setString(3, attribute.getName());
//                    ps.setString(4, attribute.getValue());
//                    ps.setString(5, attribute.getType().getName());
//                    ps.addBatch();
//                }
//                ps.executeBatch();
//            } catch (Throwable t) {
//                RATE_LIMITED_LOGGER.error("An error occurred while inserting samples. Some sample may be lost.", t);
//            }
//        }
//        ps.close();
//    }
}
