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
package org.opennms.core.test.http;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jetty.http.HttpVersion;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JUnitServer {
    private static final Logger LOG = LoggerFactory.getLogger(JUnitServer.class);

    private Server m_server;
    private JUnitHttpServer m_config;

    public JUnitServer(final JUnitHttpServer config) {
        initializeServerWithConfig(config);
        m_config = config;
    }

    protected void initializeServerWithConfig(final JUnitHttpServer config) {
        Server server = null;
        if (config.https()) {
            server = new Server();

            // SSL context configuration
            SslContextFactory sslContextFactory = new SslContextFactory.Server();
            sslContextFactory.setKeyStorePath(config.keystore());
            sslContextFactory.setKeyStorePassword(config.keystorePassword());
            sslContextFactory.setKeyManagerPassword(config.keyPassword());
            sslContextFactory.setTrustStorePath(config.keystore());
            sslContextFactory.setTrustStorePassword(config.keystorePassword());

            // HTTP Configuration
            HttpConfiguration http_config = new HttpConfiguration();
            http_config.setSecureScheme("https");
            http_config.setSecurePort(config.port());
            http_config.setOutputBufferSize(32768);
            http_config.setRequestHeaderSize(8192);
            http_config.setResponseHeaderSize(8192);
            http_config.setSendServerVersion(true);
            http_config.setSendDateHeader(false);

            // SSL HTTP Configuration
            HttpConfiguration https_config = new HttpConfiguration(http_config);
            https_config.addCustomizer(new SecureRequestCustomizer());

            // SSL Connector
            ServerConnector sslConnector = new ServerConnector(server,
                new SslConnectionFactory(sslContextFactory, HttpVersion.HTTP_1_1.asString()),
                new HttpConnectionFactory(https_config));
            sslConnector.setPort(config.port());
            server.addConnector(sslConnector);
        } else {
            server = new Server(config.port());
        }
        m_server = server;
        final ContextHandler context1 = new ContextHandler();
        context1.setContextPath("/");
        context1.setWelcomeFiles(new String[]{"index.html"});
        context1.setResourceBase(config.resource());
        context1.setClassLoader(Thread.currentThread().getContextClassLoader());
        context1.setVirtualHosts(config.vhosts());

        final ContextHandler context = context1;

        Handler topLevelHandler = null;
        final HandlerList handlers = new HandlerList();

        if (config.basicAuth()) {
            // check for basic auth if we're configured to do so
            LOG.debug("configuring basic auth");

            final HashLoginService loginService = new HashLoginService("MyRealm", config.basicAuthFile());
            loginService.setHotReload(true);
            m_server.addBean(loginService);

            final ConstraintSecurityHandler security = new ConstraintSecurityHandler();

            final Set<String> knownRoles = new HashSet<>();
            knownRoles.add("user");
            knownRoles.add("admin");
            knownRoles.add("moderator");

            final Constraint constraint = new Constraint();
            constraint.setName("auth");
            constraint.setAuthenticate(true);
            constraint.setRoles(knownRoles.toArray(new String[0]));

            final ConstraintMapping mapping = new ConstraintMapping();
            mapping.setPathSpec("/*");
            mapping.setConstraint(constraint);

            security.setConstraintMappings(Collections.singletonList(mapping), knownRoles);
            security.setAuthenticator(new BasicAuthenticator());
            security.setLoginService(loginService);
            security.setRealmName("MyRealm");

            security.setHandler(context);
            topLevelHandler = security;
        } else {
            topLevelHandler = context;
        }

        final Webapp[] webapps = config.webapps();
        if (webapps != null) {
            for (final Webapp webapp : webapps) {
                final WebAppContext wac = new WebAppContext();
                String path = null;
                if (!"".equals(webapp.pathSystemProperty()) && System.getProperty(webapp.pathSystemProperty()) != null) {
                    path = System.getProperty(webapp.pathSystemProperty());
                } else {
                    path = webapp.path();
                }
                if (path == null || "".equals(path)) {
                    throw new IllegalArgumentException("path or pathSystemProperty of @Webapp points to a null or blank value");
                }
                wac.setWar(path);
                wac.setContextPath(webapp.context());
                handlers.addHandler(wac);
            }
        }

        final ResourceHandler rh = new ResourceHandler();
        rh.setWelcomeFiles(new String[]{"index.html"});
        rh.setResourceBase(config.resource());
        handlers.addHandler(rh);

        // fall through to default
        handlers.addHandler(new DefaultHandler());

        context.setHandler(handlers);
        m_server.setHandler(topLevelHandler);        
    }

    public synchronized void start() throws Exception {
        LOG.debug("starting jetty on port {}", m_config.port());
        m_server.start();
    }

    // NOTE: we retry server stop because of a concurrency issue inside Jetty that is not
    // easily solvable.
    public synchronized void stop() throws Exception {
        LOG.debug("shutting down jetty on port {}", m_config.port());
        try {
            m_server.stop();
        } catch (final InterruptedException e) {
            LOG.debug("Interrupted while attempting to shut down Jetty, propagating interrupt and trying again.", e);
            Thread.currentThread().interrupt();
            m_server.stop();
        } catch (final RuntimeException e) {
            LOG.debug("An exception occurred while attempting to shut down Jetty.", e);
            m_server.stop();
            throw e;
        }
    }

    public synchronized int getPort() {
        if (m_server == null) return -1;

        for (final Connector conn : m_server.getConnectors()) {
            System.err.println("connector = " + conn);
            if (conn instanceof ServerConnector) {
                return ((ServerConnector)conn).getLocalPort();
            }
        }

        return -1;
    }
}
