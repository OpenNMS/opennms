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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.flows.api.FlowQueryService;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;

import com.codahale.metrics.MetricRegistry;

/**
 * Verifies the raw-vs-aggregated routing of {@link PostgresSmartQueryService}, mirroring the
 * Elasticsearch SmartQueryService semantics.
 */
public class PostgresSmartQueryServiceTest {

    private PostgresSmartQueryService svc;

    @Before
    public void setUp() {
        svc = new PostgresSmartQueryService(new MetricRegistry(), mock(FlowQueryService.class), mock(FlowQueryService.class));
    }

    private static List<Filter> range(final long start, final long end) {
        return Collections.singletonList(new TimeRangeFilter(start, end));
    }

    @Test
    public void defaultsToRawForEverything() {
        final long now = System.currentTimeMillis();
        assertEquals(PostgresSmartQueryService.QueryServiceType.RAW, svc.getDelegate(range(now - 3_600_000L, now), false));
        assertEquals(PostgresSmartQueryService.QueryServiceType.RAW, svc.getDelegate(range(now - 3_600_000L, now), true));
    }

    @Test
    public void alwaysUseAggForcesAggEvenForSpecificEntities() {
        svc.setAlwaysUseAggForQueries(true);
        final long now = System.currentTimeMillis();
        assertEquals(PostgresSmartQueryService.QueryServiceType.AGG, svc.getDelegate(range(now - 60_000L, now), false));
        assertEquals(PostgresSmartQueryService.QueryServiceType.AGG, svc.getDelegate(range(now - 60_000L, now), true));
    }

    @Test
    public void thresholdRoutingWhenNeitherFlagIsSet() {
        svc.setAlwaysUseRawForQueries(false); // both flags now false -> threshold routing
        final long now = System.currentTimeMillis();
        final long twoMin = TimeUnit.MINUTES.toMillis(2);

        // specific-entity queries always go raw
        assertEquals(PostgresSmartQueryService.QueryServiceType.RAW, svc.getDelegate(range(now - 3 * twoMin, now), true));
        // a wide-enough duration goes aggregated
        assertEquals(PostgresSmartQueryService.QueryServiceType.AGG, svc.getDelegate(range(now - 3 * twoMin, now), false));
        // a narrow, recent range stays raw
        assertEquals(PostgresSmartQueryService.QueryServiceType.RAW, svc.getDelegate(range(now - 60_000L, now), false));
        // a narrow range whose endpoint is older than 7 days goes aggregated
        final long old = now - TimeUnit.DAYS.toMillis(8);
        assertEquals(PostgresSmartQueryService.QueryServiceType.AGG, svc.getDelegate(range(old - 60_000L, old), false));
        // no time filter -> raw
        assertEquals(PostgresSmartQueryService.QueryServiceType.RAW, svc.getDelegate(Collections.emptyList(), false));
    }
}
