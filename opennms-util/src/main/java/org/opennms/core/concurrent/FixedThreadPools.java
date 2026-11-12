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
package org.opennms.core.concurrent;

import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helpers for pools created with {@link java.util.concurrent.Executors#newFixedThreadPool},
 * whose core and maximum sizes are kept equal.
 */
public final class FixedThreadPools {

    private static final Logger LOG = LoggerFactory.getLogger(FixedThreadPools.class);

    private FixedThreadPools() {
    }

    /**
     * Resizes a fixed thread pool in place and keeps the {@link LogPreservingThreadFactory}
     * that serves it in step, so that threads started during and after the resize carry the
     * new size in their names. The factory is updated before the pool grows, because
     * {@link ThreadPoolExecutor#setCorePoolSize} starts new workers right away when work
     * is queued, and after the pool shrinks.
     * <p>
     * The outcome is logged on behalf of the owner: a warning when the request is clamped,
     * debug when nothing changes, info when the size changes.
     *
     * @param pool the pool to resize
     * @param factory the factory the pool was created with, or {@code null}
     * @param owner a short description of the pool's owner for log messages
     * @param requestedSize the wanted number of threads; values below one are treated as one
     * @return the size actually applied
     */
    public static int resize(final ThreadPoolExecutor pool, final LogPreservingThreadFactory factory, final String owner, final int requestedSize) {
        Objects.requireNonNull(pool, "pool");
        final int size = Math.max(1, requestedSize);
        final int current = pool.getCorePoolSize();
        if (size != requestedSize) {
            LOG.warn("{} was asked for {} threads, using {} instead.", owner, requestedSize, size);
        }
        if (size == current) {
            LOG.debug("{} thread pool unchanged at {}.", owner, current);
            return size;
        }
        apply(pool, factory, size, current);
        LOG.info("{} thread pool resized from {} to {}.", owner, current, size);
        return size;
    }

    /**
     * Resizes a fixed thread pool in place, keeping core and maximum sizes equal.
     * <p>
     * The two sizes have to be changed in the right order: a core size above the current
     * maximum, or a maximum below the current core size, is rejected by
     * {@link ThreadPoolExecutor}. Growing therefore raises the maximum first and shrinking
     * lowers the core size first.
     * <p>
     * Growing lets the pool start workers as tasks arrive. Shrinking interrupts idle workers
     * at once and lets busy workers finish their current task before they exit; queued tasks
     * are not affected.
     *
     * @param pool the pool to resize
     * @param requestedSize the wanted number of threads; values below one are treated as one,
     *                      since a pool with no threads can never run anything
     * @return the size actually applied
     */
    public static int resize(final ThreadPoolExecutor pool, final int requestedSize) {
        Objects.requireNonNull(pool, "pool");
        final int size = Math.max(1, requestedSize);
        final int current = pool.getCorePoolSize();
        if (size != current) {
            apply(pool, null, size, current);
        }
        return size;
    }

    private static void apply(final ThreadPoolExecutor pool, final LogPreservingThreadFactory factory, final int size, final int current) {
        if (size > current) {
            if (factory != null) {
                factory.setPoolSize(size);
            }
            pool.setMaximumPoolSize(size);
            pool.setCorePoolSize(size);
        } else {
            pool.setCorePoolSize(size);
            pool.setMaximumPoolSize(size);
            if (factory != null) {
                factory.setPoolSize(size);
            }
        }
    }
}
