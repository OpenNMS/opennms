package org.opennms.netmgt.trapd;

import static org.junit.Assert.*;
import kafka.serializer.StringEncoder;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.core.camel.JaxbUtilsMarshalProcessor;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.snmp.BasicTrapProcessor;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;

public class TrapdEncoderDecoderTest {

    public static final transient Logger LOG = LoggerFactory.getLogger(TrapdEncoderDecoderTest.class);

    
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testTrapdSnmp4jObj(){
		
		PDU snmp4JV2cTrapPdu = new PDU();
		OID oid = new OID(".1.3.6.1.2.1.1.3.0");
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
				new IpAddress("127.0.0.1")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("TESTING TRAPS MSGS")));
		snmp4JV2cTrapPdu.setType(PDU.NOTIFICATION);
		
		TrapNotification snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
		InetAddressUtils.ONE_TWENTY_SEVEN, "public",
		snmp4JV2cTrapPdu, new BasicTrapProcessor());
		
		TrapdKafkaEncoder encoder = new TrapdKafkaEncoder();
		byte[] testArray = encoder.toBytes(snmp4JV2cTrap);

		System.out.println("testArray is : "+testArray);
		
		TrapdKafkaDecoder decoder = new TrapdKafkaDecoder();
		TrapNotification snmp4JV2cTrap1 = (TrapNotification) decoder.fromBytes(testArray);
		System.out.println("snmp4JV2cTrap1 is : "+snmp4JV2cTrap1);
		System.out.println("snmp4JV2cTrap1.toString() is : "+snmp4JV2cTrap1.toString());
	}

}
