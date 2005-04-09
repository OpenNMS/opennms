//
//  $Id$
//

package org.opennms.netmgt.trapd;

import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.EventWrapper;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockTrapdConfig;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.opennms.protocols.snmp.SnmpTimeTicks;
import org.opennms.protocols.snmp.SnmpVarBind;

public class TrapHandlerTest extends TestCase {
	TrapHandler m_trapHandler = null;

	EventAnticipator m_anticipator = null;

	MockEventIpcManager m_eventMgr = null;
	
	InetAddress m_localhost = null;
	
	int m_port = 10000; 

	protected void setUp() throws Exception {
		MockUtil.setupLogging(true);
		MockUtil.resetLogLevel();
		
		m_anticipator = new EventAnticipator();

		m_eventMgr = new MockEventIpcManager();
		m_eventMgr.setEventAnticipator(m_anticipator);
		
		m_localhost = InetAddress.getByName("127.0.0.1");

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
			"	&lt;p&gt;A coldStart trap signifies that the sending\n" +
			"	protocol entity is reinitializing itself such that the\n" +
			"	agent's configuration or the protocol entity implementation\n" +
			"	may be altered.&lt;/p&gt;\n" +
			"  </descr>\n" +
			"  <logmsg dest='logndisplay'>\n" +
			"	Agent Up with Possible Changes (coldStart Trap)\n" +
			"	enterprise:%id% (%id%) args(%parm[##]%):%parm[all]%\n" +
			"  </logmsg>\n" +
			"  <severity>Normal</severity>\n" +
			" </event>\n" +
			"</events>";
		
		StringReader reader = new StringReader(eventconf);

		org.opennms.netmgt.eventd.EventConfigurationManager.loadConfiguration(reader);

		setUpTrapHandler(false);
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

	public void testV1TrapNoNewSuspect() {
		doTestTrap("v1", false);
	}
	public void testV1TrapNewSuspect() {
		doTestTrap("v1", true);
	}
	public void testV2TrapNoNewSuspect() {
		doTestTrap("v2c", false);
	}
	public void testV2TrapNewSuspect() {
		doTestTrap("v2c", true);
	}
	
	public void testV1BgpEstablished() {
		anticipateEvent("uei.opennms.org/IETF/BGP/traps/bgpEstablished");
		sendV1Trap(".1.3.6.1.2.1.15.7", 6, 1);
		finishUp();
	}

	public void testV1ColdStart() {
		anticipateEvent("uei.opennms.org/generic/traps/SNMP_Cold_Start");
		sendV1Trap(".0.0", 0, 0);
		finishUp();
	}
	
	public void testV1TrapDroppedEvent() {
		sendV1Trap(".1.3.6.1.2.1.15.7", 6, 2);
		finishUp();
	}
	
	public void testV1TrapDefaultEvent() {
		anticipateEvent("uei.opennms.org/default/trap");
		sendV1Trap(".0.0", 6, 1);
		finishUp();
	}
	
	public void doTestTrap(String version, boolean newSuspectOnTrap) {
		if (newSuspectOnTrap) {
			tearDown();
			setUpTrapHandler(true);
			anticipateEvent("uei.opennms.org/internal/discovery/newSuspect");
		}

		anticipateEvent("uei.opennms.org/default/trap");
		
		if (version.equals("v1")) {
			sendV1Trap(".0.0", 6, 1);
		} else if (version.equals("v2c")) {
			sendV2Trap(".0.0", 6, 1);
		} else {
			throw new UndeclaredThrowableException(null, "unsupported SNMP version for test: " + version); 
		}
		
		finishUp();
	}

	public void anticipateEvent(String uei) {
		Event event = new Event();
		event.setInterface("127.0.0.1");
		event.setNodeid(0);
		event.setUei(uei);
		System.out.println("Anticipating: " + new EventWrapper(event));
		m_anticipator.anticipateEvent(event);
	}

	public void sendV1Trap(String enterprise, int generic, int specific) {
		SnmpPduTrap pdu = new SnmpPduTrap();
		pdu.setEnterprise(enterprise);
		pdu.setGeneric(generic);
		pdu.setSpecific(specific);
		pdu.setAgentAddress(new SnmpIPAddress(m_localhost));
		m_trapHandler.snmpReceivedTrap(null, m_localhost, m_port,
				new SnmpOctetString("public".getBytes()), pdu);
	}
	
	public void sendV2Trap(String enterprise, int generic, int specific) {
		SnmpPduRequest pdu = new SnmpPduRequest(SnmpPduPacket.V2TRAP);
		pdu.addVarBindAt(0, new SnmpVarBind(".1.3.6.1.2.1.1.3.0", new SnmpTimeTicks(0)));
		pdu.addVarBindAt(1, new SnmpVarBind(".1.3.6.1.6.3.1.1.4.1.0", new SnmpObjectId()));
		// FIXME: these lines should be doing something. :-)
//		pdu.setEnterprise(enterprise);
//		pdu.setGeneric(generic);
//		pdu.setSpecific(specific);
		m_trapHandler.snmpReceivedTrap(null, m_localhost, m_port,
				new SnmpOctetString("public".getBytes()), pdu);
	}
}