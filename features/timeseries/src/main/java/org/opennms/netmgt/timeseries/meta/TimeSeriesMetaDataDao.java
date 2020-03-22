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

import static org.opennms.netmgt.timeseries.integration.support.TimeseriesUtils.toResourceId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.joda.time.Duration;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.swrve.ratelimitedlogger.RateLimitedLog;

@Service
public class TimeSeriesMetaDataDao {

    private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesMetaDataDao.class);


    private static final RateLimitedLog RATE_LIMITED_LOGGER = RateLimitedLog
            .withRateLimit(LOG)
            .maxRate(5).every(Duration.standardSeconds(30))
            .build();

    private final DataSource dataSource;

    private int maxBatchSize = 100; // TODO Patrick: do we need to make value configurable?

    @Autowired
    public TimeSeriesMetaDataDao(final DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void store(final Collection<MetaData> metaDataCollection) throws SQLException {
        Objects.requireNonNull(metaDataCollection);

        // TODO Patrick add caching and only push changes, similar to GuavaSearchableResourceMetadataCache
        final String sql = "INSERT INTO timeseries_meta(resourceid, name, value)  values (?, ?, ?) ON CONFLICT (resourceid, name) DO UPDATE SET value=?";
        final DBUtils db = new DBUtils(this.getClass());
        try {

            Connection connection = this.dataSource.getConnection();
            db.watch(connection);

            PreparedStatement ps = connection.prepareStatement(sql);
            db.watch(ps);

            LOG.debug("Inserting {} attributes", metaDataCollection.size());
            for (MetaData metaData : metaDataCollection) {
                ps.setString(1, metaData.getResourceId());
                ps.setString(2, metaData.getName());
                ps.setString(3, metaData.getValue());
                ps.setString(4, metaData.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
            ps.close();
        } finally{
            db.cleanUp();
        }
    }

    public Map<String, String> getForResourcePath(final ResourcePath path) throws StorageException {
        Objects.requireNonNull(path);
        String resourceId = toResourceId(path);

        Map<String, String> metaData;
        final DBUtils db = new DBUtils(this.getClass());
        try {

            String sql = "SELECT name, value FROM timescale_meta where resourceid = ?";

            final Connection connection = this.dataSource.getConnection();
            db.watch(connection);

            PreparedStatement statement = connection.prepareStatement(sql);
            db.watch(statement);
            statement.setString(1, toResourceId(path));
            ResultSet rs = statement.executeQuery();
            db.watch(rs);
            metaData = new HashMap<>();
            while (rs.next()) {
                String name = rs.getString("name");
                String value = rs.getString("value");
                metaData.put(name, value);
            }
            rs.close();
        } catch (SQLException e) {
            LOG.error("Could not retrieve meta data for resourceId={}", resourceId, e);
            db.cleanUp();
            throw new StorageException(e);
        }
        return metaData;
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
