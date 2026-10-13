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
package org.opennms.core.camel.hardening;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Map;

import javax.jms.Message;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.camel.component.jms.JmsBinding;
import org.apache.camel.component.jms.JmsEndpoint;
import org.apache.camel.component.jms.JmsHeaderFilterStrategy;
import org.junit.Test;

/**
 * Runs the allow list through Camel's real JMS binding, which filters on the
 * property name as sent and only decodes it afterwards.
 */
public class JmsBindingInboundFilterTest {

    private static Map<String, Object> extract(final Message message) {
        final JmsEndpoint endpoint = new JmsEndpoint();
        endpoint.setHeaderFilterStrategy(new InboundHeaderAllowList(new JmsHeaderFilterStrategy(), Arrays.asList("JmsQueueName", "SystemId")));
        return new JmsBinding(endpoint).extractHeadersFromJms(message, null);
    }

    @Test
    public void keepsOnlyAllowedProperties() throws Exception {
        final ActiveMQTextMessage message = new ActiveMQTextMessage();
        message.setStringProperty("CamelExecCommandExecutable", "/bin/sh");
        message.setStringProperty("cAMELFileName", "x");
        message.setStringProperty("org_DOT_apache_DOT_camel_DOT_Foo", "x");
        message.setStringProperty("JmsQueueName", "OpenNMS.Sink.Heartbeat");
        message.setStringProperty("SystemId", "minion-1");
        message.setStringProperty("org_DOT_opennms_DOT_Foo", "x");

        final Map<String, Object> headers = extract(message);

        for (String dropped : new String[] { "CamelExecCommandExecutable", "cAMELFileName",
                "org.apache.camel.Foo", "org_DOT_apache_DOT_camel_DOT_Foo", "org.opennms.Foo", "org_DOT_opennms_DOT_Foo" }) {
            assertFalse(dropped, headers.containsKey(dropped));
        }
        assertEquals("OpenNMS.Sink.Heartbeat", headers.get("JmsQueueName"));
        assertEquals("minion-1", headers.get("SystemId"));
        // standard JMS header fields bypass the strategy
        assertTrue(headers.containsKey("JMSDeliveryMode"));
    }
}
