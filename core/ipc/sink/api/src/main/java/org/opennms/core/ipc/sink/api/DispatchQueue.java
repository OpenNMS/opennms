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

package org.opennms.core.ipc.sink.api;

import java.util.Map;

public interface DispatchQueue<T> {

    /**
     * Adds the message to the tail of the queue blocking if the queue is currently full.
     */
    EnqueueResult enqueue(T message, String key) throws WriteFailedException;

    /**
     * Retrieves and removes the head of this queue, waiting if necessary until an element becomes available.
     *
     * @throws InterruptedException if interrupted while waiting
     */
    Map.Entry<String, T> dequeue() throws InterruptedException;

    /**
     * @return true if the queue has no more capacity, false otherwise
     */
    boolean isFull();

    /**
     * @return the current number of queued items
     */
    int getSize();

    /**
     * The result of performing an {@link #enqueue(Object, String)}.
     */
    enum EnqueueResult {
        /**
         * Used to represent that the entry was immediately queued in such a way that it is likely to be processed soon
         * and should be tracked by the client. For instance, the entry could have been queued directly on the heap.
         * <p>
         * This result indicates that if the entry gets dispatched it will be within the lifespan of the current process
         * such that any clients tracking the entry in memory will still be valid upon dispatch.
         */
        IMMEDIATE,

        /**
         * Used to represent that the entry was queued but in such a way that it is not likely to be processed soon and
         * should not be tracked by the client. For instance, the entry could have been queued by being serialized to
         * disk and may not be processed until after a restart.
         */
        DEFERRED
    }
}
