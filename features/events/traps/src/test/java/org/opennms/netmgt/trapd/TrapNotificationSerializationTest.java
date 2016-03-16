package org.opennms.netmgt.trapd;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.ScopedPDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.VariableBinding;

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

		TrapNotification snmp4JV1Trap = new Snmp4JTrapNotifier.Snmp4JV1TrapInformation(
				inetAddress, new String("public"), snmp4JV1TrapPdu, null);
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

		TrapNotification snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
				inetAddress, new String("public"), snmp4JV2cTrapPdu, null);
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

		TrapNotification snmp4JV3Trap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
				inetAddress, new String("public"), snmp4JV3TrapPdu, null);
		assertTrue(writeTrapNotificationObject(snmp4JV3Trap));
	}

	public boolean writeTrapNotificationObject(TrapNotification object) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(bos);
			objectOutputStream.writeObject(object);
			return true;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

}
