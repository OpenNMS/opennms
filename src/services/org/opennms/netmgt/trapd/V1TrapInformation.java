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

package org.opennms.netmgt.trapd;

import java.net.InetAddress;

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
     * 
     */
    public V1TrapInformation(InetAddress agent, String community, SnmpPduTrap pdu) {
        super(agent, community);

        m_pdu = pdu;



    }

    /**
     * Returns the Protocol Data Unit that was encapsulated within the SNMP
     * Trap message
     */
    private SnmpPduTrap getPdu() {
        return m_pdu;
    }

    protected int getPduLength() {
        return getPdu().getLength();
    }

    protected long getTimeStamp() {
        long timeStamp = getPdu().getTimeStamp();
        return timeStamp;
    }

    protected TrapIdentity getTrapIdentity() {
        String entId = getPdu().getEnterprise().toString();
        if (!entId.startsWith(".")) {
            entId = "." + entId;
        }
        TrapIdentity trapIdentity = new TrapIdentity(entId, getPdu().getGeneric(), getPdu().getSpecific());
        return trapIdentity;
    }

    public String getTrapInterface() {
        return getPdu().getAgentAddress().toString();
    }

    protected SnmpVarBind getVarBindAt(int index) {
        return getPdu().getVarBindAt(index);
    }

    protected String getVersion() {
        return "v1";
    }

}
