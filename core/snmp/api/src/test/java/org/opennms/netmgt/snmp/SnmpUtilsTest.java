/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Test;

public class SnmpUtilsTest {

	/**
	 * This test is designed to test the issues in bug NMS-5281.
	 * 
	 * @see http://issues.opennms.org/browse/NMS-5281
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void testGetProtoCounter63Value() {
		for (byte[] bytes : new byte[][] {
			// Not all decimal digits
			"abcdef01".getBytes(StandardCharsets.UTF_8),
			// Highest 63-bit value
			{ (byte)0x79, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff },
			// Zero
			{ 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 },
			// One
			{ 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x1 }
		}) {
			assertNotNull(Arrays.toString(bytes), SnmpUtils.getProtoCounter63Value(bytes));
		}

		for (byte[] bytes : new byte[][] {
			// Not 8 bytes
			"abcdef".getBytes(StandardCharsets.UTF_8),
			// All decimal digits
			"01234567".getBytes(StandardCharsets.UTF_8),
			// All decimal digits
			"00000000".getBytes(StandardCharsets.UTF_8),
			// All decimal digits
			"11111111".getBytes(StandardCharsets.UTF_8),
			// All decimal digits
			"99999999".getBytes(StandardCharsets.UTF_8),
			// Special case for "not supported"
			{ (byte)0x80, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 },
			// 64-bit value
			{ (byte)0xff, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0 },
			// 64-bit value
			{ (byte)0x80, (byte)0x80, (byte)0x80, (byte)0x80, (byte)0x80, (byte)0x80, (byte)0x80, (byte)0x80 },
			// 64-bit value
			{ (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff, (byte)0xff }
		}) {
			assertNull(Arrays.toString(bytes), SnmpUtils.getProtoCounter63Value(bytes));
		}
	}
}
