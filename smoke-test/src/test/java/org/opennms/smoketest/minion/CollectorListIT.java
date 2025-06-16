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
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.CommandTestUtils;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;

/**
 * Verifies the output of "opennms:list-collectors" on both OpenNMS and Minion.
 */
@Category(MinionTests.class)
public class CollectorListIT {

    private static final Logger LOG = LoggerFactory.getLogger(CollectorListIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINION;

    private ImmutableSet<String> commonCollectors = ImmutableSet.<String> builder().add(
            "org.opennms.netmgt.collectd.HttpCollector",
            "org.opennms.netmgt.collectd.JdbcCollector",
            "org.opennms.netmgt.collectd.Jsr160Collector",
            "org.opennms.netmgt.collectd.VmwareCimCollector",
            "org.opennms.netmgt.collectd.VmwareCollector",
            "org.opennms.netmgt.collectd.WmiCollector",
            "org.opennms.netmgt.collectd.WsManCollector",
            "org.opennms.netmgt.collectd.prometheus.PrometheusCollector",
            "org.opennms.protocols.xml.collector.XmlCollector")
            .build();

    private ImmutableSet<String> expectedMinionCollectors = ImmutableSet.<String> builder().add(
            "org.opennms.protocols.nsclient.collector.NSClientCollector")
            .addAll(commonCollectors)
            .build();

    private ImmutableSet<String> expectedOpenNMSCollectors = ImmutableSet.<String> builder().add(
            "org.opennms.netmgt.collectd.SnmpCollector")
            .addAll(commonCollectors)
            .build();

    @Test
    public void canLoadCollectorsOnMinion() throws Exception {
        final InetSocketAddress sshAddr = stack.minion().getSshAddress();
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
                .until(() -> listAndVerifyCollectors(sshAddr, expectedMinionCollectors), hasSize(0));
    }

    @Test
    public void canLoadCollectorsOnOpenNMS() throws Exception {
        final InetSocketAddress sshAddr = stack.opennms().getSshAddress();
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
                .until(() -> listAndVerifyCollectors(sshAddr, expectedOpenNMSCollectors), hasSize(0));
    }

    public List<String> listAndVerifyCollectors(InetSocketAddress sshAddr, Set<String> expectedCollectors) throws Exception {
        List<String> unmatchedCollectors = new ArrayList<>();
        try (final SshClient sshClient = new SshClient(sshAddr, OpenNMSContainer.ADMIN_USER, OpenNMSContainer.ADMIN_PASSWORD)) {
            // List the collectors
            PrintStream pipe = sshClient.openShell();
            pipe.println("opennms:list-collectors");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());

            // Parse the output
            String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());

            shellOutput = StringUtils.substringAfter(shellOutput, "opennms:list-collectors");
            LOG.info("Collectors output: {}", shellOutput);
            Set<String> collectors = new HashSet<>();
            for (String collector : shellOutput.split("\\r?\\n")) {
                if (StringUtils.isNotBlank(collector)) {
                    collectors.add(collector);
                }
            }
            LOG.info("Found collectors: {}", collectors);

            // Verify
            for (String expectedCollector : expectedCollectors) {
                if (!collectors.contains(expectedCollector)) {
                    unmatchedCollectors.add(expectedCollector);
                }
            }
        }
        return unmatchedCollectors;
    }

}
