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
package org.opennms.features.distributed.kvstore.blob.cassandra;

import java.io.IOException;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.Statement;
import com.datastax.oss.driver.api.core.type.codec.TypeCodecs;
import org.opennms.features.distributed.cassandra.api.CassandraSchemaManagerFactory;
import org.opennms.features.distributed.cassandra.api.CassandraSession;
import org.opennms.features.distributed.cassandra.api.CassandraSessionFactory;
import org.opennms.features.distributed.kvstore.api.AbstractKeyValueStore;
import org.opennms.features.distributed.kvstore.api.BlobStore;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * A {@link BlobStore} that is backed by Cassandra.
 * <p>
 * This implementation persists values that implement {@link Serializable} by using vanilla Java serialization.
 * <p>
 * This implementation persists key values in a specific table that will be created in a keyspace dictated by the
 * {@link org.opennms.features.distributed.cassandra.api.CassandraSchemaManager schema manager}.
 * <p>
 * This implementation does not initiate its own {@link com.datastax.oss.driver.api.core.session.Session Cassandra session} and must be
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
    private final PreparedStatement enumerateStatement;
    private final PreparedStatement deleteStatement;
    
    // The cardinality of this thread pool will be limited by the limited number (expected) of requests that end up
    // using it
    private final Executor asyncDeleteExecutor = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("cassandra-async-delete-%d").build());

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
        enumerateStatement = session.prepare(String.format("SELECT %s, %s FROM %s WHERE %s = ?", KEY_COLUMN,
                VALUE_COLUMN, TABLE_NAME, CONTEXT_COLUMN));
        deleteStatement = session.prepare(String.format("DELETE FROM %s WHERE %s = ? and %s = ?", TABLE_NAME,
                KEY_COLUMN, CONTEXT_COLUMN));
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
        // Cassandra will throw a runtime exception here if the execution fails
        ResultSet resultSet = session.execute(selectStmt.bind(key, context));
        Row row = resultSet.one();

        // Could not find the key
        if (row == null) {
            return Optional.empty();
        }

        ByteBuffer value = row.get(VALUE_COLUMN, TypeCodecs.BLOB);
        if (value == null) {
            return Optional.empty();
        }
        return Optional.of(value.array());
    }

    @Override
    public CompletableFuture<Long> putAsync(String key, byte[] value, String context, Integer ttlInSeconds) {
        long timestamp = System.currentTimeMillis();
        Statement<?> statement = getStatementForInsert(key, context, ByteBuffer.wrap(value), timestamp, ttlInSeconds);
        return session.executeAsync(statement).thenApply(e -> timestamp).toCompletableFuture();
    }

    @Override
    public CompletableFuture<Optional<byte[]>> getAsync(String key, String context) {
        return session.executeAsync(selectStmt.bind(key, context))
                .toCompletableFuture()
                .thenApply(resultSet -> {
                        Row row = resultSet.one();

                    // Could not find the key
                    if (row == null) {
                        return Optional.empty();
                    }

                    ByteBuffer bytes = row.get(VALUE_COLUMN, TypeCodecs.BLOB);
                    if (bytes == null) {
                        return Optional.empty();
                    } else {
                        return Optional.of(bytes.array());
                    }
                });
    }

    @Override
    public Optional<Optional<byte[]>> getIfStale(String key, String context, long timestamp) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        // Note that the below is intentionally not acting atomically as the caller shouldn't be sensitive to the change
        // between the getLastUpdated and get
        OptionalLong lastUpdated = getLastUpdated(key, context);

        // key was not found
        if (lastUpdated.isEmpty()) {
            return Optional.empty();
        }

        // key was found but not stale
        if (timestamp >= lastUpdated.getAsLong()) {
            return Optional.of(Optional.empty());
        }

        // key was found and stale
        Optional<byte[]> value = get(key, context);

        // The value was removed between checking last updated and now
        if (value.isEmpty()) {
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

        Instant lastUpdated = row.get(TIMESTAMP_COLUMN, TypeCodecs.TIMESTAMP);
        if (lastUpdated == null) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(lastUpdated.toEpochMilli());
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

            if (lastUpdated.isEmpty()) {
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
                if (val.isEmpty()) {
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

        return session.executeAsync(timestampStmt.bind(key, context))
                .toCompletableFuture()
                .thenApply(resultSet -> {
                    Row row = resultSet.one();

                    // Could not find the key
                    if (row == null) {
                        return OptionalLong.empty();
                    }

                    Instant lastUpdate = row.get(TIMESTAMP_COLUMN, TypeCodecs.TIMESTAMP);
                    if (lastUpdate == null) {
                        return OptionalLong.empty();
                    }

                    return OptionalLong.of(lastUpdate.toEpochMilli());
                });
    }

    @Override
    public Map<String, byte[]> enumerateContext(String context) {
        Objects.requireNonNull(context);

        Map<String, byte[]> resultMap = new HashMap<>();
        
        session.execute(enumerateStatement.bind(context))
                .forEach(row -> resultMap.put(row.getString(KEY_COLUMN), row.get(VALUE_COLUMN, TypeCodecs.BLOB).array()));
        
        return Collections.unmodifiableMap(resultMap);
    }

    @Override
    public void delete(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);

        session.execute(deleteStatement.bind(key, context));
    }

    @Override
    public CompletableFuture<Map<String, byte[]>> enumerateContextAsync(String context) {
        Objects.requireNonNull(context);
        Map<String, byte[]> resultMap = new HashMap<>();
        return session.executeAsync(enumerateStatement.bind(context))
                .thenCompose(rs -> enumerateContext(rs, resultMap))
                .toCompletableFuture();
    }

    private CompletionStage<Map<String, byte[]>> enumerateContext(AsyncResultSet resultSet, Map<String, byte[]> resultMap) {
        for (Row row : resultSet.currentPage()) {
            String key = row.getString(KEY_COLUMN);
            if (key == null) {
                continue;
            }
            ByteBuffer value = row.get(VALUE_COLUMN, TypeCodecs.BLOB);
            if (value == null) {
                continue;
            }
            resultMap.put(key, value.array());
        }
        if (resultSet.hasMorePages()) {
            return resultSet.fetchNextPage().thenCompose(rs -> enumerateContext(rs, resultMap));
        } else {
            return CompletableFuture.completedFuture(resultMap);
        }
    }

    @Override
    public CompletableFuture<Void> deleteAsync(String key, String context) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(context);
        return session.executeAsync(deleteStatement.bind(key, context)).toCompletableFuture()
                .thenApply(res -> null);
    }

    @Override
    public CompletableFuture<Void> truncateContextAsync(String context) {
        Objects.requireNonNull(context);

        CompletableFuture<Void> truncateFuture = new CompletableFuture<>();

        // The below ends up doing deletes synchronously so we move processing off of the Cassandra session thread and
        // onto our own
        enumerateContextAsync(context).whenCompleteAsync((enumerateResult, enumerateThrowable) -> {
            if (enumerateThrowable != null) {
                truncateFuture.completeExceptionally(enumerateThrowable);
                return;
            }

            // It would be nice to fire off a bunch of deleteAsync here instead of sync deletes, however that has the
            // tendency to overwhelm the Cassandra connection pool therefore we will just tie this thread up until we
            // are done deleting synchronously
            enumerateResult.keySet().forEach(key -> {
                try {
                    delete(key, context);
                } catch (Exception e) {
                    truncateFuture.completeExceptionally(e);
                }
            });
            truncateFuture.complete(null);
        }, asyncDeleteExecutor);

        return truncateFuture;
    }

    private Statement<?> getStatementForInsert(String key, String context, ByteBuffer serializedValue, long timestamp,
                                            Integer ttlInSeconds) {
        Statement<?> statement;

        if (ttlInSeconds != null) {
            if (ttlInSeconds <= 0) {
                throw new IllegalArgumentException("TTL must be positive and greater than 0");
            }

            statement = insertWithTtlStmt.bind(key, context, serializedValue, Instant.ofEpochMilli(timestamp), ttlInSeconds);
        } else {
            statement = insertStmt.bind(key, context, serializedValue, Instant.ofEpochMilli(timestamp));
        }

        return statement;
    }

    @Override
    public String getName() {
        return "Cassandra";
    }
}
