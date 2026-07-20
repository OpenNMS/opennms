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
package org.opennms.netmgt.config.tokenauth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.core.mate.api.FallbackScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.web.EmptyKeyRelaxedTrustProvider;
import org.opennms.core.web.HttpClientWrapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Performs the HTTP call described by an {@link TokenAuth} definition and
 * returns the resulting {@link CachedToken}. Stateless; a single instance
 * may serve many auth definitions and concurrent callers.
 */
public class TokenAcquirer {

    static {
        Security.addProvider(new EmptyKeyRelaxedTrustProvider());
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    private static final int DEFAULT_CONNECT_TIMEOUT_MS = 10_000;
    private static final int DEFAULT_SOCKET_TIMEOUT_MS = 30_000;

    private final int connectTimeoutMs;
    private final int socketTimeoutMs;
    private final Clock clock;
    private volatile EntityScopeProvider entityScopeProvider;

    public TokenAcquirer() {
        this(DEFAULT_CONNECT_TIMEOUT_MS, DEFAULT_SOCKET_TIMEOUT_MS, null, Clock.systemUTC());
    }

    public TokenAcquirer(final int connectTimeoutMs, final int socketTimeoutMs) {
        this(connectTimeoutMs, socketTimeoutMs, null, Clock.systemUTC());
    }

    public TokenAcquirer(final int connectTimeoutMs, final int socketTimeoutMs,
                         final EntityScopeProvider entityScopeProvider) {
        this(connectTimeoutMs, socketTimeoutMs, entityScopeProvider, Clock.systemUTC());
    }

    public TokenAcquirer(final int connectTimeoutMs, final int socketTimeoutMs,
                         final EntityScopeProvider entityScopeProvider,
                         final Clock clock) {
        this.connectTimeoutMs = connectTimeoutMs;
        this.socketTimeoutMs = socketTimeoutMs;
        this.entityScopeProvider = entityScopeProvider;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Autowired(required = false)
    public void setEntityScopeProvider(final EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = entityScopeProvider;
    }

    String interpolate(final String text, final Scope callerScope) {
        if (text == null) {
            return text;
        }
        final Scope chain = buildScope(callerScope);
        return Interpolator.interpolate(text, chain).output;
    }

    String interpolate(final String text) {
        return interpolate(text, null);
    }

    private Scope buildScope(final Scope callerScope) {
        final Scope scv = entityScopeProvider != null
                ? firstNonNull(entityScopeProvider.getScopeForScv(), EmptyScope.EMPTY)
                : EmptyScope.EMPTY;
        final Scope env = entityScopeProvider != null
                ? firstNonNull(entityScopeProvider.getScopeForEnv(), EmptyScope.EMPTY)
                : EmptyScope.EMPTY;
        if (callerScope == null) {
            return new FallbackScope(scv, env);
        }
        return new FallbackScope(scv, env, callerScope);
    }

    private static Scope firstNonNull(final Scope a, final Scope b) {
        return a != null ? a : b;
    }

    /**
     * Cache fingerprint over the resolved auth fields. Uses SHA-256 (not
     * {@link String#hashCode()}) because a 32-bit collision would let
     * one tenant's request receive another tenant's cached token; this
     * needs to be cryptographically collision-resistant.
     */
    public String fingerprint(final TokenAuth auth, final Scope callerScope) {
        final StringBuilder sb = new StringBuilder();
        sb.append(interpolate(auth.getUrl(), callerScope)).append('\0');
        sb.append(auth.getMethod()).append('\0');
        if (auth.getBasicAuth() != null) {
            sb.append(interpolate(auth.getBasicAuth().getUsername(), callerScope)).append('\0');
            sb.append(interpolate(auth.getBasicAuth().getPassword(), callerScope)).append('\0');
        } else {
            sb.append('\0').append('\0');
        }
        if (auth.getHeaders() != null) {
            for (final Header h : auth.getHeaders()) {
                sb.append(h.getName() == null ? "" : h.getName()).append('=');
                sb.append(interpolate(h.getValue(), callerScope)).append('\0');
            }
        }
        if (auth.getContent() != null) {
            sb.append(auth.getContent().getType() == null ? "" : auth.getContent().getType()).append('\0');
            sb.append(interpolate(auth.getContent().getData(), callerScope));
        }
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            final byte[] digest = md.digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            final StringBuilder hex = new StringBuilder(digest.length * 2);
            for (final byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (final NoSuchAlgorithmException e) {
            return sb.toString();
        }
    }

    public CachedToken acquire(final TokenAuth auth) throws IOException {
        return acquireInternal(auth, null);
    }

    public CachedToken acquire(final TokenAuth auth, final Scope callerScope) throws IOException {
        if (callerScope == null) {
            return acquire(auth);
        }
        return acquireInternal(auth, callerScope);
    }

    private CachedToken acquireInternal(final TokenAuth auth, final Scope callerScope) throws IOException {
        if (auth.getUrl() == null || auth.getUrl().isEmpty()) {
            throw new IOException("auth definition '" + auth.getName() + "' has no <url>");
        }
        if (auth.getTokenFrom() == null) {
            throw new IOException("auth definition '" + auth.getName() + "' has no <token-from>");
        }

        final HttpRequestBase request = buildRequest(auth, callerScope);
        try (HttpClientWrapper client = configureClient(auth);
             CloseableHttpResponse response = client.execute(request)) {
            final int status = response.getStatusLine().getStatusCode();
            if (status < 200 || status >= 300) {
                throw new IOException("auth call to " + auth.getUrl()
                        + " returned status " + status);
            }
            final String token = extractToken(auth.getTokenFrom(), response);
            return new CachedToken(token, computeExpiresAt(auth));
        }
    }

    private HttpClientWrapper configureClient(final TokenAuth auth) throws IOException {
        final HttpClientWrapper wrapper = HttpClientWrapper.create()
                .setConnectionTimeout(connectTimeoutMs)
                .setSocketTimeout(socketTimeoutMs);
        if (auth.isUseSystemProxy()) {
            wrapper.useSystemProxySettings();
        }
        if (auth.isDisableSslVerification()) {
            try {
                wrapper.useRelaxedSSL("https");
            } catch (final GeneralSecurityException e) {
                throw new IOException(
                        "failed to configure relaxed SSL for auth '" + auth.getName() + "'", e);
            }
        }
        return wrapper;
    }

    private HttpRequestBase buildRequest(final TokenAuth auth, final Scope callerScope) throws IOException {
        final String method = auth.getMethod();
        final String url = interpolate(auth.getUrl(), callerScope);
        final HttpRequestBase request;
        if ("GET".equalsIgnoreCase(method)) {
            request = new HttpGet(url);
        } else if ("POST".equalsIgnoreCase(method)) {
            request = new HttpPost(url);
        } else if ("PUT".equalsIgnoreCase(method)) {
            request = new HttpPut(url);
        } else {
            throw new IOException("unsupported HTTP method '" + method
                    + "' in auth definition '" + auth.getName() + "'");
        }

        if (auth.getBasicAuth() != null) {
            final String userPass = interpolate(auth.getBasicAuth().getUsername(), callerScope) + ":"
                    + interpolate(auth.getBasicAuth().getPassword(), callerScope);
            final String encoded = Base64.getEncoder()
                    .encodeToString(userPass.getBytes(StandardCharsets.UTF_8));
            request.setHeader("Authorization", "Basic " + encoded);
        }

        for (final Header h : auth.getHeaders()) {
            request.setHeader(h.getName(), interpolate(h.getValue(), callerScope));
        }

        if (auth.getContent() != null && request instanceof HttpEntityEnclosingRequestBase) {
            final Content c = auth.getContent();
            final StringEntity entity = new StringEntity(
                    c.getData() == null ? "" : interpolate(c.getData(), callerScope),
                    StandardCharsets.UTF_8);
            if (c.getType() != null && !c.getType().isEmpty()) {
                entity.setContentType(c.getType());
            }
            ((HttpEntityEnclosingRequestBase) request).setEntity(entity);
        }

        return request;
    }

    private String extractToken(final TokenFrom tf,
                                final HttpResponse response) throws IOException {
        // Header extraction does not consume the body; check it first.
        if (tf.getHeader() != null && !tf.getHeader().isEmpty()) {
            final org.apache.http.Header h = response.getFirstHeader(tf.getHeader());
            if (h == null) {
                throw new IOException("auth response missing header: " + tf.getHeader());
            }
            return h.getValue();
        }

        final HttpEntity entity = response.getEntity();
        final String body = entity == null ? "" : EntityUtils.toString(entity, StandardCharsets.UTF_8);

        if (tf.isBodyAsToken()) {
            String trimmed = body.trim();
            // Some APIs (notably vSphere v8) wrap the bare string in JSON quotes.
            if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                trimmed = trimmed.substring(1, trimmed.length() - 1);
            }
            if (trimmed.isEmpty()) {
                throw new IOException("auth response body was empty");
            }
            return trimmed;
        }

        if (tf.getJsonpath() != null && !tf.getJsonpath().isEmpty()) {
            return extractJsonPath(body, tf.getJsonpath());
        }

        throw new IOException("token-from must specify jsonpath, header, or body-as-token=\"true\"");
    }

    private String extractJsonPath(final String body, final String path) throws IOException {
        JsonNode node = JSON.readTree(body);
        for (final String segment : path.split("/")) {
            if (segment.isEmpty()) {
                continue;
            }
            node = node.path(segment);
        }
        if (node.isMissingNode() || node.isNull()) {
            throw new IOException("jsonpath '" + path + "' did not resolve in auth response");
        }
        return node.asText();
    }

    /**
     * Refresh buffer: the larger of 10 minutes or 5% of the configured
     * TTL. So ttl=1h refreshes 10 min early, ttl=24h refreshes 72 min
     * early. Prevents handing back a token that's about to be rejected
     * upstream.
     */
    private static final long REFRESH_BUFFER_FLOOR_SECONDS = 600L;
    private static final double REFRESH_BUFFER_FRACTION = 0.05;

    private Instant computeExpiresAt(final TokenAuth auth) {
        final Long ttl = auth.getTtlSeconds();
        if (ttl == null || ttl <= 0) {
            return null;
        }
        final long fractional = (long) Math.ceil(ttl * REFRESH_BUFFER_FRACTION);
        final long buffer = Math.max(REFRESH_BUFFER_FLOOR_SECONDS, fractional);
        // Guard tiny TTLs from underflowing into the past.
        final long effective = Math.max(1L, ttl - buffer);
        return clock.instant().plusSeconds(effective);
    }
}
