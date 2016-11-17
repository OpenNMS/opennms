package org.opennms.core.rpc.camel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.spi.Synchronization;
import org.apache.camel.spi.UnitOfWork;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.rpc.echo.EchoClient;
import org.opennms.core.rpc.echo.EchoRequest;
import org.opennms.core.rpc.echo.EchoResponse;
import org.opennms.core.rpc.echo.EchoRpcModule;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.minion.core.api.MinionIdentity;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

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
public class EchoRpcIT {

    private static final String REMOTE_LOCATION_NAME = "remote";

    @ClassRule
    public static ActiveMQBroker s_broker = new ActiveMQBroker();

    @Autowired
    private OnmsDistPoller identity;

    @Autowired
    @Qualifier("queuingservice")
    private Component queuingservice;

    @Autowired
    private EchoClient echoClient;

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

        CamelRpcServerRouteManager routeManager = new CamelRpcServerRouteManager(context, new MinionIdentity() {
            @Override
            public String getId() {
                return "0";
            }

            @Override
            public String getLocation() {
                return REMOTE_LOCATION_NAME;
            }
        });
        routeManager.bind(echoRpcModule);

        EchoRequest request = new EchoRequest("HELLO!!!");
        request.setLocation(REMOTE_LOCATION_NAME);
        EchoResponse expectedResponse = new EchoResponse("HELLO!!!");
        EchoResponse actualResponse = echoClient.execute(request).get();
        assertEquals(expectedResponse, actualResponse);

        routeManager.unbind(echoRpcModule);
        context.stop();
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
}
