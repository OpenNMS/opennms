// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc. All rights
// reserved.
// OpenNMS(R) is a derivative work, containing both original code, included
// code and modified
// code that was published under the GNU General Public License. Copyrights
// for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 10: Eliminate warnings, use ConfigurationTestUtils. - dj@opennms.org
// 2007 Aug 24: Fix failing tests and warnings. - dj@opennms.org
//
// Original code base Copyright (C) 1999-2001 Oculan Corp. All rights
// reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
package org.opennms.netmgt.syslogd;

import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.SyslogdConfig;
import org.opennms.netmgt.config.SyslogdConfigFactory;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.OpenNMSTestCase;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.DaoTestConfigBean;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;

public class SyslogdTest extends OpenNMSTestCase {

    private Syslogd m_syslogd;

    protected void setUp() throws Exception {
        DaoTestConfigBean daoTestConfig = new DaoTestConfigBean();
        daoTestConfig.setRelativeHomeDirectory("src/test/resources");
        daoTestConfig.afterPropertiesSet();

        super.setUp();

        MockUtil.println("------------ Begin Test " + getName() + " --------------------------");
        MockLogAppender.setupLogging();

        MockNetwork network = new MockNetwork();
        MockDatabase db = new MockDatabase();
        db.populate(network);
        DataSourceFactory.setInstance(db);
        
        InputStream stream = null;
        try {
            stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-configuration.xml");
            new SyslogdConfigFactory(stream);
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }

        m_syslogd = new Syslogd();
        m_syslogd.init();
    }

    @Override
    protected void tearDown() throws Exception {
        MockUtil.println("------------ End Test " + getName() + " --------------------------");
        super.tearDown();
    }

    @Override
    public void runTest() throws Throwable {
        super.runTest();
        MockLogAppender.assertNotGreaterOrEqual(Level.FATAL);
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
     */
    private List<Event> doMessageTest(String testPDU, String expectedHost, String expectedUEI, String expectedLogMsg) throws UnknownHostException, InterruptedException {
        startSyslogdGracefully();
        
        Event expectedEvent = new Event();
        expectedEvent.setUei(expectedUEI);
        expectedEvent.setSource("syslogd");
        expectedEvent.setInterface(expectedHost);
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        logmsg.setContent(expectedLogMsg);
        expectedEvent.setLogmsg(logmsg);
    
        EventAnticipator ea = new EventAnticipator();
        getEventIpcManager().addEventListener(ea);
        ea.anticipateEvent(expectedEvent);
        
        SyslogClient sc = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
        DatagramPacket pkt = sc.getPacket(SyslogClient.LOG_DEBUG, testPDU);
        SyslogdConfig config = SyslogdConfigFactory.getInstance();
        Thread worker = new Thread(new SyslogConnection(pkt, config.getForwardingRegexp(), config.getMatchingGroupHost(), config.getMatchingGroupMessage(), config.getUeiList(), config.getHideMessages(), config.getDiscardUei()), SyslogConnection.class.getSimpleName());
        worker.run();

        ea.verifyAnticipated(1000,0,0,0,0);
        Event receivedEvent = ea.getAnticipatedEventsRecieved().get(0);
        assertEquals("Log messages do not match", expectedLogMsg, receivedEvent.getLogmsg().getContent());
        
        return ea.getAnticipatedEventsRecieved();
    }
    
    private List<Event> doMessageTest(String testPDU, String expectedHost, String expectedUEI, String expectedLogMsg, Map<String,String> expectedParams) throws UnknownHostException, InterruptedException {
        List<Event> receivedEvents = doMessageTest(testPDU, expectedHost, expectedUEI, expectedLogMsg);
        
        Map<String,String> actualParms = new HashMap<String,String>();
        for (Parm actualParm : receivedEvents.get(0).getParms().getParmCollection()) {
            actualParms.put(actualParm.getParmName(), actualParm.getValue().getContent());
        }

        for (String expectedKey : expectedParams.keySet()) {
            String expectedValue = expectedParams.get(expectedKey);
            assertTrue("Actual event does not have a parameter called " + expectedKey, actualParms.containsKey(expectedKey));
            assertEquals("Actual event has a parameter called " + expectedKey + " but its value does not match", expectedValue, actualParms.get(expectedKey));
        }
        return receivedEvents;
    }

    public void testMessaging() {
        // More of an integrations test
        // relies on you reading some of the logging....

        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 0, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_ERR, "Hello.");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }
    }

    public void XXXtestMyPatternsSyslogNG() {
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 host.domain.com A SyslogNG style message");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }
    }

    public void testIPPatternsSyslogNG() {
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 127.0.0.1 A SyslogNG style message");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }
    }

    public void testResolvePatternsSyslogNG() {
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 www.opennms.org A SyslogNG style message");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }
    }
    
    private void startSyslogdGracefully() {
        try {
            m_syslogd.start();
        } catch (UndeclaredThrowableException ute) {
            if (ute.getCause() instanceof BindException) {
                // continue, this was expected
            } else {
                throw ute;
            }
        }
    }
    
    public void testSubstrUEIRewrite() throws Exception {
        doMessageTest("2007-01-01 localhost A CRISCO message",
                      myLocalHost().getHostAddress(), "uei.opennms.org/tests/syslogd/substrUeiRewriteTest",
                      "A CRISCO message");
    }
    public void testRegexUEIRewrite() throws Exception {
//        MockLogAppender.setupLogging(true, "TRACE");
        doMessageTest("2007-01-01 localhost foo: 100 out of 666 tests failed for bar",
                      myLocalHost().getHostAddress(), "uei.opennms.org/tests/syslogd/regexUeiRewriteTest",
                      "100 out of 666 tests failed for bar");
    }
    
    public void testSubstrTESTTestThatRemovesATESTString() throws Exception {
        doMessageTest("2007-01-01 localhost A CRISCO message that is also a TESTHIDING message -- hide me!",
                      myLocalHost().getHostAddress(), "uei.opennms.org/tests/syslogd/substrUeiRewriteTest",
                      ConvertToEvent.HIDDEN_MESSAGE);
    }
    
    public void testRegexTESTTestThatRemovesADoubleSecretString() throws Exception {
        doMessageTest("2007-01-01 localhost foo: 100 out of 666 tests failed for doubleSecret",
                      myLocalHost().getHostAddress(), "uei.opennms.org/tests/syslogd/regexUeiRewriteTest",
                      ConvertToEvent.HIDDEN_MESSAGE);
    }
    
    public void testSubstrDiscard() throws Exception {
        startSyslogdGracefully();
        final String testPDU = "2007-01-01 127.0.0.1 A JUNK message";
        
        EventAnticipator ea = new EventAnticipator();
        getEventIpcManager().addEventListener(ea);
        
        SyslogClient sc = null;
        sc = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
        sc.syslog(SyslogClient.LOG_DEBUG, testPDU);
        
        Thread.sleep(3000);
        assertEquals(0, ea.unanticipatedEvents().size());
    }

    public void testRegexDiscard() throws Exception {
        startSyslogdGracefully();
        final String testPDU = "2007-01-01 127.0.0.1 A TrAsH message";
        
        EventAnticipator ea = new EventAnticipator();
        getEventIpcManager().addEventListener(ea);
        
        SyslogClient sc = null;
        sc = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
        sc.syslog(SyslogClient.LOG_DEBUG, testPDU);
        
        Thread.sleep(3000);
        assertEquals(0, ea.unanticipatedEvents().size());
    }
    
    public void testRegexUEIWithBothKindsOfParameterAssignments() throws Exception {
        final String testPDU = "2007-01-01 127.0.0.1 coffee: Secretly replaced rangerrick's coffee with 42 wombats";
        final String expectedUEI = "uei.opennms.org/tests/syslogd/regexParameterAssignmentTest/bothKinds";
        final String expectedLogMsg = "Secretly replaced rangerrick's coffee with 42 wombats";
        final String[] testGroups = { "rangerrick's", "42", "wombats" };
        
        Map<String,String> expectedParms = new HashMap<String,String>();
        expectedParms.put("group1", testGroups[0]);
        expectedParms.put("whoseBeverage", testGroups[0]);
        expectedParms.put("group2", testGroups[1]);
        expectedParms.put("count", testGroups[1]);
        expectedParms.put("group3", testGroups[2]);
        expectedParms.put("replacementItem", testGroups[2]);
        
        doMessageTest(testPDU, myLocalHost().getHostAddress(), expectedUEI, expectedLogMsg, expectedParms);
    }

    public void testRegexUEIWithOnlyUserSpecifiedParameterAssignments() throws InterruptedException {
        startSyslogdGracefully();
        
        String localhost = myLocalHost().getHostAddress();
        final String testPDU = "2007-01-01 127.0.0.1 tea: Secretly replaced cmiskell's tea with 666 ferrets";
        final String testUEI = "uei.opennms.org/tests/syslogd/regexParameterAssignmentTest/userSpecifiedOnly";
        final String testMsg = "Secretly replaced cmiskell's tea with 666 ferrets";
        final String[] testGroups = { "cmiskell's", "666", "ferrets" };
        
        Event e = new Event();
        e.setUei(testUEI);
        e.setSource("syslogd");
        e.setInterface(localhost);
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        logmsg.setContent(testMsg);
        e.setLogmsg(logmsg);
        
        Parms eventParms = new Parms();
        Parm eventParm = null;
        Value parmValue = null;
        
        eventParm = new Parm();
        eventParm.setParmName("whoseBeverage");
        parmValue = new Value();
        parmValue.setContent(testGroups[0]);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);
        
        eventParm = new Parm();
        eventParm.setParmName("count");
        parmValue = new Value();
        parmValue.setContent(testGroups[1]);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);
        
        eventParm = new Parm();
        eventParm.setParmName("replacementItem");
        parmValue = new Value();
        parmValue.setContent(testGroups[2]);
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);

        e.setParms(eventParms);
    
        EventAnticipator ea = new EventAnticipator();
        getEventIpcManager().addEventListener(ea);
        ea.anticipateEvent(e);
        
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, testPDU);
        } catch (UnknownHostException uhe) {
            //Failures are for weenies
        }
    
        assertEquals(0, ea.waitForAnticipated(1000).size());
        Thread.sleep(2000);
        assertEquals(0, ea.unanticipatedEvents().size());
    }

}
