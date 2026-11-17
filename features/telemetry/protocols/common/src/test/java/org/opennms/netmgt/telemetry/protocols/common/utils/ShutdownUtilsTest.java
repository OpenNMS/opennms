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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

public class ShutdownUtilsTest {

    @Test
    public void waitsForRunningTask() throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CountDownLatch started = new CountDownLatch(1);
        final AtomicBoolean finished = new AtomicBoolean(false);
        executor.execute(() -> {
            started.countDown();
            try {
                Thread.sleep(300);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            finished.set(true);
        });
        started.await();

        assertTrue(ShutdownUtils.shutdownAndAwait(executor, "test", Duration.ofSeconds(10)));
        assertTrue("the running task must finish before the stop returns", finished.get());
    }

    @Test
    public void givesUpAfterTimeout() throws Exception {
        final ExecutorService executor = Executors.newSingleThreadExecutor();
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        executor.execute(() -> {
            started.countDown();
            try {
                release.await();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        started.await();

        try {
            assertFalse(ShutdownUtils.shutdownAndAwait(executor, "test", Duration.ofMillis(200)));
        } finally {
            release.countDown();
        }
    }

    @Test
    public void acceptsNullExecutor() {
        assertTrue(ShutdownUtils.shutdownAndAwait(null, "test", Duration.ofSeconds(1)));
    }
}
