/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.distributed.kvstore.cassandra;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;

import org.opennms.features.distributed.cassandra.api.CassandraSchemaManagerFactory;
import org.opennms.features.distributed.cassandra.api.CassandraSession;
import org.opennms.features.distributed.cassandra.api.CassandraSessionFactory;
import org.opennms.features.distributed.kvstore.api.AbstractSerializedKVStore;
import org.opennms.features.distributed.kvstore.api.SerializedKVStore;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * A {@link SerializedKVStore key value store} that is backed by Cassandra.
 * <p>
 * This implementation persists values that implement {@link Serializable} by using vanilla Java serialization.
 * <p>
 * This implementation persists key values in a specific table that will be created in a keyspace dictated by the
 * {@link org.opennms.features.distributed.cassandra.api.CassandraSchemaManager schema manager}.
 * <p>
 * This implementation does not initiate its own {@link com.datastax.driver.core.Session Cassandra session} and must be
 * provided with one via a {@link CassandraSessionFactory session factory}.
 */
public class CassandraKVStore extends AbstractSerializedKVStore<ByteBuffer, Serializable> {
    private static final String KEY_COLUMN = "key";
    private static final String VALUE_COLUMN = "value";
    private static final String TIMESTAMP_COLUMN = "lastUpdated";
    private static final String TABLE_NAME = "kvstore";

    private final CassandraSession session;
    private final PreparedStatement insertStmt;
    private final PreparedStatement selectStmt;
    private final PreparedStatement timestampStmt;

    public CassandraKVStore(CassandraSessionFactory sessionFactory,
                            CassandraSchemaManagerFactory cassandraSchemaManagerFactory) throws IOException {
        super(CassandraJavaSerializationStrategy.INSTANCE, System::currentTimeMillis);
        Objects.requireNonNull(sessionFactory);
        Objects.requireNonNull(cassandraSchemaManagerFactory);

        cassandraSchemaManagerFactory.getSchemaManager()
                .create(() -> getClass().getResourceAsStream("/cql/kv.cql"));
        session = sessionFactory.getSession();
        insertStmt = session.prepare(String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)", TABLE_NAME,
                KEY_COLUMN,
                VALUE_COLUMN, TIMESTAMP_COLUMN));
        selectStmt = session.prepare(String.format("SELECT %s FROM %s where %s = ?", VALUE_COLUMN, TABLE_NAME,
                KEY_COLUMN));
        timestampStmt = session.prepare(String.format("SELECT %s FROM %s where %s = ?", TIMESTAMP_COLUMN, TABLE_NAME,
                KEY_COLUMN));
    }

    @Override
    protected void putSerializedValueWithTimestamp(String key, ByteBuffer serializedValue, long timestamp) {
        // Cassandra will throw a runtime exception here if the execution fails
        session.execute(insertStmt.bind(key, serializedValue, new Date(timestamp)));
    }

    @Override
    protected Optional<ByteBuffer> getSerializedValue(String key) {
        ByteBuffer serializedValue;

        // Cassandra will throw a runtime exception here if the execution fails
        ResultSet resultSet = session.execute(selectStmt.bind(key));
        Row row = resultSet.one();

        // Could not find the key
        if (row == null) {
            return Optional.empty();
        }

        serializedValue = ByteBuffer.wrap(row.getBytes(VALUE_COLUMN).array());

        return Optional.of(serializedValue);
    }

    @Override
    public OptionalLong getLastUpdated(String key) {
        // Cassandra will throw a runtime exception here if the execution fails
        ResultSet resultSet = session.execute(timestampStmt.bind(key));
        Row row = resultSet.one();

        // Could not find the key
        if (row == null) {
            return OptionalLong.empty();
        }

        Date lastUpdated = row.getTimestamp(TIMESTAMP_COLUMN);

        return OptionalLong.of(lastUpdated.getTime());
    }

    @Override
    protected CompletableFuture<Void> putSerializedValueWithTimestampAsync(String key, ByteBuffer serializedValue,
                                                                           long timestamp) {
        CompletableFuture<Void> putFuture = new CompletableFuture<>();

        try {
            // Cassandra will throw a runtime exception here if the execution fails
            session.executeAsync(insertStmt.bind(key, serializedValue, new Date(timestamp)))
                    .addListener(() -> putFuture.complete(null), MoreExecutors.directExecutor());
        } catch (RuntimeException e) {
            putFuture.completeExceptionally(e);
        }

        return putFuture;
    }

    @Override
    protected CompletableFuture<Optional<ByteBuffer>> getSerializedValueAsync(String key) {
        CompletableFuture<Optional<ByteBuffer>> getFuture = new CompletableFuture<>();

        try {
            // Cassandra will throw a runtime exception here if the execution fails
            ResultSetFuture resultSetFuture = session.executeAsync(selectStmt.bind(key));
            resultSetFuture.addListener(() -> processGetFutureResult(getFuture, resultSetFuture.getUninterruptibly()),
                    MoreExecutors.directExecutor());
        } catch (RuntimeException e) {
            getFuture.completeExceptionally(e);
        }

        return getFuture;
    }

    @Override
    public CompletableFuture<OptionalLong> getLastUpdatedAsync(String key) {
        CompletableFuture<OptionalLong> tsFuture = new CompletableFuture<>();

        try {
            // Cassandra will throw a runtime exception here if the execution fails
            ResultSetFuture resultSetFuture = session.executeAsync(timestampStmt.bind(key));
            resultSetFuture.addListener(() -> processLastUpdatedFutureResult(tsFuture,
                    resultSetFuture.getUninterruptibly()),
                    MoreExecutors.directExecutor());
        } catch (RuntimeException e) {
            tsFuture.completeExceptionally(e);
        }

        return tsFuture;
    }

    private void processGetFutureResult(CompletableFuture<Optional<ByteBuffer>> future,
                                        ResultSet resultSet) {
        Row row = resultSet.one();

        // Could not find the key
        if (row == null) {
            future.complete(Optional.empty());
            return;
        }

        future.complete(Optional.of(ByteBuffer.wrap(row.getBytes(VALUE_COLUMN).array())));
    }

    private void processLastUpdatedFutureResult(CompletableFuture<OptionalLong> future, ResultSet resultSet) {
        Row row = resultSet.one();

        // Could not find the key
        if (row == null) {
            future.complete(OptionalLong.empty());
            return;
        }

        Date lastUpdated = row.getTimestamp(TIMESTAMP_COLUMN);

        future.complete(OptionalLong.of(lastUpdated.getTime()));
    }
}
