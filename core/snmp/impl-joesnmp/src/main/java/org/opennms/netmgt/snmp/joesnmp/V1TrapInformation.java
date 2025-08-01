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
package org.opennms.netmgt.snmp.joesnmp;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpVarBindDTO;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.protocols.snmp.SnmpIPAddress;
import org.opennms.protocols.snmp.SnmpPduTrap;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * V1 trap element for processing by the queue reader
 */

public class V1TrapInformation extends TrapInformation {
    /**
     * The received PDU
     */
    private SnmpPduTrap m_pdu;

    /**
     * Constructs a new trap information instance that contains the sending
     * agent, the community string, and the Protocol Data Unit.
     * 
     * @param agent
     *            The sending agent's address
     * @param community
     *            The community string from the SNMP packet.
     * @param pdu
     *            The encapsulated Protocol Data Unit.
     */
    public V1TrapInformation(InetAddress agent, String community, SnmpPduTrap pdu) {
        super(agent, community);
        m_pdu = pdu;
    }

    @Override
    public int getPduLength() {
        return m_pdu.getLength();
    }

    @Override
    public long getTimeStamp() {
        return m_pdu.getTimeStamp();
    }

    @Override
    public TrapIdentity getTrapIdentity() {
        return new TrapIdentity(SnmpObjId.get(m_pdu.getEnterprise().getIdentifiers()), m_pdu.getGeneric(), m_pdu.getSpecific());
    }

    @Override
    public InetAddress getTrapAddress() {
        return SnmpIPAddress.toInetAddress(m_pdu.getAgentAddress());
    }

    private SnmpVarBind getVarBindAt(int index) {
        return m_pdu.getVarBindAt(index);
    }

    @Override
    public String getVersion() {
        return "v1";
    }

    @Override
    public SnmpVarBindDTO getSnmpVarBindDTO(int i) {
        SnmpObjId name = SnmpObjId.get(getVarBindAt(i).getName().getIdentifiers());
        SnmpValue value = new JoeSnmpValue(getVarBindAt(i).getValue());
        return new SnmpVarBindDTO(name, value);
    }

	@Override
	protected Integer getRequestId() {
		// JoeSnmp does not expose request-id for v1 TRAP PDU
		return 0;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("[");
		sb.append("Version=").append(getVersion())
			.append(", Agent-Addr=").append(getTrapAddress().getHostAddress())
			.append(", Length=").append(getPduLength())
			.append(", Identity=").append(getTrapIdentity().toString())
			.append("]");
		return sb.toString();
	}
}
