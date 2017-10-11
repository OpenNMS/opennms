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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.dao.hibernate.MinionDaoHibernate;
import org.opennms.netmgt.dao.hibernate.NodeDaoHibernate;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsMonitoringSystem;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.minion.OnmsMinion;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.test.system.api.AbstractTestEnvironment;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;

/**
 * This test starts up Minion with the default JMS sink and makes sure
 * that heartbeat messages continue to be processed even if the Minion
 * and OpenNMS instances are restarted.
 * 
 * @author Seth
 */
public class MinionHeartbeatOutageIT {
    @Rule
    public TestEnvironment testEnvironment = getTestEnvironment();

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    private HibernateDaoFactory m_daoFactory;

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

    /**
     * Override this method to customize the test environment.
     */
    protected TestEnvironmentBuilder getEnvironmentBuilder() {
        final TestEnvironmentBuilder builder = TestEnvironment.builder().all();
        OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
        return builder;
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
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
     * Install the Kafka features on Minion.
     * 
     * @param minionSshAddr
     * @param kafkaAddress
     * @throws Exception
     */
    protected static void installFeaturesOnMinion(InetSocketAddress minionSshAddr) throws Exception {
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
                getLogger().info("Karaf output:\n{}", sshClient.getStdout());
            }
        }
    }

    protected static void installFeaturesOnOpenNMS(InetSocketAddress opennmsSshAddr) throws Exception {
        try (final SshClient sshClient = new SshClient(opennmsSshAddr, "admin", "admin")) {
            PrintStream pipe = sshClient.openShell();

            pipe.println("feature:list -i");
            // Set the log level to INFO
            pipe.println("log:set INFO");//
            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                getLogger().info("Karaf output:\n{}", sshClient.getStdout());
            }
        }
    }

    @Test
    public void testHeartbeatOutages() throws Exception {
        Date startOfTest = new Date();

        InetSocketAddress minionSshAddr = testEnvironment.getServiceAddress(ContainerAlias.MINION, 8201);
        InetSocketAddress opennmsSshAddr = testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8101);

        installFeaturesOnMinion(minionSshAddr);

        installFeaturesOnOpenNMS(opennmsSshAddr);

        // Wait for the Minion to show up
        await().atMost(90, SECONDS).pollInterval(5, SECONDS)
            .until(DaoUtils.countMatchingCallable(
                 getDaoFactory().getDao(MinionDaoHibernate.class),
                 new CriteriaBuilder(OnmsMinion.class)
                     .gt("lastUpdated", startOfTest)
                     .eq("location", "MINION")
                     .toCriteria()
                 ),
                 is(1)
             );

        // Make sure that the node is available
        await().atMost(180, SECONDS).pollInterval(5, SECONDS)
            .until(DaoUtils.countMatchingCallable(
                getDaoFactory().getDao(NodeDaoHibernate.class),
                new CriteriaBuilder(OnmsNode.class)
                .eq("foreignSource", "Minions")
                .eq("foreignId", "00000000-0000-0000-0000-000000ddba11")
                .toCriteria()
                ),
            equalTo(1)
        );

        // Make sure that the expected events are present

        /*
        assertEquals(1, DaoUtils.countMatchingCallable(
            getDaoFactory().getDao(EventDaoHibernate.class),
            new CriteriaBuilder(OnmsEvent.class)
            .eq("eventUei", EventConstants.MONITORING_SYSTEM_ADDED_UEI)
            .like("eventParms", String.format("%%%s=%s%%", EventConstants.PARAM_MONITORING_SYSTEM_TYPE, OnmsMonitoringSystem.TYPE_MINION))
            .like("eventParms", String.format("%%%s=%s%%", EventConstants.PARAM_MONITORING_SYSTEM_ID, "00000000-0000-0000-0000-000000ddba11"))
            .like("eventParms", String.format("%%%s=%s%%", EventConstants.PARAM_MONITORING_SYSTEM_LOCATION, "MINION"))
            .toCriteria()
            ).call().intValue()
        );
        */

        assertEquals(1,
            getDaoFactory().getDao(EventDaoHibernate.class).findMatching(
                new CriteriaBuilder(OnmsEvent.class)
                    .alias("eventParameters", "eventParameters")
                    .eq("eventUei", EventConstants.MONITORING_SYSTEM_ADDED_UEI).toCriteria()).stream()
                    .filter(e -> e.getEventParameters().stream()
                            .anyMatch(p -> EventConstants.PARAM_MONITORING_SYSTEM_TYPE.equals(p.getName()) && OnmsMonitoringSystem.TYPE_MINION.equals(p.getValue())))
                    .filter(e -> e.getEventParameters().stream()
                            .anyMatch(p -> EventConstants.PARAM_MONITORING_SYSTEM_ID.equals(p.getName()) && "00000000-0000-0000-0000-000000ddba11".equals(p.getValue())))
                    .filter(e -> e.getEventParameters().stream()
                            .anyMatch(p -> EventConstants.PARAM_MONITORING_SYSTEM_LOCATION.equals(p.getName()) && "MINION".equals(p.getValue())))
                    .distinct()
                    .count()
        );

        assertEquals(0, DaoUtils.countMatchingCallable(
            getDaoFactory().getDao(EventDaoHibernate.class),
            new CriteriaBuilder(OnmsEvent.class)
            .eq("eventUei", EventConstants.MONITORING_SYSTEM_LOCATION_CHANGED_UEI)
            .toCriteria()
            ).call().intValue()
        );

        for (int i = 0; i < 3; i++) {
            restartContainer(ContainerAlias.MINION);

            // Reset the startOfTest timestamp
            startOfTest = new Date();

            await().atMost(90, SECONDS).pollInterval(5, SECONDS)
                .until(DaoUtils.countMatchingCallable(
                     getDaoFactory().getDao(MinionDaoHibernate.class),
                     new CriteriaBuilder(OnmsMinion.class)
                         .gt("lastUpdated", startOfTest)
                         .eq("location", "MINION")
                         .toCriteria()
                     ),
                     is(1)
                 );
        }

        for (int i = 0; i < 2; i++) {
            restartContainer(ContainerAlias.OPENNMS);

            // Reset the startOfTest timestamp
            startOfTest = new Date();

            await().atMost(240, SECONDS).pollInterval(5, SECONDS)
                .until(DaoUtils.countMatchingCallable(
                     getDaoFactory().getDao(MinionDaoHibernate.class),
                     new CriteriaBuilder(OnmsMinion.class)
                         .gt("lastUpdated", startOfTest)
                         .eq("location", "MINION")
                         .toCriteria()
                     ),
                     is(1)
                 );
        }

        for (int i = 0; i < 1; i++) {
            restartContainer(ContainerAlias.MINION);

            // Reset the startOfTest timestamp
            startOfTest = new Date();

            await().atMost(90, SECONDS).pollInterval(5, SECONDS)
                .until(DaoUtils.countMatchingCallable(
                     getDaoFactory().getDao(MinionDaoHibernate.class),
                     new CriteriaBuilder(OnmsMinion.class)
                         .gt("lastUpdated", startOfTest)
                         .eq("location", "MINION")
                         .toCriteria()
                     ),
                     is(1)
                 );
        }
    }

    private void restartContainer(ContainerAlias alias) {
        final DockerClient docker = ((AbstractTestEnvironment)testEnvironment).getDockerClient();
        final String id = testEnvironment.getContainerInfo(alias).id();
        final Logger logger = getLogger();
        try {
            logger.info("Restarting container: {} -> {}", alias, id);
            docker.restartContainer(id);
            logger.info("Container restarted: {} -> {}", alias, id);
        } catch (DockerException | InterruptedException e) {
            logger.warn("Unexpected exception while restarting container {}", id, e);
        }
    }

    protected static Logger getLogger() {
        return LoggerFactory.getLogger(MinionHeartbeatOutageIT.class);
    }
}
