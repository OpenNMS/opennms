//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2009 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2009 Oct 01: Add ability to update database when an interface is deleted. - ayres@opennms.org
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
/*
 * Created on Nov 14, 2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.linkd;

import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author antonio
 * 
 */

public interface QueryManager {

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
	 * the int that indicated cdp address type
	 * 
	 */

	public static final int CDP_ADDRESS_TYPE_IP_ADDRESS = 1;

	static final char ACTION_UPTODATE = 'N';

	static final char ACTION_DELETE = 'D';

    public List<LinkableNode> getSnmpNodeList() throws SQLException;

    public LinkableNode getSnmpNode(int nodeid) throws SQLException;

    public void updateDeletedNodes() throws SQLException;

    public String getSnmpPrimaryIp(int nodeid) throws SQLException;
    
    public LinkableNode storeSnmpCollection(LinkableNode node, SnmpCollection snmpColl) throws SQLException;
    
    public void storeDiscoveryLink(DiscoveryLink discoveryLink) throws SQLException;
    
    public void update(int nodeid, char action) throws SQLException;
    
    public void updateForInterface(int nodeid, String ipAddr, int ifIndex, char action) throws SQLException;
    
    /**
     * @param connectionFactory
     */    
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate);

}