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

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.api.FlowSource;
import org.opennms.netmgt.flows.api.NF5Packet;
import org.opennms.plugins.elasticsearch.rest.BulkResultWrapper;
import org.opennms.plugins.elasticsearch.rest.FailedItem;
import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.client.JestResultHandler;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

public class ElasticFlowRepository implements FlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticFlowRepository.class);

    private static final String TYPE = "netflow";

    private final JestClient client;

    private final IndexStrategy indexStrategy;

    private final DocumentEnricher documentEnricher;

    private final Netflow5Converter converter = new Netflow5Converter();

    /**
     * Flows/second throughput
     */
    private final Meter flowsPersistedMeter;

    /**
     * Time taken to convert and enrich the flows in a log
     */
    private final Timer logEnrichementTimer;

    /**
     * Time taken to persist the flows in a log
     */
    private final Timer logPersistingTimer;


    /**
     * Number of flows in a log
     */
    private final Histogram flowsPerLog;


    public ElasticFlowRepository(MetricRegistry metricRegistry, JestClient jestClient, IndexStrategy indexStrategy, DocumentEnricher documentEnricher) {
        this.client = Objects.requireNonNull(jestClient);
        this.indexStrategy = Objects.requireNonNull(indexStrategy);
        this.documentEnricher = Objects.requireNonNull(documentEnricher);

        flowsPersistedMeter = metricRegistry.meter("flowsPersisted");
        logEnrichementTimer = metricRegistry.timer("logEnrichment");
        logPersistingTimer = metricRegistry.timer("logPersisting");
        flowsPerLog = metricRegistry.histogram("flowsPerLog");
    }

    @Override
    public void persistNetFlow5Packets(Collection<? extends NF5Packet> packets, FlowSource source) throws FlowException {
        LOG.debug("Converting {} Netflow 5 packets from {} to flow documents.", packets.size(), source);
        final List<FlowDocument> flowDocuments = packets.stream()
                .map(converter::convert)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        enrichAndPersistFlows(flowDocuments, source);
    }

    private void enrichAndPersistFlows(List<FlowDocument> flowDocuments, FlowSource source) throws FlowException {
        // Track the number of flows per call
        flowsPerLog.update(flowDocuments.size());

        if (flowDocuments.isEmpty()) {
            LOG.info("Received empty flows. Nothing to do.");
            return;
        }

        LOG.debug("Enriching {} flow documents.", flowDocuments.size());
        try (final Timer.Context ctx = logEnrichementTimer.time()) {
            documentEnricher.enrich(flowDocuments, source);
        }

        LOG.debug("Persisting {} flow documents.", flowDocuments.size());
        try (final Timer.Context ctx = logPersistingTimer.time()) {
            final String index = indexStrategy.getIndex(new Date());

            final Bulk.Builder bulkBuilder = new Bulk.Builder();
            for (FlowDocument flowDocument : flowDocuments) {
                final Index.Builder indexBuilder = new Index.Builder(flowDocument)
                        .index(index)
                        .type(TYPE);
                bulkBuilder.addAction(indexBuilder.build());
            }
            final Bulk bulk = bulkBuilder.build();
            final BulkResultWrapper result = new BulkResultWrapper(executeRequest(bulk));
            if (!result.isSucceeded()) {
                final List<FailedItem<FlowDocument>> failedFlows = result.getFailedItems(flowDocuments);
                throw new PersistenceException(result.getErrorMessage(), failedFlows);
            }

            flowsPersistedMeter.mark(flowDocuments.size());
        }
    }

    @Override
    public CompletableFuture<Long> getFlowCount(long start, long end) {
        final String query = "{\n" +
                "  \"size\": 0,\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"filter\": [\n" +
                "        {\n" +
                "          \"range\": {\n" +
                "            \"@timestamp\": {\n" +
                String.format("              \"gte\": %d,\n", start) +
                String.format("              \"lte\": %d,\n", end) +
                "              \"format\": \"epoch_millis\"\n" +
                "            }\n" +
                "          }\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  }\n" +
                "}\n";
        return searchAsync(query).thenApply(SearchResult::getTotal);
    }

    private <T extends JestResult> T executeRequest(Action<T> clientRequest) throws FlowException {
        try {
            return client.execute(clientRequest);
        } catch (IOException ex) {
            LOG.error("An error occurred while executing the given request: {}", clientRequest, ex);
            throw new FlowException(ex.getMessage(), ex);
        }
    }

    private CompletableFuture<SearchResult> searchAsync(String query) {
        final CompletableFuture<SearchResult> future = new CompletableFuture<>();
        client.executeAsync(new Search.Builder(query)
                .addType(TYPE)
                .build(), new JestResultHandler<SearchResult>() {

            @Override
            public void completed(SearchResult result) {
                future.complete(result);
            }

            @Override
            public void failed(Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

}
