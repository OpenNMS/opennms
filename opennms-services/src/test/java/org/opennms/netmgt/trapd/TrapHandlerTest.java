//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
//
//  $Id$
//

package org.opennms.netmgt.trapd;

import java.io.StringReader;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.opennms.core.utils.Base64;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventConfigurationManager;
import org.opennms.netmgt.mock.EventAnticipator;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.mock.MockTrapdConfig;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpTrapBuilder;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpV1TrapBuilder;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.PropertySettingTestSuite;
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
            " <event>\n" +
            "  <mask>\n" +
            "   <maskelement>\n" +
            "    <mename>id</mename>\n" +
            "    <mevalue>.1.3.6.1.4.1.11.2.14.12.1</mevalue>\n" +
            "   </maskelement> <maskelement>\n"+
            "    <mename>generic</mename>\n" +
            "    <mevalue>6</mevalue>\n" + 
            "   </maskelement>\n" +
            "   <maskelement>\n" +
            "    <mename>specific</mename>\n" +
            "    <mevalue>5</mevalue> \n" +
            "   </maskelement>\n" +
            "   <varbind textual-convention=\"MacAddress\"> \n" +
            "    <vbnumber>3</vbnumber>\n" +
            "    <vbvalue>5</vbvalue> \n" +
            "   </varbind> \n" +
            "  </mask>\n" +
            "  <uei>uei.opennms.org/vendor/HP/traps/hpicfFaultFinderTrap</uei>\n" +
            "  <event-label>HP-ICF-FAULT-FINDER-MIB defined trap event: hpicfFaultFinderTrap</event-label>\n" +
            "  <descr>\n" +
            "    This notification is sent whenever the Fault\n" +
            "       Finder creates an entry in the\n" +
            "       hpicfFfLogTable.\n" +
            "       hpicfFfLogFaultType\n" +
            "       %parm[#1]%\n" +
            "          badDriver(1) badXcvr(2)\n" +
            "            badCable(3) tooLongCable(4) overBandwidth(5) bcastStorm(6) partition(7)\n" +
            "            misconfiguredSQE(8) polarityReversal(9) networkLoop(10) lossOfLink(11)\n" +
            "            portSecurityViolation(12) backupLinkTransition(13) meshingFault(14)\n" +
            "            fanFault(15) rpsFault(16) stuck10MbFault(17) lossOfStackMember(18)\n" +
            "            hotSwapReboot(19)\n" +
            "         hpicfFfLogAction\n" +
            "         %parm[#2]%\n" +
            "          none(1) warn(2) warnAndDisable(3)\n" +
            "            warnAndSpeedReduce(4) warnAndSpeedReduceAndDisable(5)\n" +
            "         hpicfFfLogSeverity\n" +
            "         %parm[#3]%\n" +
            "          informational(1) medium(2)\n" +
            "            critical(3)\n" +
            "         hpicfFfFaultInfoURL\n" +
            "         %parm[#4]%\n" +
            "  </descr> \n" +
            "  <logmsg dest='logndisplay'>HP Event: ICF Hub Fault Found.</logmsg>\n" +
            "  <severity>Major</severity> \n" +
    		    " </event>\n" +
			" <event>\n" +
			"  <mask>\n" +
			"   <maskelement>\n" +
			"    <mename>id</mename>\n" +
			"    <mevalue>.1.3.6.1.4.1.14179.2.6.3</mevalue>\n" +
			"   </maskelement>\n" +
			"   <maskelement>\n" +
			"    <mename>generic</mename>\n" +
			"    <mevalue>6</mevalue>\n" +
			"   </maskelement>\n" +
			"   <maskelement>\n" +
			"    <mename>specific</mename>\n" +
			"    <mevalue>38</mevalue>\n" +
			"   </maskelement>\n" +
            "   <varbind textual-convention=\"MacAddress\"> \n" +
            "    <vbnumber>1</vbnumber>\n" +
            "    <vbvalue>00:14:f1:ad:a7:50</vbvalue> \n" +
            "   </varbind> \n" +
			"  </mask>\n" +
			"  <uei>uei.opennms.org/vendor/cisco/bsnAPNoiseProfileUpdatedToPass</uei>\n" +
			"  <event-label>AIRESPACE-WIRELESS-MIB defined trap event: bsnAPNoiseProfileUpdatedToPass</event-label>\n" +
			"  <descr>\n" +
			"   &lt;p&gt;When Noise Profile state changes from FAIL to PASS, notification\n" +
			"   will be sent with Dot3 MAC address of Airespace AP and slot ID\n" +
			"   of Airespace AP IF. This trap sending can be enable/disable using\n" +
			"   bsnRrmProfileTrapControlFlag &lt;/p&gt;&lt;table&gt;\n" +
			"   &lt;tr&gt;&lt;td&gt;&lt;b&gt;\n\n" +
			"   bsnAPDot3MacAddress&lt;/b&gt;&lt;/td&gt;&lt;td&gt;\n" +
			"   %parm[#1]%;&lt;/td&gt;&lt;td&gt;&lt;p;&gt;&lt;/p&gt;&lt;/td;&gt;&lt;/tr&gt;\n" +
			"   &lt;tr&gt;&lt;td&gt;&lt;b&gt;\n\n" +
			"   bsnAPIfSlotId&lt;/b&gt;&lt;/td&gt;&lt;td&gt;\n" +
			"   %parm[#2]%;&lt;/td&gt;&lt;td&gt;&lt;p;&gt;&lt;/p&gt;&lt;/td;&gt;&lt;/tr&gt;&lt;/table&gt;" +
			"  </descr>\n" +
			"  <logmsg dest='logndisplay'>&lt;p&gt;\n" +
			"   bsnAPNoiseProfileUpdatedToPass trap received\n" +
			"   bsnAPDot3MacAddress=%parm[#1]%\n" +
			"   bsnAPIfSlotId=%parm[#2]%&lt;/p&gt;\n" +
			"  </logmsg>\n" +
			"  <severity>Cleared</severity>\n" +
			" </event>" +
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


	// FIXME: these exist to provide testing for the new Textual Convention feature
	// See EventConfDataTest for the other part of this testing
	public void FIXMEtestV1EnterpriseIdAndGenericAndSpecificAndMatchWithVarbindsAndTC()
	throws Exception {
		SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

		LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
		varbinds.put(".1.3.6.1.4.1.14179.2.6.2.20.0", valueFactory.getOctetString(new byte[]{(byte)0x00,(byte)0x14,(byte)0xf1,(byte)0xad,(byte)0xa7,(byte)0x50}));
		anticipateAndSend(false, true,
				"uei.opennms.org/vendor/cisco/bsnAPNoiseProfileUpdatedToPass",
				"v1", ".1.3.6.1.4.1.14179.2.6.3", 6, 38, varbinds);
	}

	// FIXME: these exist to provide testing for the new Textual Convention feature
	public void FIXMEtestV2EnterpriseIdAndGenericAndSpecificAndMatchWithVarbindsAndTC()
	throws Exception {
		SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();

		byte[] macAddr = new byte[]{(byte)0x00,(byte)0x14,(byte)0xf1,(byte)0xad,(byte)0xa7,(byte)0x50};

		String encoded = new String(Base64.encodeBase64(macAddr));
		byte[] decodeBytes = Base64.decodeBase64(encoded.toCharArray());
		
		assertByteArrayEquals(macAddr, decodeBytes);

		// XXX: this is a problem.. putting the bytes into a string and taking them
		// back out.. does not produce the same results
		String decoded = new String(macAddr);
		byte[] roundTripMacAddr = decoded.getBytes();
		
		assertByteArrayEquals(macAddr, roundTripMacAddr);
		
		
		LinkedHashMap<String, SnmpValue> varbinds = new LinkedHashMap <String, SnmpValue>();
		varbinds.put(".1.3.6.1.4.1.14179.2.6.2.20.0", valueFactory.getOctetString(macAddr));
		anticipateAndSend(false, true,
				"uei.opennms.org/vendor/cisco/bsnAPNoiseProfileUpdatedToPass",
				"v2c", ".1.3.6.1.4.1.14179.2.6.3", 6, 38, varbinds);
	}

	private void assertByteArrayEquals(byte[] macAddr, byte[] bytes) {
		assertEquals("expect length: "+macAddr.length, macAddr.length, bytes.length);
		for (int i = 0; i < macAddr.length; i++) {
			assertEquals("Expected byte "+i+" to match", macAddr[i], bytes[i]);
		}
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


	public void anticipateAndSend(boolean newSuspectOnTrap, boolean nodeKnown,
			String event,
			String version, String enterprise,
			int generic, int specific, LinkedHashMap<String, SnmpValue> varbinds) throws Exception {
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

		sendTrap(version, enterprise, generic, specific, varbinds);

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

		pdu.send(m_localhost.getHostAddress(), m_port, "public");
	}

	public void sendV1Trap(String enterprise, int generic, int specific, LinkedHashMap<String, SnmpValue> varbinds)
	throws Exception {
		SnmpV1TrapBuilder pdu = SnmpUtils.getV1TrapBuilder();
		pdu.setEnterprise(SnmpObjId.get(enterprise));
		pdu.setGeneric(generic);
		pdu.setSpecific(specific);
		pdu.setTimeStamp(0);
		pdu.setAgentAddress(m_localhost);
		Iterator it = varbinds.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			pdu.addVarBind(SnmpObjId.get((String) pairs.getKey()), (SnmpValue) pairs.getValue());
		}
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
		Iterator it = varbinds.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			pdu.addVarBind(SnmpObjId.get((String) pairs.getKey()), (SnmpValue) pairs.getValue());
		}

		pdu.send(m_localhost.getHostAddress(), m_port, "public");
	}

}
