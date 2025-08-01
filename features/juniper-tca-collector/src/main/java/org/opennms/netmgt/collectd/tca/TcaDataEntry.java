/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
