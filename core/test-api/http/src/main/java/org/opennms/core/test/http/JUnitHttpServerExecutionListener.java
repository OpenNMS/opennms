/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import org.eclipse.jetty.http.security.Constraint;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.opennms.core.test.OpenNMSAbstractTestExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.utils.LogUtils;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListener;

/**
 * This {@link TestExecutionListener} looks for the {@link JUnitHttpServer} annotation
 * and uses attributes on it to launch a temporary HTTP server for use during unit tests.
 *
 * @author ranger
 * @version $Id: $
 */
public class JUnitHttpServerExecutionListener extends OpenNMSAbstractTestExecutionListener {
    private Server m_server;
    
    /** {@inheritDoc} */
    @Override
    public void beforeTestMethod(final TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);
        
        final JUnitHttpServer config = findTestAnnotation(JUnitHttpServer.class, testContext);
        
        if (config == null)
            return;

        if (config.https()) {
            m_server = new Server();
            final SslContextFactory factory = new SslContextFactory(config.keystore());
            factory.setKeyStore(config.keystore());
            factory.setKeyStorePassword(config.keystorePassword());
            factory.setKeyManagerPassword(config.keyPassword());
            factory.setTrustStore(config.keystore());
            factory.setTrustStorePassword(config.keystorePassword());
            
            final SslSocketConnector connector = new SslSocketConnector(factory);
            connector.setPort(config.port());
            m_server.setConnectors(new Connector[] { connector });
        } else {
            m_server = new Server(config.port());
        }

        final ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        context.setWelcomeFiles(new String[]{"index.html"});
        context.setResourceBase(config.resource());
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setVirtualHosts(config.vhosts());

        Handler topLevelHandler = null;
        final HandlerList handlers = new HandlerList();

        if (config.basicAuth()) {
            // check for basic auth if we're configured to do so
        	LogUtils.debugf(this, "configuring basic auth");

            final HashLoginService loginService = new HashLoginService("MyRealm", config.basicAuthFile());
            loginService.setRefreshInterval(300000);
            m_server.addBean(loginService);

            final ConstraintSecurityHandler security = new ConstraintSecurityHandler();

            final Set<String> knownRoles = new HashSet<String>();
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
            security.setStrict(false);
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
                wac.setWar(webapp.path());
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

        LogUtils.debugf(this, "starting jetty on port %d", config.port());
        m_server.start();
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestMethod(final TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);

        final JUnitHttpServer config = findTestAnnotation(JUnitHttpServer.class, testContext);
        if (config == null)
            return;
        
        LogUtils.debugf(this, "shutting down jetty on port %d", config.port());
        m_server.stop();
    }

}
