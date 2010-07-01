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

package org.opennms.netmgt.provision.service.snmp;

import java.net.InetAddress;
import java.util.LinkedHashSet;
import java.util.Set;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

/**
 * <P>
 * The IfTable uses a SnmpSession to collect the entries in the remote agent's
 * interface table. It implements the SnmpHandler to receive notifications and
 * handle errors associated with the data collection. Data is collected using a
 * series of GETNEXT PDU request to walk multiple parts of the interface table
 * at once. The number of SNMP packets should not exceed the number of interface +
 * 1, assuming no lost packets or error conditions occur.
 * </P>
 *
 * <p>
 * <em>Addition by Jon Whetzel</em>
 * </p>
 * <p>
 * IfTable has an extra class variable for the SNMP version setting. If this is
 * set for SNMPv2, then a GETBULK command will be used for retrieving the
 * necessary data. Otherwise, the method will resort to its previous
 * implementation with GETNEXT commands.
 * </p>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya </A>
 * @author <A HREF="mailto:weave@oculan.com">Weave </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213 </A>
 * @version $Id: $
 */
public final class IfTable extends SnmpTable<IfTableEntry> {
    

    /**
     * <P>
     * Constructs an IfTable object that is used to collect the interface
     * elements from the remote agent. Once all the interfaces are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     *
     * @param address TODO
     * @see IfTableEntry
     */
    public IfTable(InetAddress address) {
        this(address, null);
    }
    
    /**
     * <p>Constructor for IfTable.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param ifIndices a {@link java.util.Set} object.
     */
    public IfTable(InetAddress address, Set<SnmpInstId> ifIndices) {
        super(address, "ifTable", IfTableEntry.ms_elemList, ifIndices);
    }

    /** {@inheritDoc} */
    protected IfTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IfTableEntry();
    }


    /**
     * <p>log</p>
     *
     * @return a {@link org.opennms.core.utils.ThreadCategory} object.
     */
    protected final ThreadCategory log() {
        return ThreadCategory.getInstance(IfTable.class);
    }
    
    /**
     * <p>getOperStatus</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getOperStatus(int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfOperStatus());
    }
        
    /**
     * <p>getAdminStatus</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getAdminStatus(int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfAdminStatus());
    }

    /**
     * <p>getIfType</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getIfType(int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfType());
    }

    /**
     * <p>getIfDescr</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getIfDescr(final int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfDescr());
    }

    /**
     * <p>getIfSpeed</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.Long} object.
     */
    public Long getIfSpeed(final int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfSpeed());
    }
    
    /**
     * <p>getPhysAddr</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getPhysAddr(final int ifIndex) {
        return (getEntry(ifIndex) == null ? null : getEntry(ifIndex).getPhysAddr());
    }

    /**
     * <p>updateSnmpInterfaceData</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void updateSnmpInterfaceData(OnmsNode node) {
        for(IfTableEntry entry : getEntries()) {
            updateSnmpInterfaceData(node, entry.getIfIndex());
        }
    }
    /**
     * <p>updateSnmpInterfaceData</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     * @param ifIndex a {@link java.lang.Integer} object.
     */
    public void updateSnmpInterfaceData(OnmsNode node, Integer ifIndex) {
        // first look to see if an snmpIf was created already
        OnmsSnmpInterface snmpIf = node.getSnmpInterfaceWithIfIndex(ifIndex);
        
        if (snmpIf == null) {
            // if not then create one
            snmpIf = new OnmsSnmpInterface(null, ifIndex, node);
        }
        
        updateSnmpInterfaceData(ifIndex, snmpIf);
    }

    private void updateSnmpInterfaceData(Integer ifIndex,
            OnmsSnmpInterface snmpIf) {
        // IfTable Attributes
        snmpIf.setIfType(getIfType(ifIndex));
        snmpIf.setIfAdminStatus(getAdminStatus(ifIndex));
        snmpIf.setIfDescr(getIfDescr(ifIndex));
        snmpIf.setIfSpeed(getIfSpeed(ifIndex));
        snmpIf.setPhysAddr(getPhysAddr(ifIndex));
    }

    /**
     * <p>getIfIndices</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Integer> getIfIndices() {
        Set<Integer> ifIndices = new LinkedHashSet<Integer>();
        for(SnmpInstId inst : getInstances()) {
            ifIndices.add(inst.toInt());
        }
        return ifIndices;
    }

}
