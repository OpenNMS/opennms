//
//This file is part of the OpenNMS(R) Application.
//
//OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.
//
//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
//Modifications:
//
//2003 Jan 31: Cleaned up some unused imports.
//2003 Jan 08: Added code to associate the IP address in traps with nodes
//           and added the option to discover nodes based on traps.
//
//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.                                                            
//
//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
//For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
//

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

    protected int getPduLength() {
        return m_pdu.getLength();
    }

    protected long getTimeStamp() {
        return m_pdu.getTimeStamp();
    }

    protected TrapIdentity getTrapIdentity() {
        return new TrapIdentity(SnmpObjId.get(m_pdu.getEnterprise().getIdentifiers()), m_pdu.getGeneric(), m_pdu.getSpecific());
    }

    protected InetAddress getTrapAddress() {
        return SnmpIPAddress.toInetAddress(m_pdu.getAgentAddress());
    }

    protected SnmpVarBind getVarBindAt(int index) {
        return m_pdu.getVarBindAt(index);
    }

    protected String getVersion() {
        return "v1";
    }

    protected void processVarBindAt(int i) {
        SnmpObjId name = SnmpObjId.get(getVarBindAt(i).getName().getIdentifiers());
        SnmpValue value = new JoeSnmpValue(getVarBindAt(i).getValue());
        processVarBind(name, value);
    }

}
