/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
