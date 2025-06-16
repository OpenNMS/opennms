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

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.netmgt.enlinkd.model.OspfIf;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OspfIfTableTracker extends TableTracker {

    private final static Logger LOG = LoggerFactory.getLogger(OspfIfTableTracker.class);


    public final static SnmpObjId OSPF_IF_IPADDRESS_OID = SnmpObjId.get(".1.3.6.1.2.1.14.7.1.1");
    public final static SnmpObjId OSPF_ADDRESS_LESS_IF_OID = SnmpObjId.get(".1.3.6.1.2.1.14.7.1.2");
    public final static SnmpObjId OSPF_IF_AREA_ID_OID = SnmpObjId.get(".1.3.6.1.2.1.14.7.1.3");

    public final static String OSPF_IF_IPADDRESS = "ospfIfIpAddress";
    public final static String OSPF_ADDRESS_LESS_IF = "ospfAddressLessIf";
    public final static String OSPF_IF_AREA_ID = "ospfIfAreaId";

    public static final SnmpObjId[] s_ospfiftable_elemList = new SnmpObjId[]{

            /*
             *  "The IP address of this OSPF interface."
            */
            OSPF_IF_IPADDRESS_OID,

            /*
             * "For the purpose of easing  the  instancing  of
             * addressed   and  addressless  interfaces;  This
             * variable takes the value 0 on  interfaces  with
             * IP  Addresses,  and  the corresponding value of
             * ifIndex for interfaces having no IP Address."
             *
            */
            OSPF_ADDRESS_LESS_IF_OID,
            /*
             * A 32-bit integer uniquely identifying the area
             * to which the interface connects.  Area ID
             * 0.0.0.0 is used for the OSPF backbone.
            */
            OSPF_IF_AREA_ID_OID

    };

    public static class OspfIfRow extends SnmpRowResult {

        public OspfIfRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
            LOG.debug("column count = {}, instance = {}", columnCount, instance);
        }

        public InetAddress getOspfIpAddress() {
            return getValue(OSPF_IF_IPADDRESS_OID).toInetAddress();
        }

        public Integer getOspfAddressLessIf() {
            return getValue(OSPF_ADDRESS_LESS_IF_OID).toInt();
        }

        public InetAddress getOspfIfAreaId() {
            return getValue(OSPF_IF_AREA_ID_OID).toInetAddress();
        }


        public OspfIf getOspfIf() {

            LOG.debug("getOspfIf: ospf ip address: {}, address less ifindex {}, area id {}",
                    str(getOspfIpAddress()),
                    getOspfAddressLessIf(),
                    str(getOspfIfAreaId()));

            OspfIf ospfIf = new OspfIf();
            ospfIf.setOspfIfIpaddress(getOspfIpAddress());
            ospfIf.setOspfIfAddressLessIf(getOspfAddressLessIf());
            ospfIf.setOspfIfAreaId(getOspfIfAreaId());
            return ospfIf;

        }

    }


    public OspfIfTableTracker() {
        super(s_ospfiftable_elemList);
    }

    public OspfIfTableTracker(final RowCallback rowProcessor) {
        super(rowProcessor, s_ospfiftable_elemList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new OspfIfRow(columnCount, instance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processOspfIfRow((OspfIfRow) row);
    }

    /**
     * <p>processOspfIfRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.OspfIfTableTracker.OspfIfRow} object.
     */
    public void processOspfIfRow(final OspfIfRow row) {
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_IF_IPADDRESS_OID + "." + row.getInstance().toString(), OSPF_IF_IPADDRESS, str(row.getOspfIpAddress()));
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_ADDRESS_LESS_IF_OID + "." + row.getInstance().toString(), OSPF_ADDRESS_LESS_IF, row.getOspfAddressLessIf());
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_IF_AREA_ID_OID + "." + row.getInstance().toString(), OSPF_IF_AREA_ID, str(row.getOspfIfAreaId()));
    }


}
