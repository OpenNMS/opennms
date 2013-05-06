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
public final class CiscoVlanTableEntry extends Vlan {

	// Lookup strings for specific table entries
	//
	/** Constant <code>CISCOVTP_VLAN_MTU="vtpVlanMtu"</code> */
	public final static String CISCOVTP_VLAN_MTU = "vtpVlanMtu";
	/** Constant <code>CISCOVTP_VLAN_D10S="vtpVlanDot10Said"</code> */
	public final static String CISCOVTP_VLAN_D10S = "vtpVlanDot10Said";
	/** Constant <code>CISCOVTP_VLAN_RINGN="vtpVlanRingNumber"</code> */
	public final static String CISCOVTP_VLAN_RINGN = "vtpVlanRingNumber";
	/** Constant <code>CISCOVTP_VLAN_BRIDGEN="vtpVlanBridgeNumber"</code> */
	public final static String CISCOVTP_VLAN_BRIDGEN = "vtpVlanBridgeNumber";
	/** Constant <code>CISCOVTP_VLAN_STPTYPE="vtpVlanStpType"</code> */
	public final static String CISCOVTP_VLAN_STPTYPE = "vtpVlanStpType";
	/** Constant <code>CISCOVTP_VLAN_PARV="vtpVlanParentVlan"</code> */
	public final static String CISCOVTP_VLAN_PARV = "vtpVlanParentVlan";
	/** Constant <code>CISCOVTP_VLAN_TRV1="vtpVlanTranslationalVlan1"</code> */
	public final static String CISCOVTP_VLAN_TRV1 = "vtpVlanTranslationalVlan1";
	/** Constant <code>CISCOVTP_VLAN_TRV2="vtpVlanTranslationalVlan2"</code> */
	public final static String CISCOVTP_VLAN_TRV2 = "vtpVlanTranslationalVlan2";
	/** Constant <code>CISCOVTP_VLAN_BRIDGETYPE="vtpVlanBridgeType"</code> */
	public final static String CISCOVTP_VLAN_BRIDGETYPE = "vtpVlanBridgeType";
	/** Constant <code>CISCOVTP_VLAN_AREHC="vtpVlanAreHopCount"</code> */
	public final static String CISCOVTP_VLAN_AREHC = "vtpVlanAreHopCount";
	/** Constant <code>CISCOVTP_VLAN_STEHC="vtpVlanSteHopCount"</code> */
	public final static String CISCOVTP_VLAN_STEHC = "vtpVlanSteHopCount";
	/** Constant <code>CISCOVTP_VLAN_ISCRFBACHUP="vtpVlanIsCRFBackup"</code> */
	public final static String CISCOVTP_VLAN_ISCRFBACHUP = "vtpVlanIsCRFBackup";
	/** Constant <code>CISCOVTP_VLAN_TYPEEXT="vtpVlanTypeExt"</code> */
	public final static String CISCOVTP_VLAN_TYPEEXT = "vtpVlanTypeExt";
	/** Constant <code>CISCOVTP_VLAN_IFINDEX="vtpVlanIfIndex"</code> */
	public final static String CISCOVTP_VLAN_IFINDEX = "vtpVlanIfIndex";
	
    /**
     * <P>The TABLE_OID is the object identifier that represents
     * the root of the table vtpVlanTable in the MIB forest.</P>
     */
    public static final String TABLE_OID = ".1.3.6.1.4.1.9.9.46.1.3.1.1"; // start of table (GETNEXT)

	private static String CISCOVTP_VLAN_NAME_OID   = TABLE_OID + ".4";
	
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[] cisco_vlan_elemList = new NamedSnmpVar[] {
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_STATUS,      TABLE_OID + ".2"       , 1),
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       VLAN_TYPE,        TABLE_OID + ".3"       , 2),
	    new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, VLAN_NAME,        CISCOVTP_VLAN_NAME_OID , 3)
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_MTU,         TABLE_OID + ".5", 5),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CISCOVTP_VLAN_D10S,        TABLE_OID + ".6", 6),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_RINGN,       TABLE_OID + ".7", 7),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_BRIDGEN,     TABLE_OID + ".8", 8),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_STPTYPE,     TABLE_OID + ".9", 9),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_PARV,        TABLE_OID + ".10", 10),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_TRV1,        TABLE_OID + ".11", 11),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_TRV2,        TABLE_OID + ".12", 12),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_BRIDGETYPE,  TABLE_OID + ".13", 13),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_AREHC,       TABLE_OID + ".14", 14),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_STEHC,       TABLE_OID + ".15", 15),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_ISCRFBACHUP, TABLE_OID + ".16", 16),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_TYPEEXT,     TABLE_OID + ".17", 17),
	    //new NamedSnmpVar(NamedSnmpVar.SNMPINT32,       CISCOVTP_VLAN_IFINDEX,     TABLE_OID + ".18", 18)
	};

	/**
	 * <p>Constructor for CiscoVlanTableEntry.</p>
	 */
	public CiscoVlanTableEntry() {
		super(cisco_vlan_elemList);
	}

	@Override
	protected boolean hasVlanIndexOid() {
		return false;
	}

	@Override
	public VlanStatus getVlanStatus() {
		return VlanStatus.get(getInt32(VLAN_STATUS));
	}

	@Override
	public VlanType getVlanType() {
		return VlanType.get(getInt32(VLAN_TYPE));
	}
	
}
