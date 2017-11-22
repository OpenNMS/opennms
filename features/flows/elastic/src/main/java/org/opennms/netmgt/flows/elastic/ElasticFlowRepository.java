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

import static org.opennms.netmgt.flows.api.PersistenceException.FailedItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.IndexStrategy;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.flows.api.PersistenceException;
import org.opennms.netmgt.flows.api.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

public class ElasticFlowRepository implements FlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticFlowRepository.class);

    private final JestClient client;

    private final IndexStrategy indexStrategy;

    public ElasticFlowRepository(JestClient jestClient, IndexStrategy indexStrategy) {
        this.client = Objects.requireNonNull(jestClient);
        this.indexStrategy = Objects.requireNonNull(indexStrategy);
    }

    @Override
    public void save(List<NetflowDocument> flowDocuments) throws FlowException {
        if (flowDocuments != null && !flowDocuments.isEmpty()) {
            final String index = indexStrategy.getIndex(new Date());
            final String type = "flow";

            if (flowDocuments != null && !flowDocuments.isEmpty()) {
                final Bulk.Builder bulkBuilder = new Bulk.Builder();
                for (NetflowDocument document : flowDocuments) {
                    final Index.Builder indexBuilder = new Index.Builder(document)
                            .index(index)
                            .type(type);
                    bulkBuilder.addAction(indexBuilder.build());
                }
                final Bulk bulk = bulkBuilder.build();
                final BulkResult result = executeRequest(bulk);
                if (!result.isSucceeded()) {
                    final List<FailedItem> failedFlows = determineFailedFlows(flowDocuments, result);
                    throw new PersistenceException(result.getErrorMessage(), failedFlows);
                }
            }
        } else {
            LOG.warn("Received empty or null flows. Nothing to do.");
        }
    }

    private List<FailedItem> determineFailedFlows(List<NetflowDocument> flowDocuments, BulkResult result) {
        final List<FailedItem> failedFlows = new ArrayList<>();
        for (int i=0; i<result.getItems().size(); i++) {
            final BulkResult.BulkResultItem bulkResultItem = result.getItems().get(i);
            if (bulkResultItem.error != null && !bulkResultItem.error.isEmpty()) {
                final Exception cause = convertToException(bulkResultItem.error);
                final NetflowDocument failedFlow = flowDocuments.get(i);
                failedFlows.add(new FailedItem(failedFlow, cause));
            }
        }
        return failedFlows;
    }

    @Override
    public String rawQuery(String query) throws FlowException {
        final SearchResult result = search(query);
        return result.getJsonString();
    }

    @Override
    public List<NetflowDocument> findAll(String query) throws FlowException {
        final SearchResult result = search(query);
        final List<SearchResult.Hit<NetflowDocument, Void>> hits = result.getHits(NetflowDocument.class);
        final List<NetflowDocument> data = hits.stream().map(hit -> hit.source).collect(Collectors.toList());
        return data;
    }

    private <T extends JestResult> T executeRequest(Action<T> clientRequest) throws FlowException {
        try {
            T result = client.execute(clientRequest);
            return result;
        } catch (IOException ex) {
            LOG.error("An error occurred while executing the given request: {}", clientRequest, ex);
            throw new FlowException(ex.getMessage(), ex);
        }
    }

    private SearchResult search(String query) throws FlowException {
        final SearchResult result = executeRequest(new Search.Builder(query)
                .addType("flow")
                .ignoreUnavailable(true)
                .build());
        if (!result.isSucceeded()) {
            LOG.error("Error reading flows. Query: {}, error message: {}", query, result.getErrorMessage());
            throw new QueryException("Could not read flows from repository. " + result.getErrorMessage());
        }
        return result;
    }

    protected static Exception convertToException(String error) {
        // Read error data
        final JsonObject errorObject = new JsonParser().parse(error).getAsJsonObject();
        final String errorType = errorObject.get("type").getAsString();
        final String errorReason = errorObject.get("reason").getAsString();
        final JsonElement errorCause = errorObject.get("caused_by");

        // Create Exception
        final String errorMessage = String.format("%s: %s", errorType, errorReason);
        if (errorCause != null) {
            return new Exception(errorMessage, convertToException(errorCause.toString()));
        }
        return new Exception(errorMessage);
    }
}
