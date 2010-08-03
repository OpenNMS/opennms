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
 *<P>The Dot1dStpPortTableEntry class is designed to hold all the MIB-II
 * information for one entry in the MIB II dot1dBridge.dot1dStp.dot1dStpPortTable.
 * The table effectively contains a list of these entries, each entry having information
 * about Stp Protocol on sdecific Port.</P>
 *
 * <P>This object is used by the Dot1dStpPortTable to hold infomation
 * single entries in the table. See the Dot1dStpPortTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see Dot1dStpPortTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class Dot1dStpPortTableEntry extends SnmpTableEntry {
	// Lookup strings for specific table entries
	//

	/** Constant <code>STP_PORT="dot1dStpPort"</code> */
	public final static String STP_PORT = "dot1dStpPort";

	/** Constant <code>STP_PORT_PRIORITY="dot1dStpPortPriority"</code> */
	public final static String STP_PORT_PRIORITY = "dot1dStpPortPriority";

	/** Constant <code>STP_PORT_STATE="dot1dStpPortState"</code> */
	public final static String STP_PORT_STATE = "dot1dStpPortState";

	/** Constant <code>STP_PORT_ENABLE="dot1dStpPortEnable"</code> */
	public final static String STP_PORT_ENABLE = "dot1dStpPortEnable";

	/** Constant <code>STP_PORT_PATH_COST="dot1dStpPortPathCost"</code> */
	public final static String STP_PORT_PATH_COST = "dot1dStpPortPathCost";

	/** Constant <code>STP_PORT_DESIGNATED_ROOT="dot1dStpPortDesignatedRoot"</code> */
	public final static String STP_PORT_DESIGNATED_ROOT = "dot1dStpPortDesignatedRoot";

	/** Constant <code>STP_PORT_DESIGNATED_COST="dot1dStpPortDesignatedCost"</code> */
	public final static String STP_PORT_DESIGNATED_COST = "dot1dStpPortDesignatedCost";

	/** Constant <code>STP_PORT_DESIGNATED_BRIDGE="dot1dStpPortDesignatedBridge"</code> */
	public final static String STP_PORT_DESIGNATED_BRIDGE = "dot1dStpPortDesignatedBridge";

	/** Constant <code>STP_PORT_DESIGNATED_PORT="dot1dStpPortDesignatedPort"</code> */
	public final static String STP_PORT_DESIGNATED_PORT = "dot1dStpPortDesignatedPort";

	/** Constant <code>STP_PORT_FORW_TRANS="dot1dStpPortForwardTransitions"</code> */
	public final static String STP_PORT_FORW_TRANS = "dot1dStpPortForwardTransitions";

	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the StpPortTable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static NamedSnmpVar[] stpport_elemList = null;

	/**
	 * <P>Initialize the element list for the class. This
	 * is class wide data, but will be used by each instance.</P>
	 */
	static {
		stpport_elemList = new NamedSnmpVar[10];
		int ndx = 0;

		/**
		 * The port number of the port for which this entry
 		 * contains Spanning Tree Protocol management
 		 * information.
		 */
		stpport_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_PORT, ".1.3.6.1.2.1.17.2.15.1.1", 1);
		
		/**
		 * The value of the priority field which is
 		 * contained in the first (in network byte order)
 		 * octet of the (2 octet long) Port ID. The other
 		 * octet of the Port ID is given by the value of
 		 * dot1dStpPort.
		 */
		stpport_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_PORT_PRIORITY, ".1.3.6.1.2.1.17.2.15.1.2", 2);
		
		/**
		 * The port's current state as defined by
		 * application of the Spanning Tree Protocol. This
 		 * state controls what action a port takes on
 		 * reception of a frame. If the bridge has detected
 		 * a port that is malfunctioning it will place that
 		 * port into the broken(6) state. For ports which
 		 * are disabled (see dot1dStpPortEnable), this object
 		 * will have a value of disabled(1).
		 */
		stpport_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_PORT_STATE, ".1.3.6.1.2.1.17.2.15.1.3", 3);
		
		/**
		 * The enabled/disabled status of the port.
		 */
		stpport_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_PORT_ENABLE, ".1.3.6.1.2.1.17.2.15.1.4", 4);
		
		/**
		 * The contribution of this port to the path cost of
 		 * paths towards the spanning tree root which include
 		 * this port. 802.1D-1990 recommends that the
 		 * default value of this parameter be in inverse
 		 * proportion to the speed of the attached LAN.
		 */
		stpport_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_PORT_PATH_COST, ".1.3.6.1.2.1.17.2.15.1.5", 5);
		
		/**
		 * The unique Bridge Identifier of the Bridge
 		 * recorded as the Root in the Configuration BPDUs
 		 * transmitted by the Designated Bridge for the
 		 * segment to which the port is attached.
		 */
		stpport_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				STP_PORT_DESIGNATED_ROOT, ".1.3.6.1.2.1.17.2.15.1.6", 6);
		
		
		/**
		 * The path cost of the Designated Port of the
 		 * segment connected to this port. This value is
 		 * compared to the Root Path Cost field in received
 		 * bridge PDUs.
		 */
		stpport_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,
				STP_PORT_DESIGNATED_COST, ".1.3.6.1.2.1.17.2.15.1.7", 7);
		
		/**
		 * The Bridge Identifier of the bridge which this
		 * port considers to be the Designated Bridge for
 		 * this port's segment.
		 */
		stpport_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				STP_PORT_DESIGNATED_BRIDGE, ".1.3.6.1.2.1.17.2.15.1.8", 8);
		
		/**
		 * The Port Identifier of the port on the Designated
		 * Bridge for this port's segment.
		 */
		stpport_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,
				STP_PORT_DESIGNATED_PORT, ".1.3.6.1.2.1.17.2.15.1.9", 9);
		
		/**
		 * The number of times this port has transitioned
 		 * from the Learning state to the Forwarding state.
		 */
		stpport_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPCOUNTER32,
				STP_PORT_FORW_TRANS, ".1.3.6.1.2.1.17.2.15.1.10", 10);
	}

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the Stp Port table in the MIB forest.</P>
	 */
	public static final String TABLE_OID = ".1.3.6.1.2.1.17.2.15.1"; // start of table (GETNEXT)

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
	public Dot1dStpPortTableEntry() {
		super(stpport_elemList);
	}
	
	/**
	 * <p>getDot1dStpPort</p>
	 *
	 * @return a int.
	 */
	public int getDot1dStpPort() {
		Integer dot1dStpPort = getInt32(Dot1dStpPortTableEntry.STP_PORT);
		if (dot1dStpPort == null) return -1;
		return dot1dStpPort;
	}

	/**
	 * <p>getDot1dStpPortPriority</p>
	 *
	 * @return a int.
	 */
	public int getDot1dStpPortPriority() {
		Integer dot1dStpPortPriority = getInt32(Dot1dStpPortTableEntry.STP_PORT_PRIORITY);
		if (dot1dStpPortPriority == null) return -1;
		return dot1dStpPortPriority;
	}
	
	/**
	 * <p>getDot1dStpPortState</p>
	 *
	 * @return a int.
	 */
	public int getDot1dStpPortState() {
		Integer val = getInt32(Dot1dStpPortTableEntry.STP_PORT_STATE);
		if (val == null) return -1;
		return val;
	}
	
	/**
	 * <p>getDot1dStpPortEnable</p>
	 *
	 * @return a int.
	 */
	public int getDot1dStpPortEnable() {
		Integer val = getInt32(Dot1dStpPortTableEntry.STP_PORT_ENABLE);
		if (val == null) return -1;
		return val;
	}

	/**
	 * <p>getDot1dStpPortPathCost</p>
	 *
	 * @return a int.
	 */
	public int getDot1dStpPortPathCost() {
		Integer val = getInt32(Dot1dStpPortTableEntry.STP_PORT_PATH_COST);
		if (val == null) return -1;
		return val;
	}

	/**
	 * <p>getDot1dStpPortDesignatedRoot</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDot1dStpPortDesignatedRoot() {
		return getHexString(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_ROOT);
	}

	/**
	 * <p>getDot1dStpPortDesignatedCost</p>
	 *
	 * @return a int.
	 */
	public int getDot1dStpPortDesignatedCost() {
		Integer val =  getInt32(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_COST);
		if (val == null) return -1;
		return val;
	}

	/**
	 * <p>getDot1dStpPortDesignatedBridge</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDot1dStpPortDesignatedBridge() {
		return getHexString(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_BRIDGE);
	}

	/**
	 * <p>getDot1dStpPortDesignatedPort</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDot1dStpPortDesignatedPort() {
		return getHexString(Dot1dStpPortTableEntry.STP_PORT_DESIGNATED_PORT);
		
	}

	/**
	 * <p>getDot1dStpPortForwardTransitions</p>
	 *
	 * @return a int.
	 */
	public int getDot1dStpPortForwardTransitions() {
		Integer val = getInt32(Dot1dStpPortTableEntry.STP_PORT_FORW_TRANS);
		if (val == null) return -1;
		return val;
	}

}
