// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc. All rights
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
// 2010 Aug 19: Create from SyslogdTest  - jeffg@opennms.org
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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
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

public class SyslogdLoadTest extends OpenNMSTestCase {

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
            stream = ConfigurationTestUtils.getInputStreamForResource(this, "/etc/syslogd-loadtest-configuration.xml");
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
        
        SyslogClient sc = null;
        sc = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
        sc.syslog(SyslogClient.LOG_DEBUG, testPDU);
        
        assertEquals(0, ea.waitForAnticipated(2000).size());
        Thread.sleep(2000);
        assertEquals(0, ea.unanticipatedEvents().size());
        
        Event receivedEvent = ea.getAnticipatedEventsRecieved().get(0);
        assertEquals("Log messages do not match", expectedLogMsg, receivedEvent.getLogmsg().getContent());
        
        return ea.getAnticipatedEventsRecieved();
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
    
    public void XXXtestHummerSyslogd() throws Exception {
        startSyslogdGracefully();
        
        EventAnticipator ea = new EventAnticipator();
        getEventIpcManager().addEventListener(ea);
        
        List<Event> eventsToSend = new ArrayList<Event>();
        
        for (int i = 0; i < 100000; i++) {
            double eventNum = Math.random() * 300;
            String expectedUei = "uei.example.org/syslog/loadTest/foo" + eventNum;
            Event thisEvent = new Event();
            thisEvent.setUei(expectedUei);
            Logmsg logmsg = new Logmsg();
            logmsg.setDest("logndisplay");
            logmsg.setContent("A load test has been received as a Syslog Message");
            thisEvent.setLogmsg(logmsg);
            ea.anticipateEvent(thisEvent);
            eventsToSend.add(thisEvent);
        }
        
        String testPduFormat = "2010-08-19 localhost foo%d: load test %d on tty1";
        SyslogClient sc = null;
        sc = new SyslogClient(null, 10, SyslogClient.LOG_DEBUG);
        for (Event e : eventsToSend) {
            //TODO This is where I left off
            //sc.syslog(SyslogClient.LOG_DEBUG, )
        }
    }

}
