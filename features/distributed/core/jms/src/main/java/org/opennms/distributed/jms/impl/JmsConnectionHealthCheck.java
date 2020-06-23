/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.distributed.jms.impl;

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
