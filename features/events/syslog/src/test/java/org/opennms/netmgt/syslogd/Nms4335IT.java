/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.ipc.sink.mock.MockMessageDispatcherFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.syslogd.api.SyslogConnection;
import org.opennms.netmgt.syslogd.api.SyslogMessageLogDTO;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass=MockDatabase.class)
@Transactional
public class Nms4335IT implements InitializingBean {

    private SyslogdConfigFactory m_config;

    private Syslogd m_syslogd;

    @Autowired
    private MockEventIpcManager m_eventIpcManager;

    @Autowired
    private DistPollerDao m_distPollerDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true, "TRACE");

        InputStream stream = null;
        try {
            final String config = "<?xml version=\"1.0\"?> \n" + 
            		"<syslogd-configuration> \n" + 
            		"    <configuration \n" + 
            		"            syslog-port=\"10514\" \n" + 
            		"            listen-address=\"127.0.0.1\" \n" +
            		"            new-suspect-on-message=\"false\" \n" + 
            		"            forwarding-regexp=\"^((.+?) (.*))\\n?$\" \n" + 
            		"            matching-group-host=\"2\" \n" + 
            		"            matching-group-message=\"3\" \n" + 
            		"            discard-uei=\"DISCARD-MATCHING-MESSAGES\" \n" + 
            		"            /> \n" + 
            		"\n" + 
            		"    <!-- Use the following to convert UEI ad-hoc --> \n" + 
            		"    <ueiList> \n" + 
            		"        <ueiMatch> \n" + 
            		"            <match type=\"substr\" expression=\"CRISCO\"/> \n" + 
            		"            <uei>CISCO</uei> \n" + 
            		"        </ueiMatch> \n" + 
            		"        <ueiMatch> \n" + 
            		"            <match type=\"regex\" expression=\".*su:auth.*authentication failure.*\"/> \n" + 
            		"            <uei>uei.opennms.org/syslog/pam/su/suFailure</uei> \n" + 
            		"        </ueiMatch> \n" + 
            		"        <!-- Use the following to discard a syslog message without ever creating an event for it. \n" + 
            		"             If you change the value of \"discard-uei\" above, you must change the UEI used here to match. --> \n" + 
            		"        <ueiMatch> \n" + 
            		"            <match type=\"substr\" expression=\"JUNK\"/> \n" + 
            		"            <uei>DISCARD-MATCHING-MESSAGES</uei> \n" + 
            		"        </ueiMatch> \n" + 
            		"    </ueiList> \n" + 
            		"\n" + 
            		"    <!-- Use the following to remove a syslog message from the event-trail --> \n" + 
            		"\n" + 
            		"    <hideMessage> \n" + 
            		"        <hideMatch> \n" + 
            		"            <match type=\"substr\" expression=\"SECRET\"/> \n" + 
            		"        </hideMatch> \n" + 
            		"        <hideMatch> \n" + 
            		"            <match type=\"regex\" expression=\".*(double|triple)secret.*\"/> \n" + 
            		"        </hideMatch> \n" + 
            		"    </hideMessage> \n" + 
            		"\n" + 
            		"</syslogd-configuration>\n";
            
            stream = new ByteArrayInputStream(config.getBytes());
            m_config = new SyslogdConfigFactory(stream);

            m_syslogd = new Syslogd();

            SyslogReceiverJavaNetImpl receiver = new SyslogReceiverJavaNetImpl(m_config);
            receiver.setDistPollerDao(m_distPollerDao);
            receiver.setMessageDispatcherFactory(new MockMessageDispatcherFactory<>());

            m_syslogd.setSyslogReceiver(receiver);
            m_syslogd.init();

        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoErrorOrGreater();
        m_eventIpcManager.getEventAnticipator().reset();
    }

    @Test
    public void testAuthFailureShouldLog() throws Exception {
        doMessageTest("Jan 7 12:42:46 192.168.0.1 su[25856]: pam_unix(su:auth): authentication failure; logname=jeffg uid=1004 euid=0 tty=pts/1 ruser=jeffg rhost= user=root",
                      "192.168.0.1",
                      "uei.opennms.org/syslog/pam/su/suFailure",
                      "pam_unix(su:auth): authentication failure; logname=jeffg uid=1004 euid=0 tty=pts/1 ruser=jeffg rhost= user=root");
    }

    @Test
    public void testAuthFailureShouldNotLog() throws Exception {
        doMessageTest("Jan 7 12:42:48 192.168.0.1 su[25856]: pam_authenticate: Authentication failure",
                      "192.168.0.1",
                      "uei.opennms.org/syslogd/system/Debug",
                      "pam_authenticate: Authentication failure");
    }
    
    /**
     * Send a raw syslog message and expect a given event as a result
     * 
     * @param testPDU The raw syslog message as it would appear on the wire (just the UDP payload)
     * @param expectedHost The host from which the event should be resolved as originating
     * @param expectedUEI The expected UEI of the resulting event
     * @param expectedLogMsg The expected contents of the logmsg for the resulting event 
     * 
     * @throws UnknownHostException 
     * @throws InterruptedException 
     * @throws ExecutionException 
     */
    private List<Event> doMessageTest(String testPDU, String expectedHost, String expectedUEI, String expectedLogMsg) throws UnknownHostException, InterruptedException, ExecutionException {
        SyslogdTestUtils.startSyslogdGracefully(m_syslogd);
        
        final EventBuilder expectedEventBldr = new EventBuilder(expectedUEI, "syslogd");
        expectedEventBldr.setInterface(addr(expectedHost));
        expectedEventBldr.setLogDest("logndisplay");
        expectedEventBldr.setLogMessage(expectedLogMsg);
    
        m_eventIpcManager.getEventAnticipator().anticipateEvent(expectedEventBldr.getEvent());

        final SyslogSinkConsumer syslogSinkConsumer = new SyslogSinkConsumer(new MetricRegistry());
        syslogSinkConsumer.setDistPollerDao(m_distPollerDao);
        syslogSinkConsumer.setSyslogdConfig(m_config);
        syslogSinkConsumer.setEventForwarder(m_eventIpcManager);

        final SyslogSinkModule syslogSinkModule = syslogSinkConsumer.getModule();

        final SyslogClient sc = new SyslogClient(null, 10, SyslogClient.LOG_DAEMON, addr("127.0.0.1"));
        final DatagramPacket pkt = sc.getPacket(SyslogClient.LOG_DEBUG, testPDU);

        final SyslogMessageLogDTO messageLog = syslogSinkModule.toMessageLog(new SyslogConnection(pkt, false));
        syslogSinkConsumer.handleMessage(messageLog);

        m_eventIpcManager.getEventAnticipator().verifyAnticipated(5000,0,0,0,0);
        final Event receivedEvent = m_eventIpcManager.getEventAnticipator().getAnticipatedEventsReceived().get(0);
        assertEquals("Log messages do not match", expectedLogMsg, receivedEvent.getLogmsg().getContent());
        
        return m_eventIpcManager.getEventAnticipator().getAnticipatedEventsReceived();
    }
}
