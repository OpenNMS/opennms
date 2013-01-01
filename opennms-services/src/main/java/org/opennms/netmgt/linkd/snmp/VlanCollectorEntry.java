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
