package org.opennms.core.test.http;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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
            final SslContextFactory factory = new SslContextFactory(config.keystore());
            factory.setKeyStorePath(config.keystore());
            factory.setKeyStorePassword(config.keystorePassword());
            factory.setKeyManagerPassword(config.keyPassword());
            factory.setTrustStore(config.keystore());
            factory.setTrustStorePassword(config.keystorePassword());
            
            final SslSocketConnector connector = new SslSocketConnector(factory);
            connector.setPort(config.port());
            server.setConnectors(new Connector[] { connector });
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
}
