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
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.GsonBuilder;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.opennms.netmgt.endpoints.grafana.api.Dashboard;
import org.opennms.netmgt.endpoints.grafana.api.DashboardWithMeta;
import org.opennms.netmgt.endpoints.grafana.api.GrafanaClient;
import org.opennms.netmgt.endpoints.grafana.api.Panel;

import com.google.common.base.Strings;
import com.google.gson.Gson;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class GrafanaClientImpl implements GrafanaClient {
    private final GrafanaServerConfiguration config;

    private final Gson gson;
    private final OkHttpClient client;
    private final HttpUrl baseUrl;

    public GrafanaClientImpl(GrafanaServerConfiguration grafanaServerConfiguration) {
        this.config = Objects.requireNonNull(grafanaServerConfiguration);
        baseUrl = HttpUrl.parse(grafanaServerConfiguration.getUrl());

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(config.getConnectTimeoutSeconds(), TimeUnit.SECONDS)
                .readTimeout(config.getReadTimeoutSeconds(), TimeUnit.SECONDS);
        builder = configureToIgnoreCertificate(builder);
        client = builder.build();

        // The schema changed between Grafana 7 and 8 for the `datasource` property on panels.
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Panel.class, new PanelDeserializer())
                .create();
    }

    @Override
    public List<Dashboard> getDashboards() throws IOException {
        final HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("api")
                .addPathSegment("search")
                .query("query=&type=dash-db")
                .build();
        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + extractMessageFromErrorResponse(response));
            }
            final String json = response.body().string();
            final Dashboard[] dashboards = gson.fromJson(json, Dashboard[].class);
            return Arrays.asList(dashboards);
        }
    }

    @Override
    public Dashboard getDashboardByUid(String uid) throws IOException {
        final HttpUrl url = baseUrl.newBuilder()
                .addPathSegment("api")
                .addPathSegment("dashboards")
                .addPathSegment("uid")
                .addPathSegment(uid)
                .build();

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Request failed: " + extractMessageFromErrorResponse(response));
            }
            final String json = response.body().string();
            final DashboardWithMeta dashboardWithMeta = gson.fromJson(json, DashboardWithMeta.class);
            // Copy the meta-data over to the dashboard for ease of use
            dashboardWithMeta.getDashboard().setMeta(dashboardWithMeta.getMeta());
            return dashboardWithMeta.getDashboard();
        }
    }

    @Override
    public CompletableFuture<byte[]> renderPngForPanel(Dashboard dashboard, Panel panel, int width, int height, long from, long to, String timezone, Map<String, String> variables) {
        final HttpUrl.Builder builder = baseUrl.newBuilder()
                .addPathSegment("render")
                .addPathSegment("d-solo")
                .addPathSegment(dashboard.getUid())
                .addPathSegments(dashboard.getMeta().getSlug());

        // Query parameters
        builder.addQueryParameter("panelId", Integer.toString(panel.getId()))
                .addQueryParameter("from", Long.toString(from))
                .addQueryParameter("to", Long.toString(to))
                .addQueryParameter("width", Integer.toString(width))
                .addQueryParameter("height", Integer.toString(height))
                // Set a render timeout equal to the client's read timeout
                .addQueryParameter("timeout", Integer.toString(config.getReadTimeoutSeconds()))
                .addQueryParameter("theme", "light"); // Use the light theme
        if (!Strings.isNullOrEmpty(timezone)) {
            builder.addQueryParameter("tz", timezone);
        }
        variables.forEach((k,v) -> builder.addQueryParameter("var-"+ k, v));

        final Request request = new Request.Builder()
                .url(builder.build())
                .addHeader("Authorization", "Bearer " + config.getApiKey())
                .build();

        final CompletableFuture<byte[]> future = new CompletableFuture<>();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
            }

            @Override
            public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        try {
                            future.completeExceptionally(new IOException("Request failed: " + extractMessageFromErrorResponse(response)));
                        } catch (IOException e) {
                            future.completeExceptionally(new IOException("Could not extract message from error response", e));
                        }
                    }

                    try (InputStream is = responseBody.byteStream()) {
                        future.complete(inputStreamToByteArray(is));
                    } catch (IOException e) {
                        future.completeExceptionally(e);
                    }
                }
            }
        });
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

    private static OkHttpClient.Builder configureToIgnoreCertificate(OkHttpClient.Builder builder) {
        try {

            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0]);
            builder.hostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return builder;
    }

    private static String extractMessageFromErrorResponse(Response response) throws IOException {
        final String contentType = response.header("Content-Type");
        if (contentType.toLowerCase().contains("application/json")) {
            final JSONTokener tokener = new JSONTokener(response.body().string());
            final JSONObject json = new JSONObject(tokener);
            if (json.has("message")) {
                return json.getString("message");
            } else {
                return json.toString();
            }
        }
        return response.body().string();
    }
}
