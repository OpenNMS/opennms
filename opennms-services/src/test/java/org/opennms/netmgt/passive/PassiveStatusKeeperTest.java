/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.passive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.monitors.PassiveServiceMonitor;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

public class PassiveStatusKeeperTest {
    
    /* TODO for PassiveSTatusKeeper
     add reason mapper for status reason
     
     be able to create an event with translated values
     - determine new event values based on config
     - assign computed values to new event
     - copy over (or not) untranslated attributes
     
     make sure we can translate uei if desired
     
     modify passive status config to handle specific event with specific parms
     
     
     */


    private PassiveStatusKeeper m_psk;
    private MockEventIpcManager m_eventMgr;
    private MockDatabase m_db;
    private MockNetwork m_network;
    private EventAnticipator m_anticipator;
    private OutageAnticipator m_outageAnticipator;

    @Before
    public void setUp() throws Exception {
//        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        MockLogAppender.setupLogging();

        createMockNetwork();
        createMockDb();
        createAnticipators();

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventWriter(m_db);
        m_eventMgr.setEventAnticipator(m_anticipator);
        m_eventMgr.addEventListener(m_outageAnticipator);
        m_eventMgr.setSynchronous(true);

        m_psk = new PassiveStatusKeeper();
        m_psk.setEventManager(m_eventMgr);
        m_psk.setDataSource(m_db);
        
        PassiveStatusKeeper.setInstance(m_psk);
        
        m_psk.init();
        m_psk.start();
        
    }

    @After
    public void tearDown() throws Exception {
        m_eventMgr.finishProcessingEvents();
        m_psk.stop();
        sleep(200);
        MockLogAppender.assertNoWarningsOrGreater();
        m_db.drop();
//        MockUtil.println("------------ End Test "+getName()+" --------------------------");
    }
    

    private void createAnticipators() {
        m_anticipator = new EventAnticipator();
        m_outageAnticipator = new OutageAnticipator(m_db);
    }

    private void createMockDb() throws Exception {
        m_db = new MockDatabase();
        m_db.populate(m_network);
        DataSourceFactory.setInstance(m_db);
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
    
    /**
     * This is a test for the passive status keeper where all the required parms are included
     * in the event.
     */
    @Test
    public void testEventWithPassiveStatusParms() {
        Event e = createPassiveStatusEvent("Router", "192.168.1.1", "ICMP", "Down");

        assertTrue(m_psk.isPassiveStatusEvent(e));
        
    }
    
    /**
     * This is a test for the method that verifies valid passive status events
     * for the passive status keeper.
     * @throws ValidationException 
     * @throws MarshalException 
     *
     */
    @Test
    public void testIsPassiveStatusEvent() throws MarshalException, ValidationException {
        
        Event e = createPassiveStatusEvent("Router", "192.168.1.1", "ICMP", "Down");
        assertTrue(m_psk.isPassiveStatusEvent(e));
        
        //test for missing required parms
        e = createPassiveStatusEvent("Router", "192.168.1.1", null, "Down");
        assertFalse(m_psk.isPassiveStatusEvent(e));
        
        //this will test the event simply doesn't match a registered uei.
        e.setUei("bogusUei");
        assertFalse(m_psk.isPassiveStatusEvent(e));
                
    }
    
    @Test
    public void testSetStatus() {
        testSetStatus("localhost", "127.0.0.1", "PSV", PollStatus.up());
        
    }

    private void testSetStatus(String nodeLabel, String ipAddr, String svcName, PollStatus pollStatus) {
        PassiveStatusKeeper.getInstance().setStatus(nodeLabel, ipAddr, svcName, pollStatus);
        assertEquals(pollStatus, PassiveStatusKeeper.getInstance().getStatus(nodeLabel, ipAddr, svcName));
    }
    
    @Test
    public void testRestart() {
        testSetStatus("localhost", "127.0.0.1", "PSV", PollStatus.up());

        testSetStatus("localhost", "127.0.0.1", "PSV2", PollStatus.down());
        
        MockService svc = m_network.getService(100, "127.0.0.1", "PSV2");
        Event downEvent = svc.createDownEvent();
        m_db.writeEvent(downEvent);
        m_db.createOutage(svc, downEvent);

        m_psk.stop();
        
        m_psk.setEventManager(m_eventMgr);
        m_psk.setDataSource(m_db);
        m_psk.init();
        m_psk.start();
        
        assertEquals(PollStatus.up(), PassiveStatusKeeper.getInstance().getStatus("localhost", "127.0.0.1", "PSV"));
        assertEquals(PollStatus.down(), PassiveStatusKeeper.getInstance().getStatus("localhost", "127.0.0.1", "PSV2"));
    }
    
    @Test
    public void testDownPassiveStatus() throws InterruptedException, UnknownHostException {

        Event e = createPassiveStatusEvent("Router", "192.168.1.1", "ICMP", "Down");
        m_eventMgr.sendNow(e);
        
        PollStatus ps = m_psk.getStatus("Router", "192.168.1.1", "ICMP");
        
        assertTrue(ps.isDown());
        
        MockMonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.addr("192.168.1.1"), "ICMP" );
        
        ServiceMonitor m = new PassiveServiceMonitor();
        m.initialize((Map<String,Object>)null);
        m.initialize(svc);
        PollStatus ps2 = m.poll(svc, null);
        m.release(svc);
        m.release();
        
        assertEquals(ps, ps2);
    }

    private Event createPassiveStatusEvent(String nodeLabel, String ipAddr, String serviceName, String status) {
        final List<Parm> parms = new ArrayList<Parm>();

        if(nodeLabel != null) parms.add(buildParm(EventConstants.PARM_PASSIVE_NODE_LABEL, nodeLabel));
        if(ipAddr != null) parms.add(buildParm(EventConstants.PARM_PASSIVE_IPADDR, ipAddr));
        if(serviceName != null) parms.add(buildParm(EventConstants.PARM_PASSIVE_SERVICE_NAME, serviceName));
        if(status != null) parms.add(buildParm(EventConstants.PARM_PASSIVE_SERVICE_STATUS, status));

		return createEventWithParms("uei.opennms.org/services/passiveServiceStatus", parms);
    }

    private Event createEventWithParms(String uei, List<Parm> parms) {
		Event e = MockEventUtil.createEventBuilder("Test", uei).getEvent();
		e.setHost("localhost");
        
        e.setParmCollection(parms);
        Logmsg logmsg = new Logmsg();
        logmsg.setContent("Testing Passive Status Keeper with down status");
        e.setLogmsg(logmsg);
        return e;
	}
    
    
    
    private Parm buildParm(String parmName, String parmValue) {
        Value v = new Value();
        v.setContent(parmValue);
        Parm p = new Parm();
        p.setParmName(parmName);
        p.setValue(v);
        return p;
    }
    
    @SuppressWarnings("unused")
    private String getTranslationTestConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<passive-status-configuration \n" + 
        "xmlns=\"http://xmlns.opennms.org/xsd/passive-status-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
        "  <translation>\n" +
        "   <event-translation-spec uei=\"uei.opennms.org/services/translationEvent\">\n" + 
        "      <mappings>\n" + 
        "        <mapping>\n" +
        "          <assignment type=\"field\" name=\"nodeid\">\n" +  
        "            <value type=\"sql\" result=\"select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D'\" >\n" +
        "				<value type=\"parameter\" name=\"passiveNodeLabel\" matches=\"Router\" result=\"Firewall\" />\n" +
        "				<value type=\"constant\" result=\"192.168.1.4\" />\n" +
        "			</value>\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"nodeLabel\">\n" +  
        "            <value type=\"field\" name=\"host\" result=\"Switch\" />\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"field\" name=\"interface\">\n" + 
        "            <value type=\"parameter\" name=\"passiveIpAddr\" matches=\".*(192\\.168\\.1\\.1).*\" result=\"192.168.1.1\" />\n" +
        "          </assignment>\n" +
        "		  <assignment type=\"field\" name=\"host\">\n" +
        "			<value type=\"field\" name=\"host\" result=\"www.opennms.org\" />\n" +
        "		  </assignment>\n" + 
        "		  <assignment type=\"field\" name=\"descr\">\n" +
        "			<value type=\"constant\" result=\"a generated event\" />\n" +
        "		  </assignment>\n" + 
        "          <assignment type=\"field\" name=\"service\">\n" + 
        "            <value type=\"parameter\" name=\"passiveServiceName\" result=\"PSV\" />\n" + 
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"passiveStatus\">\n" + 
        "            <value type=\"parameter\" name=\"passiveStatus\" matches=\".*(Up|Down).*\" result=\"${1}\" />\n" + 
        "          </assignment>\n" + 
        "        </mapping>\n" + 
        "      </mappings>\n" + 
        "    </event-translation-spec>\n" + 
        "  </translation>\n" +
        "</passive-status-configuration>\n" + 
        "";
    }
    

    
    @SuppressWarnings("unused")
    private String getStandardConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<passive-status-configuration \n" + 
        "xmlns=\"http://xmlns.opennms.org/xsd/passive-status-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
        "  <translation>\n" +
        "   <event-translation-spec uei=\"uei.opennms.org/services/translationEvent\">\n" + 
        "      <mappings>\n" + 
        "        <mapping>\n" +
        "          <assignment type=\"field\" name=\"nodeid\">\n" +  
        "            <value type=\"sql\" result=\"select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D'\" >\n" +
        "				<value type=\"parameter\" name=\"passiveNodeLabel\" matches=\"Router\" result=\"Firewall\" />\n" +
        "				<value type=\"constant\" result=\"192.168.1.4\" />\n" +
        "			</value>\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"nodeLabel\">\n" +  
        "            <value type=\"field\" name=\"host\" result=\"Switch\" />\n" +
        "          </assignment>\n" + 
        "          <assignment type=\"field\" name=\"interface\">\n" + 
        "            <value type=\"parameter\" name=\"passiveIpAddr\" matches=\".*(192\\.168\\.1\\.1).*\" result=\"192.168.1.1\" />\n" +
        "          </assignment>\n" +
        "		  <assignment type=\"field\" name=\"host\">\n" +
        "			<value type=\"field\" name=\"host\" result=\"www.opennms.org\" />\n" +
        "		  </assignment>\n" + 
        "		  <assignment type=\"field\" name=\"descr\">\n" +
        "			<value type=\"constant\" result=\"a generated event\" />\n" +
        "		  </assignment>\n" + 
        "          <assignment type=\"field\" name=\"service\">\n" + 
        "            <value type=\"parameter\" name=\"passiveServiceName\" result=\"PSV\" />\n" + 
        "          </assignment>\n" + 
        "          <assignment type=\"parameter\" name=\"passiveStatus\">\n" + 
        "            <value type=\"parameter\" name=\"passiveStatus\" matches=\".*(Up|Down).*\" result=\"${1}\" />\n" + 
        "          </assignment>\n" + 
        "        </mapping>\n" + 
        "      </mappings>\n" + 
        "    </event-translation-spec>\n" + 
        "  </translation>\n" +
        "  <passive-events>\n" + 
        "    <passive-event uei=\"uei.opennms.org/services/passiveServiceStatus\">\n" + 
        "      <status-key>\n" + 
        "        <node-label>\n" + 
        "          <event-token is-parm=\"true\" name=\"passiveNodeLabel\" value=\"Router\"/>\n" + 
        "        </node-label>\n" + 
        "        <ipaddr>\n" + 
        "          <event-token is-parm=\"true\" name=\"passiveIpAddr\" value=\"192.168.1.1\"/>\n" + 
        "        </ipaddr>\n" + 
        "        <service-name>\n" + 
        "          <event-token is-parm=\"true\" name=\"passiveServiceName\" value=\"ICMP\"/>\n" + 
        "        </service-name>\n" + 
        "        <status>\n" + 
        "          <event-token is-parm=\"true\" name=\"passiveStatus\" value=\"Down\"/>\n" + 
        "        </status>\n" + 
        "      </status-key>\n" + 
        "    </passive-event>\n" + 
        "  </passive-events>\n" + 
        "</passive-status-configuration>\n" + 
        "";
    }
    
    @SuppressWarnings("unused")
    private String getLiteralFieldConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<this:passive-status-configuration \n" + 
        "xmlns:this=\"http://xmlns.opennms.org/xsd/passive-status-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" + 
        "  <this:passive-events>\n" + 
        "    <this:passive-event uei=\"uei.opennms.org/services/passiveServiceStatus\">\n" + 
        "      <this:status-key>\n" + 
        "        <this:node-label>\n" + 
        "          <this:event-token is-parm=\"false\" name=\"host\" value=\"Router\"/>\n" + 
        "        </this:node-label>\n" + 
        "        <this:ipaddr>\n" + 
        "          <this:event-token is-parm=\"false\" name=\"source\" value=\"192.168.1.1\"/>\n" + 
        "        </this:ipaddr>\n" + 
        "        <this:service-name>\n" + 
        "          <this:event-token is-parm=\"false\" name=\"service\" value=\"ICMP\"/>\n" + 
        "        </this:service-name>\n" + 
        "        <this:status>\n" + 
        "          <this:event-token is-parm=\"false\" name=\"descr\" value=\"Down\"/>\n" + 
        "        </this:status>\n" + 
        "      </this:status-key>\n" + 
        "    </this:passive-event>\n" + 
        "  </this:passive-events>\n" + 
        "</this:passive-status-configuration>\n" + 
        "";
    }
    
    @SuppressWarnings("unused")
    private String getLiteralParmConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<this:passive-status-configuration \n" + 
        "xmlns:this=\"http://xmlns.opennms.org/xsd/passive-status-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" + 
        "  <this:passive-events>\n" + 
        "    <this:passive-event uei=\"uei.opennms.org/services/passiveServiceStatus\">\n" + 
        "      <this:status-key>\n" + 
        "        <this:node-label>\n" + 
        "          <this:event-token is-parm=\"true\" name=\"passiveNodeLabel\" value=\"Router\"/>\n" + 
        "        </this:node-label>\n" + 
        "        <this:ipaddr>\n" + 
        "          <this:event-token is-parm=\"true\" name=\"passiveIpAddr\" value=\"192.168.1.1\"/>\n" + 
        "        </this:ipaddr>\n" + 
        "        <this:service-name>\n" + 
        "          <this:event-token is-parm=\"true\" name=\"passiveServiceName\" value=\"ICMP\"/>\n" + 
        "        </this:service-name>\n" + 
        "        <this:status>\n" + 
        "          <this:event-token is-parm=\"true\" name=\"passiveStatus\" value=\"Down\"/>\n" + 
        "        </this:status>\n" + 
        "      </this:status-key>\n" + 
        "    </this:passive-event>\n" + 
        "  </this:passive-events>\n" + 
        "</this:passive-status-configuration>\n" + 
        "";
    }
    
    @SuppressWarnings("unused")
    private String getRegExFieldConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<this:passive-status-configuration \n" + 
        "xmlns:this=\"http://xmlns.opennms.org/xsd/passive-status-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" + 
        "  <this:passive-events>\n" + 
        "    <this:passive-event uei=\"uei.opennms.org/services/passiveServiceStatus\">\n" + 
        "      <this:status-key>\n" + 
        "        <this:node-label>\n" + 
        "          <this:event-token is-parm=\"false\" name=\"host\" value=\"~.*\"/>\n" + 
        "        </this:node-label>\n" + 
        "        <this:ipaddr>\n" + 
        "          <this:event-token is-parm=\"false\" name=\"source\" value=\"~.*(192\\.168\\.1\\.1).*\"/>\n" + 
        "        </this:ipaddr>\n" + 
        "        <this:service-name>\n" + 
        "          <this:event-token is-parm=\"false\" name=\"service\" value=\"~.*(ICMP).*\" format=\"$1\"/>\n" + 
        "        </this:service-name>\n" + 
        "        <this:status>\n" + 
        "          <this:event-token is-parm=\"false\" name=\"descr\" value=\"~.*is(Down).*\" format=\"$1\"/>\n" + 
        "        </this:status>\n" + 
        "      </this:status-key>\n" + 
        "    </this:passive-event>\n" + 
        "  </this:passive-events>\n" + 
        "</this:passive-status-configuration>\n" + 
        "";
    }
    
    @SuppressWarnings("unused")
    private String getRegExParmConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<this:passive-status-configuration \n" + 
        "xmlns:this=\"http://xmlns.opennms.org/xsd/passive-status-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" + 
        "  <translation>\n" +
        "   <event-translation-spec uei=\"uei.opennms.org/services/translationEvent\">\n" + 
        "      <mappings>\n" + 
        "        <mapping>\n" +
        "          <assignment type=\"field\" name=\"uei\">\n" + 
        "            <value type=\"constant\" result=\"uei.opennms.org/services/passiveServiceStatus\" />\n" +
        "          </assignment>\n" +
        "        </mapping>\n" + 
        "      </mappings>\n" + 
        "    </event-translation-spec>\n" + 
        "  </translation>\n" +
        "</this:passive-status-configuration>\n" + 
        "";
    }

}
