/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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

package org.opennms.features.minion.cluster.hazelcast.impl;

import java.io.Closeable;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.features.minion.cluster.api.Leader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ILock;

public class HazelcastLeader implements Leader, Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(HazelcastLeader.class);

    public static final String LOCK_NAME = "leadership";

    private final HazelcastInstance hazelcast;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "Hazelcast leadership");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * Registered listeners to notify about leadership change.
     */
    private final Set<Listener> listeners = Sets.newConcurrentHashSet();

    /**
     * Flag indicating this minion is participating in leadership election.
     */
    private final AtomicBoolean running = new AtomicBoolean(true);

    /**
     * The lock to hold by the elected leader.
     */
    private final ILock lock;

    /**
     * Future controlling the running leadership initiator.
     */
    private final Future<?> initiator;

    public HazelcastLeader(final HazelcastInstance hazelcast) {
        this.hazelcast = Objects.requireNonNull(hazelcast);

        // Get handle to distributed lock
        this.lock = this.hazelcast.getLock(LOCK_NAME);

        // Start the leadership initialisation
        this.initiator = this.executorService.submit(this);
    }

    @Override
    public boolean isLeader() {
        return this.lock.isLocked();
    }


    @Override
    public Closeable watch(final Listener listener) {
        this.listeners.add(listener);
        return () -> this.listeners.remove(listener);
    }

    @Override
    public void retire() {
        this.running.set(false);
        this.initiator.cancel(true);
    }

    @Override
    public void run() {
        while (this.running.get()) {
            try {
                // Race for leadership
                this.lock.lock();

                // Notify about gained leadership
                this.listeners.forEach(Listener::onGranted);

                // Stay in charge as long as possible
                Thread.sleep(Long.MAX_VALUE);

            } catch (final InterruptedException e) {
                // Ignored
            } catch (final Exception e) {
                // On exception, give up leadership and move on
                LOG.error("Exception during leadership selection", e);

            } finally {
                // Give up leadership
                this.lock.unlock();

                // Notify about lost leadership
                this.listeners.forEach(Listener::onGranted);
            }
        }
    }
}
