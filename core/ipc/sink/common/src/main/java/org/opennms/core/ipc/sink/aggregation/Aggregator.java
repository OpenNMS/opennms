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

package org.opennms.core.ipc.sink.aggregation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;

import org.opennms.core.ipc.sink.api.AggregationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.Striped;

/**
 * This aggregator is used to realize a given {@link AggregationPolicy}.
 *
 * This class is designed to delegate dispatching to the calling threads as much
 * as possible (those which make calls to {@link #aggregate(Message).}
 *
 * @author jwhite
 *
 * @param <S> individual message
 * @param <T> aggregated message (i.e. bucket)
 */
public class Aggregator<S, T> implements AutoCloseable, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(Aggregator.class);

    /**
     * System property used to override the default number of stripe locks.
     */
    public static final String NUM_STRIPE_LOCKS_SYS_PROP = "org.opennms.ipc.sink.aggregation.stripes";

    public static final int DEFAULT_NUM_STRIPE_LOCKS = 64;

    /**
     * The number of stripe locks used to lock the buckets.
     *
     * Increasing this number will reduce the chance of collisions, but will cost
     * more in terms of memory.
     */
    private static final int NUM_STRIPE_LOCKS = Integer.getInteger(NUM_STRIPE_LOCKS_SYS_PROP, DEFAULT_NUM_STRIPE_LOCKS);

    private final AggregationPolicy<S,T,Object> aggregationPolicy;

    private final AggregatingMessageProducer<S,T> messageProducer;

    private final int completionSize;

    private final long completionIntervalMs;

    private final Timer flushTimer;

    private final ConcurrentHashMap<Object, Bucket> buckets = new ConcurrentHashMap<>();

    private final Striped<Lock> lockStripes = Striped.lock(NUM_STRIPE_LOCKS);

    public Aggregator(String id, AggregationPolicy<S,T,?> policy, AggregatingMessageProducer<S,T> messageProducer) {
        aggregationPolicy = (AggregationPolicy<S,T,Object>)Objects.requireNonNull(policy);
        this.messageProducer = Objects.requireNonNull(messageProducer);
        completionSize = aggregationPolicy.getCompletionSize();
        completionIntervalMs = aggregationPolicy.getCompletionIntervalMs();

        if (completionIntervalMs > 0) {
            // Periodically verify the buckets, and flush those that are older than completionIntervalMs
            flushTimer = new Timer(String.format("AggregatorFlush-%s", id));
            flushTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Aggregator.this.run();
                    } catch (Throwable t) {
                        // The timer may abort if we throw, so we catch here to make
                        // sure that the timer keeps running
                        LOG.error("An error occurred while flushing one or more aggregates in module '{}'.", id, t);
                    }
                }
            }, completionIntervalMs, completionIntervalMs);
        } else {
            flushTimer = null;
        }
    }

    /**
     * Aggregates the given messages into a bucket and returns
     * the bucket if it is ready to be dispatched.
     *
     * @param message the message to aggregated
     * @return the bucket if it is ready to be dispatched, or <code>null</code>
     * if nothing is ready to be dispatched
     */
    public T aggregate(S message) {
        // Compute the key
        final Object key = aggregationPolicy.key(message);
        // Lock the bucket
        final Lock lock = lockStripes.get(key);
        try {
            lock.lock();
            // Obtain the bucket, creating a new one if it doesn't already exist
            Bucket bucket = buckets.get(key);
            if (bucket == null) {
                bucket = new Bucket();
                buckets.put(key, bucket);
            }

            // Accumulate into the bucket
            T accumulator = bucket.accumulate(message);
            if (accumulator != null) {
                // The bucket is ready to be dispatched
                buckets.remove(key);
                return accumulator;
            } else {
                // The bucket is NOT ready to be dispatched
                return null;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void run() {
        final List<T> messagesReadyForDispatch = new LinkedList<>();
        // Grab a copy of all the current bucket keys
        final Set<Object> keys = new HashSet<>(buckets.keySet());
        // NMS-9114: As we iterate over the keys to add them to set above,
        // it's possible that one of the buckets was removed
        // in which case the key set may contain a null key
        // so we remove it here for good measure, otherwise
        // the call to bulkGet bellow will fail with an NPE
        keys.remove(null);

        // Lock all the buckets
        final Iterable<Lock> locks = lockStripes.bulkGet(keys);
        try {
            locks.forEach(Lock::lock);
            // Determine which buckets are ready to be dispatched
            // and remove these from the map
            final long cutOff = System.currentTimeMillis() - completionIntervalMs;
            for (final Object key : keys) {
                final Bucket bucket = buckets.get(key);
                // The bucket may have been removed between the time we retrieved
                // the keys, and the time we obtained the lock, so we make sure
                // it's non-null before accessing it's properties
                if (bucket != null && bucket.getFirstTimeMillis() != null && bucket.getFirstTimeMillis() <= cutOff) {
                    messagesReadyForDispatch.add(bucket.getValue());
                    buckets.remove(key);
                }
            }
        } finally {
            locks.forEach(Lock::unlock);
        }

        // Dispatch!
        for (T message : messagesReadyForDispatch) {
            messageProducer.dispatch(message);
        }
    }

    @Override
    public void close() throws Exception {
        if (flushTimer != null) {
            flushTimer.cancel();
        }
    }

    protected class Bucket {
        private Object accumulator;
        private int count = 0;
        private Long firstTimeMillis;

        public T accumulate(S message) {
            accumulator = aggregationPolicy.aggregate(accumulator, message);
            count++;
            if (count >= completionSize) {
                // We're ready!
                return aggregationPolicy.build(accumulator);
            } else if (completionIntervalMs > 0) {
                final long now = System.currentTimeMillis();
                if (firstTimeMillis == null) {
                    firstTimeMillis = now;
                } else if (now - firstTimeMillis >= completionIntervalMs) {
                    // We're ready!
                    return aggregationPolicy.build(accumulator);
                }
            }
            // We're NOT ready yet...
            return null;
        }

        public T getValue() {
            return aggregationPolicy.build(accumulator);
        }

        public Long getFirstTimeMillis() {
            return firstTimeMillis;
        }
    }
    
}
