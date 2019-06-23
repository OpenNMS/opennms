/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;

/**
 *<P>The Dot1dTpFdbTableTracker class is designed to hold all the MIB-II
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
public class Dot1dTpFdbTableTracker extends TableTracker {

	/**
	 * The status of the info in FDB table entry The status of this entry. The
	 * meanings of the values are: learned(3) : the value of the corresponding
	 * instance of dot1dTpFdbPort was learned, and is being used.
	 */
	public static final int SNMP_DOT1D_FDB_STATUS_LEARNED = 3;


	 public final static SnmpObjId DOT1D_TP_FDB_ADDRESS = SnmpObjId.get(".1.3.6.1.2.1.17.4.3.1.1");
	 public final static SnmpObjId DOT1D_TP_FDB_PORT    = SnmpObjId.get(".1.3.6.1.2.1.17.4.3.1.2");
	 public final static SnmpObjId DOT1D_TP_FDB_STATUS  = SnmpObjId.get(".1.3.6.1.2.1.17.4.3.1.3");


	public static final SnmpObjId[] ms_elemList = new SnmpObjId[] {
	    /**
	     * A unicast MAC address for which the bridge has
	     * forwarding and/or filtering information.
	     *  REFERENCE
           "IEEE 802.1D-1998: clause 7.9.1, 7.9.2"
	     */
	    DOT1D_TP_FDB_ADDRESS,

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
	    DOT1D_TP_FDB_PORT,

	    /**
         *  "The status of this entry.  The meanings of the
         *  values are:
         *      other(1) - none of the following.  This would
         *          include the case where some other MIB object
         *          (not the corresponding instance of
         *          dot1dTpFdbPort, nor an entry in the
         *          dot1dStaticTable) is being used to determine if
         *          and how frames addressed to the value of the
         *          corresponding instance of dot1dTpFdbAddress are
         *          being forwarded.
         *      invalid(2) - this entry is no longer valid (e.g.,
         *          it was learned but has since aged out), but has
         *          not yet been flushed from the table.
         *      learned(3) - the value of the corresponding instance
         *          of dot1dTpFdbPort was learned, and is being
         *          used.
         *      self(4) - the value of the corresponding instance of
         *          dot1dTpFdbAddress represents one of the bridge's
         *          addresses.  The corresponding instance of
         *          dot1dTpFdbPort indicates which of the bridge's
         *          ports has this address.
         *      mgmt(5) - the value of the corresponding instance of
         *          dot1dTpFdbAddress is also the value of an
         *          existing instance of dot1dStaticAddress."
	     */
	    DOT1D_TP_FDB_STATUS
	};

	public static class Dot1dTpFdbRow extends SnmpRowResult {
		public Dot1dTpFdbRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
		}

		/**
		 * <p>getDot1dTpFdbAddress</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getDot1dTpFdbAddress() {
			if (getValue(DOT1D_TP_FDB_ADDRESS) != null)
				return getValue(DOT1D_TP_FDB_ADDRESS).toHexString();
			return null;
		}
	
		/**
		 * <p>getDot1dTpFdbPort</p>
		 *
		 * @return a int.
		 */
		public Integer getDot1dTpFdbPort() {
			if (getValue(DOT1D_TP_FDB_PORT) != null)
				return getValue(DOT1D_TP_FDB_PORT).toInt();
			return null;
		}
	
		/**
		 * <p>getDot1dTpFdbStatus</p>
		 *
		 * @return a int.
		 */
		public Integer getDot1dTpFdbStatus() {
			if (getValue(DOT1D_TP_FDB_STATUS) != null)
				return getValue(DOT1D_TP_FDB_STATUS).toInt();
			return null;
		}

		public BridgeForwardingTableEntry getLink() {
			BridgeForwardingTableEntry link = new BridgeForwardingTableEntry();
			link.setBridgePort(getDot1dTpFdbPort());
			link.setMacAddress(getDot1dTpFdbAddress());
			if (getDot1dTpFdbStatus() != null)
				link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.get(getDot1dTpFdbStatus()));
            return link;
		}

	}
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
	public Dot1dTpFdbTableTracker() {
		super(ms_elemList);
	}
	
	public Dot1dTpFdbTableTracker(RowCallback rowProcessor) {
		super(rowProcessor, ms_elemList);
	}

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new Dot1dTpFdbRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processDot1dTpFdbRow((Dot1dTpFdbRow)row);
    }

    /**
     * <p>processIpNetToMediaRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.Dot1dTpFdbTableTracker.Dot1dTpFdbRow} object.
     */
    public void processDot1dTpFdbRow(final Dot1dTpFdbRow row) {
    }
	

}
