/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.DistPollerDaoMinion;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.snmp.BasicTrapProcessor;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
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

public class TrapDTOMapperTest {

	@Test
	public void object2dtoTest() throws UnknownHostException {
		long testStartTime = new Date().getTime();

		PDU snmp4JV2cTrapPdu = new PDU();
		snmp4JV2cTrapPdu.setType(PDU.TRAP);
		OID oid = new OID(".1.3.6.1.2.1.1.3.0");
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.sysUpTime, new TimeTicks(5000)));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapOID, oid));
		snmp4JV2cTrapPdu.add(new VariableBinding(SnmpConstants.snmpTrapAddress,new IpAddress("127.0.0.1")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Trap Msg v2-1")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString("Trap Msg v2-2")));

		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
				new OctetString("Trap v1 msg-1")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3"),
				new OctetString("Trap v1 msg-2")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.6.3.1.1.4.1.1"), 
				new OctetString("Trap v1 msg-3")));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID(".1.3.6.1.4.1.733.6.3.18.1.5.0"),
				new Integer32(1))); 
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
				new Null())); 
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.1"),
				new Null(128)));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.2"),
				new Null(129)));
		snmp4JV2cTrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.3"),
				new Null(130)));

		TrapInformation snmp4JV2cTrap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
			InetAddressUtils.ONE_TWENTY_SEVEN,
			"public",
			snmp4JV2cTrapPdu,
			new BasicTrapProcessor()
		);

		OnmsDistPoller distPoller = new OnmsDistPoller();
		distPoller.setId(DistPollerDao.DEFAULT_DIST_POLLER_ID);
		distPoller.setLabel(DistPollerDao.DEFAULT_DIST_POLLER_ID);
		distPoller.setLocation("localhost");
		DistPollerDao distPollerDao = new DistPollerDaoMinion(distPoller);

		TrapObjectToDTOProcessor mapper = new TrapObjectToDTOProcessor();
		mapper.setDistPollerDao(distPollerDao);

		TrapDTO trapDto = mapper.object2dto(snmp4JV2cTrap);
		System.out.println("trapDto is : " + trapDto);
		System.out.println("trapDto.getBody() is : " + trapDto.getBody());
		System.out.println("trapDto.getCommunity() is : " + trapDto.getHeader(TrapDTO.COMMUNITY));

		TrapNotification snmp4JV2cTrap1 = TrapDTOToObjectProcessor.dto2object(trapDto);

		assertEquals(".1.3.6.1.2.1.1.3", ((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getTrapIdentity().getEnterpriseId());
		assertEquals(6, ((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getTrapIdentity().getGeneric());
		assertEquals(0, ((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getTrapIdentity().getSpecific());
		assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, ((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getAgentAddress());
		assertEquals("public", ((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getCommunity());
		assertEquals("localhost", ((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getLocation());
		assertEquals(DistPollerDao.DEFAULT_DIST_POLLER_ID, ((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getSystemId());
		assertEquals(5000, ((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getTimeStamp());
		// Trap and agent address are identical with SNMPv2
		assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, ((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getTrapAddress());
		assertEquals("v2", ((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getVersion());

		// Make sure that the message was created after the start of the test
		assertTrue(((BasicTrapProcessor)snmp4JV2cTrap1.getTrapProcessor()).getCreationTime() >= testStartTime);
	}

	@Test
	public void object2dtoTestV1() throws UnknownHostException {
		long testStartTime = new Date().getTime();

		PDUv1 snmp4JV1TrapPdu = new PDUv1();
		snmp4JV1TrapPdu.setType(PDU.V1TRAP);
		snmp4JV1TrapPdu.setEnterprise(new OID(".1.3.6.1.6.3.1.1.4.1.0"));
		snmp4JV1TrapPdu.setGenericTrap(10);
		snmp4JV1TrapPdu.setSpecificTrap(0);
		snmp4JV1TrapPdu.setTimestamp(5000);
		snmp4JV1TrapPdu.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0"),
				new OctetString("mockhost")));
		snmp4JV1TrapPdu.add(new VariableBinding(new OID(".1.3.6.1.2.1.1.3"),
				new OctetString("mockhost")));
		snmp4JV1TrapPdu.add(new VariableBinding(new OID(
				".1.3.6.1.6.3.1.1.4.1.0"), new OctetString("mockhost")));

		InetAddress inetAddress= InetAddress.getByName("127.0.0.1");;
		TrapInformation snmp4JV1Trap = new Snmp4JTrapNotifier.Snmp4JV1TrapInformation(
				inetAddress, "public", snmp4JV1TrapPdu, null);
		
		OnmsDistPoller distPoller = new OnmsDistPoller();
		distPoller.setId(DistPollerDao.DEFAULT_DIST_POLLER_ID);
		distPoller.setLabel(DistPollerDao.DEFAULT_DIST_POLLER_ID);
		distPoller.setLocation("localhost");
		DistPollerDao distPollerDao = new DistPollerDaoMinion(distPoller);

		TrapObjectToDTOProcessor mapper = new TrapObjectToDTOProcessor();
		mapper.setDistPollerDao(distPollerDao);

		TrapDTO trapDto = mapper.object2dto(snmp4JV1Trap);
		System.out.println("trapDto is : " + trapDto);
		System.out.println("trapDto.getBody() is : " + trapDto.getBody());
		System.out.println("trapDto.getCommunity() is : " + trapDto.getHeader(TrapDTO.COMMUNITY));

		TrapNotification snmp4JV1Trap1 = TrapDTOToObjectProcessor.dto2object(trapDto);
		snmp4JV1Trap1.setTrapProcessor(new BasicTrapProcessor());

		assertEquals(".1.3.6.1.6.3.1.1.4.1.0", ((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getTrapIdentity().getEnterpriseId());
		assertEquals(10, ((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getTrapIdentity().getGeneric());
		assertEquals(0, ((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getTrapIdentity().getSpecific());
		assertEquals(InetAddressUtils.ONE_TWENTY_SEVEN, ((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getAgentAddress());
		assertEquals("public", ((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getCommunity());
		assertEquals("localhost", ((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getLocation());
		assertEquals(DistPollerDao.DEFAULT_DIST_POLLER_ID, ((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getSystemId());
		assertEquals(5000, ((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getTimeStamp());
		// This is the "default" value from SNMP4J that indicates that the trap has not been forwarded
		assertEquals(InetAddressUtils.addr("0.0.0.0"), ((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getTrapAddress());
		assertEquals("v1", ((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getVersion());

		// Make sure that the message was created after the start of the test
		assertTrue(((BasicTrapProcessor)snmp4JV1Trap1.getTrapProcessor()).getCreationTime() >= testStartTime);
	}
}
