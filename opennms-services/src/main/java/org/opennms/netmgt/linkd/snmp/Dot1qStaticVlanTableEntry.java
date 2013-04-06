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

import org.opennms.netmgt.model.OnmsVlan.VlanStatus;
import org.opennms.netmgt.model.OnmsVlan.VlanType;

/**
 *<P>The Dot1qStaticVlanTableEntry class is designed to hold all the MIB-II
 * information for one entry in the
 * .iso.org.dod.internet.mgmt.mib-2.
 * dot1dBridge.qBridgeMIB.qBridgeMIBObjects.dot1qVlan.dot1qVlanCurrentTable
 * <P>This object is used by the Dot1qStaticVlanTable to hold information
 * single entries in the table. See the Dot1qStaticVlanTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see Dot1qStaticVlanTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class Dot1qStaticVlanTableEntry extends Vlan {

	// Lookup strings for specific table entries
	//
	/** Constant <code>VLAN_STATICEGRESSPORTS="dot1qVlanStaticEgressPorts"</code> */
	public final static String VLAN_STATICEGRESSPORTS = "dot1qVlanStaticEgressPorts";
	/** Constant <code>VLAN_FORBIDDENEGRESSPORTS="dot1qVlanStaticForbiddenEgressPorts"</code> */
	public final static String VLAN_FORBIDDENEGRESSPORTS = "dot1qVlanStaticForbiddenEgressPorts";
	/** Constant <code>VLAN_STATICUNTAGGEDPORTS="dot1qVlanStaticUntaggedPorts"</code> */
	public final static String VLAN_STATICUNTAGGEDPORTS = "dot1qVlanStaticUntaggedPorts";

	private static String VLAN_NAME_OID=".1.3.6.1.2.1.17.7.1.4.3.1.1";
	
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static final NamedSnmpVar[] hpVlan_elemList = new NamedSnmpVar[] {
	    new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, VLAN_NAME,VLAN_NAME_OID,1),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32, VLAN_STATUS, ".1.3.6.1.2.1.17.7.1.4.3.1.5", 2)
//	    new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, VLAN_STATICEGRESSPORTS, ".1.3.6.1.2.1.17.7.1.4.3.1.2", 4),
//	    new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, VLAN_FORBIDDENEGRESSPORTS, ".1.3.6.1.2.1.17.7.1.4.3.1.3", 5),
//	    new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, VLAN_STATICUNTAGGEDPORTS, ".1.3.6.1.2.1.17.7.1.4.3.1.4", 6),
	};

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the table dot1qVlanCurrentTable in the MIB forest.</P>
	 */
	public static final String TABLE_OID = ".1.3.6.1.2.1.17.7.1.4.3.1"; // start of table (GETNEXT)

	/**
	 * <P>The class constructor used to initialize the
	 * object to its initial state. Although the
	 * object's member variables can change after an
	 * instance is created, this constructor will
	 * initialize all the variables as per their named
	 * variable from the passed array of SNMP varbinds.</P>
	 *
	 * <P>If the information in the object should not be
	 * modified then a <EM>final</EM> modifier can be
	 * applied to the created object.</P>
	 */
	public Dot1qStaticVlanTableEntry() {
		super(hpVlan_elemList);
	}

	@Override
	protected boolean hasVlanIndexOid() {
		return false;
	}

	@Override
	public VlanStatus getVlanStatus() {
		return VlanStatus.get(VlanStatus.ROWSTATUS_STARTING_INDEX+getInt32(VLAN_STATUS));
	}

	@Override
	public VlanType getVlanType() {
		return VlanType.CISCO_VTP_ETHERNET;
	}
}
