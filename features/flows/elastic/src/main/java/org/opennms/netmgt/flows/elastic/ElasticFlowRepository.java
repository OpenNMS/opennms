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
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.api.FlowException;
import org.opennms.netmgt.flows.api.FlowRepository;
import org.opennms.netmgt.flows.api.IndexStrategy;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.flows.api.QueryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.ByteStreams;

import io.searchbox.action.Action;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.Bulk;
import io.searchbox.core.BulkResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.indices.template.PutTemplate;

public class ElasticFlowRepository implements FlowRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticFlowRepository.class);

    private static final String FLOW_TEMPLATE_NAME = "netflow";

    private static final String TEMPLATE_RESOURCE = "/netflow-template.json";

    private final JestClient client;

    private final IndexStrategy indexStrategy;

    public ElasticFlowRepository(JestClient jestClient, IndexStrategy indexStrategy) {
        this.client = Objects.requireNonNull(jestClient);
        this.indexStrategy = Objects.requireNonNull(indexStrategy);
    }

    // Is invoked by the blueprint container and should take care of all initialization work, which may be
    // required in order to persist flows later on
    public void init() throws IOException {
        // Read template
        final InputStream inputStream = getClass().getResourceAsStream(TEMPLATE_RESOURCE);
        if (inputStream == null) {
            throw new NullPointerException("Template from '" + TEMPLATE_RESOURCE +  "' is null");
        }
        final byte[] bytes = new byte[inputStream.available()];
        ByteStreams.readFully(inputStream, bytes);

        // Post it to elastic
        final PutTemplate putTemplate = new PutTemplate.Builder(FLOW_TEMPLATE_NAME, new String(bytes)).build();
        final JestResult result = client.execute(putTemplate);
        if (!result.isSucceeded()) {
            // In case the template could not be created, we bail
            throw new IllegalStateException("Template '" + FLOW_TEMPLATE_NAME + "' could not be persisted. Reason: " + result.getErrorMessage());
        }
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
                    LOG.error("Error while writing flows: {}", result.getErrorMessage());
                }
            }
        } else {
            LOG.warn("Received empty or null flows. Nothing to do.");
        }
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
            final T result = client.execute(clientRequest);
            return result;
        } catch (IOException e) {
            throw new FlowException("Error executing query", e);
        }
    }

    private SearchResult search(String query) throws FlowException {
        final SearchResult result = executeRequest(new Search.Builder(query)
                .addType("flow")
                .build());
        if (!result.isSucceeded()) {
            LOG.error("Error reading flows {}", result.getErrorMessage());
            throw new QueryException("Could not read flows from repository. " + result.getErrorMessage());
        }
        return result;
    }
}
