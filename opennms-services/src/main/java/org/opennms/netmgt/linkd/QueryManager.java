/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.linkd;

import java.util.List;

import org.opennms.netmgt.model.OnmsArpInterface.StatusType;

/**
 * <p>QueryManager interface.</p>
 *
 * @author antonio
 * @version $Id: $
 */
public interface QueryManager {

    public static final int SNMP_IF_TYPE_ETHERNET = 6;

    public static final int SNMP_IF_TYPE_PROP_VIRTUAL = 53;

    public static final int SNMP_IF_TYPE_L2_VLAN = 135;

    public static final int SNMP_IF_TYPE_L3_VLAN = 136;

	/**
	 * The status of the info in FDB table entry The meanings of the value is
	 * other(1): none of the following. This would include the case where some
	 * other MIB object (not the corresponding instance of dot1dTpFdbPort, nor
	 * an entry in the dot1dStaticTable) is being used to determine if and how
	 * frames addressed to the value of the corresponding instance of
	 * dot1dTpFdbAddress are being forwarded.
	 */
	public static final int SNMP_DOT1D_FDB_STATUS_OTHER = 1;

	/**
	 * The status of the info in FDB table entry The status of this entry. The
	 * meanings of the values are: invalid(2) : this entry is not longer valid
	 * (e.g., it was learned but has since aged-out), but has not yet been
	 * flushed from the table.
	 */
	public static final int SNMP_DOT1D_FDB_STATUS_INVALID = 2;

	/**
	 * The status of the info in FDB table entry The status of this entry. The
	 * meanings of the values are: learned(3) : the value of the corresponding
	 * instance of dot1dTpFdbPort was learned, and is being used.
	 */
	public static final int SNMP_DOT1D_FDB_STATUS_LEARNED = 3;

	/**
	 * The status of the info in FDB table entry The status of this entry. The
	 * meanings of the values are: self(4) : the value of the corresponding
	 * instance of dot1dTpFdbAddress represents one of the bridge's addresses.
	 * The corresponding instance of dot1dTpFdbPort indicates which of the
	 * bridge's ports has this address.
	 */
	public static final int SNMP_DOT1D_FDB_STATUS_SELF = 4;

	/**
	 * mgmt(5) : the value of the corresponding instance of dot1dTpFdbAddress is
	 * also the value of an existing instance of dot1dStaticAddress.
	 */
	public static final int SNMP_DOT1D_FDB_STATUS_MGMT = 5;

    /**
     * <p>getSnmpNodeList</p>
     *
     * @return a {@link java.util.List} object.
     * @throws java.sql.SQLException if any.
     */
    List<LinkableNode> getSnmpNodeList();

    /**
     * <p>getSnmpNode</p>
     *
     * @param nodeid a int.
     * @return a {@link org.opennms.netmgt.linkd.LinkableNode} object.
     * @throws java.sql.SQLException if any.
     */
    LinkableNode getSnmpNode(int nodeid);

    /**
     * <p>updateDeletedNodes</p>
     *
     * @throws java.sql.SQLException if any.
     */
    void updateDeletedNodes();

    /**
     * <p>storeSnmpCollection</p>
     *
     * @param node a {@link org.opennms.netmgt.linkd.LinkableNode} object.
     * @param snmpColl a {@link org.opennms.netmgt.linkd.SnmpCollection} object.
     * @return a {@link org.opennms.netmgt.linkd.LinkableNode} object.
     * @throws java.sql.SQLException if any.
     */
    LinkableNode storeSnmpCollection(LinkableNode node, SnmpCollection snmpColl);
    
    /**
     * <p>storeDiscoveryLink</p>
     *
     * @param discoveryLink a {@link org.opennms.netmgt.linkd.DiscoveryLink} object.
     * @throws java.sql.SQLException if any.
     */
    void storeDiscoveryLink(DiscoveryLink discoveryLink);
    
    /**
     * <p>update</p>
     *
     * @param nodeid a int.
     * @param action a char.
     * @throws java.sql.SQLException if any.
     */
    void update(int nodeid, StatusType action);
    
    /**
     * <p>updateForInterface</p>
     *
     * @param nodeid a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param ifIndex a int.
     * @param action a char.
     * @throws java.sql.SQLException if any.
     */
    void updateForInterface(int nodeid, String ipAddr, int ifIndex, StatusType action);
    
    Linkd getLinkd();
	void setLinkd(final Linkd linkd);

}
