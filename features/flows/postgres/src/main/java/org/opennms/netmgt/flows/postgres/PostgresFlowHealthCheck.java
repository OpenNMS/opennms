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
package org.opennms.netmgt.flows.postgres;

import java.sql.Connection;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;

/**
 * Health check for the PostgreSQL flow repository: verifies that a connection to the configured flow
 * database (the {@code org.opennms.features.flows.persistence.postgres} pid) can be established and is
 * valid. This is the PostgreSQL-store counterpart to the Elasticsearch "cluster health check for Flows",
 * and is registered whenever the flows-postgres feature is installed.
 */
public class PostgresFlowHealthCheck implements HealthCheck {

    private static final int VALIDATION_TIMEOUT_SECONDS = 5;

    private final FlowDataSourceProvider dataSourceProvider;

    public PostgresFlowHealthCheck(final FlowDataSourceProvider dataSourceProvider) {
        this.dataSourceProvider = Objects.requireNonNull(dataSourceProvider);
    }

    @Override
    public String getDescription() {
        return "PostgreSQL database health check for Flows";
    }

    @Override
    public List<String> getTags() {
        return List.of("flows", "postgres");
    }

    @Override
    public Response perform(final Context context) {
        final DataSource dataSource = dataSourceProvider.getDataSource();
        if (dataSource == null) {
            return new Response(Status.Success, "Not configured");
        }
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(VALIDATION_TIMEOUT_SECONDS)) {
                return new Response(Status.Success);
            }
            return new Response(Status.Failure, "The flow database connection is not valid.");
        } catch (final Exception e) {
            return new Response(e);
        }
    }
}
