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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;
import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

import com.codahale.metrics.MetricRegistry;

/**
 * Verifies that the PostgreSQL flow bundle degrades gracefully when no usable flow DataSource is
 * configured: the blueprint beans must load without throwing (so the feature installs cleanly), log an
 * error, and then do nothing — rather than failing the blueprint container (which previously surfaced as
 * an NPE when the internal datasource was unavailable, e.g. on Sentinel with an empty configuration).
 */
public class PostgresFlowDisabledTest {

    @Test
    public void providerLoadsCleanlyAndStaysInertWhenTheDataSourceCannotBeCreated() {
        final FlowDataSourceProvider provider = new FlowDataSourceProvider();
        provider.setUrl("jdbc:postgresql://localhost:5432/irrelevant"); // dedicated mode
        provider.setDriverClass("com.example.NoSuchDriver");            // fails to load -> pool build throws
        provider.init();                                                // must NOT throw
        assertNull("no DataSource must be produced when creation fails", provider.getDataSource());
        provider.close();                                               // must be safe when nothing usable was created
    }

    /** An injected provider that produced no DataSource — the exact blueprint scenario that used to fail. */
    private static FlowDataSourceProvider inertProvider() {
        final FlowDataSourceProvider provider = new FlowDataSourceProvider();
        provider.setUrl("jdbc:postgresql://localhost:5432/irrelevant");
        provider.setDriverClass("com.example.NoSuchDriver");
        provider.init();
        assertNull(provider.getDataSource());
        return provider;
    }

    @Test
    public void repositoryIsInertWhenTheProviderHasNoDataSource() throws Exception {
        final PostgresFlowRepository repository = new PostgresFlowRepository(new MetricRegistry());
        repository.setRunSchemaChangelog(false);
        repository.setDataSourceProvider(inertProvider());
        repository.start();                                             // must NOT throw (feature loads)
        repository.persist(Collections.singletonList(mock(Flow.class))); // must be a no-op, not an NPE
        repository.stop();
    }

    @Test
    public void queryServiceIsInertWhenTheProviderHasNoDataSource() throws Exception {
        final PostgresFlowQueryService queryService = new PostgresFlowQueryService();
        queryService.setDataSourceProvider(inertProvider());
        queryService.start();                                          // must NOT throw (feature loads)
        final List<Filter> filters = Collections.singletonList(new TimeRangeFilter(0, 1000));
        final CompletableFuture<Long> result = queryService.getFlowCount(filters);
        assertTrue("queries must fail cleanly, not NPE, when unconfigured", result.isCompletedExceptionally());
        queryService.stop();
    }
}
