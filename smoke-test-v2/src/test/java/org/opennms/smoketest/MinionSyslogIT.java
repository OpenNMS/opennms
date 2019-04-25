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
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.greaterThan;
import static org.testcontainers.containers.PostgreSQLContainer.POSTGRESQL_PORT;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.smoketest.containers.MinionContainer;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.containers.PostgreSQLContainer;
import org.opennms.smoketest.utils.DaoUtils;
import org.opennms.smoketest.utils.HibernateDaoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Verify that we can send a Syslog message to a Minion
 * and find the corresponding event in the database.
 */
public class MinionSyslogIT {

    private static final Logger LOG = LoggerFactory.getLogger(MinionSyslogIT.class);

    private static final PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer();

    private static final OpenNMSContainer opennmsContainer = new OpenNMSContainer();

    private static final MinionContainer minionContainer = new MinionContainer();

    private static final AtomicInteger ORDINAL = new AtomicInteger();

    private static HibernateDaoFactory daoFactory;

    @ClassRule
    public static TestRule chain = RuleChain
            .outerRule(postgreSQLContainer)
            .around(opennmsContainer)
            .around(minionContainer);

    @Test
    public void canReceiveSyslogMessages() throws Exception {
        final Date startOfTest = new Date();

        // Send a syslog packet(s) to the Minion syslog listener
        sendMessages(minionContainer, "myhost", 10);

        // Parsing the message correctly relies on the customized syslogd-configuration.xml that is part of the OpenNMS image
        final EventDao eventDao = getDaoFactory().getDao(EventDaoHibernate.class);
        final Criteria criteria = new CriteriaBuilder(OnmsEvent.class)
                .eq("eventUei", "uei.opennms.org/vendor/cisco/syslog/SEC-6-IPACCESSLOGP/aclDeniedIPTraffic")
                // eventCreateTime is the storage time of the event in the database so
                // it should be after the start of this test
                .ge("eventCreateTime", startOfTest)
                .toCriteria();

        await().atMost(1, MINUTES).pollInterval(5, SECONDS).until(DaoUtils.countMatchingCallable(eventDao, criteria), greaterThan(0));
    }

    protected HibernateDaoFactory getDaoFactory() {
        if (daoFactory == null) {
            // Connect to the postgresql container
            final InetSocketAddress pgsql = new InetSocketAddress(postgreSQLContainer.getContainerIpAddress(),
                    postgreSQLContainer.getMappedPort(POSTGRESQL_PORT));
            daoFactory = new HibernateDaoFactory(pgsql);
        }
        return daoFactory;
    }

    /**
     * Use a {@link DatagramChannel} to send a number of syslog messages to the Minion host.
     *
     * @param host Hostname to inject into the syslog message
     * @param eventCount Number of messages to send
     * @throws IOException
     */
    protected void sendMessages(MinionContainer container, final String host, final int eventCount) throws IOException {
        final InetSocketAddress syslogAddr = container.getSyslogAddress();

        String message = "<190>Mar 11 08:35:17 " + host + " 30128311: Mar 11 08:35:16.844 CST: %SEC-6-IPACCESSLOGP: list in110 denied tcp 192.168.10.100(63923) -> 192.168.11.128(1521), " + ORDINAL.getAndIncrement() + " packet\n";

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
                channel.send(buffer, syslogAddr);
                buffer.clear();
            }
        } catch (IOException e) {
            LOG.error("Send failed with: {}", e.getMessage(), e);
            throw new IOException(e);
        }
    }

}
