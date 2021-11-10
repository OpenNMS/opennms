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


import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.snmp.TableTracker;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.RowCallback;
import org.opennms.netmgt.snmp.SnmpValue;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeTetraLldpRemTableTracker extends TableTracker {
    private final static Logger LOG = LoggerFactory.getLogger(TimeTetraLldpRemTableTracker.class);

    public static final SnmpObjId TIMETETRA_LLDP_REM_TABLE_ENTRY = SnmpObjId.get(".1.3.6.1.4.1.6527.3.1.2.59.4.1.1"); // start of table (GETNEXT)


    public final static SnmpObjId TIMETETRA_LLDP_REM_CHASSIS_ID_SUBTYPE = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"4");
    public final static SnmpObjId TIMETETRA_LLDP_REM_CHASSIS_ID         = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"5");
    public final static SnmpObjId TIMETETRA_LLDP_REM_PORT_ID_SUBTYPE    = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"6");
    public final static SnmpObjId TIMETETRA_LLDP_REM_PORT_ID            = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"7");
    public final static SnmpObjId TIMETETRA_LLDP_REM_PORT_DESCR         = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"8");
    public final static SnmpObjId TIMETETRA_LLDP_REM_SYSNAME            = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"9");

    public static final SnmpObjId[] s_timetetralldpremtable_elemList = new SnmpObjId[] {

        /**
         *  "The type of encoding used to identify the chassis associated
         *  with the remote system."
         */
            TIMETETRA_LLDP_REM_CHASSIS_ID_SUBTYPE,

        /**
         * "The string value used to identify the chassis component
         * associated with the remote system."
         */
            TIMETETRA_LLDP_REM_CHASSIS_ID,

        /**
         * "The type of port identifier encoding used in the associated
         * 'lldpRemPortId' object."
         */
            TIMETETRA_LLDP_REM_PORT_ID_SUBTYPE,

        /**
         * "The string value used to identify the port component
            associated with the remote system."
         */
            TIMETETRA_LLDP_REM_PORT_ID,

        /**
         * 	"The string value used to identify the description of
         *  the given port associated with the remote system."
         */
            TIMETETRA_LLDP_REM_PORT_DESCR,

        /**
         * "The string value used to identify the port component
         * associated with the remote system."
         */
            TIMETETRA_LLDP_REM_SYSNAME

    };

    public static class TimeTetraLldpRemRow extends SnmpRowResult {

		public TimeTetraLldpRemRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
		}

	    public Integer getLldpRemLocalPortNum() {
	    	return getInstance().getSubIdAt(3);
	    }
        public Integer getTmnxLldpRemLocalDestMACAddress() {
            return getInstance().getSubIdAt(2);
        }
        public Integer getIfindex() {
            return getInstance().getSubIdAt(1);
        }

	    public Integer getLldpRemChassisidSubtype() {
	    	return getValue(TIMETETRA_LLDP_REM_CHASSIS_ID_SUBTYPE).toInt();
	    }

	    public SnmpValue getLldpRemChassisId() {
	        return getValue(TIMETETRA_LLDP_REM_CHASSIS_ID);
	    }

	    public Integer getLldpRemPortidSubtype() {
	    	return getValue(TIMETETRA_LLDP_REM_PORT_ID_SUBTYPE).toInt();
	    }

	    public String getLldpRemPortid() {
	    	return LldpRemTableTracker.decodeLldpPortId(getLldpRemPortidSubtype(), getValue(TIMETETRA_LLDP_REM_PORT_ID));
	    }

	    public String getLldpRemPortDescr() {
	    	if (getValue(TIMETETRA_LLDP_REM_PORT_DESCR) != null)
	    		return getValue(TIMETETRA_LLDP_REM_PORT_DESCR).toDisplayString();
	    	return "";
	    }

	    public String getLldpRemSysname() {
	        return getValue(TIMETETRA_LLDP_REM_SYSNAME).toDisplayString();
	    }


	    public LldpLink getLldpLink() {

            LldpLink lldpLink = new LldpLink();
            lldpLink.setLldpLocalPortNum(getLldpRemLocalPortNum());
            lldpLink.setLldpPortIfindex(getIfindex());
            lldpLink.setLldpRemChassisId(LldpLocalGroupTracker.decodeLldpChassisId(getLldpRemChassisId(), getLldpRemChassisidSubtype()));
            lldpLink.setLldpRemChassisIdSubType(LldpChassisIdSubType.get(getLldpRemChassisidSubtype()));
            lldpLink.setLldpRemSysname(getLldpRemSysname());
            lldpLink.setLldpRemPortId(getLldpRemPortid());
            lldpLink.setLldpRemPortIdSubType(LldpPortIdSubType.get(getLldpRemPortidSubtype()));
            lldpLink.setLldpRemPortDescr(getLldpRemPortDescr());
            LOG.debug( "getLldpLink: local port num: {}, ifindex: {}, TmnxLldpRemLocalDestMACAddress: {}, identifier: {}, chassis subtype: {}, \n rem sysname: {}, rem port: {}, rem port subtype: {}",
                       lldpLink.getLldpLocalPortNum(),
                       lldpLink.getLldpPortIfindex(),
                        getTmnxLldpRemLocalDestMACAddress(),
                       lldpLink.getLldpRemChassisId(),
                       LldpChassisIdSubType.getTypeString(getLldpRemChassisidSubtype()),
                       lldpLink.getLldpRemSysname(),
                       lldpLink.getLldpRemPortId(),
                       LldpPortIdSubType.getTypeString(getLldpRemPortidSubtype()));

            return lldpLink;
	    }
    }

    public TimeTetraLldpRemTableTracker() {
        super(s_timetetralldpremtable_elemList);
    }

    /**
     * <p>Constructor for LldpRemTableTracker.</p>
     *
     * @param rowProcessor a {@link RowCallback} object.
     */
    public TimeTetraLldpRemTableTracker(final RowCallback rowProcessor) {
        super(rowProcessor, s_timetetralldpremtable_elemList);
    }
    
    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new TimeTetraLldpRemRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processLldpRemRow((TimeTetraLldpRemRow)row);
    }

    /**
     * <p>processLldpRemRow</p>
     *
     * @param row a {@link TimeTetraLldpRemRow} object.
     */
    public void processLldpRemRow(final TimeTetraLldpRemRow row) {
    }

}
