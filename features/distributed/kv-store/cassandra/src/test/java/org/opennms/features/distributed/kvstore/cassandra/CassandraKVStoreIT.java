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

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.CQLDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.features.distributed.cassandra.api.CassandraSession;
import org.opennms.features.distributed.kvstore.api.KeyValueStore;
import org.opennms.features.distributed.kvstore.api.SerializingKeyValueStore;
import org.opennms.newts.cassandra.SchemaManager;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.DriverException;

public class CassandraKVStoreIT {
    private static final String KEYSPACE = "opennms";

    private final CQLDataSet testSet = new CQLDataSet() {
        @Override
        public List<String> getCQLStatements() {
            return Collections.emptyList();
        }

        @Override
        public String getKeyspaceName() {
            return null;
        }

        @Override
        public boolean isKeyspaceCreation() {
            return false;
        }

        @Override
        public boolean isKeyspaceDeletion() {
            return false;
        }
    };

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(testSet);

    private KeyValueStore kvStore;

    private SerializingKeyValueStore<String> serializingKeyValueStore;

    private CassandraSession cassandraSession;

    @Before
    public void initStore() throws IOException {
        if (cassandraUnit.getSession().isClosed()) {
            cassandraSession = getSession(cassandraUnit.getCluster().connect());
        } else if (cassandraSession == null) {
            cassandraSession = getSession(cassandraUnit.getSession());
        }

        kvStore = new CassandraKVStore(() -> {
            cassandraSession.execute(String.format("USE %s;", KEYSPACE));
            return cassandraSession;
        }, () -> schema -> {
            InetSocketAddress cassandraAddress = cassandraUnit.getSession()
                    .getCluster()
                    .getMetadata()
                    .getAllHosts()
                    .iterator()
                    .next()
                    .getSocketAddress();
            SchemaManager sm = new SchemaManager(KEYSPACE, cassandraAddress.getHostName(),
                    cassandraAddress.getPort(), "cassandra", "cassandra", false);
            sm.create(schema::getInputStream);
        });
        
        serializingKeyValueStore = new SerializingKeyValueStore<>(kvStore, String::getBytes, String::new);
    }

    @Test
    public void canPersistAndRetrieve() {
        String key = "test";
        String context = "canPersistAndRetrieve";
        String state = "state";

        serializingKeyValueStore.put(key, state, context);
        Optional<String> value = serializingKeyValueStore.get(key, context);
        assertThat(value.get(), equalTo(state));
    }

    @Test
    public void emptyWhenKeyDoesNotExist() {
        // If Cassandra is available, but the key does not exist we should get an empty optional back
        assertThat(serializingKeyValueStore.get("test", "emptyWhenKeyDoesNotExist"), equalTo(Optional.empty()));
    }

    @Test
    public void completesExceptionallyWithCassandraError() throws InterruptedException, IOException {
        KeyValueStore exceptionalKvStore = new CassandraKVStore(() -> new CassandraSession() {
            @Override
            public PreparedStatement prepare(String statement) {
                return null;
            }

            @Override
            public PreparedStatement prepare(RegularStatement statement) {
                return null;
            }

            @Override
            public ResultSetFuture executeAsync(Statement statement) {
                throw new DriverException("test");
            }

            @Override
            public ResultSet execute(Statement statement) {
                throw new DriverException("test");
            }

            @Override
            public ResultSet execute(String statement) {
                throw new DriverException("test");
            }

            @Override
            public Future<Void> shutdown() {
                return null;
            }
        }, () -> (schema) -> {});
        serializingKeyValueStore = new SerializingKeyValueStore<>(exceptionalKvStore, String::getBytes, String::new);
        cassandraUnit.getSession().close();

        try {
            serializingKeyValueStore.putAsync("test", "test", "completesExceptionallyWithCassandraError").get();
            fail("Should have triggered an ExecutionException");
        } catch (ExecutionException e) {
        }

        try {
            serializingKeyValueStore.getAsync("test", "completesExceptionallyWithCassandraError").get();
            fail("Should have triggered an ExecutionException");
        } catch (ExecutionException e) {
        }
    }

    @Test
    public void keysExpire() throws InterruptedException {
        String key = "test";
        String context = "keysExpire";
        String value = "test";
        int ttl = 1;
        serializingKeyValueStore.put(key, value, context, ttl);
        assertThat(serializingKeyValueStore.get(key, context).get(), equalTo(value));
        Thread.sleep(ttl * 1000);
        assertThat(serializingKeyValueStore.get(key, context), equalTo(Optional.empty()));
    }

    @Test
    public void canPersistAndRetrieveAsync() throws ExecutionException, InterruptedException, TimeoutException {
        List<CompletableFuture<Long>> putFutures = new ArrayList<>();

        String context = "canPersistAndRetrieveAsync";

        for (int i = 0; i < 1000; i++) {
            String iStr = Integer.toString(i);
            putFutures.add(serializingKeyValueStore.putAsync(iStr, Integer.toString(i), context));
        }

        // Verify that all the puts finish
        CompletableFuture[] allPutFutures = putFutures.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(allPutFutures).get(1, TimeUnit.MINUTES);

        List<CompletableFuture<Optional<String>>> getFutures = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            getFutures.add(serializingKeyValueStore.getAsync(Integer.toString(i), context));
        }

        // Verify that all the gets finish
        CompletableFuture[] allGetFutures = getFutures.toArray(new CompletableFuture[0]);
        CompletableFuture.allOf(allGetFutures).get(1, TimeUnit.MINUTES);
        getFutures.stream()
                .filter(f -> {
                    try {
                        return f.get().equals(Optional.empty());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .findAny()
                .ifPresent(f -> fail("Should not have found empty optional"));
    }

    @Test
    public void canDetermineIfLatest() throws InterruptedException {
        String key = "tesT";
        String context = "canDetermineIfLatest";
        String originalState = "original";
        long originalTimestamp = serializingKeyValueStore.put(key, originalState, context);

        Thread.sleep(10);

        assertThat(originalTimestamp, equalTo(serializingKeyValueStore.getLastUpdated(key, context).getAsLong()));
        assertThat(serializingKeyValueStore.get(key, context).get(), equalTo(originalState));

        String updatedState = "updated";
        long updatedTimestamp = serializingKeyValueStore.put(key, updatedState, context);
        assertThat(originalTimestamp, lessThan(updatedTimestamp));
        assertThat(serializingKeyValueStore.get(key, context).get(), equalTo(updatedState));
    }

    @Test
    public void canGetLastUpdatedAsync() throws InterruptedException, ExecutionException, TimeoutException {
        String key = "test";
        String context = "canGetLastUpdatedAsync";
        long timestamp = serializingKeyValueStore.putAsync(key, "test", context).get(5, TimeUnit.SECONDS);
        long lastUpdated = serializingKeyValueStore.getLastUpdatedAsync(key, context).get(5, TimeUnit.SECONDS).getAsLong();
        assertThat(timestamp, equalTo(lastUpdated));
    }

    @Test
    public void canGetIfStale() {
        String key = "key";
        String context = "canGetIfStale";
        String value = "test";
        
        long timestamp = serializingKeyValueStore.put(key, value, context);
        
        Optional<Optional<String>> currentValue = serializingKeyValueStore.getIfStale(key, context, timestamp);
        // Should find the key but should already have the latest
        assertThat(currentValue.get(), equalTo(Optional.empty()));

        currentValue = serializingKeyValueStore.getIfStale(key, context, timestamp - 1);
        // Should find the key and should see its stale
        assertThat(currentValue.get().get(), equalTo(value));
    }
    
    private CassandraSession getSession(Session session) {
        return new CassandraSession() {
            @Override
            public PreparedStatement prepare(String statement) {
                return session.prepare(statement);
            }

            @Override
            public PreparedStatement prepare(RegularStatement statement) {
                return session.prepare(statement);
            }

            @Override
            public ResultSetFuture executeAsync(Statement statement) {
                return session.executeAsync(statement);
            }

            @Override
            public ResultSet execute(Statement statement) {
                return session.execute(statement);
            }

            @Override
            public ResultSet execute(String statement) {
                return session.execute(statement);
            }

            @Override
            public Future<Void> shutdown() {
                return CompletableFuture.completedFuture(null);
            }
        };
    }
}
