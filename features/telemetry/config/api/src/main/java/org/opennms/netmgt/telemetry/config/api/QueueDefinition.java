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
package org.opennms.netmgt.telemetry.config.api;

import java.util.Optional;

/**
 * Telemetry protocol configuration.
 */
public interface QueueDefinition {

    /**
     * The name of the protocol.
     *
     * This is used as a suffix for any associated queues that are created and
     * must be the same on both OpenNMS and Minion.
     *
     * @return the protocol name
     */
    String getName();

    /**
     * Number of threads used for consuming/dispatching messages.
     *
     * @return the number of threads
     */
    Optional<Integer> getNumThreads();

    /**
     * Messages are aggregated in batches before being dispatched.
     * When the batch reaches this size, it will be dispatched.
     *
     * @return the batch size
     */
    Optional<Integer> getBatchSize();

    /**
     * Messages are aggregated in batches before being dispatched.
     * When the batch has been created for longer than this interval (ms)
     * it will be dispatched, regardless of the current size.
     *
     * @return the batch interval
     */
    Optional<Integer> getBatchIntervalMs();

    /**
     * Maximum number of messages to keep in memory while waiting
     * to be dispatched.
     *
     * @return the queue size
     */
    Optional<Integer> getQueueSize();

    /**
     * Whether or not the routing key should be used when forwarding messages to the broker.
     *
     * @return whether or not to use the routing key
     */
    Optional<Boolean> getUseRoutingKey();

}
