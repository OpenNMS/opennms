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

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
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
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.eventd.Eventd;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.events.api.support.TcpEventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
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
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-eventUtil.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/mockMessageDispatcherFactory.xml",
        "classpath:/overrideEventdPort.xml",
        "classpath:/syslogdTest.xml"
})
@JUnitConfigurationEnvironment(systemProperties = { "io.netty.leakDetectionLevel=ADVANCED" })
@JUnitTemporaryDatabase
public class SyslogdLoadIT implements InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(SyslogdLoadIT.class);

    private EventCounter m_eventCounter;

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Autowired
    private Eventd m_eventd;

    @Autowired
    private SyslogdConfig m_config;

    @Autowired
    @Qualifier("eventdPort")
    private int m_eventdPort;

    @Autowired
    private MockMessageDispatcherFactory<SyslogConnection, SyslogMessageLogDTO> m_messageDispatcherFactory;

    private Syslogd m_syslogd;

    private SyslogSinkConsumer m_syslogSinkConsumer;

    private SyslogSinkModule m_syslogSinkModule;

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

        m_syslogSinkConsumer = new SyslogSinkConsumer(new MetricRegistry());
        m_syslogSinkConsumer.setDistPollerDao(m_distPollerDao);
        m_syslogSinkConsumer.setSyslogdConfig(m_config);
        m_syslogSinkConsumer.setEventForwarder(m_eventIpcManager);
        m_syslogSinkModule = m_syslogSinkConsumer.getModule();
        m_messageDispatcherFactory.setConsumer(m_syslogSinkConsumer);
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoErrorOrGreater();
        if (m_syslogd != null) {
            m_syslogd.stop();
        }
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
        // Update the beans with the new config.
        if (m_syslogSinkConsumer != null) {
            m_syslogSinkConsumer.setSyslogdConfig(m_config);
            m_syslogSinkModule = m_syslogSinkConsumer.getModule();
        }
    }

    private void startSyslogdJavaNet() throws Exception {
        m_syslogd = new Syslogd();
        SyslogReceiverJavaNetImpl receiver = new SyslogReceiverJavaNetImpl(m_config);
        receiver.setDistPollerDao(m_distPollerDao);
        receiver.setMessageDispatcherFactory(m_messageDispatcherFactory);
        m_syslogd.setSyslogReceiver(receiver);
        m_syslogd.init();
        SyslogdTestUtils.startSyslogdGracefully(m_syslogd);
    }

    private void startSyslogdCamelNetty() throws Exception {
        m_syslogd = new Syslogd();
        SyslogReceiverCamelNettyImpl receiver = new SyslogReceiverCamelNettyImpl(m_config);
        receiver.setDistPollerDao(m_distPollerDao);
        receiver.setMessageDispatcherFactory(m_messageDispatcherFactory);
        m_syslogd.setSyslogReceiver(receiver);
        m_syslogd.init();
        SyslogdTestUtils.startSyslogdGracefully(m_syslogd);
    }

    @Test
    @Transactional
    public void testSyslogConnectionHandlerDefaultImpl() throws Exception {

        final int eventCount = 100;

        List<Integer> foos = new ArrayList<>();

        for (int i = 0; i < eventCount; i++) {
            int eventNum = Double.valueOf(Math.random() * 10000).intValue();
            foos.add(eventNum);
        }

        m_eventCounter.setAnticipated(eventCount);

        String testPduFormat = "2010-08-19 localhost foo%d: load test %d on tty1";
        SyslogClient sc = new SyslogClient(null, 10, SyslogClient.LOG_USER, addr("127.0.0.1"));

        // Test by directly invoking the SyslogConnection task
        System.err.println("Starting to send packets");
        final long start = System.currentTimeMillis();
        for (int i = 0; i < eventCount; i++) {
            int foo = foos.get(i);
            DatagramPacket pkt = sc.getPacket(SyslogClient.LOG_DEBUG, String.format(testPduFormat, foo, foo));
            SyslogMessageLogDTO messageLog = m_syslogSinkModule.toMessageLog(new SyslogConnection(pkt, false));
            m_syslogSinkConsumer.handleMessage(messageLog);
        }

        long mid = System.currentTimeMillis();
        System.err.println(String.format("Sent %d packets in %d milliseconds", eventCount, mid - start));

        m_eventCounter.waitForFinish(60000);
        long end = System.currentTimeMillis();

        System.err.println(String.format("Events expected: %d, events received: %d", eventCount, m_eventCounter.getCount()));
        final long total = (end - start);
        final double eventsPerSecond = (eventCount * 1000.0 / total);
        System.err.println(String.format("total time: %d, wait time: %d, events per second: %8.4f", total, (end - mid), eventsPerSecond));

        assertEquals(eventCount, m_eventCounter.getCount());
    }

    @Test
    @Transactional
    public void testSyslogReceiverJavaNet() throws Exception {
        startSyslogdJavaNet();
        doTestSyslogReceiver();
    }

    @Test
    @Transactional
    public void testSyslogReceiverCamelNetty() throws Exception {
        startSyslogdCamelNetty();
        // Wait for the Camel context to start
        await().atMost(10, TimeUnit.SECONDS).pollInterval(200, TimeUnit.MILLISECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return ((SyslogReceiverCamelNettyImpl)m_syslogd.getSyslogReceiver()).isStarted();
            }
        });
        doTestSyslogReceiver();
    }

    public void doTestSyslogReceiver() throws Exception {
        final int eventCount = 100;
        
        List<Integer> foos = new ArrayList<>();

        for (int i = 0; i < eventCount; i++) {
            int eventNum = Double.valueOf(Math.random() * 10000).intValue();
            foos.add(eventNum);
        }

        m_eventCounter.setAnticipated(eventCount);

        String testPduFormat = "2010-08-19 localhost foo%d: load test %d on tty1";
        SyslogClient sc = new SyslogClient(null, 10, SyslogClient.LOG_USER, addr("127.0.0.1"));

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

        /*
        // Test by sending over an NIO channel
        SocketAddress address = new InetSocketAddress(InetAddressUtils.getLocalHostAddress(), SyslogClient.PORT);
        final DatagramChannel channel = DatagramChannel.open();
        final ByteBuffer buffer = ByteBuffer.allocate(SyslogReceiverNioThreadPoolImpl.MAX_PACKET_SIZE);
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
        */

        long mid = System.currentTimeMillis();
        System.err.println(String.format("Sent %d packets in %d milliseconds", eventCount, mid - start));

        m_eventCounter.waitForFinish(30000);
        long end = System.currentTimeMillis();

        System.err.println(String.format("Events expected: %d, events received: %d", eventCount, m_eventCounter.getCount()));
        final long total = (end - start);
        final double eventsPerSecond = (eventCount * 1000.0 / total);
        System.err.println(String.format("total time: %d, wait time: %d, events per second: %8.4f", total, (end - mid), eventsPerSecond));

        assertEquals(eventCount, m_eventCounter.getCount());
    }

    @Test
    @Transactional
    public void testRfcSyslog() throws Exception {
        loadSyslogConfiguration("/etc/syslogd-rfc-configuration.xml");

        startSyslogdJavaNet();

        m_eventCounter.anticipate();

        InetAddress address = InetAddress.getLocalHost();

        // handle an invalid packet
        byte[] bytes = "<34>1 2010-08-19T22:14:15.000Z localhost - - - - \uFEFFfoo0: load test 0 on tty1\0".getBytes();
        DatagramPacket pkt = new DatagramPacket(bytes, bytes.length, address, SyslogClient.PORT);
        SyslogMessageLogDTO messageLog = m_syslogSinkModule.toMessageLog(new SyslogConnection(pkt, false));
        m_syslogSinkConsumer.handleMessage(messageLog);

        // handle a valid packet
        bytes = "<34>1 2003-10-11T22:14:15.000Z plonk -ev/pts/8\0".getBytes();
        pkt = new DatagramPacket(bytes, bytes.length, address, SyslogClient.PORT);
        messageLog = m_syslogSinkModule.toMessageLog(new SyslogConnection(pkt, false));
        m_syslogSinkConsumer.handleMessage(messageLog);

        m_eventCounter.waitForFinish(120000);
        
        assertEquals(1, m_eventCounter.getCount());
    }

    @Test
    @Transactional
    public void testNGSyslog() throws Exception {
        loadSyslogConfiguration("/etc/syslogd-syslogng-configuration.xml");

        startSyslogdJavaNet();

        m_eventCounter.anticipate();

        InetAddress address = InetAddress.getLocalHost();

        // handle an invalid packet
        byte[] bytes = "<34>main: 2010-08-19 localhost foo0: load test 0 on tty1\0".getBytes();
        DatagramPacket pkt = new DatagramPacket(bytes, bytes.length, address, SyslogClient.PORT);
        SyslogMessageLogDTO messageLog = m_syslogSinkModule.toMessageLog(new SyslogConnection(pkt, false));
        m_syslogSinkConsumer.handleMessage(messageLog);

        // handle a valid packet
        bytes = "<34>monkeysatemybrain!\0".getBytes();
        pkt = new DatagramPacket(bytes, bytes.length, address, SyslogClient.PORT);
        messageLog = m_syslogSinkModule.toMessageLog(new SyslogConnection(pkt, false));
        m_syslogSinkConsumer.handleMessage(messageLog);

        m_eventCounter.waitForFinish(120000);
        
        assertEquals(1, m_eventCounter.getCount());
    }

    @Test
    @Transactional
    public void testEventd() throws Exception {
        m_eventd.start();

        EventProxy ep = createEventProxy();

        Log eventLog = new Log();
        Events events = new Events();
        eventLog.setEvents(events);
        
        int eventCount = 10000;
        m_eventCounter.setAnticipated(eventCount);

        for (int i = 0; i < eventCount; i++) {
            int eventNum = Double.valueOf(Math.random() * 300).intValue();
            String expectedUei = "uei.example.org/syslog/loadTest/foo" + eventNum;
            final EventBuilder eb = new EventBuilder(expectedUei, "SyslogdLoadTest");

            Event thisEvent = eb.setInterface(addr("127.0.0.1"))
                .setLogDest("logndisplay")
                .setLogMessage("A load test has been received as a Syslog Message")
                .getEvent();
//            LOG.debug("event = {}", thisEvent);
            events.addEvent(thisEvent);
        }

        long start = System.currentTimeMillis();
        ep.send(eventLog);
        long mid = System.currentTimeMillis();
        // wait up to 2 minutes for the events to come through
        m_eventCounter.waitForFinish(120000);
        long end = System.currentTimeMillis();

        assertEquals(eventCount, m_eventCounter.getCount());

        m_eventd.stop();

        final long total = (end - start);
        final double eventsPerSecond = (eventCount * 1000.0 / total);
        System.err.println(String.format("total time: %d, wait time: %d, events per second: %8.4f", total, (end - mid), eventsPerSecond));
    }

    private EventProxy createEventProxy() throws UnknownHostException {
        return new TcpEventProxy(new InetSocketAddress(InetAddressUtils.ONE_TWENTY_SEVEN, m_eventdPort), TcpEventProxy.DEFAULT_TIMEOUT);
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
