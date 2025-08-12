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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.Gson;
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

import org.opennms.features.elastic.client.model.BulkRequest;
import org.opennms.features.elastic.client.model.BulkResponse;
import org.opennms.features.elastic.client.model.BulkOperation;
import org.opennms.features.elastic.client.model.SearchRequest;
import org.opennms.features.elastic.client.model.SearchResponse;


public class DefaultElasticRestClient implements ElasticRestClient {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultElasticRestClient.class);
    private static final long[] RETRY_SLEEP_MULTIPLIERS = new long[]{1, 2, 10, 20, 60, 120};

    private int bulkRetryCount;
    private int connTimeout;
    private int readTimeout;
    private int retryCooldown;

    private final AtomicInteger threadCountForBulk = new AtomicInteger(1);
    private final ThreadFactory bulkExecuteThreadFactory = runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("elastic-client-async-bulk-execute-" + threadCountForBulk.getAndIncrement());
        return thread;
    };
    private final ExecutorService executor = Executors.newCachedThreadPool(bulkExecuteThreadFactory);

    private final AtomicInteger threadCountForSearch = new AtomicInteger(1);
    private final ThreadFactory bulkSearchThreadFactory = runnable -> {
        Thread thread = new Thread(runnable);
        thread.setName("elastic-client-search-async-" + threadCountForSearch.getAndIncrement());
        return thread;
    };

    private final ExecutorService searchExecutor = Executors.newCachedThreadPool(bulkSearchThreadFactory);

    private final String[] hosts;
    private final String username;
    private final String password;
    private RestClient restClient;
    private final Gson gson = new Gson();

    public void setBulkRetryCount(int bulkRetryCount) {
        this.bulkRetryCount = bulkRetryCount;
    }

    public int getBulkRetryCount() {
        return bulkRetryCount;
    }

    public void setConnTimeout(int connTimeout) {
        this.connTimeout = connTimeout;
    }

    public int getConnTimeout() {
        return connTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setRetryCooldown(int retryCooldown) {
        this.retryCooldown = retryCooldown;
    }

    public int getRetryCooldown() {
        return retryCooldown;
    }

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
                    .setRequestConfigCallback(requestConfigBuilder ->
                            requestConfigBuilder
                                    .setConnectTimeout(connTimeout)
                                    .setSocketTimeout(readTimeout))
                    .setHttpClientConfigCallback(httpClientBuilder ->
                            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                    .build();
        } else {
            restClient = RestClient.builder(httpHosts)
                    .setRequestConfigCallback(requestConfigBuilder ->
                            requestConfigBuilder
                                    .setConnectTimeout(connTimeout)
                                    .setSocketTimeout(readTimeout))
                    .build();
        }
    }

    @Override
    public String health() throws IOException {
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
        executor.shutdown();
        searchExecutor.shutdown();
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

    @Override
    public BulkResponse executeBulk(BulkRequest bulkRequest) throws IOException {
        if (bulkRequest.isEmpty()) {
            return new BulkResponse(false, new ArrayList<>(), 0);
        }

        LOG.debug("Executing bulk request with {} operations", bulkRequest.size());

        // Build the bulk request body
        String bulkBody = buildBulkRequestBody(bulkRequest);

        // Execute with retry logic
        BulkResponse response = null;
        IOException lastException = null;

        for (int retry = 0; retry <= bulkRetryCount; retry++) {
            try {
                response = executeBulkRequest(bulkBody, bulkRequest.getRefresh());

                // If successful or no retries allowed for errors, return
                if (!response.hasErrors() || bulkRetryCount == 0) {
                    return response;
                }

                // Check if all items failed with retriable errors
                if (hasRetriableErrors(response) && retry < bulkRetryCount) {
                    LOG.warn("Bulk request had errors, retrying... (attempt {} of {})", retry + 1, bulkRetryCount + 1);
                    waitBeforeRetrying(retry);
                    continue;
                }

                // Some errors are not retriable, return the response
                return response;

            } catch (IOException e) {
                lastException = e;
                if (retry < bulkRetryCount) {
                    LOG.warn("Bulk request failed, retrying... (attempt {} of {})", retry + 1, bulkRetryCount + 1, e);
                    waitBeforeRetrying(retry);
                } else {
                    throw e;
                }
            }
        }

        // Should not reach here, but handle it just in case
        if (lastException != null) {
            throw lastException;
        }
        return response;
    }

    private String buildBulkRequestBody(BulkRequest bulkRequest) {
        StringBuilder bulkBody = new StringBuilder();
        for (BulkOperation operation : bulkRequest.getOperations()) {
            // Add action line
            JsonObject actionObject = new JsonObject();
            JsonObject actionParams = new JsonObject();
            actionParams.addProperty("_index", operation.getIndex());
            if (operation.getId() != null) {
                actionParams.addProperty("_id", operation.getId());
            }

            String actionName = operation.getType().name().toLowerCase();
            actionObject.add(actionName, actionParams);
            bulkBody.append(gson.toJson(actionObject)).append("\n");

            // Add source line (except for delete operations)
            if (operation.getType() != BulkOperation.Type.DELETE && operation.getSource() != null) {
                if (operation.getType() == BulkOperation.Type.UPDATE) {
                    // For updates, wrap the source in a "doc" object
                    JsonObject updateDoc = new JsonObject();
                    if (operation.getSource() instanceof String) {
                        updateDoc.add("doc", JsonParser.parseString((String) operation.getSource()));
                    } else {
                        updateDoc.add("doc", gson.toJsonTree(operation.getSource()));
                    }
                    updateDoc.addProperty("doc_as_upsert", true);
                    bulkBody.append(gson.toJson(updateDoc)).append("\n");
                } else {
                    // For index operations, add source directly
                    if (operation.getSource() instanceof String) {
                        bulkBody.append(operation.getSource()).append("\n");
                    } else {
                        bulkBody.append(gson.toJson(operation.getSource())).append("\n");
                    }
                }
            }
        }
        return bulkBody.toString();
    }

    private BulkResponse executeBulkRequest(String bulkBody, String refresh) throws IOException {
        Request request = new Request("POST", "/_bulk");
        if (refresh != null) {
            request.addParameter("refresh", refresh);
        }
        request.setJsonEntity(bulkBody);

        long startTime = System.currentTimeMillis();
        Response response = restClient.performRequest(request);
        long tookInMillis = System.currentTimeMillis() - startTime;

        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Bulk request failed with status: " +
                    response.getStatusLine().getStatusCode());
        }
        return parseBulkResponse(response, tookInMillis);
    }

    private boolean hasRetriableErrors(BulkResponse response) {
        if (!response.hasErrors()) {
            return false;
        }

        // Check if any of the errors are retriable (e.g., 429 Too Many Requests, 503 Service Unavailable)
        for (BulkResponse.BulkItemResponse item : response.getItems()) {
            if (item.getError() != null) {
                int status = item.getStatus();
                // Retry on rate limiting or temporary unavailability
                if (status != 429 && status != 503 && status != 504) {
                    // Found a non-retriable error
                    return false;
                }
            }
        }
        return true;
    }

    private void waitBeforeRetrying(int retryCount) {
        try {
            long sleepTime = getSleepTime(retryCount);
            if (sleepTime > 0) {
                LOG.debug("Waiting {} ms before retrying", sleepTime);
                Thread.sleep(sleepTime);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted while waiting for retry");
        }
    }

    private long getSleepTime(int retry) {
        int index = Math.min(retry, RETRY_SLEEP_MULTIPLIERS.length - 1);
        return retryCooldown * RETRY_SLEEP_MULTIPLIERS[index];
    }

    @Override
    public CompletableFuture<BulkResponse> executeBulkAsync(BulkRequest bulkRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeBulk(bulkRequest);
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute bulk request", e);
            }
        }, executor);
    }

    private BulkResponse parseBulkResponse(Response response, long tookInMillis) throws IOException {
        if (response.getEntity() == null) {
            throw new IOException("Empty response from Elasticsearch");
        }
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject responseJson = JsonParser.parseReader(new StringReader(responseBody)).getAsJsonObject();

        boolean hasErrors = responseJson.has("errors") && responseJson.get("errors").getAsBoolean();
        List<BulkResponse.BulkItemResponse> items = new ArrayList<>();

        if (responseJson.has("items")) {
            JsonArray itemsArray = responseJson.getAsJsonArray("items");
            for (JsonElement itemElement : itemsArray) {
                JsonObject item = itemElement.getAsJsonObject();

                // Find the operation type (index, update, delete)
                Map.Entry<String, JsonElement> entry = item.entrySet().iterator().next();
                JsonObject opResult = entry.getValue().getAsJsonObject();
                String index = opResult.has("_index") ? opResult.get("_index").getAsString() : null;
                String id = opResult.has("_id") ? opResult.get("_id").getAsString() : null;
                int status = opResult.has("status") ? opResult.get("status").getAsInt() : 200;
                String error = null;

                if (opResult.has("error")) {
                    JsonElement errorElement = opResult.get("error");
                    if (errorElement.isJsonObject()) {
                        error = errorElement.getAsJsonObject().toString();
                    } else {
                        error = errorElement.getAsString();
                    }
                }
                items.add(new BulkResponse.BulkItemResponse(index, id, status, error));
            }
        }

        return new BulkResponse(hasErrors, items, tookInMillis);
    }

    @Override
    public SearchResponse search(SearchRequest searchRequest) throws IOException {
        String indexPath = String.join(",", searchRequest.getIndices());
        Request request = new Request("POST", "/" + indexPath + "/_search");

        // Add query parameters
        for (Map.Entry<String, String> param : searchRequest.getParameters().entrySet()) {
            request.addParameter(param.getKey(), param.getValue());
        }

        request.setJsonEntity(searchRequest.getQuery());

        long startTime = System.currentTimeMillis();
        Response response = restClient.performRequest(request);
        long tookInMillis = System.currentTimeMillis() - startTime;

        return parseSearchResponse(response, tookInMillis);
    }

    @Override
    public CompletableFuture<SearchResponse> searchAsync(SearchRequest searchRequest) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return search(searchRequest);
            } catch (IOException e) {
                throw new RuntimeException("Failed to execute search request", e);
            }
        }, searchExecutor);
    }

    private SearchResponse parseSearchResponse(Response response, long tookInMillis) throws IOException {
        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject responseJson = JsonParser.parseReader(new StringReader(responseBody)).getAsJsonObject();

        boolean timedOut = responseJson.has("timed_out") && responseJson.get("timed_out").getAsBoolean();
        JsonObject shards = responseJson.has("_shards") ? responseJson.getAsJsonObject("_shards") : new JsonObject();
        JsonObject aggregations = responseJson.has("aggregations") ? responseJson.getAsJsonObject("aggregations") : null;
        String scrollId = responseJson.has("_scroll_id") ? responseJson.get("_scroll_id").getAsString() : null;

        SearchResponse.SearchHits hits = null;
        if (responseJson.has("hits")) {
            hits = parseSearchHits(responseJson.getAsJsonObject("hits"));
        }

        return new SearchResponse(tookInMillis, timedOut, shards, hits, aggregations, scrollId);
    }

    private SearchResponse.SearchHits parseSearchHits(JsonObject hitsObject) {
        long totalHits = 0;
        String totalHitsRelation = "eq";

        if (hitsObject.has("total")) {
            JsonElement totalElement = hitsObject.get("total");
            if (totalElement.isJsonObject()) {
                JsonObject totalObject = totalElement.getAsJsonObject();
                totalHits = totalObject.has("value") ? totalObject.get("value").getAsLong() : 0;
                totalHitsRelation = totalObject.has("relation") ? totalObject.get("relation").getAsString() : "eq";
            } else {
                totalHits = totalElement.getAsLong();
            }
        }

        double maxScore = hitsObject.has("max_score") && !hitsObject.get("max_score").isJsonNull()
                ? hitsObject.get("max_score").getAsDouble() : 0.0;

        List<SearchResponse.SearchHit> hits = new ArrayList<>();
        if (hitsObject.has("hits")) {
            JsonArray hitsArray = hitsObject.getAsJsonArray("hits");
            for (JsonElement hitElement : hitsArray) {
                JsonObject hit = hitElement.getAsJsonObject();
                String index = hit.has("_index") ? hit.get("_index").getAsString() : null;
                String id = hit.has("_id") ? hit.get("_id").getAsString() : null;
                double score = hit.has("_score") && !hit.get("_score").isJsonNull()
                        ? hit.get("_score").getAsDouble() : 0.0;
                JsonObject source = hit.has("_source") ? hit.getAsJsonObject("_source") : new JsonObject();

                hits.add(new SearchResponse.SearchHit(index, id, score, source));
            }
        }

        return new SearchResponse.SearchHits(totalHits, totalHitsRelation, maxScore, hits);
    }


    @Override
    public boolean applyLegacyIndexTemplate(String templateName, String templateBody) throws IOException {
        Request request = new Request("PUT", "/_template/" + templateName);
        request.setJsonEntity(templateBody);

        Response response = restClient.performRequest(request);
        if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201) {
            LOG.info("Successfully applied legacy template: {}", templateName);
            return true;
        } else {
            LOG.error("Failed to apply legacy template: {}, status: {}", templateName, response.getStatusLine().getStatusCode());
            return false;
        }
    }

    @Override
    public String getServerVersion() throws IOException {
        Request request = new Request("GET", "/");
        Response response = restClient.performRequest(request);

        String responseBody = EntityUtils.toString(response.getEntity());
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

        JsonObject version = json.getAsJsonObject("version");
        if (version != null && version.has("number")) {
            return version.get("number").getAsString();
        }

        throw new IOException("Could not retrieve server version from response: " + responseBody);
    }

    @Override
    public boolean deleteIndex(String indices) throws IOException {

        try {
            Request request = new Request("DELETE", "/" + indices);
            Response response = restClient.performRequest(request);

            int statusCode = response.getStatusLine().getStatusCode();
            boolean success = statusCode >= 200 && statusCode < 300;

            if (success) {
                LOG.info("Successfully deleted indices: {}", indices);
            } else {
                LOG.warn("Failed to delete indices: {}, status code: {}", indices, statusCode);
            }

            return success;
        } catch (Exception e) {
            LOG.error("Failed to delete indices '{}'", indices, e);
            throw e;
        }
    }
}