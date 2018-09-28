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

import org.opennms.netmgt.model.BridgeStpLink;
import org.opennms.netmgt.model.BridgeStpLink.BridgeDot1dStpPortEnable;
import org.opennms.netmgt.model.BridgeStpLink.BridgeDot1dStpPortState;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;

/**
 *<P>The Dot1dStpPortTableTracker class is designed to hold all the MIB-II
 * information for one entry in the MIB II dot1dBridge.dot1dStp.dot1dStpPortTable.
 * The table effectively contains a list of these entries, each entry having information
 * about STP Protocol on specific Port.</P>
 *
 * <P>This object is used by the Dot1dStpPortTable to hold information
 * single entries in the table. See the Dot1dStpPortTable documentation
 * form more information.</P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see Dot1dStpPortTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public class Dot1dStpPortTableTracker extends TableTracker {

	public final static SnmpObjId DOT1D_STP_PORT                   = SnmpObjId.get(".1.3.6.1.2.1.17.2.15.1.1");
	public final static SnmpObjId DOT1D_STP_PORT_PRIORITY          = SnmpObjId.get(".1.3.6.1.2.1.17.2.15.1.2");
	public final static SnmpObjId DOT1D_STP_PORT_STATE             = SnmpObjId.get(".1.3.6.1.2.1.17.2.15.1.3");
	public final static SnmpObjId DOT1D_STP_PORT_ENABLE            = SnmpObjId.get(".1.3.6.1.2.1.17.2.15.1.4");
	public final static SnmpObjId DOT1D_STP_PORT_PATH_COST         = SnmpObjId.get(".1.3.6.1.2.1.17.2.15.1.5");
	public final static SnmpObjId DOT1D_STP_PORT_DESIGNATED_ROOT   = SnmpObjId.get(".1.3.6.1.2.1.17.2.15.1.6");
	public final static SnmpObjId DOT1D_STP_PORT_DESIGNATED_COST   = SnmpObjId.get(".1.3.6.1.2.1.17.2.15.1.7");
	public final static SnmpObjId DOT1D_STP_PORT_DESIGNATED_BRIDGE = SnmpObjId.get(".1.3.6.1.2.1.17.2.15.1.8");
	public final static SnmpObjId DOT1D_STP_PORT_DESIGNATED_PORT   = SnmpObjId.get(".1.3.6.1.2.1.17.2.15.1.9");

	public static final SnmpObjId[] stpport_elemList = new SnmpObjId[] {
		/**
	     * SYNTAX      Integer32 (1..65535)
         * MAX-ACCESS  read-only
         * STATUS      current
         * DESCRIPTION
         *  "The port number of the port for which this entry
         *  contains Spanning Tree Protocol management information."
         * REFERENCE
         *  "IEEE 802.1D-1998: clause 14.8.2.1.2"		 
         *  
         */
		DOT1D_STP_PORT,
		DOT1D_STP_PORT_PRIORITY,
				
		/**
		 *  dot1dStpPortState OBJECT-TYPE
         * SYNTAX      INTEGER {
         *              disabled(1),
         *              blocking(2),
         *              listening(3),
         *              learning(4),
         *              forwarding(5),
         *              broken(6)
         *          }
         * MAX-ACCESS  read-only
         * STATUS      current
         * DESCRIPTION
         * "The port's current state, as defined by application of
         *  the Spanning Tree Protocol.  This state controls what
         *  action a port takes on reception of a frame.  If the
         *  bridge has detected a port that is malfunctioning, it
         *  will place that port into the broken(6) state.  For
         *  ports that are disabled (see dot1dStpPortEnable), this
         *  object will have a value of disabled(1)."
         * REFERENCE
         *  "IEEE 802.1D-1998: clause 8.5.5.2"
         */
		DOT1D_STP_PORT_STATE,
		
		/**
		 *    dot1dStpPortEnable OBJECT-TYPE
         * SYNTAX      INTEGER {
         *              enabled(1),
         *              disabled(2)
         *          }
         * MAX-ACCESS  read-write
         * STATUS      current
         * DESCRIPTION
         *  "The enabled/disabled status of the port."
         * REFERENCE
         *  "IEEE 802.1D-1998: clause 8.5.5.2"
		 */
		DOT1D_STP_PORT_ENABLE,
		DOT1D_STP_PORT_PATH_COST,
		DOT1D_STP_PORT_DESIGNATED_ROOT,
		DOT1D_STP_PORT_DESIGNATED_COST,
		/**
		 *    dot1dStpPortDesignatedBridge OBJECT-TYPE
         * SYNTAX      BridgeId
         * MAX-ACCESS  read-only
         * STATUS      current
         * DESCRIPTION
         *  "The Bridge Identifier of the bridge that this
         *  port considers to be the Designated Bridge for
         *  this port's segment."
         * REFERENCE
         *  "IEEE 802.1D-1998: clause 8.5.5.6"
		 */
		DOT1D_STP_PORT_DESIGNATED_BRIDGE,
		
		/**
		 * SYNTAX      OCTET STRING (SIZE (2))
         * MAX-ACCESS  read-only
         * STATUS      current
		 * The Port Identifier of the port on the Designated
		 * Bridge for this port's segment.
		 *  REFERENCE
         *  "IEEE 802.1D-1998: clause 8.5.5.7"
		 */
		DOT1D_STP_PORT_DESIGNATED_PORT
	};

	public static class Dot1dStpPortRow extends SnmpRowResult {

		public Dot1dStpPortRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
		}

		/**
		 * <p>getDot1dStpPort</p>
		 *
		 * @return a int.
		 */
		public Integer getDot1dStpPort() {
			return getValue(DOT1D_STP_PORT).toInt();
		}
			

		public Integer getDot1dStpPortPriority() {
			return getValue(DOT1D_STP_PORT_PRIORITY).toInt();
		}
		/**
		 * <p>getDot1dStpPortState</p>
		 *
		 * @return a int.
		 */
		public Integer getDot1dStpPortState() {
			return  getValue(DOT1D_STP_PORT_STATE).toInt();
		}
		
		/**
		 * <p>getDot1dStpPortEnable</p>
		 *
		 * @return a int.
		 */
		public Integer getDot1dStpPortEnable() {
			return getValue(DOT1D_STP_PORT_ENABLE).toInt();
		}
		
		public Integer getDot1dStpPortPathCost() {
			return getValue(DOT1D_STP_PORT_PATH_COST).toInt();
		}
		
		public String getDot1dStpPortDesignatedRoot() {
			return getValue(DOT1D_STP_PORT_DESIGNATED_ROOT).toHexString();
		}

		public Integer getDot1dStpPortDesignatedCost() {
			return getValue(DOT1D_STP_PORT_DESIGNATED_COST).toInt();
		}
		/**
		 * <p>getDot1dStpPortDesignatedBridge</p>
		 *BridgeId ::= TEXTUAL-CONVENTION
         * STATUS      current
         * DESCRIPTION
         *  "The Bridge-Identifier, as used in the Spanning Tree
         *  Protocol, to uniquely identify a bridge.  Its first two
         *  octets (in network byte order) contain a priority value,
         *  and its last 6 octets contain the MAC address used to
         *  refer to a bridge in a unique fashion (typically, the
         *  numerically smallest MAC address of all ports on the
         *  bridge)."
         * SYNTAX      OCTET STRING (SIZE (8))
         *
		 * @return a {@link java.lang.String} object.
		 */
		public String getDot1dStpPortDesignatedBridge() {
			return getValue(DOT1D_STP_PORT_DESIGNATED_BRIDGE).toHexString();
		}
	
		/**
		 * <p>getDot1dStpPortDesignatedPort</p>
		 *
		 * @return a {@link java.lang.String} object.
		 */
		public String getDot1dStpPortDesignatedPort() {
			return getValue(DOT1D_STP_PORT_DESIGNATED_PORT).toHexString();
			
		}

		public BridgeStpLink getLink() {
			BridgeStpLink link = new BridgeStpLink();
            link.setStpPort(getDot1dStpPort());
            link.setStpPortPriority(getDot1dStpPortPriority());
            link.setStpPortState(BridgeDot1dStpPortState.get(getDot1dStpPortState()));
            link.setStpPortEnable(BridgeDot1dStpPortEnable.get(getDot1dStpPortEnable()));
            link.setStpPortPathCost(getDot1dStpPortPathCost());
            link.setDesignatedRoot(getDot1dStpPortDesignatedRoot());
            link.setDesignatedCost(getDot1dStpPortDesignatedCost());
            link.setDesignatedBridge(getDot1dStpPortDesignatedBridge());
            link.setDesignatedPort(getDot1dStpPortDesignatedPort());
    		return link;

		}

	}
	
	public Dot1dStpPortTableTracker() {
		super(stpport_elemList);
	}

	public Dot1dStpPortTableTracker(RowCallback rowProcessor) {
		super(rowProcessor, stpport_elemList);
	}

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new Dot1dStpPortRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processDot1dStpPortRow((Dot1dStpPortRow)row);
    }

    /**
     * <p>processDot1dStpPortRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.Dot1dStpPortTableTracker.Dot1dStpPortRow} object.
     */
    public void processDot1dStpPortRow(final Dot1dStpPortRow row) {
    }
	


}
