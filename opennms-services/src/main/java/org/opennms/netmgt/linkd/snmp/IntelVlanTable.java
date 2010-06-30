//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.snmp.SnmpTable;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * <P>
 * IntelVlanTable uses a SnmpSession to collect specific Intel Vendor vtp Vlan Table
 * entries. It implements the SnmpHandler to receive notifications when a reply
 * is received/error occurs in the SnmpSession used to send requests /recieve
 * replies.
 * </P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 * @version $Id: $
 */
public class IntelVlanTable extends SnmpTable<IntelVlanTableEntry> {

	/**
	 * <p>Constructor for IntelVlanTable.</p>
	 *
	 * @param address a {@link java.net.InetAddress} object.
	 */
	public IntelVlanTable(InetAddress address) {
        super(address, "intelVlanTable", IntelVlanTableEntry.intelVlan_elemList);
    }
    
    /** {@inheritDoc} */
    protected IntelVlanTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IntelVlanTableEntry();
    }


    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected final ThreadCategory log() {
        return ThreadCategory.getInstance(IntelVlanTable.class);
    }

}

