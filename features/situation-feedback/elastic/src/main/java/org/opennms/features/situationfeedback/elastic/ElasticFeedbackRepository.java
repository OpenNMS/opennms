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
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.AlarmFeedbackListener;
import org.opennms.features.situationfeedback.api.FeedbackException;
import org.opennms.features.situationfeedback.api.FeedbackRepository;
import org.opennms.features.jest.client.bulk.BulkRequest;
import org.opennms.features.jest.client.bulk.BulkWrapper;
import org.opennms.features.jest.client.index.IndexStrategy;
import org.opennms.features.jest.client.template.IndexSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import io.searchbox.client.JestClient;
import io.searchbox.core.Bulk;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.SearchResult.Hit;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;

public class ElasticFeedbackRepository implements FeedbackRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticFeedbackRepository.class);

    private static final String INDEX_NAME = "situation-feedback";

    private final Gson gson = new Gson();

    private final ElasticFeedbackRepositoryInitializer initializer;

    private final JestClient client;

    private final int bulkRetryCount;

    private final IndexSettings indexSettings;

    private IndexStrategy indexStrategy;

    /**
     * The collection of listeners interested in alarm feedback, populated via runtime binding.
     */
    private final Collection<AlarmFeedbackListener> alarmFeedbackListeners = new CopyOnWriteArrayList<>();

    public ElasticFeedbackRepository(JestClient jestClient, IndexStrategy indexStrategy, int bulkRetryCount, ElasticFeedbackRepositoryInitializer initializer) {
        this.client = jestClient;
        this.indexStrategy = indexStrategy;
        this.bulkRetryCount = bulkRetryCount;
        this.initializer = initializer;
        this.indexSettings = initializer.getIndexSettings();
    }

    @Override
    public void persist(List<AlarmFeedback> feedback) throws FeedbackException {
        ensureInitialized();
        if (LOG.isDebugEnabled()) {
            for (AlarmFeedback fb : feedback) {
                LOG.debug("Persisting {} feedback.", fb);
            }
        }

        List<FeedbackDocument> feedbackDocuments = feedback.stream().map(FeedbackDocument::from).collect(Collectors.toList());
        BulkRequest<FeedbackDocument> bulkRequest = new BulkRequest<>(client, feedbackDocuments, (documents) -> {
            final Bulk.Builder bulkBuilder = new Bulk.Builder();
            for (FeedbackDocument document : documents) {
                final String index = indexStrategy.getIndex(indexSettings, INDEX_NAME, Instant.ofEpochMilli(document.getTimestamp()));
                final Index.Builder indexBuilder = new Index.Builder(document).index(index);
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

        notifyListeners(feedback);
    }

    @Override
    public Collection<AlarmFeedback> getFeedback(String situationKey) throws FeedbackException {
        String query = "{\n" + "  \"query\": { \"match\": { \"situation_key\": " + gson.toJson(situationKey) + " } }\n" + "}";
        return search(query);
    }
    
    @Override
    public List<AlarmFeedback> getAllFeedback() throws FeedbackException {
        String query = "{\n" + "\t\"query\": {\"match_all\": {}},\n\t\"sort\": [{\"@timestamp\": {\"order\" : \"asc\"}}]\n" + "}";
        return search(query);
    }

    @Override
    public List<String> getTags(String prefix) throws FeedbackException {
        String query = "{\n" + 
                "  \"size\": 0,\n" + 
                "  \"aggs\": {\n" + 
                "    \"terms\": {\n" + 
                "      \"terms\": {\n" + 
                "        \"field\": \"tags\",\n" + 
                "        \"include\": \"" + gson.toJson(prefix) + ".*\"\n" + 
                "      }\n" + 
                "    }\n" + 
                "  }\n" + 
                "}";

        Search.Builder builder = new Search.Builder(query);
        Search search = builder.build();
        SearchResult result;
        try {
            result = client.execute(search);
        } catch (IOException e) {
            throw new FeedbackException("Failed to execute Tags search", e);
        }
        if (result == null) {
            return Collections.emptyList();
        }
        final MetricAggregation aggregations = result.getAggregations();
        if (aggregations == null) {
            return Collections.emptyList();
        }
        final TermsAggregation terms = aggregations.getTermsAggregation("terms");
        if (terms == null) {
            return Collections.emptyList();
        }
        return terms.getBuckets().stream().map(TermsAggregation.Entry::getKey).collect(Collectors.toList());
    }

    /**
     * Add listeners to {@link #alarmFeedbackListeners} during runtime as they become available.
     */
    public synchronized void onBind(AlarmFeedbackListener alarmFeedbackListener, Map properties) {
        LOG.debug("bind called with {}: {}", alarmFeedbackListener, properties);

        if (alarmFeedbackListener != null) {
            alarmFeedbackListeners.add(alarmFeedbackListener);
        }
    }

    /**
     * Remove listeners from {@link #alarmFeedbackListeners} during runtime as they become unavailable.
     */
    public synchronized void onUnbind(AlarmFeedbackListener alarmFeedbackListener, Map properties) {
        LOG.debug("Unbind called with {}: {}", alarmFeedbackListener, properties);

        if (alarmFeedbackListener != null) {
            alarmFeedbackListeners.remove(alarmFeedbackListener);
        }
    }

    private void notifyListeners(List<AlarmFeedback> feedback) {
        LOG.debug("Notifying listeners {} of feedback {}", alarmFeedbackListeners, feedback);
        alarmFeedbackListeners.forEach(listener -> {
            try {
                LOG.trace("Notifying listener {}", listener);
                listener.handleAlarmFeedback(feedback);
            } catch (Exception e) {
                LOG.warn("Failed to notify listener of alarm feedback", e);
            }
        });
    }

    private List<AlarmFeedback> search(String query) throws FeedbackException {
        Search.Builder builder = new Search.Builder(query);
        try {
            return execute(builder.build());
        } catch (IOException e) {
            throw new FeedbackException("Failed to get feedback for query: " + query, e);
        }
    }

    private List<AlarmFeedback> execute(Search search) throws IOException, FeedbackException {
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
