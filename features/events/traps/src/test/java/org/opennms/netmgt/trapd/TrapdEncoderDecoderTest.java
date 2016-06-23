package org.opennms.netmgt.trapd;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.snmp.BasicTrapProcessor;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
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
	public void testTrapdSnmp4jV2Trap(){
		
		PDU snmp4JV2cTrapPdu = new PDU();

		OID oid = new OID(".1.3.6.1.2.1.1.3.0");
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,new IpAddress("127.0.0.1")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Trap Msg v2")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
				new OctetString("Trap v1 msg-1")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3"),
				new OctetString("Trap v1 msg-2")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.6.3.1.1.4.1.1"), 
				new OctetString("Trap v1 msg-3")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
		new Integer32(1234))); 
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
		new Null())); 
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.1"),
		new Null(128)));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.2"),
		new Null(129)));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.3"),
		new Null(130)));
		snmp4JV2cTrapPdu.setType(PDU.NOTIFICATION);

		TrapNotification snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
				InetAddressUtils.ONE_TWENTY_SEVEN, "public",
				snmp4JV2cTrapPdu, new BasicTrapProcessor());
		
		TrapdKafkaEncoder encoder = new TrapdKafkaEncoder();
		byte[] testArray = encoder.toBytes(snmp4JV2cTrap);

		System.out.println("testArray is : "+testArray);
		
		TrapdKafkaDecoder decoder = new TrapdKafkaDecoder();
		TrapNotification snmp4JV2cTrap1 = (TrapNotification) decoder.fromBytes(testArray);
		LOG.debug("snmp4JV2cTrap1 is : "+snmp4JV2cTrap1);
		LOG.debug("snmp4JV2cTrap1.toString() is : "+snmp4JV2cTrap1.toString());
	}
	
	@Test
	public void testTrapdSnmp4jV1Trap(){
		
		PDUv1 snmp4JV1TrapPdu = new PDUv1();
		snmp4JV1TrapPdu.setType(PDU.V1TRAP);
		
		OID oid = new OID(".1.3.6.1.2.1.1.3.0");
		snmp4JV1TrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));   
		snmp4JV1TrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, new OID(oid)));
		snmp4JV1TrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,
				new IpAddress("127.0.0.1")));
		
		snmp4JV1TrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
				new OctetString("Trap v1 msg-1")));
		snmp4JV1TrapPdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3"),
				new OctetString("Trap v1 msg-2")));
		snmp4JV1TrapPdu.add(new VariableBinding(new OID(".1.3.6.1.6.3.1.1.4.1.1"), 
				new OctetString("Trap v1 msg-3")));
		snmp4JV1TrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
		new Integer32(1234))); 
		snmp4JV1TrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
		new Null())); 
		snmp4JV1TrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.1"),
		new Null(128)));
		snmp4JV1TrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.2"),
		new Null(129)));
		snmp4JV1TrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.3"),
		new Null(130)));
		
		TrapNotification snmp4JV1Trap = new Snmp4JTrapNotifier.Snmp4JV1TrapInformation(
				InetAddressUtils.ONE_TWENTY_SEVEN, new String("public"), snmp4JV1TrapPdu, new BasicTrapProcessor());
		
		TrapdKafkaEncoder encoder = new TrapdKafkaEncoder();
		byte[] testArray = encoder.toBytes(snmp4JV1Trap);

		System.out.println("testArray is : "+testArray);
		
		TrapdKafkaDecoder decoder = new TrapdKafkaDecoder();
		TrapNotification snmp4JV1cTrap1 = (TrapNotification) decoder.fromBytes(testArray);
		LOG.debug("snmp4JV1cTrap1 is : "+snmp4JV1cTrap1);
		LOG.debug("snmp4JV1cTrap1.toString() is : "+snmp4JV1cTrap1.toString());
	}

}
