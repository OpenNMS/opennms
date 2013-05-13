/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.snmp;

import java.net.InetAddress;
import java.util.Set;

import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;

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
 * @see <A HREF="http://www.ietf.org/rfc/rfc2233.txt">RFC2233 </A>
 */
public final class IfXTable extends SnmpTable<IfXTableEntry> {

    /**
     * <P>
     * Constructs an IfXTable object that is used to collect the interface
     * elements from the remote agent. Once all the interfaces are collected, or
     * there is an error in the collection the signaler object is <EM>notified
     * </EM> to inform other threads.
     * </P>
     *
     * @param address TODO
     * @see IfXTableEntry
     */
    public IfXTable(InetAddress address) {
        this(address, null);
    }

    /**
     * <p>Constructor for IfXTable.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param ifIndices a {@link java.util.Set} object.
     */
    public IfXTable(InetAddress address, Set<SnmpInstId> ifIndices) {
        super(address, "ifXTable", IfXTableEntry.ms_elemList, ifIndices);
    }

    /** {@inheritDoc} */
    @Override
    protected IfXTableEntry createTableEntry(SnmpObjId base, SnmpInstId inst, Object val) {
        return new IfXTableEntry(inst.toInt());
    }
    
    /**
     * <p>getIfName</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getIfName(int ifIndex) {
        return getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfName();
    }

    /**
     * <p>getIfAlias</p>
     *
     * @param ifIndex a int.
     * @return a {@link java.lang.String} object.
     */
    public String getIfAlias(int ifIndex) {
        return getEntry(ifIndex) == null ? null : getEntry(ifIndex).getIfAlias();
    }

    /**
     * <p>updateSnmpInterfaceData</p>
     *
     * @param node a {@link org.opennms.netmgt.model.OnmsNode} object.
     */
    public void updateSnmpInterfaceData(OnmsNode node) {
        for(IfXTableEntry entry : getEntries()) {
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
        OnmsSnmpInterface snmpIf2 = node.getSnmpInterfaceWithIfIndex(ifIndex);
        
        if (snmpIf2 == null) {
            // if not then create one
            snmpIf2 = new OnmsSnmpInterface(node, ifIndex);
        }
        // ifXTable Attributes
        snmpIf2.setIfAlias(getIfAlias(ifIndex));
        snmpIf2.setIfName(getIfName(ifIndex));
    }

}
