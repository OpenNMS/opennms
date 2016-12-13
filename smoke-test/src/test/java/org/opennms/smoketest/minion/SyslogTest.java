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
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.camel.IndexNameFunction;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.test.elasticsearch.JUnitElasticsearchServer;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MinionDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MonitoredServiceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.RateLimiter;

/**
 * Verifies that syslog messages sent to the Minion generate
 * events in OpenNMS.
 *
 * @author Seth
 * @author jwhite
 */
public class SyslogTest {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogTest.class);

    private static TestEnvironment minionSystem;

    private HibernateDaoFactory daoFactory;

    private static final AtomicInteger ORDINAL = new AtomicInteger();

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all();
            builder.kafka();
            builder.withOpenNMSEnvironment()
                    // Set logging to INFO level
                    .addFile(SyslogTest.class.getResource("/log4j2-info.xml"), "etc/log4j2.xml")
                    .addFile(SyslogTest.class.getResource("/eventconf.xml"), "etc/eventconf.xml")
                    .addFile(SyslogTest.class.getResource("/events/Cisco.syslog.events.xml"), "etc/events/Cisco.syslog.events.xml")
                    .addFile(SyslogTest.class.getResource("/syslogd-configuration.xml"), "etc/syslogd-configuration.xml")
                    .addFile(SyslogTest.class.getResource("/syslog/Cisco.syslog.xml"), "etc/syslog/Cisco.syslog.xml")
                    .addFile(SyslogTest.class.getResource("/service-configuration.xml"), "etc/service-configuration.xml");
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            minionSystem = builder.build();
            return minionSystem;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @ClassRule
    public static final JUnitElasticsearchServer ELASTICSEARCH = new JUnitElasticsearchServer();

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    @Before
    public void setup() {
        // Connect to the postgresql container
        final InetSocketAddress pgsql = minionSystem.getServiceAddress(ContainerAlias.POSTGRES, 5432);
        this.daoFactory = new HibernateDaoFactory(pgsql);
    }

    @Test
    public void canReceiveSyslogMessages() throws Exception {
        final Date startOfTest = new Date();

        // Send a syslog packet to the Minion syslog listener
        SyslogTest.sendMessage("myhost", 1);

        // Parsing the message correctly relies on the customized syslogd-configuration.xml that is part of the OpenNMS image
        final EventDao eventDao = this.daoFactory.getDao(EventDaoHibernate.class);
        final Criteria criteria = new CriteriaBuilder(OnmsEvent.class)
                .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                // eventCreateTime is the storage time of the event in the database so 
                // it should be after the start of this test
                .ge("eventCreateTime", startOfTest)
                .toCriteria();

        await().atMost(1, MINUTES).pollInterval(5, SECONDS).until(DaoUtils.countMatchingCallable(eventDao, criteria), greaterThan(0));
    }

    @Test
    public void testNewSuspect() throws Exception {
        final Date startOfTest = new Date();

        final String sender = minionSystem.getContainerInfo(ContainerAlias.SNMPD).networkSettings().ipAddress();

        // Wait for the minion to show up
        await().atMost(90, SECONDS).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(this.daoFactory.getDao(MinionDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsMinion.class)
                                                             .gt("lastUpdated", startOfTest)
                                                             .eq("location", "MINION")
                                                             .toCriteria()),
                      is(1));

        // Send the initial message
        SyslogTest.sendMessage(sender, 1);

        // Wait for the syslog message
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(this.daoFactory.getDao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                                             .ge("eventCreateTime", startOfTest)
                                                             .toCriteria()),
                      is(1));

        //Wait for the new suspect
        final OnmsEvent event = await()
                .atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(this.daoFactory.getDao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/internal/discovery/newSuspect")
                                                             .ge("eventTime", startOfTest)
                                                             .eq("ipAddr", Inet4Address.getByName(sender))
                                                             .isNull("node")
                                                             .toCriteria()),
                       notNullValue());
        assertThat(event.getDistPoller().getLocation(), is("MINION"));

        // Check if the node was detected
        final OnmsNode node = await()
                .atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(this.daoFactory.getDao(NodeDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsNode.class)
                                                             .eq("label", "snmpd")
                                                             .toCriteria()),
                       notNullValue());
        assertThat(node.getLocation().getLocationName(), is("MINION"));

        // Check if the service was discovered
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(() -> this.daoFactory.getDao(MonitoredServiceDaoHibernate.class)
                                           .getPrimaryService(node.getId(), "SNMP"),
                      notNullValue());

        // Send the second message
        SyslogTest.sendMessage(sender, 1);

        // Wait for the second message with the node assigned
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(this.daoFactory.getDao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                                             .ge("eventCreateTime", startOfTest)
                                                             .eq("node", node)
                                                             .toCriteria()),
                      is(1));
    }

    /**
     * This test will send syslog messages and verify that they have been processed into
     * Elasticsearch records.
     */
    @Test
    @Ignore
    public void testElasticsearchSyslogs() throws Exception {
        Date startOfTest = new Date();
        int numMessages = 1000000;
        int packetsPerSecond = 9000;

        // Install the Kafka syslog and trap handlers on the Minion system
        InetSocketAddress minionSshAddr = minionSystem.getServiceAddress(ContainerAlias.MINION, 8201);
        InetSocketAddress opennmsSshAddr = minionSystem.getServiceAddress(ContainerAlias.OPENNMS, 8101);
        InetSocketAddress kafkaAddress = minionSystem.getServiceAddress(ContainerAlias.KAFKA, 9092);
        InetSocketAddress zookeeperAddress = minionSystem.getServiceAddress(ContainerAlias.KAFKA, 2181);

        try (final SshClient sshClient = new SshClient(minionSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            pipe.println("config:edit org.opennms.netmgt.syslog.handler.kafka");
            // Set the IP address for Kafka to the address of the Docker host
            pipe.println("config:property-set kafkaAddress " + InetAddress.getLocalHost().getHostAddress() + ":" + kafkaAddress.getPort());
            pipe.println("config:update");
            // Uninstall all of the syslog and trap features with ActiveMQ handlers
            pipe.println("feature:uninstall -v opennms-syslogd-listener-camel-netty opennms-trapd-listener opennms-syslogd-handler-minion opennms-trapd-handler-minion");
            // Reinstall all of the syslog and trap features with Kafka handlers
            pipe.println("feature:install -v opennms-syslogd-listener-camel-netty opennms-trapd-listener opennms-syslogd-handler-kafka opennms-trapd-handler-kafka");
            pipe.println("feature:list -i");
            pipe.println("list");
            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }

        try (final SshClient sshClient = new SshClient(opennmsSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();
            // Configure and install the Elasticsearch event forwarder
            pipe.println("config:edit org.opennms.features.elasticsearch.eventforwarder");
            // Set the IP address for Elasticsearch to the address of the Docker host
            pipe.println("config:propset elasticsearchIp " + InetAddress.getLocalHost().getHostAddress());
            pipe.println("config:update");
            pipe.println("features:install opennms-elasticsearch-event-forwarder");
            pipe.println("features:list -i");

            // Configure and install the  Kafka syslog and trap handlers on the OpenNMS system
            pipe.println("config:edit org.opennms.netmgt.syslog.handler.kafka.default");
            // Set the IP address for Kafka to the address of the Docker host
            pipe.println("config:propset kafkaAddress " + InetAddress.getLocalHost().getHostAddress() + ":" + kafkaAddress.getPort());
            // Set the IP address for Zookeeper to the address of the Docker host
            pipe.println("config:propset zookeeperhost " + InetAddress.getLocalHost().getHostAddress());
            pipe.println("config:propset zookeeperport " + zookeeperAddress.getPort());
            pipe.println("config:propset consumerstreams " + 1);
            pipe.println("config:update");
            pipe.println("features:install opennms-syslogd-handler-kafka-default opennms-trapd-handler-kafka-default");
            pipe.println("features:list -i");
            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }

        final String sender = minionSystem.getContainerInfo(ContainerAlias.SNMPD).networkSettings().ipAddress();

        // Wait for the minion to show up
        await().atMost(90, SECONDS).pollInterval(5, SECONDS)
            .until(DaoUtils.countMatchingCallable(
                 this.daoFactory.getDao(MinionDaoHibernate.class),
                 new CriteriaBuilder(OnmsMinion.class)
                     .gt("lastUpdated", startOfTest)
                     .eq("location", "MINION")
                     .toCriteria()
                 ),
                 is(1)
             );

        LOG.info("Warming up syslog routes by sending 100 packets");

        // Warm up the routes
        SyslogTest.sendMessage(sender, 100);

        for (int i = 0; i < 10; i++) {
            LOG.info("Slept for " + i + " seconds");
            Thread.sleep(1000);
        }

        LOG.info("Resetting statistics");

        // Reset route statistics on Minion
        try (final SshClient sshClient = new SshClient(minionSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();

            // Syslog sender
            pipe.println("camel:route-reset-stats syslogListen");
            pipe.println("camel:route-reset-stats syslogMarshal");
            pipe.println("camel:route-reset-stats syslogSendKafka");

            // Trap sender
            pipe.println("camel:route-reset-stats trapMarshal");
            pipe.println("camel:route-reset-stats trapSendKafka");

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

            // Syslog receiver
            pipe.println("camel:route-reset-stats receiveSyslogConnectionViaKafka");
            pipe.println("camel:route-reset-stats syslogHandler");

            // Trap receiver
            pipe.println("camel:route-reset-stats receiveTrapConnectionViaKafka");
            pipe.println("camel:route-reset-stats trapHandler");

            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }

        for (int i = 0; i < 30; i++) {
            LOG.info("Slept for " + i + " seconds");
            Thread.sleep(1000);
        }

        // Make sure that this evenly divides into the numMessages
        int chunk = 500;
        int count = 0;
        long start = System.currentTimeMillis();
        // Send ${numMessages} syslog messages
        RateLimiter limiter = RateLimiter.create(packetsPerSecond);
        for (int i = 0; i < (numMessages / chunk); i++) {
            limiter.acquire(chunk);
            SyslogTest.sendMessage(sender, chunk);
            count += chunk;
            if (count % 1000 == 0) {
                long mid = System.currentTimeMillis();
                LOG.info(String.format("Sent %d packets in %d milliseconds", 1000, mid - start));
                start = System.currentTimeMillis();
            }
        }

        Thread.sleep(3600000);

        with().pollInterval(1, SECONDS).await().atMost(30, SECONDS).until(() -> {
            try {
                // Refresh the "opennms-2011.01" index
                ELASTICSEARCH.getClient().admin().indices().prepareRefresh(new IndexNameFunction().apply("opennms", startOfTest)).execute().actionGet();

                // Search for all entries in the index
                SearchResponse response = ELASTICSEARCH.getClient()
                    // Search the index that the event above created
                    .prepareSearch(new IndexNameFunction().apply("opennms", startOfTest)) // opennms-2011.01
                    .setQuery(QueryBuilders.termQuery("eventuei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic"))
                    .execute()
                    .actionGet();

                LOG.debug("RESPONSE: {}", response.toString());

                assertEquals("ES search hits was not equal to " + numMessages, numMessages, response.getHits().totalHits());
                assertEquals("Event UEI did not match", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic", response.getHits().getAt(0).getSource().get("eventuei"));
                //assertEquals("Event IP address did not match", "4.2.2.2", response.getHits().getAt(0).getSource().get("ipaddr"));
            } catch (Throwable e) {
                LOG.warn(e.getMessage(), e);
                return false;
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
    private static void sendMessage(final String host, final int eventCount) throws IOException {
        final InetSocketAddress syslogAddr = minionSystem.getServiceAddress(ContainerAlias.MINION, 1514, "udp");

        List<Integer> randomNumbers = new ArrayList<Integer>();

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
