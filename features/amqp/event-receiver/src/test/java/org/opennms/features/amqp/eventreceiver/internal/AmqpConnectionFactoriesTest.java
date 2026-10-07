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
package org.opennms.features.amqp.eventreceiver.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDeserializationPolicy;
import org.junit.Test;

public class AmqpConnectionFactoriesTest {

    private static JmsDeserializationPolicy policyOf(final JmsConnectionFactory factory) {
        return factory.getDeserializationPolicy();
    }

    @Test
    public void rejectsEveryClassByDefault() {
        final JmsConnectionFactory factory = AmqpConnectionFactories.create("u", "p", "amqp://localhost:5672", "");
        assertFalse(policyOf(factory).isTrustedType(null, String.class));
        assertFalse(policyOf(factory).isTrustedType(null, java.util.HashMap.class));
        assertEquals("topic://", factory.getTopicPrefix());
        assertEquals("amqp://localhost:5672", factory.getRemoteURI());
    }

    @Test
    public void honoursConfiguredWhiteList() {
        final JmsConnectionFactory factory = AmqpConnectionFactories.create("u", "p", "amqp://localhost:5672", "java.lang, org.opennms");
        assertTrue(policyOf(factory).isTrustedType(null, String.class));
        assertFalse(policyOf(factory).isTrustedType(null, java.util.HashMap.class));
    }

    @Test
    public void uriOptionsStillOverrideThePolicy() {
        final JmsConnectionFactory factory = AmqpConnectionFactories.create("u", "p",
                "amqp://localhost:5672?jms.deserializationPolicy.whiteList=java.util", "");
        assertTrue(policyOf(factory).isTrustedType(null, java.util.HashMap.class));
        assertFalse(policyOf(factory).isTrustedType(null, String.class));
        assertEquals("amqp://localhost:5672", factory.getRemoteURI());
    }
}
