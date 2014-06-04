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

package org.opennms.netmgt.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.db.MockDatabase;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.EventTranslatorConfigFactory;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.OutageAnticipator;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Logmsg;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

public class EventTranslatorTest {
    
    /* TODO for PassiveSTatusKeeper
     add reason mapper for status reason
     
     be able to create an event with translated values
     - determine new event values based on config
     - assign computed values to new event
     - copy over (or not) untranslated attributes
     
     make sure we can translate uei if desired
     
     modify passive status config to handle specific event with specific parms
     
     
     */

    private EventTranslator m_translator;
    private String m_passiveStatusConfiguration = getStandardConfig();
    private MockEventIpcManager m_eventMgr;
    private MockDatabase m_db;
    private MockNetwork m_network;
    private EventAnticipator m_anticipator;
    private OutageAnticipator m_outageAnticipator;
    private EventTranslatorConfigFactory m_config;

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

        InputStream rdr = new ByteArrayInputStream(m_passiveStatusConfiguration.getBytes("UTF-8"));
        m_config = new EventTranslatorConfigFactory(rdr, m_db);
        EventTranslatorConfigFactory.setInstance(m_config);
        
        m_translator = EventTranslator.getInstance();
        m_translator.setEventManager(m_eventMgr);
        m_translator.setConfig(EventTranslatorConfigFactory.getInstance());
        m_translator.setDataSource(m_db);
        
        m_translator.init();
        m_translator.start();
        
    }

    @After
    public void tearDown() throws Exception {
        m_eventMgr.finishProcessingEvents();
        m_translator.stop();
        sleep(200);
        MockLogAppender.assertNoWarningsOrGreater();
        m_db.drop();
//        MockUtil.println("------------ End Test "+getName()+" --------------------------");
//        super.tearDown();
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
    
    @Test
    public void testSubElementString() throws Exception {
    	m_passiveStatusConfiguration = getSqlSubValueString();
    	tearDown();
    	setUp();
    	testTranslateEvent();
        
    }
    
    @Test
    public void testSubElementLong() throws Exception {
    	m_passiveStatusConfiguration = getSqlSubValueLong();
    	tearDown();
    	setUp();
    	testTranslateEvent();
    }
    
    @Test
    public void testIsTranslationEvent() throws Exception {
        // test non matching uei match fails
        Event pse = createTestEvent("someOtherUei", "Router", "192.168.1.1", "ICMP", "Down");
        assertFalse(m_config.isTranslationEvent(pse));
        
        // test matchin uei succeeds
        Event te = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        assertTrue(m_config.isTranslationEvent(te));
        
        // test null parms fails
        Event teWithNullParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        teWithNullParms.setParmCollection(null);
        assertFalse(m_config.isTranslationEvent(teWithNullParms));
        
        // test empty  parm list fails
        Event teWithNoParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        teWithNoParms.setParmCollection(new ArrayList<Parm>());
        assertFalse(m_config.isTranslationEvent(teWithNoParms));

        // test missing a parm fails
        Event teWithWrongParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        List<Parm> p = teWithWrongParms.getParmCollection();
        p.get(2).setParmName("unmatching"); // change the name for the third parm so it fails to match
        assertFalse(m_config.isTranslationEvent(teWithWrongParms));

        // that a matching parm value succeeds
        Event te2 = createTestEvent("translationTest", "Router", "xxx192.168.1.1xxx", "ICMP", "Down");
        assertTrue(m_config.isTranslationEvent(te2));
        
        // that a matching parm value succeeds
        Event te3 = createTestEvent("translationTest", "Router", "xxx192.168.1.2", "ICMP", "Down");
        assertFalse(m_config.isTranslationEvent(te3));
    }
    
    @Test
    public void testTranslateEvent() throws MarshalException, ValidationException {
    	
   		// test non matching uei match fails
        Event pse = createTestEvent("someOtherUei", "Router", "192.168.1.1", "ICMP", "Down");
        assertTrue(m_config.translateEvent(pse).isEmpty());
        
        // test matchin uei succeeds
        Event te = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "This node is way Down!");
        List<Event> translatedEvents = m_config.translateEvent(te);
		assertNotNull(translatedEvents);
		assertEquals(1, translatedEvents.size());
        validateTranslatedEvent((Event)translatedEvents.get(0));

        // test null parms fails
        Event teWithNullParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        teWithNullParms.setParmCollection(null);
        assertTrue(m_config.translateEvent(teWithNullParms).isEmpty());
        
        // test empty  parm list fails
        Event teWithNoParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        teWithNoParms.setParmCollection(new ArrayList<Parm>());
        assertTrue(m_config.translateEvent(teWithNoParms).isEmpty());

        // test missing a parm fails
        Event teWithWrongParms = createTestEvent("translationTest", "Router", "192.168.1.1", "ICMP", "Down");
        List<Parm> p = teWithWrongParms.getParmCollection();
        p.get(2).setParmName("unmatching"); // change the name for the third parm so it fails to match
        assertTrue(m_config.translateEvent(teWithWrongParms).isEmpty());

        // that a matching parm value succeeds
        Event te2 = createTestEvent("translationTest", "Router", "xxx192.168.1.1xxx", "ICMP", "Down");
        assertNotNull(m_config.translateEvent(te2));
		assertEquals(1, translatedEvents.size());
        validateTranslatedEvent((Event)translatedEvents.get(0));
        
        // that a matching parm value succeeds
        Event te3 = createTestEvent("translationTest", "Router", "xxx192.168.1.2", "ICMP", "Down");
        assertTrue(m_config.translateEvent(te3).isEmpty());
    }
    
    @Test
    public void testTranslateLinkDown() throws MarshalException, ValidationException, SQLException, UnsupportedEncodingException {
        InputStream rdr = new ByteArrayInputStream(getLinkDownTranslation().getBytes("UTF-8"));
        m_config = new EventTranslatorConfigFactory(rdr, m_db);
        EventTranslatorConfigFactory.setInstance(m_config);
        
        m_translator = EventTranslator.getInstance();
        m_translator.setEventManager(m_eventMgr);
        m_translator.setConfig(EventTranslatorConfigFactory.getInstance());
        //m_translator.setDataSource(m_db);
        
        
        Connection c = m_db.getConnection();
        Statement stmt = c.createStatement();
        stmt.executeUpdate("update snmpinterface set snmpifname = 'david', snmpifalias = 'p-brane' WHERE nodeid = 1 and snmpifindex = 2");
        stmt.close();
        c.close();
        
        List<Event> translatedEvents = m_config.translateEvent(createLinkDownEvent());
        assertNotNull(translatedEvents);
        assertEquals(1, translatedEvents.size());
        assertEquals(3, translatedEvents.get(0).getParmCollection().size());
        assertEquals(".1.3.6.1.2.1.2.2.1.1.2", translatedEvents.get(0).getParmCollection().get(0).getParmName());
        assertEquals("ifName", translatedEvents.get(0).getParmCollection().get(1).getParmName());
        assertEquals("ifAlias", translatedEvents.get(0).getParmCollection().get(2).getParmName());
        assertEquals("david", translatedEvents.get(0).getParmCollection().get(1).getValue().getContent());
        assertEquals("p-brane", translatedEvents.get(0).getParmCollection().get(2).getValue().getContent());
    }

	private String getLinkDownTranslation() {
	    String linkDownConfig = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
	    		"<event-translator-configuration xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" + 
	    		"  xsi:schemaLocation=\"http://xmlns.opennms.org/xsd/translator-configuration http://www.opennms.org/xsd/config/translator-configuration.xsd \">\n" + 
	    		"  <translation>\n" + 
	    		"    <!-- This translation is predifined for integration with Hyperic-HQ server and the OpenNMS integrations found in\n" + 
	    		"         the $OPENNMS_HOME/contrib/hyperic-integration directory -->\n" + 
	    		"    <event-translation-spec uei=\"uei.opennms.org/generic/traps/SNMP_Link_Down\">\n" + 
	    		"      <mappings>\n" + 
	    		"        <mapping>\n" + 
	    		"          <assignment name=\"ifName\" type=\"parameter\">\n" + 
	    		"            <value type=\"sql\" result=\"SELECT snmp.snmpIfName FROM snmpInterface snmp WHERE snmp.nodeid = ?::integer AND snmp.snmpifindex = ?::integer\" >\n" + 
                "              <value type=\"field\" name=\"nodeid\" matches=\".*\" result=\"${0}\" />\n" + 
	    		"              <value type=\"parameter\" name=\"~^\\.1\\.3\\.6\\.1\\.2\\.1\\.2\\.2\\.1\\.1\\.([0-9]*)$\" matches=\".*\" result=\"${0}\" />\n" + 
	    		"            </value>\n" + 
	    		"          </assignment>\n" + 
                "          <assignment name=\"ifAlias\" type=\"parameter\">\n" + 
                "            <value type=\"sql\" result=\"SELECT snmp.snmpIfAlias FROM snmpInterface snmp WHERE snmp.nodeid = ?::integer AND snmp.snmpifindex = ?::integer\" >\n" + 
                "              <value type=\"field\" name=\"nodeid\" matches=\".*\" result=\"${0}\" />\n" + 
                "              <value type=\"parameter\" name=\"~^\\.1\\.3\\.6\\.1\\.2\\.1\\.2\\.2\\.1\\.1\\.([0-9]*)$\" matches=\".*\" result=\"${0}\" />\n" + 
                "            </value>\n" + 
                "          </assignment>\n" + 
	    		"        </mapping>\n" + 
	    		"      </mappings>\n" + 
	    		"    </event-translation-spec>\n" + 
	    		"  </translation>\n" + 
	    		"</event-translator-configuration>";
	    return linkDownConfig;
    }

    private void validateTranslatedEvent(Event event) {
		assertEquals(m_translator.getName(), event.getSource());
		assertEquals(Long.valueOf(3), event.getNodeid());
		assertEquals("www.opennms.org", event.getHost());
        assertEquals("a generated event", event.getDescr());
        assertEquals("192.168.1.1", event.getInterface());
        assertEquals("Switch", EventUtils.getParm(event, "nodeLabel"));
        assertEquals("PSV", event.getService());
        assertEquals("Down", EventUtils.getParm(event, "passiveStatus"));
	}
    
    @Test
    public void testUEIList() {
    		List<String> ueis = m_config.getUEIList();
    		assertEquals(1, ueis.size());
    		assertTrue(ueis.contains("uei.opennms.org/services/translationTest"));
    }
    
    private Event createLinkDownEvent() {
        EventBuilder builder = new EventBuilder("uei.opennms.org/generic/traps/SNMP_Link_Down", "Trapd");
        builder.setField("nodeid", "1");
        builder.addParam(".1.3.6.1.2.1.2.2.1.1.2", "2");
        return builder.getEvent();
    }

    private Event createTestEvent(String type, String nodeLabel, String ipAddr, String serviceName, String status) {
        final List<Parm> parms = new ArrayList<Parm>();

        if(nodeLabel != null) parms.add(buildParm(EventConstants.PARM_PASSIVE_NODE_LABEL, nodeLabel));
        if(ipAddr != null) parms.add(buildParm(EventConstants.PARM_PASSIVE_IPADDR, ipAddr));
        if(serviceName != null) parms.add(buildParm(EventConstants.PARM_PASSIVE_SERVICE_NAME, serviceName));
        if(status != null) parms.add(buildParm(EventConstants.PARM_PASSIVE_SERVICE_STATUS, status));

		return createEventWithParms("uei.opennms.org/services/"+type, parms);
	}

    private Event createEventWithParms(String uei, List<Parm> parms) {
		Event e = MockEventUtil.createEventBuilder("Automation", uei).getEvent();
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
    
    
    private String getSqlSubValueLong() {
    	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<event-translator-configuration \n" + 
        "xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
        "  <translation>\n" +
        "   <event-translation-spec uei=\"uei.opennms.org/services/translationTest\">\n" + 
        "      <mappings>\n" + 
        "        <mapping>\n" +
        "          <assignment type=\"field\" name=\"nodeid\">\n" +  
        "            <value type=\"sql\" result=\"select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D' and to_number(?, '999999') = 9999 \" >\n" +
        "				<value type=\"parameter\" name=\"passiveNodeLabel\" matches=\"Router\" result=\"Firewall\" />\n" +
        "				<value type=\"constant\" result=\"192.168.1.4\" />\n" +
        "				<value type=\"field\" name=\"nodeid\" result=\"9999\" />\n" +
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
        "</event-translator-configuration>\n" + 
        "";
    }
    private String getSqlSubValueString() {
    	return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<event-translator-configuration \n" + 
        "xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
        "  <translation>\n" +
        "   <event-translation-spec uei=\"uei.opennms.org/services/translationTest\">\n" + 
        "      <mappings>\n" + 
        "        <mapping>\n" +
        "          <assignment type=\"field\" name=\"nodeid\">\n" +  
        "            <value type=\"sql\" result=\"select node.nodeid from node, ipInterface where node.nodeLabel=? and ipinterface.ipaddr=? and node.nodeId=ipinterface.nodeid and ipInterface.isManaged != 'D' and node.nodeType != 'D' and ? = 'test' \" >\n" +
        "				<value type=\"parameter\" name=\"passiveNodeLabel\" matches=\"Router\" result=\"Firewall\" />\n" +
        "				<value type=\"constant\" result=\"192.168.1.4\" />\n" +
        "				<value type=\"field\" name=\"host\" result=\"test\" />\n" +
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
        "</event-translator-configuration>\n" + 
        "";
    }
    
    
    
    private String getStandardConfig() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
        "<event-translator-configuration \n" + 
        "xmlns=\"http://xmlns.opennms.org/xsd/translator-configuration\" \n" + 
        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" >\n" +
        "  <translation>\n" +
        "   <event-translation-spec uei=\"uei.opennms.org/services/translationTest\">\n" + 
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
        "</event-translator-configuration>\n" + 
        "";
    }
    
}
