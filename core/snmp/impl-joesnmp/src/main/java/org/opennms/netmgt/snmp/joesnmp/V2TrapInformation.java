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
import org.opennms.protocols.snmp.SnmpInt32;
import org.opennms.protocols.snmp.SnmpObjectId;
import org.opennms.protocols.snmp.SnmpPduPacket;
import org.opennms.protocols.snmp.SnmpSMI;
import org.opennms.protocols.snmp.SnmpSyntax;
import org.opennms.protocols.snmp.SnmpTimeTicks;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * V2 Trap information object for processing by the queue reader
 */
public class V2TrapInformation extends TrapInformation {
	/**
	 * The received PDU
	 */
	private SnmpPduPacket m_pdu;
    /**
     * The snmp sysUpTime OID is the first varbind
     */
    static final int SNMP_SYSUPTIME_OID_INDEX = 0;
    /**
     * The snmp trap OID is the second varbind
     */
    static final int SNMP_TRAP_OID_INDEX = 1;
    /**
     * The sysUpTimeOID, which should be the first varbind in a V2 trap
     */
    static final String SNMP_SYSUPTIME_OID = ".1.3.6.1.2.1.1.3.0";
    /**
     * The sysUpTimeOID, which should be the first varbind in a V2 trap, but in
     * the case of Extreme Networks only mostly
     */
    static final String EXTREME_SNMP_SYSUPTIME_OID = ".1.3.6.1.2.1.1.3";
    /**
     * The snmpTrapOID, which should be the second varbind in a V2 trap
     */
    static final String SNMP_TRAP_OID = ".1.3.6.1.6.3.1.1.4.1.0";

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
	public V2TrapInformation(InetAddress agent, String community, SnmpPduPacket pdu, TrapProcessor trapProcessor) {
		super(agent, community, trapProcessor);
        m_pdu = pdu;
	}

    /**
	 * Returns the Protocol Data Unit that was encapsulated within the SNMP
	 * Trap message
	 */
	private SnmpPduPacket getPdu() {
		return m_pdu;
	}

	protected int getPduLength() {
        return getPdu().getLength();
    }
    
    protected long getTimeStamp() {

        if (log().isDebugEnabled()) {
            log().debug("V2 trap first varbind value: " + getPdu().getVarBindAt(0).getValue().toString());
        }

        switch (getPdu().getVarBindAt(V2TrapInformation.SNMP_SYSUPTIME_OID_INDEX).getValue().typeId()) {
        case SnmpSMI.SMI_TIMETICKS:
            log().debug("V2 trap first varbind value is of type TIMETICKS (correct)");
            return ((SnmpTimeTicks) getPdu().getVarBindAt(V2TrapInformation.SNMP_SYSUPTIME_OID_INDEX).getValue()).getValue();
        case SnmpSMI.SMI_INTEGER:
            log().debug("V2 trap first varbind value is of type INTEGER, casting to TIMETICKS");
            return ((SnmpInt32) getPdu().getVarBindAt(V2TrapInformation.SNMP_SYSUPTIME_OID_INDEX).getValue()).getValue();
        default:
            throw new IllegalArgumentException("V2 trap does not have the required first varbind as TIMETICKS - cannot process trap");
        }
    }

    protected TrapIdentity getTrapIdentity() {
        // Get the value for the snmpTrapOID
        SnmpObjectId snmpTrapOid = (SnmpObjectId) getPdu().getVarBindAt(V2TrapInformation.SNMP_TRAP_OID_INDEX).getValue();
        SnmpObjectId lastVarBindOid = getPdu().getVarBindAt(getPduLength() - 1).getName();
        SnmpSyntax lastVarBindValue = getPdu().getVarBindAt(getPduLength() - 1).getValue();
        return new TrapIdentity(SnmpObjId.get(snmpTrapOid.getIdentifiers()), SnmpObjId.get(lastVarBindOid.getIdentifiers()), new JoeSnmpValue(lastVarBindValue));
    }

    public InetAddress getTrapAddress() {
        return getAgentAddress();
    }

    protected SnmpVarBind getVarBindAt(int index) {
        return getPdu().getVarBindAt(index);
    }

    protected String getVersion() {
        return "v2";
    }

    protected void validate() {
        //
        // verify the type
        //
        if (getPdu().typeId() != (byte) (SnmpPduPacket.V2TRAP)) {
            // if not V2 trap, do nothing
            throw new IllegalArgumentException("Received not SNMPv2 Trap from host " + getTrapAddress() + "PDU Type = " + getPdu().getCommand());
        }
        if (log().isDebugEnabled()) {
            log().debug("V2 trap numVars or pdu length: " + getPduLength());
        }
        if (getPduLength() < 2) // check number of varbinds
        {
            throw new IllegalArgumentException("V2 trap from " + getTrapAddress() + " IGNORED due to not having the required varbinds.  Have " + getPduLength() + ", needed 2");
        }
        // The first varbind has the sysUpTime
        // Modify the sysUpTime varbind to add the trailing 0 if it is
        // missing
        // The second varbind has the snmpTrapOID
        // Confirm that these two are present
        //
        String varBindName0 = getPdu().getVarBindAt(0).getName().toString();
        String varBindName1 = getPdu().getVarBindAt(1).getName().toString();
        if (varBindName0.equals(V2TrapInformation.EXTREME_SNMP_SYSUPTIME_OID)) {
            log().info("V2 trap from " + getTrapAddress() + " has been corrected due to the sysUptime.0 varbind not having been sent with a trailing 0.\n\tVarbinds received are : " + varBindName0 + " and " + varBindName1);
            varBindName0 = V2TrapInformation.SNMP_SYSUPTIME_OID;
        }
        if ((!(varBindName0.equals(V2TrapInformation.SNMP_SYSUPTIME_OID))) || (!(varBindName1.equals(V2TrapInformation.SNMP_TRAP_OID)))) {
            throw new IllegalArgumentException("V2 trap from " + getTrapAddress() + " IGNORED due to not having the required varbinds.\n\tThe first varbind must be sysUpTime.0 and the second snmpTrapOID.0\n\tVarbinds received are : " + varBindName0 + " and " + varBindName1);
        }
    }

    protected void processVarBindAt(int i) {
    	if (i<2) {
            if (i == 0) {
            	log().debug("Skipping processing of varbind it is the sysuptime and the first varbind, it is not processed as a parm per RFC2089");
            } else {
            	log().debug("Skipping processing of varbind it is the trap OID and the second varbind, it is not processed as a parm per RFC2089");				
			}
    	} else {
    		SnmpObjId name = SnmpObjId.get(getVarBindAt(i).getName().getIdentifiers());
    		SnmpValue value = new JoeSnmpValue(getVarBindAt(i).getValue());
    		processVarBind(name, value);
    	}
    }
}