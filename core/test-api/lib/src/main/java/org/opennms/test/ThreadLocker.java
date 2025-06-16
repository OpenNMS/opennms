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