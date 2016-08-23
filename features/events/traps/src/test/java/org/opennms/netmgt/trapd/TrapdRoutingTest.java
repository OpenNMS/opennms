/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
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
				onException(IOException.class)
					.handled(true)
					.logStackTrace(true)
					.stop();

				from("netty-http:http://localhost:8980/opennms/rest/config/trapd")
					.process(new JaxbUtilsUnmarshalProcessor(TrapdConfigBean.class))
					.to("bean:trapd?method=onUpdate")
					.to("mock:result")
					.bean(TrapReceiverImpl.class, "setTrapdConfig");
			}
		};
	}


	@Test
	public void testTrapRouting() throws Exception {
		for (RouteDefinition route : new ArrayList<RouteDefinition>(context.getRouteDefinitions())) {
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
