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

package org.opennms.netmgt.trapd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.Base64;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-trapDaemon.xml",
        "classpath:/org/opennms/netmgt/trapd/applicationContext-trapDaemonTest.xml"}
)
@JUnitConfigurationEnvironment
public class TrapHandlerTestCase implements InitializingBean {

    @Autowired
    private Trapd m_trapd = null;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    @Autowired
    private MockTrapdIpMgr m_trapdIpMgr;

    @Autowired
    private TrapQueueProcessorFactory m_processorFactory;

    private EventAnticipator m_anticipator;

    private InetAddress m_localhost = null;

    @Resource(name="snmpTrapPort")
    private Integer m_snmpTrapPort;

    private boolean m_doStop = false;

    private static final String m_ip = "127.0.0.1";
    
    private static final long m_nodeId = 1;

    @BeforeClass
    public static void setUpLogging() {
        MockLogAppender.setupLogging();
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_anticipator = new EventAnticipator();
        m_eventMgr.setEventAnticipator(m_anticipator);

        m_localhost = InetAddressUtils.addr(m_ip);
        
        m_trapdIpMgr.clearKnownIpsMap();
        m_trapdIpMgr.setNodeId(m_ip, m_nodeId);

        m_trapd.start();
        m_doStop = true;
    }

    public Collection<Event> finishUp() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // do nothing
        }
        m_eventMgr.finishProcessingEvents();
        m_anticipator.verifyAnticipated(1000, 0, 0, 0, 0);
        return m_anticipator.getAnticipatedEventsRecieved();
    }

    @After
    public void tearDown() throws Exception {
        if (m_trapd != null && m_doStop) {
            m_trapd.stop();
            m_trapd = null;
        }
        
    }

    @Test
    @DirtiesContext
    public void testV1TrapNoNewSuspect() throws Exception {
        m_trapdIpMgr.clearKnownIpsMap();
        anticipateAndSend(false, false, "uei.opennms.org/default/trap", "v1",
                null, 6, 1);
    }

    @Test
    @DirtiesContext
    public void testV2TrapNoNewSuspect() throws Exception {
        m_trapdIpMgr.clearKnownIpsMap();
        anticipateAndSend(false, false, "uei.opennms.org/default/trap",
                "v2c", null, 6, 1);
    }

    @Test
    @DirtiesContext
    public void testV1TrapNewSuspect() throws Exception {
        m_trapdIpMgr.clearKnownIpsMap();
        anticipateAndSend(true, false, "uei.opennms.org/default/trap",
                "v1", null, 6, 1);
    }

    @Test
    @DirtiesContext
    public void testV2TrapNewSuspect() throws Exception {
        m_trapdIpMgr.clearKnownIpsMap();
        anticipateAndSend(true, false, "uei.opennms.org/default/trap",
                "v2c", null, 6, 1);
    }

    @Test
    @DirtiesContext
    public void testV1EnterpriseIdAndGenericMatch() throws Exception {
        anticipateAndSend(false, true,
                "uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                "v1", ".1.3.6.1.2.1.15.7", 6, 1);
    }

    @Test
    @DirtiesContext
    public void testV2EnterpriseIdAndGenericAndSpecificMatch()
    throws Exception {
        anticipateAndSend(false, true,
                "uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                "v2c", ".1.3.6.1.2.1.15.7", 6, 1);
    }

    @Test
    @DirtiesContext
    public void testV1EnterpriseIdAndGenericAndSpecificAndMatchWithVarbinds()
    throws Exception {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

        LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.4.2404", valueFactory.getInt32(3));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.5.2404", valueFactory.getInt32(2));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.6.2404", valueFactory.getInt32(5));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.3.0.2404", valueFactory.getOctetString("http://a.b.c.d/cgi/fDetail?index=2404".getBytes()));
        anticipateAndSend(false, true,
                "uei.opennms.org/vendor/HP/traps/hpicfFaultFinderTrap",
                "v1", ".1.3.6.1.4.1.11.2.14.12.1", 6, 5, varbinds);
    }
    
    @Test
    @DirtiesContext
    public void testV1TrapOIDWildCardMatch()
    throws Exception {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

        LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
        varbinds.put(".1.3.6.1.4.1.32473.42.42.42", valueFactory.getInt32(42));
        anticipateAndSend(false, true,
                "uei.opennms.org/IANA/Example/traps/exampleEnterpriseTrap",
                "v1", ".1.3.6.1.4.1.32473.42", 6, 5, varbinds);
    }

    @Test
    @DirtiesContext
    public void testV2EnterpriseIdAndGenericAndSpecificAndMatchWithVarbinds()
    throws Exception {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

        LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.4.2404", valueFactory.getInt32(3));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.5.2404", valueFactory.getInt32(2));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.2.1.6.2404", valueFactory.getInt32(5));
        varbinds.put(".1.3.6.1.4.1.11.2.14.11.1.7.3.0.2404", valueFactory.getOctetString("http://a.b.c.d/cgi/fDetail?index=2404".getBytes()));
        anticipateAndSend(false, true,
                "uei.opennms.org/vendor/HP/traps/hpicfFaultFinderTrap",
                "v2c", ".1.3.6.1.4.1.11.2.14.12.1", 6, 5, varbinds);
    }


    // These exist to provide testing for the new Textual Convention feature
    // See EventConfDataTest for the other part of this testing
    @Test
    @DirtiesContext
    public void testV1EnterpriseIdAndGenericAndSpecificAndMatchWithVarbindsAndTC()
    throws Exception {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

        LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
        varbinds.put(".1.3.6.1.4.1.14179.2.6.2.20.0", valueFactory.getOctetString(new byte[]{(byte)0x00,(byte)0x14,(byte)0xf1,(byte)0xad,(byte)0xa7,(byte)0x50}));
        Collection<Event> events = anticipateAndSend(false, true,
                "uei.opennms.org/vendor/cisco/bsnAPNoiseProfileUpdatedToPass",
                "v1", ".1.3.6.1.4.1.14179.2.6.3", 6, 38, varbinds);

        boolean foundMacAddress = false;
        // Assert that the MAC address varbind has been formatted into a colon-separated octet string
        for (Event event : events) {
            for (Parm parm : event.getParmCollection()) {
                if (".1.3.6.1.4.1.14179.2.6.2.20.0".equals(parm.getParmName())) {
                    assertEquals("MAC address does not match", "00:14:F1:AD:A7:50", parm.getValue().getContent());
                    foundMacAddress = true;
                }
            }
        }
        assertTrue("Did not find expected MAC address parm", foundMacAddress);
    }

    // FIXME: these exist to provide testing for the new Textual Convention feature
    @Test
    @DirtiesContext
    public void testV2EnterpriseIdAndGenericAndSpecificAndMatchWithVarbindsAndTC()
    throws Exception {
        SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

        byte[] macAddr = new byte[]{(byte)0x00,(byte)0x14,(byte)0xf1,(byte)0xad,(byte)0xa7,(byte)0x50};

        String encoded = new String(Base64.encodeBase64(macAddr));
        byte[] decodeBytes = Base64.decodeBase64(encoded.toCharArray());

        assertByteArrayEquals(macAddr, decodeBytes);

        LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
        varbinds.put(".1.3.6.1.4.1.14179.2.6.2.20.0", valueFactory.getOctetString(macAddr));
        Collection<Event> events = anticipateAndSend(false, true,
                "uei.opennms.org/vendor/cisco/bsnAPNoiseProfileUpdatedToPass",
                "v2c", ".1.3.6.1.4.1.14179.2.6.3", 6, 38, varbinds);

        boolean foundMacAddress = false;
        // Assert that the MAC address varbind has been formatted into a colon-separated octet string
        for (Event event : events) {
            for (Parm parm : event.getParmCollection()) {
                if (".1.3.6.1.4.1.14179.2.6.2.20.0".equals(parm.getParmName())) {
                    assertEquals("MAC address does not match", "00:14:F1:AD:A7:50", parm.getValue().getContent());
                    foundMacAddress = true;
                }
            }
        }
        assertTrue("Did not find expected MAC address parm", foundMacAddress);
    }

    private void assertByteArrayEquals(byte[] macAddr, byte[] bytes) {
        assertEquals("expect length: "+macAddr.length, macAddr.length, bytes.length);
        for (int i = 0; i < macAddr.length; i++) {
            assertEquals("Expected byte "+i+" to match", macAddr[i], bytes[i]);
        }
    }

    @Test
    @DirtiesContext
    public void testV2EnterpriseIdAndGenericAndSpecificMatchWithZero()
    throws Exception {
        anticipateAndSend(false, true,
                "uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                "v2c", ".1.3.6.1.2.1.15.7.0", 6, 1);
    }

    @Test
    @DirtiesContext
    public void testV2EnterpriseIdAndGenericAndSpecificMissWithExtraZeros()
    throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap", "v2c",
                ".1.3.6.1.2.1.15.7.0.0", 6, 1);
    }

    @Test
    @DirtiesContext
    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongGeneric()
    throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap", "v1",
                ".1.3.6.1.2.1.15.7", 5, 1);
    }

    @Test
    @DirtiesContext
    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongSpecific()
    throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap", "v1",
                ".1.3.6.1.2.1.15.7", 6, 50);
    }

    @Test
    @DirtiesContext
    public void testV1GenericMatch() throws Exception {
        anticipateAndSend(false, true,
                "uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v1", null, 0, 0);
    }

    @Test
    @DirtiesContext
    public void testV2GenericMatch() throws Exception {
        anticipateAndSend(false, true,
                "uei.opennms.org/generic/traps/SNMP_Cold_Start",
                "v2c", ".1.3.6.1.6.3.1.1.5.1", 0, 0);
    }

    @Test
    @DirtiesContext
    public void testV1TrapDroppedEvent() throws Exception {
        anticipateAndSend(false, true, null, "v1", ".1.3.6.1.2.1.15.7", 6, 2);
    }

    @Test
    @DirtiesContext
    public void testV2TrapDroppedEvent() throws Exception {
        anticipateAndSend(false, true, null, "v2c", ".1.3.6.1.2.1.15.7", 6, 2);
    }

    @Test
    @DirtiesContext
    public void testV1TrapDefaultEvent() throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap",
                "v1", null, 6, 1);
    }

    @Test
    @DirtiesContext
    public void testV2TrapDefaultEvent() throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap",
                "v2c", null, 6, 1);
    }

    @Test
    @DirtiesContext
    public void testNodeGainedModifiesIpMgr() throws Exception {
        long nodeId = 2;
        m_processorFactory.setNewSuspect(true);

        anticipateEvent("uei.opennms.org/default/trap", m_ip, nodeId);

        Event event =
            anticipateEvent(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI,
                    m_ip, nodeId);
        m_eventMgr.sendNow(event);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // do nothing
        }

        sendTrap("v1", null, 6, 1);

        finishUp();
    }

    @Test
    @DirtiesContext
    public void testInterfaceReparentedModifiesIpMgr() throws Exception {
        long nodeId = 2;
        m_processorFactory.setNewSuspect(true);

        anticipateEvent("uei.opennms.org/default/trap", m_ip, nodeId);

        Event event =
            anticipateEvent(EventConstants.INTERFACE_REPARENTED_EVENT_UEI,
                    m_ip, nodeId);
        m_eventMgr.sendNow(event);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // do nothing
        }

        sendTrap("v1", null, 6, 1);

        finishUp();
    }

    @Test
    @DirtiesContext
    public void testInterfaceDeletedModifiesIpMgr() throws Exception {
        long nodeId = 0;
        m_processorFactory.setNewSuspect(true);

        anticipateEvent("uei.opennms.org/default/trap", m_ip, nodeId);

        Event event =
            anticipateEvent(EventConstants.INTERFACE_DELETED_EVENT_UEI,
                    m_ip, nodeId);
        m_eventMgr.sendNow(event);

        anticipateEvent(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, m_ip, nodeId);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            // do nothing
        }

        sendTrap("v1", null, 6, 1);

        finishUp();
    }

    public Event anticipateEvent(String uei) {
        return anticipateEvent(uei, m_ip, m_nodeId);
    }

    public Event anticipateEvent(String uei, String ip, long nodeId) {
        EventBuilder bldr = new EventBuilder(uei, "TrapHandlerTestCase");
        bldr.setNodeid(nodeId);
        bldr.setInterface(addr(ip));
        m_anticipator.anticipateEvent(bldr.getEvent());
        return bldr.getEvent();
    }

    public Collection<Event> anticipateAndSend(boolean newSuspectOnTrap, boolean nodeKnown,
            String event,
            String version, String enterprise,
            int generic, int specific) throws Exception {
        return anticipateAndSend(newSuspectOnTrap, nodeKnown, event, version, enterprise, generic, specific, null);
    }


    /**
     * @param newSuspectOnTrap Will a new suspect event be triggered by the trap?
     * @param nodeKnown Is the node in the database?
     * @param event Event that is anticipated to result when the trap is processed
     * @param snmpTrapVersion SNMP version of trap, valid values: <code>v1</code>, <code>v2c</code>
     * @param enterprise Enterprise ID of the trap
     * @param varbinds Varbinds attached to the trap
     */
    public Collection<Event> anticipateAndSend(boolean newSuspectOnTrap, boolean nodeKnown,
            String event,
            String snmpTrapVersion, String enterprise,
            int generic, int specific, LinkedHashMap<String, SnmpValue> varbinds) throws Exception {
        m_processorFactory.setNewSuspect(newSuspectOnTrap);

        if (newSuspectOnTrap) {
            // Note: the nodeId will be zero because the node is not known
            anticipateEvent(EventConstants.NEW_SUSPECT_INTERFACE_EVENT_UEI, m_ip, 0);
        }

        if (event != null) {
            if (nodeKnown) {
                anticipateEvent(event);
            } else {
                /*
                 * If the node is unknown, the nodeId on the trap event
                 * will be zero.
                 */
                anticipateEvent(event, m_ip, 0);
            }
        }

        if (varbinds == null) {
            sendTrap(snmpTrapVersion, enterprise, generic, specific);
        } else {
            sendTrap(snmpTrapVersion, enterprise, generic, specific, varbinds);
        }

        return finishUp();
    }

    public void sendTrap(String version, String enterprise, int generic,
            int specific) throws Exception {
        if (enterprise == null) {
            enterprise = ".0.0";
        }

        if (version.equals("v1")) {
            sendV1Trap(enterprise, generic, specific);
        } else if (version.equals("v2c")) {
            sendV2Trap(enterprise, specific);
        } else {
            throw new Exception("unsupported SNMP version for test: "
                    + version);
        }
    }

    private void sendTrap(String version, String enterprise, int generic, 
            int specific, LinkedHashMap<String, SnmpValue> varbinds) throws Exception {
        if (enterprise == null) {
            enterprise = ".0.0";
        }

        if (version.equals("v1")) {
            sendV1Trap(enterprise, generic, specific, varbinds);
        } else if (version.equals("v2c")) {
            sendV2Trap(enterprise, specific, varbinds);
        } else {
            throw new Exception("unsupported SNMP version for test: "
                    + version);
        }
    }

    public void sendV1Trap(String enterprise, int generic, int specific)
    throws Exception {
        SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();
        pdu.setEnterprise(SnmpObjId.get(enterprise));
        pdu.setGeneric(generic);
        pdu.setSpecific(specific);
        pdu.setTimeStamp(0);
        pdu.setAgentAddress(m_localhost);

        pdu.send(getHostAddress(), m_snmpTrapPort, "public");
    }

	private String getHostAddress() {
		return InetAddressUtils.str(m_localhost);
	}

    public void sendV1Trap(String enterprise, int generic, int specific, LinkedHashMap<String, SnmpValue> varbinds)
    throws Exception {
        SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();
        pdu.setEnterprise(SnmpObjId.get(enterprise));
        pdu.setGeneric(generic);
        pdu.setSpecific(specific);
        pdu.setTimeStamp(0);
        pdu.setAgentAddress(m_localhost);
        Iterator<Map.Entry<String,SnmpValue>> it = varbinds.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String,SnmpValue> pairs = it.next();
            pdu.addVarBind(SnmpObjId.get(pairs.getKey()), pairs.getValue());
        }
        pdu.send(getHostAddress(), m_snmpTrapPort, "public");
    }


    public void sendV2Trap(String enterprise, int specific) throws Exception {
        SnmpObjId enterpriseId = SnmpObjId.get(enterprise);
        boolean isGeneric = false;
        SnmpObjId trapOID;
        if (SnmpObjId.get(".1.3.6.1.6.3.1.1.5").isPrefixOf(enterpriseId)) {
            isGeneric = true;
            trapOID = enterpriseId;
        } else {
            trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(specific));
            // XXX or should it be this
            // trap OID = enterprise + ".0." + specific;
        }

        SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"),
                SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"),
                SnmpUtils.getValueFactory().getObjectId(trapOID));
        if (isGeneric) {
            pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"),
                    SnmpUtils.getValueFactory().getObjectId(enterpriseId));
        }

        pdu.send(getHostAddress(), m_snmpTrapPort, "public");
    }

    public void sendV2Trap(String enterprise, int specific, LinkedHashMap<String, SnmpValue> varbinds) throws Exception {
        SnmpObjId enterpriseId = SnmpObjId.get(enterprise);
        boolean isGeneric = false;
        SnmpObjId trapOID;
        if (SnmpObjId.get(".1.3.6.1.6.3.1.1.5").isPrefixOf(enterpriseId)) {
            isGeneric = true;
            trapOID = enterpriseId;
        } else {
            trapOID = SnmpObjId.get(enterpriseId, new SnmpInstId(specific));
            // XXX or should it be this
            // trap OID = enterprise + ".0." + specific;
        }

        SnmpTrapBuilder pdu = SnmpUtils.getV2TrapBuilder();
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.2.1.1.3.0"),
                SnmpUtils.getValueFactory().getTimeTicks(0));
        pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.1.0"),
                SnmpUtils.getValueFactory().getObjectId(trapOID));
        if (isGeneric) {
            pdu.addVarBind(SnmpObjId.get(".1.3.6.1.6.3.1.1.4.3.0"),
                    SnmpUtils.getValueFactory().getObjectId(enterpriseId));
        }
        for (Map.Entry<String, SnmpValue> entry : varbinds.entrySet()) {
            pdu.addVarBind(SnmpObjId.get(entry.getKey()), entry.getValue());
        }

        pdu.send(getHostAddress(), m_snmpTrapPort, "public");
    }

}
