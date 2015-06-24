/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd.snmp;

import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.opennms.netmgt.snmp.SnmpUtils;

/**
 *<P>The CdpInterfaceTableEntry class is designed to hold all the MIB-II
 * information for one entry in the
 * .iso.org.dod.internet.private.enterprises.cisco.ciscoMgmt.
 * ciscoCdpMIB.ciscoCdpMIBObjects.cdpInterface.cdpInterfaceTable.cdpInterfaceEntry </P>
 * <P>This object is used by the CdpInterfaceTable  to hold information
 * single entries in the table. See the CdpInterfaceTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see CdpInterfaceTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class CdpInterfaceTableEntry extends SnmpStore {

	// Lookup strings for specific table entries
	//
	/** Constant <code>CDP_IFINDEX="cdpCacheIfIndex"</code> */
	public final static String CDP_INTERFACE_IFINDEX = "cdpInterfaceIfIndex";
        public final static String CDP_INTERFACE_IFNAME  = "cdpInterfaceIfName";

	private boolean hasIfIndex = false;

	private final static String CDP_INTERFACE_IFINDEX_OID = ".1.3.6.1.4.1.9.9.23.1.1.1.1.1";
        private final static String CDP_INTERFACE_IFNAME_OID  = ".1.3.6.1.4.1.9.9.23.1.1.1.1.6";
                                                           
	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static final NamedSnmpVar[] cdpInterface_elemList = new NamedSnmpVar[] {
		new NamedSnmpVar(NamedSnmpVar.SNMPINT32, CDP_INTERFACE_IFINDEX, CDP_INTERFACE_IFINDEX_OID, 1),

		new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, CDP_INTERFACE_IFNAME, CDP_INTERFACE_IFNAME_OID, 2)
	};

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the table CdPCacheTable in the MIB forest.</P>
	 */
	public static final String TABLE_OID = ".1.3.6.1.4.1.9.9.23.1.1.1.1"; // start of table (GETNEXT)

	/**
	 * <p>Constructor for CdpCacheTableEntry.</p>
	 */
	public CdpInterfaceTableEntry() {
		super(cdpInterface_elemList);
	}


	/** {@inheritDoc} */
	@Override
	public void storeResult(SnmpResult res) {
		if (!hasIfIndex) {
			int ifindex = res.getInstance().getLastSubId();
                        super.storeResult(new SnmpResult(SnmpObjId.get(CDP_INTERFACE_IFINDEX_OID), res.getInstance(), 
                                                         SnmpUtils.getValueFactory().getInt32(ifindex)));
			hasIfIndex = true;
		}
		super.storeResult(res);
	}
	
	/**
	 * <p>getCdpInterfaceIfIndex</p>
	 *
	 * @return a int.
	 */
	public int getCdpInterfaceIfIndex() {
	    return getInt32(CdpInterfaceTableEntry.CDP_INTERFACE_IFINDEX);
	}
	
	/**
	 * <p>getCdpInterfaceName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCdpInterfaceName() {
		return 	getDisplayString(CdpInterfaceTableEntry.CDP_INTERFACE_IFNAME);
	}
}
