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

/**
 * Interface for an Elasticsearch client that can connect to ElasticSearch
 */
public interface ElasticRestClient {

    /**
     * Gets the health status of the Elasticsearch cluster.
     *
     * @return cluster health status string (e.g., "green", "yellow", "red")
     * @throws IOException if a connection error occurs
     */
    String health() throws IOException;

    /**
     * Lists all templates currently registered in the Elasticsearch cluster.
     *
     * @return a map of template names to template definitions
     * @throws IOException if a connection error occurs
     */
    Map<String, String> listTemplates() throws IOException;


    /**
     * Closes the client and releases any resources.
     *
     * @throws IOException if an error occurs during close
     */
    void close() throws IOException;

    /**
     * Applies an ILM policy to Elasticsearch.
     */
    boolean applyILMPolicy(String policyName, String policyBody) throws IOException;

    /**
     * Applies a component template to Elasticsearch.
     */
    boolean applyComponentTemplate(String componentName, String componentBody) throws IOException;

    /**
     * Applies a composable index template to Elasticsearch.
     */
    boolean applyComposableIndexTemplate(String templateName, String templateBody) throws IOException;

    /**
     * Loads and applies all templates from configured directories.
     */
    int applyAllTemplatesFromDirectory(String templateDirectory) throws IOException;

    // Bulk Operations
    
    /**
     * Executes a bulk request with retry logic.
     *
     * @param bulkRequest the bulk request to execute
     * @return the bulk response
     * @throws IOException if an error occurs during execution
     */
    BulkResponse executeBulk(BulkRequest bulkRequest) throws IOException;

    /**
     * Executes a bulk request asynchronously.
     *
     * @param bulkRequest the bulk request to execute
     * @return a CompletableFuture containing the bulk response
     */
    CompletableFuture<BulkResponse> executeBulkAsync(BulkRequest bulkRequest);

    // Search Operations
    
    /**
     * Executes a search request.
     *
     * @param searchRequest the search request to execute
     * @return the search response
     * @throws IOException if an error occurs during search
     */
    SearchResponse search(SearchRequest searchRequest) throws IOException;

    /**
     * Executes a search request asynchronously.
     *
     * @param searchRequest the search request to execute
     * @return a CompletableFuture containing the search response
     */
    CompletableFuture<SearchResponse> searchAsync(SearchRequest searchRequest);
    
    // Legacy Template Support
    
    /**
     * Applies a legacy index template to Elasticsearch.
     * This is for backward compatibility with old template format.
     *
     * @param templateName the name of the template
     * @param templateBody the template body as JSON
     * @return true if successful
     * @throws IOException if an error occurs during template creation
     */
    boolean applyLegacyIndexTemplate(String templateName, String templateBody) throws IOException;
    
    /**
     * Gets server version information.
     *
     * @return version string (e.g., "8.11.0")
     * @throws IOException if an error occurs during version retrieval
     */
    String getServerVersion() throws IOException;

    /**
     * Deletes one or more indices from Elasticsearch.
     *
     * @param indices the indices to delete (supports wildcards)
     * @return true if the indices were successfully deleted
     * @throws IOException if an error occurs during deletion
     */
    boolean deleteIndex(String indices) throws IOException;
}