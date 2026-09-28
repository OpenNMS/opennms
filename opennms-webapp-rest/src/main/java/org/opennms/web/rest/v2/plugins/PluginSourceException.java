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
package org.opennms.web.rest.v2.plugins;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.net.ssl.SSLException;

/**
 * A failure talking to a plugin repository, already phrased for the operator and
 * mapped to the HTTP status the REST layer answers with.
 */
public class PluginSourceException extends Exception {
    private static final long serialVersionUID = 1L;

    public static final int BAD_GATEWAY = 502;
    public static final int GATEWAY_TIMEOUT = 504;
    public static final int INSUFFICIENT_STORAGE = 507;

    static final String FROM_FILE = "or load the plugin from a file instead.";

    private final int status;

    public PluginSourceException(final int status, final String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    static PluginSourceException transport(final IOException e, final String host, final int readTimeoutMillis) {
        if (e instanceof InterruptedIOException) {
            return new PluginSourceException(GATEWAY_TIMEOUT, label(host) + " did not answer within " + (readTimeoutMillis / 1000) + " s. Try again later, " + FROM_FILE);
        }
        final String reason;
        if (e instanceof UnknownHostException) {
            reason = "the name " + host + " cannot be resolved";
        } else if (e instanceof SSLException) {
            reason = "TLS handshake failed (" + describe(e) + ")";
        } else {
            reason = describe(e);
        }
        return new PluginSourceException(BAD_GATEWAY, "Could not reach " + host + " from this server: " + reason + ". Check the server's network access or proxy settings, " + FROM_FILE);
    }

    static PluginSourceException rateLimited(final String resetEpochSeconds) {
        return new PluginSourceException(BAD_GATEWAY, "GitHub rate limit reached; resets at " + resetTime(resetEpochSeconds)
                + ". Set " + GitHubReleases.TOKEN_PROPERTY + " in opennms.properties to raise it, or load from a file.");
    }

    static String label(final String host) {
        final String lower = host == null ? "" : host.toLowerCase(Locale.ROOT);
        return lower.equals("github.com") || lower.endsWith(".github.com") || lower.endsWith(".githubusercontent.com") ? "GitHub" : host;
    }

    static String describe(final Throwable t) {
        final String message = t.getMessage();
        return message == null || message.isBlank() ? t.getClass().getSimpleName() : message;
    }

    private static String resetTime(final String epochSeconds) {
        try {
            final Instant reset = Instant.ofEpochSecond(Long.parseLong(epochSeconds.trim()));
            return DateTimeFormatter.ofPattern("HH:mm z", Locale.ROOT).format(reset.atZone(ZoneId.systemDefault()));
        } catch (final RuntimeException e) {
            return "the top of the hour";
        }
    }
}
