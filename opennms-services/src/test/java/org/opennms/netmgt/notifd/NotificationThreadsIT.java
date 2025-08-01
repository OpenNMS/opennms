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
package org.opennms.netmgt.notifd;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.mock.MockNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.test.ThreadLocker;

public class NotificationThreadsIT extends NotificationsITCase  {

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * NMS-9766: Verifies that the number of threads used to concurrently execute
     * the notification strategies does not exceed the "max-threads" configuration.
     */
    @Test
    public void canLimitThreads() throws Exception {
        // Verify the thread limit N
        final int N = m_notifdConfig.getConfiguration().getMaxThreads();
        assertTrue( N + " should be some positive value < max int.",
                0 < N && N < Integer.MAX_VALUE);

        // Setup the locker
        final ThreadLocker threadLocker = ThreadLockingNotificationStrategy.getThreadLocker();
        final CompletableFuture<Integer> future = ThreadLockingNotificationStrategy.getThreadLocker().waitForThreads(N);

        // Trigger M notifications where M = 2 * N
        final int M = 2 * N;
        for (int i = 0; i < M; i++) {
            MockNode node = m_network.getNode(1);
            EventBuilder eb = new EventBuilder("uei.opennms.org/test/notificationConcurrencyTest", "test");
            eb.setNodeid(node.getNodeId());
            m_eventMgr.sendEventToListeners(eb.getEvent());
        }

        // Wait until N threads a locked
        future.get(1, TimeUnit.MINUTES);

        // Wait a little longer and verify that no extra threads are waiting
        // This validates that no more than N threads are currently invoking
        // the notification strategy
        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
        assertEquals(0, threadLocker.getNumExtraThreadsWaiting());

        // Release the gate
        threadLocker.release();

        // Wait until all M notifications have been executed
        // This validates that all of the M notifications end up
        // being invoked
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        await().atMost(5, TimeUnit.SECONDS)
                .until(ThreadLockingNotificationStrategy::getNotificationsSent, equalTo((long)M));
    }

}
