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

import java.util.concurrent.CompletableFuture;

/**
 * Used to asynchronously dispatch messages.
 *
 * Instances of these should be created by the {@link MessageDispatcherFactory}.
 *
 * @author jwhite
 */
public interface AsyncDispatcher<S extends Message> extends AutoCloseable {

    /**
     * Asynchronously send the given message.
     *
     * @param message the message to send
     * @return a future that is resolved once the message was dispatched or queued
     */
    CompletableFuture<DispatchStatus> send(S message);

    /**
     * Returns the number of messages that are currently queued
     * awaiting for dispatch.
     *
     * @return current queue size
     */
    int getQueueSize();
    
    enum DispatchStatus {
        /**
         * The message was actually dispatched.
         */
        DISPATCHED,

        /**
         * The message has been queued to be dispatched later.
         */
        QUEUED
    }

}
