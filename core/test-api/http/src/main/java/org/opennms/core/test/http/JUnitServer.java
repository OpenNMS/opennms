/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
            SslContextFactory sslContextFactory = new SslContextFactory();
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
