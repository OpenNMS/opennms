/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpStore;

/**
 * The Class TcaDataEntry.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public final class TcaDataEntry extends SnmpStore {

	/** Constant <code>TCA_PEER_ADDRESS="jnxTcaSlaRawDataPeerIPAddress"</code>. */
	public static final String	TCA_PEER_ADDRESS = "jnxTcaSlaRawDataPeerIPAddress";

	/** Constant <code>TCA_RAW_DATA="jnxTcaSlaRawDataString"</code>. */
	public static final String	TCA_RAW_DATA = "jnxTcaSlaRawDataString";

	/** The Constant TCA Element List. */
	public static final NamedSnmpVar[] tca_elemList = new NamedSnmpVar[] {
		new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, TCA_PEER_ADDRESS, ".1.3.6.1.4.1.27091.3.1.6.1.1", 1),
		new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, TCA_RAW_DATA, ".1.3.6.1.4.1.27091.3.1.6.1.2", 2),
	};
	
	private final SnmpInstId m_instance;

	/**
	 * Instantiates a new TCA data entry.
	 */
	public TcaDataEntry(SnmpInstId instance) {
		super(tca_elemList);
		m_instance = instance;
	}
	
	
	public SnmpInstId getInstance() {
		return m_instance;
	}

	/**
	 * Gets the peer address.
	 *
	 * @return the peer address
	 */
	public String getPeerAddress() {
		return getDisplayString(TCA_PEER_ADDRESS);
	}

	/**
	 * Gets the raw data.
	 *
	 * @return the raw data
	 */
	public String getRawData() {
		return getDisplayString(TCA_RAW_DATA);
	}
}
