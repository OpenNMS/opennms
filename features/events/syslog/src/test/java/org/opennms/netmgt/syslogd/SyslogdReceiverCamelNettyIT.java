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
