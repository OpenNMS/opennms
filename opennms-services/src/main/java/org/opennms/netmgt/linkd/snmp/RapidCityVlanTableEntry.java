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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.linkd.snmp;

import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;
import org.opennms.netmgt.capsd.snmp.SnmpTableEntry;

/**
 *<P>The RapidCityVlanTableEntry class is designed to hold all the MIB
 * information for one entry in the:
 * .1.3.6.1.4.1.2272.1.3.2.1
 *
 * <P>This object is used by the IntelVlanTable  to hold infomation
 * single entries in the table. See the IntelVlanTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see IntelVlanTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class RapidCityVlanTableEntry extends SnmpTableEntry
implements VlanCollectorEntry {

	// Lookup strings for specific table entries
	//

	private static String VLAN_INDEX_OID=".1.3.6.1.4.1.2272.1.3.2.1.1";
	private static String VLAN_NAME_OID=".1.3.6.1.4.1.2272.1.3.2.1.2";
	
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[] rcVlan_elemList = null;

	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static {
		rcVlan_elemList = new NamedSnmpVar[2];

		int ndx = 0;

		rcVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_INDEX, VLAN_INDEX_OID, 1);
		
		rcVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				VLAN_NAME, VLAN_NAME_OID, 2);
		
	}

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the table vtpVlanTable in the MIB forest.</P>
	 */
	public static final String TABLE_OID = ".1.3.6.1.4.1.2272.1.3.2.1"; // start of table (GETNEXT)

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
	public RapidCityVlanTableEntry() {
		super(rcVlan_elemList);
	}
	
}
