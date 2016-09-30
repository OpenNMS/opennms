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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.core.camel.MinionDTO;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.smi.IpAddress;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

public class TrapDTOToObjectProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(TrapDTOToObjectProcessor.class);

	private static final String SNMP_V1="v1";
	private static final String SNMP_V2="v2";
	private static final String SNMP_V3="v3";

	@Override
	public void process(final Exchange exchange) throws Exception {
		final TrapDTO object = exchange.getIn().getBody(TrapDTO.class);
		exchange.getIn().setBody(dto2object(object), TrapNotification.class);
	}

	public static TrapNotification dto2object(TrapDTO trapDto) {
		if (SNMP_V1.equalsIgnoreCase(trapDto.getHeader(TrapDTO.VERSION))) {
			PDUv1 pdu = new PDUv1();
			pdu.setType(PDU.NOTIFICATION);
			pdu.setAgentAddress(new IpAddress(trapDto.getHeader(TrapDTO.SOURCE_ADDRESS)));
			pdu.setTimestamp(Long.parseLong(trapDto.getHeader(TrapDTO.TIMESTAMP)));

			// TODO: Fill in these values
			/*
			pdu.setEnterprise(enterprise);
			pdu.setGenericTrap(genericTrap);
			pdu.setSpecificTrap(specificTrap);
			*/

			for (SnmpResult snmpResult : trapDto.getResults()) {
				final int type = snmpResult.getValue().getType();
				final byte[] value = snmpResult.getValue().getBytes();
				final OID oid = new OID(snmpResult.getBase().toString());

				pdu.add(new VariableBinding(oid, ((Snmp4JValue)SnmpUtils.getValueFactory().getValue(type, value)).getVariable()));
			}

			TrapInformation retval = new Snmp4JTrapNotifier.Snmp4JV1TrapInformation(
				InetAddrUtils.addr(trapDto.getHeader(TrapDTO.AGENT_ADDRESS)),
				trapDto.getHeader(TrapDTO.COMMUNITY),
				pdu,
				null
			);
			retval.setCreationTime(Long.parseLong(trapDto.getHeader(TrapDTO.CREATION_TIME)));
			retval.setLocation(trapDto.getHeader(MinionDTO.LOCATION));
			retval.setSystemId(trapDto.getHeader(MinionDTO.SYSTEM_ID));
			return retval;
		} else if (
			SNMP_V2.equalsIgnoreCase(trapDto.getHeader(TrapDTO.VERSION)) ||
			SNMP_V3.equalsIgnoreCase(trapDto.getHeader(TrapDTO.VERSION))
		) {
			PDU pdu = new PDU();
			pdu.setType(PDU.NOTIFICATION);

			for (SnmpResult snmpResult : trapDto.getResults()) {
				final int type = snmpResult.getValue().getType();
				final byte[] value = snmpResult.getValue().getBytes();
				final OID oid = new OID(snmpResult.getBase().toString());

				pdu.add(new VariableBinding(oid, ((Snmp4JValue)SnmpUtils.getValueFactory().getValue(type, value)).getVariable()));
			}

			TrapInformation retval = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(
				InetAddrUtils.addr(trapDto.getHeader(TrapDTO.SOURCE_ADDRESS)),
				trapDto.getHeader(TrapDTO.COMMUNITY),
				pdu,
				null
			);
			retval.setCreationTime(Long.parseLong(trapDto.getHeader(TrapDTO.CREATION_TIME)));
			retval.setLocation(trapDto.getHeader(MinionDTO.LOCATION));
			retval.setSystemId(trapDto.getHeader(MinionDTO.SYSTEM_ID));
			return retval;
		} else {
			throw new IllegalArgumentException("Unrecognized trap version in DTO: " + trapDto.getHeader(TrapDTO.VERSION));
		}
	}
}
