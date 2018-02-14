/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.syslogd;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.ipc.sink.common.ThreadLockingDispatcherFactory;
import org.opennms.core.ipc.sink.common.ThreadLockingSyncDispatcher;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.syslogd.api.SyslogConnection;

import com.google.common.util.concurrent.RateLimiter;

import io.netty.util.ResourceLeakDetector;

public class SyslogdReceiverCamelNettyIT {

    public static final String NETTY_LEAK_DETECTION_LEVEL = "io.netty.leakDetectionLevel";

    private static String s_oldLeakLevel;

    @BeforeClass
    public static void setSerializablePackages() {
        s_oldLeakLevel = System.getProperty(NETTY_LEAK_DETECTION_LEVEL);
        System.setProperty(NETTY_LEAK_DETECTION_LEVEL, ResourceLeakDetector.Level.PARANOID.toString());
    }

    @AfterClass
    public static void resetSerializablePackages() {
        if (s_oldLeakLevel == null) {
            System.clearProperty(NETTY_LEAK_DETECTION_LEVEL);
        } else {
            System.setProperty(NETTY_LEAK_DETECTION_LEVEL, s_oldLeakLevel);
        }
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoErrorOrGreater();
    }

    @Test(timeout=3 * 60 * 1000)
    public void testParallelismAndQueueing() throws UnknownHostException, InterruptedException, ExecutionException {
        final int NUM_GENERATORS = 3;
        final double MESSAGE_RATE_PER_GENERATOR = 1000.0;
        final int NUM_CONSUMER_THREADS = 8;
        final int MESSAGE_QUEUE_SIZE = 529;

        ThreadLockingDispatcherFactory<SyslogConnection> threadLockingDispatcherFactory = new ThreadLockingDispatcherFactory<>();
        ThreadLockingSyncDispatcher<SyslogConnection> syncDispatcher = threadLockingDispatcherFactory.getThreadLockingSyncDispatcher();
        CompletableFuture<Integer> future = syncDispatcher.waitForThreads(NUM_CONSUMER_THREADS);

        SyslogdConfig syslogdConfig = mock(SyslogdConfig.class);
        when(syslogdConfig.getSyslogPort()).thenReturn(SyslogClient.PORT);
        when(syslogdConfig.getNumThreads()).thenReturn(NUM_CONSUMER_THREADS);
        when(syslogdConfig.getQueueSize()).thenReturn(MESSAGE_QUEUE_SIZE);

        DistPollerDao distPollerDao = mock(DistPollerDao.class, Mockito.RETURNS_DEEP_STUBS);
        when(distPollerDao.whoami().getId()).thenReturn("");
        when(distPollerDao.whoami().getLocation()).thenReturn("");

        SyslogReceiverCamelNettyImpl syslogReceiver = new SyslogReceiverCamelNettyImpl(syslogdConfig);
        syslogReceiver.setMessageDispatcherFactory(threadLockingDispatcherFactory);
        syslogReceiver.setDistPollerDao(distPollerDao);
        syslogReceiver.run();

        // Fire up the syslog generators
        List<SyslogGenerator> generators = new ArrayList<>(NUM_GENERATORS);
        for (int k = 0; k < NUM_GENERATORS; k++) {
            SyslogGenerator generator = new SyslogGenerator(addr("127.0.0.1"), k, MESSAGE_RATE_PER_GENERATOR);
            generator.start();
            generators.add(generator);
        }

        // Wait until we have NUM_CONSUMER_THREADS locked
        future.get();

        // Now all of the threads are locked, and the queue is full
        // Let's continue generating traffic for a few seconds
        Thread.sleep(SECONDS.toMillis(10));

        // Verify that there aren't more than NUM_CONSUMER_THREADS waiting
        assertEquals(0, syncDispatcher.getNumExtraThreadsWaiting());

        // Release the producer threads
        syncDispatcher.release();

        // Stop the receiver
        syslogReceiver.stop();

        // Stop the generators
        for (int k = 0; k < NUM_GENERATORS; k++) {
            generators.get(k).stop();
        }
    }

    /**
     * Used to generate and sends syslog messages at a given rate.
     *
     * @author jwhite
     */
    public static class SyslogGenerator {
        Thread thread;
        
        final InetAddress targetHost;
        final double rate;
        final int id;
        final AtomicBoolean stopped = new AtomicBoolean(false);

        public SyslogGenerator(InetAddress targetHost, int id, double rate) {
            this.targetHost = targetHost;
            this.id = id;
            this.rate = rate;
        }

        public synchronized void start() {
            stopped.set(false);
            final RateLimiter rateLimiter = RateLimiter.create(rate);
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    final String testPduFormat = "2016-12-08 localhost gen%d: load test %d on tty1";
                    final SyslogClient sc = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG, targetHost);
                    int k = 0;
                    while(!stopped.get()) {
                        k++;
                        rateLimiter.acquire();
                        sc.syslog(SyslogClient.LOG_DEBUG, String.format(testPduFormat, id, k));
                    }
                }
            });
            thread.start();
        }

        public synchronized void stop() throws InterruptedException {
            stopped.set(true);
            if (thread != null) {
                thread.join();
                thread = null;
            }
        }
    }
}
