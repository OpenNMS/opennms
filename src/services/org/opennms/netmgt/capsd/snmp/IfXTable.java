//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//
// IfXTable.java,v 1.1.1.1 2001/11/11 17:34:36 ben Exp
//

package org.opennms.netmgt.capsd.snmp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.opennms.netmgt.utils.Signaler;
import org.opennms.protocols.snmp.SnmpSession;
import org.opennms.protocols.snmp.SnmpVarBind;

/**
 * <P>
 * The IfXTable uses a SnmpSession to collect the entries in the remote agent's
 * interface extensions table. It implements the SnmpHandler to receive
 * notifications and handle errors associated with the data collection. Data is
 * collected using a series of GETNEXT PDU request to walk multiple parts of the
 * interface table at once. The number of SNMP packets should not exceed the
 * number of interface + 1, assuming no lost packets or error conditions occur.
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 * @version 1.1.1.1
 * 
 * @see <A HREF="http://www.ietf.org/rfc/rfc2233.txt">RFC2233 </A>
 */
public final class IfXTable extends SnmpTableWalker {

    /**
     * <P>
     * The list of interfaces from the remote's interface extensions table. The
     * list contains a set of IfXTableEntry objects that were collected from the
     * remote host.
     * </P>
     * 
     * @see IfXTableEntry
     */
    private List m_entries;

    /**
     * <P>
     * Constructs an IfXTable object that is used to collect the interface
     * elements from the remote agent. Once all the interfaces are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     * @param session
     *            The session with the remote agent.
     * @param address TODO
     * @param signaler
     *            The object to notify waiters.
     * @param version
     *            SNMP version to use
     * 
     * @see IfXTableEntry
     */
    public IfXTable(SnmpSession session, InetAddress address, Signaler signaler, int version) {
        super(address, signaler, version, "ifXTable", IfXTableEntry.ms_elemList, IfXTableEntry.TABLE_OID);

        m_entries = new ArrayList(2);
        
        start(session);
    }

    /**
     * <P>
     * Returns the list of entry maps that can be used to access all the
     * information about the interface extensions table.
     * </P>
     * 
     * @return The list of ifXTableEntry maps.
     */
    public List getEntries() {
        return m_entries;
    }

    protected void processTableRow(SnmpVarBind[] vblist) {
        IfXTableEntry ent = new IfXTableEntry(vblist);
        m_entries.add(ent);
    }

    public String getIfName(int ifIndex) {
    
        // Find ifXTable entry with matching ifIndex
        //
        Iterator iter = getEntries().iterator();
        while (iter.hasNext()) {
            IfXTableEntry ifXEntry = (IfXTableEntry) iter.next();
    
            int ifXIndex = -1;
            Integer snmpIfIndex = ifXEntry.getIfIndex();
            if (snmpIfIndex != null)
                ifXIndex = snmpIfIndex.intValue();
    
            // compare with passed ifIndex
            if (ifXIndex == ifIndex) {
                // Found match! Get the ifName
                return ifXEntry.getIfName();
            }
    
        }
        return null;
    }

    public String getIfIndex(int ifIndex) {
        // Find ifXTable entry with matching ifIndex
        //
        Iterator iter = getEntries().iterator();
        while (iter.hasNext()) {
            IfXTableEntry ifXEntry = (IfXTableEntry) iter.next();
    
            int ifXIndex = -1;
            Integer snmpIfIndex = ifXEntry.getIfIndex();
            if (snmpIfIndex != null)
                ifXIndex = snmpIfIndex.intValue();
    
            // compare with passed ifIndex
            if (ifXIndex == ifIndex) {
                // Found match! Get the ifAlias
                return ifXEntry.getIfAlias();
            }
    
        }
        return null;
    }

}
