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
package org.opennms.core.wsman.utils;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.opennms.core.wsman.Identity;
import org.opennms.core.wsman.WSManClient;
import org.opennms.core.wsman.WSManClientFactory;
import org.opennms.core.wsman.WSManEndpoint;
import org.opennms.core.wsman.WSManVersion;
import org.opennms.core.wsman.cxf.CXFWSManClientFactory;
import org.opennms.core.wsman.exceptions.WSManException;
import org.opennms.core.wsman.shell.CommandResult;
import org.opennms.core.wsman.shell.ShellOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;

/**
 * A {@link WSManClientFactory} that reuses clients for endpoints with Kerberos
 * message encryption enabled.
 *
 * <p>The collector, monitor, detectors and asset adapter each create a client per
 * operation. That is free for basic or plain GSS authentication, since those clients
 * hold no state. A Kerberos-encrypted client, however, owns a JAAS login, a GSS
 * context and the TCP connection the Windows host bound that context to. Recreating
 * it on every poll means a KDC round trip and a handshake per poll, and closing it
 * on every poll throws that work away. This factory keeps one client per distinct
 * endpoint so successive operations share the session, which is how the library is
 * meant to be used.
 *
 * <p>Callers still work with try-with-resources: the client handed out for a cached
 * endpoint is a thin wrapper whose {@link WSManClient#close()} is a no-op, so the
 * shared session survives the caller. The library reaps idle sessions on its own
 * (the connection after a minute, the GSS context and login after fifteen), and a
 * reaped session re-establishes itself on next use, so an entry that sits in this
 * cache costs nothing while idle. Entries that go unused for {@link #EXPIRE_AFTER}
 * are dropped and their client closed, which covers endpoints that disappear from
 * the configuration.
 *
 * <p>Endpoints without Kerberos encryption are passed straight through to the
 * delegate and are not cached, so their behaviour is unchanged.
 *
 * <p>Operations on a shared Kerberos client serialize on its connection, so two
 * threads polling the same endpoint through the same factory instance take turns.
 * That is a protocol constraint, not a choice made here.
 */
public class CachingWSManClientFactory implements WSManClientFactory {
    private static final Logger LOG = LoggerFactory.getLogger(CachingWSManClientFactory.class);

    static final Duration EXPIRE_AFTER = Duration.ofHours(1);

    private final WSManClientFactory m_delegate;

    private final Cache<EndpointKey, WSManClient> m_clients;

    public CachingWSManClientFactory() {
        this(new CXFWSManClientFactory());
    }

    public CachingWSManClientFactory(WSManClientFactory delegate) {
        this(delegate, EXPIRE_AFTER);
    }

    CachingWSManClientFactory(WSManClientFactory delegate, Duration expireAfterAccess) {
        m_delegate = Objects.requireNonNull(delegate, "delegate");
        m_clients = CacheBuilder.newBuilder()
                .expireAfterAccess(expireAfterAccess.toNanos(), TimeUnit.NANOSECONDS)
                .removalListener((RemovalNotification<EndpointKey, WSManClient> n) -> closeQuietly(n.getValue()))
                .build();
    }

    @Override
    public WSManClient getClient(WSManEndpoint endpoint) {
        Objects.requireNonNull(endpoint, "endpoint");
        if (!endpoint.isKerberosEncryption()) {
            return m_delegate.getClient(endpoint);
        }
        final EndpointKey key = new EndpointKey(endpoint);
        try {
            final WSManClient shared = m_clients.get(key, () -> {
                LOG.debug("Creating shared Kerberos-encrypted WS-Man client for {}", endpoint);
                return m_delegate.getClient(endpoint);
            });
            return new SharedClient(shared);
        } catch (ExecutionException e) {
            throw new WSManException("Failed to create WS-Man client for " + endpoint, e.getCause());
        }
    }

    /**
     * Closes and forgets every cached client.
     */
    public void close() {
        m_clients.invalidateAll();
        m_clients.cleanUp();
    }

    /** Number of clients currently cached. Visible for tests. */
    long size() {
        m_clients.cleanUp();
        return m_clients.size();
    }

    private static void closeQuietly(WSManClient client) {
        if (client == null) {
            return;
        }
        try {
            client.close();
        } catch (Exception e) {
            LOG.debug("Error closing WS-Man client {}", client, e);
        }
    }

    /**
     * Identity of a {@link WSManEndpoint}, which has no equals/hashCode of its own.
     */
    static final class EndpointKey {
        private final URL url;
        private final String username;
        private final String password;
        private final boolean gssAuth;
        private final boolean kerberosEncryption;
        private final boolean strictSSL;
        private final WSManVersion serverVersion;
        private final Integer maxElements;
        private final Integer maxEnvelopeSize;
        private final Integer connectionTimeout;
        private final Integer receiveTimeout;

        EndpointKey(WSManEndpoint e) {
            url = e.getUrl();
            username = e.getUsername();
            password = e.getPassword();
            gssAuth = e.isGSSAuth();
            kerberosEncryption = e.isKerberosEncryption();
            strictSSL = e.isStrictSSL();
            serverVersion = e.getServerVersion();
            maxElements = e.getMaxElements();
            maxEnvelopeSize = e.getMaxEnvelopeSize();
            connectionTimeout = e.getConnectionTimeout();
            receiveTimeout = e.getReceiveTimeout();
        }

        @Override
        public int hashCode() {
            // URL.hashCode() resolves the host; compare the external form instead
            return Objects.hash(url.toExternalForm(), username, password, gssAuth, kerberosEncryption, strictSSL,
                    serverVersion, maxElements, maxEnvelopeSize, connectionTimeout, receiveTimeout);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof EndpointKey)) {
                return false;
            }
            final EndpointKey other = (EndpointKey) obj;
            return Objects.equals(url.toExternalForm(), other.url.toExternalForm())
                    && Objects.equals(username, other.username)
                    && Objects.equals(password, other.password)
                    && gssAuth == other.gssAuth
                    && kerberosEncryption == other.kerberosEncryption
                    && strictSSL == other.strictSSL
                    && serverVersion == other.serverVersion
                    && Objects.equals(maxElements, other.maxElements)
                    && Objects.equals(maxEnvelopeSize, other.maxEnvelopeSize)
                    && Objects.equals(connectionTimeout, other.connectionTimeout)
                    && Objects.equals(receiveTimeout, other.receiveTimeout);
        }
    }

    /**
     * Delegates every operation to the shared client but ignores {@link #close()},
     * so callers can use try-with-resources uniformly without tearing down the
     * shared session.
     */
    static final class SharedClient implements WSManClient {
        private final WSManClient m_delegate;

        SharedClient(WSManClient delegate) {
            m_delegate = Objects.requireNonNull(delegate);
        }

        WSManClient getDelegate() {
            return m_delegate;
        }

        @Override
        public Identity identify() {
            return m_delegate.identify();
        }

        @Override
        public Node get(String resourceUri, Map<String, String> selectors) {
            return m_delegate.get(resourceUri, selectors);
        }

        @Override
        public String enumerate(String resourceUri) {
            return m_delegate.enumerate(resourceUri);
        }

        @Override
        public String enumerateWithFilter(String resourceUri, String dialect, String filter) {
            return m_delegate.enumerateWithFilter(resourceUri, dialect, filter);
        }

        @Override
        public String pull(String contextId, String resourceUri, List<Node> nodes, boolean recursive) {
            return m_delegate.pull(contextId, resourceUri, nodes, recursive);
        }

        @Override
        public String enumerateAndPull(String resourceUri, List<Node> nodes, boolean recursive) {
            return m_delegate.enumerateAndPull(resourceUri, nodes, recursive);
        }

        @Override
        public String enumerateAndPullUsingFilter(String resourceUri, String dialect, String filter, List<Node> nodes, boolean recursive) {
            return m_delegate.enumerateAndPullUsingFilter(resourceUri, dialect, filter, nodes, recursive);
        }

        @Override
        public CommandResult runCommand(String executable, String[] args, Duration timeout, ShellOptions options) {
            return m_delegate.runCommand(executable, args, timeout, options);
        }

        @Override
        public void close() {
            // The shared client is owned by the factory
        }

        @Override
        public String toString() {
            return m_delegate.toString();
        }
    }
}
