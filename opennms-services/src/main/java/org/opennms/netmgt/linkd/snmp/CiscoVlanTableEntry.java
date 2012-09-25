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

import org.opennms.netmgt.capsd.snmp.NamedSnmpVar;
import org.opennms.netmgt.capsd.snmp.SnmpStore;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;

/**
 *<P>The CiscoVlanTableEntry class is designed to hold all the information
 * for one entry in the:
 * iso.org.dod.internet.private.enterprises.cisco.ciscoMgmt.ciscoVtpMIB.vtpMIBObjects.vlanInfo.vtpVlanTable</P>
 *
 * <P>This object is used by the CiscoVlanTable  to hold information
 * single entries in the table. See the CiscoVlanPortTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see CiscoVlanTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 */
public final class CiscoVlanTableEntry extends SnmpStore implements VlanCollectorEntry {

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

    /**
     * <P>The TABLE_OID is the object identifier that represents
     * the root of the table vtpVlanTable in the MIB forest.</P>
     */
    public static final String TABLE_OID = ".1.3.6.1.4.1.9.9.46.1.3.1.1"; // start of table (GETNEXT)

	private static String VLAN_INDEX_OID  = TABLE_OID + ".1";
	private static String VLAN_NAME_OID   = TABLE_OID + ".4";
	
	private boolean hasVlanIndex = false;
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[] ciscoVlan_elemList = new NamedSnmpVar[] {
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_INDEX,       VLAN_INDEX_OID, 1),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_STATUS,      TABLE_OID + ".2", 2),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_TYPE,        TABLE_OID + ".3", 3),
	    new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, VLAN_NAME,        TABLE_OID + ".4", 4),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_MTU,         TABLE_OID + ".5", 5),
	    new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, VLAN_D10S,        TABLE_OID + ".6", 6),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_RINGN,       TABLE_OID + ".7", 7),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_BRIDGEN,     TABLE_OID + ".8", 8),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_STPTYPE,     TABLE_OID + ".9", 9),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_PARV,        TABLE_OID + ".10", 10),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_TRV1,        TABLE_OID + ".11", 11),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_TRV2,        TABLE_OID + ".12", 12),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_BRIDGETYPE,  TABLE_OID + ".13", 13),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_AREHC,       TABLE_OID + ".14", 14),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_STEHC,       TABLE_OID + ".15", 15),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_ISCRFBACHUP, TABLE_OID + ".16", 16),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_TYPEEXT,     TABLE_OID + ".17", 17),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_IFINDEX,     TABLE_OID + ".18", 18)
	};

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
