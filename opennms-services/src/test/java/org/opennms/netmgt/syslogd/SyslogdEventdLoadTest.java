/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.syslogd;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.config.syslogd.HideMessage;
import org.opennms.netmgt.config.syslogd.Match;
import org.opennms.netmgt.config.syslogd.UeiList;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.eventd.Eventd;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.utils.TcpEventProxy;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Events;
import org.opennms.netmgt.xml.event.Log;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;


@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/testEventProxy.xml"
})
@JUnitTemporaryDatabase
public class SyslogdEventdLoadTest {

    private EventCounter m_eventCounter;
    private static final String MATCH_PATTERN = "^.*\\s(19|20)\\d\\d([-/.])(0[1-9]|1[012])\\2(0[1-9]|[12][0-9]|3[01])(\\s+)(\\S+)(\\s)(\\S.+)";
    private static final int HOST_GROUP = 6;
    private static final int MESSAGE_GROUP = 8;
    private static final HideMessage HIDE_MESSAGE = new HideMessage();
    private static final String DISCARD_UEI = "DISCARD-MATCHING-MESSAGES";
    private static final UeiList UEI_LIST = new UeiList();

    @Autowired
    @Qualifier(value="eventIpcManagerImpl")
    private EventIpcManager m_eventIpcManager;

    @Autowired
    private Eventd m_eventd;
    
    private Syslogd m_syslogd;

    static {
        UeiMatch ueiMatch;
        Match match;
        for (int i = 0; i < 10000; i++) {
            /* <ueiMatch>
             *   <match type="regex" expression=".*foo0: .*load test (\S+) on ((pts\/\d+)|(tty\d+)).*"/><uei>uei.example.org/syslog/loadTest/foo0</uei>
             * </ueiMatch> */

            ueiMatch = new UeiMatch();
            match = new Match();
            match.setType("regex");
            match.setExpression(String.format(".*foo%d: .*load test (\\S+) on ((pts\\/\\d+)|(tty\\d+)).*", i));
            ueiMatch.setMatch(match);
            ueiMatch.setUei(String.format("uei.example.org/syslog/loadTest/foo%d", i));
            UEI_LIST.addUeiMatch(ueiMatch);
        }
    }

    @Before
    public void setUp() throws Exception {
    	MockLogAppender.setupLogging(true, "DEBUG");

        loadSyslogConfiguration("/etc/syslogd-loadtest-configuration.xml");

        m_eventCounter = new EventCounter();
        this.m_eventIpcManager.addEventListener(m_eventCounter);
    }

    @After
    public void tearDown() throws Exception {
        if (m_syslogd != null) {
            m_syslogd.stop();
        }
        MockLogAppender.assertNotGreaterOrEqual(Level.FATAL);
    }

    private void loadSyslogConfiguration(final String configuration) throws IOException, MarshalException, ValidationException {
        InputStream stream = null;
        try {
            stream = ConfigurationTestUtils.getInputStreamForResource(this, configuration);
            SyslogdConfigFactory cf = new SyslogdConfigFactory(stream);
            SyslogdConfigFactory.setInstance(cf);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    private void startSyslogdGracefully() {
        //ConvertToEvent.invalidate();
        try {
            m_syslogd = new Syslogd();
            m_syslogd.init();
            m_syslogd.start();
        } catch (UndeclaredThrowableException ute) {
            if (ute.getCause() instanceof BindException) {
                LogUtils.warnf(this, ute, "received a bind exception");
                // continue, this was expected
            } else {
                throw ute;
            }
        }
    }

    @Test
    @Transactional
    public void testDefaultSyslogd() throws Exception {
        startSyslogdGracefully();

        int eventCount = 100;
        
        List<Integer> foos = new ArrayList<Integer>();

        for (int i = 0; i < eventCount; i++) {
            int eventNum = Double.valueOf(Math.random() * 10000).intValue();
            foos.add(eventNum);
        }

        m_eventCounter.setAnticipated(eventCount);

        long start = System.currentTimeMillis();
        final String testPduFormat = "2010-08-19 localhost foo%d: load test %d on tty1";
        final SyslogClient sc = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
        for (int i = 0; i < eventCount; i++) {
            final int foo = foos.get(i);
            //DatagramPacket pkt = sc.getPacket(SyslogClient.LOG_DEBUG, String.format(testPduFormat, foo, foo));
            //Thread worker = new Thread(new SyslogConnection(pkt, MATCH_PATTERN, HOST_GROUP, MESSAGE_GROUP, UEI_LIST, HIDE_MESSAGE, DISCARD_UEI), SyslogConnection.class.getSimpleName());
            //worker.start();
            new Thread() {
              public void run() {
                sc.syslog(SyslogClient.LOG_DEBUG, String.format(testPduFormat, foo, foo));
              }
            }.start();
        }

        long mid = System.currentTimeMillis();
        m_eventCounter.waitForFinish(120000);
        long end = System.currentTimeMillis();
        
        final long total = (end - start);
        final double eventsPerSecond = (eventCount * 1000.0 / total);
        System.err.println(String.format("total time: %d, wait time: %d, events per second: %8.4f", total, (end - mid), eventsPerSecond));
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

            Event thisEvent = eb.setInterface("127.0.0.1")
                .setLogDest("logndisplay")
                .setLogMessage("A load test has been received as a Syslog Message")
                .getEvent();
//            LogUtils.debugf(this, "event = %s", thisEvent);
            events.addEvent(thisEvent);
        }

        long start = System.currentTimeMillis();
        ep.send(eventLog);
        long mid = System.currentTimeMillis();
        // wait up to 2 minutes for the events to come through
        m_eventCounter.waitForFinish(120000);
        long end = System.currentTimeMillis();

        m_eventd.stop();

        final long total = (end - start);
        final double eventsPerSecond = (eventCount * 1000.0 / total);
        System.err.println(String.format("total time: %d, wait time: %d, events per second: %8.4f", total, (end - mid), eventsPerSecond));
    }

    private static EventProxy createEventProxy() throws UnknownHostException {
        /*
         * Rather than defaulting to localhost all the time, give an option in properties
         */
        String proxyHostName = "127.0.0.1";
        String proxyHostPort = "5837";
        String proxyHostTimeout = String.valueOf(TcpEventProxy.DEFAULT_TIMEOUT);
        InetAddress proxyAddr = null;
        EventProxy proxy = null;

        proxyAddr = InetAddress.getByName(proxyHostName);

        if (proxyAddr == null) {
        	proxy = new TcpEventProxy();
        } else {
            proxy = new TcpEventProxy(new InetSocketAddress(proxyAddr, Integer.parseInt(proxyHostPort)), Integer.parseInt(proxyHostTimeout));
        }
        return proxy;
    }

    public static class EventCounter implements EventListener {
        private AtomicInteger m_eventCount = new AtomicInteger(0);
        private int m_expectedCount = 0;

        public String getName() {
            return "eventCounter";
        }

        // Me love you, long time.
        public void waitForFinish(final long time) {
            final long start = System.currentTimeMillis();
            while (this.getCount() < m_expectedCount) {
                if (System.currentTimeMillis() - start > time) {
                    LogUtils.warnf(this, "waitForFinish timeout (%s) reached", time);
                    break;
                }
                try {
                    Thread.sleep(50);
                } catch (final InterruptedException e) {
                    LogUtils.warnf(this, e, "thread was interrupted while sleeping");
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

        public void onEvent(final Event e) {
            final int current = m_eventCount.incrementAndGet();
            if (current % 100 == 0) {
                System.err.println(current + " < " + m_expectedCount);
            }
        }

    }
}
