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

/**
 * Defines the behavior of asynchronous dispatching.
 *
 * @author jwhite
 */
public interface AsyncPolicy {

    /**
     * Maximum number of messages that can be queued awaiting
     * for dispatch.
     *
     * @return queue size
     */
    int getQueueSize();

    /**
     * Number of background threads that will be used to
     * dispatch messages from the queue.
     *
     * @return number of threads
     */
    int getNumThreads();

    /**
     * Used to control the behavior of a dispatch when the queue
     * is full.
     *
     * When <code>true</code> the calling thread will be blocked
     * until the queue can accept the message, or the thread is
     * interrupted.
     *
     * When <code>false</code> the dispatch will return a future
     * with a {@link java.util.concurrent.RejectedExecutionException}/
     *
     * @return whether or not the thread calling dispatch
     * should block when the queue is full
     */
    boolean isBlockWhenFull();
}
