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
package org.opennms.smoketest.sentinel;

import static org.awaitility.Awaitility.await;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;
import org.opennms.smoketest.junit.SentinelTests;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.stacks.SentinelProfile;
import org.opennms.smoketest.stacks.StackModel;
import org.opennms.smoketest.stacks.TimeSeriesStrategy;
import org.opennms.smoketest.utils.SshClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Verifies that all exposed service DAOs can be loaded in a sentinel container
@Category(SentinelTests.class)
public class DaoIT {

    @Rule
    public Timeout timeout = new Timeout(20, TimeUnit.MINUTES);

    @ClassRule
    public static final OpenNMSStack stack = OpenNMSStack.withModel(StackModel.newBuilder()
            .withMinion()
            .withSentinels(SentinelProfile.newBuilder()
                    .withFile(Paths.get("target/deploy-artifacts/org.opennms.features.distributed.dao-test.jar"),
                            "deploy/org.opennms.features.distributed.dao-test.jar")
                    .build())
            .withTimeSeriesStrategy(TimeSeriesStrategy.NEWTS)
            .build());

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Test
    public void verifyDaos() {
        // Ensure we are actually started the sink and are ready to listen for messages
        await().atMost(5, MINUTES)
                .pollInterval(5, SECONDS)
                .until(() -> {
                    try (final SshClient sshClient = stack.sentinel().ssh()) {
                        final PrintStream pipe = sshClient.openShell();
                        final String command ="bundle:list";
                        pipe.println(command);
                        pipe.println("logout");

                        // Wait for karaf to process the commands
                        await().atMost(10, SECONDS).until(sshClient.isShellClosedCallable());

                        // Read stdout and verify
                        final String shellOutput = sshClient.getStdout();
                        final boolean bundleActive = Arrays.stream(shellOutput.split("\n"))
                                            .filter(row -> row.contains("OpenNMS :: Features :: Distributed :: DAO :: Test"))
                                            .findFirst().filter(bundle -> bundle.contains("Active"))
                                            .isPresent();
                        logger.info(command);
                        logger.info("{}", shellOutput);
                        return bundleActive;
                    } catch (Exception ex) {
                        logger.error("Error while trying to verify sentinel startup: {}", ex.getMessage());
                        return false;
                    }
                });

    }
}
