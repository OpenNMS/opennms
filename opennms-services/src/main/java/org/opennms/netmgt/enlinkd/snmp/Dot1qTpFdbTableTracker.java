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

import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
import org.opennms.netmgt.model.topology.BridgeForwardingTableEntry;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;

/**
 * <P>
 * The Dot1qTpFdbTableTracker class is designed to hold all the MIB-II
 * information for one entry in the MIB II dot1qTpFdbTable. The table
 * effectively contains a list of these entries, each entry having information
 * about bridge forwarding table.
 * </P>
 * <P>
 * This object is used by the Dot1qTpFdbTable to hold information single
 * entries in the table. See the Dot1qTpFdbTable documentation form more
 * information.
 * </P>
 *
 * @author <A HREF="mailto:rssntn67@yahoo.it">Antonio</A>
 * @see Dot1dTpFdbTable
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public class Dot1qTpFdbTableTracker extends TableTracker {

    public final static SnmpObjId DOT1Q_TP_FDB_PORT = SnmpObjId.get(".1.3.6.1.2.1.17.7.1.2.2.1.2");
    public final static SnmpObjId DOT1Q_TP_FDB_STATUS = SnmpObjId.get(".1.3.6.1.2.1.17.7.1.2.2.1.3");

    public static SnmpObjId[] ms_elemList = new SnmpObjId[] {
    /**
     * dot1qTpFdbPort OBJECT-TYPE SYNTAX Integer32 (0..65535) MAX-ACCESS
     * read-only STATUS current DESCRIPTION "Either the value '0', or the port
     * number of the port on which a frame having a source address equal to
     * the value of the corresponding instance of dot1qTpFdbAddress has been
     * seen. A value of '0' indicates that the port number has not been
     * learned but that the device does have some forwarding/filtering
     * information about this address (e.g., in the dot1qStaticUnicastTable).
     * Implementors are encouraged to assign the port value to this object
     * whenever it is learned, even for addresses for which the corresponding
     * value of dot1qTpFdbStatus is not learned(3)."
     */

    DOT1Q_TP_FDB_PORT,

    /**
     * dot1qTpFdbStatus OBJECT-TYPE SYNTAX INTEGER { other(1), invalid(2),
     * learned(3), self(4), mgmt(5) } MAX-ACCESS read-only STATUS current
     * DESCRIPTION "The status of this entry. The meanings of the values are:
     * other(1) - none of the following. This may include the case where some
     * other MIB object (not the corresponding instance of dot1qTpFdbPort, nor
     * an entry in the dot1qStaticUnicastTable) is being used to determine if
     * and how frames addressed to the value of the corresponding instance of
     * dot1qTpFdbAddress are being forwarded. invalid(2) - this entry is no
     * longer valid (e.g., it was learned but has since aged out), but has not
     * yet been flushed from the table. learned(3) - the value of the
     * corresponding instance of dot1qTpFdbPort was learned and is being used.
     * self(4) - the value of the corresponding instance of dot1qTpFdbAddress
     * represents one of the device's addresses. The corresponding instance of
     * dot1qTpFdbPort indicates which of the device's ports has this address.
     * mgmt(5) - the value of the corresponding instance of dot1qTpFdbAddress
     * is also the value of an existing instance of dot1qStaticAddress."
     */
    DOT1Q_TP_FDB_STATUS };

    public static class Dot1qTpFdbRow extends SnmpRowResult {

        public Dot1qTpFdbRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }

        /**
         * <p>
         * getDot1dTpFdbAddress
         * </p>
         *
         * @return a {@link java.lang.String} object.
         */
        public String getDot1qTpFdbAddress() {
            int[] identifiers = getInstance().getIds();
            if (identifiers.length != 7) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (int i = identifiers.length - 6; i < identifiers.length; i++) {
                if (identifiers[i] >= 16) {
                    sb.append(Integer.toHexString(identifiers[i]));
                } else {
                    sb.append("0").append(Integer.toHexString(identifiers[i]));
                }
            }
            return sb.toString();
        }

        /**
         * <p>
         * getDot1qTpFdbPort
         * </p>
         *
         * @return a int.
         */
        public Integer getDot1qTpFdbPort() {
            if (getValue(DOT1Q_TP_FDB_PORT) != null)
                return getValue(DOT1Q_TP_FDB_PORT).toInt();
            return null;
        }

        /**
         * <p>
         * getDot1qTpFdbStatus
         * </p>
         *
         * @return a int.
         */
        public Integer getDot1qTpFdbStatus() {
            if (getValue(DOT1Q_TP_FDB_STATUS) != null)
                return getValue(DOT1Q_TP_FDB_STATUS).toInt();
            return null;
        }

        public BridgeForwardingTableEntry getLink() {
            BridgeForwardingTableEntry link = new BridgeForwardingTableEntry();
            link.setBridgePort(getDot1qTpFdbPort());
            link.setMacAddress(getDot1qTpFdbAddress());
            if (getDot1qTpFdbStatus() != null) {
                link.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.get(getDot1qTpFdbStatus()));
            }
            return link;
        }
    }

    /**
     * <p>
     * Constructor for Dot1qTpFdbTableEntry.
     * </p>
     */
    public Dot1qTpFdbTableTracker() {
        super(ms_elemList);
    }

    public Dot1qTpFdbTableTracker(RowCallback rowProcessor) {
        super(rowProcessor, ms_elemList);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount,
            final SnmpInstId instance) {
        return new Dot1qTpFdbRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processDot1qTpFdbRow((Dot1qTpFdbRow) row);
    }

    /**
     * <p>
     * processIpNetToMediaRow
     * </p>
     *
     * @param row
     *            a
     *            {@link org.opennms.netmgt.enlinkd.snmp.Dot1qTpFdbTableTracker.Dot1qTpFdbRow}
     *            object.
     */
    public void processDot1qTpFdbRow(final Dot1qTpFdbRow row) {
    }

}
