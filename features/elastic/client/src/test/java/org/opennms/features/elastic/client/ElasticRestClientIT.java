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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.features.elastic.client.model.BulkRequest;
import org.opennms.features.elastic.client.model.BulkResponse;
import org.opennms.features.elastic.client.model.SearchRequest;
import org.opennms.features.elastic.client.model.SearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.containers.ToxiproxyContainer;
import org.testcontainers.containers.Network;
import eu.rekawek.toxiproxy.Proxy;
import eu.rekawek.toxiproxy.ToxiproxyClient;


public class ElasticRestClientIT {

    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:7.17.9";

    private static final Logger LOG = LoggerFactory.getLogger(ElasticRestClientIT.class);

    private ExecutorService executor;

    @ClassRule
    public static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
    
    private DefaultElasticRestClient client;
    
    @Before
    public void setUp() throws Exception {

        String[] hosts = {elasticsearch.getHttpHostAddress()};
        client = new DefaultElasticRestClient(hosts);

        Awaitility.setDefaultTimeout(15, TimeUnit.SECONDS);
        Awaitility.setDefaultPollInterval(500, TimeUnit.MILLISECONDS);
    }
    
    @After
    public void tearDown() throws Exception {
        // Clean up test indices to ensure test isolation
        if (executor != null) {
            executor.shutdown();
        }
        try {
            Request deleteRequest = new Request("DELETE", "/netflow-*");
            client.getRestClient().performRequest(deleteRequest);
        } catch (Exception e) {
            // Ignore errors during cleanup
        }
        
        // Close the client
        if (client != null) {
            client.close();
        }

    }

    
    @Test
    public void testApplyAllTemplatesFromDirectory() throws IOException, URISyntaxException {

        String indexTemplate = "index-template";

        Path resourcePath = Paths.get(
                Objects.requireNonNull(getClass().getClassLoader().getResource("templates")).toURI());
        int count = client.applyAllTemplatesFromDirectory(resourcePath.toString());
        LOG.info("Applied {} templates", count);
        assertEquals("Should have applied 4 templates", 4, count);
        
        Awaitility.await().atMost(60, TimeUnit.SECONDS).pollInterval(2, TimeUnit.SECONDS).until(() -> {
            try {
                Request request = new Request("GET", "/_index_template/" + indexTemplate);
                Response response = client.getRestClient().performRequest(request);
                int statusCode = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());
                LOG.info("Index template verification - Status: {}, Body: {}", statusCode, body);

                return statusCode == 200 && 
                       body.contains(indexTemplate) && 
                       body.contains("composed_of") && 
                       body.contains("component-settings") && 
                       body.contains("component-mappings");
            } catch (Exception e) {
                LOG.error("Error verifying index template: {}", e.getMessage());
                return false;
            }
        });
        
        // Create an index that matches the pattern and verify settings & mappings are applied
        String indexName = "test-pattern-2-index";  // Match the pattern in the template
        LOG.info("Creating index to test template application: {}", indexName);
        
        try {
            Request createIndexRequest = new Request("PUT", "/" + indexName);
            Response createResponse = client.getRestClient().performRequest(createIndexRequest);
            LOG.info("Index creation response: {}", createResponse.getStatusLine().getStatusCode());
        } catch (Exception e) {
            LOG.error("Error creating index: {}", e.getMessage());
            Assert.fail("Failed to create index: " + e.getMessage());
        }
        
        // Verify the index was created with the expected settings and mappings
        Awaitility.await().atMost(30, TimeUnit.SECONDS).pollInterval(1, TimeUnit.SECONDS).until(() -> {
            try {
                // Check settings separately
                Request settingsRequest = new Request("GET", "/" + indexName + "/_settings");
                Response settingsResponse = client.getRestClient().performRequest(settingsRequest);
                String settingsBody = EntityUtils.toString(settingsResponse.getEntity());
                LOG.info("Index settings response status: {}", settingsResponse.getStatusLine().getStatusCode());
                
                boolean settingsMatch = settingsResponse.getStatusLine().getStatusCode() == 200 &&
                                       settingsBody.contains("\"number_of_shards\":\"1\"");
                
                if (!settingsMatch) {
                    LOG.info("Settings don't match expected values");
                    return false;
                }
                
                // Check mappings separately
                Request mappingsRequest = new Request("GET", "/" + indexName + "/_mapping");
                Response mappingsResponse = client.getRestClient().performRequest(mappingsRequest);
                String mappingsBody = EntityUtils.toString(mappingsResponse.getEntity());
                LOG.info("Index mappings response status: {}", mappingsResponse.getStatusLine().getStatusCode());
                
                boolean mappingsMatch = mappingsResponse.getStatusLine().getStatusCode() == 200 &&
                                      mappingsBody.contains("field1") &&
                                      mappingsBody.contains("field2");
                
                if (!mappingsMatch) {
                    LOG.info("Mappings don't match expected values");
                    return false;
                }
                
                return true;
            } catch (Exception e) {
                LOG.error("Error verifying index settings and mappings: {}", e.getMessage());
                return false;
            }
        });
    }

    @Test
    public void testBulkIndexingFlowDocuments() throws IOException {
        // Create a bulk request with sample flow documents
        BulkRequest bulkRequest = new BulkRequest();
        
        // Create sample flow documents similar to what FlowRepository would create
        int flowCount = 100;
        for (int i = 0; i < flowCount; i++) {
            Map<String, Object> flow = createMockFlowDocument(i);
            bulkRequest.index("netflow-" + getIndexSuffix(), null, flow);
        }
        
        // Execute bulk request
        BulkResponse response = client.executeBulk(bulkRequest);
        
        // Verify response
        assertNotNull(response);
        assertFalse("Bulk operation should not have errors", response.hasErrors());
        assertEquals("Should have indexed all flows", flowCount, response.getItems().size());
        
        // Verify all items were created successfully
        for (BulkResponse.BulkItemResponse item : response.getItems()) {
            assertFalse("Item should not be failed", item.isFailed());
            assertTrue("Status should be 2xx", item.getStatus() >= 200 && item.getStatus() < 300);
            assertNotNull("Should have generated ID", item.getId());
            assertNotNull("Should have index name", item.getIndex());
        }
        
        // Force refresh to make documents searchable
        Request refreshRequest = new Request("POST", "/netflow-*/_refresh");
        client.getRestClient().performRequest(refreshRequest);
        
        // Verify documents were actually indexed by searching
        String searchQuery = "{\"query\": {\"match_all\": {}}}";
        SearchRequest searchRequest = SearchRequest.forIndices(List.of("netflow-*"), searchQuery);
        SearchResponse searchResponse = client.search(searchRequest);
        
        assertNotNull(searchResponse);
        assertEquals("Should find all indexed flows", flowCount, searchResponse.getHits().getTotalHits());
    }

    @Test
    public void testBulkWithMixedOperations() throws IOException {
        String indexName = "netflow-" + getIndexSuffix();
        
        // First, index a document that we'll update later using direct REST API
        String docId = "test-doc-1";
        Map<String, Object> initialFlow = createMockFlowDocument(1);
        Request indexRequest = new Request("PUT", "/" + indexName + "/_doc/" + docId);
        indexRequest.setJsonEntity(new Gson().toJson(initialFlow));
        client.getRestClient().performRequest(indexRequest);
        
        // Force refresh to make document searchable
        Request refreshRequest = new Request("POST", "/" + indexName + "/_refresh");
        client.getRestClient().performRequest(refreshRequest);
        
        // Create bulk request with mixed operations
        BulkRequest bulkRequest = new BulkRequest();
        
        // Index new documents
        bulkRequest.index(indexName, null, createMockFlowDocument(2));
        bulkRequest.index(indexName, null, createMockFlowDocument(3));
        
        // Update existing document
        Map<String, Object> updatedFlow = createMockFlowDocument(1);
        updatedFlow.put("updated", true);
        updatedFlow.put("bytes", 2048);
        bulkRequest.update(indexName, docId, updatedFlow);
        
        // Delete a document (even if it doesn't exist, should not fail)
        bulkRequest.delete(indexName, "non-existent-doc");
        
        // Execute bulk request
        BulkResponse response = client.executeBulk(bulkRequest);
        
        assertNotNull(response);
        assertEquals("Should have 4 operations", 4, response.getItems().size());
        
        // Check individual operations
        // First two should be successful index operations
        assertTrue("First index should succeed", response.getItems().get(0).getStatus() >= 200 && response.getItems().get(0).getStatus() < 300);
        assertTrue("Second index should succeed", response.getItems().get(1).getStatus() >= 200 && response.getItems().get(1).getStatus() < 300);
        
        // Update should succeed
        BulkResponse.BulkItemResponse updateResponse = response.getItems().get(2);
        assertEquals("Update should succeed", 200, updateResponse.getStatus());
        assertEquals("Update should have correct ID", docId, updateResponse.getId());
        
        // Delete of non-existent doc should return 404 but not be marked as error in bulk
        BulkResponse.BulkItemResponse deleteResponse = response.getItems().get(3);
        assertEquals("Delete should return 404", 404, deleteResponse.getStatus());
    }

    @Test
    public void testBulkWithRefreshParameter() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefresh("wait_for");
        
        // Add a single flow document
        bulkRequest.index("netflow-" + getIndexSuffix(), null, createMockFlowDocument(1));
        
        // Execute with refresh
        BulkResponse response = client.executeBulk(bulkRequest);
        assertNotNull(response);
        assertFalse(response.hasErrors());
        
        // Should be immediately searchable due to refresh=wait_for
        String searchQuery = "{\"query\": {\"match_all\": {}}}";
        SearchRequest searchRequest = SearchRequest.forIndices(List.of("netflow-*"), searchQuery);
        SearchResponse searchResponse = client.search(searchRequest);
        
        assertEquals("Document should be immediately searchable", 1, searchResponse.getHits().getTotalHits());
    }


    @Test
    public void testEmptyBulkRequest() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        
        // Execute empty bulk request
        BulkResponse response = client.executeBulk(bulkRequest);
        
        assertNotNull(response);
        assertFalse(response.hasErrors());
        assertEquals(0, response.getItems().size());
        assertEquals(0L, response.getTookInMillis());
    }

    /**
     * Create a mock flow document similar to what would be created by FlowRepository
     */
    private Map<String, Object> createMockFlowDocument(int index) {
        Map<String, Object> flow = new HashMap<>();
        
        // Basic flow fields
        flow.put("@timestamp", Instant.now().toEpochMilli());
        flow.put("@version", "1.0");
        
        // Network fields
        flow.put("srcAddr", "192.168." + (index / 256) + "." + (index % 256));
        flow.put("dstAddr", "10.0." + (index / 256) + "." + (index % 256));
        flow.put("srcPort", 1024 + index);
        flow.put("dstPort", 80 + (index % 10));
        
        // Flow metrics
        flow.put("bytes", 1024 * (index + 1));
        flow.put("packets", 10 * (index + 1));
        flow.put("protocol", index % 2 == 0 ? 6 : 17); // TCP or UDP
        
        // NetFlow specific fields
        flow.put("netflowVersion", "V5");
        flow.put("ipProtocolVersion", 4);
        flow.put("flowDirection", "ingress");
        flow.put("samplingInterval", 1);
        
        // Optional fields that might be null
        if (index % 3 == 0) {
            flow.put("application", "http");
        }
        if (index % 5 == 0) {
            flow.put("vlan", 100 + index);
        }
        
        return flow;
    }
    
    /**
     * Get current index suffix (YYYY.MM format)
     */
    private String getIndexSuffix() {
        Instant now = Instant.now();
        return String.format("%d.%02d", 
                now.atZone(java.time.ZoneId.systemDefault()).getYear(),
                now.atZone(java.time.ZoneId.systemDefault()).getMonthValue());
    }

    
    @Test
    public void testBulkAsyncExecution() throws Exception {
        BulkRequest bulkRequest = new BulkRequest();
        
        // Add multiple flow documents
        int flowCount = 50;
        for (int i = 0; i < flowCount; i++) {
            bulkRequest.index("netflow-" + getIndexSuffix(), null, createMockFlowDocument(i));
        }
        
        // Execute asynchronously
        CompletableFuture<BulkResponse> future = client.executeBulkAsync(bulkRequest);
        
        // Wait for completion
        BulkResponse response = future.get(10, TimeUnit.SECONDS);
        
        assertNotNull(response);
        assertFalse(response.hasErrors());
        assertEquals(flowCount, response.getItems().size());
    }

    @Test
    public void testBulkRetryWithNetworkFailure() throws Exception {

        // Create a custom network for this test
        Network network = Network.newNetwork();
        
        // Start separate Elasticsearch container for this test
        ElasticsearchContainer testElastic = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
                .withEnv("xpack.security.enabled", "false")
                .withEnv("discovery.type", "single-node")
                .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
                .withNetwork(network)
                .withNetworkAliases("elasticsearch");
        testElastic.start();

        // Start Toxiproxy container on same network
        ToxiproxyContainer toxiproxy = new ToxiproxyContainer("ghcr.io/shopify/toxiproxy:2.5.0")
                .withNetwork(network);
        toxiproxy.start();

        try {
            // Create Toxiproxy client
            ToxiproxyClient toxiproxyClient = new ToxiproxyClient(toxiproxy.getHost(), toxiproxy.getControlPort());

            // Create proxy to Elasticsearch
            Proxy elasticProxy = toxiproxyClient.createProxy("elasticsearch-proxy",
                    "0.0.0.0:8666",
                    "elasticsearch:9200");

            // Get proxy endpoint
            String proxyEndpoint = toxiproxy.getHost() + ":" + toxiproxy.getMappedPort(8666);
            LOG.info("Elasticsearch proxy available at: {}", proxyEndpoint);

            // Create client pointing to proxy with retry settings
            DefaultElasticRestClient retryClient = new DefaultElasticRestClient(proxyEndpoint, null, null);
            retryClient.setBulkRetryCount(6); // Allow retries
            retryClient.setConnTimeout(5000);
            retryClient.setReadTimeout(30000);
            retryClient.setRetryCooldown(500);

            // Verify it works initially through proxy
            String initialHealth = retryClient.health();
            LOG.info("Initial health check through proxy successful: {}", initialHealth);

            // Create a bulk request
            BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.index("test-retry-index", "test-retry-1", createTestDocument());

            // Execute successfully first
            BulkResponse response1 = retryClient.executeBulk(bulkRequest);
            assertNotNull("First response should not be null", response1);
            assertFalse("First response should not have errors", response1.hasErrors());

            // Now simulate network failure by disabling the proxy
            LOG.info("Disabling proxy to simulate network failure...");
            elasticProxy.disable();

            // Start bulk operation in background - this should fail and retry
            executor = Executors.newSingleThreadExecutor();
            AtomicInteger attemptCount = new AtomicInteger(0);

            CompletableFuture<BulkResponse> future = CompletableFuture.supplyAsync(() -> {
                try {
                    attemptCount.incrementAndGet();
                    LOG.info("Starting bulk operation during network failure, attempt #{}", attemptCount.get());
                    return retryClient.executeBulk(bulkRequest);
                } catch (IOException e) {
                    LOG.error("Bulk operation failed with IOException", e);
                    throw new RuntimeException("Bulk operation failed", e);
                }
            }, executor);

            // Wait to ensure operation starts and begins retrying
            Thread.sleep(4000);

            // Re-enable the proxy to restore connectivity
            LOG.info("Re-enabling proxy to restore connectivity...");
            elasticProxy.enable();

            // Wait for the operation to succeed
            BulkResponse response = future.get(30, TimeUnit.SECONDS);

            // Verify the response
            assertNotNull("Response should not be null", response);
            assertFalse("Response should not have errors", response.hasErrors());
            assertEquals("Response should have items", 1, response.getItems().size());

            LOG.info("Bulk retry test with network failure completed successfully");
            
            retryClient.close();

        } finally {
            toxiproxy.stop();
            testElastic.stop();
            network.close();
        }
    }

    /**
     * Create a simple test document
     */
    private Map<String, Object> createTestDocument() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("message", "test document for retry testing");
        doc.put("timestamp", System.currentTimeMillis());
        doc.put("field1", "value1");
        doc.put("field2", 42);
        return doc;
    }
}