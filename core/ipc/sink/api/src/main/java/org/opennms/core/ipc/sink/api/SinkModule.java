/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
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
 * Defines how the messages will be routed and marshaled/unmarshaled over the wire.
 *
 * Messages can be aggregated by an optional {@link AggregationPolicy}.
 * If aggregation is not used, the message type sent by the producers must match
 * the message type received by the consumers.
 *
 * @author jwhite
 *
 * @param <S> type of message that will be sent by the producers
 * @param <T> type of message that will be received by the consumers
 */
public interface SinkModule<S extends Message, T extends Message> {

    /**
     * Globally unique identifier.
     *
     * Used in the JMS queue name in the Camel implementation.
     */
    String getId();

    /**
     * The number of threads used to consume from the broker.
     */
    int getNumConsumerThreads();

    /**
     * Marshals the message to a byte array.
     */
    byte[] marshal(T message);

    /**
     * Unmarshals the message from a byte array.
     */
    T unmarshal(byte[]  message);

    /**
     * Defines how messages should be combined, and when they
     * should be "released".
     *
     * Modules that do not wish to use aggregation can return {@code null}.
     *
     * @return the {@link AggregationPolicy} used to combine messages, or {@code null}
     * if the messages should not be combined.
     */
    AggregationPolicy<S,T,?> getAggregationPolicy();

    /**
     * Defines how messages should be asynchronously dispatched.
     *
     * @return the {@link AsyncPolicy} used when asynchronously dispatching
     * messages for this module.
     */
    AsyncPolicy getAsyncPolicy();
}
