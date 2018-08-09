/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
package org.opennms.features.situationfeedback.elastic;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.FeedbackException;
import org.opennms.features.situationfeedback.api.FeedbackRepository;
import org.opennms.plugins.elasticsearch.rest.bulk.BulkRequest;
import org.opennms.plugins.elasticsearch.rest.bulk.BulkWrapper;
import org.opennms.plugins.elasticsearch.rest.index.IndexStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;

public class ElasticFeedbackRepository implements FeedbackRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticFeedbackRepository.class);

    private static final String TYPE = "situation-feedback";

    private final ElasticFeedbackRepositoryInitializer initializer;

    private final JestClient client;

    private final int bulkRetryCount;

    private IndexStrategy indexStrategy;

    public ElasticFeedbackRepository(JestClient jestClient, IndexStrategy indexStrategy, int bulkRetryCount, ElasticFeedbackRepositoryInitializer initializer) {
        this.client = jestClient;
        this.indexStrategy = indexStrategy;
        this.bulkRetryCount = bulkRetryCount;
        this.initializer = initializer;
    }

    @Override
    public void persist(Collection<AlarmFeedback> feedback) throws FeedbackException {
        ensureInitialized();
        if (LOG.isDebugEnabled()) {
            for (AlarmFeedback fb : feedback) {
                LOG.debug("Persiting {} feedback.", fb);
            }
        }

        List<FeedbackDocument> feedbackDocuments = feedback.stream().map(FeedbackDocument::from).collect(Collectors.toList());
        BulkRequest<FeedbackDocument> bulkRequest = new BulkRequest<>(client, feedbackDocuments, (documents) -> {
            final Bulk.Builder bulkBuilder = new Bulk.Builder();
            for (FeedbackDocument document : documents) {
                final String index = indexStrategy.getIndex(TYPE, Instant.ofEpochMilli(document.getTimestamp()));
                final Index.Builder indexBuilder = new Index.Builder(document).index(index).type(TYPE);
                bulkBuilder.addAction(indexBuilder.build());
            }
            return new BulkWrapper(bulkBuilder);
        }, bulkRetryCount);
        // the bulk request considers retries
        try {
            bulkRequest.execute();
        } catch (IOException e) {
            LOG.error("Failed to persist feedback [{}]: {}", feedback, e.getMessage());
            throw new FeedbackException("Failed to persist feedback", e);
        }
    }

    @Override
    public Collection<AlarmFeedback> getFeedback(String situationKey) throws FeedbackException {
        String query = "{\n" + "  \"query\": { \"match\": { \"situation_key\": \"" + situationKey + "\" } }\n" + "}";
        return search(query);
    }

    private Collection<AlarmFeedback> search(String query) throws FeedbackException {
        Search.Builder builder = new Search.Builder(query).addType(TYPE);
        try {
            return execute(builder.build());
        } catch (IOException e) {
            throw new FeedbackException("Failed to get feedback for query: " + query, e);
        }
    }

    private Collection<AlarmFeedback> execute(Search search) throws IOException, FeedbackException {
        SearchResult result = client.execute(search);
        if (result == null) {
            throw new FeedbackException("Failed to get result");
        }
        List<Hit<FeedbackDocument, Void>> feedback = result.getHits(FeedbackDocument.class);
        if (feedback == null) {
            return Collections.emptyList();
        }
        return feedback.stream().map(hit -> hit.source).map(FeedbackDocument::toAlarmFeedback).collect(Collectors.toList());
    }

    private void ensureInitialized() {
        if (!initializer.isInitialized()) {
            LOG.debug("Initializing Repository.");
            initializer.initialize();
        }
    }
}
