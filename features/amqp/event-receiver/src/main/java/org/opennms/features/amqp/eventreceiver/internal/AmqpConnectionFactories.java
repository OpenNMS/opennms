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

import org.apache.qpid.jms.JmsConnectionFactory;
import org.apache.qpid.jms.policy.JmsDefaultDeserializationPolicy;

/**
 * Builds the Qpid JMS connection factory the way AMQPComponent.amqpComponent()
 * does, but with a restrictive ObjectMessage deserialization policy.
 *
 * Qpid JMS otherwise deserializes any class from an ObjectMessage (CVE-2026-40860),
 * and the receiver only reads text messages.
 */
public final class AmqpConnectionFactories {

    private AmqpConnectionFactories() {
    }

    /**
     * @param whiteList comma-separated classes or packages an ObjectMessage may
     *                  deserialize to; empty rejects every class
     */
    public static JmsConnectionFactory create(final String username, final String password,
            final String remoteUri, final String whiteList) {
        final JmsDefaultDeserializationPolicy policy = new JmsDefaultDeserializationPolicy();
        policy.setWhiteList(whiteList == null ? "" : whiteList.trim());

        final JmsConnectionFactory factory = new JmsConnectionFactory(username, password);
        factory.setTopicPrefix("topic://");
        factory.setDeserializationPolicy(policy);
        // last, so jms.* options in the URI (e.g. jms.deserializationPolicy.whiteList) still win
        factory.setRemoteURI(remoteUri);
        return factory;
    }
}
