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
/*
 * Created on 12-dic-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.linkd.snmp;

/**
 * <p>VlanCollectorEntry interface.</p>
 *
 * @author antonio
 *
 * See CISCO-VTP-MIB for definition
 * @version $Id: $
 */
public interface VlanCollectorEntry {
	
	/** Constant <code>VLAN_INDEX="vtpVlanIndex"</code> */
	public final static String VLAN_INDEX = "vtpVlanIndex";
	/** Constant <code>VLAN_NAME="vtpVlanName"</code> */
	public final static String VLAN_NAME = "vtpVlanName";
	/** Constant <code>VLAN_STATUS="vtpVlanStatus"</code> */
	public final static String VLAN_STATUS = "vtpVlanStatus";
	/** Constant <code>VLAN_TYPE="vtpVlanType"</code> */
	public final static String VLAN_TYPE = "vtpVlanType";

	/** Constant <code>VLAN_TYPE_ETHERNET=1</code> */
	public final static int VLAN_TYPE_ETHERNET = 1;
	/** Constant <code>VLAN_TYPE_FDDI=2</code> */
	public final static int VLAN_TYPE_FDDI = 2;
	/** Constant <code>VLAN_TYPE_TOKENRING=3</code> */
	public final static int VLAN_TYPE_TOKENRING = 3;
	/** Constant <code>VLAN_TYPE_FDDINET=4</code> */
	public final static int VLAN_TYPE_FDDINET = 4;
	/** Constant <code>VLAN_TYPE_TRNET=5</code> */
	public final static int VLAN_TYPE_TRNET = 5;
	/** Constant <code>VLAN_TYPE_DEPRECATED=6</code> */
	public final static int VLAN_TYPE_DEPRECATED = 6;
	
	/** Constant <code>VLAN_STATUS_OPERATIONAL=1</code> */
	public final static int VLAN_STATUS_OPERATIONAL = 1;
	/** Constant <code>VLAN_STATUS_SUSPENDED=2</code> */
	public final static int VLAN_STATUS_SUSPENDED = 2;
	/** Constant <code>VLAN_STATUS_mtuTooBigForDevice=3</code> */
	public final static int VLAN_STATUS_mtuTooBigForDevice = 3;
	/** Constant <code>VLAN_STATUS_mtuTooBigForTrunk=4</code> */
	public final static int VLAN_STATUS_mtuTooBigForTrunk = 4;
	
}
