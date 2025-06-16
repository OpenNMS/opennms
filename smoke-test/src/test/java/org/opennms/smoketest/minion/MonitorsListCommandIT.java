/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.smoketest.minion;

import static org.awaitility.Awaitility.await;
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
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.CommandTestUtils;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

@Category(MinionTests.class)
public class MonitorsListCommandIT {

    private static final Logger LOG = LoggerFactory.getLogger(MonitorsListCommandIT.class);

    @ClassRule
    public final static OpenNMSStack stack = OpenNMSStack.MINION;

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
            "org.opennms.netmgt.poller.monitors.MinaSshMonitor",
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
            "org.opennms.netmgt.poller.monitors.ActiveMQMonitor",
            "org.opennms.netmgt.poller.monitors.OmsaStorageMonitor",
            "org.opennms.netmgt.poller.monitors.HostResourceSwRunMonitor",
            "org.opennms.netmgt.poller.monitors.NetScalerGroupHealthMonitor",
            "org.opennms.netmgt.poller.monitors.WebMonitor", 
            "org.opennms.netmgt.poller.monitors.CiscoIpSlaMonitor",
            "org.opennms.netmgt.poller.monitors.VmwareMonitor",
            "org.opennms.netmgt.poller.monitors.VmwareCimMonitor",
            "org.opennms.netmgt.poller.monitors.WsManMonitor",
            "org.opennms.netmgt.poller.monitors.DhcpMonitor")
            .build();

    @Test
    public void canLoadMonitorsOnMinion() throws Exception {
        final InetSocketAddress sshAddr = stack.minion().getSshAddress();
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
                .until(() -> listAndVerifyMonitors("Minion", sshAddr), hasSize(0));
    }

    @Test
    public void canLoadMonitorsOnOpenNMS() throws Exception {
        final InetSocketAddress sshAddr = stack.opennms().getSshAddress();
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
                .until(() -> listAndVerifyMonitors("OpenNMS", sshAddr), hasSize(0));
    }

    public List<String> listAndVerifyMonitors(String host, InetSocketAddress sshAddr) throws Exception {
        List<String> unmatchedMonitors = new ArrayList<>();
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {
            // List the monitors
            PrintStream pipe = sshClient.openShell();
            pipe.println("opennms:list-monitors");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());

            // Parse the output
            String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());

            shellOutput = StringUtils.substringAfter(shellOutput, "opennms:list-monitors");
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
