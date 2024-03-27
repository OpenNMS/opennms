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
package org.opennms.features.distributed.cassandra.impl;

import java.util.Objects;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;
import org.opennms.features.distributed.cassandra.api.CassandraSession;
import org.opennms.features.distributed.cassandra.api.CassandraSessionFactory;

/**
 * Serves the Cassandra session initiated by Newts by proxying it through our own {@link CassandraSession session}
 * object.
 */
public class NewtsCassandraSessionFactory implements CassandraSessionFactory {
    private final CassandraSession proxySession;

    public NewtsCassandraSessionFactory(org.opennms.newts.cassandra.CassandraSession newtsCassandraSession) {
        Objects.requireNonNull(newtsCassandraSession);

        // Map between our proxy session and the session owned by newts
        proxySession = NewtsCassandraSessionFactory.of(newtsCassandraSession);
    }

    public static CassandraSession of(org.opennms.newts.cassandra.CassandraSession newtsCassandraSession) {
        return new CassandraSession() {
            @Override
            public PreparedStatement prepare(String statement) {
                return newtsCassandraSession.prepare(statement);
            }

            @Override
            public PreparedStatement prepare(SimpleStatement statement) {
                return newtsCassandraSession.prepare(statement);
            }

            @Override
            public CompletionStage<AsyncResultSet> executeAsync(Statement statement) {
                return newtsCassandraSession.executeAsync(statement);
            }

            @Override
            public ResultSet execute(Statement statement) {
                return newtsCassandraSession.execute(statement);
            }

            @Override
            public ResultSet execute(String statement) {
                return newtsCassandraSession.execute(statement);
            }

            @Override
            public Future<Void> shutdown() {
                return newtsCassandraSession.shutdown().toCompletableFuture();
            }
        };
    }

    @Override
    public CassandraSession getSession() {
        return proxySession;
    }
}
