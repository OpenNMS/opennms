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

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.opennms.smoketest.containers.MinionContainer;
import org.opennms.smoketest.junit.MinionTests;
import org.opennms.smoketest.stacks.MinionProfile;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.utils.CommandTestUtils;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

@Category(MinionTests.class)
public class NMS14655_IT {
    private static final Logger LOG = LoggerFactory.getLogger(NMS14655_IT.class);

    @ClassRule
    public static final MinionContainer MINION_CONTAINER = new MinionContainer(StackModel.newBuilder().build(), MinionProfile.newBuilder()
            .withId("00000000-0000-0000-0000-000000ddba11")
            .withLocation("Fulda")
            .withDominionGrpcScvClientSecret("foobar")
            // we do not wait for health check since it will never succeed without the appliance
            .withWaitStrategy(c -> new AbstractWaitStrategy() {
                @Override
                protected void waitUntilReady() {
                }
            })
            .build());

    @Test
    public void verifyStartup() {
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    final String bundleCount = ssh("bundle:list | grep SCV | wc -l");
                    final String scvBundles = ssh("bundle:list | grep SCV");
                    LOG.warn("bundleCount: {}", bundleCount);
                    LOG.warn("SCV bundles: {}", scvBundles);
                    return bundleCount.contains("3") && scvBundles.contains("Dominion gRPC Impl") && !scvBundles.contains("JCEKS Impl");
                });
    }

    private String ssh(final String command) {
        try (final SshClient sshClient = MINION_CONTAINER.ssh()) {
            final PrintStream pipe = sshClient.openShell();
            if (command != null) {
                pipe.println(command);
            }
            pipe.println("logout");
            await().atMost(30, SECONDS).until(sshClient.isShellClosedCallable());
            // Retrieve output and strip ANSI sequences
            String output = CommandTestUtils.stripAnsiCodes(sshClient.getStdout());

            // Extract command output
            final Pattern pattern = Pattern.compile(".*admin@minion\\(\\)>(.*)admin@minion\\(\\)>.*", Pattern.DOTALL);
            final Matcher matcher = pattern.matcher(output);
            if (matcher.matches()) {
                return matcher.group(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
