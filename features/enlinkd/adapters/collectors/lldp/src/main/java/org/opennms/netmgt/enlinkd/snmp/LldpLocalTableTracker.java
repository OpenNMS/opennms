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


import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.TableTracker;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpInstId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

public class LldpLocalTableTracker extends TableTracker {
    private final static Logger LOG = LoggerFactory.getLogger(LldpLocalTableTracker.class);


    public static final SnmpObjId[] s_lldploctable_elemList = new SnmpObjId[] {

            /*
               "The type of port identifier encoding used in the associated 'lldpLocPortId' object."
             */
        LldpLocPortGetter.LLDP_LOC_PORTID_SUBTYPE_OID,

            /*
              "The string value used to identify the port component associated with a given port in the local system."
             */
        LldpLocPortGetter.LLDP_LOC_PORTID_OID,

            /*
              "The string value used to identify the 802
              LAN station's port description associated with the local system.
              If the local agent supports IETF RFC 2863,
              lldpLocPortDesc object should have the same value of ifDescr object."
             */
        LldpLocPortGetter.LLDP_LOC_DESCR_OID
    };

    public static class LldpLocalPortRow extends SnmpRowResult {

		public LldpLocalPortRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
		}

	    public Integer getMtxrIndex() {
	    	return getInstance().getSubIdAt(0);
	    }

	    public LldpUtils.LldpPortIdSubType getLldpLocalPortIdSubtype() {
	    	return LldpUtils.LldpPortIdSubType.get(getValue(LldpLocPortGetter.LLDP_LOC_PORTID_SUBTYPE_OID).toInt());
	    }

	    public String getLldpLocPortId() {
	    	return LldpRemTableTracker.decodeLldpPortId(getLldpLocalPortIdSubtype().getValue(),getValue(LldpLocPortGetter.LLDP_LOC_PORTID_OID));
	    }

	    public String getLldpLocPortDesc() {
	    	if (getValue(LldpLocPortGetter.LLDP_LOC_DESCR_OID) != null) {
                return getValue(LldpLocPortGetter.LLDP_LOC_DESCR_OID).toDisplayString();
            }	
	    	return "";
	    }
    }

    public LldpLocalTableTracker() {
        super(s_lldploctable_elemList);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new LldpLocalPortRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processLldpLocPortRow((LldpLocalPortRow)row);
    }

    /**
     * <p>processLldpLocPortRow</p>
     *
     * @param row a {@link LldpLocalTableTracker.LldpLocalPortRow} object.
     */
    public void processLldpLocPortRow(final LldpLocalPortRow row) {
        System.out.printf("\t\t%s (%s)= %s (%s)\n", LldpLocPortGetter.LLDP_LOC_PORTID_SUBTYPE_OID + "." + row.getInstance().toString(), LldpLocPortGetter.LLDP_LOC_PORTID_SUBTYPE, row.getLldpLocalPortIdSubtype(), LldpUtils.LldpPortIdSubType.getTypeString(row.getLldpLocalPortIdSubtype().getValue()));
        System.out.printf("\t\t%s (%s)= %s \n", LldpLocPortGetter.LLDP_LOC_PORTID_OID + "." + row.getInstance().toString(), LldpLocPortGetter.LLDP_LOC_PORTID, row.getLldpLocPortId());
        System.out.printf("\t\t%s (%s)= %s \n", LldpLocPortGetter.LLDP_LOC_DESCR_OID + "." + row.getInstance().toString(), LldpLocPortGetter.LLDP_LOC_DESCR, row.getLldpLocPortDesc());
    }

    public static LldpElement getLldpElement(String sysname, Collection<LldpLocalPortRow> rows) {
        LldpElement element = new LldpElement();
        element.setLldpSysname(sysname);
        for (LldpLocalPortRow row: rows) {
            if (row.getLldpLocalPortIdSubtype().equals(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_MACADDRESS)) {
                LOG.debug("getLldpElement: parsing lldp_chassis_id {}", row.getLldpLocPortId());
                if (element.getLldpChassisId() == null || element.getLldpChassisId().compareTo(row.getLldpLocPortId()) > 0 ) {
                    LOG.debug("getLldpElement: set lldp_chassis_id {}", row.getLldpLocPortId());
                    element.setLldpChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS);
                    element.setLldpChassisId(row.getLldpLocPortId());
                }
            }
        }
        if (element.getLldpChassisId() == null) {
            element.setLldpChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_LOCAL);
            element.setLldpChassisId(sysname);
        }
        LOG.info("getLldpElement: {}", element);
        return element;
    }

    public static  LldpLink getLldpLink(MtxrLldpRemTableTracker.MtxrLldpRemRow mtxrlldprow, Integer mtxrIndex, Map<Integer, LldpLocalPortRow> mtxrLldpLocalPortMap) {
        LldpLink lldpLink = mtxrlldprow.getLldpLink();
        if (mtxrIndex != null && mtxrLldpLocalPortMap.containsKey(mtxrIndex)) {
            lldpLink.setLldpPortIfindex(mtxrIndex);
            lldpLink.setLldpPortIdSubType(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME);
            lldpLink.setLldpPortId(mtxrLldpLocalPortMap.get(mtxrIndex).getLldpLocPortDesc());
            lldpLink.setLldpPortDescr(mtxrLldpLocalPortMap.get(mtxrIndex).getLldpLocPortDesc());
            LOG.debug("getLldpLink: interfaceId {} -> portId {}", mtxrIndex,lldpLink.getLldpPortId());
        } else {
            lldpLink.setLldpPortIdSubType(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACEALIAS);
            lldpLink.setLldpPortId("\"Not Found On lldpLocPortTable\"");
            lldpLink.setLldpPortDescr("");
            LOG.debug("getLldpLink: setting default not found Values: portidtype \"InterfaceAlias\", portid=\"Not Found On lldpLocPortTable\"");
        }
        return lldpLink;
    }

}
