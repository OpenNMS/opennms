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

package org.opennms.core.ipc.sink.api;

/**
 * Defines how messages will be aggregated.
 *
 * When a {@link SinkModule} defines a {@link AggregationPolicy}, messages
 * will be aggregated into "buckets", which are keyed based on the object
 * returned by {@link #key(Object)}.
 *
 * The aggregation function {@link #aggregate(Message, Object)} is called to
 * create buckets and combine messages into an existing bucket.
 *
 * The completion size and completion interval options determine the conditions
 * under which the buckets will be dispatched.
 *
 * @author jwhite
 *
 * @param <S> type of message that will be sent by the producers
 * @param <T> type of message (bucket) that will be received by the consumers
 */
public interface AggregationPolicy<S, T> {

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
     * Aggregate the given message into an existing bucket, or
     * create a new bucket if no bucket exists.
     *
     * @param oldBucket the existing bucket, or <code>null</code> if a new bucket should be created
     * @param newMessage the message to aggregate
     * @return the new bucket
     */
    T aggregate(T oldBucket, S newMessage);

}
