/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.core.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reentrant lock that is not bound to a thread.
 *
 * {@link java.util.concurrent.locks.ReentrantLock} must be unlocked by the same thread that acquired the lock. This
 * class provides a {@link Locker} abstraction that can be passed between threads. In other words, a lock acquired by
 * a specific locker in some thread can be unlocked in a different thread. An {@code AsyncReentrantLock} is reentrant,
 * i.e. it can be locked by the same locker several times.
 *
 * <strong>Note:</strong> An {@code AsyncReentrantLock} must not be used by several threads concurrently, i.e. it does
 * not synchronize between threads. An {@code AsyncReentrantLock} is for executions that may span several threads but
 * at every time only a single thread is active.
 */
public class AsyncReentrantLock {

    private static final Logger LOG = LoggerFactory.getLogger(AsyncReentrantLock.class);

    private Locker lockedBy = null;
    private int cnt = 0;
    private Stack<String> lockedAt = new Stack<>();

    public AsyncReentrantLock(boolean fair) {
    }

    private static String at(Exception ex) {
        return Arrays.stream(ex.getStackTrace()).map(e -> e.toString()).collect(Collectors.joining("\n"));
    }

    public static class Locker {
        private static AtomicInteger counter = new AtomicInteger();

        private String createdAt = at(new Exception());
        private int id = counter.incrementAndGet();

        @Override
        public String toString() {
            return "Locker{" +
                   "id: " + id +
                   "createdAt='" + createdAt + '\'' +
                   '}';
        }
    }

    public synchronized void unlock(Locker locker) {
        Objects.requireNonNull(locker);
        if (lockedBy != locker) {
            throw new IllegalStateException("lock is not owned by given locker");
        }
        cnt--;
        lockedAt.pop();
        if (cnt == 0) {
            lockedBy = null;
        }
        notify();
    }

    public synchronized void lock(Locker locker) {
        Objects.requireNonNull(locker);
        while(lockedBy != null && lockedBy != locker) {
            try {
                wait();
            } catch (InterruptedException e) {
                // ignore
            }
        }
        cnt++;
        lockedBy = locker;
        lockedAt.push(at(new Exception()));
        notify();
    }

    public synchronized boolean tryLock(Locker locker, long duration, TimeUnit timeUnit) throws InterruptedException {
        Objects.requireNonNull(locker);
        var start = System.currentTimeMillis();
        var total = timeUnit.toMillis(duration);
        while (lockedBy != null && lockedBy != locker) {
            var ellapsed = System.currentTimeMillis() - start;
            var remaining = total - ellapsed;
            if (remaining > 0) {
                wait(remaining);
            } else {
//                LOG.debug("could not lock - locker: " + locker + "; cnt: " + cnt + "; lockedBy: " + lockedBy);
//                lockedAt.forEach(ex -> LOG.debug("lockedAt: " + ex));
                return false;
            }
        }
        cnt++;
        lockedBy = locker;
        lockedAt.push(at(new Exception()));
        notify();
        return true;
    }
}
