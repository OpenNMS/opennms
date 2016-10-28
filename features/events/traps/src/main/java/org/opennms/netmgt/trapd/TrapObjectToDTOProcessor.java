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

	private static final String SNMP_V1 = "v1";
	private static final String SNMP_V2 = "v2";
	private static final String SNMP_V3 = "v3";

	public static final String INCLUDE_RAW_MESSAGE = "includeRawMessage";
	public static final boolean INCLUDE_RAW_MESSAGE_DEFAULT = Boolean.FALSE;

	public void setDistPollerDao(DistPollerDao distPollerDao) {
		this.m_distPollerDao = distPollerDao;
	}

	@Override
	public void process(final Exchange exchange) throws Exception {
		final TrapInformation object = exchange.getIn().getBody(TrapInformation.class);
		boolean trapRawMessageFlag = (boolean)exchange.getIn().getHeader(INCLUDE_RAW_MESSAGE);
		exchange.getIn().setBody(object2dto(object, trapRawMessageFlag), TrapDTO.class);
	}

	public TrapDTO object2dto(TrapInformation trapInfo) {
		return object2dto(trapInfo, INCLUDE_RAW_MESSAGE_DEFAULT);
	}

	public TrapDTO object2dto(TrapInformation trapInfo, boolean trapRawMessageFlag) {

		TrapDTO trapDTO = new TrapDTO();

		String version = trapInfo.getVersion();

		String id = m_distPollerDao.whoami().getId();
		String location = m_distPollerDao.whoami().getLocation();

		if (version.equalsIgnoreCase(SNMP_V1)) {

			Snmp4JTrapNotifier.Snmp4JV1TrapInformation v1Trap = (Snmp4JTrapNotifier.Snmp4JV1TrapInformation)trapInfo;
			String community = v1Trap.getCommunity();
			InetAddress trapAddress = v1Trap.getTrapAddress();
			PDUv1 pdu = v1Trap.getPdu();

			if(trapRawMessageFlag){
				try {
					byte[] byteArray = Snmp4JUtils.convertPduToBytes(trapAddress, 0, community, pdu);
					trapDTO.setBody(byteArray);
				} catch (Throwable e) {
					LOG.warn("Unable to convert PDU into bytes: {}", e.getMessage());
				}
			}

			trapDTO.setAgentAddress(v1Trap.getAgentAddress());
			trapDTO.setCommunity(community);
			trapDTO.setCreationTime(v1Trap.getCreationTime());
			trapDTO.setLocation(location);
			trapDTO.setPduLength(v1Trap.getPduLength());
			trapDTO.setSourceAddress(trapAddress);
			trapDTO.setSystemId(id);
			// NOTE: This value is an SNMP TimeTicks value, not an epoch timestamp
			trapDTO.setTimestamp(v1Trap.getTimeStamp());
			trapDTO.setVersion(version);

			List<SnmpResult> results = new ArrayList<SnmpResult>();

			for (final VariableBinding varBind : pdu.getVariableBindings()) {
				final SnmpObjId oid = SnmpObjId.get(varBind.getOid().toString());
				final SnmpValue value = new Snmp4JValue(varBind.getVariable());
				final SnmpResult snmpResult = new SnmpResult(oid, null, value);
				results.add(snmpResult);
			}

			trapDTO.setResults(results);
		} else if (
			version.equalsIgnoreCase(SNMP_V2) ||
			version.equalsIgnoreCase(SNMP_V3)
		) {

			Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = (Snmp4JTrapNotifier.Snmp4JV2TrapInformation)trapInfo;
			String community = v2Trap.getCommunity();
			InetAddress trapAddress = v2Trap.getTrapAddress();
			PDU pdu = v2Trap.getPdu();

			if(trapRawMessageFlag){
				try {
					byte[] byteArray = Snmp4JUtils.convertPduToBytes(trapAddress, 0, community, pdu);
					trapDTO.setBody(byteArray);
				} catch (Throwable e) {
					LOG.warn("Unable to convert PDU into bytes: {}", e.getMessage());
				}
			}

			trapDTO.setAgentAddress(v2Trap.getAgentAddress());
			trapDTO.setCommunity(community);
			trapDTO.setCreationTime(v2Trap.getCreationTime());
			trapDTO.setLocation(location);
			trapDTO.setPduLength(v2Trap.getPduLength());
			trapDTO.setSourceAddress(trapAddress);
			trapDTO.setSystemId(id);
			// NOTE: This value is an SNMP TimeTicks value, not an epoch timestamp
			trapDTO.setTimestamp(v2Trap.getTimeStamp());
			trapDTO.setVersion(version);

			List<SnmpResult> results = new ArrayList<SnmpResult>();

			for (final VariableBinding varBind : pdu.getVariableBindings()) {
				final SnmpObjId oid = SnmpObjId.get(varBind.getOid().toString());
				final SnmpValue value = new Snmp4JValue(varBind.getVariable());
				final SnmpResult snmpResult = new SnmpResult(oid, null, value);
				results.add(snmpResult);
			}

			trapDTO.setResults(results);
		}

		return trapDTO;
	}
}
