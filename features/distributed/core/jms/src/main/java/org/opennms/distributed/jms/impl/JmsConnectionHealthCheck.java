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
package org.opennms.distributed.jms.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;

import org.opennms.core.health.api.Context;
import org.opennms.core.health.api.HealthCheck;
import org.opennms.core.health.api.Response;
import org.opennms.core.health.api.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import static org.opennms.core.health.api.HealthCheckConstants.BROKER;

/**
 * Verifies that a connection to the configured ActiveMQ Broker can be established.
 * In earlier versions of this a timeout was implemented individually.
 * With the introduction of the {@link HealthCheck} interface, the {@link org.opennms.core.health.api.HealthCheckService}
 * should take care of this.
 *
 * @author mvrueden
 */
public class JmsConnectionHealthCheck implements HealthCheck {

    private final BundleContext bundleContext;

    public JmsConnectionHealthCheck(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public String getDescription() {
        return "Connecting to JMS Broker";
    }

    @Override
    public List<String> getTags() {
        return Arrays.asList(BROKER);
    }

    @Override
    public Response perform(Context context) {
        final ServiceReference<ConnectionFactory> connectionFactoryRef = bundleContext.getServiceReference(ConnectionFactory.class);
        if (connectionFactoryRef != null) {
            try {
                final ConnectionFactory connectionFactory = bundleContext.getService(connectionFactoryRef);
                if (connectionFactory != null) {
                    Connection jmsConnection = null;
                    try {
                        jmsConnection = connectionFactory.createConnection();
                        // NMS-9445: Attempt to use the connection by creating a session
                        // and immediately closing it.
                        jmsConnection.createSession(false, Session.AUTO_ACKNOWLEDGE).close();
                    } finally{
                        if (jmsConnection != null) {
                            try {
                                jmsConnection.close();
                            } catch(JMSException ex) {
                                return new Response(Status.Failure, "Failed to close the JMS connection: " + ex.getMessage());
                            }
                        }
                    }
                    return new Response(Status.Success);
                }
            } catch (Exception ex) {
                return new Response(Status.Failure, "Failed to create a JMS session." + ex.getMessage());
            } finally {
                bundleContext.ungetService(connectionFactoryRef);
            }
        }
        return null;
    }
}
