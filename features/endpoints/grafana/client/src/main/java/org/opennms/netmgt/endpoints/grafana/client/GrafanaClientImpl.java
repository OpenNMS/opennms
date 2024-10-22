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
package org.opennms.netmgt.endpoints.grafana.client;

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.opennms.netmgt.endpoints.grafana.api.Dashboard;
import org.opennms.netmgt.endpoints.grafana.api.DashboardWithMeta;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClient;
import org.opennms.netmgt.endpoints.grafana.api.Panel;
import com.google.gson.Gson;

;

public class GrafanaClientImpl implements GrafanaClient {
    private final GrafanaServerConfiguration config;
    private final GrafanaClient client; // Class-level variable

    public GrafanaClientImpl(GrafanaServerConfiguration grafanaServerConfiguration) {
        this.config = Objects.requireNonNull(grafanaServerConfiguration);
        GrafanaClient tempClient = null;
        try {
            tempClient = this;
            List<Dashboard> list = getDashboards();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.client = tempClient;
    }

    private HttpURLConnection createConnection(String endpoint, String method) throws Exception {
        URL url = new URL(config.getUrl() + endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setConnectTimeout((int) config.getConnectTimeoutSeconds() * 1000);
        connection.setReadTimeout((int) config.getReadTimeoutSeconds() * 1000);
        connection.setRequestProperty("Content-Type", "application/json");
        return connection;
    }

    public String sendRequest(String endpoint, String requestBody) throws Exception {
        HttpURLConnection connection = createConnection(endpoint, "POST");

        connection.setDoOutput(true);
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString();
            }
        } else {
            throw new RuntimeException("Request failed with code: " + connection.getResponseCode());
        }
    }

    @Override
    public List<Dashboard> getDashboards() throws IOException {
        String url = String.format("%s/api/search?query=&type=dash-db", config.getUrl());
        HttpURLConnection connection = null;

        try {
            // Create the connection
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.setConnectTimeout((int) config.getConnectTimeoutSeconds() * 1000);
            connection.setReadTimeout((int) config.getReadTimeoutSeconds() * 1000);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    return Arrays.asList(new Gson().fromJson(response.toString(), Dashboard[].class));
                }
            } else {
                throw new IOException("Request failed: " + responseCode);
            }
        } catch (IOException e) {
            throw new IOException("Request failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    @Override
    public Dashboard getDashboardByUid(String uid) throws IOException {
        String url = String.format("%s/api/dashboards/uid/%s", config.getUrl(), uid);
        HttpURLConnection connection = null;

        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.setConnectTimeout((int) config.getConnectTimeoutSeconds() * 1000);
            connection.setReadTimeout((int) config.getReadTimeoutSeconds() * 1000);

            int responseCode = connection.getResponseCode();


            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }

                    DashboardWithMeta dashboardWithMeta = new Gson().fromJson(response.toString(), DashboardWithMeta.class);

                    if (dashboardWithMeta != null) {
                        dashboardWithMeta.getDashboard().setMeta(dashboardWithMeta.getMeta());
                        return dashboardWithMeta.getDashboard();
                    } else {
                        throw new IOException("Dashboard not found");
                    }
                }
            } else {
                throw new IOException("Request failed: " + responseCode);
            }
        } catch (IOException e) {
            throw new IOException("Request failed: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    @Override
    public CompletableFuture<byte[]> renderPngForPanel(Dashboard dashboard, Panel panel, int width, int height, long from, long to, String timezone, Map<String, String> variables) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        // Build the URL with query parameters
        StringBuilder urlBuilder = new StringBuilder(String.format("%s/render/d-solo/%s/%s?", config.getUrl(), dashboard.getUid(), dashboard.getMeta().getSlug()));
        urlBuilder.append("panelId=").append(panel.getId())
                .append("&from=").append(from)
                .append("&to=").append(to)
                .append("&width=").append(width)
                .append("&height=").append(height)
                .append("&timeout=").append(config.getReadTimeoutSeconds())
                .append("&theme=light");

        if (timezone != null && !timezone.isEmpty()) {
            urlBuilder.append("&tz=").append(timezone);
        }

        variables.forEach((k, v) -> urlBuilder.append("&var-").append(k).append("=").append(v));

        String url = urlBuilder.toString();
        HttpURLConnection connection = null;

        try {

            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + config.getApiKey());
            connection.setConnectTimeout((int) config.getConnectTimeoutSeconds() * 1000);
            connection.setReadTimeout((int) config.getReadTimeoutSeconds() * 1000);

            HttpURLConnection finalConnection = connection;
            new Thread(() -> {
                try {
                    int responseCode = finalConnection.getResponseCode();

                    if (responseCode != HttpURLConnection.HTTP_OK) {
                        future.completeExceptionally(new IOException("Request failed: " + responseCode));
                        return;
                    }

                    try (InputStream inputStream = finalConnection.getInputStream()) {
                        byte[] imageBytes = inputStream.readAllBytes();
                        future.complete(imageBytes);
                    }
                } catch (IOException e) {
                    future.completeExceptionally(e);
                } finally {
                    finalConnection.disconnect();
                }
            }).start();
        } catch (IOException e) {
            future.completeExceptionally(e);
        }

        return future;
    }
    private static byte[] inputStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }
    public GrafanaClient getClient() {
        return client;
    }
}
