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

import java.util.Base64;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
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

public class TrapDTOToObjectProcessor implements Processor{
	public static final Logger LOG = LoggerFactory.getLogger(TrapObjectToDTOProcessor.class);

	private final Class<?> m_class;
	private static final String SNMP_V1="v1";
	private static final String SNMP_V2="v2";
	private static final String SNMP_V3="v3";
	
	@SuppressWarnings("rawtypes") // Because Aries Blueprint cannot handle generics
	public TrapDTOToObjectProcessor(Class clazz) {
		m_class = clazz;
	}

	public TrapDTOToObjectProcessor(String className) throws ClassNotFoundException {
		m_class = Class.forName(className);
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		final Object object = exchange.getIn().getBody(m_class);
		exchange.getIn().setBody(dto2object(object), TrapNotification.class);
	}
	

	public TrapNotification dto2object(Object obj) {
		TrapDTO trapDto = (TrapDTO) obj;
		Snmp4JTrapNotifier.Snmp4JV1TrapInformation v1Trap = null;

		Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = null;

		if (trapDto.getFromMap(TrapDTO.VERSION).equalsIgnoreCase(SNMP_V1)) {
			PDUv1 snmp4JV1cTrapPdu = new PDUv1();
			snmp4JV1cTrapPdu.setType(PDU.NOTIFICATION);

			for (SnmpResult snmpResult : trapDto.getResults()) {
				
				int type = snmpResult.getValue().getType();
				
				OID oid = new OID(snmpResult
						.getValue().toString());
				
		        switch (type) {
	            case 2:  snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Integer32(Integer.parseInt(snmpResult.getBase().toString()))));
                		 break;
	            case 4:  snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(new String(Base64.getDecoder().decode(snmpResult.getBase().toString())))));
                		 break;
	            case 5:  snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Null()));
                		 break;
	            case 6:  snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(snmpResult.getBase().toString())));
                		 break;
	            case 64: snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new IpAddress(snmpResult.getBase().toString())));
                		 break;
	            case 67: snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new TimeTicks(Long.parseLong(snmpResult.getBase().toString()))));
	                     break;
	            case 128:snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Null(128)));
	                     break;
	            case 129:snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Null(129)));
	                     break;
	            case 130:snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new Null(130)));
	                     break;
	            default: snmp4JV1cTrapPdu.add(new VariableBinding(new OID(oid), new OctetString(snmpResult.getBase().toString())));
	                     break;
	            }

			}

			v1Trap = new Snmp4JTrapNotifier.Snmp4JV1TrapInformation(
					InetAddrUtils.addr(trapDto
							.getFromMap(TrapDTO.SOURCE_ADDRESS)),
					trapDto.getFromMap(TrapDTO.COMMUNITY), snmp4JV1cTrapPdu,
					null);
			return v1Trap;
		} else if (trapDto.getFromMap(TrapDTO.VERSION).equalsIgnoreCase(SNMP_V2)
				|| trapDto.getFromMap(TrapDTO.VERSION).equalsIgnoreCase(SNMP_V3)) {
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
