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

package org.opennms.features.distributed.blob.cassandra;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.cassandraunit.CassandraCQLUnit;
import org.cassandraunit.dataset.CQLDataSet;
import org.junit.Rule;
import org.junit.Test;
import org.opennms.features.distributed.blob.BaseBlobStoreIT;
import org.opennms.features.distributed.cassandra.api.CassandraSession;
import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.features.distributed.kvstore.api.SerializingBlobStore;
import org.opennms.features.distributed.kvstore.blob.cassandra.CassandraBlobStore;
import org.opennms.newts.cassandra.SchemaManager;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.exceptions.DriverException;

public class CassandraBlobStoreIT extends BaseBlobStoreIT {
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

    private CassandraSession cassandraSession;

    public void init() throws IOException {
        if (cassandraUnit.getSession().isClosed()) {
            cassandraSession = getSession(cassandraUnit.getCluster().connect());
        } else if (cassandraSession == null) {
            cassandraSession = getSession(cassandraUnit.getSession());
        }

        blobStore = new CassandraBlobStore(() -> {
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

        serializingBlobStore = new SerializingBlobStore<>(blobStore, String::getBytes, String::new);
    }

    @Test
    public void completesExceptionallyWithCassandraError() throws InterruptedException, IOException {
        BlobStore exceptionalKvStore = new CassandraBlobStore(() -> new CassandraSession() {
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
        serializingBlobStore = new SerializingBlobStore<>(exceptionalKvStore, String::getBytes, String::new);
        cassandraUnit.getSession().close();

        try {
            serializingBlobStore.putAsync("test", "test", "completesExceptionallyWithCassandraError").get();
            fail("Should have triggered an ExecutionException");
        } catch (ExecutionException e) {
        }

        try {
            serializingBlobStore.getAsync("test", "completesExceptionallyWithCassandraError").get();
            fail("Should have triggered an ExecutionException");
        } catch (ExecutionException e) {
        }
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
