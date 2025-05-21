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
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
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

    private static final String ELASTICSEARCH_IMAGE = "docker.elastic.co/elasticsearch/elasticsearch:7.17.9";

    private static final Logger LOG = LoggerFactory.getLogger(ElasticRestClientIT.class);

    @ClassRule
    public static ElasticsearchContainer elasticsearch = new ElasticsearchContainer(ELASTICSEARCH_IMAGE)
            .withEnv("xpack.security.enabled", "false")
            .withEnv("discovery.type", "single-node")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
    
    private DefaultElasticRestClient client;
    private Path tempDirectory;
    
    @Before
    public void setUp() throws Exception {

        String[] hosts = {elasticsearch.getHttpHostAddress()};
        client = new DefaultElasticRestClient(hosts);
        client.connect();

        Awaitility.setDefaultTimeout(15, TimeUnit.SECONDS);
        Awaitility.setDefaultPollInterval(500, TimeUnit.MILLISECONDS);
    }
    
    @After
    public void tearDown() throws Exception {
        
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

}