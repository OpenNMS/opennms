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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class AbstractSnmpValueTest {

	/**
	 * This test is designed to test the issues in bug NMS-5281.
	 * 
	 * @see http://issues.opennms.org/browse/NMS-5281
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void testAllBytesDisplayable() throws UnsupportedEncodingException {
		String[] strings = new String[4];

		// These are German strings in non-UTF-8 encoding
		// ISO-8859-1
		strings[0] = "4465722041756674726167207775726465206572666f6c67726569636820616267657363686c6f7373656e2e2045732074726174656e206a65646f63682064696520666f6c67656e64656e20426564696e67756e67656e206175663a0a0a31204461746569656e2077757264656e20fc626572737072756e67656e2e0a";
		// ISO-8859-1
		strings[1] = "5352562d45584330312c205352562d45584330322d4261636b7570202d204d6f2d446f2c2046722c206d746c2e2d4232442d4d6f6e7461672d446f6e6e657273746167202d2d204465722041756674726167207363686c7567206d697420666f6c67656e64656d204665686c6572206665686c3a20446572204d656469656e736572766572206b6f6e6e7465206b65696e652056657262696e64756e67207a756d2072656d6f74656e20436f6d7075746572206865727374656c6c656e2e204175662064656d2072656d6f74656e20436f6d7075746572206ce4756674206df6676c69636865727765697365206e69636874204261636b75702045786563200d0a52656d6f7465204167656e7420666f722057696e646f77732f4c696e75782e205374656c6c656e20536965207369636865722c206461737320646965206b6f7272656b74652056657273696f6e20766f6e2052656d6f7465204167656e74206175662064656d205a69656c636f6d707574657220696e7374616c6c696572742069737420756e64206175";
		// ISO-8859-1
		strings[2] = "56697275732f4d616c776172653a2054524f4a5f47452e443843463531313520436f6d70757465723a205352562d4653303220446f6de46e653a2054636c73675c2044617465693a20443a5c36352d6f323031302d70726f66696c655c73616e6472612e7265696e68617264745c55504d5f50726f66696c655c417070446174615c526f616d696e675c4d6963726f736f66745c75706361706933322e65786520446174756d2f5568727a6569743a2032302e30352e323031342030383a30313a33322045726765626e69733a20476573e4756265727420";
		// ISO-8859-1
		strings[3] = "56697275732f4d616c776172653a2045696361725f746573745f66696c6520436f6d70757465723a2049542d30303220446f6de46e653a2050632d6974206f686e65206669726577616c6c5c2044617465693a20473a5c6569636172202d204b6f7069655c6569636172202d204b6f7069652e746172202865696361722e636f6d2920446174756d2f5568727a6569743a2030312e31312e323031332031313a34383a34382045726765626e69733a2045732077757264652065696e20566972757320656e746465636b742e2053e4756265726e206e69636874206df6676c6963682e202851756172616e74e46e652920";

		for (String string : strings) {
			System.out.println(new String(hexStringToBytes(string), StandardCharsets.UTF_8));
			System.out.println(new String(hexStringToBytes(string), StandardCharsets.ISO_8859_1));
		}

		for (String string : strings) {
			System.out.println(new String(hexStringToBytes(string)));
			assertTrue(new String(hexStringToBytes(string), StandardCharsets.ISO_8859_1), AbstractSnmpValue.allBytesDisplayable(hexStringToBytes(string)));
		}
	}

	/**
	 * This test is designed to test the issues in bug NMS-5281.
	 * 
	 * @see http://issues.opennms.org/browse/NMS-5281
	 * 
	 * @throws UnsupportedEncodingException
	 */
	@Test
	public void testStringWithAllBytesNotDisplayable() throws UnsupportedEncodingException {
		String[] badStrings = new String[4];

		// Dell DRAC IPMI "Platform event traps"
		// These are binary data and should be rejected as unreadable
		badStrings[0] = "44454c4c4d0010518033c6c04f58344a00001b46ce0cffff202100ff0018000fffff000000000019000002a201007673706865726530352e74636c73672e6c6f63616cc1";
		badStrings[1] = "44454c4c340010538046b5c04f54344a00001db612e7ffff202100ff0018000fffff000000000019000002a201007673706865726530312e74636c73672e6c6f63616cc1";
		badStrings[2] = "44454c4c360010518033b2c04f58344a00001e912535ffff202100ff0018000fffff000000000019000002a201007372762d666230312e74636c73672e6c6f63616c00c1";

		// Windows-1252?? For some reason this sequence has a hyphen with code 0x96.
		// There's no elegant way that we can handle this because virtually every
		// code is valid in Windows-1252 so it will be difficult to distinguish between
		// it and binary data.
		badStrings[3] = "42656e61636872696368746967756e67209620546573746d656c64756e6720";

		for (String string : badStrings) {
			assertFalse(new String(hexStringToBytes(string), "Windows-1252"), AbstractSnmpValue.allBytesDisplayable(hexStringToBytes(string)));
		}
	}

	private static byte[] hexStringToBytes(String hexString) {
		assertTrue(hexString.length() % 2 == 0);
		byte[] retval = new byte[hexString.length() / 2];
		for (int i = 0; i * 2 < hexString.length(); i++) {
			try {
				retval[i] = (byte)Integer.parseInt(hexString.substring(i * 2,  (i * 2) + 2), 16);
			} catch (NumberFormatException e) {
				System.out.println("Error while processing i = " + i);
				e.printStackTrace();
			}
		}
		return retval;
	}
}
