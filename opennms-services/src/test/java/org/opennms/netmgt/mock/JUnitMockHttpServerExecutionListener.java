package org.opennms.netmgt.mock;

import java.lang.reflect.Method;

import org.mortbay.jetty.Handler;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

public class JUnitMockHttpServerExecutionListener extends AbstractTestExecutionListener {
    private Server m_server;

    @Override
    public void beforeTestMethod(TestContext testContext) throws Exception {
        JUnitMockHttpServer config = findCollectorAnnotation(testContext);
        if (config == null) {
            return;
        }

        m_server = new Server(config.port());
        ResourceHandler rh = new ResourceHandler();
        rh.setResourceBase(config.directory());
        
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{rh, new DefaultHandler()});
        m_server.setHandler(handlers);
        
        m_server.start();
        Thread.sleep(100);
    }
    
    @Override
    public void afterTestMethod(TestContext testContext) throws Exception {
        JUnitMockHttpServer config = findCollectorAnnotation(testContext);
        if (config == null) {
            return;
        }
        
        if (m_server != null) {
            m_server.stop();
        }
    }
    
    private JUnitMockHttpServer findCollectorAnnotation(TestContext testContext) {
        Method testMethod = testContext.getTestMethod();
        JUnitMockHttpServer config = testMethod.getAnnotation(JUnitMockHttpServer.class);
        if (config != null) {
            return config;
        }

        Class<?> testClass = testContext.getTestClass();
        return testClass.getAnnotation(JUnitMockHttpServer.class);
    }

}
