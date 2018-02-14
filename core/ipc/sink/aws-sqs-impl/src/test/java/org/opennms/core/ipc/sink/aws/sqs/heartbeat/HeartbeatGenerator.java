/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.sink.aws.sqs.heartbeat;

import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.core.ipc.sink.api.SyncDispatcher;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.google.common.util.concurrent.RateLimiter;

/**
 * The Class HeartbeatGenerator.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class HeartbeatGenerator {

    /** The thread. */
    Thread thread;

    /** The dispatcher. */
    final SyncDispatcher<Heartbeat> dispatcher;

    /** The rate. */
    final double rate;

    /** The stopped. */
    final AtomicBoolean stopped = new AtomicBoolean(false);

    /** The sent meter. */
    private final Meter sentMeter;

    /** The send timer. */
    private final Timer sendTimer;

    /**
     * Instantiates a new heartbeat generator.
     *
     * @param dispatcher the dispatcher
     * @param rate the rate
     */
    public HeartbeatGenerator(SyncDispatcher<Heartbeat> dispatcher, double rate) {
        this.dispatcher = dispatcher;
        this.rate = rate;
        MetricRegistry metrics = new MetricRegistry();
        this.sentMeter = metrics.meter("sent");
        this.sendTimer = metrics.timer("send");
    }

    /**
     * Instantiates a new heartbeat generator.
     *
     * @param dispatcher the dispatcher
     * @param rate the rate
     * @param sentMeter the sent meter
     * @param sendTimer the send timer
     */
    public HeartbeatGenerator(SyncDispatcher<Heartbeat> dispatcher, double rate, Meter sentMeter, Timer sendTimer) {
        this.dispatcher = dispatcher;
        this.rate = rate;
        this.sentMeter = sentMeter;
        this.sendTimer = sendTimer;
    }

    /**
     * Start.
     */
    public synchronized void start() {
        stopped.set(false);
        final RateLimiter rateLimiter = RateLimiter.create(rate);
        thread = new Thread(new Runnable() {
            @Override
            public void run() {

                while(!stopped.get()) {
                    rateLimiter.acquire();
                    try (Context ctx = sendTimer.time()) {
                        dispatcher.send(new Heartbeat());
                        sentMeter.mark();
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Stop.
     *
     * @throws InterruptedException the interrupted exception
     */
    public synchronized void stop() throws InterruptedException {
        stopped.set(true);
        if (thread != null) {
            thread.join();
            thread = null;
        }
    }
}