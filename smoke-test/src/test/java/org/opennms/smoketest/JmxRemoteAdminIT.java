/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.smoketest;

import static com.jayway.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMISocketFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.bootstrap.HostRMIServerSocketFactory;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class JmxRemoteAdminIT {
    private static final Logger LOG = LoggerFactory.getLogger(JmxRemoteAdminIT.class);
    private static TestEnvironment m_testEnvironment;

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        LOG.warn("Setting up Docker test environment.");
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().opennms();
            builder.skipTearDown(Boolean.getBoolean("org.opennms.smoketest.docker.skipTearDown"));
            builder.useExisting(Boolean.getBoolean("org.opennms.smoketest.docker.useExisting"));

            builder.withOpenNMSEnvironment()
            .optIn(false)
            .addFile(OpenNMSSeleniumTestCase.class.getResource("etc/monitoring-locations.xml"), "etc/monitoring-locations.xml")
            .addFiles(Paths.get("src/test/resources/org/opennms/smoketest/etc"), "etc");
            m_testEnvironment = builder.build();
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
        return m_testEnvironment;
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    public static final class JMXTestClientSocketFactory extends HostRMIServerSocketFactory {
        private static final long serialVersionUID = 1L;
        @Override
        public Socket createSocket(final String host, final int port) throws IOException {
            final Socket s = new Socket(host, port);
            s.setTcpNoDelay(true);
            s.setReuseAddress(true);
            s.setSoTimeout(5000);
            return s;
        }
    }

    @Test
    public void canConnect() throws Exception {
        final InetSocketAddress addr = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 18980);
        final String hostString = "localhost".equals(addr.getHostString())? "127.0.0.1" : addr.getHostString();
        final int port = addr.getPort();

        final RMISocketFactory socketFactory = new JMXTestClientSocketFactory();

        System.setProperty("sun.rmi.transport.tcp.responseTimeout", "5000");
        final Callable<Integer> getRmiConnection = new Callable<Integer>() {
            @Override public Integer call() throws Exception {
                LOG.debug("getRmiConnection({}:{})", hostString, port);
                try {
                    final Registry registry = LocateRegistry.getRegistry(hostString, port, socketFactory);
                    final String[] bound = registry.list();
                    LOG.debug("bound={}", Arrays.asList(bound));
                    if (bound.length > 0) {
                        return bound.length;
                    }
                } catch (final Exception e) {
                }
                return null;
            }
        };
        await().atMost(5, MINUTES).pollInterval(10, SECONDS).until(getRmiConnection, greaterThanOrEqualTo(1));

        final Callable<Integer> getJmxConnection = new Callable<Integer>() {
            @Override public Integer call() throws Exception {
                LOG.debug("getJmxConnection({}:{})", hostString, port);
                try {
                    RMISocketFactory.setSocketFactory(socketFactory);
                    final Map<String,Object> env = new HashMap<>();
                    final String[] credentials = { "admin", "admin" };
                    env.put(JMXConnector.CREDENTIALS, credentials);
                    env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, socketFactory);
                    final String urlString = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", hostString, port);
                    LOG.debug("getJmxConnection(): connecting to {}", urlString);
                    final JMXServiceURL url = new JMXServiceURL(urlString);
                    final JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
                    final MBeanServerConnection  mbsc = jmxc.getMBeanServerConnection();
                    LOG.debug("mbeanCount={}", mbsc.getMBeanCount());
                    if (mbsc.getMBeanCount() > 0) {
                        return mbsc.getMBeanCount();
                    }
                } catch (final Exception e) {
                }
                return null;
            }
        };
        await().atMost(5, MINUTES).pollInterval(10, SECONDS).until(getJmxConnection, greaterThanOrEqualTo(1));
    }
}
