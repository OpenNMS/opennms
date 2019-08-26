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

package org.opennms.features.distributed.kvstore.blob.cassandra;

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
import org.opennms.features.distributed.kvstore.api.AbstractKeyValueStore;
import org.opennms.features.distributed.kvstore.api.BlobStore;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Statement;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * A {@link BlobStore} that is backed by Cassandra.
 * <p>
 * This implementation persists values that implement {@link Serializable} by using vanilla Java serialization.
 * <p>
 * This implementation persists key values in a specific table that will be created in a keyspace dictated by the
 * {@link org.opennms.features.distributed.cassandra.api.CassandraSchemaManager schema manager}.
 * <p>
 * This implementation does not initiate its own {@link com.datastax.driver.core.Session Cassandra session} and must be
 * provided with one via a {@link CassandraSessionFactory session factory}.
 */
public class CassandraBlobStore extends AbstractKeyValueStore<byte[]> implements BlobStore {
    private static final String KEY_COLUMN = "key";
    private static final String CONTEXT_COLUMN = "context";
    private static final String VALUE_COLUMN = "value";
    private static final String TIMESTAMP_COLUMN = "lastUpdated";
    private static final String TABLE_NAME = "kvstore_blob";

    private final CassandraSession session;
    private final PreparedStatement insertStmt;
    private final PreparedStatement insertWithTtlStmt;
    private final PreparedStatement selectStmt;
    private final PreparedStatement timestampStmt;

    public CassandraBlobStore(CassandraSessionFactory sessionFactory,
                              CassandraSchemaManagerFactory cassandraSchemaManagerFactory) throws IOException {
        Objects.requireNonNull(sessionFactory);
        Objects.requireNonNull(cassandraSchemaManagerFactory);

        cassandraSchemaManagerFactory.getSchemaManager()
                .create(() -> getClass().getResourceAsStream("/cql/kv.cql"));
        session = sessionFactory.getSession();
        insertStmt = session.prepare(String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)", TABLE_NAME,
                KEY_COLUMN, CONTEXT_COLUMN, VALUE_COLUMN, TIMESTAMP_COLUMN));
        insertWithTtlStmt = session.prepare(String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?) USING " +
                "TTL ?", TABLE_NAME, KEY_COLUMN, CONTEXT_COLUMN, VALUE_COLUMN, TIMESTAMP_COLUMN));
        selectStmt = session.prepare(String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", VALUE_COLUMN,
                TABLE_NAME, KEY_COLUMN, CONTEXT_COLUMN));
        timestampStmt = session.prepare(String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", TIMESTAMP_COLUMN,
                TABLE_NAME, KEY_COLUMN, CONTEXT_COLUMN));
    }

    @Override
    public long put(String key, byte[] value, String context, Integer ttlInSeconds) {
        long timestamp = System.currentTimeMillis();
        Statement statement = getStatementForInsert(key, context, ByteBuffer.wrap(value), timestamp, ttlInSeconds);
        // Cassandra will throw a runtime exception here if the execution fails
        session.execute(statement);
        return timestamp;
    }

    @Override
    public Optional<byte[]> get(String key, String context) {
        byte[] serializedValue;

        // Cassandra will throw a runtime exception here if the execution fails
        ResultSet resultSet = session.execute(selectStmt.bind(key, context));
        Row row = resultSet.one();

        // Could not find the key
        if (row == null) {
            return Optional.empty();
        }

        serializedValue = row.getBytes(VALUE_COLUMN).array();

        return Optional.of(serializedValue);
    }

    @Override
    public CompletableFuture<Long> putAsync(String key, byte[] value, String context, Integer ttlInSeconds) {
        CompletableFuture<Long> putFuture = new CompletableFuture<>();
        long timestamp = System.currentTimeMillis();

        try {
            Statement statement = getStatementForInsert(key, context, ByteBuffer.wrap(value), timestamp, ttlInSeconds);
            // Cassandra will throw a runtime exception here if the execution fails
            ResultSetFuture resultSetFuture = session.executeAsync(statement);
            resultSetFuture
                    .addListener(() -> {
                        try {
                            resultSetFuture.getUninterruptibly();
                            putFuture.complete(timestamp);
                        } catch (Exception e) {
                            putFuture.completeExceptionally(e);
                        }
                    }, MoreExecutors.directExecutor());
        } catch (Exception e) {
            putFuture.completeExceptionally(e);
        }

        return putFuture;
    }

    @Override
    public CompletableFuture<Optional<byte[]>> getAsync(String key, String context) {
        CompletableFuture<Optional<byte[]>> getFuture = new CompletableFuture<>();

        try {
            // Cassandra will throw a runtime exception here if the execution fails
            ResultSetFuture resultSetFuture = session.executeAsync(selectStmt.bind(key, context));
            resultSetFuture.addListener(() -> processGetFutureResult(getFuture, resultSetFuture),
                    MoreExecutors.directExecutor());
        } catch (Exception e) {
            getFuture.completeExceptionally(e);
        }

        return getFuture;
    }

    @Override
    public Optional<Optional<byte[]>> getIfStale(String key, String context, long timestamp) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        // Note that the below is intentionally not acting atomically as the caller shouldn't be sensitive to the change
        // between the getLastUpdated and get
        OptionalLong lastUpdated = getLastUpdated(key, context);

        // key was not found
        if (!lastUpdated.isPresent()) {
            return Optional.empty();
        }

        // key was found but not stale
        if (timestamp >= lastUpdated.getAsLong()) {
            return Optional.of(Optional.empty());
        }

        // key was found and stale
        Optional<byte[]> value = get(key, context);

        // The value was removed between checking last updated and now
        if (!value.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    @Override
    public OptionalLong getLastUpdated(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        // Cassandra will throw a runtime exception here if the execution fails
        ResultSet resultSet = session.execute(timestampStmt.bind(key, context));
        Row row = resultSet.one();

        // Could not find the key
        if (row == null) {
            return OptionalLong.empty();
        }

        Date lastUpdated = row.getTimestamp(TIMESTAMP_COLUMN);

        return OptionalLong.of(lastUpdated.getTime());
    }

    @Override
    public CompletableFuture<Optional<Optional<byte[]>>> getIfStaleAsync(String key, String context, long timestamp) {
        CompletableFuture<Optional<Optional<byte[]>>> getIfStaleFuture = new CompletableFuture<>();

        // Note that the below is intentionally not acting atomically as the caller shouldn't be sensitive to the change
        // between the getLastUpdated and get
        getLastUpdatedAsync(key, context).whenComplete((lastUpdated, t) -> {
            if (t != null) {
                getIfStaleFuture.completeExceptionally(t);
                return;
            }

            if (!lastUpdated.isPresent()) {
                getIfStaleFuture.complete(Optional.empty());
                return;
            }

            // key was found but not stale
            if (timestamp >= lastUpdated.getAsLong()) {
                getIfStaleFuture.complete(Optional.of(Optional.empty()));
                return;
            }

            // key was found and stale
            getAsync(key, context).whenComplete((val, th) -> {
                if (th != null) {
                    getIfStaleFuture.completeExceptionally(th);
                    return;
                }

                // The value was removed between checking last updated and now
                if (!val.isPresent()) {
                    getIfStaleFuture.complete(Optional.empty());
                }

                getIfStaleFuture.complete(Optional.of(val));
            });
        });

        return getIfStaleFuture;
    }

    @Override
    public CompletableFuture<OptionalLong> getLastUpdatedAsync(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        CompletableFuture<OptionalLong> tsFuture = new CompletableFuture<>();

        try {
            // Cassandra will throw a runtime exception here if the execution fails
            ResultSetFuture resultSetFuture = session.executeAsync(timestampStmt.bind(key, context));
            resultSetFuture.addListener(() -> processLastUpdatedFutureResult(tsFuture, resultSetFuture),
                    MoreExecutors.directExecutor());
        } catch (Exception e) {
            tsFuture.completeExceptionally(e);
        }

        return tsFuture;
    }

    private Statement getStatementForInsert(String key, String context, ByteBuffer serializedValue, long timestamp,
                                            Integer ttlInSeconds) {
        Statement statement;

        if (ttlInSeconds != null) {
            if (ttlInSeconds <= 0) {
                throw new IllegalArgumentException("TTL must be positive and greater than 0");
            }

            statement = insertWithTtlStmt.bind(key, context, serializedValue, new Date(timestamp), ttlInSeconds);
        } else {
            statement = insertStmt.bind(key, context, serializedValue, new Date(timestamp));
        }

        return statement;
    }

    private static void processGetFutureResult(CompletableFuture<Optional<byte[]>> future,
                                               ResultSetFuture resultSetFuture) {
        ResultSet resultSet;

        try {
            resultSet = resultSetFuture.getUninterruptibly();
        } catch (Exception e) {
            future.completeExceptionally(e);
            return;
        }

        Row row = resultSet.one();

        // Could not find the key
        if (row == null) {
            future.complete(Optional.empty());
            return;
        }

        future.complete(Optional.of(row.getBytes(VALUE_COLUMN).array()));
    }

    private static void processLastUpdatedFutureResult(CompletableFuture<OptionalLong> future,
                                                       ResultSetFuture resultSetFuture) {
        ResultSet resultSet;

        try {
            resultSet = resultSetFuture.getUninterruptibly();
        } catch (Exception e) {
            future.completeExceptionally(e);
            return;
        }

        Row row = resultSet.one();

        // Could not find the key
        if (row == null) {
            future.complete(OptionalLong.empty());
            return;
        }

        Date lastUpdated = row.getTimestamp(TIMESTAMP_COLUMN);

        future.complete(OptionalLong.of(lastUpdated.getTime()));
    }

    @Override
    public String getBackingImplName() {
        return "Cassandra";
    }
}
