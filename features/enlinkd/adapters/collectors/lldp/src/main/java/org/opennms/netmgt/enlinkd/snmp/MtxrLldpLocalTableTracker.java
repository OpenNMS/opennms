/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
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


import org.opennms.core.utils.LldpUtils;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.TableTracker;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.RowCallback;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MtxrLldpLocalTableTracker extends TableTracker {
    private final static Logger LOG = LoggerFactory.getLogger(MtxrLldpLocalTableTracker.class);


    public static final SnmpObjId[] s_lldploctable_elemList = new SnmpObjId[] {

            /*
               "The type of port identifier encoding used in the associated 'lldpLocPortId' object."
             */
        LldpLocPortGetter.LLDP_LOC_PORTID_SUBTYPE,

            /*
              "The string value used to identify the port component associated with a given port in the local system."
             */
        LldpLocPortGetter.LLDP_LOC_PORTID,

            /*
              "The string value used to identify the 802
              LAN station's port description associated with the local system.
              If the local agent supports IETF RFC 2863,
              lldpLocPortDesc object should have the same value of ifDescr object."
             */
        LldpLocPortGetter.LLDP_LOC_DESCR
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
	    	return LldpUtils.LldpPortIdSubType.get(getValue(LldpLocPortGetter.LLDP_LOC_PORTID_SUBTYPE).toInt());
	    }

	    public String getLldpLocPortId() {
	    	return LldpRemTableTracker.decodeLldpPortId(getLldpLocalPortIdSubtype().getValue(),getValue(LldpLocPortGetter.LLDP_LOC_PORTID));
	    }

	    public String getLldpLocPortDesc() {
	    	if (getValue(LldpLocPortGetter.LLDP_LOC_DESCR) != null) {
                return getValue(LldpLocPortGetter.LLDP_LOC_DESCR).toDisplayString();
            }	
	    	return "";
	    }
    }

    public Map<Integer, LldpLocalPortRow> getMtxrLldpLocalPortMap() {
        return mtxrLldpLocalPortMap;
    }

    private final Map<Integer, LldpLocalPortRow> mtxrLldpLocalPortMap = new HashMap<>();

    public MtxrLldpLocalTableTracker() {
        super(s_lldploctable_elemList);
    }

    /**
     * <p>Constructor for MikrotikLldpLocalTableTracker.</p>
     *
     * @param rowProcessor a {@link RowCallback} object.
     */
    public MtxrLldpLocalTableTracker(final RowCallback rowProcessor) {
        super(rowProcessor, s_lldploctable_elemList);
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
     * <p>processLldpRemRow</p>
     *
     * @param row a {@link MtxrLldpLocalTableTracker.LldpLocalPortRow} object.
     */
    public void processLldpLocPortRow(final LldpLocalPortRow row) {
        LOG.debug("processLldpLocPortRow: mtxrIndex {} -> {} {} {}", row.getMtxrIndex(), row.getLldpLocalPortIdSubtype(), row.getLldpLocPortId(), row.getLldpLocPortDesc());
        mtxrLldpLocalPortMap.put(row.getMtxrIndex(),row);
    }

    public LldpElement getLldpElement(String sysname) {
        LldpElement element = new LldpElement();
        element.setLldpSysname(sysname);
        for (LldpLocalPortRow row: mtxrLldpLocalPortMap.values()) {
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

    public LldpLink getLldpLink(MtxrLldpRemTableTracker.MtxrLldpRemRow mtxrlldprow, Integer mtxrIndex) {
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
