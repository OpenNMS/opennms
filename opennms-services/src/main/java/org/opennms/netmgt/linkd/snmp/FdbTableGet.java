/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd.snmp;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session creating
 * and collection occurs in the main run method of the instance. This allows the
 * collection to occur in a thread if necessary.
 */
public final class FdbTableGet {
    private static final Logger LOG = LoggerFactory.getLogger(FdbTableGet.class);

	private final static String FDB_PORT_OID = ".1.3.6.1.2.1.17.4.3.1.2";

	private final static String FDB_STATUS_OID = ".1.3.6.1.2.1.17.4.3.1.3";

	private final static String QFDB_PORT_OID = ".1.3.6.1.2.1.17.7.1.2.2.1.2";

	private final static String QFDB_STATUS_OID = ".1.3.6.1.2.1.17.7.1.2.2.1.3";
	/**
	 * The SnmpPeer object used to communicate via SNMP with the remote host.
	 */
	private SnmpAgentConfig m_agentConfig;

	private String m_mac;

	/**
	 * <p>Constructor for FdbTableGet.</p>
	 *
	 * @param config a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
	 * @param mac a {@link java.lang.String} object.
	 */
	public FdbTableGet(SnmpAgentConfig config, String mac) {
		m_agentConfig = config;
		m_mac = getInstanceString(mac);
	}


	/**
	 * <p>getBridgePort</p>
	 *
	 * @return a int.
	 */
	public int getBridgePort() {
		
		SnmpValue val = SnmpUtils.get(m_agentConfig, getOid(FDB_PORT_OID));
		if (val == null) return -1;
		if (val.isNull() || val.isError()) return -1;
		if (val.isNumeric()) return val.toInt();
		LOG.debug("getBridgePort: mac/bridgeport: {}/{}", m_mac, val.toDisplayString());

		return -1;
	}

	/**
	 * <p>getQBridgePort</p>
	 *
	 * @return a int.
	 */
	public int getQBridgePort() {
		
		SnmpValue val = SnmpUtils.get(m_agentConfig, getOid(QFDB_PORT_OID));
		if (val == null) return -1;
		if (val.isNull() || val.isError()) return -1;
		if (val.isNumeric()) return val.toInt();
		LOG.debug("getQBridgePort: mac/bridgeport: {}/{}", m_mac, val.toDisplayString());

		return -1;
	}

	/**
	 * <p>getBridgePortStatus</p>
	 *
	 * @return a int.
	 */
	public int getBridgePortStatus() {
		SnmpValue val = SnmpUtils.get(m_agentConfig, getOid(FDB_STATUS_OID));
		if (val == null) return -1;
		if (val.isNull() || val.isError()) return -1;
		if (val.isNumeric()) return val.toInt();
		LOG.debug("getBridgePortStatus: mac/bridgeport: {}/{}", m_mac, val.toDisplayString());
		return -1;
		
	}

	/**
	 * <p>getQBridgePortStatus</p>
	 *
	 * @return a int.
	 */
	public int getQBridgePortStatus() {
		SnmpValue val = SnmpUtils.get(m_agentConfig, getOid(QFDB_STATUS_OID));
		if (val == null) return -1;
		if (val.isNull() || val.isError()) return -1;
		if (val.isNumeric()) return val.toInt();
		LOG.debug("getQBridgePortStatus: mac/bridgeport: {}/{}", m_mac, val.toDisplayString());
		return -1;
		
	}

	private SnmpObjId getOid(String oid) {
		return SnmpObjId.get(oid+"."+m_mac);
	}
	
	private String getInstanceString(String mac) {
		
		return 		Integer.parseInt(mac.substring(0, 2),16) + "." +
					Integer.parseInt(mac.substring(2, 4),16) + "." +
					Integer.parseInt(mac.substring(4, 6),16) + "." +
					Integer.parseInt(mac.substring(6, 8),16) + "." +
					Integer.parseInt(mac.substring(8, 10),16)+ "." +
					Integer.parseInt(mac.substring(10, 12),16);

	}

}
