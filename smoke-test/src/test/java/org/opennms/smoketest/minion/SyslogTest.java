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
import static org.junit.Assert.assertTrue;

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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
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
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.smoketest.utils.RestClient;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.RateLimiter;

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
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all()
                // Add another Minion in a different location
                .minionWithOtherLocation()
                // Also enable Kafka and Elasticsearch
                .kafka().es2();
            builder.withOpenNMSEnvironment()
                    // Set logging to INFO level
                    .addFile(SyslogTest.class.getResource("/log4j2-info.xml"), "etc/log4j2.xml")
                    .addFile(SyslogTest.class.getResource("/eventconf.xml"), "etc/eventconf.xml")
                    .addFile(SyslogTest.class.getResource("/events/Cisco.syslog.events.xml"), "etc/events/Cisco.syslog.events.xml")
                    // Disable Alarmd
                    .addFile(SyslogTest.class.getResource("/service-configuration-disable-alarmd.xml"), "etc/service-configuration.xml")
                    .addFile(SyslogTest.class.getResource("/syslogd-configuration.xml"), "etc/syslogd-configuration.xml")
                    .addFile(SyslogTest.class.getResource("/syslog/Cisco.syslog.xml"), "etc/syslog/Cisco.syslog.xml")
                    .addFile(SyslogTest.class.getResource("/service-configuration.xml"), "etc/service-configuration.xml")
                    // Switch sink impl to Kafka using opennms-properties.d file
                    .addFile(SyslogTest.class.getResource("/opennms.properties.d/kafka-sink.properties"), "etc/opennms.properties.d/kafka-sink.properties");
            builder.withMinionEnvironment()
                    // Switch sink impl to Kafka using features.boot file
                    .addFile(SyslogTest.class.getResource("/featuresBoot.d/kafka.boot"), "etc/featuresBoot.d/kafka.boot");
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

    /**
     * @see https://issues.opennms.org/browse/NMS-8798
     * 
     * @throws Exception
     */
    @Test
    public void testAssociateSyslogsWithNodesWithOverlappingIpAddresses() throws Exception {
        final Date startOfTest = new Date();
        final String hostIpAddress = "1.2.3.4";

        // Create requisition with two node in different locations but same IP
        final RestClient client = new RestClient(minionSystem.getServiceAddress(ContainerAlias.OPENNMS, 8980));
        final Requisition requisition = new Requisition("overlapping");
        final RequisitionNode node1 = new RequisitionNode();
        node1.setNodeLabel("node_1");
        node1.setLocation("MINION");
        final RequisitionInterface interface1 = new RequisitionInterface();
        interface1.setIpAddr(hostIpAddress);
        interface1.setManaged(true);
        interface1.setSnmpPrimary(PrimaryType.PRIMARY);
        node1.setInterfaces(ImmutableList.of(interface1));
        node1.setForeignId("node_1");
        requisition.insertNode(node1);
        final RequisitionNode node2 = new RequisitionNode();
        node2.setNodeLabel("node_2");
        node2.setLocation("BANANA");
        final RequisitionInterface interface2 = new RequisitionInterface();
        interface2.setIpAddr(hostIpAddress);
        interface2.setManaged(true);
        interface2.setSnmpPrimary(PrimaryType.PRIMARY);
        node2.setInterfaces(ImmutableList.of(interface2));
        node2.setForeignId("node_2");
        requisition.insertNode(node2);
        client.addOrReplaceRequisition(requisition);
        client.importRequisition("overlapping");

        // Wait for the nodes to be provisioned
        final OnmsNode onmsNode1 = await()
                .atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(this.daoFactory.getDao(NodeDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsNode.class)
                                                             .eq("label", "node_1")
                                                             .toCriteria()),
                       notNullValue());
        final OnmsNode onmsNode2 = await()
                .atMost(1, MINUTES).pollInterval(5, SECONDS)
                .until(DaoUtils.findMatchingCallable(this.daoFactory.getDao(NodeDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsNode.class)
                                                             .eq("label", "node_2")
                                                             .toCriteria()),
                       notNullValue());

        // Sending syslog messages to each node and expect it to appear on the node
        sendMessage(ContainerAlias.MINION, hostIpAddress, 1);
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(this.daoFactory.getDao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                                             .ge("eventCreateTime", startOfTest)
                                                             .eq("node", onmsNode1)
                                                             .toCriteria()),
                      is(1));

        sendMessage(ContainerAlias.MINION_OTHER_LOCATION, hostIpAddress, 1);
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(this.daoFactory.getDao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                                             .ge("eventCreateTime", startOfTest)
                                                             .eq("node", onmsNode2)
                                                             .toCriteria()),
                      is(1));
    }

    @Test
    public void canReceiveSyslogMessages() throws Exception {
        final Date startOfTest = new Date();

        // Send a syslog packet to the Minion syslog listener
        SyslogTest.sendMessage(ContainerAlias.MINION, "myhost", 1);

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
        SyslogTest.sendMessage(ContainerAlias.MINION, sender, 1);

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
        SyslogTest.sendMessage(ContainerAlias.MINION, sender, 1);

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
     * This test will send syslog messages over the following message bus:
     * 
     * Minion -> Kafka -> OpenNMS Eventd -> Elasticsearch Forwarder -> Elasticsearch
     */
    @Test(timeout=1200000)
    public void testMinionSyslogsOverKafkaToEsForwarder() throws Exception {
        Date startOfTest = new Date();
        int numMessages = 10000;
        int packetsPerSecond = 500;

        InetSocketAddress minionSshAddr = minionSystem.getServiceAddress(ContainerAlias.MINION, 8201);
        InetSocketAddress esRestAddr = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), 9200);
        InetSocketAddress esTransportAddr = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), 9300);
        InetSocketAddress opennmsSshAddr = minionSystem.getServiceAddress(ContainerAlias.OPENNMS, 8101);
        InetSocketAddress kafkaAddress = minionSystem.getServiceAddress(ContainerAlias.KAFKA, 9092);
        InetSocketAddress zookeeperAddress = minionSystem.getServiceAddress(ContainerAlias.KAFKA, 2181);

        // Install the Kafka syslog and trap handlers on the Minion system
        installFeaturesOnMinion(minionSshAddr, kafkaAddress);

        // Install the Kafka and Elasticsearch features on the OpenNMS system
        installFeaturesOnOpenNMS(opennmsSshAddr, kafkaAddress, zookeeperAddress, false);

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

        // Create the indices manually. If we don't do this, some versions of 
        // ES will drop messages while the index is being auto-created.
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(startOfTest);
        calendar.set(Calendar.MONTH, Calendar.MARCH);

        createElasticsearchIndex(esTransportAddr, startOfTest);
        createElasticsearchIndex(esTransportAddr, calendar.getTime());

        LOG.info("Warming up syslog routes by sending 100 packets");

        // Warm up the routes
        SyslogTest.sendMessage(ContainerAlias.MINION, sender, 100);

        for (int i = 0; i < 10; i++) {
            LOG.info("Slept for " + i + " seconds");
            Thread.sleep(1000);
        }

        LOG.info("Resetting statistics");
        resetRouteStatistics(opennmsSshAddr, minionSshAddr);

        for (int i = 0; i < 20; i++) {
            LOG.info("Slept for " + i + " seconds");
            Thread.sleep(1000);
        }

        // Make sure that this evenly divides into the numMessages
        final int chunk = 500;
        // Make sure that this is an even multiple of chunk
        final int logEvery = 1000;

        int count = 0;
        long start = System.currentTimeMillis();

        // Send ${numMessages} syslog messages
        RateLimiter limiter = RateLimiter.create(packetsPerSecond);
        for (int i = 0; i < (numMessages / chunk); i++) {
            limiter.acquire(chunk);
            SyslogTest.sendMessage(ContainerAlias.MINION, sender, chunk);
            count += chunk;
            if (count % logEvery == 0) {
                long mid = System.currentTimeMillis();
                LOG.info(String.format("Sent %d packets in %d milliseconds", logEvery, mid - start));
                start = System.currentTimeMillis();
            }
        }

        // 100 warm-up messages plus ${numMessages} messages
        pollForElasticsearchEventsUsingJest(esRestAddr, 100 + numMessages);
    }

    /**
     * This test will send syslog messages over the following message bus:
     * 
     * Minion -> Kafka -> OpenNMS Eventd -> Elasticsearch REST -> Elasticsearch
     */
    @Test(timeout=600000)
    public void testMinionSyslogsOverKafkaToEsRest() throws Exception {
        Date startOfTest = new Date();
        int numMessages = 10000;
        int packetsPerSecond = 500;

        InetSocketAddress minionSshAddr = minionSystem.getServiceAddress(ContainerAlias.MINION, 8201);
        InetSocketAddress esRestAddr = minionSystem.getServiceAddress(ContainerAlias.ELASTICSEARCH_2, 9200);
        InetSocketAddress opennmsSshAddr = minionSystem.getServiceAddress(ContainerAlias.OPENNMS, 8101);
        InetSocketAddress kafkaAddress = minionSystem.getServiceAddress(ContainerAlias.KAFKA, 9092);
        InetSocketAddress zookeeperAddress = minionSystem.getServiceAddress(ContainerAlias.KAFKA, 2181);

        // Install the Kafka syslog and trap handlers on the Minion system
        installFeaturesOnMinion(minionSshAddr, kafkaAddress);

        // Install the Kafka and Elasticsearch features on the OpenNMS system
        installFeaturesOnOpenNMS(opennmsSshAddr, kafkaAddress, zookeeperAddress, true);

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
        SyslogTest.sendMessage(ContainerAlias.MINION, sender, 100);

        for (int i = 0; i < 10; i++) {
            LOG.info("Slept for " + i + " seconds");
            Thread.sleep(1000);
        }

        LOG.info("Resetting statistics");
        resetRouteStatistics(opennmsSshAddr, minionSshAddr);

        for (int i = 0; i < 20; i++) {
            LOG.info("Slept for " + i + " seconds");
            Thread.sleep(1000);
        }

        // Make sure that this evenly divides into the numMessages
        final int chunk = 500;
        // Make sure that this is an even multiple of chunk
        final int logEvery = 1000;

        int count = 0;
        long start = System.currentTimeMillis();

        // Send ${numMessages} syslog messages
        RateLimiter limiter = RateLimiter.create(packetsPerSecond);
        for (int i = 0; i < (numMessages / chunk); i++) {
            limiter.acquire(chunk);
            SyslogTest.sendMessage(ContainerAlias.MINION, sender, chunk);
            count += chunk;
            if (count % logEvery == 0) {
                long mid = System.currentTimeMillis();
                LOG.info(String.format("Sent %d packets in %d milliseconds", logEvery, mid - start));
                start = System.currentTimeMillis();
            }
        }

        // 100 warm-up messages plus ${numMessages} messages
        pollForElasticsearchEventsUsingJest(esRestAddr, 100 + numMessages);
    }

    /**
     * Install the Kafka features on Minion.
     * 
     * @param minionSshAddr
     * @param kafkaAddress
     * @throws Exception
     */
    private static void installFeaturesOnMinion(InetSocketAddress minionSshAddr, InetSocketAddress kafkaAddress) throws Exception {
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

    private static void installFeaturesOnOpenNMS(InetSocketAddress opennmsSshAddr, InetSocketAddress kafkaAddress, InetSocketAddress zookeeperAddress, boolean useEsRest) throws Exception {
        try (final SshClient sshClient = new SshClient(opennmsSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();

            if (useEsRest) {
                // Configure and install the Elasticsearch REST event forwarder
                pipe.println("config:edit org.opennms.plugin.elasticsearch.rest.forwarder");
                pipe.println("config:propset logAllEvents true");
                pipe.println("config:propset batchSize 500");
                pipe.println("config:propset batchInterval 500");
                pipe.println("config:update");
                pipe.println("features:install opennms-es-rest");
            } else {
                // Configure and install the Elasticsearch event forwarder
                pipe.println("config:edit org.opennms.features.elasticsearch.eventforwarder");
                // Set the IP address for Elasticsearch to the address of the Docker host
                pipe.println("config:propset elasticsearchIp " + InetAddress.getLocalHost().getHostAddress());
                pipe.println("config:propset logAllEvents true");
                pipe.println("config:update");
                pipe.println("features:install opennms-elasticsearch-event-forwarder");
            }

            pipe.println("features:list -i");
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

    private static void resetRouteStatistics(InetSocketAddress opennmsSshAddr, InetSocketAddress minionSshAddr) throws Exception {
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

    private static void createElasticsearchIndex(InetSocketAddress esTransportAddr, Date date) {
        Settings settings = ImmutableSettings.settingsBuilder()
            .put("cluster.name", "opennms").build();
        TransportClient esClient = new TransportClient(settings).addTransportAddress(new InetSocketTransportAddress(esTransportAddr));

        String indexName = new IndexNameFunction().apply("opennms", date);

        try {
            // Delete the index if it already exists
            boolean indexExists = esClient.admin().indices().prepareExists(indexName).execute().actionGet().isExists();
            if (indexExists) {
                esClient.admin().indices().prepareDelete(indexName).execute().actionGet();
            }
            esClient.admin().indices().prepareCreate(indexName).execute().actionGet();
        } catch (Throwable e) {
            LOG.warn("Error while trying to create index: " + indexName, e);
        } finally {
            esClient.close();
        }

        LOG.info("Finished creating index {}", new IndexNameFunction().apply("opennms", date));

        // Sleep to ensure that the index is fully operational
        try { Thread.sleep(1000); } catch (InterruptedException e) {}
    }

    private static void pollForElasticsearchEventsUsingJest(InetSocketAddress esTransportAddr, int numMessages) {
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(String.format("http://%s:%d", esTransportAddr.getHostString(), esTransportAddr.getPort()))
            .multiThreaded(true)
            .build());
        JestClient client = factory.getObject();

        try {
            with().pollInterval(15, SECONDS).await().atMost(5, MINUTES).until(() -> {
                try {
                    SearchResult response = client.execute(
                        new Search.Builder(new SearchSourceBuilder()
                            .query(QueryBuilders.matchQuery("eventuei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic"))
                            .toString()
                        )
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
                }
                return true;
            });
        } finally {
            client.shutdownClient();
        }
    }

    /**
     * Use a {@link DatagramChannel} to send a number of syslog messages to the Minion host.
     * 
     * @param host Hostname to inject into the syslog message
     * @param eventCount Number of messages to send
     * @throws IOException
     */
    private static void sendMessage(ContainerAlias alias, final String host, final int eventCount) throws IOException {
        final InetSocketAddress syslogAddr = minionSystem.getServiceAddress(alias, 1514, "udp");

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
