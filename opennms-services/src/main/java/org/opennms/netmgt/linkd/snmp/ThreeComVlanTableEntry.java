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
 *<P>The ThreeComVlanTableEntry class is designed to hold all the MIB
 * information for one entry in the:
 * 1.3.6.1.4.1.43.10.1.14.1.2.1
 *
 * <P>This object is used by the ThreeComVlanTable  to hold information
 * single entries in the table. See the ThreeComVlanTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see ThreeComVlanTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class ThreeComVlanTableEntry extends Vlan {

	// Lookup strings for specific table entries
	//
    /** Constant <code>VLAN_IN="a3ComVlanindex"</code> */
    public final static String VLAN_IN = "a3ComVlanIfIndex";
	/** Constant <code>VLAN_IFINFO="a3ComVlanIfInfo"</code> */
	public final static String VLAN_IFINFO = "a3ComVlanIfInfo";

	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public final static NamedSnmpVar[] threeComVlan_elemList = new NamedSnmpVar[] {
//	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32, VLAN_IN, ".1.3.6.1.4.1.43.10.1.14.1.2.1.1", 1),
	    new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, VLAN_NAME, ".1.3.6.1.4.1.43.10.1.14.1.2.1.2", 1),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32, VLAN_TYPE, ".1.3.6.1.4.1.43.10.1.14.1.2.1.3", 2),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32, VLAN_INDEX, ".1.3.6.1.4.1.43.10.1.14.1.2.1.4", 3),
//	    new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, VLAN_IFINFO, ".1.3.6.1.4.1.43.10.1.14.1.2.1.5", 5),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32, VLAN_STATUS, ".1.3.6.1.4.1.43.10.1.14.1.2.1.6", 4)
	};

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the table vtpVlanTable in the MIB forest.</P>
	 */
	public static final String TABLE_OID = " 1.3.6.1.4.1.43.10.1.14.1.2.1"; // start of table (GETNEXT)

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
	public ThreeComVlanTableEntry() {
		super(threeComVlan_elemList);
	}
	
	@Override
	protected boolean hasVlanIndexOid() {
		return true;
	}

	@Override
	public VlanStatus getVlanStatus() {
		return VlanStatus.get(VlanStatus.ROWSTATUS_STARTING_INDEX+getInt32(VLAN_STATUS));
	}

	@Override
	public VlanType getVlanType() {
		return VlanType.get(VlanType.THREECOM_STARTING_INDEX+getInt32(VLAN_TYPE));
	}

}
