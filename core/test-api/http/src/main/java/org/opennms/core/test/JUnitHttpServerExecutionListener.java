package org.opennms.core.test;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.ContextHandler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.security.Constraint;
import org.mortbay.jetty.security.ConstraintMapping;
import org.mortbay.jetty.security.HashUserRealm;
import org.mortbay.jetty.security.SecurityHandler;
import org.mortbay.jetty.security.SslSocketConnector;
import org.opennms.core.test.annotations.JUnitHttpServer;
import org.opennms.core.utils.LogUtils;
import org.springframework.test.context.TestContext;

public class JUnitHttpServerExecutionListener extends OpenNMSAbstractTestExecutionListener {
    private Server m_server;
    
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
            constraint.setName(Constraint.__BASIC_AUTH);;
            constraint.setRoles(new String[]{"user","admin","moderator"});
            constraint.setAuthenticate(true);

            ConstraintMapping cm = new ConstraintMapping();
            cm.setConstraint(constraint);
            cm.setPathSpec("/*");

            SecurityHandler sh = new SecurityHandler();
            sh.setUserRealm(new HashUserRealm("MyRealm",config.basicAuthFile()));
            sh.setConstraintMappings(new ConstraintMapping[]{cm});

            handlers.addHandler(sh);
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
