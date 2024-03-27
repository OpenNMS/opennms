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
 * Defines how messages will be aggregated.
 *
 * When a {@link SinkModule} defines a {@link AggregationPolicy}, messages
 * will be aggregated by accumulators, which are keyed based on the object
 * returned by {@link #key(Object)}.
 *
 * The aggregation function {@link #aggregate(Object, Object)} is called to
 * create accumulators and add messages to existing accumulators.
 *
 * The completion size and completion interval options determine the conditions
 * under which the buckets will be dispatched.
 *
 * @author jwhite
 *
 * @param <S> type of message that will be sent by the producers
 * @param <T> type of message that will be received by the consumers
 * @param <U> intermerdiary accumulator type used to aggregate the messages
 */
public interface AggregationPolicy<S, T, U> {

    /**
     * Maximum number of messages to be added to a bucket before dispatching.
     *
     * If this value is <= 1, the buckets should be dispatched immediately after
     * adding a single element.
     *
     * @return maximum number of messages per bucket
     */
    int getCompletionSize();

    /**
     * Maximum number of milliseconds for which buckets should
     * continue accumulating messages after creation.
     *
     * If a bucket has been created for longer than this interval, it
     * will be dispatched regardless of it's current size.
     *
     * Values <= 0 will disable periodic flushing and buckets will
     * only be dispatched once they have reached the maximum size.
     *
     * @return number of milliseconds to keep a bucket before dispatching
     */
    int getCompletionIntervalMs();

    /**
     * Calculate a key for the given message.
     *
     * Objects with the same key will be aggregated together.
     *
     * Returned values should non-null.
     *
     * @param message the message
     * @return the message's key
     */
    Object key(S message);

    /**
     * Aggregate the given message into an existing accumulator, or
     * create a new accumulator if no accumulator exists.
     *
     * @param accumulator the existing accumulator, or <code>null</code> if a new accumulator should be created
     * @param newMessage the message to aggregate
     * @return the new or updated accumulator
     */
     U aggregate(U accumulator, S newMessage);

    /**
     * Build the resulting message from the accumulator.
     *
     * @param accumulator an existing accumulator
     * @return the aggregated message to dispatch
     */
     T build(U accumulator);

}
