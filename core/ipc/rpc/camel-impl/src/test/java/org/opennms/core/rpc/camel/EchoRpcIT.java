/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.core.rpc.camel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RequestRejectedException;
import org.opennms.core.rpc.api.RequestTimedOutException;
import org.opennms.core.rpc.echo.EchoClient;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;
import org.opennms.core.rpc.echo.MyEchoException;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-queuingservice-mq-vm.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-client-camel.xml",
        "classpath:/META-INF/opennms/applicationContext-rpc-echo.xml"
})
@JUnitConfigurationEnvironment
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
@TestExecutionListeners({DirtiesContextTestExecutionListener.class})
public class EchoRpcIT {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker s_broker = new ActiveMQBroker();

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    @Qualifier("queuingservice")
    private ActiveMQComponent queuingservice;

    @Autowired
    private EchoClient echoClient;

    @Autowired
    @Qualifier("rpcClient")
    private CamelContext rpcClientContext;

    @Test(timeout=60000)
    public void canExecuteRpcViaCurrentLocation() throws InterruptedException, ExecutionException {
        EchoRequest request = new EchoRequest("HELLO!");
        EchoResponse expectedResponse = new EchoResponse("HELLO!");
        EchoResponse actualResponse = echoClient.execute(request).get();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test(timeout=60000)
    public void canExecuteRpcViaAnotherLocation() throws Exception {
        assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());
        EchoRpcModule echoRpcModule = new EchoRpcModule();

        SimpleRegistry registry = new SimpleRegistry();
        CamelContext context = new DefaultCamelContext(registry);
        context.addComponent("queuingservice", queuingservice);

        CamelRpcServerRouteManager routeManager = new CamelRpcServerRouteManager(context,
                new MockMinionIdentity(REMOTE_LOCATION_NAME));
        routeManager.bind(echoRpcModule);

        EchoRequest request = new EchoRequest("HELLO!!!");
        request.setLocation(REMOTE_LOCATION_NAME);
        EchoResponse expectedResponse = new EchoResponse("HELLO!!!");
        EchoResponse actualResponse = echoClient.execute(request).get();
        assertEquals(expectedResponse, actualResponse);

        routeManager.unbind(echoRpcModule);
        context.stop();
    }

    /**
     * Verifies that the future fails with the original exception if
     * an error occurs when executing locally.
     */
    @Test(timeout=60000)
    public void futureFailsWithOriginalExceptionWhenExecutingLocally() throws InterruptedException, ExecutionException {
        EchoRequest request = new EchoRequest("Oops!");
        request.shouldThrow(true);
        try {
            echoClient.execute(request).get();
            fail();
        } catch (ExecutionException e) {
            assertEquals("Oops!", e.getCause().getMessage());
            assertEquals(MyEchoException.class, e.getCause().getClass());
        }
    }

    /**
     * Verifies that the future fails with a {@code RemoteExecutionException} when
     * if an error occurs when executing remotely.
     */
    @Test(timeout=60000)
    public void futureFailsWithRemoteExecutionExceptionWhenExecutingRemotely() throws Exception {
        assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());
        EchoRpcModule echoRpcModule = new EchoRpcModule();

        SimpleRegistry registry = new SimpleRegistry();
        CamelContext context = new DefaultCamelContext(registry);
        context.addComponent("queuingservice", queuingservice);

        CamelRpcServerRouteManager routeManager = new CamelRpcServerRouteManager(context,
                new MockMinionIdentity(REMOTE_LOCATION_NAME));
        routeManager.bind(echoRpcModule);

        EchoRequest request = new EchoRequest("Oops!");
        request.shouldThrow(true);
        request.setLocation(REMOTE_LOCATION_NAME);
        try {
            echoClient.execute(request).get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getMessage(), e.getCause().getMessage().contains("Oops!"));
            assertEquals(RemoteExecutionException.class, e.getCause().getClass());
        }

        routeManager.unbind(echoRpcModule);
        context.stop();
    }

    /**
     * Verifies that the future fails with a {@code RequestRejectedException} when
     * when the client context is stopped.
     */
    @Test(timeout=60000)
    public void futureFailsWithRequestRejectedExceptionWhenClientContextIsStopped() throws Exception {
        assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());

        // Stop the client context, this will happen when OpenNMS is shutting down
        rpcClientContext.stop();

        // Now issue an RPC
        EchoRequest request = new EchoRequest("Helló");
        request.setLocation(REMOTE_LOCATION_NAME);
        try {
            echoClient.execute(request).get();
            fail();
        } catch (ExecutionException e) {
            assertEquals(RequestRejectedException.class, e.getCause().getClass());
        }
    }

    @Test(timeout=60000)
    public void checkDefinedTimeout() throws Exception {
        System.getProperties().setProperty(CamelRpcClientPreProcessor.CAMEL_JMS_REQUEST_TIMEOUT_PROPERTY, "12345");

        SimpleRegistry registry = new SimpleRegistry();
        CamelContext context = new DefaultCamelContext(registry);
        context.addComponent("queuingservice", queuingservice);

        CamelRpcRequest<EchoRequest,EchoResponse> wrapper = new CamelRpcRequest<>(new EchoRpcModule(), new EchoRequest());

        CamelRpcClientPreProcessor camelRpcClientPreProcessor = new CamelRpcClientPreProcessor();
        DefaultExchange defaultExchange = new DefaultExchange(context);
        defaultExchange.getIn().setBody(wrapper);
        camelRpcClientPreProcessor.process(defaultExchange);

        context.stop();

        assertEquals(12345L, defaultExchange.getIn().getHeader(CamelRpcConstants.CAMEL_JMS_REQUEST_TIMEOUT_HEADER));
    }

    @Test(timeout=60000)
    public void checkUndefinedTimeout() throws Exception {
        SimpleRegistry registry = new SimpleRegistry();
        CamelContext context = new DefaultCamelContext(registry);
        context.addComponent("queuingservice", queuingservice);

        CamelRpcRequest<EchoRequest,EchoResponse> wrapper = new CamelRpcRequest<>(new EchoRpcModule(), new EchoRequest());

        CamelRpcClientPreProcessor camelRpcClientPreProcessor = new CamelRpcClientPreProcessor();
        DefaultExchange defaultExchange = new DefaultExchange(context);
        defaultExchange.getIn().setBody(wrapper);
        camelRpcClientPreProcessor.process(defaultExchange);

        context.stop();

        assertEquals(CamelRpcClientPreProcessor.CAMEL_JMS_REQUEST_TIMEOUT_DEFAULT, defaultExchange.getIn().getHeader(CamelRpcConstants.CAMEL_JMS_REQUEST_TIMEOUT_HEADER));
    }

    @Test(timeout=60000)
    public void checkZeroTimeout() throws Exception {
        System.getProperties().setProperty(CamelRpcClientPreProcessor.CAMEL_JMS_REQUEST_TIMEOUT_PROPERTY, "0");

        SimpleRegistry registry = new SimpleRegistry();
        CamelContext context = new DefaultCamelContext(registry);
        context.addComponent("queuingservice", queuingservice);

        EchoRequest echoRequest = new EchoRequest();
        CamelRpcRequest<EchoRequest,EchoResponse> wrapper = new CamelRpcRequest<>(new EchoRpcModule(), echoRequest);

        CamelRpcClientPreProcessor camelRpcClientPreProcessor = new CamelRpcClientPreProcessor();
        DefaultExchange defaultExchange = new DefaultExchange(context);
        defaultExchange.getIn().setBody(wrapper);
        camelRpcClientPreProcessor.process(defaultExchange);

        context.stop();

        assertEquals(CamelRpcClientPreProcessor.CAMEL_JMS_REQUEST_TIMEOUT_DEFAULT, defaultExchange.getIn().getHeader(CamelRpcConstants.CAMEL_JMS_REQUEST_TIMEOUT_HEADER));
    }

    @Test(timeout=CamelRpcClientPreProcessor.CAMEL_JMS_REQUEST_TIMEOUT_DEFAULT * 4)
    public void throwsRequestTimedOutExceptionOnTimeout() throws Exception {
        assertNotEquals(REMOTE_LOCATION_NAME, identity.getLocation());
        EchoRpcModule echoRpcModule = new EchoRpcModule();

        SimpleRegistry registry = new SimpleRegistry();
        CamelContext context = new DefaultCamelContext(registry);
        context.getShutdownStrategy().setTimeout(5);
        context.getShutdownStrategy().setTimeUnit(TimeUnit.SECONDS);
        context.addComponent("queuingservice", queuingservice);

        CamelRpcServerRouteManager routeManager = new CamelRpcServerRouteManager(context,
                new MockMinionIdentity(REMOTE_LOCATION_NAME));
        routeManager.bind(echoRpcModule);

        EchoRequest request = new EchoRequest("HELLO!!!");
        request.setLocation(REMOTE_LOCATION_NAME);
        request.setDelay(CamelRpcClientPreProcessor.CAMEL_JMS_REQUEST_TIMEOUT_DEFAULT * 2);

        try {
            echoClient.execute(request).get();
            fail("Did not get ExecutionException");
        } catch (ExecutionException e) {
            assertTrue("Cause is not of type RequestTimedOutException: " + ExceptionUtils.getStackTrace(e), e.getCause() instanceof RequestTimedOutException);
        }

        routeManager.unbind(echoRpcModule);
        context.stop();
    }
}
