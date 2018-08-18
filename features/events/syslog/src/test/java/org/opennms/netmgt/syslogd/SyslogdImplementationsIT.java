/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.mock.MockMessageDispatcherFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.codahale.metrics.MetricRegistry;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-eventUtil.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/mockMessageDispatcherFactory.xml",
        "classpath:/applicationContext-syslogImplementations.xml"
})
@JUnitConfigurationEnvironment(systemProperties = { "io.netty.leakDetectionLevel=PARANOID" })
@JUnitTemporaryDatabase
public class SyslogdImplementationsIT implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(SyslogdImplementationsIT.class);

    private EventCounter m_eventCounter;

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @Autowired
    private MockMessageDispatcherFactory<SyslogConnection, SyslogMessageLogDTO> m_messageDispatcherFactory;

    @Autowired
    private SyslogdConfigFactory m_config;

    @Autowired
    @Qualifier("syslogReceiverJavaNet")
    private SyslogReceiver m_java;

    @Autowired
    @Qualifier("syslogReceiverCamelNetty")
    private SyslogReceiver m_netty;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "WARN");

        loadSyslogConfiguration("/etc/syslogd-loadtest-configuration.xml");

        m_eventCounter = new EventCounter();
        this.m_eventIpcManager.addEventListener(m_eventCounter);

        SyslogSinkConsumer consumer = new SyslogSinkConsumer(new MetricRegistry());
        consumer.setSyslogdConfig(m_config);
        consumer.setEventForwarder(m_eventIpcManager);
        m_messageDispatcherFactory.setConsumer(consumer);
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoErrorOrGreater();
    }

    private void loadSyslogConfiguration(final String configuration) throws IOException {
        InputStream stream = null;
        try {
            stream = ConfigurationTestUtils.getInputStreamForResource(this, configuration);
            m_config = new SyslogdConfigFactory(stream);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @Test(timeout=3*60*1000)
    @Transactional
    public void testCamelNettyReceiver() throws Exception {
        doTestSyslogd(m_netty);
    }

    @Test(timeout=3*60*1000)
    @Transactional
    public void testJavaNetReceiver() throws Exception {
        doTestSyslogd(m_java);
    }

    private void doTestSyslogd(SyslogReceiver receiver) throws Exception {
        Thread listener = new Thread(receiver);
        listener.start();
        Thread.sleep(3000);

        final int eventCount = 100;
        
        List<Integer> foos = new ArrayList<>();

        for (int i = 0; i < eventCount; i++) {
            int eventNum = Double.valueOf(Math.random() * 10000).intValue();
            foos.add(eventNum);
        }

        m_eventCounter.setAnticipated(eventCount);

        String testPduFormat = "2010-08-19 localhost foo%d: load test %d on tty1";
        /*
        SyslogClient sc = new SyslogClient(null, 10, SyslogClient.LOG_USER, addr("127.0.0.1"));
        // Test by directly invoking the SyslogConnection task
        System.err.println("Starting to send packets");
        final long start = System.currentTimeMillis();
        for (int i = 0; i < eventCount; i++) {
            int foo = foos.get(i);
            DatagramPacket pkt = sc.getPacket(SyslogClient.LOG_DEBUG, String.format(testPduFormat, foo, foo));
            WaterfallExecutor.waterfall(m_executorService, new SyslogConnection(pkt, m_config));
        }

        // Test by sending over a java.net socket
        final DatagramSocket socket = new DatagramSocket();
        System.err.println("Starting to send packets");
        final long start = System.currentTimeMillis();
        for (int i = 0; i < eventCount; i++) {
            int foo = foos.get(i);
            DatagramPacket pkt = sc.getPacket(SyslogClient.LOG_DEBUG, String.format(testPduFormat, foo, foo));
            socket.send(pkt);
        }
        socket.close();
        */

        // Test by sending over an NIO channel
        SocketAddress address = new InetSocketAddress(InetAddressUtils.getLocalHostAddress(), SyslogClient.PORT);
        final DatagramChannel channel = DatagramChannel.open();
        final ByteBuffer buffer = ByteBuffer.allocate(SyslogConnection.MAX_PACKET_SIZE);
        buffer.clear();
        System.err.println("Starting to send packets");
        final long start = System.currentTimeMillis();
        for (int i = 0; i < eventCount; i++) {
            int foo = foos.get(i);
            buffer.put(SyslogClient.getPacketPayload(SyslogClient.LOG_USER, null, SyslogClient.LOG_DEBUG, String.format(testPduFormat, foo, foo)));
            buffer.flip();
            channel.send(buffer, address);
            buffer.clear();
        }
        channel.close();

        long mid = System.currentTimeMillis();
        System.err.println(String.format("Sent %d packets in %d milliseconds", eventCount, mid - start));

        m_eventCounter.waitForFinish(120000);
        long end = System.currentTimeMillis();

        System.err.println(String.format("Events expected: %d, events received: %d", eventCount, m_eventCounter.getCount()));
        final long total = (end - start);
        final double eventsPerSecond = (eventCount * 1000.0 / total);
        System.err.println(String.format("total time: %d, wait time: %d, events per second: %8.4f", total, (end - mid), eventsPerSecond));

        listener.interrupt();
        listener.join();
    }

    public static class EventCounter implements EventListener {
        private AtomicInteger m_eventCount = new AtomicInteger(0);
        private int m_expectedCount = 0;

        @Override
        public String getName() {
            return "eventCounter";
        }

        // Me love you, long time.
        public void waitForFinish(final long time) {
            final long start = System.currentTimeMillis();
            while (this.getCount() < m_expectedCount) {
                if (System.currentTimeMillis() - start > time) {
                    LOG.warn("waitForFinish timeout ({}) reached", time);
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (final InterruptedException e) {
                    LOG.warn("thread was interrupted while sleeping", e);
                    Thread.currentThread().interrupt();
                }
            }
        }

        public void setAnticipated(final int eventCount) {
            m_expectedCount = eventCount;
        }

        public int getCount() {
            return m_eventCount.get();
        }

        public void anticipate() {
            m_expectedCount++;
        }

        @Override
        public void onEvent(final Event e) {
            final int current = m_eventCount.incrementAndGet();
            if (current % 100 == 0) {
                System.err.println(current + " out of " + m_expectedCount + " expected events received");
            }
        }

    }
}
