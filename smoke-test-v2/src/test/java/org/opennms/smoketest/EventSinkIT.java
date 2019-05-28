/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertTrue;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Date;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.smoketest.containers.MinionContainer;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.containers.PostgreSQLContainer;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventSinkIT {

    private static final Logger LOG = LoggerFactory.getLogger(EventSinkIT.class);

    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static final OpenNMSContainer opennmsContainer = new OpenNMSContainer();

    private static final MinionContainer minionContainer = new MinionContainer();

    private static HibernateDaoFactory daoFactory;

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(postgreSQLContainer)
            .around(opennmsContainer)
            .around(minionContainer);

    protected HibernateDaoFactory getDaoFactory() {
        if (daoFactory == null) {
            // Connect to the postgresql container
            final InetSocketAddress pgsql = new InetSocketAddress(postgreSQLContainer.getContainerIpAddress(),
                    postgreSQLContainer.getMappedPort(POSTGRESQL_PORT));
            daoFactory = new HibernateDaoFactory(pgsql);
        }
        return daoFactory;
    }


    @Test
    public void canReceiveEvents() throws Exception {
        Date startOfTest = new Date();
        final InetSocketAddress sshAddr = minionContainer.getSshAddress();
        assertTrue("failed to send event from minion", sendEventsFromMinion(sshAddr));

        EventDao eventDao = getDaoFactory().getDao(EventDaoHibernate.class);
        final OnmsEvent onmsEvent = await().atMost(2, MINUTES).pollInterval(10, SECONDS)
                .until(DaoUtils.findMatchingCallable(eventDao, new CriteriaBuilder(OnmsEvent.class).eq("eventUei", "uei.opennms.org/alarms/trigger")
                        .eq("eventSource", "karaf-shell").ge("eventCreateTime", startOfTest).toCriteria()), notNullValue());

        assertNotNull(onmsEvent);
    }

    private boolean sendEventsFromMinion(InetSocketAddress sshAddr) {
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {
            // Issue events:send command
            PrintStream pipe = sshClient.openShell();
            pipe.println("events:send -u 'uei.opennms.org/alarms/trigger'");
            pipe.println("logout");

            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());
            // Grab the output
            String shellOutput = sshClient.getStdout();
            LOG.info("events:send output: {}", shellOutput);
            // Verify
            return shellOutput.contains("sent");
        } catch (Exception e) {
            LOG.error("Failed to send event from Minion", e);
        }
        return false;
    }
}
