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
 *<P>The Dot1dBaseTableEntry class is designed to hold all the MIB-II
 * information for one entry in the .iso.org.dod.internet.mgmt.mib-2.dot1dBridge.dot1dBase.dot1dBasePortTable
 * The table effectively contains a list of these entries, each entry having information
 * about bridge info. The entry dot1dBasePortTable.dot1dBasePortEntry contains:
 *
 *							dot1dBasePort
 *							dot1dBasePortIfIndex
 *							dot1dBasePortCircuit
 *							dot1dBasePortDelayExceededDiscards
 *							dot1dBasePortMtuExceededDiscards.</P>
 *
 * <P>This object is used by the Dot1dBasePortTable  to hold infomation
 * single entries in the table. See the Dot1dBasePortTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see Dot1dBasePortTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class Dot1dBasePortTableEntry extends SnmpTableEntry {
	// Lookup strings for specific table entries
	//
	/** Constant <code>BASE_PORT="dot1dBasePort"</code> */
	public final static String BASE_PORT = "dot1dBasePort";

	/** Constant <code>BASE_IFINDEX="dot1dBasePortIfIndex"</code> */
	public final static String BASE_IFINDEX = "dot1dBasePortIfIndex";

	/** Constant <code>BASE_PORT_CIRCUIT="dot1dBasePortCircuit"</code> */
	public final static String BASE_PORT_CIRCUIT = "dot1dBasePortCircuit";

	/** Constant <code>BASE_DELAY_EX_DIS="dot1dBasePortDelayExceededDiscards"</code> */
	public final static String BASE_DELAY_EX_DIS = "dot1dBasePortDelayExceededDiscards";

	/** Constant <code>BASE_MTU_EX_DIS="dot1dBasePortMtuExceededDiscards"</code> */
	public final static String BASE_MTU_EX_DIS = "dot1dBasePortMtuExceededDiscards";

	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the dot1dbasetable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[] bridgePort_elemList = null;

	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static {
		bridgePort_elemList = new NamedSnmpVar[5];
		int ndx = 0;

		/**
		 * The port number of the port for which this entry
 		 * contains bridge management information.
		 */
		bridgePort_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				BASE_PORT, ".1.3.6.1.2.1.17.1.4.1.1", 1);
		
		/**
		 * The value of the instance of the ifIndex object,
		 * defined in MIB-II, for the interface corresponding
 		 * to this port.
		 */
		bridgePort_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				BASE_IFINDEX, ".1.3.6.1.2.1.17.1.4.1.2", 2);
		
		/**
		 * For a port which (potentially) has the same value
 		 * of dot1dBasePortIfIndex as another port on the
 		 * same bridge, this object contains the name of an
 		 * object instance unique to this port. For example,
 		 * in the case where multiple ports correspond one-
 		 * to-one with multiple X.25 virtual circuits, this
 		 * value might identify an (e.g., the first) object
 		 * instance associated with the X.25 virtual circuit
 		 * corresponding to this port.
 		 * For a port which has a unique value of
 		 * dot1dBasePortIfIndex, this object can have the
 		 * value { 0 0 }.
		 */
		bridgePort_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOBJECTID,
				BASE_PORT_CIRCUIT, ".1.3.6.1.2.1.17.1.4.1.3", 3);
		
		/**
		 * The number of frames discarded by this port due
 		 * to excessive transit delay through the bridge. It
 		 * is incremented by both transparent and source
 		 * route bridges.
		 */
		bridgePort_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
				BASE_DELAY_EX_DIS, ".1.3.6.1.2.1.17.1.4.1.4", 4);
		
		/**
		 * The number of frames discarded by this port due
 		 * to an excessive size. It is incremented by both
 		 * transparent and source route bridges.
		 */
		bridgePort_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
				BASE_MTU_EX_DIS, ".1.3.6.1.2.1.17.1.4.1.5", 5);
	}

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the Dot1dBridge.Dot1dBase table in the MIB forest.</P>
	 */
	public static final String TABLE_OID = ".1.3.6.1.2.1.17.1.4.1"; // start of table (GETNEXT)

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
	public Dot1dBasePortTableEntry() {
		super(bridgePort_elemList);
	}
	
	/**
	 * <p>getBaseBridgePort</p>
	 *
	 * @return a int.
	 */
	public int getBaseBridgePort() {
		Integer basePort = getInt32(Dot1dBasePortTableEntry.BASE_PORT);
		if (basePort == null) return -1;
		return basePort;
	}

	/**
	 * <p>getBaseBridgePortIfindex</p>
	 *
	 * @return a int.
	 */
	public int getBaseBridgePortIfindex() {
		Integer basePort = getInt32(Dot1dBasePortTableEntry.BASE_IFINDEX);
		if (basePort == null) return -1;
		return basePort;
	}
	
	/**
	 * <p>getBasePortCircuit</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getBasePortCircuit() {
		return getObjectID(Dot1dBasePortTableEntry.BASE_PORT_CIRCUIT);
	}
	

	/**
	 * <p>getBasePortDelayExceededDiscards</p>
	 *
	 * @return a int.
	 */
	public int getBasePortDelayExceededDiscards() {
		Integer  delayExceededDiscards =getInt32(Dot1dBasePortTableEntry.BASE_DELAY_EX_DIS);
		if (delayExceededDiscards == null) return -1;
		return delayExceededDiscards;
	}

	/**
	 * <p>getBasePortMtuExceededDiscards</p>
	 *
	 * @return a int.
	 */
	public int getBasePortMtuExceededDiscards() {
		Integer mtuExceededDiscards = getInt32(Dot1dBasePortTableEntry.BASE_MTU_EX_DIS);
		if (mtuExceededDiscards == null) return -1;
		return mtuExceededDiscards;
		
	}
}
