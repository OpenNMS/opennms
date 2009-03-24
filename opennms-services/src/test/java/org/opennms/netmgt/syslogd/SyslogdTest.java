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

import java.io.Reader;
import java.net.UnknownHostException;

import org.apache.log4j.Level;
import org.opennms.netmgt.config.DataSourceFactory;
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
        
        Reader rdr = ConfigurationTestUtils.getReaderForResource(this, "/etc/syslogd-configuration.xml");
        try {  
            new SyslogdConfigFactory(rdr);
        } finally {
            rdr.close();
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

    public void testSyslogdStart() {
        assertEquals("START_PENDING", m_syslogd.getStatusText());
        m_syslogd.start();
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

    public void testMyPatternsSyslogNG() {
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, "2007-01-01 host.domain.com A SyslogNG style message");
        } catch (UnknownHostException e) {
            //Failures are for weenies
        }

        //LoggingEvent[] events = MockLogAppender.getEventsGreaterOrEqual(Level.WARN);
        //assertEquals("number of logged events", 0, events.length);
        //assertEquals("first logged event severity (should be ERROR)", Level.ERROR, events[0].getLevel());

        MockLogAppender.resetEvents();
        MockLogAppender.resetLogLevel();
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

    public void testSubstrUEIRewrite() throws InterruptedException {
    	String localhost = myLocalHost();
    	final String testPDU = "2007-01-01 www.opennms.org A CISCO message";
    	final String testUEI = "uei.opennms.org/tests/syslogd/substrUeiRewriteTest";
    	final String testMsg = "A CISCO message";
    	
        Event e = new Event();
        e.setUei(testUEI);
        e.setSource("syslogd");
        e.setInterface(localhost);
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        logmsg.setContent(testMsg);
        e.setLogmsg(logmsg);

        EventAnticipator ea = new EventAnticipator();
        ea.anticipateEvent(e);
        
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, testPDU);
        } catch (UnknownHostException uhe) {
            //Failures are for weenies
        }
        
        assertEquals(1, ea.waitForAnticipated(1000).size());
        Thread.sleep(2000);
        assertEquals(0, ea.unanticipatedEvents().size());
        
        assertFalse(ea.getAnticipatedEvents().isEmpty());
        
        Event ne = (Event) ea.getAnticipatedEvents().iterator().next();
        assertEquals(testUEI, ne.getUei());
        assertEquals("syslogd", ne.getSource());
        assertEquals(testMsg, ne.getLogmsg().getContent());
    }

    public void testRegexUEIRewrite() throws InterruptedException {
    	String localhost = myLocalHost();
    	final String testPDU = "2007-01-01 www.opennms.org foo: 100 out of 666 tests failed for bar";
    	final String testUEI = "uei.opennms.org/tests/syslogd/regexUeiRewriteTest";
    	final String testMsg = "foo: 100 out of 666 tests failed for bar";
    	
        Event e = new Event();
        e.setUei(testUEI);
        e.setSource("syslogd");
        e.setInterface(localhost);
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        logmsg.setContent(testMsg);
        e.setLogmsg(logmsg);

        EventAnticipator ea = new EventAnticipator();
        ea.anticipateEvent(e);
        
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, testPDU);
        } catch (UnknownHostException uhe) {
            //Failures are for weenies
        }
        
        assertEquals(1, ea.waitForAnticipated(1000).size());
        Thread.sleep(2000);
        assertEquals(0, ea.unanticipatedEvents().size());
        
        assertFalse(ea.getAnticipatedEvents().isEmpty());
        
        Event ne = (Event) ea.getAnticipatedEvents().iterator().next();
        assertEquals(testUEI, ne.getUei());
        assertEquals("syslogd", ne.getSource());
        assertEquals(testMsg, ne.getLogmsg().getContent());    
    }
    
    public void testSubstrTESTTestThatRemovesATESTString() throws InterruptedException {
    	String localhost = myLocalHost();
    	final String testPDU = "2007-01-01 www.opennms.org A CISCO message that is also a TEST message -- hide me!";
    	final String testUEI = "uei.opennms.org/tests/syslogd/substrUeiRewriteTest";
    	final String testMsg = ConvertToEvent.HIDDEN_MESSAGE;
    	
        Event e = new Event();
        e.setUei("uei.opennms.org/tests/syslogd/substrUeiRewriteTest");
        e.setSource("syslogd");
        e.setInterface(localhost);
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        logmsg.setContent(testMsg);
        e.setLogmsg(logmsg);

        EventAnticipator ea = new EventAnticipator();
        ea.anticipateEvent(e);
        
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, testPDU);
        } catch (UnknownHostException uhe) {
            //Failures are for weenies
        }
        
        assertEquals(1, ea.waitForAnticipated(1000).size());
        Thread.sleep(2000);
        assertEquals(0, ea.unanticipatedEvents().size());
        
        assertFalse(ea.getAnticipatedEvents().isEmpty());
        
        Event ne = (Event) ea.getAnticipatedEvents().iterator().next();
        assertEquals(testUEI, ne.getUei());
        assertEquals("syslogd", ne.getSource());
        assertEquals(testMsg, ne.getLogmsg().getContent());    
    }
    
    public void testRegexTESTTestThatRemovesADoubleSecretString() throws InterruptedException {
    	String localhost = myLocalHost();
    	final String testPDU = "2007-01-01 www.opennms.org foo: 100 out of 666 tests failed for bar";
    	final String testUEI = "uei.opennms.org/tests/syslogd/regexUeiRewriteTest";
    	final String testMsg = ConvertToEvent.HIDDEN_MESSAGE;
    	final String[] testGroups = { "100", "666", "bar" };
    	
        Event e = new Event();
        e.setUei("uei.opennms.org/tests/syslogd/regexUeiRewriteTest");
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
        eventParm.setParmName("group1");
        parmValue = new Value();
        parmValue.setContent(testGroups[0]);
        eventParm.setValue(parmValue);
        
        eventParm = new Parm();
        eventParm.setParmName("group2");
        parmValue = new Value();
        parmValue.setContent(testGroups[1]);
        eventParm.setValue(parmValue);
        
        eventParm = new Parm();
        eventParm.setParmName("group3");
        parmValue = new Value();
        parmValue.setContent(testGroups[2]);
        eventParm.setValue(parmValue);
        
        e.setParms(eventParms);

        EventAnticipator ea = new EventAnticipator();
        ea.anticipateEvent(e);
        
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, testPDU);
        } catch (UnknownHostException uhe) {
            //Failures are for weenies
        }

        assertEquals(1, ea.waitForAnticipated(1000).size());
        Thread.sleep(2000);
        assertEquals(0, ea.unanticipatedEvents().size());
        
        assertFalse(ea.getAnticipatedEvents().isEmpty());
        
        Event ne = (Event) ea.getAnticipatedEvents().iterator().next();
        assertEquals(testUEI, ne.getUei());
        assertEquals("syslogd", ne.getSource());
        assertEquals(testMsg, ne.getLogmsg().getContent());      
    }

    public void testSubstrDiscard() throws InterruptedException {
        String localhost = myLocalHost();
        final String testPDU = "2007-01-01 www.opennms.org A JUNK message";
        final String testUEI = "DISCARD-MATCHING-MESSAGES";
        final String testMsg = "A JUNK message";
        
        Event e = new Event();
        e.setUei(testUEI);
        e.setSource("syslogd");
        e.setInterface(localhost);
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        logmsg.setContent(testMsg);
        e.setLogmsg(logmsg);

        EventAnticipator ea = new EventAnticipator();
        ea.anticipateEvent(e);
        
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, testPDU);
        } catch (UnknownHostException uhe) {
            //Failures are for weenies
        }
        
        assertEquals(1, ea.waitForAnticipated(1000).size());
        Thread.sleep(2000);
        assertEquals(0, ea.unanticipatedEvents().size());
        
        assertFalse(ea.getAnticipatedEvents().isEmpty());
        
        Event ne = (Event) ea.getAnticipatedEvents().iterator().next();
        assertEquals(testUEI, ne.getUei());
        assertEquals("syslogd", ne.getSource());
        assertEquals(testMsg, ne.getLogmsg().getContent());
    }

    public void testRegexDiscard() throws InterruptedException {
        String localhost = myLocalHost();
        final String testPDU = "2007-01-01 www.opennms.org A TrAsH message";
        final String testUEI = "DISCARD-MATCHING-MESSAGES";
        final String testMsg = "A TrAsH message";
        
        Event e = new Event();
        e.setUei(testUEI);
        e.setSource("syslogd");
        e.setInterface(localhost);
        Logmsg logmsg = new Logmsg();
        logmsg.setDest("logndisplay");
        logmsg.setContent(testMsg);
        e.setLogmsg(logmsg);

        EventAnticipator ea = new EventAnticipator();
        ea.anticipateEvent(e);
        
        SyslogClient s = null;
        try {
            s = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
            s.syslog(SyslogClient.LOG_DEBUG, testPDU);
        } catch (UnknownHostException uhe) {
            //Failures are for weenies
        }
        
        assertEquals(1, ea.waitForAnticipated(1000).size());
        Thread.sleep(2000);
        assertEquals(0, ea.unanticipatedEvents().size());
        
        assertFalse(ea.getAnticipatedEvents().isEmpty());
        
        Event ne = (Event) ea.getAnticipatedEvents().iterator().next();
        assertEquals(testUEI, ne.getUei());
        assertEquals("syslogd", ne.getSource());
        assertEquals(testMsg, ne.getLogmsg().getContent());    
    }
}
