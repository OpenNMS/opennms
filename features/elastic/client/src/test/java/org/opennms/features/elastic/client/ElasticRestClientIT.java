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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.awaitility.Awaitility;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.elasticsearch.ElasticsearchContainer;


public class ElasticRestClientIT {

    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:8.7.0";

    private static final Logger LOG = LoggerFactory.getLogger(DefaultElasticRestClient.class);

    @ClassRule
    public static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
    
    private DefaultElasticRestClient client;
    private Path tempDirectory;
    
    @Before
    public void setUp() throws Exception {
        tempDirectory = Files.createTempDirectory("es-client-test");

        Files.createDirectories(tempDirectory.resolve("policies"));
        Files.createDirectories(tempDirectory.resolve("components"));
        Files.createDirectories(tempDirectory.resolve("index-templates"));

        String[] hosts = {elasticsearch.getHttpHostAddress()};
        client = new DefaultElasticRestClient(hosts);
        client.init();
        client.connect();

        Awaitility.setDefaultTimeout(20, TimeUnit.SECONDS);
        Awaitility.setDefaultPollInterval(500, TimeUnit.MILLISECONDS);
    }
    
    @After
    public void tearDown() throws Exception {
        
        // Close the client
        if (client != null) {
            client.close();
        }
        // Delete temp directory
        deleteDirectory(tempDirectory.toFile());
    }


    @Test
    public void testILMPolicy() throws IOException {
        // Prepare ILM policy
        String policyName = "test-policy";
        String policyJson = "{\n" +
                "  \"policy\": {\n" +
                "    \"phases\": {\n" +
                "      \"hot\": {\n" +
                "        \"min_age\": \"0ms\",\n" +
                "        \"actions\": {}\n" +
                "      },\n" +
                "      \"delete\": {\n" +
                "        \"min_age\": \"1d\",\n" +
                "        \"actions\": {\n" +
                "          \"delete\": {}\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        // Apply the policy
        boolean result = client.applyILMPolicy(policyName, policyJson);
        assertTrue("ILM policy application should succeed", result);

        Awaitility.await().until(() -> {
            try {
                Request request = new Request("GET", "/_ilm/policy/" + policyName);
                Response response = client.getRestClient().performRequest(request);
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                LOG.info("ILM policy response (status: {}): {}", statusCode, responseBody);
                return statusCode == 200 && responseBody.contains(policyName);
            } catch (Exception e) {
                LOG.error("Error waiting for ILM policy: {}", e.getMessage());
                return false;
            }
        });
    }
    
    @Test
    public void testComponentTemplate() throws IOException {
        // Prepare component template
        String componentName = "test-component";
        String componentJson = "{\n" +
                "  \"template\": {\n" +
                "    \"settings\": {\n" +
                "      \"index\": {\n" +
                "        \"number_of_shards\": \"1\",\n" +
                "        \"number_of_replicas\": \"0\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        
        // Apply the component template
        boolean result = client.applyComponentTemplate(componentName, componentJson);
        assertTrue("Component template application should succeed", result);

        Awaitility.await().until(() -> {
            try {
                Request request = new Request("GET", "/_component_template/" + componentName);
                Response response = client.getRestClient().performRequest(request);
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                LOG.info("Component template response (status: {}): {}", statusCode, responseBody);
                return statusCode == 200 && responseBody.contains(componentName);
            } catch (Exception e) {
                LOG.error("Error waiting for component template: {}", e.getMessage());
                return false;
            }
        });
    }
    
    @Test
    public void testComposableIndexTemplate() throws IOException {
        // First create component templates
        String settingsComponentName = "test-settings-component";
        String settingsComponentJson = "{\n" +
                "  \"template\": {\n" +
                "    \"settings\": {\n" +
                "      \"index\": {\n" +
                "        \"number_of_shards\": \"1\",\n" +
                "        \"number_of_replicas\": \"0\"\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        client.applyComponentTemplate(settingsComponentName, settingsComponentJson);
        
        String mappingsComponentName = "test-mappings-component";
        String mappingsComponentJson = "{\n" +
                "  \"template\": {\n" +
                "    \"mappings\": {\n" +
                "      \"properties\": {\n" +
                "        \"field1\": { \"type\": \"keyword\" }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        client.applyComponentTemplate(mappingsComponentName, mappingsComponentJson);
        
        // Create index template with unique pattern and high priority
        String indexTemplateName = "test-index-template-1";
        String indexTemplateJson = "{\n" +
                "  \"index_patterns\": [\"test-pattern-1-*\"],\n" +
                "  \"composed_of\": [\"test-settings-component\", \"test-mappings-component\"],\n" +
                "  \"priority\": 600\n" +
                "}";
        
        // Apply the index template
        boolean result = client.applyComposableIndexTemplate(indexTemplateName, indexTemplateJson);
        assertTrue("Composable index template application should succeed", result);

        Awaitility.await().until(() -> {
            try {
                Request request = new Request("GET", "/_index_template/" + indexTemplateName);
                Response response = client.getRestClient().performRequest(request);
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity());
                LOG.info("Index template response (status: {}): {}", statusCode, responseBody);
                return statusCode == 200 && 
                       responseBody.contains(indexTemplateName) &&
                       responseBody.contains(settingsComponentName) &&
                       responseBody.contains(mappingsComponentName);
            } catch (Exception e) {
                LOG.error("Error waiting for index template: {}", e.getMessage());
                return false;
            }
        });
    }
    
    @Test
    public void testApplyAllTemplatesFromDirectory() throws IOException {
        // Create test templates in the temp directory structure
        
        // 1. Create ILM policy
        String policyName = "test-lifecycle";
        String policyJson = "{\n" +
                "  \"policy\": {\n" +
                "    \"phases\": {\n" +
                "      \"hot\": { \"min_age\": \"0ms\", \"actions\": {} },\n" +
                "      \"delete\": { \"min_age\": \"1d\", \"actions\": { \"delete\": {} } }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        Files.write(tempDirectory.resolve("policies").resolve(policyName + ".json"), policyJson.getBytes());
        
        // 2. Create component templates
        String settingsComponent = "settings-component";
        String settingsJson = "{\n" +
                "  \"template\": {\n" +
                "    \"settings\": {\n" +
                "      \"index\": {\n" +
                "        \"number_of_shards\": \"1\",\n" +
                "        \"number_of_replicas\": \"0\",\n" +
                "        \"lifecycle\": { \"name\": \"test-lifecycle\" }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        Files.write(tempDirectory.resolve("components").resolve(settingsComponent + ".json"), settingsJson.getBytes());
        
        String mappingsComponent = "mappings-component";
        String mappingsJson = "{\n" +
                "  \"template\": {\n" +
                "    \"mappings\": {\n" +
                "      \"properties\": {\n" +
                "        \"field1\": { \"type\": \"keyword\" },\n" +
                "        \"field2\": { \"type\": \"integer\" }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        Files.write(tempDirectory.resolve("components").resolve(mappingsComponent + ".json"), mappingsJson.getBytes());
        
        // 3. Create index template
        String indexTemplate = "index-template";
        String indexTemplateJson = "{\n" +
                "  \"index_patterns\": [\"test-pattern-2-*\"],\n" +
                "  \"composed_of\": [\"settings-component\", \"mappings-component\"],\n" +
                "  \"priority\": 700\n" +
                "}";
        Files.write(tempDirectory.resolve("index-templates").resolve(indexTemplate + ".json"), indexTemplateJson.getBytes());

        
        // Check policy files
        Path policyFilePath = tempDirectory.resolve("policies").resolve(policyName + ".json");
        assertTrue("Policy file should exist: " + policyFilePath, Files.exists(policyFilePath));
        // Check component files
        Path settingsFilePath = tempDirectory.resolve("components").resolve(settingsComponent + ".json");
        assertTrue("Settings component file should exist: " + settingsFilePath, Files.exists(settingsFilePath));
        Path mappingsFilePath = tempDirectory.resolve("components").resolve(mappingsComponent + ".json");
        assertTrue("Mappings component file should exist: " + mappingsFilePath, Files.exists(mappingsFilePath));
        // Check index template file
        Path indexTemplateFilePath = tempDirectory.resolve("index-templates").resolve(indexTemplate + ".json");
        assertTrue("Index template file should exist: " + indexTemplateFilePath, Files.exists(indexTemplateFilePath));

        
        // Apply all templates
        int count = client.applyAllTemplatesFromDirectory(tempDirectory.toString());
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
                       body.contains("settings-component") && 
                       body.contains("mappings-component");
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
    
    /**
     * Helper method to recursively delete a directory.
     */
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}