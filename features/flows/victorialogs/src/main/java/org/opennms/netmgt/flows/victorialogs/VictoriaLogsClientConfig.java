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

import java.time.Duration;
import java.util.Objects;

/**
 * Connection and ingestion settings for {@link VictoriaLogsClient}.
 *
 * <p>The stream fields deserve a note, because getting them wrong is the single most likely way to
 * make VictoriaLogs perform badly. VictoriaLogs indexes every field and handles high-cardinality
 * values without complaint, but only as <em>regular</em> fields. Fields listed in
 * {@code _stream_fields} must be low-cardinality and constant for the lifetime of a stream —
 * associating something like {@code netflow.src_addr} with a stream produces unbounded stream churn
 * that degrades both ingestion and querying. The default below is therefore limited to the exporter
 * identity and the flow direction; the address/port tuple stays out of it deliberately.
 */
public class VictoriaLogsClientConfig {

    /**
     * Exporter location, exporter node and direction are constant per stream and bounded by the size
     * of the managed estate. Everything else stays a regular field.
     */
    public static final String DEFAULT_STREAM_FIELDS = "location,node_exporter.node_id,netflow.direction";

    /**
     * The serializer emits a dedicated RFC3339 {@code _time} alongside the epoch-milli
     * {@code @timestamp} it keeps for wire compatibility, and we point VictoriaLogs at the
     * unambiguous one. See {@link FlowJsonSerializer#TIME_FIELD}.
     */
    public static final String DEFAULT_TIME_FIELD = FlowJsonSerializer.TIME_FIELD;

    private String url = "http://localhost:9428";
    private String username;
    private String password;
    private String streamFields = DEFAULT_STREAM_FIELDS;
    private String timeField = DEFAULT_TIME_FIELD;
    private boolean httpCompression = true;
    private Duration connectTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(30);

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = Objects.requireNonNull(url);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getStreamFields() {
        return streamFields;
    }

    public void setStreamFields(final String streamFields) {
        this.streamFields = streamFields;
    }

    public String getTimeField() {
        return timeField;
    }

    /**
     * @param timeField the field VictoriaLogs reads a record's instant from
     *
     * <p>Deliberately not exposed as a Config Admin property: {@code FlowJsonSerializer} writes the
     * instant into {@code _time} specifically, so changing this without changing the serializer
     * gives VictoriaLogs a field that does not exist and every record takes the ingestion time. It
     * stays settable for tests.
     */
    public void setTimeField(final String timeField) {
        this.timeField = timeField;
    }

    public boolean isHttpCompression() {
        return httpCompression;
    }

    public void setHttpCompression(final boolean httpCompression) {
        this.httpCompression = httpCompression;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeoutMs(final int connectTimeoutMs) {
        this.connectTimeout = Duration.ofMillis(connectTimeoutMs);
    }

    public Duration getReadTimeout() {
        return readTimeout;
    }

    /**
     * @param readTimeoutMs deadline for a whole request/response exchange, in milliseconds
     *
     * <p>Named for familiarity, but this is not a socket read timeout: it becomes
     * {@code HttpRequest.timeout()}, which bounds the <em>total</em> exchange. A large gzipped batch
     * over a slow uplink can exceed it while the server is responding perfectly normally, so size it
     * against the batch, not against the round trip.
     */
    public void setReadTimeoutMs(final int readTimeoutMs) {
        this.readTimeout = Duration.ofMillis(readTimeoutMs);
    }

    /** True when both a username and a password are set, i.e. requests should carry Basic auth. */
    /** Whether a complete Basic credential pair was configured. */
    public boolean hasCredentials() {
        return username != null && !username.isEmpty()
                && password != null && !password.isEmpty();
    }

    /**
     * Refuses half a credential pair.
     *
     * <p>A username with no password — a rotation half-applied, or a token-style username someone
     * expected to stand alone — otherwise downgrades silently to anonymous and produces a 401 on
     * every batch, surfacing only as flows lost to a backend that looks unreachable. The one INFO
     * line that mentions authentication was written at startup and says {@code authenticated=false},
     * which reads as intentional.
     *
     * <p>Separate from {@link #hasCredentials()} rather than folded into it, because that method is
     * called from {@link #toString()} and a {@code toString} that throws would take out the very log
     * line meant to describe the configuration.
     */
    public void requireCompleteCredentials() {
        final boolean hasUser = username != null && !username.isEmpty();
        final boolean hasPassword = password != null && !password.isEmpty();
        if (hasUser != hasPassword) {
            throw new IllegalArgumentException("VictoriaLogs credentials are incomplete: "
                    + (hasUser ? "a username was configured without a password"
                               : "a password was configured without a username")
                    + ". Set both, or neither for anonymous access.");
        }
    }

    @Override
    public String toString() {
        return "VictoriaLogsClientConfig{url=" + url
                + ", streamFields=" + streamFields
                + ", timeField=" + timeField
                + ", httpCompression=" + httpCompression
                + ", authenticated=" + hasCredentials()
                + '}';
    }
}
