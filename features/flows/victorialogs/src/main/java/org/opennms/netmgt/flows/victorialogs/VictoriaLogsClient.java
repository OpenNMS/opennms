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

package org.opennms.netmgt.flows.victorialogs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPOutputStream;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimal HTTP client for VictoriaLogs.
 *
 * <p><strong>Why {@code /insert/jsonline} and not the Elasticsearch bulk endpoint.</strong>
 * VictoriaLogs does expose an Elasticsearch-compatible {@code /insert/elasticsearch/_bulk} endpoint,
 * and it is tempting because an existing Elasticsearch writer can be repointed at it with no code
 * change. It is not used here, for three reasons. It requires an action line before every document,
 * so a batch of 1000 flows costs 2000 parsed lines instead of 1000, half of them carrying no data.
 * It stops parsing at the first malformed document and answers {@code 200 OK} regardless, with no
 * per-document detail — silent loss, which is the worst failure mode available to a monitoring
 * system. And the one advantage it has, reusing the existing Elasticsearch client, is worth nothing
 * here because that client is exactly what this module exists to avoid depending on.
 * {@code /insert/jsonline} by contrast skips invalid lines, continues with the rest, and increments
 * {@code vl_http_errors_total} per bad line.
 *
 * <p>VictoriaLogs' fastest ingestion path is the binary {@code /insert/native} protocol used by
 * vlagent, but no public wire specification for it exists. Implementing it would mean tracking an
 * unversioned format by inspection — trading one kind of version coupling for another — so it is
 * deliberately not attempted.
 *
 * <p><strong>Authentication.</strong> VictoriaLogs has none of its own; the documented model is to
 * put vmauth (or an equivalent reverse proxy) in front of it and keep VictoriaLogs itself on a
 * trusted network. The Basic credentials here are therefore aimed at that proxy, not at
 * VictoriaLogs.
 */
public class VictoriaLogsClient implements AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(VictoriaLogsClient.class);

    private static final String INSERT_PATH = "/insert/jsonline";
    private static final String HEALTH_PATH = "/health";
    private static final String METRICS_PATH = "/metrics";
    private static final String QUERY_PATH = "/select/logsql/query";

    private final VictoriaLogsClientConfig config;
    private final HttpClient httpClient;
    private final URI insertUri;
    private final String configurationError;

    public VictoriaLogsClient(final VictoriaLogsClientConfig config) {
        this.config = Objects.requireNonNull(config);
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(config.getConnectTimeout())
                .build();
        // Built inside the recorded-fault path, not before it. URI.create throws for a url that is
        // syntactically illegal rather than merely relative -- a stray space, an unbracketed IPv6
        // literal -- and a throwing constructor fails the whole blueprint container, which is the
        // failure this class was changed to stop having. Validating after building only ever saw
        // urls that already parsed.
        URI uri = null;
        String fault = null;
        try {
            uri = buildInsertUri(config);
        } catch (final IllegalArgumentException malformed) {
            fault = "the url is not a valid URI: " + redact(config.getUrl())
                    + " (" + malformed.getMessage() + ")";
        }
        this.insertUri = uri;
        this.configurationError = fault != null ? fault : validate(uri, config);
        if (configurationError == null) {
            LOG.info("VictoriaLogs client configured: {}", config);
        } else {
            LOG.error("VictoriaLogs is misconfigured and will not be used: {}", configurationError);
        }
    }

    /**
     * Records why this configuration cannot work, rather than throwing.
     *
     * <p>Throwing here would be fail-fast in the wrong place. This object is constructed
     * unconditionally by blueprint, and a constructor that throws fails the whole container — which
     * takes down the {@code FlowRepository}, the metric set, and, self-defeatingly, the health check
     * whose entire job is to tell the operator that this backend is unwell. The result was that a
     * blanked url produced no VictoriaLogs entry in {@code opennms:health-check} at all, and the
     * diagnosis lived only in a startup log line.
     *
     * <p>So the fault is remembered instead: logged loudly at ERROR on the way past, reported by
     * {@link #isHealthy()} and named by {@link #getConfigurationError()}, and turned into a
     * {@link VictoriaLogsException} by anything that actually tries to use the connection. Nothing
     * is silently tolerated; it simply fails where someone is looking.
     */
    private static String validate(final URI insertUri, final VictoriaLogsClientConfig config) {
        final List<String> faults = new ArrayList<>(2);
        if (insertUri.getScheme() == null || insertUri.getHost() == null) {
            faults.add("the url must be absolute and include a scheme and host, for example "
                    + "http://localhost:9428 -- got: " + redact(config.getUrl()));
        }
        if (insertUri.getUserInfo() != null) {
            LOG.warn("The VictoriaLogs url embeds credentials in its userinfo component; use the "
                    + "username and password properties instead so they stay out of the logs.");
        }
        try {
            config.requireCompleteCredentials();
        } catch (final IllegalArgumentException incomplete) {
            faults.add(incomplete.getMessage());
        }
        // Every fault at once. Reporting only the first costs the operator a diagnose-fix-reload
        // cycle per mistake, for a set that was fully known the first time.
        return faults.isEmpty() ? null : String.join("; ", faults);
    }

    /**
     * Renders a url for a message, without its credentials.
     *
     * <p>This string reaches the ERROR log, {@link #getConfigurationError()}, the health check's
     * operator-facing text and every {@link VictoriaLogsException} raised from a misconfigured
     * client. A url carrying userinfo — {@code http://user:secret@host} — is exactly the mistake
     * that lands here, so echoing it verbatim would spread the secret to all four.
     */
    static String redact(final String url) {
        if (url == null || url.isEmpty()) {
            return "<empty>";
        }
        final int at = url.lastIndexOf('@');
        final int scheme = url.indexOf("//");
        if (at < 0 || scheme < 0 || at < scheme) {
            return url;
        }
        return url.substring(0, scheme + 2) + "<redacted>" + url.substring(at);
    }

    /** @return why this client cannot be used, or null when it is usable */
    public String getConfigurationError() {
        return configurationError;
    }

    /** Fails the caller with the recorded configuration fault, if there is one. */
    private void requireUsable() throws VictoriaLogsException {
        if (configurationError != null) {
            throw new VictoriaLogsException("VictoriaLogs is misconfigured: " + configurationError);
        }
    }

    private static URI buildInsertUri(final VictoriaLogsClientConfig config) {
        final StringBuilder sb = new StringBuilder(trimTrailingSlash(config.getUrl()))
                .append(INSERT_PATH)
                .append("?_time_field=").append(encode(config.getTimeField()));
        if (config.getStreamFields() != null && !config.getStreamFields().isEmpty()) {
            sb.append("&_stream_fields=").append(encode(config.getStreamFields()));
        }
        return URI.create(sb.toString());
    }

    /**
     * Ships a newline-delimited JSON body to VictoriaLogs.
     *
     * <p>A 2xx response means the request was accepted, not that every line in it was stored —
     * VictoriaLogs skips lines it cannot parse. Callers that need to know whether anything was
     * dropped must compare against {@link #fetchIngestStats()}; this method deliberately does not
     * pretend to offer per-document accounting the protocol cannot give it.
     */
    public void ingest(final String ndjson) throws VictoriaLogsException {
        requireUsable();
        if (ndjson == null || ndjson.isEmpty()) {
            return;
        }
        final byte[] raw = ndjson.getBytes(StandardCharsets.UTF_8);
        final boolean compress = config.isHttpCompression();
        final byte[] body = compress ? gzip(raw) : raw;

        final HttpRequest.Builder builder = HttpRequest.newBuilder(insertUri)
                .timeout(config.getReadTimeout())
                .header("Content-Type", "application/x-ndjson")
                .POST(HttpRequest.BodyPublishers.ofByteArray(body));
        if (compress) {
            builder.header("Content-Encoding", "gzip");
        }
        applyAuth(builder);

        final HttpResponse<String> response;
        try {
            response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (final IOException e) {
            throw new VictoriaLogsException("Failed to send flows to " + insertUri, e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VictoriaLogsException("Interrupted while sending flows to " + insertUri, e);
        }

        if (response.statusCode() / 100 != 2) {
            throw new VictoriaLogsException("VictoriaLogs rejected the request with HTTP "
                    + response.statusCode() + ": " + response.body());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Ingested {} bytes ({} on the wire) into VictoriaLogs.", raw.length, body.length);
        }
    }

    /**
     * Runs a LogsQL query and returns one object per result line.
     *
     * <p>{@code /select/logsql/query} answers with newline-delimited JSON rather than a single
     * document, so results stream rather than arriving as one array.
     */
    public List<JsonObject> query(final String logsQl) throws VictoriaLogsException {
        requireUsable();
        final HttpRequest.Builder builder =
                HttpRequest.newBuilder(URI.create(trimTrailingSlash(config.getUrl()) + QUERY_PATH))
                        .timeout(config.getReadTimeout())
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString("query=" + encode(logsQl)));
        applyAuth(builder);

        final HttpResponse<String> response;
        try {
            response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (final IOException e) {
            throw new VictoriaLogsException("Failed to query VictoriaLogs", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VictoriaLogsException("Interrupted while querying VictoriaLogs", e);
        }
        if (response.statusCode() / 100 != 2) {
            throw new VictoriaLogsException("VictoriaLogs rejected the query with HTTP "
                    + response.statusCode() + ": " + response.body());
        }

        final List<JsonObject> rows = new ArrayList<>();
        for (final String line : response.body().split("\n")) {
            if (!line.isBlank()) {
                rows.add(JsonParser.parseString(line).getAsJsonObject());
            }
        }
        return rows;
    }

    /** True when VictoriaLogs answers its health endpoint. */
    public boolean isHealthy() {
        if (configurationError != null) {
            return false;
        }
        try {
            final HttpRequest.Builder builder =
                    HttpRequest.newBuilder(URI.create(trimTrailingSlash(config.getUrl()) + HEALTH_PATH))
                            .timeout(config.getConnectTimeout())
                            .GET();
            applyAuth(builder);
            final HttpResponse<String> response =
                    httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return response.statusCode() / 100 == 2;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (final Exception e) {
            LOG.debug("VictoriaLogs health check failed.", e);
            return false;
        }
    }

    /**
     * Reads the ingestion counters from VictoriaLogs' Prometheus endpoint.
     *
     * <p>This is not decoration. Because the ingestion protocol reports acceptance rather than
     * storage, reconciling what was sent against {@code vl_rows_ingested_total} and
     * {@code vl_rows_dropped_total} is the only way to detect loss — notably flows whose timestamps
     * fall outside the configured retention window, which VictoriaLogs discards silently.
     */
    public IngestStats fetchIngestStats() throws VictoriaLogsException {
        requireUsable();
        final HttpRequest.Builder builder =
                HttpRequest.newBuilder(URI.create(trimTrailingSlash(config.getUrl()) + METRICS_PATH))
                        .timeout(config.getReadTimeout())
                        .GET();
        applyAuth(builder);
        try {
            final HttpResponse<String> response =
                    httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new VictoriaLogsException("Failed to read metrics: HTTP " + response.statusCode());
            }
            return IngestStats.parse(response.body());
        } catch (final IOException e) {
            throw new VictoriaLogsException("Failed to read metrics from VictoriaLogs", e);
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new VictoriaLogsException("Interrupted while reading metrics from VictoriaLogs", e);
        }
    }

    private void applyAuth(final HttpRequest.Builder builder) {
        if (config.hasCredentials()) {
            final String token = config.getUsername() + ':' + config.getPassword();
            builder.header("Authorization", "Basic "
                    + Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private static byte[] gzip(final byte[] raw) {
        final ByteArrayOutputStream out = new ByteArrayOutputStream(raw.length / 4);
        try (final GZIPOutputStream gz = new GZIPOutputStream(out)) {
            gz.write(raw);
        } catch (final IOException e) {
            // Compressing an in-memory buffer cannot fail for any reason the caller could act on.
            throw new UncheckedIOException(e);
        }
        return out.toByteArray();
    }

    private static String trimTrailingSlash(final String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static String encode(final String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    /**
     * Releases the client's selector thread and default executor.
     *
     * <p>Not optional: the blueprint property placeholder uses {@code update-strategy="reload"}, so
     * every configuration change destroys and recreates this bean. Leaving the underlying
     * {@link HttpClient} to garbage collection would leak a selector thread and a thread pool on
     * each reload.
     */
    @Override
    public void close() {
        httpClient.close();
    }
}
