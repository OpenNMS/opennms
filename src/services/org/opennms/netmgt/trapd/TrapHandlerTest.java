//
//  $Id$
//

package org.opennms.netmgt.trapd;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import junit.framework.TestCase;

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
	}

	public void tearDown() throws Exception {
		m_trapHandler.stop();
		m_trapHandler = null;
		m_eventMgr.finishProcessingEvents();
		
		m_anticipator.verifyAnticipated(1000, 0, 0, 0, 0);
	}

	public void testV1TrapNoNewSuspect() throws UnknownHostException, InterruptedException {
		doTestTrap(1, false);
	}
	public void testV1TrapNewSuspect() throws UnknownHostException, InterruptedException {
		doTestTrap(1, true);
	}
	public void testV2TrapNoNewSuspect() throws UnknownHostException, InterruptedException {
		doTestTrap(2, false);
	}
	public void testV2TrapNewSuspect() throws UnknownHostException, InterruptedException {
		doTestTrap(2, true);
	}
	
	// TODO: enable this test when I start to implement the features for bug 898
	public void xtestV1TrapNoEvent() throws UnknownHostException, InterruptedException {
		MockTrapdConfig mockTrapdConfig = new MockTrapdConfig();
		mockTrapdConfig.setSnmpTrapPort(10000);
		mockTrapdConfig.setNewSuspectOnTrap(false);
			
		m_trapHandler = new TrapHandler();
		m_trapHandler.setTrapdConfig(mockTrapdConfig);
		m_trapHandler.setEventManager(m_eventMgr);
		m_trapHandler.init();
		m_trapHandler.start();
		
		SnmpPduTrap pdu = new SnmpPduTrap();
		pdu.setAgentAddress(new SnmpIPAddress(InetAddress.getByName("127.0.0.1")));
		m_trapHandler.snmpReceivedTrap(null, InetAddress.getByName("127.0.0.1"), 10000,
				new SnmpOctetString("public".getBytes()), pdu);
		
		Thread.sleep(1000);
	}
	
	public void testV1TrapDefaultEvent() throws UnknownHostException, InterruptedException {
		MockTrapdConfig mockTrapdConfig = new MockTrapdConfig();
		mockTrapdConfig.setSnmpTrapPort(10000);
		mockTrapdConfig.setNewSuspectOnTrap(false);

		Event snmpEvent = new Event();
		snmpEvent.setInterface("127.0.0.1");
		snmpEvent.setNodeid(0);
		snmpEvent.setUei("uei.opennms.org/default/event"); // XXX should this be default/trap???
		System.out.println("Anticipating: " + new EventWrapper(snmpEvent));
		m_anticipator.anticipateEvent(snmpEvent);
			
		m_trapHandler = new TrapHandler();
		m_trapHandler.setTrapdConfig(mockTrapdConfig);
		m_trapHandler.setEventManager(m_eventMgr);
		m_trapHandler.init();
		m_trapHandler.start();
		
		SnmpPduTrap pdu = new SnmpPduTrap();
		pdu.setAgentAddress(new SnmpIPAddress(InetAddress.getByName("127.0.0.1")));
		m_trapHandler.snmpReceivedTrap(null, InetAddress.getByName("127.0.0.1"), 10000,
				new SnmpOctetString("public".getBytes()), pdu);
		
		Thread.sleep(1000);
	}
	
	public void doTestTrap(int version, boolean newSuspectOnTrap) throws UnknownHostException, InterruptedException {
		setUpTrapHandler(newSuspectOnTrap);
		
		Event snmpEvent = new Event();
		snmpEvent.setInterface("127.0.0.1");
		snmpEvent.setNodeid(0);
		snmpEvent.setUei("uei.opennms.org/default/event");
		System.out.println("Anticipating: " + new EventWrapper(snmpEvent));
		m_anticipator.anticipateEvent(snmpEvent);
		
		if (newSuspectOnTrap) {
			Event newSuspectEvent = new Event();
			newSuspectEvent.setInterface("127.0.0.1");
			newSuspectEvent.setNodeid(0);
			newSuspectEvent.setUei("uei.opennms.org/internal/discovery/newSuspect");
			System.out.println("Anticipating: " + new EventWrapper(newSuspectEvent));
			m_anticipator.anticipateEvent(newSuspectEvent);
		}
		
		if (version == 1) {
			SnmpPduTrap pdu = new SnmpPduTrap();
			pdu.setAgentAddress(new SnmpIPAddress(InetAddress.getByName("127.0.0.1")));
			m_trapHandler.snmpReceivedTrap(null, InetAddress.getByName("127.0.0.1"), 10000,
					new SnmpOctetString("public".getBytes()), pdu);
		} else if (version == 2) {
			m_trapHandler.snmpReceivedTrap(null, InetAddress.getByName("127.0.0.1"), 10000,
					new SnmpOctetString("public".getBytes()), new SnmpPduRequest(SnmpPduPacket.V2TRAP));
		} else {
			throw new UndeclaredThrowableException(null, "unsupported SNMP version for test: " + version); 
		}
		
		Thread.sleep(1000);
	}
}