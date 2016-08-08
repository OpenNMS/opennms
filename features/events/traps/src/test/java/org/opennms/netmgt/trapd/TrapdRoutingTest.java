package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.camel.JaxbUtilsUnmarshalProcessor;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/META-INF/opennms/emptyContext.xml" })
public class TrapdRoutingTest extends CamelTestSupport {

	@Override
    protected JndiRegistry createRegistry() throws Exception
    {
        JndiRegistry registry = super.createRegistry();

        registry.bind( "trapd", new TrapdConfigBean() );

        return registry;
    }

	/**
	 * Delay calling context.start() so that you can attach an
	 * {@link AdviceWithRouteBuilder} to the context before it starts.
	 */
	@Override
	public boolean isUseAdviceWith() {
		return true;
	}

	/**
	 * Build the route for all of the config parsing messages.
	 */
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				// Add exception handlers
				onException(IOException.class).handled(true)
						.logStackTrace(true).stop();

				from("netty-http:http://localhost:8980/opennms/rest/config/trapd")
						.process(
								new JaxbUtilsUnmarshalProcessor(
										TrapdConfigBean.class))
						.to("bean:trapd?method=onUpdate").to("mock:result")
						.bean(TrapReceiverImpl.class, "setTrapdConfig");

			}
		};
	}
	

	@Test
	public void testTrapRouting() throws Exception {
			for (RouteDefinition route : new ArrayList<RouteDefinition>(
					context.getRouteDefinitions())) {
				route.adviceWith(context, new AdviceWithRouteBuilder() {
					@Override
					public void configure() throws Exception {
						mockEndpoints();
					}
				});
			}
			context.start();

			MockEndpoint endpoint = getMockEndpoint( "mock:result", false );
			endpoint.setExpectedMessageCount( 1 );

			TrapdConfigBean config = new TrapdConfigBean();
			config.setSnmpTrapPort(10514);
			config.setSnmpTrapAddress("127.0.0.1");
			config.setNewSuspectOnTrap(false);

			template.requestBody( endpoint, config);
			
			assertNotNull(context.hasEndpoint("mock:result"));
			
			assertMockEndpointsSatisfied();
	}

}


