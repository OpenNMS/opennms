//
//  $Id$
//

package org.opennms.netmgt.trapd;

import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventConfigurationManager;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.EventWrapper;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockTrapdConfig;
import org.opennms.netmgt.snmp.PropertySettingTestSuite;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockLogAppender;

public class TrapHandlerTest extends TestCase {
    
    public static TestSuite suite() {
        Class testClass = TrapHandlerTest.class;
        TestSuite suite = new TestSuite(testClass.getName());
        suite.addTest(new PropertySettingTestSuite(testClass, "JoeSnmp Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.joesnmp.JoeSnmpStrategy"));
        suite.addTest(new PropertySettingTestSuite(testClass, "Snmp4J Tests", "org.opennms.snmp.strategyClass", "org.opennms.netmgt.snmp.snmp4j.Snmp4JStrategy"));
        return suite;
    }


    private TrapHandler m_trapHandler = null;

    private EventAnticipator m_anticipator = null;

    private MockEventIpcManager m_eventMgr = null;
    
    private InetAddress m_localhost = null;
    
    private int m_port = 10000; 

    private static final String m_ip = "127.0.0.1";
    private static final long m_nodeId = 1;

    protected void setUp() throws Exception {
        MockLogAppender.setupLogging();
        
        m_anticipator = new EventAnticipator();

        m_eventMgr = new MockEventIpcManager();
        m_eventMgr.setEventAnticipator(m_anticipator);
        
        m_localhost = InetAddress.getByName(m_ip);

        String eventconf =
            "<events xmlns=\"http://xmlns.opennms.org/xsd/eventconf\">\n" +
            " <global>\n" +
            "  <security>\n" +
            "   <doNotOverride>logmsg</doNotOverride>\n" +
            "   <doNotOverride>operaction</doNotOverride>\n" +
            "   <doNotOverride>autoaction</doNotOverride>\n" +
            "   <doNotOverride>tticket</doNotOverride>\n" +
            "   <doNotOverride>script</doNotOverride>\n" +
            "  </security>\n" +
            " </global>\n" +
            "\n" +
            " <event>\n" +
            "  <mask>\n" +
            "   <maskelement>\n" +
            "    <mename>id</mename>\n" +
            "    <mevalue>.1.3.6.1.2.1.15.7</mevalue>\n" +
            "   </maskelement>\n" +
            "   <maskelement>\n" +
            "    <mename>generic</mename>\n" +
            "    <mevalue>6</mevalue>\n" +
            "   </maskelement>\n" +
            "   <maskelement>\n" +
            "    <mename>specific</mename>\n" +
            "    <mevalue>1</mevalue>\n" +
            "   </maskelement>\n" +
            "  </mask>\n" +
            "  <uei>uei.opennms.org/IETF/BGP/traps/bgpEstablished</uei>\n" +
            "  <event-label>BGP4-MIB defined trap event: bgpEstablished</event-label>\n" +
            "  <descr>&lt;p&gt;The BGP Established event is generated when\n" +
            "   the BGP FSM enters the ESTABLISHED state.&lt;/p&gt;&lt;table&gt;\n" +
            "   &lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" +
            "   bgpPeerLastError&lt;/b&gt;&lt;/td&gt;&lt;td&gt;%parm[#1]%\n" +
            "   &lt;/td&gt;&lt;td&gt;&lt;p;&gt;&lt;/p&gt;&lt;/td;&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" +
            "   bgpPeerState&lt;/b&gt;&lt;/td&gt;&lt;td&gt;%parm[#2]%\n" +
            "   &lt;/td&gt;&lt;td&gt;&lt;p;&gt;\n" +
            "   idle(1) connect(2) active(3) opensent(4) openconfirm(5) established(6)&lt;/p&gt;\n" +
            "   &lt;/td;&gt;&lt;/tr&gt;&lt;/table&gt;\n" +
            "  </descr>\n" +
            "  <logmsg dest='logndisplay'>&lt;p&gt;BGP Event: FSM entered connected state.&lt;/p&gt;</logmsg>\n" +
            "  <severity>Normal</severity>\n" +
            " </event>\n" +
            "\n" +
            " <event>\n" +
            "  <mask>\n" +
            "   <maskelement>\n" +
            "    <mename>id</mename>\n" +
            "    <mevalue>.1.3.6.1.2.1.15.7</mevalue>\n" +
            "   </maskelement>\n" +
            "   <maskelement>\n" +
            "    <mename>generic</mename>\n" +
            "    <mevalue>6</mevalue>\n" +
            "   </maskelement>\n" +
            "   <maskelement>\n" +
            "    <mename>specific</mename>\n" +
            "    <mevalue>2</mevalue>\n" +
            "   </maskelement>\n" +
            "  </mask>\n" +
            "  <uei>uei.opennms.org/IETF/BGP/traps/bgpBackwardTransition</uei>\n" +
            "  <event-label>BGP4-MIB defined trap event: bgpBackwardTransition</event-label>\n" +
            "  <descr>&lt;p&gt;The BGPBackwardTransition Event is generated\n" +
            "   when the BGP FSM moves from a higher numbered\n" +
            "   state to a lower numbered state.&lt;/p&gt;&lt;table&gt;\n" +
            "   &lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" +
            "   bgpPeerLastError&lt;/b&gt;&lt;/td&gt;&lt;td&gt;%parm[#1]%\n" + 
            "   &lt;/td&gt;&lt;td&gt;&lt;p;&gt;&lt;/p&gt;&lt;/td;&gt;&lt;/tr&gt;&lt;tr&gt;&lt;td&gt;&lt;b&gt;\n" +
            "   bgpPeerState&lt;/b&gt;&lt;/td&gt;&lt;td&gt;%parm[#2]%\n" + 
            "   &lt;/td&gt;&lt;td&gt;&lt;p;&gt;\n" + 
            "   idle(1) connect(2) active(3) opensent(4) openconfirm(5) established(6)&lt;/p&gt;\n" +
            "   &lt;/td;&gt;&lt;/tr&gt;&lt;/table&gt;\n" +
            "  </descr>\n" + 
            "  <logmsg dest='discardtraps'>&lt;p&gt;BGP Event: FSM Backward Transistion.&lt;/p&gt;</logmsg>\n" + 
            "  <severity>Warning</severity>\n" +
            " </event>\n" +
            "\n" +
            " <event>\n" +
            "  <mask>\n" +
            "   <maskelement>\n" +
            "    <mename>generic</mename>\n" +
            "    <mevalue>0</mevalue>\n" +
            "   </maskelement>\n" +
            "  </mask>\n" +
            "  <uei>uei.opennms.org/generic/traps/SNMP_Cold_Start</uei>\n" +
            "  <event-label>OpenNMS-defined trap event: SNMP_Cold_Start</event-label>\n" +
            "  <descr>\n" +
            "    &lt;p&gt;A coldStart trap signifies that the sending\n" +
            "    protocol entity is reinitializing itself such that the\n" +
            "    agent's configuration or the protocol entity implementation\n" +
            "    may be altered.&lt;/p&gt;\n" +
            "  </descr>\n" +
            "  <logmsg dest='logndisplay'>\n" +
            "    Agent Up with Possible Changes (coldStart Trap)\n" +
            "    enterprise:%id% (%id%) args(%parm[##]%):%parm[all]%\n" +
            "  </logmsg>\n" +
            "  <severity>Normal</severity>\n" +
            " </event>\n" +
            "</events>";
        
        StringReader reader = new StringReader(eventconf);

        EventConfigurationManager.loadConfiguration(reader);

        TrapdIPMgr.clearKnownIpsMap();
        TrapdIPMgr.setNodeId(m_ip, m_nodeId);
    }
    
    protected void setUpTrapHandler(boolean newSuspectOnTrap) {
        MockTrapdConfig mockTrapdConfig = new MockTrapdConfig();
        mockTrapdConfig.setSnmpTrapPort(m_port);
        mockTrapdConfig.setNewSuspectOnTrap(newSuspectOnTrap);

        m_trapHandler = new TrapHandler();
        m_trapHandler.setTrapdConfig(mockTrapdConfig);
        m_trapHandler.setEventManager(m_eventMgr);
        m_trapHandler.init();
        m_trapHandler.start();
    }

    public void finishUp() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // do nothing
        }
        m_eventMgr.finishProcessingEvents();
        m_anticipator.verifyAnticipated(1000, 0, 0, 0, 0);
    }

    public void tearDown() {
        if (m_trapHandler != null) {
            m_trapHandler.stop();
            m_trapHandler = null;
        }
    }

    public void testV1TrapNoNewSuspect() throws Exception {
        TrapdIPMgr.clearKnownIpsMap();
        anticipateAndSend(false, false, "uei.opennms.org/default/trap", "v1",
                          null, 6, 1);
    }
    
    public void testV2TrapNoNewSuspect() throws Exception {
        TrapdIPMgr.clearKnownIpsMap();
        anticipateAndSend(false, false, "uei.opennms.org/default/trap",
                          "v2c", null, 6, 1);
    }
    
    public void testV1TrapNewSuspect() throws Exception {
        TrapdIPMgr.clearKnownIpsMap();
        anticipateAndSend(true, false, "uei.opennms.org/default/trap",
                          "v1", null, 6, 1);
    }
    
    public void testV2TrapNewSuspect() throws Exception {
        TrapdIPMgr.clearKnownIpsMap();
        anticipateAndSend(true, false, "uei.opennms.org/default/trap",
                          "v2c", null, 6, 1);
    }
    
    public void testV1EnterpriseIdAndGenericMatch() throws Exception {
        anticipateAndSend(false, true,
                          "uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                          "v1", ".1.3.6.1.2.1.15.7", 6, 1);
    }
    
    public void testV2EnterpriseIdAndGenericAndSpecificMatch()
        throws Exception {
        anticipateAndSend(false, true,
                          "uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                          "v2c", ".1.3.6.1.2.1.15.7", 6, 1);
    }

    public void testV2EnterpriseIdAndGenericAndSpecificMatchWithZero()
        throws Exception {
        anticipateAndSend(false, true,
                          "uei.opennms.org/IETF/BGP/traps/bgpEstablished",
                          "v2c", ".1.3.6.1.2.1.15.7.0", 6, 1);
    }
    
    public void testV2EnterpriseIdAndGenericAndSpecificMissWithExtraZeros()
        throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap", "v2c",
                          ".1.3.6.1.2.1.15.7.0.0", 6, 1);
    }

    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongGeneric()
        throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap", "v1",
                          ".1.3.6.1.2.1.15.7", 5, 1);
    }
    
    public void testV1EnterpriseIdAndGenericAndSpecificMissWithWrongSpecific()
        throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap", "v1",
                          ".1.3.6.1.2.1.15.7", 6, 50);
    }

    public void testV1GenericMatch() throws Exception {
        anticipateAndSend(false, true,
                          "uei.opennms.org/generic/traps/SNMP_Cold_Start",
                          "v1", null, 0, 0);
    }
    
    public void testV2GenericMatch() throws Exception {
        anticipateAndSend(false, true,
                          "uei.opennms.org/generic/traps/SNMP_Cold_Start",
                          "v2c", ".1.3.6.1.6.3.1.1.5.1", 0, 0);
    }
    
    public void testV1TrapDroppedEvent() throws Exception {
        anticipateAndSend(false, true, null, "v1", ".1.3.6.1.2.1.15.7", 6, 2);
    }
    
    public void testV2TrapDroppedEvent() throws Exception {
        anticipateAndSend(false, true, null, "v2c", ".1.3.6.1.2.1.15.7", 6, 2);
    }
    
    public void testV1TrapDefaultEvent() throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap",
                          "v1", null, 6, 1);
    }
    
    public void testV2TrapDefaultEvent() throws Exception {
        anticipateAndSend(false, true, "uei.opennms.org/default/trap",
                          "v2c", null, 6, 1);
    }
 
    public void testNodeGainedModifiesIpMgr() throws Exception {
        long nodeId = 2;
        setUpTrapHandler(true);
        
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

    public void testInterfaceReparentedModifiesIpMgr() throws Exception {
        long nodeId = 2;
        setUpTrapHandler(true);
        
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

    public void testInterfaceDeletedModifiesIpMgr() throws Exception {
        long nodeId = 0;
        setUpTrapHandler(true);
        
        anticipateEvent("uei.opennms.org/default/trap", m_ip, nodeId);

        Event event =
            anticipateEvent(EventConstants.INTERFACE_DELETED_EVENT_UEI,
                            m_ip, nodeId);
        m_eventMgr.sendNow(event);

        anticipateEvent("uei.opennms.org/internal/discovery/newSuspect",
                        m_ip, nodeId);

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
        Event event = new Event();
        event.setInterface(ip);
        event.setNodeid(nodeId);
        event.setUei(uei);
        m_anticipator.anticipateEvent(event);
        return event;
    }

    
    public void anticipateAndSend(boolean newSuspectOnTrap, boolean nodeKnown,
                                  String event,
                                  String version, String enterprise,
                                  int generic, int specific) throws Exception {
        setUpTrapHandler(newSuspectOnTrap);
        
        if (newSuspectOnTrap) {
            // Note: the nodeId will be zero because the node is not known
            anticipateEvent("uei.opennms.org/internal/discovery/newSuspect",
                            m_ip, 0);
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
        
        sendTrap(version, enterprise, generic, specific);
        
        finishUp();
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

    public void sendV1Trap(String enterprise, int generic, int specific)
        throws Exception {
        SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();
        pdu.setEnterprise(SnmpObjId.get(enterprise));
        pdu.setGeneric(generic);
        pdu.setSpecific(specific);
        pdu.setTimeStamp(0);
        pdu.setAgentAddress(m_localhost);
        
        pdu.send(m_localhost.getHostAddress(), m_port, "public");
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

        pdu.send(m_localhost.getHostAddress(), m_port, "public");
    }
}
