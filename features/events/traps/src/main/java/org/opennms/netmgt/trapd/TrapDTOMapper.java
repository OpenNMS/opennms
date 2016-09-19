/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.trapd;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Vector;

import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JUtils;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.Null;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.VariableBinding;

public class TrapDTOMapper {

	private static final Logger LOG = LoggerFactory.getLogger(TrapDTOMapper.class);

	public TrapDTO object2dto(Object obj) {

		TrapDTO trapDTO = new TrapDTO();

		TrapInformation trapInfo = (TrapInformation) obj;

		String version = trapInfo.getVersion();

		Snmp4JTrapNotifier.Snmp4JV1TrapInformation v1Trap = null;
		Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = null;

		if (version.equalsIgnoreCase("v1")) {

			v1Trap = (Snmp4JTrapNotifier.Snmp4JV1TrapInformation) obj;
			InetAddress agentAddress = v1Trap.getAgentAddress();
			String community = v1Trap.getCommunity();
			int pduLength = v1Trap.getPduLength();
			// String version = v1Trap.getVersion();
			InetAddress trapAddress = v1Trap.getTrapAddress();
			long timestamp = v1Trap.getTimeStamp();
			PDUv1 pdu = v1Trap.getPduv1();
			byte[] byteArray = null;
			try {
				byteArray = Snmp4JUtils.convertPduToBytes(trapAddress, 0,
						community, pdu);
			} catch (Exception e) {
				LOG.warn("unable to convert Pdu inot Bytes {}", e.getMessage());
			}

			trapDTO.setBody(byteArray);

			trapDTO.setCommunity(community);
			trapDTO.setPduLength(String.valueOf(pduLength));
			trapDTO.setAgentAddress(agentAddress);
			trapDTO.setTimestamp(timestamp);
			trapDTO.setTrapAddress(trapAddress);
			trapDTO.setVersion(version);

			List<SnmpResult> results = new ArrayList<SnmpResult>();

			SnmpResult snmpResult = null;

			Vector<? extends VariableBinding> vector = pdu
					.getVariableBindings();
			for (int i = 0; i < vector.size(); i++) {
				VariableBinding varBind = (VariableBinding) vector.get(i);

				SnmpObjId oid = SnmpObjId.get(varBind.getOid().toString());
				SnmpValue value = new Snmp4JValue(varBind.getVariable());
				snmpResult = new SnmpResult(oid, null, value);
				results.add(snmpResult);
			}
			trapDTO.setResults(results);

		} else if (version.equalsIgnoreCase("v2")
				|| version.equalsIgnoreCase("v3")) {

			v2Trap = (Snmp4JTrapNotifier.Snmp4JV2TrapInformation) obj;
			InetAddress agentAddress = v2Trap.getAgentAddress();
			String community = v2Trap.getCommunity();
			int pduLength = v2Trap.getPduLength();
			// String version = v2Trap.getVersion();
			InetAddress trapAddress = v2Trap.getTrapAddress();
			long timestamp = v2Trap.getTimeStamp();
			PDU pdu = v2Trap.getPdu();
			byte[] byteArray = null;
			try {
				byteArray = Snmp4JUtils.convertPduToBytes(trapAddress, 0,
						community, pdu);
			} catch (Exception e) {
				LOG.warn("unable to convert Pdu inot Bytes {}", e.getMessage());
			}

			trapDTO.setBody(byteArray); // use configAttribute to set body

			trapDTO.setCommunity(community);
			trapDTO.setPduLength(String.valueOf(pduLength));
			trapDTO.setAgentAddress(agentAddress);
			trapDTO.setTimestamp(timestamp);
			trapDTO.setTrapAddress(trapAddress);
			trapDTO.setVersion(version);

			List<SnmpResult> results = new ArrayList<SnmpResult>();

			SnmpResult snmpResult = null;

			Vector<? extends VariableBinding> vector = pdu
					.getVariableBindings();
			for (int i = 0; i < vector.size(); i++) {
				VariableBinding varBind = (VariableBinding) vector.get(i);
				SnmpObjId oid = SnmpObjId.get(varBind.getOid().toString());
				SnmpValue value = new Snmp4JValue(varBind.getVariable());
				snmpResult = new SnmpResult(oid, null, value);
				results.add(snmpResult);
			}
			trapDTO.setResults(results);

		}

		return trapDTO;
	}

	public TrapNotification dto2object(Object obj) {
		TrapDTO trapDto = (TrapDTO) obj;
		Snmp4JTrapNotifier.Snmp4JV1TrapInformation v1Trap = null;

		Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = null;

		if (trapDto.getFromMap(TrapDTO.VERSION).equalsIgnoreCase("v1")) {
			PDUv1 snmp4JV1cTrapPdu = new PDUv1();
			snmp4JV1cTrapPdu.setType(PDU.NOTIFICATION);

			for (SnmpResult snmpResult : trapDto.getResults()) {
				snmp4JV1cTrapPdu.add(new VariableBinding(new OID(snmpResult
						.getBase().toString()), new OctetString(snmpResult
						.getValue().toString())));

			}

			v1Trap = new Snmp4JTrapNotifier.Snmp4JV1TrapInformation(
					InetAddrUtils.addr(trapDto
							.getFromMap(TrapDTO.SOURCE_ADDRESS)),
					trapDto.getFromMap(TrapDTO.COMMUNITY), snmp4JV1cTrapPdu,
					null);
			return v1Trap;
		} else if (trapDto.getFromMap(TrapDTO.VERSION).equalsIgnoreCase("v2")
				|| trapDto.getFromMap(TrapDTO.VERSION).equalsIgnoreCase("v3")) {
			PDU snmp4JV2cTrapPdu = new PDU();
			snmp4JV2cTrapPdu.setType(PDU.NOTIFICATION);

			for (SnmpResult snmpResult : trapDto.getResults()) {
				
				int type = snmpResult.getValue().getType();
				
				OID oid = new OID(snmpResult
						.getValue().toString());
				
		        switch (type) {
	            case 2:  snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Integer32(Integer.parseInt(snmpResult.getBase().toString()))));
                		 break;
	            case 4:  snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(new String(Base64.getDecoder().decode(snmpResult.getBase().toString())))));
                		 break;
	            case 5:  snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Null()));
                		 break;
	            case 6:  snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(snmpResult.getBase().toString())));
                		 break;
	            case 64: snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new IpAddress(snmpResult.getBase().toString())));
                		 break;
	            case 67: snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new TimeTicks(Long.parseLong(snmpResult.getBase().toString()))));
	                     break;
	            case 128:snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Null(128)));
	                     break;
	            case 129:snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Null(129)));
	                     break;
	            case 130:snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new Null(130)));
	                     break;
	            default: snmp4JV2cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(snmpResult.getBase().toString())));
	                     break;
	            }

			}

			v2Trap = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
					InetAddrUtils.addr(trapDto
							.getFromMap(TrapDTO.SOURCE_ADDRESS)),
					trapDto.getFromMap(TrapDTO.COMMUNITY), snmp4JV2cTrapPdu,
					null);
			return v2Trap;
		}

		return null;
	}

}