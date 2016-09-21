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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JUtils;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.smi.VariableBinding;

public class TrapObjectToDTOProcessor implements Processor {
	public static final Logger LOG = LoggerFactory.getLogger(TrapObjectToDTOProcessor.class);

	private DistPollerDao m_distPollerDao;

	private static final String SNMP_V1="v1";
	private static final String SNMP_V2="v2";
	private static final String SNMP_V3="v3";

	public static final String INCLUDE_RAW_MESSAGE = "includeRawMessage";

	public void setDistPollerDao(DistPollerDao minionIdentity) {
		this.m_distPollerDao = minionIdentity;
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		final TrapInformation object = exchange.getIn().getBody(TrapInformation.class);
		boolean trapRawMessageFlag = (boolean)exchange.getIn().getHeader(INCLUDE_RAW_MESSAGE);
		exchange.getIn().setBody(object2dto(object, trapRawMessageFlag), TrapDTO.class);
	}

	public TrapDTO object2dto(TrapInformation trapInfo, boolean trapRawMessageFlag) {

		TrapDTO trapDTO = new TrapDTO();

		String version = trapInfo.getVersion();

		String id = m_distPollerDao.whoami().getId();
		String location = m_distPollerDao.whoami().getLocation();

		if (version.equalsIgnoreCase(SNMP_V1)) {

			Snmp4JTrapNotifier.Snmp4JV1TrapInformation v1Trap = (Snmp4JTrapNotifier.Snmp4JV1TrapInformation)trapInfo;
			InetAddress agentAddress = v1Trap.getAgentAddress();
			String community = v1Trap.getCommunity();
			int pduLength = v1Trap.getPduLength();
			// String version = v1Trap.getVersion();
			InetAddress trapAddress = v1Trap.getTrapAddress();
			long timestamp = v1Trap.getTimeStamp();
			PDUv1 pdu = v1Trap.getPduv1();
			byte[] byteArray = null;
			try {
				byteArray = Snmp4JUtils.convertPduToBytes(trapAddress, 0, community, pdu);
			} catch (Throwable e) {
				LOG.warn("Unable to convert PDU into bytes: {}", e.getMessage());
			}

			trapDTO.setSystemId(id);
			trapDTO.setLocation(location);
			
			if(trapRawMessageFlag){
				trapDTO.setBody(byteArray);
			}
			trapDTO.setCommunity(community);
			trapDTO.setPduLength(Integer.toString(pduLength));
			trapDTO.setAgentAddress(agentAddress);
			trapDTO.setTimestamp(timestamp);
			trapDTO.setTrapAddress(trapAddress);
			trapDTO.setVersion(version);

			List<SnmpResult> results = new ArrayList<SnmpResult>();

			SnmpResult snmpResult = null;

			Vector<? extends VariableBinding> vector = pdu.getVariableBindings();
			for (int i = 0; i < vector.size(); i++) {
				VariableBinding varBind = (VariableBinding) vector.get(i);

				SnmpObjId oid = SnmpObjId.get(varBind.getOid().toString());
				SnmpValue value = new Snmp4JValue(varBind.getVariable());
				snmpResult = new SnmpResult(oid, null, value);
				results.add(snmpResult);
			}
			trapDTO.setResults(results);

		} else if (
			version.equalsIgnoreCase(SNMP_V2) ||
			version.equalsIgnoreCase(SNMP_V3)
		) {

			Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = (Snmp4JTrapNotifier.Snmp4JV2TrapInformation)trapInfo;
			InetAddress agentAddress = v2Trap.getAgentAddress();
			String community = v2Trap.getCommunity();
			int pduLength = v2Trap.getPduLength();
			// String version = v2Trap.getVersion();
			InetAddress trapAddress = v2Trap.getTrapAddress();
			long timestamp = v2Trap.getTimeStamp();
			PDU pdu = v2Trap.getPdu();
			byte[] byteArray = null;
			try {
				byteArray = Snmp4JUtils.convertPduToBytes(trapAddress, 0, community, pdu);
			} catch (Throwable e) {
				LOG.warn("Unable to convert PDU into bytes: {}", e.getMessage());
			}

			trapDTO.setSystemId(id);
			trapDTO.setLocation(location);
			
			if(trapRawMessageFlag){
				trapDTO.setBody(byteArray);
			}
			trapDTO.setCommunity(community);
			trapDTO.setPduLength(Integer.toString(pduLength));
			trapDTO.setAgentAddress(agentAddress);
			trapDTO.setTimestamp(timestamp);
			trapDTO.setTrapAddress(trapAddress);
			trapDTO.setVersion(version);

			List<SnmpResult> results = new ArrayList<SnmpResult>();

			SnmpResult snmpResult = null;

			Vector<? extends VariableBinding> vector = pdu.getVariableBindings();
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
}
