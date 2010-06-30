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
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 *<P>The ThreeComVlanTableEntry class is designed to hold all the MIB
 * information for one entry in the:
 * 1.3.6.1.4.1.43.10.1.14.1.2.1
 *
 * <P>This object is used by the ThreeComVlanTable  to hold infomation
 * single entries in the table. See the ThreeComVlanTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @see ThreeComVlanTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class ThreeComVlanTableEntry extends SnmpTableEntry
implements VlanCollectorEntry {

	// Lookup strings for specific table entries
	//
    /** Constant <code>VLAN_IN="a3ComVlanindex"</code> */
    public final static String VLAN_IN = "a3ComVlanindex";
	/** Constant <code>VLAN_IFINFO="a3ComVlanIfInfo"</code> */
	public final static String VLAN_IFINFO = "a3ComVlanIfInfo";

	private static String VLAN_INDEX_OID=".1.3.6.1.4.1.43.10.1.14.1.2.1.4";
	private static String VLAN_NAME_OID=".1.3.6.1.4.1.43.10.1.14.1.2.1.2";
	
	private boolean hasVlanIndex = false;
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[] threeComVlan_elemList = null;

	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static {
		threeComVlan_elemList = new NamedSnmpVar[6];

		int ndx = 0;

		threeComVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_IN, ".1.3.6.1.4.1.43.10.1.14.1.2.1.1", 1);
		
		threeComVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				VLAN_NAME, ".1.3.6.1.4.1.43.10.1.14.1.2.1.2", 2);
		
		threeComVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_TYPE, ".1.3.6.1.4.1.43.10.1.14.1.2.1.3", 3);

		threeComVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_INDEX, ".1.3.6.1.4.1.43.10.1.14.1.2.1.4", 4);
		
		threeComVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				VLAN_IFINFO, ".1.3.6.1.4.1.43.10.1.14.1.2.1.5", 5);

		threeComVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_STATUS, ".1.3.6.1.4.1.43.10.1.14.1.2.1.6", 6);

	}

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
	
	/** {@inheritDoc} */
	@Override
	public void storeResult(SnmpObjId base, SnmpInstId inst, SnmpValue val) {
		if (!hasVlanIndex) {
			int vlanid = inst.getLastSubId();
			super.storeResult(SnmpObjId.get(VLAN_INDEX_OID), inst, 
						SnmpUtils.getValueFactory().getInt32(vlanid));
			super.storeResult(SnmpObjId.get(VLAN_NAME_OID), inst, 
						SnmpUtils.getValueFactory().getOctetString("default".getBytes()));
			hasVlanIndex = true;
		}
		super.storeResult(base, inst, val);
	}

}
