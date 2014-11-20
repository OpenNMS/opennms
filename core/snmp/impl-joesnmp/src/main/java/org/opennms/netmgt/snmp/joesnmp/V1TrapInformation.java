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

package org.opennms.netmgt.snmp.joesnmp;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TrapIdentity;
import org.opennms.netmgt.snmp.TrapInformation;
import org.opennms.netmgt.snmp.TrapProcessor;
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
     * @param trapProcessor The trap processor used to process the trap data
     * 
     */
    public V1TrapInformation(InetAddress agent, String community, SnmpPduTrap pdu, TrapProcessor trapProcessor) {
        super(agent, community, trapProcessor);
        m_pdu = pdu;
    }

    @Override
    protected int getPduLength() {
        return m_pdu.getLength();
    }

    @Override
    protected long getTimeStamp() {
        return m_pdu.getTimeStamp();
    }

    @Override
    protected TrapIdentity getTrapIdentity() {
        return new TrapIdentity(SnmpObjId.get(m_pdu.getEnterprise().getIdentifiers()), m_pdu.getGeneric(), m_pdu.getSpecific());
    }

    @Override
    protected InetAddress getTrapAddress() {
        return SnmpIPAddress.toInetAddress(m_pdu.getAgentAddress());
    }

    private SnmpVarBind getVarBindAt(int index) {
        return m_pdu.getVarBindAt(index);
    }

    @Override
    protected String getVersion() {
        return "v1";
    }

    @Override
    protected void processVarBindAt(int i) {
        SnmpObjId name = SnmpObjId.get(getVarBindAt(i).getName().getIdentifiers());
        SnmpValue value = new JoeSnmpValue(getVarBindAt(i).getValue());
        processVarBind(name, value);
    }

}
