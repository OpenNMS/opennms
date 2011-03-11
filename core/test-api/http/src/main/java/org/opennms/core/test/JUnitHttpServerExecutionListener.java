package org.opennms.core.test;

import org.eclipse.jetty.http.security.Constraint;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;
import org.opennms.core.test.annotations.JUnitHttpServer;
import org.opennms.core.test.annotations.Webapp;
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
    public void beforeTestMethod(TestContext testContext) throws Exception {
        super.beforeTestMethod(testContext);
        
        JUnitHttpServer config = findTestAnnotation(JUnitHttpServer.class, testContext);
        
        if (config == null)
            return;

        if (config.https()) {
            m_server = new Server();
            SslSocketConnector connector = new SslSocketConnector();
            connector.setPort(config.port());
            connector.setKeystore(config.keystore());
            connector.setPassword(config.keystorePassword());
            connector.setKeyPassword(config.keyPassword());
            connector.setTruststore(config.keystore());
            connector.setTrustPassword(config.keystorePassword());
            m_server.setConnectors(new Connector[] { connector });
        } else {
            m_server = new Server(config.port());
        }

        ContextHandler context = new ContextHandler();
        context.setContextPath("/");
        context.setWelcomeFiles(new String[]{"index.html"});
        context.setResourceBase(config.resource());
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        context.setVirtualHosts(config.vhosts());

        HandlerList handlers = new HandlerList();

        if (config.basicAuth()) {
            // check for basic auth if we're configured to do so
            Constraint constraint = new Constraint();
            constraint.setName(Constraint.__BASIC_AUTH);
            constraint.setRoles(new String[]{"user","admin","moderator"});
            constraint.setAuthenticate(true);

            ConstraintMapping cm = new ConstraintMapping();
            cm.setConstraint(constraint);
            cm.setPathSpec("/*");

            HashLoginService service = new HashLoginService("MyRealm", config.basicAuthFile());
            service.setRefreshInterval(300000);
            service.start();

            ConstraintSecurityHandler sh = new ConstraintSecurityHandler();
            sh.setLoginService(service);
            sh.setRealmName("MyRealm");
            sh.addConstraintMapping(cm);
            handlers.addHandler(sh);
        }

        Webapp[] webapps = config.webapps();
        if (webapps != null) {
            for (Webapp webapp : webapps) {
                WebAppContext wac = new WebAppContext();
                wac.setWar(webapp.path());
                wac.setContextPath(webapp.context());
                handlers.addHandler(wac);
            }
        }

        ResourceHandler rh = new ResourceHandler();
        rh.setWelcomeFiles(new String[]{"index.html"});
        rh.setResourceBase(config.resource());
        handlers.addHandler(rh);

        // fall through to default
        handlers.addHandler(new DefaultHandler());

        context.setHandler(handlers);
        m_server.setHandler(context);

        LogUtils.debugf(this, "starting jetty on port %d", config.port());
        m_server.start();
    }

    /** {@inheritDoc} */
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        super.afterTestMethod(testContext);

        JUnitHttpServer config = findTestAnnotation(JUnitHttpServer.class, testContext);
        if (config == null)
            return;
        
        LogUtils.debugf(this, "shutting down jetty on port %d", config.port());
        m_server.stop();
    }

}
