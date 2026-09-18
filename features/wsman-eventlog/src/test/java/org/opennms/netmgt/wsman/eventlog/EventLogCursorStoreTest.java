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
package org.opennms.netmgt.wsman.eventlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class EventLogCursorStoreTest {

    @Test
    public void keepsOneKeyPerNodeAndLogSoConcurrentPollsDoNotClobberEachOther() throws Exception {
        final EventLogCursorStore store = new EventLogCursorStore(new InMemoryJsonStore());
        final CountDownLatch go = new CountDownLatch(1);
        final List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            final String log = i % 2 == 0 ? "System" : "Application";
            final long value = i % 2 == 0 ? 106 : 107;
            final Thread t = new Thread(() -> {
                try {
                    go.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                store.put(7, log, value);
            });
            threads.add(t);
            t.start();
        }
        go.countDown();
        for (Thread t : threads) {
            t.join();
        }
        assertEquals(Long.valueOf(106), store.get(7, "System"));
        assertEquals(Long.valueOf(107), store.get(7, "Application"));
    }

    @Test
    public void clearsOneLogOrTheWholeNode() {
        final EventLogCursorStore store = new EventLogCursorStore(new InMemoryJsonStore());
        store.put(7, "System", 10);
        store.put(7, "Application", 20);
        store.put(8, "System", 30);
        assertEquals(Long.valueOf(10), store.get(7, "system"));

        store.clear(7, "System");
        assertNull(store.get(7, "System"));
        assertEquals(Long.valueOf(20), store.get(7, "Application"));

        store.clear(7);
        assertNull(store.get(7, "Application"));
        assertEquals(Long.valueOf(30), store.get(8, "System"));
    }
}
