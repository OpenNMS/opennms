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
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.util.Date;

import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MinionDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MonitoredServiceDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoredService;
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

import com.google.common.collect.Iterables;

/**
 * Verifies that syslog messages sent to the Minion generate
 * events in OpenNMS.
 *
 * @author jwhite
 */
public class SyslogTest {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogTest.class);

    private static TestEnvironment minionSystem;

    private HibernateDaoFactory daoFactory;

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all();
            builder.withOpenNMSEnvironment()
                    .addFile(SyslogTest.class.getResource("/eventconf.xml"), "etc/eventconf.xml")
                    .addFile(SyslogTest.class.getResource("/events/Cisco.syslog.events.xml"), "etc/events/Cisco.syslog.events.xml")
                    .addFile(SyslogTest.class.getResource("/syslogd-configuration.xml"), "etc/syslogd-configuration.xml")
                    .addFile(SyslogTest.class.getResource("/syslog/Cisco.syslog.xml"), "etc/syslog/Cisco.syslog.xml");
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            minionSystem = builder.build();
            return minionSystem;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    @Before
    public void enableSyslog() throws Exception {
        // Install the handler on the OpenNMS system (this should probably be installed by default)
        final InetSocketAddress sshAddr = minionSystem.getServiceAddress(ContainerAlias.OPENNMS, 8101);
        try (
                final SshClient sshClient = new SshClient(sshAddr, "admin", "admin");
        ) {
            PrintStream pipe = sshClient.openShell();
            // Install the syslog and trap handler features
            pipe.println("features:install opennms-syslogd-handler-default opennms-trapd-handler-default");
            pipe.println("features:list -i");
            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }
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
        this.sendMessage("myhost");

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
    @Ignore
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
        this.sendMessage(sender);

        // Wait for the syslog message
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(this.daoFactory.getDao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                                             .ge("eventTime", startOfTest)
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
        final OnmsNode node = Iterables.getOnlyElement(this.daoFactory.getDao(NodeDaoHibernate.class)
                                                                      .findMatching(new CriteriaBuilder(OnmsNode.class)
                                                                                            .eq("label", "snmpd")
                                                                                            .toCriteria()));
        assertThat(node, notNullValue());
        assertThat(node.getLocation().getLocationName(), is("MINION"));

        // Check if the service was decovered
        final OnmsMonitoredService service = this.daoFactory.getDao(MonitoredServiceDaoHibernate.class).getPrimaryService(node.getId(), "SNMP");
        assertThat(service, notNullValue());

        // Send the second message
        this.sendMessage(sender);

        // Wait for the second message with the node assigned
        await().atMost(1, MINUTES).pollInterval(5, SECONDS)
               .until(DaoUtils.countMatchingCallable(this.daoFactory.getDao(EventDaoHibernate.class),
                                                     new CriteriaBuilder(OnmsEvent.class)
                                                             .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                                                             .ge("eventTime", startOfTest)
                                                             .eq("node", node)
                                                             .toCriteria()),
                      is(1));
    }

    private void sendMessage(final String host) throws IOException {
        final InetSocketAddress syslogAddr = minionSystem.getServiceAddress(ContainerAlias.MINION, 1514, "udp");
        byte[] message = ("<190>Mar 11 08:35:17 " + host + " 30128311: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), 1 packet\n").getBytes();
        DatagramPacket packet = new DatagramPacket(message, message.length, syslogAddr.getAddress(), syslogAddr.getPort());
        DatagramSocket dsocket = new DatagramSocket();
        dsocket.send(packet);
        dsocket.close();
    }
}
