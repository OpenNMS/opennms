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

import org.opennms.netmgt.enlinkd.model.OspfArea;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.TableTracker;


public class OspfAreaTableTracker extends TableTracker {

    //public static final SnmpObjId OSPF_AREA_ENTRY = SnmpObjId.get(".1.3.6.1.2.1.14.2.1");

    public final static SnmpObjId OSPF_AREA_ID_OID = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.1");
    public final static SnmpObjId OSPF_AUTH_TYPE_OID = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.2");
    public final static SnmpObjId OSPF_IMPORT_AS_EXTERN_OID = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.3");
    //public final static SnmpObjId OSPF_SPF_RUNS       = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.4");
    public final static SnmpObjId OSPF_AREA_BDR_RTR_COUNT_OID = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.5");
    public final static SnmpObjId OSPF_AS_BDR_RTR_COUNT_OID = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.6");
    public final static SnmpObjId OSPF_AREA_LSA_COUNT_OID = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.7");
    //    public final static SnmpObjId OSPF_AREA_LSA_CKSUM_SUM                       = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.8");
    //    public final static SnmpObjId OSPF_AREA_SUMMARY                             = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.9");
    //    public final static SnmpObjId OSPF_AREA_STATUS                              = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.10");
    //    public final static SnmpObjId OSPF_AREA_NSSA_TRANSLATOR_ROLE                = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.11");
    //    public final static SnmpObjId OSPF_AREA_NSSA_TRANSLATOR_STATE               = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.12");
    //    public final static SnmpObjId OSPF_AREA_NSSA_TRANSLATOR_STABILITY_INTERVAL  = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.13");
    //    public final static SnmpObjId OSPF_AREA_NSSA_TRANSLATOR_EVENTS              = SnmpObjId.get(".1.3.6.1.2.1.14.2.1.14");

    public final static String OSPF_AREA_ID = "ospfAreaId";
    public final static String OSPF_AUTH_TYPE = "ospfAuthType";
    public final static String OSPF_IMPORT_AS_EXTERN = "ospfImportAsExtern";
    public final static String OSPF_AREA_BDR_RTR_COUNT = "ospfAreaBdrRtrCount";
    public final static String OSPF_AS_BDR_RTR_COUNT = "ospfAsBdrRtrCount";
    public final static String OSPF_AREA_LSA_COUNT = "ospfAreaLsaCount";

    public static final SnmpObjId[] s_ospfAreatable_elemList = new SnmpObjId[]{
            /*
             * A 32-bit integer uniquely identifying an area.
             *           Area ID 0.0.0.0 is used for the OSPF backbone.
            */
            OSPF_AREA_ID_OID,
            /*
             * The authentication type specified for an area.
            */
            OSPF_AUTH_TYPE_OID,
            /*
             * Indicates if an area is a stub area, NSSA, or standard
            area.  Type-5 AS-external LSAs and type-11 Opaque LSAs are
            not imported into stub areas or NSSAs.  NSSAs import
            AS-external data as type-7 LSAs
            */
            OSPF_IMPORT_AS_EXTERN_OID,
            /*
             * The total number of Area Border Routers reachable
             * within this area.  This is initially zero and is
             * calculated in each Shortest Path First (SPF) pass.
            */
            OSPF_AREA_BDR_RTR_COUNT_OID,
            /*
             * The total number of Autonomous System Border
             * Routers reachable within this area.  This is
             * initially zero and is calculated in each SPF
             * pass.
            */
            OSPF_AS_BDR_RTR_COUNT_OID,
            /* The total number of link state advertisements
             * in this area's link state database, excluding
             * AS-external LSAs.
            */
            OSPF_AREA_LSA_COUNT_OID
    };

    public static class OspfAreaRow extends SnmpRowResult {

        public OspfAreaRow(int columnCount, SnmpInstId instance) {
            super(columnCount, instance);
        }

        public InetAddress getOspfAreaId() {
            return getValue(OSPF_AREA_ID_OID).toInetAddress();
        }

        public Integer getOspfAuthType() {
            return getValue(OSPF_AUTH_TYPE_OID).toInt();
        }

        public Integer getOspfImportAsExtern() {
            return getValue(OSPF_IMPORT_AS_EXTERN_OID).isNull() ? null : getValue(OSPF_IMPORT_AS_EXTERN_OID).toInt();
        }

        public Integer getOspfAreaBdrRtrCount() {
            return getValue(OSPF_AREA_BDR_RTR_COUNT_OID).toInt();
        }

        public Integer getOspfAsBdrRtrCount() {
            return getValue(OSPF_AS_BDR_RTR_COUNT_OID).toInt();
        }

        public Integer getOspfAreaLsaCount() {
            return getValue(OSPF_AREA_LSA_COUNT_OID).toInt();
        }

        public OspfArea getOspfArea() {
            final OspfArea area = new OspfArea();
            area.setOspfAreaId(getOspfAreaId());
            area.setOspfAuthType(getOspfAuthType());
            area.setOspfImportAsExtern(getOspfImportAsExtern());
            area.setOspfAreaBdrRtrCount(getOspfAreaBdrRtrCount());
            area.setOspfAsBdrRtrCount(getOspfAsBdrRtrCount());
            area.setOspfAreaLsaCount(getOspfAreaLsaCount());

            return area;
        }
    }

    public OspfAreaTableTracker() {
        super(s_ospfAreatable_elemList);
    }

    public OspfAreaTableTracker(RowCallback rowProcessor) {
        super(rowProcessor, s_ospfAreatable_elemList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new OspfAreaTableTracker.OspfAreaRow(columnCount, instance);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processOspfAreaRow((OspfAreaTableTracker.OspfAreaRow) row);
    }

    /**
     * <p>processOspfIfRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.OspfAreaTableTracker.OspfAreaRow} object.
     */
    public void processOspfAreaRow(final OspfAreaTableTracker.OspfAreaRow row) {
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_AREA_ID_OID + "." + row.getInstance().toString(), OSPF_AREA_ID, str(row.getOspfAreaId()));
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_AUTH_TYPE_OID + "." + row.getInstance().toString(), OSPF_AUTH_TYPE, row.getOspfAuthType());
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_IMPORT_AS_EXTERN_OID + "." + row.getInstance().toString(), OSPF_IMPORT_AS_EXTERN, row.getOspfImportAsExtern());
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_AREA_BDR_RTR_COUNT_OID + "." + row.getInstance().toString(), OSPF_AREA_BDR_RTR_COUNT, row.getOspfAreaBdrRtrCount());
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_AS_BDR_RTR_COUNT_OID + "." + row.getInstance().toString(), OSPF_AS_BDR_RTR_COUNT, row.getOspfAsBdrRtrCount());
        System.out.printf("\t\t%s (%s)= %s \n", OSPF_AREA_LSA_COUNT_OID + "." + row.getInstance().toString(), OSPF_AREA_LSA_COUNT, row.getOspfAreaLsaCount());
    }
}
