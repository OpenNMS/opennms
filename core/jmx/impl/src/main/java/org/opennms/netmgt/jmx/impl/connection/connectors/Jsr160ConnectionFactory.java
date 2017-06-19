/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jmx.impl.connection.connectors;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opennms.netmgt.jmx.connection.JmxConnectionConfigBuilder;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates a connection to the remote server.
 */
public abstract class Jsr160ConnectionFactory {
    
    private static final Logger LOG = LoggerFactory.getLogger(Jsr160ConnectionFactory.class);

    private static final long DEFAULT_TIMEOUT = 30000; // 30 secs

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    public static JmxServerConnectionWrapper getMBeanServerConnection(Map<String,String> propertiesMap, InetAddress address) throws IOException {
        final long timeout = DEFAULT_TIMEOUT;

        propertiesMap.putIfAbsent("factory", "STANDARD");
        propertiesMap.putIfAbsent("port",     "1099");
        propertiesMap.putIfAbsent("protocol", "rmi");
        propertiesMap.putIfAbsent("urlPath",  "/jmxrmi");
        propertiesMap.putIfAbsent("timeout", Long.toString(timeout));

        final Callable<JmxServerConnectionWrapper> task = new Callable<JmxServerConnectionWrapper>() {
            @Override
            public JmxServerConnectionWrapper call() throws Exception {
                return new DefaultJmxConnector().createConnection(address, propertiesMap);
            }
        };
        final Future<JmxServerConnectionWrapper> future = executor.submit(task);
        try {
            final JmxServerConnectionWrapper connectionWrapper = future.get(timeout, TimeUnit.MILLISECONDS);
            return connectionWrapper;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            final String url = JmxConnectionConfigBuilder.buildFrom(address, propertiesMap).build().getUrl();
            LOG.info("Exception connecting JMXConnectorFactory url {} , Error: {}", url, e.getMessage());
            if (!future.isDone()) {
                future.cancel(true);
                LOG.info(" the task {}", future.isCancelled() ? "was cancelled" : "could not be cancelled");
            }
            throw new ConnectException("Error connecting JMXConnectionFactory  " + url);
        }
    }
}
