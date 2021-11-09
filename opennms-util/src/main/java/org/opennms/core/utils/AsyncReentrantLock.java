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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A reentrant lock that is not bound to a thread.
 *
 * {@link java.util.concurrent.locks.ReentrantLock} must be unlocked by the same thread that acquired the lock. This
 * class provides a {@link Locker} abstraction that can be passed between threads. In other words a lock acquired by
 * a specific locker in some thread can be unlocked in a different thread. An {@code AsyncReentrantLock} can be
 * acquired several times by the same locker.
 */
public class AsyncReentrantLock {

    private final Semaphore semaphore;

    public AsyncReentrantLock(boolean fair) {
        semaphore = new Semaphore(1, fair);
    }

    public static class Locker {

        private Map<Semaphore, Integer> map = new IdentityHashMap<>();

        private synchronized void lock(Semaphore s) {
            Integer i = map.get(s);
            if (i == null) {
                s.acquireUninterruptibly();
                map.put(s, 1);
            } else {
                map.put(s, i + 1);
            }
        }

        private synchronized boolean tryLock(Semaphore s, long time, TimeUnit timeUnit) throws InterruptedException {
            Integer i = map.get(s);
            if (i == null) {
                var acquired = s.tryAcquire(time, timeUnit);
                if (acquired) {
                    map.put(s, 1);
                }
                return acquired;
            } else {
                map.put(s, i + 1);
                return true;
            }
        }

        private synchronized void unlock(Semaphore s) {
            Integer i = map.get(s);
            if (i == null) {
                throw new IllegalStateException("semaphore was not acquired first");
            } else if (i == 1) {
                map.remove(s);
                s.release();
            } else {
                map.put(s, i - 1);
            }
        }

    }

    public void unlock(Locker locker) {
        locker.unlock(semaphore);
    }

    public void lock(Locker locker) {
        locker.lock(semaphore);
    }

    public boolean tryLock(Locker locker, long time, TimeUnit timeUnit) throws InterruptedException {
        return locker.tryLock(semaphore, time, timeUnit);
    }
}
