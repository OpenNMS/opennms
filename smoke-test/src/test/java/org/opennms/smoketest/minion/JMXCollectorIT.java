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

import java.io.PrintStream;
import java.net.InetSocketAddress;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.hamcrest.Matchers.containsString;

@Category(MinionTests.class)
public class JMXCollectorIT {
    private static final Logger LOG = LoggerFactory.getLogger(JMXCollectorIT.class);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINION;

    @Test
    public void canPerformAdhocJmxCollection() throws Exception {
        final InetSocketAddress sshAddr = stack.opennms().getSshAddress();
        await().atMost(3, MINUTES).pollInterval(15, SECONDS).pollDelay(0, SECONDS)
                // Issue the collection and verify that a known string appears in the output
                // Which string it is doesn't really matter provided that it is only returned when the collection
                // was successfull
                .until(() -> doCollect(sshAddr), containsString("java_lang_type_OperatingSystem"));
    }

    public String doCollect(InetSocketAddress sshAddr) throws Exception {
        try (final SshClient sshClient = new SshClient(sshAddr, "admin", "admin")) {
            // Perform an adhoc collection from against the Minion JVM
            final PrintStream pipe = sshClient.openShell();
            pipe.println("opennms:collect -l MINION org.opennms.netmgt.collectd.Jsr160Collector 127.0.0.1 port=18980");
            pipe.println("logout");
            await().atMost(1, MINUTES).until(sshClient.isShellClosedCallable());

            // Sanitize the output
            String shellOutput = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());
            shellOutput = StringUtils.substringAfter(shellOutput, "opennms:collect");
            LOG.info("Collect output: {}", shellOutput);

            return shellOutput;
        }
    }
}
