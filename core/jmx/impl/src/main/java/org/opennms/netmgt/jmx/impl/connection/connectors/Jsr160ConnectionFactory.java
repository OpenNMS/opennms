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
