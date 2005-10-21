//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.passive;

import java.io.Reader;
import java.io.StringReader;
import java.net.UnknownHostException;
import java.util.Date;

import org.jmock.cglib.MockObjectTestCase;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.PassiveStatusConfigFactory;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockLogAppender;
import org.opennms.netmgt.mock.MockMonitoredService;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.monitors.PassiveServiceMonitor;
import org.opennms.netmgt.poller.pollables.PollStatus;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

public class PassiveStatusKeeperTest extends MockObjectTestCase {


    private PassiveStatusKeeper m_psk;
    private String passiveStatusConfiguration ="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<tns:passiveStatus-configuration xmlns:tns=\"http://www.example.org/xsd/passive-status-configuration\"\n" + 
            "                                 xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" + 
            "  <tns:passive-status-uei key=\"passiveStatus\" value=\"uei.opennms.org/services/passiveServiceStatus\"/>\n" + 
            "</tns:passiveStatus-configuration>\n" + 
            "";
    private MockEventIpcManager m_eventMgr;
    private MockDatabase m_db;
    private MockNetwork m_network;
    private EventAnticipator m_anticipator;
    private OutageAnticipator m_outageAnticipator;

    protected void setUp() throws Exception {
        super.setUp();

        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        MockLogAppender.setupLogging();

        createMockNetwork();
        createMockDb();
        createAnticipators();

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);
        m_eventMgr.setSynchronous(true);

        Reader rdr = new StringReader(passiveStatusConfiguration);
        PassiveStatusConfigFactory.setInstance(new PassiveStatusConfigFactory(rdr));
        
        m_psk = PassiveStatusKeeper.getInstance();
        m_psk.setEventManager(m_eventMgr);
        m_psk.setConfig(PassiveStatusConfigFactory.getInstance());
        m_psk.setDbConnectionFactory(m_db);
        
        m_psk.init();
        m_psk.start();
        
    }

    protected void tearDown() throws Exception {
        m_eventMgr.finishProcessingEvents();
        m_psk.stop();
        m_psk.destroy();
        sleep(200);
        MockLogAppender.assertNoWarningsOrGreater();
        DatabaseConnectionFactory.setInstance(null);
        m_db.drop();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");
        super.tearDown();
    }
    

    private void createAnticipators() {
        m_anticipator = new EventAnticipator();
        m_outageAnticipator = new OutageAnticipator(m_db);
    }

    private void createMockDb() {
        m_db = new MockDatabase();
        m_db.populate(m_network);
        DatabaseConnectionFactory.setInstance(m_db);
    }

    private void createMockNetwork() {
        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("192.168.1.1");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addInterface("192.168.1.2");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addNode(2, "Server");
        m_network.addInterface("192.168.1.3");
        m_network.addService("ICMP");
        m_network.addService("HTTP");
        m_network.addNode(3, "Firewall");
        m_network.addInterface("192.168.1.4");
        m_network.addService("SMTP");
        m_network.addService("HTTP");
        m_network.addInterface("192.168.1.5");
        m_network.addService("SMTP");
        m_network.addService("HTTP");
        m_network.addNode(100, "localhost");
        m_network.addInterface("127.0.0.1");
        m_network.addService("PSV");
        m_network.addService("PSV2");
    }

    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }


    public void testSetStatus() {
        testSetStatus("localhost", "127.0.0.1", "PSV", PollStatus.STATUS_UP);
        
    }

    private void testSetStatus(String nodeLabel, String ipAddr, String svcName, PollStatus pollStatus) {
        PassiveStatusKeeper.getInstance().setStatus(nodeLabel, ipAddr, svcName, pollStatus);
        assertEquals(pollStatus, PassiveStatusKeeper.getInstance().getStatus(nodeLabel, ipAddr, svcName));
    }
    
    public void testRestart() {
        testSetStatus("localhost", "127.0.0.1", "PSV", PollStatus.STATUS_UP);

        testSetStatus("localhost", "127.0.0.1", "PSV2", PollStatus.STATUS_DOWN);
        
        MockService svc = m_network.getService(100, "127.0.0.1", "PSV2");
        Event downEvent = svc.createDownEvent();
        m_db.writeEvent(downEvent);
        m_db.createOutage(svc, downEvent);

        m_psk.stop();
        m_psk.destroy();
        
        
        m_psk.setEventManager(m_eventMgr);
        m_psk.setConfig(PassiveStatusConfigFactory.getInstance());
        m_psk.setDbConnectionFactory(m_db);
        m_psk.init();
        m_psk.start();
        
        assertEquals(PollStatus.STATUS_UP, PassiveStatusKeeper.getInstance().getStatus("localhost", "127.0.0.1", "PSV"));
        assertEquals(PollStatus.STATUS_DOWN, PassiveStatusKeeper.getInstance().getStatus("localhost", "127.0.0.1", "PSV2"));
    }
    
    public void testDownPassiveStatus() throws InterruptedException, UnknownHostException {

        String uei = "uei.opennms.org/services/passiveServiceStatus";
        Event e = createEvent("Automation", uei);
        Parms parms = new Parms();

        parms.addParm(buildParm(EventConstants.PARM_PASSIVE_NODE_LABEL, "Router"));
        parms.addParm(buildParm(EventConstants.PARM_PASSIVE_IPADDR, "192.168.1.1"));
        parms.addParm(buildParm(EventConstants.PARM_PASSIVE_SERVICE_NAME, "ICMP"));
        parms.addParm(buildParm(EventConstants.PARM_PASSIVE_SERVICE_STATUS, "Down"));
        
        e.setParms(parms);
        Logmsg logmsg = new Logmsg();
        logmsg.setContent("Testing Passive Status Keeper with down status");
        e.setLogmsg(logmsg);
        m_eventMgr.sendNow(e);
        
        PollStatus ps = m_psk.getStatus("Router", "192.168.1.1", "ICMP");
        
        assertTrue(ps.isDown());
        
        MockMonitoredService svc = new MockMonitoredService(1, "Router", "192.168.1.1", "ICMP" );
        
        ServiceMonitor m = new PassiveServiceMonitor();
        m.initialize(null, null);
        m.initialize(svc);
        PollStatus ps2 = m.poll(svc, null, null);
        m.release(svc);
        m.release();
        
        assertEquals(ps, ps2);
    }
    
    private Parm buildParm(String parmName, String parmValue) {
        Value v = new Value();
        v.setContent(parmValue);
        Parm p = new Parm();
        p.setParmName(parmName);
        p.setValue(v);
        return p;
    }

    private static Event createEvent(String source, String uei) {
        Event event = new Event();
        event.setSource(source);
        event.setUei(uei);
        String eventTime = EventConstants.formatToString(new Date());
        event.setCreationTime(eventTime);
        event.setTime(eventTime);
        return event;
    }

}
