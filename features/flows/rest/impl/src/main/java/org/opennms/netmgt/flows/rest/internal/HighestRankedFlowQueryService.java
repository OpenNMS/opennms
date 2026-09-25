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
package org.opennms.netmgt.flows.rest.internal;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowQueryService;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.LimitedCardinalityField;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Table;

/**
 * A {@link FlowQueryService} that always delegates to the highest service-ranked {@code FlowQueryService}
 * currently registered, re-selecting live as services come and go.
 *
 * <p>OpenNMS ships Elasticsearch flow reads ({@code SmartQueryService}, default ranking) as part of the
 * {@code opennms-flows} feature. The optional {@code opennms-flows-postgres} feature registers its
 * {@code PostgresFlowQueryService} at a higher ranking, so simply installing that feature forces flow
 * reads onto PostgreSQL with no operator configuration; uninstalling it falls back to Elasticsearch.
 * A plain Blueprint {@code <reference>} could not do this — it binds the best service available at bind
 * time and never swaps to a higher-ranked one that appears later — so we track the services directly.
 */
public class HighestRankedFlowQueryService implements FlowQueryService {

    private static final Logger LOG = LoggerFactory.getLogger(HighestRankedFlowQueryService.class);

    private final BundleContext bundleContext;
    private ServiceTracker<FlowQueryService, FlowQueryService> tracker;

    public HighestRankedFlowQueryService(final BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void init() {
        tracker = new ServiceTracker<>(bundleContext, FlowQueryService.class, null);
        tracker.open();
        LOG.debug("HighestRankedFlowQueryService started; tracking FlowQueryService registrations.");
    }

    public void destroy() {
        if (tracker != null) {
            tracker.close();
            tracker = null;
        }
    }

    /** The highest service-ranked FlowQueryService currently registered (ServiceTracker resolves the ranking). */
    private FlowQueryService delegate() {
        final ServiceTracker<FlowQueryService, FlowQueryService> t = tracker;
        final FlowQueryService svc = (t != null) ? t.getService() : null;
        if (svc == null) {
            throw new IllegalStateException("No FlowQueryService is currently available.");
        }
        return svc;
    }

    @Override
    public CompletableFuture<Long> getFlowCount(final List<Filter> filters) {
        return delegate().getFlowCount(filters);
    }

    @Override
    public CompletableFuture<List<String>> getApplications(final String matchingPrefix, final long limit, final List<Filter> filters) {
        return delegate().getApplications(matchingPrefix, limit, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(final int N, final boolean includeOther, final List<Filter> filters) {
        return delegate().getTopNApplicationSummaries(N, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(final Set<String> applications, final boolean includeOther, final List<Filter> filters) {
        return delegate().getApplicationSummaries(applications, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(final Set<String> applications, final long step, final boolean includeOther, final List<Filter> filters) {
        return delegate().getApplicationSeries(applications, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(final int N, final long step, final boolean includeOther, final List<Filter> filters) {
        return delegate().getTopNApplicationSeries(N, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<String>> getConversations(final String locationPattern, final String protocolPattern,
                                                            final String lowerIPPattern, final String upperIPPattern,
                                                            final String applicationPattern, final long limit, final List<Filter> filters) {
        return delegate().getConversations(locationPattern, protocolPattern, lowerIPPattern, upperIPPattern, applicationPattern, limit, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(final int N, final boolean includeOther, final List<Filter> filters) {
        return delegate().getTopNConversationSummaries(N, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(final Set<String> conversations, final boolean includeOther, final List<Filter> filters) {
        return delegate().getConversationSummaries(conversations, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(final Set<String> conversations, final long step, final boolean includeOther, final List<Filter> filters) {
        return delegate().getConversationSeries(conversations, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(final int N, final long step, final boolean includeOther, final List<Filter> filters) {
        return delegate().getTopNConversationSeries(N, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<String>> getHosts(final String regex, final long limit, final List<Filter> filters) {
        return delegate().getHosts(regex, limit, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(final int N, final boolean includeOther, final List<Filter> filters) {
        return delegate().getTopNHostSummaries(N, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(final Set<String> hosts, final boolean includeOther, final List<Filter> filters) {
        return delegate().getHostSummaries(hosts, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(final Set<String> hosts, final long step, final boolean includeOther, final List<Filter> filters) {
        return delegate().getHostSeries(hosts, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(final int N, final long step, final boolean includeOther, final List<Filter> filters) {
        return delegate().getTopNHostSeries(N, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<String>> getFieldValues(final LimitedCardinalityField field, final List<Filter> filters) {
        return delegate().getFieldValues(field, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getFieldSummaries(final LimitedCardinalityField field, final List<Filter> filters) {
        return delegate().getFieldSummaries(field, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getFieldSeries(final LimitedCardinalityField field, final long step, final List<Filter> filters) {
        return delegate().getFieldSeries(field, step, filters);
    }
}