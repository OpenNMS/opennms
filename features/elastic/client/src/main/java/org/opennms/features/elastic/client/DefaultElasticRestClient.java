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
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
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

    public RestClient getRestClient() {
        return restClient;
    }


    public DefaultElasticRestClient(String[] hosts) {
        this(hosts, null, null);
        init();
    }

    public DefaultElasticRestClient(String hostsString, String username, String password) {
        if (hostsString == null || hostsString.trim().isEmpty()) {
            throw new IllegalArgumentException("Hosts string must be specified");
        }

        this.hosts = Arrays.stream(hostsString.split(",")).map(String::trim).filter(h -> !h.isEmpty()).toArray(String[]::new);

        this.username = (username != null && !username.isEmpty()) ? username : null;
        this.password = (password != null && !password.isEmpty()) ? password : null;
        init();
    }


    public DefaultElasticRestClient(String[] hosts, String username, String password) {
        if (hosts == null || hosts.length == 0) {
            throw new IllegalArgumentException("At least one host must be specified");
        }
        this.hosts = Arrays.copyOf(hosts, hosts.length);
        this.username = username;
        this.password = password;
        init();
    }

    public void init() {
        HttpHost[] httpHosts = Arrays.stream(hosts)
                .map(host -> {
                    try {
                        URI uri = host.contains("://") ? new URI(host) : new URI("http://" + host);
                        String scheme = uri.getScheme() != null ? uri.getScheme() : "http";
                        String hostname = uri.getHost();
                        int port = uri.getPort() != -1 ? uri.getPort() : 9200;

                        if (hostname == null) {
                            throw new IllegalArgumentException("Invalid host: " + host);
                        }

                        return new HttpHost(hostname, port, scheme);
                    } catch (Exception e) {
                        throw new RuntimeException("Invalid Elasticsearch host: " + host, e);
                    }
                }).toArray(HttpHost[]::new);

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
    public String health() throws IOException {
        LOG.debug("Checking Elasticsearch cluster health at {}", Arrays.toString(hosts));
        Request healthRequest = new Request("GET", "/_cluster/health");
        Response healthResponse = restClient.performRequest(healthRequest);
        int statusCode = healthResponse.getStatusLine().getStatusCode();

        if (statusCode >= 200 && statusCode < 300) {
            String responseBody = EntityUtils.toString(healthResponse.getEntity());
            JsonObject healthJson = JsonParser.parseReader(new StringReader(responseBody)).getAsJsonObject();
            String status = healthJson.get("status").getAsString();
            LOG.debug("Elasticsearch cluster health: {}", status);
            return status;
        } else {
            throw new IOException("Failed to get cluster health: HTTP " + statusCode);
        }
    }


    @Override
    public Map<String, String> listTemplates() throws IOException {
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
    public void close() throws IOException {
        if (restClient != null) {
            LOG.info("Closing Elasticsearch client");
            restClient.close();
        }
    }

    @Override
    public boolean applyILMPolicy(String policyName, String policyBody) throws IOException {
        LOG.info("Applying ILM policy '{}' to Elasticsearch cluster", policyName);

        try {
            Request request = new Request("PUT", "/_ilm/policy/" + policyName);
            request.setJsonEntity(policyBody);

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
        LOG.info("Applying component template '{}' to Elasticsearch cluster", componentName);
        LOG.debug("Component template body: {}", componentBody);

        try {
            Request request = new Request("PUT", "/_component_template/" + componentName);
            request.setJsonEntity(componentBody);

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
        LOG.info("Applying composable index template '{}' to Elasticsearch cluster", templateName);

        try {
            Request request = new Request("PUT", "/_index_template/" + templateName);
            request.setJsonEntity(templateBody);

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
        int appliedCount = 0;

        File directory = new File(templateDirectory);
        if (!directory.exists() || !directory.isDirectory()) {
            LOG.error("Template directory does not exist or is not a directory: {}", templateDirectory);
            return 0;
        }

        // Recursively find all .json files
        List<File> jsonFiles;
        try (Stream<Path> paths = Files.walk(directory.toPath())) {
            jsonFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .map(Path::toFile)
                    .toList();
        } catch (IOException e) {
            LOG.error("Error walking through template directory: {}", e.getMessage());
            return 0;
        }

        if (jsonFiles.isEmpty()) {
            LOG.info("No template files found in {}", directory.getAbsolutePath());
            return 0;
        }

        LOG.info("Found {} template files", jsonFiles.size());


        // Sort files into categories based on filename patterns
        java.util.List<File> ilmPolicyFiles = new java.util.ArrayList<>();
        java.util.List<File> componentTemplateFiles = new java.util.ArrayList<>();
        java.util.List<File> indexTemplateFiles = new java.util.ArrayList<>();
        
        for (File file : jsonFiles) {
            String fileName = file.getName().toLowerCase();
            if (fileName.contains("ilm") || fileName.contains("policy") || fileName.contains("lifecycle")) {
                ilmPolicyFiles.add(file);
            } else if (fileName.contains("component") || fileName.contains("setting") ||
                       fileName.contains("mapping") || fileName.contains("alias")) {
                componentTemplateFiles.add(file);
            } else if (fileName.contains("composable") || fileName.contains("index")) {
                indexTemplateFiles.add(file);
            } else {
                LOG.warn("Unknown template type for file: {}", fileName);
            }
        }
        
        // Apply templates in the correct order: ILM policies first
        LOG.info("Processing {} ILM policy files", ilmPolicyFiles.size());
        for (File policyFile : ilmPolicyFiles) {
            try {
                String policyName = policyFile.getName().replaceAll("\\.json$", "");
                String policyContent = Files.readString(policyFile.toPath(), StandardCharsets.UTF_8);
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
        
        // Component templates second
        LOG.info("Processing {} component template files", componentTemplateFiles.size());
        for (File componentFile : componentTemplateFiles) {
            try {
                String componentName = componentFile.getName().replaceAll("\\.json$", "");
                String componentContent = Files.readString(componentFile.toPath(), StandardCharsets.UTF_8);
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
        
        // Index templates last
        LOG.info("Processing {} index template files", indexTemplateFiles.size());
        for (File templateFile : indexTemplateFiles) {
            try {
                String templateName = templateFile.getName().replaceAll("\\.json$", "");
                String templateContent = Files.readString(templateFile.toPath(), StandardCharsets.UTF_8);
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

        LOG.info("Successfully applied {} templates/policies from {}", appliedCount, templateDirectory);
        return appliedCount;
    }
}