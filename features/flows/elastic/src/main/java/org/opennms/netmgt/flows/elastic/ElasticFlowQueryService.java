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
package org.opennms.netmgt.flows.elastic;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.opennms.features.jest.client.index.IndexSelector;
import org.opennms.netmgt.flows.api.Directional;
import org.opennms.netmgt.flows.api.FlowQueryService;
import org.opennms.netmgt.flows.filter.api.TimeRangeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

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

    /**
     * Rebuilds the table, mapping the row keys using the given function and fills
     * in missing cells with NaN values.
     */
    protected static <T> CompletableFuture<Table<Directional<T>, Long, Double>> mapTable(final Table<Directional<String>, Long, Double> source,
                                                                                         final Function<String, CompletableFuture<T>> fn) {
        final Set<Long> columnKeys = source.columnKeySet();

        final List<CompletableFuture<Map.Entry<Directional<T>, Map<Long, Double>>>> rowKeys = source.rowKeySet().stream()
                                                                                                    .map(rk -> fn.apply(rk.getValue()).thenApply(t -> Maps.immutableEntry(new Directional<>(t, rk.isIngress()), source.row(rk))))
                                                                                                    .collect(Collectors.toList());

        return ElasticFlowQueryService.transpose(rowKeys, Collectors.toList())
                                      .thenApply(rows -> {
                    final ImmutableTable.Builder<Directional<T>, Long, Double> target = ImmutableTable.builder();
                    for (final Map.Entry<Directional<T>, Map<Long, Double>> row : rows) {
                        for (final Long columnKey : columnKeys) {
                            Double value = row.getValue().get(columnKey);
                            if (value == null) {
                                value = Double.NaN;
                            }
                            target.put(row.getKey(), columnKey, value);
                        }
                    }
                    return target.build();
                });
    }

    protected static <T, A, R> CompletableFuture<R> transpose(final Iterable<CompletableFuture<T>> futures,
                                                              final Collector<? super T, A, R> collector) {
        final CompletableFuture<T>[] array = Iterables.toArray(futures, CompletableFuture.class);
        return CompletableFuture.allOf(array)
                                .thenApply(v -> Arrays.stream(array)
                        .map(CompletableFuture::join)
                        .collect(collector));
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
