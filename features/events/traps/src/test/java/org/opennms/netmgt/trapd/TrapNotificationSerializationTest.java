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
package org.opennms.netmgt.trapd;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

/**
 * This test used to test that the objects were {@link Serializable} but
 * now it makes sure that they are not {@link Serializable} since we are
 * using JAXB serialization for these objects.
 */
public class TrapNotificationSerializationTest {
	
	private InetAddress inetAddress;
	
	@Before
	public void setup() throws UnknownHostException{
		inetAddress = InetAddress.getByName("127.0.0.1");
	}
	
	@Test
	public void testsnmp4JV1Serialization() throws UnknownHostException {
		// create instance of Snmp4JV1

		PDUv1 snmp4JV1TrapPdu = new PDUv1();
		snmp4JV1TrapPdu.setType(PDU.V1TRAP);
		snmp4JV1TrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
				new OctetString("mockhost")));
		snmp4JV1TrapPdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3"),
				new OctetString("mockhost")));
		snmp4JV1TrapPdu.add(new VariableBinding(new OID(
				".1.3.6.1.6.3.1.1.4.1.0"), new OctetString("mockhost")));

		TrapInformation snmp4JV1Trap = new Snmp4JTrapNotifier.Snmp4JV1TrapInformation(
				inetAddress, new String("public"), snmp4JV1TrapPdu);
		assertTrue(writeTrapNotificationObject(snmp4JV1Trap));
	}

	@Test
	public void testsnmp4JV2cSerialization() throws UnknownHostException {
		// create instance of snmp4JV2cTrap
		PDU snmp4JV2cTrapPdu = new PDU();
		snmp4JV2cTrapPdu.setType(PDU.TRAP);
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
				new OctetString("mockhost")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3"),
				new OctetString("mockhost")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(
				".1.3.6.1.6.3.1.1.4.1.0"), new OctetString("mockhost")));

		TrapInformation snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2V3TrapInformation(
				inetAddress, new String("public"), snmp4JV2cTrapPdu);
		assertTrue(writeTrapNotificationObject(snmp4JV2cTrap));
	}

	@Test
	public void testsnmp4JV3Serialization() throws UnknownHostException {

		// create instance of snmp4JV3Trap
		PDU snmp4JV3TrapPdu = new ScopedPDU();
		snmp4JV3TrapPdu.setType(PDU.TRAP);
		snmp4JV3TrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
				new OctetString("mockhost")));
		snmp4JV3TrapPdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3"),
				new OctetString("mockhost")));
		snmp4JV3TrapPdu.add(new VariableBinding(new OID(
				".1.3.6.1.6.3.1.1.4.1.0"), new OctetString("mockhost")));

		TrapInformation snmp4JV3Trap = new Snmp4JTrapNotifier.Snmp4JV2V3TrapInformation(
				inetAddress, new String("public"), snmp4JV3TrapPdu);
		assertTrue(writeTrapNotificationObject(snmp4JV3Trap));
	}

	public boolean writeTrapNotificationObject(TrapInformation object) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
			objectOutputStream.writeObject(object);

			ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
			TrapInformation notification = (TrapInformation)in.readObject();
		} catch (NotSerializableException e) {
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		return false;
	}

}
