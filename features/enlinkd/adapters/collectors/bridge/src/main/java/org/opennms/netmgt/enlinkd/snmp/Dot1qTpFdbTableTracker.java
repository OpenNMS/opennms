/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd.snmp;

import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry;
import org.opennms.netmgt.enlinkd.service.api.BridgeForwardingTableEntry.BridgeDot1qTpFdbStatus;
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
 * @see <A HREF="http://www.ietf.org/rfc/rfc1213.txt">RFC1213</A>
 * @version $Id: $
 */
public class Dot1qTpFdbTableTracker extends TableTracker {

    public final static SnmpObjId DOT1Q_TP_FDB_ADDRESS_OID = SnmpObjId.get(".1.3.6.1.2.1.17.7.1.2.2.1.1");
    public final static SnmpObjId DOT1Q_TP_FDB_PORT_OID = SnmpObjId.get(".1.3.6.1.2.1.17.7.1.2.2.1.2");
    public final static SnmpObjId DOT1Q_TP_FDB_STATUS_OID = SnmpObjId.get(".1.3.6.1.2.1.17.7.1.2.2.1.3");

    /*
     *    dot1qTpFdbAddress OBJECT-TYPE
     * SYNTAX MacAddress
     * ACCESS not-accessible
     * STATUS mandatory
     * DESCRIPTION "A unicast MAC address for which the device has forwarding and/or filtering information."
     */
    public final static String DOT1Q_TP_FDB_ADDRESS = "dot1qTpFdbAddress";
    public final static String DOT1Q_TP_FDB_PORT = "dot1qTpFdbPort";
    public final static String DOT1Q_TP_FDB_STATUS = "dot1qTpFdbStatus";
    public static SnmpObjId[] ms_elemList = new SnmpObjId[] {
    /*
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

            DOT1Q_TP_FDB_PORT_OID,

    /*
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
            DOT1Q_TP_FDB_STATUS_OID};

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
            if (getValue(DOT1Q_TP_FDB_PORT_OID) != null)
                return getValue(DOT1Q_TP_FDB_PORT_OID).toInt();
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
            if (getValue(DOT1Q_TP_FDB_STATUS_OID) != null)
                return getValue(DOT1Q_TP_FDB_STATUS_OID).toInt();
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
        System.out.printf("\t\t%s (%s)= %s (this is extracted from table index)\n", DOT1Q_TP_FDB_ADDRESS_OID + "." + row.getInstance().toString(), DOT1Q_TP_FDB_ADDRESS, row.getDot1qTpFdbAddress());
        System.out.printf("\t\t%s (%s)= %s \n", DOT1Q_TP_FDB_PORT_OID + "." + row.getInstance().toString(), DOT1Q_TP_FDB_PORT, row.getDot1qTpFdbPort() );
        System.out.printf("\t\t%s (%s)= %s (%s)\n", DOT1Q_TP_FDB_STATUS_OID + "." + row.getInstance().toString(), DOT1Q_TP_FDB_STATUS, row.getDot1qTpFdbStatus(), BridgeDot1qTpFdbStatus.get(row.getDot1qTpFdbStatus())  );

    }

}
