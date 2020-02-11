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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.timeseries.api.domain.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * TimeSeriesMetaDataDao stores string values associated with resourceids in the database. It leverages a Guava cache.
 * Design choice: for speed and high throughput we don't keep the cache and the database 100% in sync at the time of writing.
 * Latest 5 minutes after writing they will be the same.
 */
@Service
public class TimeSeriesMetaDataDao {

    private static final Logger LOG = LoggerFactory.getLogger(TimeSeriesMetaDataDao.class);

    private final DataSource dataSource;

    private final Map<String, Map<String, String>> cache; // resourceId, Map<name, value>

    @Autowired
    public TimeSeriesMetaDataDao(final DataSource dataSource) {
        this.dataSource = dataSource;
        Cache<String, Map<String, String>> cache = CacheBuilder.newBuilder()
                .maximumSize(10000)
                .expireAfterWrite(5, TimeUnit.MINUTES) // to make sure cache and db are in sync
                .build();
        this.cache = cache.asMap();
    }

    public void store(final Collection<MetaData> metaDataCollection) throws SQLException {
        Objects.requireNonNull(metaDataCollection);
        Set<MetaData> uncached = new HashSet<>();

        // find all MetaData that is not present in the cache
        for(MetaData meta : metaDataCollection) {
            if(!Optional
                    .ofNullable(cache.get(meta.getResourceId()))
                    .map(entry -> entry.get(meta.getName()))
                    .isPresent()) {
                uncached.add(meta);
            }
        }
        storeUncached(uncached);
    }

    public void storeUncached(final Collection<MetaData> metaDataCollection) throws SQLException {
        Objects.requireNonNull(metaDataCollection);

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
        } finally{
            db.cleanUp();
        }
    }

    public Map<String, String> getForResourcePath(final ResourcePath path) throws StorageException {
        Objects.requireNonNull(path);
        String resourceId = toResourceId(path);

        // check cache first
        Map<String, String> cachedEntry = this.cache.get(resourceId);
        if(cachedEntry != null) {
            return cachedEntry;
        }

        // not in cache -> look in database
        Map<String, String> metaData;
        final DBUtils db = new DBUtils(this.getClass());
        try {

            String sql = "SELECT name, value FROM timeseries_meta where resourceid = ?";

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
        } catch (SQLException e) {
            LOG.error("Could not retrieve meta data for resourceId={}", resourceId, e);
            throw new StorageException(e);
        } finally {
            db.cleanUp();
        }

        // put in cache
        this.cache.put(resourceId, metaData);

        return metaData;
    }

    @RequiredArgsConstructor
    @Data
    private final static class MetaDataKey {
        private final String resourceId;
        private final String name;
    }

}
