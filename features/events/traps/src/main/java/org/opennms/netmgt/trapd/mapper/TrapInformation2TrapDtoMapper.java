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

package org.opennms.netmgt.trapd.mapper;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.dao.api.DistPollerDao;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JTrapNotifier;
import org.opennms.netmgt.snmp.snmp4j.Snmp4JValue;
import org.opennms.netmgt.trapd.TrapDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.VariableBinding;

public class TrapInformation2TrapDtoMapper {
	public static final Logger LOG = LoggerFactory.getLogger(TrapInformation2TrapDtoMapper.class);

	private final DistPollerDao distPollerDao;

	public TrapInformation2TrapDtoMapper(DistPollerDao distPollerDao) {
		this.distPollerDao = Objects.requireNonNull(distPollerDao);
	}

	public TrapDTO object2dto(TrapInformation trapInfo) {
		final OnmsDistPoller distPoller = distPollerDao.whoami();
		final TrapDTO trapDTO = new TrapDTO();
		String version = trapInfo.getVersion();

		if ("v1".equalsIgnoreCase(trapInfo.getVersion())) {

			// TODO MVR add joesnmp stuff as well
			Snmp4JTrapNotifier.Snmp4JV1TrapInformation v1Trap = (Snmp4JTrapNotifier.Snmp4JV1TrapInformation)trapInfo;
			String community = v1Trap.getCommunity();
			InetAddress trapAddress = v1Trap.getTrapAddress();
			PDUv1 pdu = v1Trap.getPdu();

			trapDTO.setAgentAddress(v1Trap.getAgentAddress());
			trapDTO.setCommunity(community);
			trapDTO.setCreationTime(v1Trap.getCreationTime());
			trapDTO.setLocation(distPoller.getLocation());
			trapDTO.setPduLength(v1Trap.getPduLength());
			trapDTO.setSourceAddress(trapAddress);
			trapDTO.setSystemId(distPoller.getId());
			// NOTE: This value is an SNMP TimeTicks value, not an epoch timestamp
			trapDTO.setTimestamp(v1Trap.getTimeStamp());
			trapDTO.setVersion(version);
			trapDTO.setEnterpriseId(pdu.getEnterprise());
			trapDTO.setGeneric(pdu.getGenericTrap());
			trapDTO.setSpecific(pdu.getSpecificTrap());

			List<SnmpResult> results = new ArrayList<>();

			for (final VariableBinding varBind : pdu.getVariableBindings()) {
				final SnmpObjId oid = SnmpObjId.get(varBind.getOid().toString());
				final SnmpValue value = new Snmp4JValue(varBind.getVariable());
				final SnmpResult snmpResult = new SnmpResult(oid, null, value);
				results.add(snmpResult);
			}

			trapDTO.setResults(results);
		} else if (
			"v2".equalsIgnoreCase(version) ||
			"v3".equalsIgnoreCase(version)) {

			Snmp4JTrapNotifier.Snmp4JV2TrapInformation v2Trap = (Snmp4JTrapNotifier.Snmp4JV2TrapInformation)trapInfo;
			String community = v2Trap.getCommunity();
			InetAddress trapAddress = v2Trap.getTrapAddress();
			PDU pdu = v2Trap.getPdu();

			trapDTO.setAgentAddress(v2Trap.getAgentAddress());
			trapDTO.setCommunity(community);
			trapDTO.setCreationTime(v2Trap.getCreationTime());
			trapDTO.setLocation(distPoller.getLocation());
			trapDTO.setPduLength(v2Trap.getPduLength());
			trapDTO.setSourceAddress(trapAddress);
			trapDTO.setSystemId(distPoller.getId());
			// NOTE: This value is an SNMP TimeTicks value, not an epoch timestamp
			trapDTO.setTimestamp(v2Trap.getTimeStamp());
			trapDTO.setVersion(version);
			trapDTO.setEnterpriseId(new OID(v2Trap.getTrapIdentity().getEnterpriseId()));
			trapDTO.setGeneric(v2Trap.getTrapIdentity().getGeneric());
			trapDTO.setSpecific(v2Trap.getTrapIdentity().getSpecific());

			List<SnmpResult> results = new ArrayList<>();

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
