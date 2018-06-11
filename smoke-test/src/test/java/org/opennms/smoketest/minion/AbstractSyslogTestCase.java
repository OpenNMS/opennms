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
package org.opennms.smoketest.minion;

import static com.jayway.awaitility.Awaitility.await;
import static com.jayway.awaitility.Awaitility.with;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.Timeout;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;

/**
 * Verifies that syslog messages sent to the Minion generate
 * events in OpenNMS.
 *
 * @author Seth
 * @author jwhite
 */
public abstract class AbstractSyslogTestCase {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSyslogTestCase.class);

    protected static final String SYSLOG_MESSAGE_UEI = "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic";

    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    private HibernateDaoFactory m_daoFactory;

    private static final AtomicInteger ORDINAL = new AtomicInteger();

    public final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            return getEnvironmentBuilder().build();
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    protected HibernateDaoFactory getDaoFactory() {
        if (m_daoFactory == null) {
            // Connect to the postgresql container
            final InetSocketAddress pgsql = testEnvironment.getServiceAddress(ContainerAlias.POSTGRES, 5432);
            m_daoFactory = new HibernateDaoFactory(pgsql);
        }
        return m_daoFactory;
    }

    /**
     * Override this method to customize the test environment.
     */
    protected TestEnvironmentBuilder getEnvironmentBuilder() {
        final TestEnvironmentBuilder builder = TestEnvironment.builder().all()
                // Enable Kafka
                .kafka();
        builder.withOpenNMSEnvironment()
                // Set logging to INFO level
                .addFile(AbstractSyslogTestCase.class.getResource("/log4j2-info.xml"), "etc/log4j2.xml")
                .addFile(AbstractSyslogTestCase.class.getResource("/eventconf.xml"), "etc/eventconf.xml")
                .addFile(AbstractSyslogTestCase.class.getResource("/events/Cisco.syslog.events.xml"), "etc/events/Cisco.syslog.events.xml")
                // Disable Alarmd, enable Syslogd
                .addFile(AbstractSyslogTestCase.class.getResource("/service-configuration-disable-alarmd.xml"), "etc/service-configuration.xml")
                .addFile(AbstractSyslogTestCase.class.getResource("/syslogd-configuration.xml"), "etc/syslogd-configuration.xml")
                .addFile(AbstractSyslogTestCase.class.getResource("/syslog/Cisco.syslog.xml"), "etc/syslog/Cisco.syslog.xml")
                // Switch sink impl to Kafka using opennms-properties.d file
                .addFile(AbstractSyslogTestCase.class.getResource("/opennms.properties.d/kafka-sink.properties"), "etc/opennms.properties.d/kafka-sink.properties");
        builder.withMinionEnvironment()
                // Switch sink impl to Kafka using features.boot file
                .addFile(AbstractSyslogTestCase.class.getResource("/featuresBoot.d/kafka.boot"), "etc/featuresBoot.d/kafka.boot");
        OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
        return builder;
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    /**
     * Install the Kafka features on Minion.
     * 
     * @param minionSshAddr
     * @param kafkaAddress
     * @throws Exception
     */
    protected static void installFeaturesOnMinion(InetSocketAddress minionSshAddr, InetSocketAddress kafkaAddress) throws Exception {
        try (final SshClient sshClient = new SshClient(minionSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("feature:list -i");
            pipe.println("list");
            // Set the log level to INFO
            pipe.println("log:set INFO");
            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }
    }

    protected static void installFeaturesOnOpenNMS(InetSocketAddress opennmsSshAddr, InetSocketAddress kafkaAddress, InetSocketAddress zookeeperAddress) throws Exception {
        try (final SshClient sshClient = new SshClient(opennmsSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();

            // Configure and install the Elasticsearch REST event forwarder
            pipe.println("config:edit org.opennms.plugin.elasticsearch.rest.forwarder");
            pipe.println("config:property-set elasticUrl http://elasticsearch:9200");
            pipe.println("config:property-set logAllEvents true");
            pipe.println("config:property-set batchSize 500");
            pipe.println("config:property-set batchInterval 500");
            pipe.println("config:property-set connTimeout 5000");
            pipe.println("config:property-set retries 10");
            pipe.println("config:update");
            pipe.println("feature:install opennms-es-rest");

            pipe.println("feature:list -i");
            // Set the log level to INFO
            pipe.println("log:set INFO");
            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }
    }

    protected static void resetRouteStatistics(InetSocketAddress opennmsSshAddr, InetSocketAddress minionSshAddr) throws Exception {
        // Reset route statistics on Minion
        try (final SshClient sshClient = new SshClient(minionSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();

            // Syslog listener
            pipe.println("camel:route-reset-stats syslogListen");

            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }

        // Reset route statistics on OpenNMS
        try (final SshClient sshClient = new SshClient(opennmsSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();

            // Elasticsearch forwarder
            pipe.println("camel:route-reset-stats alarmsFromOpennms");
            pipe.println("camel:route-reset-stats enrichAlarmsAndEvents");
            pipe.println("camel:route-reset-stats eventsFromOpennms");
            pipe.println("camel:route-reset-stats toElasticsearch");
            pipe.println("camel:route-reset-stats updateElastisearchTemplateMappingRunOnlyOnce");

            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }
    }

    protected static String doQuery() {
        return new SearchSourceBuilder()
                .query(QueryBuilders.matchQuery("eventuei", SYSLOG_MESSAGE_UEI))
                .toString();
    }

    protected static void pollForElasticsearchEventsUsingJest(InetSocketAddress esTransportAddr, int numMessages) {
        pollForElasticsearchEventsUsingJest(() -> esTransportAddr, numMessages, AbstractSyslogTestCase::doQuery);
    }

    protected static void pollForElasticsearchEventsUsingJest(InetSocketAddress esTransportAddr, int numMessages, Supplier<String> query) {
        pollForElasticsearchEventsUsingJest(() -> esTransportAddr, numMessages, query);
    }

    protected static void pollForElasticsearchEventsUsingJest(Supplier<InetSocketAddress> esTransportAddr, int numMessages) {
        pollForElasticsearchEventsUsingJest(esTransportAddr, numMessages, AbstractSyslogTestCase::doQuery);
    }

    protected static void pollForElasticsearchEventsUsingJest(Supplier<InetSocketAddress> esTransportAddr, int numMessages, Supplier<String> query) {
        with().pollInterval(15, SECONDS).await().atMost(5, MINUTES).until(() -> {
            JestClient client = null;
            try {
                JestClientFactory factory = new JestClientFactory();
                factory.setHttpClientConfig(new HttpClientConfig.Builder(String.format("http://%s:%d", esTransportAddr.get().getHostString(), esTransportAddr.get().getPort()))
                    .multiThreaded(true)
                    .build());
                client = factory.getObject();

                final String queryString = query.get();
                LOG.debug("SEARCH QUERY: {}", queryString);
                SearchResult response = client.execute(
                    new Search.Builder(queryString)
                        .addIndex("opennms*")
                        .build()
                );
                LOG.debug("SEARCH RESPONSE: {}", response.toString());

                // Sometimes, the first warm-up message is successful so treat both message counts as valid
                assertTrue("ES search hits was not equal to " + numMessages + ": " + response.getTotal(),
                    (numMessages == response.getTotal())
                );
                //assertEquals("Event UEI did not match", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic", response.getHits().getAt(0).getSource().get("eventuei"));
                //assertEquals("Event IP address did not match", "4.2.2.2", response.getHits().getAt(0).getSource().get("ipaddr"));
            } catch (Throwable e) {
                LOG.warn(e.getMessage(), e);
                return false;
            } finally {
                if (client != null) {
                    client.shutdownClient();
                }
            }
            return true;
        });
    }

    /**
     * Use a {@link DatagramChannel} to send a number of syslog messages to the Minion host.
     * 
     * @param host Hostname to inject into the syslog message
     * @param eventCount Number of messages to send
     * @throws IOException
     */
    protected void sendMessage(ContainerAlias alias, final String host, final int eventCount) throws IOException {
        final InetSocketAddress syslogAddr = testEnvironment.getServiceAddress(alias, 1514, "udp");

        List<Integer> randomNumbers = new ArrayList<>();

        for (int i = 0; i < eventCount; i++) {
            int eventNum = Double.valueOf(Math.random() * 10000).intValue();
            randomNumbers.add(eventNum);
        }

        String message = "<190>Mar 11 08:35:17 " + host + " 30128311: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), " + ORDINAL.getAndIncrement() + " packet\n";

        Set<Integer> sendSizes = new HashSet<>();

        // Test by sending over an IPv4 NIO channel
        try (final DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET)) {

            // Set the socket send buffer to the maximum value allowed by the kernel
            channel.setOption(StandardSocketOptions.SO_SNDBUF, Integer.MAX_VALUE);
            if (LOG.isTraceEnabled()) {
                LOG.trace("Actual send buffer size: " + channel.getOption(StandardSocketOptions.SO_SNDBUF));
            }
            channel.connect(syslogAddr);

            final ByteBuffer buffer = ByteBuffer.allocate(4096);
            buffer.clear();

            for (int i = 0; i < eventCount; i++) {
                buffer.put(message.getBytes());
                buffer.flip();
                final int sent = channel.send(buffer, syslogAddr);
                sendSizes.add(sent);
                buffer.clear();
            }
        }

        if (LOG.isTraceEnabled()) {
            LOG.info("sendSizes: " + sendSizes.toString());
        }
    }
}
