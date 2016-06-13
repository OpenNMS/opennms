/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.config.syslogd.UeiMatch;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/applicationContext-eventDaemon.xml",
        "classpath:/META-INF/opennms/applicationContext-eventUtil.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(dirtiesContext=false,tempDbClass=MockDatabase.class)
@Transactional
public class SyslogdIT implements InitializingBean {
    
    final String m_localhost = "127.0.0.1";

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
        MockLogAppender.setupLogging();
        MockLogAppender.resetEvents();

        InputStream stream = null;
        try {
            stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-configuration.xml");
            m_config = new SyslogdConfigFactory(stream);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }

        // Verify that the test syslogd-configuration.xml file was loaded
        boolean foundBeer = false;
        boolean foundMalt = false;
        assertEquals(10514, m_config.getSyslogPort());
        for (final UeiMatch match : m_config.getUeiList().getUeiMatch()) {
            if (match.getProcessMatch() != null) {
                if (!foundBeer && "beerd".equals(match.getProcessMatch().getExpression())) {
                    foundBeer = true;
                } else if (!foundMalt && "maltd".equals(match.getProcessMatch().getExpression())) {
                    foundMalt = true;
                }
            }
        }
        assertTrue(foundBeer);
        assertTrue(foundMalt);

        m_syslogd = new Syslogd();
        SyslogReceiverJavaNetImpl receiver = new SyslogReceiverJavaNetImpl(m_config);
        receiver.setDistPollerDao(m_distPollerDao);
        receiver.setSyslogConnectionHandlers(new SyslogConnectionHandlerDefaultImpl());
        m_syslogd.setSyslogReceiver(receiver);
        m_syslogd.init();
        SyslogdTestUtils.startSyslogdGracefully(m_syslogd);
    }

    @After
    public void tearDown() throws Exception {
        m_syslogd.stop();
        MockLogAppender.assertNoErrorOrGreater();
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
        final EventBuilder expectedEventBldr = new EventBuilder(expectedUEI, "syslogd");
        expectedEventBldr.setInterface(addr(expectedHost));
        expectedEventBldr.setLogDest("logndisplay");
        expectedEventBldr.setLogMessage(expectedLogMsg);
    
        final EventAnticipator ea = new EventAnticipator();
        m_eventIpcManager.addEventListener(ea);
        ea.anticipateEvent(expectedEventBldr.getEvent());
        
        final SyslogClient sc = new SyslogClient(null, 10, SyslogClient.LOG_DAEMON, addr(m_localhost));
        final DatagramPacket pkt = sc.getPacket(SyslogClient.LOG_DEBUG, testPDU);
        new SyslogConnectionHandlerDefaultImpl().handleSyslogConnection(new SyslogConnection(pkt, m_config, m_distPollerDao.whoami().getId()));

        ea.verifyAnticipated(5000,0,0,0,0);
        final Event receivedEvent = ea.getAnticipatedEventsRecieved().get(0);
        assertEquals("Log messages do not match", expectedLogMsg, receivedEvent.getLogmsg().getContent());
        
        return ea.getAnticipatedEventsRecieved();
    }
    
	private List<Event> doMessageTest(String testPDU, String expectedHost, String expectedUEI, String expectedLogMsg, Map<String,String> expectedParams) throws UnknownHostException, InterruptedException, ExecutionException {
    	final List<Event> receivedEvents = doMessageTest(testPDU, expectedHost, expectedUEI, expectedLogMsg);

        final Map<String,String> actualParms = new HashMap<String,String>();
        for (final Parm actualParm : receivedEvents.get(0).getParmCollection()) {
            actualParms.put(actualParm.getParmName(), actualParm.getValue().getContent());
        }

        for (final String expectedKey : expectedParams.keySet()) {
        	final String expectedValue = expectedParams.get(expectedKey);
            assertTrue("Actual event does not have a parameter called " + expectedKey, actualParms.containsKey(expectedKey));
            assertEquals("Actual event has a parameter called " + expectedKey + " but its value does not match", expectedValue, actualParms.get(expectedKey));
        }
        return receivedEvents;
    }

    @Test
    public void testMessaging() throws UnknownHostException {
        // More of an integrations test
        // relies on you reading some of the logging....

        final SyslogClient s = new SyslogClient(null, 0, SyslogClient.LOG_DAEMON, addr(m_localhost));
        s.syslog(SyslogClient.LOG_ERR, "Hello.");
    }

    @Test
    public void testMyPatternsSyslogNG() throws UnknownHostException {
        final SyslogClient s = new SyslogClient(null, 10, SyslogClient.LOG_DAEMON, addr(m_localhost));
        s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 host.domain.com A SyslogNG style message");
    }

    @Test
    public void testRegexSeverityMatch() throws Exception {
        MockLogAppender.setupLogging(true, "TRACE");
        String localhost = m_localhost;
        final String testPDU = "2007-01-01 127.0.0.1 beer - Not just for dinner anymore";
        final String testUEI = "uei.opennms.org/tests/syslogd/nonMessageMatch/severityOnly";
        final String testMsg = "beer - Not just for dinner anymore";
    
        final EventBuilder expectedEventBldr = new EventBuilder(testUEI, "syslogd");
        expectedEventBldr.setInterface(addr(localhost));
        expectedEventBldr.setLogDest("logndisplay");
        expectedEventBldr.setLogMessage(testMsg);
        
        final EventAnticipator ea = new EventAnticipator();
        m_eventIpcManager.addEventListener(ea);
        ea.anticipateEvent(expectedEventBldr.getEvent());
        
        final SyslogClient s = new SyslogClient(null, 10, SyslogClient.LOG_DAEMON, addr(m_localhost));
        s.syslog(SyslogClient.LOG_CRIT, testPDU);

        ea.verifyAnticipated(10000, 0, 0, 0, 0);
    }

    @Test
    public void testRegexFacilitySeverityProcessMatch() throws Exception {
        MockLogAppender.setupLogging(true, "TRACE");
        String localhost = m_localhost;
        final String testPDU = "2007-01-01 127.0.0.1 maltd: beer - Not just for lunch anymore";
        final String testUEI = "uei.opennms.org/tests/syslogd/nonMessageMatch/facilitySeverityProcess";
        final String testMsg = "beer - Not just for lunch anymore";
    
        final EventBuilder expectedEventBldr = new EventBuilder(testUEI, "syslogd");
        expectedEventBldr.setInterface(addr(localhost));
        expectedEventBldr.setLogDest("logndisplay");
        expectedEventBldr.setLogMessage(testMsg);
        
        expectedEventBldr.addParam("process", "maltd");
        expectedEventBldr.addParam("service", "local1");
        expectedEventBldr.addParam("severity", "Warning");
    
        final EventAnticipator ea = new EventAnticipator();
        m_eventIpcManager.addEventListener(ea);
        ea.anticipateEvent(expectedEventBldr.getEvent());
        
        final SyslogClient s = new SyslogClient("maltd", 10, SyslogClient.LOG_LOCAL1, addr(m_localhost));
        s.syslog(SyslogClient.LOG_WARNING, testPDU);
    
        ea.verifyAnticipated(5000, 0, 0, 0, 0);
    }
    
    @Test
    public void testRegexFacilitySeverityMatch() throws Exception {
        MockLogAppender.setupLogging(true, "TRACE");
        final String localhost = m_localhost;
        final String testPDU = "2007-01-01 127.0.0.1 beer - Not just for lunch anymore";
        final String testUEI = "uei.opennms.org/tests/syslogd/nonMessageMatch/facilitySeverity";
        final String testMsg = "beer - Not just for lunch anymore";
    
        final EventBuilder expectedEventBldr = new EventBuilder(testUEI, "syslogd");
        expectedEventBldr.setInterface(addr(localhost));
        expectedEventBldr.setLogDest("logndisplay");
        expectedEventBldr.setLogMessage(testMsg);
        
        expectedEventBldr.addParam("service", "local1");
        expectedEventBldr.addParam("severity", "Warning");
    
        final EventAnticipator ea = new EventAnticipator();
        m_eventIpcManager.addEventListener(ea);
        ea.anticipateEvent(expectedEventBldr.getEvent());
        
        final SyslogClient s = new SyslogClient(null, 10, SyslogClient.LOG_LOCAL1, addr(m_localhost));
        s.syslog(SyslogClient.LOG_WARNING, testPDU);
    
        ea.verifyAnticipated(5000, 0, 0, 0, 0);
    }
    
    @Test
    public void testRegexFacilityMatch() throws Exception {
        MockLogAppender.setupLogging(true, "TRACE");
        final String localhost = m_localhost;
        final String testPDU = "2007-01-01 127.0.0.1 beer - Not just for lunch anymore";
        final String testUEI = "uei.opennms.org/tests/syslogd/nonMessageMatch/facilityOnly";
        final String testMsg = "beer - Not just for lunch anymore";
    
        final EventBuilder expectedEventBldr = new EventBuilder(testUEI, "syslogd");
        expectedEventBldr.setInterface(addr(localhost));
        expectedEventBldr.setLogDest("logndisplay");
        expectedEventBldr.setLogMessage(testMsg);
        
        expectedEventBldr.addParam("service", "local0");
    
        final EventAnticipator ea = new EventAnticipator();
        m_eventIpcManager.addEventListener(ea);
        ea.anticipateEvent(expectedEventBldr.getEvent());
        
        final SyslogClient s = new SyslogClient(null, 10, SyslogClient.LOG_LOCAL0, addr(localhost));
        s.syslog(SyslogClient.LOG_DEBUG, testPDU);

        ea.verifyAnticipated(5000, 0, 0, 0, 0);
    }
    
    @Test
    public void testRegexProcessMatch() throws Exception {
        MockLogAppender.setupLogging(true, "TRACE");
        final String localhost = m_localhost;
        final String testPDU = "2007-01-01 127.0.0.1 beerd: beer - Not just for breakfast anymore";
        final String testUEI = "uei.opennms.org/tests/syslogd/nonMessageMatch/processOnly";
        final String testMsg = "beer - Not just for breakfast anymore";

        final EventBuilder expectedEventBldr = new EventBuilder(testUEI, "syslogd");
        expectedEventBldr.setInterface(addr(localhost));
        expectedEventBldr.setLogDest("logndisplay");
        expectedEventBldr.setLogMessage(testMsg);
        
        expectedEventBldr.addParam("process", "beerd");

        final EventAnticipator ea = new EventAnticipator();
        m_eventIpcManager.addEventListener(ea);
        ea.anticipateEvent(expectedEventBldr.getEvent());
        
        final SyslogClient s = new SyslogClient("beerd", 10, SyslogClient.LOG_DAEMON, addr(m_localhost));
        s.syslog(SyslogClient.LOG_DEBUG, testPDU);
    
        ea.verifyAnticipated(5000, 0, 0, 0, 0);
    }

    @Test
    public void testIPPatternsSyslogNG() throws UnknownHostException {
        final SyslogClient s = new SyslogClient(null, 10, SyslogClient.LOG_DAEMON, addr(m_localhost));
        s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 127.0.0.1 A SyslogNG style message");
    }

    @Test
    public void testResolvePatternsSyslogNG() throws UnknownHostException {
        final SyslogClient s = new SyslogClient(null, 10, SyslogClient.LOG_DAEMON, addr(m_localhost));
        s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 www.opennms.org A SyslogNG style message");
    }

    @Test
    public void testSubstrUEIRewrite() throws Exception {
        doMessageTest("2007-01-01 localhost A CRISCO message",
                      m_localhost, "uei.opennms.org/tests/syslogd/substrUeiRewriteTest",
                      "A CRISCO message");
    }

    @Test
    public void testRegexUEIRewrite() throws Exception {
        MockLogAppender.setupLogging(true, "TRACE");
        doMessageTest("2007-01-01 localhost foo: 100 out of 666 tests failed for bar",
                      m_localhost, "uei.opennms.org/tests/syslogd/regexUeiRewriteTest",
                      "100 out of 666 tests failed for bar");
    }
    
    @Test
    public void testSubstrTESTTestThatRemovesATESTString() throws Exception {
        doMessageTest("2007-01-01 localhost A CRISCO message that is also a TESTHIDING message -- hide me!",
                      m_localhost, "uei.opennms.org/tests/syslogd/substrUeiRewriteTest",
                      ConvertToEvent.HIDDEN_MESSAGE);
    }
    
    @Test
    public void testRegexTESTTestThatRemovesADoubleSecretString() throws Exception {
        doMessageTest("2007-01-01 localhost foo: 100 out of 666 tests failed for doubleSecret",
                      m_localhost, "uei.opennms.org/tests/syslogd/regexUeiRewriteTest",
                      ConvertToEvent.HIDDEN_MESSAGE);
    }
    
    @Test
    public void testSubstrDiscard() throws Exception {
        final String testPDU = "2007-01-01 127.0.0.1 A JUNK message";
        
        final EventAnticipator ea = new EventAnticipator();
        m_eventIpcManager.addEventListener(ea);
        
        final SyslogClient sc = new SyslogClient(null, 10, SyslogClient.LOG_DAEMON, addr(m_localhost));
        sc.syslog(SyslogClient.LOG_DEBUG, testPDU);

        ea.verifyAnticipated(5000, 0, 0, 0, 0);
    }

    @Test
    public void testRegexDiscard() throws Exception {
        final String testPDU = "2007-01-01 127.0.0.1 A TrAsH message";
        
        final EventAnticipator ea = new EventAnticipator();
        m_eventIpcManager.addEventListener(ea);
        
        final SyslogClient sc = new SyslogClient(null, 10, SyslogClient.LOG_DAEMON, addr(m_localhost));
        sc.syslog(SyslogClient.LOG_DEBUG, testPDU);

        ea.verifyAnticipated(5000, 0, 0, 0, 0);
    }
    
    @Test
    public void testRegexUEIWithBothKindsOfParameterAssignments() throws Exception {
        final String testPDU = "2007-01-01 127.0.0.1 coffee: Secretly replaced rangerrick's coffee with 42 wombats";
        final String expectedUEI = "uei.opennms.org/tests/syslogd/regexParameterAssignmentTest/bothKinds";
        final String expectedLogMsg = "Secretly replaced rangerrick's coffee with 42 wombats";
        final String[] testGroups = { "rangerrick's", "42", "wombats" };
        
        final Map<String,String> expectedParms = new HashMap<String,String>();
        expectedParms.put("group1", testGroups[0]);
        expectedParms.put("whoseBeverage", testGroups[0]);
        expectedParms.put("group2", testGroups[1]);
        expectedParms.put("count", testGroups[1]);
        expectedParms.put("group3", testGroups[2]);
        expectedParms.put("replacementItem", testGroups[2]);
        
        doMessageTest(testPDU, m_localhost, expectedUEI, expectedLogMsg, expectedParms);
    }

    @Test
    public void testRegexUEIWithOnlyUserSpecifiedParameterAssignments() throws InterruptedException, UnknownHostException {
        final String localhost = m_localhost;
        final String testPDU = "2007-01-01 127.0.0.1 tea: Secretly replaced cmiskell's tea with 666 ferrets";
        final String testUEI = "uei.opennms.org/tests/syslogd/regexParameterAssignmentTest/userSpecifiedOnly";
        final String testMsg = "Secretly replaced cmiskell's tea with 666 ferrets";
        final String[] testGroups = { "cmiskell's", "666", "ferrets" };

        final EventBuilder expectedEventBldr = new EventBuilder(testUEI, "syslogd");
        expectedEventBldr.setInterface(addr(localhost));
        expectedEventBldr.setLogDest("logndisplay");
        expectedEventBldr.setLogMessage(testMsg);
        
        expectedEventBldr.addParam("whoseBeverage", testGroups[0]);
        expectedEventBldr.addParam("count", testGroups[1]);
        expectedEventBldr.addParam("replacementItem", testGroups[2]);
    
        final EventAnticipator ea = new EventAnticipator();
        m_eventIpcManager.addEventListener(ea);
        ea.anticipateEvent(expectedEventBldr.getEvent());
        
        final SyslogClient s = new SyslogClient(null, 10, SyslogClient.LOG_DAEMON, addr(m_localhost));
        s.syslog(SyslogClient.LOG_DEBUG, testPDU);

        ea.verifyAnticipated(5000, 0, 0, 0, 0);
    }

}
