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
package org.opennms.features.distributed.cassandra.api;

import com.datastax.oss.driver.api.core.cql.AsyncResultSet;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.Statement;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Future;

/**
 * A thin facade on top of Cassandra's {@link com.datastax.oss.driver.api.core.session.Session session}.
 * 
 * The purpose of using this interface rather than Cassandra's {@link com.datastax.oss.driver.api.core.session.Session} directly is
 * because Newts wraps the session in a similar interface and does not expose the session directly. So rather than
 * depending on the newts version of the interface in OpenNMS we have this one and proxy between them in
 * NewtsCassandraSessionFactory. The implication being that any future implementations that may expose a
 * {@link com.datastax.oss.driver.api.core.session.Session} directly will have to wrap it with this interface, but that should be
 * trivial.
 */
public interface CassandraSession {
    PreparedStatement prepare(String statement);

    PreparedStatement prepare(SimpleStatement statement);

    CompletionStage<AsyncResultSet> executeAsync(Statement statement);

    ResultSet execute(Statement statement);

    ResultSet execute(String statement);

    Future<Void> shutdown();
}
