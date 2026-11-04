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
package org.opennms.netmgt.telemetry.protocols.common.utils;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ShutdownUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ShutdownUtils.class);

    private ShutdownUtils() {
    }

    /**
     * Shuts down the executor and waits for the running and queued tasks to finish.
     *
     * Telemetryd closes the dispatchers right after the parsers stop. Records that are
     * still in the executor at that point are lost, so the stop waits for them.
     *
     * @return true when the executor terminated within the timeout
     */
    public static boolean shutdownAndAwait(final ExecutorService executor, final String name, final Duration timeout) {
        if (executor == null) {
            return true;
        }
        executor.shutdown();
        try {
            if (executor.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
                return true;
            }
            LOG.warn("Executor of {} did not finish its tasks within {} ms. Records that are still in flight are lost.", name, timeout.toMillis());
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted while waiting for the executor of {} to finish.", name);
        }
        return false;
    }
}
