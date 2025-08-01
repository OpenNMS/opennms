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
