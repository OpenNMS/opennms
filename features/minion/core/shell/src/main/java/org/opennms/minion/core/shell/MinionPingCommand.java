/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/
package org.opennms.minion.core.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.minion.core.api.RestClient;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Session;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

@Command(scope = "minion", name = "ping", description="Tests connectivity with the controller.")
@Service
public class MinionPingCommand implements Action {

    @Reference
    private BundleContext bundleContext;

    @Reference
    public RestClient restClient;

    @Option(name = "-j", description = "Maximum number of milliseconds to wait before failing when attempting to establish a JMS session.")
    public long jmsTimeoutMillis = 20L * 1000L;

    @Override
    public Object execute() throws Exception {
        System.out.println("Connecting to ReST...");
        restClient.ping();
        System.out.println("OK");

        final ServiceReference<ConnectionFactory> connectionFactoryRef = bundleContext.getServiceReference(ConnectionFactory.class);
        if (connectionFactoryRef != null) {
            final ConnectionFactory connectionFactory = bundleContext.getService(connectionFactoryRef);
            if (connectionFactory != null) {
                System.out.println("Connecting to Broker...");
                testJmsConnectivity(connectionFactory, jmsTimeoutMillis);
                System.out.println("OK");
            }
        }
        return null;
    }

    private void testJmsConnectivity(ConnectionFactory connectionFactory, long maxDurationMillis) throws InterruptedException, ExecutionException, TimeoutException {
        final AtomicReference<Throwable> throwableRef = new AtomicReference<>();
        // Establishing the session in separate thread, allowing
        // us to control how long we wait for.
        final Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
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
                                System.out.println("Failed to close the JMS connection: " + ex.getMessage());
                            }
                        }
                    }
                } catch (Throwable t) {
                    throwableRef.set(t);
                }
            }
        });
        t.setName("minion:ping");
        t.start();
        t.join(maxDurationMillis);
        if (t.isAlive()) {
            t.interrupt();
            throw new TimeoutException(String.format("Failed to create a JMS session within %d milliseconds.", maxDurationMillis));
        }
        if (throwableRef.get() != null) {
            throw new ExecutionException("Failed to create a JMS session.", throwableRef.get());
        }
    }
}
