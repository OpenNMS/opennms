/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.model.events.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.netmgt.xml.event.Parm;

public class SyntaxToEventTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testProcessSyntaxZeros() {
		SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();
		assertNotNull(valueFactory);
		byte [] macAddr = {000, 000, 000, 000, 000, 000};

		SnmpValue octetString = valueFactory.getOctetString(macAddr);

		Parm parm = SyntaxToEvent.processSyntax("Test",octetString);

		assertEquals("Test", parm.getParmName());
		assertEquals("00:00:00:00:00:00", parm.getValue().getContent());	
	}

	@Test
	public void testProcessSyntaxWithNull() {
		SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();
		assertNotNull(valueFactory);
		byte [] macAddr = {0x00, 0x55, 0x55, 0x55, 0x55, 0x55};

		SnmpValue octetString = valueFactory.getOctetString(macAddr);

		Parm parm = SyntaxToEvent.processSyntax("Test",octetString);

		assertEquals("Test", parm.getParmName());
		assertEquals(EventConstants.XML_ENCODING_MAC_ADDRESS, parm.getValue().getEncoding());
		assertEquals("00:55:55:55:55:55", parm.getValue().getContent());	
	}

	/**
	 * We are allowing NULL in the last position of the string in case there
	 * are any SNMP agents sending OctetString values in this format.
	 * 
	 * @see SnmpUtils#allBytesDisplayable()
	 */
	@Test
	public void testProcessSyntaxWithTerminatingNull() {
		SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();
		assertNotNull(valueFactory);
		byte [] macAddr = {0x55, 0x55, 0x55, 0x55, 0x55, 0x00};

		SnmpValue octetString = valueFactory.getOctetString(macAddr);

		Parm parm = SyntaxToEvent.processSyntax("Test",octetString);

		assertEquals("Test", parm.getParmName());
		assertEquals(EventConstants.XML_ENCODING_TEXT, parm.getValue().getEncoding());
		// I'm not sure what is converting the NULL char to a "."...
		assertEquals("UUUUU.", parm.getValue().getContent());	
	}

	@Test
	public void testProcessSyntaxForMacAddresses() {
		SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();
		assertNotNull(valueFactory);
		byte [] macAddr = {001, 002, 003, 004, 005, 006};

		SnmpValue octetString = valueFactory.getOctetString(macAddr);

		Parm parm = SyntaxToEvent.processSyntax("testMacAddress",octetString);

		assertEquals("testMacAddress", parm.getParmName());
		assertEquals(EventConstants.XML_ENCODING_MAC_ADDRESS, parm.getValue().getEncoding());
		assertEquals("01:02:03:04:05:06", parm.getValue().getContent());

		macAddr = new byte[] { (byte) 0x80, (byte) 0x81, (byte) 0x8C, (byte) 0x8F, (byte) 0xFF, (byte) 0x05 };

		octetString = valueFactory.getOctetString(macAddr);

		parm = SyntaxToEvent.processSyntax("testMacAddress",octetString);

		assertEquals("testMacAddress", parm.getParmName());
		assertEquals(EventConstants.XML_ENCODING_MAC_ADDRESS, parm.getValue().getEncoding());
		assertEquals("80:81:8C:8F:FF:05", parm.getValue().getContent());

		macAddr = new byte[] { 0101, 0101, 0101, 0101, 0101, 0101 };

		octetString = valueFactory.getOctetString(macAddr);

		parm = SyntaxToEvent.processSyntax("otherDataType",octetString);

		assertEquals("otherDataType", parm.getParmName());
		assertEquals(EventConstants.XML_ENCODING_TEXT, parm.getValue().getEncoding());
		assertEquals("AAAAAA", parm.getValue().getContent());

		parm = SyntaxToEvent.processSyntax("testMacAddress",octetString);

		assertEquals("testMacAddress", parm.getParmName());
		assertEquals(EventConstants.XML_ENCODING_MAC_ADDRESS, parm.getValue().getEncoding());
		assertEquals("41:41:41:41:41:41", parm.getValue().getContent());
	}

    @Test
	public void testProcessSyntaxForUnprintableBytes() {
		SnmpValueFactory valueFactory = SnmpUtils.getValueFactory();
		assertNotNull(valueFactory);
		byte [] ipAddr = { (byte) 0x4d, (byte) 0x5f, (byte) 0xf1, (byte) 0x95};

		SnmpValue octetString = valueFactory.getOctetString(ipAddr);

		Parm parm = SyntaxToEvent.processSyntax("testOtherData",octetString);

		assertEquals("testOtherData", parm.getParmName());
		assertEquals(EventConstants.XML_ENCODING_BASE64, parm.getValue().getEncoding());
		assertEquals("TV/xlQ==", parm.getValue().getContent());

		byte [] macAddr = new byte[] { (byte) 0x4c, (byte) 0x66, (byte) 0x41, (byte) 0xd9, (byte) 0x9a, (byte) 0xf6 };

		octetString = valueFactory.getOctetString(macAddr);

		parm = SyntaxToEvent.processSyntax("testSomeMacAddress", octetString);
		assertEquals("testSomeMacAddress", parm.getParmName());
		assertEquals(EventConstants.XML_ENCODING_MAC_ADDRESS, parm.getValue().getEncoding());
		assertEquals("4C:66:41:D9:9A:F6", parm.getValue().getContent());
	}
}
