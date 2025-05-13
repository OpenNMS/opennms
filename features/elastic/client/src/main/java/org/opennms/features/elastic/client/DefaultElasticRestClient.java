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
import java.io.File;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class DefaultElasticRestClient implements ElasticRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultElasticRestClient.class);

    private final String[] hosts;
    private final String username;
    private final String password;
    private RestClient restClient;
    private final AtomicBoolean connected = new AtomicBoolean(false);

    public RestClient getRestClient() {
        return restClient;
    }


    public DefaultElasticRestClient(String[] hosts) {
        this(hosts, null, null);
    }

    public DefaultElasticRestClient(String hostsString, String username, String password) {
        if (hostsString == null || hostsString.trim().isEmpty()) {
            throw new IllegalArgumentException("Hosts string must be specified");
        }

        this.hosts = Arrays.stream(hostsString.split(",")).map(String::trim).filter(h -> !h.isEmpty()).toArray(String[]::new);

        this.username = (username != null && !username.isEmpty()) ? username : null;
        this.password = (password != null && !password.isEmpty()) ? password : null;
    }


    public DefaultElasticRestClient(String[] hosts, String username, String password) {
        if (hosts == null || hosts.length == 0) {
            throw new IllegalArgumentException("At least one host must be specified");
        }
        this.hosts = Arrays.copyOf(hosts, hosts.length);
        this.username = username;
        this.password = password;
    }

    public void init() {
        HttpHost[] httpHosts = Arrays.stream(hosts)
                .map(host -> {
                    String[] parts = host.split(":");
                    String hostname = parts[0];
                    int port = parts.length > 1 ? Integer.parseInt(parts[1]) : 9200;
                    return new HttpHost(hostname, port, "http");
                })
                .toArray(HttpHost[]::new);

        // Create the low-level client
        if (username != null && password != null) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    AuthScope.ANY, new UsernamePasswordCredentials(username, password));

            restClient = RestClient.builder(httpHosts)
                    .setHttpClientConfigCallback(httpClientBuilder ->
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .build();
        } else {
            restClient = RestClient.builder(httpHosts).build();
        }
    }

    @Override
    public void connect() throws IOException {
        if (connected.get()) {
            LOG.info("Already connected to Elasticsearch cluster");
            return;
        }

        LOG.info("Connecting to Elasticsearch cluster at {}", Arrays.toString(hosts));
        Request healthRequest = new Request("GET", "/_cluster/health");
        Response healthResponse = restClient.performRequest(healthRequest);
        int statusCode = healthResponse.getStatusLine().getStatusCode();

        if (statusCode >= 200 && statusCode < 300) {
            String responseBody = EntityUtils.toString(healthResponse.getEntity());
            JsonObject healthJson = JsonParser.parseReader(new StringReader(responseBody)).getAsJsonObject();
            String clusterName = healthJson.get("cluster_name").getAsString();
            String status = healthJson.get("status").getAsString();

            LOG.info("Connected to Elasticsearch cluster '{}' with status: {}", clusterName, status);
            connected.set(true);
        } else {
            throw new IOException("Failed to connect to Elasticsearch: HTTP " + statusCode);
        }
    }


    @Override
    public Map<String, String> listTemplates() throws IOException {
        if (!connected.get()) {
            throw new IllegalStateException("Not connected to Elasticsearch cluster");
        }

        Map<String, String> templates = new HashMap<>();

        try {
            Request request = new Request("GET", "/_index_template");
            Response response = restClient.performRequest(request);

            if (response.getStatusLine().getStatusCode() == 200) {
                String responseBody = EntityUtils.toString(response.getEntity());
                JsonObject templatesJson = JsonParser.parseReader(new StringReader(responseBody)).getAsJsonObject();

                if (templatesJson.has("index_templates")) {
                    JsonArray templateArray = templatesJson.getAsJsonArray("index_templates");
                    for (JsonElement element : templateArray) {
                        JsonObject template = element.getAsJsonObject();
                        String name = template.get("name").getAsString();
                        templates.put(name, template.toString());
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to list templates: {}", e.getMessage(), e);
        }

        return templates;
    }


    @Override
    public boolean isConnected() {
        return connected.get();
    }


    @Override
    public void close() throws IOException {
        if (restClient != null) {
            LOG.info("Closing Elasticsearch client");
            restClient.close();
            connected.set(false);
        }
    }

    @Override
    public boolean applyILMPolicy(String policyName, String policyBody) throws IOException {
        if (!connected.get()) {
            throw new IllegalStateException("Not connected to Elasticsearch cluster");
        }

        LOG.info("Applying ILM policy '{}' to Elasticsearch cluster", policyName);

        try {
            Request request = new Request("PUT", "/_ilm/policy/" + policyName);
            request.setEntity(new StringEntity(policyBody, ContentType.APPLICATION_JSON));

            Response response = restClient.performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            boolean acknowledged = statusCode >= 200 && statusCode < 300;

            if (acknowledged) {
                LOG.info("ILM policy '{}' successfully applied", policyName);
            } else {
                LOG.warn("ILM policy '{}' application not acknowledged, status code: {}", policyName, statusCode);
            }

            return acknowledged;
        } catch (Exception e) {
            LOG.error("Failed to apply ILM policy '{}': {}", policyName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean applyComponentTemplate(String componentName, String componentBody) throws IOException {
        if (!connected.get()) {
            throw new IllegalStateException("Not connected to Elasticsearch cluster");
        }

        LOG.info("Applying component template '{}' to Elasticsearch cluster", componentName);
        LOG.debug("Component template body: {}", componentBody);

        try {
            Request request = new Request("PUT", "/_component_template/" + componentName);
            request.setEntity(new StringEntity(componentBody, ContentType.APPLICATION_JSON));

            Response response = restClient.performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String responseBody = EntityUtils.toString(response.getEntity());
            LOG.info("Component template response (status: {}): {}", statusCode, responseBody);

            boolean acknowledged = statusCode >= 200 && statusCode < 300;

            if (acknowledged) {
                LOG.info("Component template {}' successfully applied", componentName);
            } else {
                LOG.warn("Component template '{}' application not acknowledged, status code: {}", componentName, statusCode);
            }
            return acknowledged;
        } catch (Exception e) {
            LOG.error("Failed to apply component template '{}': {}", componentName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean applyComposableIndexTemplate(String templateName, String templateBody) throws IOException {
        if (!connected.get()) {
            throw new IllegalStateException("Not connected to Elasticsearch cluster");
        }

        LOG.info("Applying composable index template '{}' to Elasticsearch cluster", templateName);

        try {
            Request request = new Request("PUT", "/_index_template/" + templateName);
            request.setEntity(new StringEntity(templateBody, ContentType.APPLICATION_JSON));

            Response response = restClient.performRequest(request);
            int statusCode = response.getStatusLine().getStatusCode();
            boolean acknowledged = statusCode >= 200 && statusCode < 300;

            if (acknowledged) {
                LOG.info("Composable index template '{}' successfully applied", templateName);
            } else {
                LOG.warn("Composable index template '{}' application not acknowledged, status code: {}", templateName, statusCode);
            }

            return acknowledged;
        } catch (Exception e) {
            LOG.error("Failed to apply composable index template '{}': {}", templateName, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public int applyAllTemplatesFromDirectory(String templateDirectory) throws IOException {
        if (!connected.get()) {
            throw new IllegalStateException("Not connected to Elasticsearch cluster");
        }

        LOG.info("Applying all templates from directory: {}", templateDirectory);
        int appliedCount = 0;

        // ILM policies first
        File policiesDir = new File(templateDirectory, "policies");
        if (policiesDir.exists() && policiesDir.isDirectory()) {
            LOG.info("Processing ILM policies from: {}", policiesDir.getAbsolutePath());
            File[] policyFiles = policiesDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (policyFiles != null && policyFiles.length > 0) {
                LOG.info("Found {} ILM policy files", policyFiles.length);
                for (File policyFile : policyFiles) {
                    try {
                        String policyName = policyFile.getName().replaceAll("\\.json$", "");
                        String policyContent = Files.readString(policyFile.toPath(), StandardCharsets.UTF_8);
                        LOG.info("Applying ILM policy: {}", policyName);
                        if (applyILMPolicy(policyName, policyContent)) {
                            LOG.info("Successfully applied ILM policy: {}", policyName);
                            appliedCount++;
                        } else {
                            LOG.warn("Failed to apply ILM policy: {}", policyName);
                        }
                    } catch (Exception e) {
                        LOG.error("Error applying ILM policy from file {}: {}", policyFile.getName(), e.getMessage(), e);
                    }
                }
            } else {
                LOG.info("No ILM policy files found in {}", policiesDir.getAbsolutePath());
            }
        } else {
            LOG.info("Policies directory does not exist: {}", policiesDir.getAbsolutePath());
        }

        // Component templates
        File componentsDir = new File(templateDirectory, "components");
        if (componentsDir.exists() && componentsDir.isDirectory()) {
            LOG.info("Processing component templates from: {}", componentsDir.getAbsolutePath());
            File[] componentFiles = componentsDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (componentFiles != null && componentFiles.length > 0) {
                LOG.info("Found {} component template files", componentFiles.length);
                for (File componentFile : componentFiles) {
                    try {
                        String componentName = componentFile.getName().replaceAll("\\.json$", "");
                        String componentContent = Files.readString(componentFile.toPath(), StandardCharsets.UTF_8);
                        LOG.info("Applying component template: {}", componentName);
                        if (applyComponentTemplate(componentName, componentContent)) {
                            LOG.info("Successfully applied component template: {}", componentName);
                            appliedCount++;
                        } else {
                            LOG.warn("Failed to apply component template: {}", componentName);
                        }
                    } catch (Exception e) {
                        LOG.error("Error applying component template from file {}: {}", componentFile.getName(), e.getMessage(), e);
                    }
                }
            } else {
                LOG.info("No component template files found in {}", componentsDir.getAbsolutePath());
            }
        } else {
            LOG.info("Components directory does not exist: {}", componentsDir.getAbsolutePath());
        }

        // Index templates
        File indexTemplatesDir = new File(templateDirectory, "index-templates");
        if (indexTemplatesDir.exists() && indexTemplatesDir.isDirectory()) {
            LOG.info("Processing index templates from: {}", indexTemplatesDir.getAbsolutePath());
            File[] templateFiles = indexTemplatesDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (templateFiles != null && templateFiles.length > 0) {
                LOG.info("Found {} index template files", templateFiles.length);
                for (File templateFile : templateFiles) {
                    try {
                        String templateName = templateFile.getName().replaceAll("\\.json$", "");
                        String templateContent = Files.readString(templateFile.toPath(), StandardCharsets.UTF_8);
                        ;
                        LOG.info("Applying index template: {}", templateName);
                        if (applyComposableIndexTemplate(templateName, templateContent)) {
                            LOG.info("Successfully applied index template: {}", templateName);
                            appliedCount++;
                        } else {
                            LOG.warn("Failed to apply index template: {}", templateName);
                        }
                    } catch (Exception e) {
                        LOG.error("Error applying index template from file {}: {}", templateFile.getName(), e.getMessage(), e);
                    }
                }
            } else {
                LOG.info("No index template files found in {}", indexTemplatesDir.getAbsolutePath());
            }
        } else {
            LOG.info("Index templates directory does not exist: {}", indexTemplatesDir.getAbsolutePath());
        }

        LOG.info("Successfully applied {} templates/policies from {}", appliedCount, templateDirectory);
        return appliedCount;
    }
}