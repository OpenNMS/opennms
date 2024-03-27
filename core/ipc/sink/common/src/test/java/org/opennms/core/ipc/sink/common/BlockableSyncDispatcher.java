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
package org.opennms.core.ipc.sink.common;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SyncDispatcher;

public class BlockableSyncDispatcher<S extends Message> implements SyncDispatcher<S> {

    private final DispatchThreadLatch dispatchThreadLatch = new DispatchThreadLatch();
    private final AtomicInteger blockedThreads = new AtomicInteger(0);
    private final AtomicInteger numDispatched = new AtomicInteger(0);
    private final List<S> dispatchedMessages = new CopyOnWriteArrayList<>();

    @Override
    public void send(S message) {
        blockedThreads.incrementAndGet();
        dispatchThreadLatch.await();
        numDispatched.incrementAndGet();
        dispatchedMessages.add(message);
        blockedThreads.decrementAndGet();
    }

    @Override
    public void close() {
        // pass
    }

    public void block() {
        dispatchThreadLatch.lock();
    }

    public void unblock() {
        dispatchThreadLatch.unlock();
    }

    public int getBlockedThreadCount() {
        return blockedThreads.get();
    }

    public int getNumMessageDispatched() {
        return numDispatched.get();
    }

    public List<S> getDispatchedMessages() {
        return Collections.unmodifiableList(dispatchedMessages);
    }

    private static class DispatchThreadLatch {
        private boolean blocked = false;

        synchronized void await() {
            try {
                while (blocked) {
                    wait();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        synchronized void lock() {
            blocked = true;
        }

        synchronized void unlock() {
            blocked = false;
            notifyAll();
        }
    }

}
