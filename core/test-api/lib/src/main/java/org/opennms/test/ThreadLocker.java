/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class can be used to verify the number of threads that are
 * calling a particular method.
 *
 * This is achieved by locking all of the threads that call {@link MessageProducer#send}
 * and providing a callback (via a {@link CompletableFuture}) that is invoked
 * once the desired number of threads is reached.
 *
 * If a larger number of threads than expected call {@link MessageProducer#send},
 * these are locked as well, and the number of these is provided by
 * {@link #getNumExtraThreadsWaiting()}.
 *
 * @author jwhite
 */
public class ThreadLocker {
    private static final Logger LOG = LoggerFactory.getLogger(ThreadLocker.class);

    private CyclicBarrier barrier;
    private Semaphore semaphore;
    private final ReentrantLock lock = new ReentrantLock();

    public CompletableFuture<Integer> waitForThreads(int numThreads) {
        // Used to lock the threads in the barrier once the barrier has the
        // desired number of threads
        lock.lock();

        // Create a future that will be resolved once the barrier has
        // the desired number of threads
        final CompletableFuture<Integer> future = new CompletableFuture<>();

        barrier = new CyclicBarrier(numThreads, new Runnable() {
            @Override
            public void run() {
                future.complete(numThreads);
                // Wait until we're released
                lock.lock();
                lock.unlock();
            }
        });

        semaphore = new Semaphore(numThreads, true);
        return future;
    }

    public void park() {
        try {
            semaphore.acquire();
            barrier.await();
            semaphore.release();
        } catch (InterruptedException e) {
            LOG.error("Interrupted.", e);
        } catch (BrokenBarrierException e) {
            LOG.error("Broken barrier.", e);
        }
    }

    public void release() {
        lock.unlock();
    }

    public int getNumExtraThreadsWaiting() {
        return semaphore.getQueueLength();
    }
}