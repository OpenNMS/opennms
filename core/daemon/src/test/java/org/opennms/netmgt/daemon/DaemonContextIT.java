/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.daemon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.jms.Connection;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.nms20397.DeserializationProbe;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.opennms.core.camel.hardening.DiscardUnreadableBodies;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false)
public class DaemonContextIT {

    @Autowired
    ActiveMQConnectionFactory activeMQConnectionFactory;

    @Autowired
    @Qualifier("queuingservice")
    JmsComponent queuingservice;

	/**
	* Verifies that the embedded ActiveMQ broker bootstraps successfully
	* and is accessible using the provided connection factory.
	*/
	@Test
	public void canUseEmbeddedActiveMQBroker() throws Throwable {
	    QueueConnection connection = activeMQConnectionFactory.createQueueConnection();
	    QueueSession session = connection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE );
	    TextMessage message = session.createTextMessage();
	    message.setText("ping");
	    QueueSender sender = session.createSender(session.createQueue("pong"));
	    sender.send(message);
	}

	/**
	* Verifies that inbound Camel headers are filtered on the JMS component (CVE-2026-40453).
	*/
	@Test
	public void queuingserviceDropsInboundCamelHeaders() {
	    assertTrue(queuingservice.getHeaderFilterStrategy().applyFilterToExternalHeaders("CAmelExecCommandExecutable", "x", null));
	    assertFalse(queuingservice.getHeaderFilterStrategy().applyFilterToExternalHeaders("JmsQueueName", "x", null));
	}

	/**
	* A producer on the broker sets Camel internal headers, in plain, case-variant and
	* JMS-encoded form, on a message consumed through queuingservice (CVE-2026-40453).
	*/
	@Test
	public void queuingserviceDropsInboundCamelHeadersEndToEnd() throws Exception {
	    final DefaultCamelContext camel = new DefaultCamelContext();
	    camel.addComponent("queuingservice", queuingservice);
	    camel.addRoutes(new RouteBuilder() {
	        @Override
	        public void configure() {
	            from("queuingservice:queue:NMS-20397.headers").to("mock:headers");
	        }
	    });
	    camel.start();
	    try {
	        final MockEndpoint mock = camel.getEndpoint("mock:headers", MockEndpoint.class);
	        mock.expectedMessageCount(1);
	        sendRaw("NMS-20397.headers", session -> {
	            final TextMessage m = session.createTextMessage("payload");
	            m.setStringProperty("CamelExecCommandExecutable", "/bin/sh");
	            m.setStringProperty("cAMELJmsDestinationName", "somewhere-else");
	            m.setStringProperty("org_DOT_apache_DOT_camel_DOT_Foo", "x");
	            m.setStringProperty("SystemId", "minion-1");
	            m.setStringProperty("NotSomethingOpenNMSReads", "x");
	            return m;
	        });
	        mock.assertIsSatisfied(10000);
	        final org.apache.camel.Message in = mock.getExchanges().get(0).getIn();
	        for (String name : new String[] { "CamelExecCommandExecutable", "cAMELJmsDestinationName",
	                "CamelJmsDestinationName", "org.apache.camel.Foo", "org_DOT_apache_DOT_camel_DOT_Foo",
	                "NotSomethingOpenNMSReads" }) {
	            assertNull(name, in.getHeader(name));
	        }
	        assertEquals("minion-1", in.getHeader("SystemId"));
	        assertEquals("payload", in.getBody(String.class));
	    } finally {
	        camel.stop();
	    }
	}

	/**
	* A producer on the broker sends an ObjectMessage. The connection factory refuses to
	* deserialize it (CVE-2026-40860) and the route drops it once, without redelivery.
	*/
	@Test
	public void queuingserviceDropsObjectMessagesOnce() throws Exception {
	    final DefaultCamelContext camel = new DefaultCamelContext();
	    camel.getShutdownStrategy().setTimeout(5);
	    camel.addComponent("queuingservice", queuingservice);
	    final AtomicInteger deliveries = new AtomicInteger();
	    final BlockingQueue<String> bodies = new LinkedBlockingQueue<>();
	    camel.addRoutes(new RouteBuilder() {
	        @Override
	        public void configure() {
	            from("queuingservice:queue:NMS-20397.objects")
	                .process(exchange -> deliveries.incrementAndGet())
	                .process(new DiscardUnreadableBodies())
	                .process(exchange -> bodies.add(exchange.getIn().getBody(String.class)));
	        }
	    });
	    camel.start();
	    try {
	        sendRaw("NMS-20397.objects", session -> session.createObjectMessage(new DeserializationProbe()));
	        sendRaw("NMS-20397.objects", session -> session.createTextMessage("after"));
	        assertEquals("after", bodies.poll(10, TimeUnit.SECONDS));
	        Thread.sleep(2000); // long enough for a rollback to have been redelivered
	        assertEquals("ObjectMessage consumed once, text message once", 2, deliveries.get());
	        assertTrue(bodies.isEmpty());
	        assertFalse("payload must not be deserialized", DeserializationProbe.DESERIALIZED.get());
	    } finally {
	        camel.stop();
	    }
	}

	private interface MessageFactory {
	    Message create(Session session) throws Exception;
	}

	/** Sends with a plain client connection, marshalling the message as a remote producer would. */
	private void sendRaw(final String queue, final MessageFactory factory) throws Exception {
	    final ActiveMQConnectionFactory producerFactory = new ActiveMQConnectionFactory("vm://localhost?create=false&marshal=true");
	    try (Connection connection = producerFactory.createConnection()) {
	        final Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
	        final MessageProducer producer = session.createProducer(session.createQueue(queue));
	        producer.send(factory.create(session));
	    }
	}
}
