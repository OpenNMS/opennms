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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.jms.JmsComponent;
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

}
