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
package org.opennms.features.elastic.client;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.opennms.features.elastic.client.model.BulkRequest;
import org.opennms.features.elastic.client.model.BulkResponse;
import org.opennms.features.elastic.client.model.SearchRequest;
import org.opennms.features.elastic.client.model.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;

/**
 * A decorator for ElasticRestClient that adds circuit breaker protection
 * to bulk and search operations.
 */
public class ElasticRestClientWithCircuitBreaker implements ElasticRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticRestClientWithCircuitBreaker.class);

    private final ElasticRestClient delegate;
    private final CircuitBreaker circuitBreaker;

    public ElasticRestClientWithCircuitBreaker(ElasticRestClient delegate, CircuitBreaker circuitBreaker) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
    }

    // Methods that need circuit breaker protection

    @Override
    public BulkResponse executeBulk(BulkRequest bulkRequest) throws IOException {
        try {
            return circuitBreaker.executeCallable(() -> delegate.executeBulk(bulkRequest));
        } catch (CallNotPermittedException e) {
            LOG.error("Circuit breaker is open. Bulk request rejected.", e);
            throw new IOException("Circuit breaker is open. Request rejected.", e);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("Unexpected error during bulk execution", e);
        }
    }

    @Override
    public CompletableFuture<BulkResponse> executeBulkAsync(BulkRequest bulkRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeBulk(bulkRequest);
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute bulk request", e);
            }
        });
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        try {
            return circuitBreaker.executeCallable(() -> delegate.search(searchRequest));
        } catch (CallNotPermittedException e) {
            LOG.error("Circuit breaker is open. Search request rejected.", e);
            throw new IOException("Circuit breaker is open. Request rejected.", e);
        } catch (Exception e) {
            if (e instanceof IOException) {
                throw (IOException) e;
            }
            throw new IOException("Unexpected error during search execution", e);
        }
    }

    @Override
    public CompletableFuture<SearchResponse> searchAsync(SearchRequest searchRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return search(searchRequest);
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute search request", e);
            }
        });
    }

    // Delegate all other methods without circuit breaker protection

    @Override
    public String health() throws IOException {
        return delegate.health();
    }

    @Override
    public Map<String, String> listTemplates() throws IOException {
        return delegate.listTemplates();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }

    @Override
    public boolean applyILMPolicy(String policyName, String policyBody) throws IOException {
        return delegate.applyILMPolicy(policyName, policyBody);
    }

    @Override
    public boolean applyComponentTemplate(String componentName, String componentBody) throws IOException {
        return delegate.applyComponentTemplate(componentName, componentBody);
    }

    @Override
    public boolean applyComposableIndexTemplate(String templateName, String templateBody) throws IOException {
        return delegate.applyComposableIndexTemplate(templateName, templateBody);
    }

    @Override
    public int applyAllTemplatesFromDirectory(String templateDirectory) throws IOException {
        return delegate.applyAllTemplatesFromDirectory(templateDirectory);
    }

    @Override
    public boolean applyLegacyIndexTemplate(String templateName, String templateBody) throws IOException {
        return delegate.applyLegacyIndexTemplate(templateName, templateBody);
    }

    @Override
    public String getServerVersion() throws IOException {
        return delegate.getServerVersion();
    }

    @Override
    public boolean deleteIndex(String indices) throws IOException {
        return delegate.deleteIndex(indices);
    }
}