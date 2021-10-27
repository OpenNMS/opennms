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
import org.opennms.netmgt.snmp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MikrotikLldpLocalTableTracker extends TableTracker {
    private final static Logger LOG = LoggerFactory.getLogger(MikrotikLldpLocalTableTracker.class);


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

	    public Integer getLldpLocalPortIdSubtype() {
	    	return getValue(LldpLocPortGetter.LLDP_LOC_PORTID_SUBTYPE).toInt();
	    }

	    public String getLldpLocPortId() {
	    	return LldpRemTableTracker.decodeLldpPortId(getLldpLocalPortIdSubtype(),getValue(LldpLocPortGetter.LLDP_LOC_PORTID));
	    }

	    public String getLldpRemLocalPortDescr() {
	    	if (getValue(LldpLocPortGetter.LLDP_LOC_DESCR) != null)
	    		return getValue(LldpLocPortGetter.LLDP_LOC_DESCR).toDisplayString();
	    	return "";
	    }
    }

    public Map<Integer, LldpLocalPortRow> getMikrotikPortTable() {
        return mikrotikPortTable;
    }

    private final Map<Integer, LldpLocalPortRow> mikrotikPortTable = new HashMap<>();

    public MikrotikLldpLocalTableTracker() {
        super(s_lldploctable_elemList);
    }

    /**
     * <p>Constructor for LldpRemTableTracker.</p>
     *
     * @param rowProcessor a {@link RowCallback} object.
     */
    public MikrotikLldpLocalTableTracker(final RowCallback rowProcessor) {
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
     * @param row a {@link MikrotikLldpLocalTableTracker.LldpLocalPortRow} object.
     */
    public void processLldpLocPortRow(final LldpLocalPortRow row) {
        LOG.debug("processLldpLocPortRow: mtxrIndex {} -> {}", row.getMtxrIndex(),row.getLldpRemLocalPortDescr());
        mikrotikPortTable.put(row.getMtxrIndex(),row);
    }

    //FIXME
    public LldpElement getLldpElement(String sysnname) {
        LldpElement element = new LldpElement();
        element.setLldpSysname(sysnname);
        element.setLldpChassisIdSubType(LldpUtils.LldpChassisIdSubType.LLDP_CHASSISID_SUBTYPE_MACADDRESS);
        element.setLldpChassisId(mikrotikPortTable.get(1).getLldpLocPortId());
        return element;
    }

    public MikrotikLldpLink getLldpLink(MikrotikLldpLink mktlldpLink) {
        if (mikrotikPortTable.containsKey(mktlldpLink.getMtxrIndex())) {
            mktlldpLink.getLldpLink().setLldpPortIfindex(mktlldpLink.getMtxrIndex());
            mktlldpLink.getLldpLink().setLldpPortIdSubType(LldpUtils.LldpPortIdSubType.LLDP_PORTID_SUBTYPE_INTERFACENAME);
            mktlldpLink.getLldpLink().setLldpPortId(mikrotikPortTable.get(mktlldpLink.getMtxrIndex()).getLldpRemLocalPortDescr());
        }
        return mktlldpLink;
    }

}
