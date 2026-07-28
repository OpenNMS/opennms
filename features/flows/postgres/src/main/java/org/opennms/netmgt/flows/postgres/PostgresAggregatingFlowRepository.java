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

import java.util.List;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.opennms.netmgt.flows.aggregation.AbstractAggregatingFlowRepository;
import org.opennms.netmgt.flows.aggregation.AggregatedFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

/**
 * PostgreSQL {@link AbstractAggregatingFlowRepository}: its sink is a {@link FlowAggWriter} that batch-
 * inserts closed-window rows into the {@code flow_agg} table, sharing the flow {@link
 * FlowDataSourceProvider} with the raw repository/query services. {@link #createSink} returns
 * {@code null} when no DataSource is configured, so aggregation stays inert and the feature still loads
 * cleanly on an unconfigured node (e.g. a Sentinel with an empty configuration).
 */
public class PostgresAggregatingFlowRepository extends AbstractAggregatingFlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(PostgresAggregatingFlowRepository.class);

    private FlowDataSourceProvider dataSourceProvider;
    private DataSource dataSource;

    public PostgresAggregatingFlowRepository(final MetricRegistry metrics) {
        super(metrics);
    }

    @Override
    protected Consumer<List<AggregatedFlow>> createSink(final String writerId) {
        if (this.dataSource == null && this.dataSourceProvider != null) {
            this.dataSource = this.dataSourceProvider.getDataSource();
        }
        if (this.dataSource == null) {
            LOG.error("Write-time flow aggregation not started: no flow DataSource is configured. Flows will "
                    + "NOT be aggregated until datasource.url is set on the "
                    + "org.opennms.features.flows.persistence.postgres pid.");
            return null;
        }
        return new FlowAggWriter(this.dataSource, writerId);
    }

    /** Blueprint wiring: supplies the flow DataSource in start() (may be inert, i.e. return null). */
    public void setDataSourceProvider(final FlowDataSourceProvider dataSourceProvider) { this.dataSourceProvider = dataSourceProvider; }
    /** Test/embedding hook: use this DataSource directly instead of resolving one from the provider. */
    public void setDataSource(final DataSource dataSource) { this.dataSource = dataSource; }
}