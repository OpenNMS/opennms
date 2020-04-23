/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.opennms.features.jest.client.index.IndexSelector;
import org.opennms.netmgt.flows.api.FlowQueryService;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

public abstract class ElasticFlowQueryService implements FlowQueryService {
    private static final Logger LOG = LoggerFactory.getLogger(ElasticFlowQueryService.class);

    private final JestClient client;
    private final IndexSelector indexSelector;

    public ElasticFlowQueryService(JestClient client, IndexSelector indexSelector) {
        this.client = Objects.requireNonNull(client);
        this.indexSelector = Objects.requireNonNull(indexSelector);
    }

    public CompletableFuture<SearchResult> searchAsync(String query, TimeRangeFilter timeRangeFilter) {
        Search.Builder builder = new Search.Builder(query);
        if(timeRangeFilter != null) {
            final List<String> indices = indexSelector.getIndexNames(timeRangeFilter.getStart(), timeRangeFilter.getEnd());
            builder.addIndices(indices);
            builder.setParameter("ignore_unavailable", "true"); // ignore unknown index

            LOG.debug("Executing asynchronous query on {}: {}", indices, query);
        } else {
            LOG.debug("Executing asynchronous query on all indices: {}", query);
        }
        return executeAsync(builder.build());
    }

    public <T extends JestResult> CompletableFuture<T> executeAsync(Action<T> action) {
        final CompletableFuture<T> future = new CompletableFuture<>();
        client.executeAsync(action, new JestResultHandler<T>() {
            @Override
            public void completed(T result) {
                if (!result.isSucceeded()) {
                    future.completeExceptionally(new Exception(result.getErrorMessage()));
                } else {
                    future.complete(result);
                }
            }
            @Override
            public void failed(Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

}
