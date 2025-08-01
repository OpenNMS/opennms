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
