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
import java.util.List;
import java.util.Vector;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JUtils;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValue;
import org.snmp4j.PDU;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

public class TrapDTOMapper {

	public TrapDTO object2dto(Object obj){
		
		TrapDTO trapDTO = new TrapDTO();
		
		TrapInformation trapInfo = (TrapInformation)obj;
		
       	String version = trapInfo.getVersion();
		
		//Snmp4JTrapNotifier.Snmp4JV1TrapInformation v1Trap = null;
		Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = null;
		
//		if(version.equalsIgnoreCase("v1")){
//			
//			v1Trap = (Snmp4JTrapNotifier.Snmp4JV1TrapInformation)obj;
//			InetAddress agentAddress = v1Trap.getAgentAddress();
//			String community = v1Trap.getCommunity();
//			int pduLength = v1Trap.getPduLength();
//			//String version = v1Trap.getVersion();
//			InetAddress trapAddress = v1Trap.getTrapAddress();
//			long timestamp = v1Trap.getTimeStamp();
//			
//			//trapDTO.setVersion(processor.);
//			
//		}else 
		if(version.equalsIgnoreCase("v2") || version.equalsIgnoreCase("v3")){
			
			v2Trap = (Snmp4JTrapNotifier.Snmp4JV2TrapInformation)obj;
			InetAddress agentAddress = v2Trap.getAgentAddress();
			String community = v2Trap.getCommunity();
			int pduLength = v2Trap.getPduLength();
			//String version = v2Trap.getVersion();
			InetAddress trapAddress = v2Trap.getTrapAddress();
			long timestamp = v2Trap.getTimeStamp();
			PDU pdu = v2Trap.getPdu();
			byte[] byteArray = null;
			try {
				byteArray = Snmp4JUtils.convertPduToBytes(trapAddress, 0, community, pdu);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			
			Vector<? extends VariableBinding> vector = pdu.getVariableBindings();
			for(int i=0 ; i < vector.size() ; i++){
				VariableBinding varBind = (VariableBinding)vector.get(i);
				
	            SnmpObjId oid = SnmpObjId.get(varBind.getOid().toString());
	            SnmpValue value = new Snmp4JValue(varBind.getVariable());
	            snmpResult = new SnmpResult(oid, null, value);
	            results.add(snmpResult);
			}
			trapDTO.setResults(results);

		}
						
		return trapDTO;
	}
	
	public TrapNotification dto2object(Object obj){
//		TrapDTO trapDto = (TrapDTO)obj;
//		Snmp4JTrapNotifier.Snmp4JV1TrapInformation v1Trap = null;
//		Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = null;
//		
//		if(trapDto.getFromMap(TrapDTO.VERSION).equalsIgnoreCase("v2") || trapDto.getFromMap(TrapDTO.VERSION).equalsIgnoreCase("v3")){
//			PDU pdu = new PDU();
//			for(SnmpResult snmpResult : trapDto.getResults()){
//				OID oid = new OID();
//				oid.se
//				VariableBinding varBind = new VariableBinding();
//				varBind.setOid(oid);
//			}
//			Snmp4JV2TrapInformation = new Snmp4JTrapNotifier.Snmp4JV2TrapInformation(trapDto.getFromMap(TrapDTO.SOURCE_ADDRESS), trapDto.getFromMap(TrapDTO.COMMUNITY), , null);
//		
//		}else if(trapDto.getFromMap(TrapDTO.VERSION).equalsIgnoreCase("v1")){
//			// build snmpv1 object
//		}
		
		return null;
	}

	
}