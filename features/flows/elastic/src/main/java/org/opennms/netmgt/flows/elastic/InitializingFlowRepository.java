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
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.flows.api.ConversationKey;
import org.opennms.netmgt.flows.api.Converter;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.TrafficSummary;
import org.opennms.netmgt.flows.elastic.template.IndexSettings;

import com.google.common.collect.Table;

import io.searchbox.client.JestClient;

/**
 * This {@link FlowRepository} wrapper will ensure that the repository has
 * been initialized before any *write* calls are made to the given delegate.
 */
public class InitializingFlowRepository implements FlowRepository {

    private final ElasticFlowRepositoryInitializer initializer;
    private final FlowRepository delegate;

    public InitializingFlowRepository(final FlowRepository delegate, final JestClient client, final IndexSettings indexSettings) {
        Objects.requireNonNull(client);
        Objects.requireNonNull(delegate);
        Objects.requireNonNull(indexSettings);

        this.initializer = new ElasticFlowRepositoryInitializer(client, indexSettings);
        this.delegate = delegate;
    }

    protected InitializingFlowRepository(final FlowRepository delegate, final JestClient client) {
        this(delegate, client, new IndexSettings());
    }

    @Override
    public <P> void persist(Collection<? extends P> packets, FlowSource source, Converter<P> converter) throws FlowException {
        ensureInitialized();
        delegate.persist(packets, source, converter);
    }

    @Override
    public CompletableFuture<Long> getFlowCount(long start, long end) {
        return delegate.getFlowCount(start, end);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<String>>> getTopNApplications(int N, long start, long end) {
        return delegate.getTopNApplications(N, start, end);
    }

    @Override
    public CompletableFuture<Table<Directional<String>, Long, Double>> getTopNApplicationsSeries(int N, long start, long end, long step) {
        return delegate.getTopNApplicationsSeries(N, start, end, step);
    }

    @Override
    public CompletableFuture<List<TrafficSummary<ConversationKey>>> getTopNConversations(int N, long start, long end) {
        return delegate.getTopNConversations(N, start, end);
    }

    @Override
    public CompletableFuture<Table<Directional<ConversationKey>, Long, Double>> getTopNConversationsSeries(int N, long start, long end, long step) {
        return delegate.getTopNConversationsSeries(N, start, end, step);
    }

    private void ensureInitialized() {
        if (!initializer.isInitialized()) {
            initializer.initialize();
        }
    }

}
