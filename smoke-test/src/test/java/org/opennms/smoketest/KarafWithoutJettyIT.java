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
package org.opennms.smoketest;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;

import java.nio.file.Path;

import org.junit.ClassRule;
import org.junit.Test;
import org.opennms.smoketest.containers.OpenNMSContainer;
import org.opennms.smoketest.stacks.OpenNMSStack;
import org.opennms.smoketest.utils.KarafShellUtils;
import org.opennms.smoketest.utils.TestContainerUtils;
import org.testcontainers.containers.wait.strategy.AbstractWaitStrategy;

import com.github.dockerjava.api.exception.NotFoundException;

/** Verifies that Karaf is owned by the core daemon lifecycle, not Jetty. */
public class KarafWithoutJettyIT {
    @ClassRule
    public static final OpenNMSStack STACK = createStack();

    private static OpenNMSStack createStack() {
        final OpenNMSStack stack = OpenNMSStack.minimal(
                builder -> builder.withWaitStrategy(KarafWaitStrategy::new));
        stack.opennms().withEnv("CORE_SERVICE_JETTYSERVER_ENABLED", "false");
        return stack;
    }

    @Test
    public void karafStartsWithoutJetty() {
        KarafShellUtils.awaitHealthCheckSucceeded(STACK.opennms());
    }

    private static final class KarafWaitStrategy extends AbstractWaitStrategy {
        private final OpenNMSContainer container;

        private KarafWaitStrategy(final OpenNMSContainer container) {
            this.container = container;
        }

        @Override
        protected void waitUntilReady() {
            final Path managerLog = OpenNMSContainer.CONTAINER_LOG_DIR.resolve("manager.log");
            await("waiting for OpenNMS startup without Jetty")
                    .atMost(5, MINUTES)
                    .failFast("container is no longer running", () -> !container.isRunning())
                    .ignoreException(NotFoundException.class)
                    .until(() -> TestContainerUtils.getFileFromContainerAsString(container, managerLog),
                            containsString("Starter: Startup complete"));
            KarafShellUtils.awaitHealthCheckSucceeded(container);
        }
    }
}
