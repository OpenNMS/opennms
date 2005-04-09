//
//  $Id$
//

package org.opennms.netmgt.trapd;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.EventWrapper;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockTrapdConfig;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpOctetString;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpPduRequest;
import org.opennms.protocols.snmp.SnmpPduTrap;

public class TrapHandlerTest extends TestCase {
	TrapHandler m_trapHandler = null;

	EventAnticipator m_anticipator = null;

	MockEventIpcManager m_eventMgr = null;

	protected void setUp() throws Exception {
		MockUtil.setupLogging(true);
		MockUtil.resetLogLevel();
		
		m_anticipator = new EventAnticipator();

		m_eventMgr = new MockEventIpcManager();
		m_eventMgr.setEventAnticipator(m_anticipator);

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

	}

	protected void setUpTrapHandler(boolean newSuspectOnTrap) {
		MockTrapdConfig mockTrapdConfig = new MockTrapdConfig();
		mockTrapdConfig.setSnmpTrapPort(10000);
		mockTrapdConfig.setNewSuspectOnTrap(newSuspectOnTrap);

		m_trapHandler = new TrapHandler();
		m_trapHandler.setTrapdConfig(mockTrapdConfig);
		m_trapHandler.setEventManager(m_eventMgr);
		m_trapHandler.init();
		m_trapHandler.start();
		
		if (newSuspectOnTrap) {
			Event newSuspectEvent = new Event();
			newSuspectEvent.setInterface("127.0.0.1");
			newSuspectEvent.setNodeid(0);
			newSuspectEvent.setUei("uei.opennms.org/internal/discovery/newSuspect");
			System.out.println("Anticipating: " + new EventWrapper(newSuspectEvent));
			m_anticipator.anticipateEvent(newSuspectEvent);
		}
	}
	
	public void finishUp() throws InterruptedException {
		Thread.sleep(1000);
		
		m_eventMgr.finishProcessingEvents();
	
		m_anticipator.verifyAnticipated(1000, 0, 0, 0, 0);
	}

	public void tearDown() throws Exception {
		m_trapHandler.stop();
		m_trapHandler = null;
	}

	public void testV1TrapNoNewSuspect() throws UnknownHostException, InterruptedException {
		doTestTrap("v1", false);
	}
	public void testV1TrapNewSuspect() throws UnknownHostException, InterruptedException {
		doTestTrap("v1", true);
	}
	public void testV2TrapNoNewSuspect() throws UnknownHostException, InterruptedException {
		doTestTrap("v2c", false);
	}
	public void testV2TrapNewSuspect() throws UnknownHostException, InterruptedException {
		doTestTrap("v2c", true);
	}
	
	public void testV1BgpEstablished() throws UnknownHostException, InterruptedException {
		setUpTrapHandler(false);

		Event snmpEvent = new Event();
		snmpEvent.setInterface("127.0.0.1");
		snmpEvent.setNodeid(0);
		snmpEvent.setUei("uei.opennms.org/IETF/BGP/traps/bgpEstablished");
		System.out.println("Anticipating: " + new EventWrapper(snmpEvent));
		m_anticipator.anticipateEvent(snmpEvent);
		
		SnmpPduTrap pdu = new SnmpPduTrap();
		pdu.setEnterprise(".1.3.6.1.2.1.15.7");
		pdu.setGeneric(6);
		pdu.setSpecific(1);
		pdu.setAgentAddress(new SnmpIPAddress(InetAddress.getByName("127.0.0.1")));
		m_trapHandler.snmpReceivedTrap(null, InetAddress.getByName("127.0.0.1"), 10000,
				new SnmpOctetString("public".getBytes()), pdu);
		
		finishUp();
	}

	public void testV1ColdStart() throws InterruptedException, MarshalException, ValidationException, IOException {
		setUpTrapHandler(false);

		Event snmpEvent = new Event();
		snmpEvent.setInterface("127.0.0.1");
		snmpEvent.setNodeid(0);
		snmpEvent.setUei("uei.opennms.org/generic/traps/SNMP_Cold_Start");
		System.out.println("Anticipating: " + new EventWrapper(snmpEvent));
		m_anticipator.anticipateEvent(snmpEvent);
		
		SnmpPduTrap pdu = new SnmpPduTrap();
		pdu.setGeneric(0);
		pdu.setAgentAddress(new SnmpIPAddress(InetAddress.getByName("127.0.0.1")));
		m_trapHandler.snmpReceivedTrap(null, InetAddress.getByName("127.0.0.1"), 10000,
				new SnmpOctetString("public".getBytes()), pdu);

		finishUp();
	}
	
	public void testV1TrapDroppedEvent() throws UnknownHostException, InterruptedException {
		setUpTrapHandler(false);
		
		SnmpPduTrap pdu = new SnmpPduTrap();
		pdu.setEnterprise(".1.3.6.1.2.1.15.7");
		pdu.setGeneric(6);
		pdu.setSpecific(2);
		pdu.setAgentAddress(new SnmpIPAddress(InetAddress.getByName("127.0.0.1")));
		m_trapHandler.snmpReceivedTrap(null, InetAddress.getByName("127.0.0.1"), 10000,
				new SnmpOctetString("public".getBytes()), pdu);
		
		finishUp();
	}
	
	public void testV1TrapDefaultEvent() throws UnknownHostException, InterruptedException {
		setUpTrapHandler(false);

		Event snmpEvent = new Event();
		snmpEvent.setInterface("127.0.0.1");
		snmpEvent.setNodeid(0);
		snmpEvent.setUei("uei.opennms.org/default/trap");
		System.out.println("Anticipating: " + new EventWrapper(snmpEvent));
		m_anticipator.anticipateEvent(snmpEvent);
		
		SnmpPduTrap pdu = new SnmpPduTrap();
		pdu.setGeneric(6);
		pdu.setSpecific(1);
		pdu.setAgentAddress(new SnmpIPAddress(InetAddress.getByName("127.0.0.1")));
		m_trapHandler.snmpReceivedTrap(null, InetAddress.getByName("127.0.0.1"), 10000,
				new SnmpOctetString("public".getBytes()), pdu);
		
		finishUp();
	}
	
	public void doTestTrap(String version, boolean newSuspectOnTrap) throws UnknownHostException, InterruptedException {
		setUpTrapHandler(newSuspectOnTrap);

		Event snmpEvent = new Event();
		snmpEvent.setInterface("127.0.0.1");
		snmpEvent.setNodeid(0);
		snmpEvent.setUei("uei.opennms.org/default/trap");
		System.out.println("Anticipating: " + new EventWrapper(snmpEvent));
		m_anticipator.anticipateEvent(snmpEvent);
		
		if (version.equals("v1")) {
			SnmpPduTrap pdu = new SnmpPduTrap();
			pdu.setGeneric(6);
			pdu.setSpecific(1);
			pdu.setAgentAddress(new SnmpIPAddress(InetAddress.getByName("127.0.0.1")));
			m_trapHandler.snmpReceivedTrap(null, InetAddress.getByName("127.0.0.1"), 10000,
					new SnmpOctetString("public".getBytes()), pdu);
		} else if (version.equals("v2c")) {
			SnmpPduRequest pdu = new SnmpPduRequest(SnmpPduPacket.V2TRAP);
//			pdu.setGeneric(6);
//			pdu.setSpecific(1);
			m_trapHandler.snmpReceivedTrap(null, InetAddress.getByName("127.0.0.1"), 10000,
					new SnmpOctetString("public".getBytes()), pdu);
		} else {
			throw new UndeclaredThrowableException(null, "unsupported SNMP version for test: " + version); 
		}
		
		finishUp();
	}
}