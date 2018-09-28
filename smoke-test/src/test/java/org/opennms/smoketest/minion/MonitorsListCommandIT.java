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
import static org.hamcrest.Matchers.hasSize;

import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.NullTestEnvironment;
import org.opennms.smoketest.OpenNMSSeleniumTestCase;
import org.opennms.test.system.api.TestEnvironment;
import org.opennms.test.system.api.TestEnvironmentBuilder;
import org.opennms.test.system.api.NewTestEnvironment.ContainerAlias;
import org.opennms.test.system.api.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

public class MonitorsListCommandIT {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorsListCommandIT.class);

    private static TestEnvironment m_testEnvironment;

    private ImmutableSet<String> expectedMonitors = ImmutableSet.<String> builder().add(
            "org.opennms.netmgt.poller.monitors.DNSResolutionMonitor",
            "org.opennms.netmgt.poller.monitors.JDBCQueryMonitor", 
            "org.opennms.netmgt.poller.monitors.DskTableMonitor",
            "org.opennms.netmgt.poller.monitors.SmbMonitor", 
            "org.opennms.netmgt.poller.monitors.HttpMonitor",
            "org.opennms.netmgt.poller.monitors.PercMonitor", 
            "org.opennms.netmgt.poller.monitors.HttpsMonitor",
            "org.opennms.netmgt.poller.monitors.SSLCertMonitor",
            "org.opennms.netmgt.poller.monitors.MailTransportMonitor",
            "org.opennms.netmgt.poller.monitors.OpenManageChassisMonitor",
            "org.opennms.netmgt.poller.monitors.BgpSessionMonitor",
            "org.opennms.netmgt.poller.monitors.TcpMonitor",
            "org.opennms.netmgt.poller.monitors.MemcachedMonitor",
            "org.opennms.netmgt.poller.monitors.ImapMonitor",
            "org.opennms.netmgt.poller.monitors.SshMonitor", 
            "org.opennms.netmgt.poller.monitors.PageSequenceMonitor",
            "org.opennms.netmgt.poller.monitors.LaTableMonitor", 
            "org.opennms.netmgt.poller.monitors.HttpPostMonitor",
            "org.opennms.netmgt.poller.monitors.Jsr160Monitor",
            "org.opennms.netmgt.poller.monitors.LdapsMonitor",
            "org.opennms.netmgt.poller.monitors.ImapsMonitor", 
            "org.opennms.netmgt.poller.monitors.DiskUsageMonitor",
            "org.opennms.netmgt.poller.monitors.SystemExecuteMonitor",
            "org.opennms.netmgt.poller.monitors.CitrixMonitor", 
            "org.opennms.netmgt.poller.monitors.SmtpMonitor",
            "org.opennms.netmgt.poller.monitors.TrivialTimeMonitor",
            "org.opennms.netmgt.poller.monitors.JolokiaBeanMonitor",
            "org.opennms.netmgt.poller.monitors.LoopMonitor", 
            "org.opennms.netmgt.poller.monitors.FtpMonitor",
            "org.opennms.netmgt.poller.monitors.NrpeMonitor", 
            "org.opennms.netmgt.poller.monitors.AvailabilityMonitor",
            "org.opennms.netmgt.poller.monitors.LogMatchTableMonitor",
            "org.opennms.netmgt.poller.monitors.Win32ServiceMonitor", 
            "org.opennms.netmgt.poller.monitors.NtpMonitor",
            "org.opennms.netmgt.poller.monitors.CiscoPingMibMonitor",
            "org.opennms.netmgt.poller.monitors.StrafePingMonitor", 
            "org.opennms.netmgt.poller.monitors.LdapMonitor",
            "org.opennms.netmgt.poller.monitors.Pop3Monitor", 
            "org.opennms.netmgt.poller.monitors.DominoIIOPMonitor",
            "org.opennms.netmgt.poller.monitors.DnsMonitor", 
            "org.opennms.netmgt.poller.monitors.SnmpMonitor",
            "org.opennms.netmgt.poller.monitors.PrTableMonitor",
            "org.opennms.netmgt.poller.monitors.IcmpMonitor",
            "org.opennms.netmgt.poller.monitors.JDBCMonitor",
            "org.opennms.netmgt.poller.monitors.JDBCStoredProcedureMonitor",
            "org.opennms.netmgt.poller.monitors.BSFMonitor",
            "org.opennms.netmgt.poller.monitors.OmsaStorageMonitor",
            "org.opennms.netmgt.poller.monitors.HostResourceSwRunMonitor",
            "org.opennms.netmgt.poller.monitors.NetScalerGroupHealthMonitor",
            "org.opennms.netmgt.poller.monitors.WebMonitor", 
            "org.opennms.netmgt.poller.monitors.CiscoIpSlaMonitor",
            "org.opennms.netmgt.poller.monitors.VmwareMonitor",
            "org.opennms.netmgt.poller.monitors.VmwareCimMonitor",
            "org.opennms.netmgt.poller.monitors.WsManMonitor")
            .build();

    @ClassRule
    public static final TestEnvironment getTestEnvironment() {
        if (!OpenNMSSeleniumTestCase.isDockerEnabled()) {
            return new NullTestEnvironment();
        }
        try {
            final TestEnvironmentBuilder builder = TestEnvironment.builder().all();
            OpenNMSSeleniumTestCase.configureTestEnvironment(builder);
            m_testEnvironment = builder.build();
            return m_testEnvironment;
        } catch (final Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Before
    public void checkForDocker() {
        Assume.assumeTrue(OpenNMSSeleniumTestCase.isDockerEnabled());
    }

    @Test
    public void canLoadMonitorsOnMinion() throws Exception {
        final InetSocketAddress sshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.MINION, 8201);
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
                .until(() -> listAndVerifyMonitors("Minion", sshAddr), hasSize(0));
    }

    @Test
    public void canLoadMonitorsOnOpenNMS() throws Exception {
        final InetSocketAddress sshAddr = m_testEnvironment.getServiceAddress(ContainerAlias.OPENNMS, 8101);
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
                .until(() -> listAndVerifyMonitors("OpenNMS", sshAddr), hasSize(0));
    }

    public List<String> listAndVerifyMonitors(String host, InetSocketAddress sshAddr) throws Exception {
        List<String> unmatchedMonitors = new ArrayList<>();
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {
            // List the monitors
            PrintStream pipe = sshClient.openShell();
            pipe.println("poller:list-monitors");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());

            // Parse the output
            String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());

            shellOutput = StringUtils.substringAfter(shellOutput, "poller:list-monitors");
            LOG.info("Monitors output: {}", shellOutput);
            Set<String> monitors = new HashSet<>();
            for (String monitor : shellOutput.split("\\r?\\n")) {
                if (StringUtils.isNotBlank(monitor)) {
                    monitors.add(monitor);
                }
            }
            LOG.info("Found monitors: {}", monitors);

            // Verify
            for (String monitorName : expectedMonitors) {
                if (!monitors.contains(monitorName)) {
                    unmatchedMonitors.add(monitorName);
                }
            }
        }
        return unmatchedMonitors;
    }

}
