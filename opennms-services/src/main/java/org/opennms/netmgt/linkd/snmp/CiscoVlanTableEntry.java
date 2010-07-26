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
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;

/**
 *<P>The CiscoVlanTableEntry class is designed to hold all the information
 * for one entry in the:
 * iso.org.dod.internet.private.enterprises.cisco.ciscoMgmt.
 * ciscoVtpMIB.vtpMIBObjects.vlanInfo.vtpVlanTable
 *
 * <P>This object is used by the CiscoVlanTable  to hold infomation
 * single entries in the table. See the CiscoVlanPortTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS</A>
 * @see CiscoVlanTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class CiscoVlanTableEntry extends SnmpTableEntry
implements VlanCollectorEntry {

	// Lookup strings for specific table entries
	//
	/** Constant <code>VLAN_MTU="vtpVlanMtu"</code> */
	public final static String VLAN_MTU = "vtpVlanMtu";
	/** Constant <code>VLAN_D10S="vtpVlanDot10Said"</code> */
	public final static String VLAN_D10S = "vtpVlanDot10Said";
	/** Constant <code>VLAN_RINGN="vtpVlanRingNumber"</code> */
	public final static String VLAN_RINGN = "vtpVlanRingNumber";
	/** Constant <code>VLAN_BRIDGEN="vtpVlanBridgeNumber"</code> */
	public final static String VLAN_BRIDGEN = "vtpVlanBridgeNumber";
	/** Constant <code>VLAN_STPTYPE="vtpVlanStpType"</code> */
	public final static String VLAN_STPTYPE = "vtpVlanStpType";
	/** Constant <code>VLAN_PARV="vtpVlanParentVlan"</code> */
	public final static String VLAN_PARV = "vtpVlanParentVlan";
	/** Constant <code>VLAN_TRV1="vtpVlanTranslationalVlan1"</code> */
	public final static String VLAN_TRV1 = "vtpVlanTranslationalVlan1";
	/** Constant <code>VLAN_TRV2="vtpVlanTranslationalVlan2"</code> */
	public final static String VLAN_TRV2 = "vtpVlanTranslationalVlan2";
	/** Constant <code>VLAN_BRIDGETYPE="vtpVlanBridgeType"</code> */
	public final static String VLAN_BRIDGETYPE = "vtpVlanBridgeType";
	/** Constant <code>VLAN_AREHC="vtpVlanAreHopCount"</code> */
	public final static String VLAN_AREHC = "vtpVlanAreHopCount";
	/** Constant <code>VLAN_STEHC="vtpVlanSteHopCount"</code> */
	public final static String VLAN_STEHC = "vtpVlanSteHopCount";
	/** Constant <code>VLAN_ISCRFBACHUP="vtpVlanIsCRFBackup"</code> */
	public final static String VLAN_ISCRFBACHUP = "vtpVlanIsCRFBackup";
	/** Constant <code>VLAN_TYPEEXT="vtpVlanTypeExt"</code> */
	public final static String VLAN_TYPEEXT = "vtpVlanTypeExt";
	/** Constant <code>VLAN_IFINDEX="vtpVlanIfIndex"</code> */
	public final static String VLAN_IFINDEX = "vtpVlanIfIndex";

	private static String VLAN_INDEX_OID=".1.3.6.1.4.1.9.9.46.1.3.1.1.1";
	private static String VLAN_NAME_OID=".1.3.6.1.4.1.9.9.46.1.3.1.1.4";
	
	private boolean hasVlanIndex = false;
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[] ciscoVlan_elemList = null;

	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static {
		ciscoVlan_elemList = new NamedSnmpVar[18];

		int ndx = 0;

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_INDEX, VLAN_INDEX_OID, 1);
		
		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_STATUS, ".1.3.6.1.4.1.9.9.46.1.3.1.1.2", 2);
		
		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_TYPE, ".1.3.6.1.4.1.9.9.46.1.3.1.1.3", 3);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				VLAN_NAME, ".1.3.6.1.4.1.9.9.46.1.3.1.1.4", 4);
		
		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_MTU, ".1.3.6.1.4.1.9.9.46.1.3.1.1.5", 5);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				VLAN_D10S, ".1.3.6.1.4.1.9.9.46.1.3.1.1.6", 6);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_RINGN, ".1.3.6.1.4.1.9.9.46.1.3.1.1.7", 7);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_BRIDGEN, ".1.3.6.1.4.1.9.9.46.1.3.1.1.8", 8);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_STPTYPE, ".1.3.6.1.4.1.9.9.46.1.3.1.1.9", 9);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_PARV, ".1.3.6.1.4.1.9.9.46.1.3.1.1.10", 10);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_TRV1, ".1.3.6.1.4.1.9.9.46.1.3.1.1.11", 11);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_TRV2, ".1.3.6.1.4.1.9.9.46.1.3.1.1.12", 12);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_BRIDGETYPE, ".1.3.6.1.4.1.9.9.46.1.3.1.1.13", 13);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_AREHC, ".1.3.6.1.4.1.9.9.46.1.3.1.1.14", 14);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_STEHC, ".1.3.6.1.4.1.9.9.46.1.3.1.1.15", 15);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_ISCRFBACHUP, ".1.3.6.1.4.1.9.9.46.1.3.1.1.16", 16);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_TYPEEXT, ".1.3.6.1.4.1.9.9.46.1.3.1.1.17", 17);

		ciscoVlan_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				VLAN_IFINDEX, ".1.3.6.1.4.1.9.9.46.1.3.1.1.18", 18);
	}

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the table vtpVlanTable in the MIB forest.</P>
	 */
	public static final String TABLE_OID = ".1.3.6.1.4.1.9.9.46.1.3.1.1"; // start of table (GETNEXT)

	/**
	 * <p>Constructor for CiscoVlanTableEntry.</p>
	 */
	public CiscoVlanTableEntry() {
		super(ciscoVlan_elemList);
	}
	
	/** {@inheritDoc} */
	@Override
	public void storeResult(SnmpResult res) {
		if (!hasVlanIndex) {
			int vlanid = res.getInstance().getLastSubId();
			super.storeResult(new SnmpResult(SnmpObjId.get(VLAN_INDEX_OID), res.getInstance(), 
						SnmpUtils.getValueFactory().getInt32(vlanid)));
			super.storeResult(new SnmpResult(SnmpObjId.get(VLAN_NAME_OID), res.getInstance(), 
						SnmpUtils.getValueFactory().getOctetString("default".getBytes())));
			hasVlanIndex = true;
		}
		super.storeResult(res);
	}

}
