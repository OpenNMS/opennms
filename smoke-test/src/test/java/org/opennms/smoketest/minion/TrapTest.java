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
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.Callable;

import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.dao.hibernate.EventDaoHibernate;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
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

/**
 * Verifies that SNMP traps sent to the Minion generate
 * events in OpenNMS.
 *
 * @author seth
 */
public class TrapTest {
    private static final Logger LOG = LoggerFactory.getLogger(TrapTest.class);

    private static TestEnvironment minionSystem;

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

    @Test
    public void canReceiveTraps() throws Exception {
        Date startOfTest = new Date();

        // Install the handler on the OpenNMS system (this should probably be installed by default)
        final InetSocketAddress sshAddr = minionSystem.getServiceAddress(ContainerAlias.OPENNMS, 8101);
        try (
            final SshClient sshClient = new SshClient(sshAddr, "admin", "admin");
        ) {
            PrintStream pipe = sshClient.openShell();
            // Install the syslog and trap handler features
            pipe.println("features:install opennms-syslogd-handler-default opennms-trapd-handler-default");
            pipe.println("features:list -i");
            pipe.println("list");
            pipe.println("logout");
            try {
                await().atMost(2, MINUTES).until(sshClient.isShellClosedCallable());
            } finally {
                LOG.info("Karaf output:\n{}", sshClient.getStdout());
            }
        }

        final InetSocketAddress trapAddr = minionSystem.getServiceAddress(ContainerAlias.MINION, 162, "udp");

        // Connect to the postgresql container
        InetSocketAddress pgsql = minionSystem.getServiceAddress(ContainerAlias.POSTGRES, 5432);
        HibernateDaoFactory daoFactory = new HibernateDaoFactory(pgsql);
        EventDao eventDao = daoFactory.getDao(EventDaoHibernate.class);

        // Parsing the message correctly relies on the customized syslogd-configuration.xml that is part of the OpenNMS image
        Criteria criteria = new CriteriaBuilder(OnmsEvent.class)
                .eq("eventUei", "uei.opennms.org/generic/traps/SNMP_Warm_Start")
                .ge("eventTime", startOfTest)
                .toCriteria();

        // Send traps to the Minion listener until one makes it through
        await().atMost(5, MINUTES).pollInterval(30, SECONDS).pollDelay(0, SECONDS).until(new Callable<Boolean>() {
            @Override public Boolean call() throws Exception {
                sendTrap(trapAddr);
                try {
                    await().atMost(30, SECONDS).pollInterval(5, SECONDS).until(DaoUtils.countMatchingCallable(eventDao, criteria), greaterThanOrEqualTo(1));
                } catch (final Exception e) {
                    return false;
                }
                return true;
            }
        });
    }

    private void sendTrap(final InetSocketAddress trapAddr) {
        LOG.info("Sending trap");
        try {
            SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
            pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"), SnmpUtils.getValueFactory().getTimeTicks(0));
            // warmStart
            pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"), SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(".1.3.6.1.6.3.1.1.5.2")));
            pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"), SnmpUtils.getValueFactory().getObjectId(SnmpObjId.get(".1.3.6.1.4.1.5813")));
            pdu.send(InetAddressUtils.str(trapAddr.getAddress()), trapAddr.getPort(), "public");
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
        }
        LOG.info("Trap has been sent");
    }
}
