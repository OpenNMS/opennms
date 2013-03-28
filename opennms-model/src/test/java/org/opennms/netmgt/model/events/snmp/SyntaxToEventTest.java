/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model.events.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.EventConstants;
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
}
