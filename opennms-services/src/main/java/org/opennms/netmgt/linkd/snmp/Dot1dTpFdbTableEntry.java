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

/**
 *<P>The Dot1dTpFdbTableEntry class is designed to hold all the MIB-II
 * information for one entry in the MIB II dot1dBridge.dot1dTp.dot1dTpFdbTable.
 * The table effectively contains a list of these entries, each entry having information
 * about bridge forwarding table.</P>
 *
 * <P>This object is used by the Dot1dTpFdbTable to hold information
 * single entries in the table. See the Dot1dTpFdbTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see Dot1dTpFdbTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public final class Dot1dTpFdbTableEntry extends SnmpStore {
	// Lookup strings for specific table entries
	//

	/** Constant <code>FDB_ADDRESS="dot1dTpFdbAddress"</code> */
	public final static String FDB_ADDRESS = "dot1dTpFdbAddress";

	/** Constant <code>FDB_PORT="dot1dTpFdbPort"</code> */
	public final static String FDB_PORT = "dot1dTpFdbPort";

	/** Constant <code>FDB_STATUS="dot1dTpFdbStatus"</code> */
	public final static String FDB_STATUS = "dot1dTpFdbStatus";


	/**
	 * <P>The keys that will be supported by default from the 
	 * TreeMap base class. Each of the elements in the list
	 * are an instance of the Dot1dTpFbTable. Objects
	 * in this list should be used by multiple instances of
	 * this class.</P>
	 */
	public static final NamedSnmpVar[] ms_elemList = new NamedSnmpVar[] {
	    /**
	     * A unicast MAC address for which the bridge has
	     * forwarding and/or filtering information.
	     */
	    new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, FDB_ADDRESS, ".1.3.6.1.2.1.17.4.3.1.1", 1),

	    /**
	     * Either the value '0', or the port number of the
	     * port on which a frame having a source address
	     * equal to the value of the corresponding instance
	     * of dot1dTpFdbAddress has been seen. A value of
	     * '0' indicates that the port number has not been
	     * learned but that the bridge does have some
	     * forwarding/filtering information about this
	     * address (e.g. in the dot1dStaticTable).
	     * Implementors are encouraged to assign the port
	     * value to this object whenever it is learned even
	     * for addresses for which the corresponding value of
	     * dot1dTpFdbStatus is not learned(3).
	     */
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32, FDB_PORT, ".1.3.6.1.2.1.17.4.3.1.2", 2),

	    /**
	     * The status of this entry. The meanings of the
	     * values are:
	     * other(1) : none of the following. This would
	     * include the case where some other
	     * MIB object (not the corresponding
	     * instance of dot1dTpFdbPort, nor an
	     * entry in the dot1dStaticTable) is
	     * being used to determine if and how
	     * frames addressed to the value of
	     * the corresponding instance of
	     * dot1dTpFdbAddress are being
	     * forwarded.
	     * invalid(2) : this entry is not longer valid
	     * (e.g., it was learned but has since
	     * aged-out), but has not yet been
	     * flushed from the table.
	     * learned(3) : the value of the corresponding
	     * instance of dot1dTpFdbPort was
	     * learned, and is being used.
	     * self(4) : the value of the corresponding
	     * instance of dot1dTpFdbAddress
	     * represents one of the bridge's
	     * addresses. The corresponding
	     * instance of dot1dTpFdbPort
	     * indicates which of the bridge's
	     * ports has this address.
	     * mgmt(5) : the value of the corresponding
	     * instance of dot1dTpFdbAddress is
	     * also the value of an existing
	     * instance of dot1dStaticAddress.
	     */
	    new NamedSnmpVar(NamedSnmpVar.SNMPINT32, FDB_STATUS, ".1.3.6.1.2.1.17.4.3.1.3", 3)
	};

	/**
	 * <P>The TABLE_OID is the object identifier that represents
	 * the root of the Bridge Forward table in the MIB forest.</P>
	 */
	public static final String TABLE_OID = ".1.3.6.1.2.1.17.4.3.1"; // start of table (GETNEXT)

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
	public Dot1dTpFdbTableEntry() {
		super(ms_elemList);
	}
	
	/**
	 * <p>getDot1dTpFdbAddress</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDot1dTpFdbAddress() {
		return getHexString(Dot1dTpFdbTableEntry.FDB_ADDRESS);
	}

	/**
	 * <p>getDot1dTpFdbPort</p>
	 *
	 * @return a int.
	 */
	public int getDot1dTpFdbPort() {
		Integer val = getInt32(Dot1dTpFdbTableEntry.FDB_PORT);
		if (val == null) return -1;
		return val;
	}

	/**
	 * <p>getDot1dTpFdbStatus</p>
	 *
	 * @return a int.
	 */
	public int getDot1dTpFdbStatus() {
		Integer val = getInt32(Dot1dTpFdbTableEntry.FDB_STATUS);
		if (val == null) return -1;
		return val;
	}
	
}
