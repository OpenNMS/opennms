/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * <P>IpRouteTable uses a SnmpSession to collect the ipRouteTable entries
 * It implements the SnmpHandler to receive notifications when a reply is
 * received/error occurs in the SnmpSession used to send requests/receive
 * replies.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio Russo</A>
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public class IpRouteTable extends SnmpTable<IpRouteTableEntry>
{
	/**
	 * <P>Constructs an IpRouteTable object that is used to collect
	 * the address elements from the remote agent. Once all
	 * the elements are collected, or there is an error in
	 * the collection the signaler object is <EM>notified</EM>
	 * to inform other threads.</P>
	 *
	 * @see IpRouteTableEntry
	 * @param address a {@link java.net.InetAddress} object.
	 */
	public IpRouteTable(InetAddress address)
	{
        super(address, "ipRouteTable", IpRouteTableEntry.ms_elemList);
	}
	
    /** {@inheritDoc} */
    protected IpRouteTableEntry createTableEntry(final SnmpObjId base, final SnmpInstId inst, final Object val) {
        return new IpRouteTableEntry();
    }

}
