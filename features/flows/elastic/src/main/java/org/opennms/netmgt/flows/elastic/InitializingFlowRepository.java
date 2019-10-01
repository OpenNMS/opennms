/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.opennms.features.jest.client.ConnectionPoolShutdownException;
import org.opennms.features.jest.client.template.IndexSettings;
import org.opennms.netmgt.flows.api.Conversation;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.Host;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.api.UnrecoverableFlowException;
import org.opennms.netmgt.flows.filter.api.Filter;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Table;

import io.searchbox.client.JestClient;

/**
 * This {@link FlowRepository} wrapper will ensure that the repository has
 * been initialized before any *write* calls are made to the given delegate.
 */
public class InitializingFlowRepository implements FlowRepository {

    private final ElasticFlowRepositoryInitializer initializer;
    private final FlowRepository delegate;

    public InitializingFlowRepository(final BundleContext bundleContext, final FlowRepository delegate, final JestClient client, final IndexSettings indexSettings) {
        this(delegate, new ElasticFlowRepositoryInitializer(bundleContext, client, indexSettings));
    }

    protected InitializingFlowRepository(final FlowRepository delegate, final JestClient client) {
        this(delegate, new ElasticFlowRepositoryInitializer(client));
    }

    private InitializingFlowRepository(final FlowRepository delegate, final ElasticFlowRepositoryInitializer initializer) {
        this.delegate = Objects.requireNonNull(delegate);
        this.initializer = Objects.requireNonNull(initializer);
    }

    @Override
    public void persist(Collection<Flow> flows, FlowSource source) throws FlowException {
        try {
            ensureInitialized();
            delegate.persist(flows, source);
        } catch (ConnectionPoolShutdownException ex) {
            throw new UnrecoverableFlowException(ex.getMessage(), ex);
        }
    }

    @Override
    public CompletableFuture<Long> getFlowCount(List<Filter> filters) {
        return delegate.getFlowCount(filters);
    }

    @Override
    public CompletableFuture<List<String>> getApplications(String matchingPrefix, long limit, List<Filter> filters) {
        return delegate.getApplications(matchingPrefix, limit, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getApplicationSeries(Set<String> applications,
                                                                                            long step,
                                                                                            boolean includeOther,
                                                                                            List<Filter> filters) {
        return delegate.getApplicationSeries(applications, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplicationSummaries(int N, boolean includeOther, List<Filter> filters) {
        return delegate.getTopNApplicationSummaries(N, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getApplicationSummaries(Set<String> applications,
                                                                                   boolean includeOther,
                                                                                   List<Filter> filters) {
        return delegate.getApplicationSummaries(applications, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return delegate.getTopNApplicationSeries(N, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<String>> getConversations(String locationPattern, String protocolPattern,
                                                            String lowerIPPattern, String upperIPPattern,
                                                            String applicationPattern, long limit,
                                                            List<Filter> filters) {
        return delegate.getConversations(locationPattern, protocolPattern, lowerIPPattern, upperIPPattern,
                applicationPattern, limit, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getTopNConversationSummaries(int N, boolean includeOther, List<Filter> filters) {
        return delegate.getTopNConversationSummaries(N, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Conversation>>> getConversationSummaries(Set<String> conversations, boolean includeOther, List<Filter> filters) {
        return delegate.getConversationSummaries(conversations, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getConversationSeries(Set<String> conversations, long step, boolean includeOther, List<Filter> filters) {
        return delegate.getConversationSeries(conversations, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Conversation>, Long, Double>> getTopNConversationSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return delegate.getTopNConversationSeries(N, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<String>> getHosts(String regex, long limit, List<Filter> filters) {
        return delegate.getHosts(regex, limit, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getTopNHostSummaries(int N, boolean includeOther,
                                                                              List<Filter> filters) {
        return delegate.getTopNHostSummaries(N, includeOther, filters);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<Host>>> getHostSummaries(Set<String> hosts, boolean includeOther, List<Filter> filters) {
        return delegate.getHostSummaries(hosts, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getHostSeries(Set<String> hosts, long step,
                                                                                     boolean includeOther,
                                                                                     List<Filter> filters) {
        return delegate.getHostSeries(hosts, step, includeOther, filters);
    }

    @Override
    public CompletableFuture<Table<Directional<Host>, Long, Double>> getTopNHostSeries(int N, long step, boolean includeOther, List<Filter> filters) {
        return delegate.getTopNHostSeries(N, step, includeOther, filters);
    }

    private void ensureInitialized() {
        if (!initializer.isInitialized()) {
            initializer.initialize();
        }
    }

}
