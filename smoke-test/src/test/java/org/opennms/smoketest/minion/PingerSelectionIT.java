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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShell;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.Container;

/**
 * Verifies that the assembled Core and Minion select the FFM pinger.
 *
 * The scan falls back to a deprecated pinger when the FFM pinger does not work,
 * so a working ping alone does not show which pinger answered.
 */
@Category(MinionTests.class)
public class PingerSelectionIT {

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.MINION;

    private static final Path MINION_KARAF_LOG = Paths.get("/opt", "minion", "data", "log", "karaf.log");

    /** What opennms:ping prints for a reply. A failure prints an exception class name instead of the time. */
    private static final Pattern PING_REPLY = Pattern.compile("PING: /127\\.0\\.0\\.1 \\S+ ms");

    private final KarafShell m_karafShell = new KarafShell(stack.opennms().getSshAddress());

    private void awaitPingReply(final String command) {
        m_karafShell.runCommand(command, output -> PING_REPLY.matcher(output).find(), false);
    }

    @Test
    public void coreSelectsTheFfmPinger() throws Exception {
        // A reply shows that the scan ran and found a working pinger.
        awaitPingReply("opennms:ping 127.0.0.1");

        // Core filters most logs at WARN, so look for the two warnings the scan logs when it
        // does not select the FFM pinger. grep exits with 1 when no line matches.
        final Container.ExecResult result = stack.opennms().execInContainer("grep", "-rhE",
                "FFM pinger cannot be used here|No ICMP pinger works here",
                OpenNMSContainer.CONTAINER_LOG_DIR.toString());
        assertEquals("Core did not select the FFM pinger: " + result.getStdout() + result.getStderr(),
                1, result.getExitCode());
    }

    @Test
    public void minionSelectsTheFfmPinger() {
        awaitPingReply("opennms:ping -l " + stack.minion().getLocation() + " 127.0.0.1");

        // Minion logs org.opennms at INFO, so the result of the scan is in karaf.log.
        assertThat(TestContainerUtils.getFileFromContainerAsString(stack.minion(), MINION_KARAF_LOG),
                containsString("Best available pinger is: class org.opennms.netmgt.icmp.ffm.FfmPinger"));
    }
}
