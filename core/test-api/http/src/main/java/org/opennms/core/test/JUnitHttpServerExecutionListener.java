package org.opennms.core.test;

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
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
            m_server.setConnectors(new Connector[] { connector });
        } else {
            m_server = new Server(config.port());
        }

        ResourceHandler handler = new ResourceHandler();
        handler.setWelcomeFiles(new String[]{"index.html"});
        handler.setResourceBase(config.resource());
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{handler,new DefaultHandler()});
        m_server.setHandler(handlers);
        
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
