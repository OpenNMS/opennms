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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.CQLDataSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.features.distributed.cassandra.api.CassandraSession;
import org.opennms.features.distributed.kvstore.api.SerializedKVStore;
import org.opennms.newts.cassandra.SchemaManager;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;

public class CassandraKVStoreIT {
    private static final String KEYSPACE = "opennms";

    @Rule
    public CassandraCQLUnit cassandraUnit = new CassandraCQLUnit(new CQLDataSet() {
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
    });

    private SerializedKVStore<Serializable> kvStore;

    private final CassandraSession cassandraSession = new CassandraSession() {
        @Override
        public PreparedStatement prepare(String statement) {
            return cassandraUnit.getSession().prepare(statement);
        }

        @Override
        public PreparedStatement prepare(RegularStatement statement) {
            return cassandraUnit.getSession().prepare(statement);
        }

        @Override
        public ResultSetFuture executeAsync(Statement statement) {
            return cassandraUnit.getSession().executeAsync(statement);
        }

        @Override
        public ResultSet execute(Statement statement) {
            return cassandraUnit.getSession().execute(statement);
        }

        @Override
        public ResultSet execute(String statement) {
            return cassandraUnit.getSession().execute(statement);
        }

        @Override
        public Future<Void> shutdown() {
            return CompletableFuture.completedFuture(null);
        }
    };

    @Before
    public void initStore() throws IOException {
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
    }

    @Test
    public void canPersistAndRetrieve() throws IOException,
            ClassNotFoundException {
        String key = "test";
        int i = 100;
        String s = "Tubessss";
        State state = new State(i, s);

        kvStore.put(key, state);
        Optional<Serializable> value = kvStore.get(key);
        assertThat(value.get(), equalTo(state));
    }

    @Test
    public void emptyWhenKeyDoesNotExist() throws IOException, ClassNotFoundException {
        // If Cassandra is available, but the key does not exist we should get an empty optional back
        assertThat(kvStore.get("test"), equalTo(Optional.empty()));
    }

    @Test
    public void exceptionWhenCassandraUnavailable() {
        cassandraUnit.getSession().close();
        try {
            kvStore.put("test", new State(1, "a"));
            fail("Should have triggered an exception");
        } catch (Exception ignore) {
        }

        try {
            kvStore.get("test");
            fail("Should have triggered an exception");
        } catch (Exception ignore) {
        }
    }

    @Test
    public void canPersistAndRetrieveAsync() throws ExecutionException, InterruptedException {
        List<CompletableFuture<Long>> putFutures = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            String iStr = Integer.toString(i);
            putFutures.add(kvStore.putAsync(iStr, new CassandraKVStoreIT.State(i, iStr)));
        }

        // Since we did 1000 puts async they shouldn't be all done immediately but we should be able to wait for them
        // to finish
        CompletableFuture[] allPutFutures = putFutures.toArray(new CompletableFuture[0]);
        assertThat(CompletableFuture.allOf(allPutFutures).isDone(), equalTo(false));
        CompletableFuture.allOf(allPutFutures).get();

        List<CompletableFuture<Optional<Serializable>>> getFutures = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            String iStr = Integer.toString(i);
            getFutures.add(kvStore.getAsync(iStr));
        }

        // Since we did 1000 gets async they shouldn't be all done immediately but we should be able to wait for them
        // to finish and they should all have non-empty values once complete
        CompletableFuture[] allGetFutures = getFutures.toArray(new CompletableFuture[0]);
        assertThat(CompletableFuture.allOf(allGetFutures).isDone(), equalTo(false));
        CompletableFuture.allOf(allGetFutures).get();
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

    static class State implements Serializable {
        private final int i;
        private final String s;

        State(int i, String s) {
            this.i = i;
            this.s = s;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return i == state.i &&
                    Objects.equals(s, state.s);
        }

        @Override
        public int hashCode() {
            return Objects.hash(i, s);
        }
    }
}
