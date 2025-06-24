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

    public final static String TIMETETRA_LLDP_REM_CHASSIS_ID_SUBTYPE = "tmnxLldpRemChassisIdSubtype";
    public final static String TIMETETRA_LLDP_REM_CHASSIS_ID = "tmnxLldpRemChassisId";
    public final static String TIMETETRA_LLDP_REM_PORT_ID_SUBTYPE = "tmnxLldpRemPortIdSubtype";
    public final static String TIMETETRA_LLDP_REM_PORT_ID = "tmnxLldpRemPortId";
    public final static String TIMETETRA_LLDP_REM_PORT_DESCR = "tmnxLldpRemPortDesc";
    public final static String TIMETETRA_LLDP_REM_SYSNAME = "tmnxLldpRemSysName";


    public final static SnmpObjId TIMETETRA_LLDP_REM_CHASSIS_ID_SUBTYPE_OID = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"4");
    public final static SnmpObjId TIMETETRA_LLDP_REM_CHASSIS_ID_OID = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"5");
    public final static SnmpObjId TIMETETRA_LLDP_REM_PORT_ID_SUBTYPE_OID = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"6");
    public final static SnmpObjId TIMETETRA_LLDP_REM_PORT_ID_OID = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"7");
    public final static SnmpObjId TIMETETRA_LLDP_REM_PORT_DESCR_OID = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"8");
    public final static SnmpObjId TIMETETRA_LLDP_REM_SYSNAME_OID = SnmpObjId.get(TIMETETRA_LLDP_REM_TABLE_ENTRY,"9");

    public static final SnmpObjId[] s_timetetralldpremtable_elemList = new SnmpObjId[] {

        /*
         *  "The type of encoding used to identify the chassis associated
         *  with the remote system."
         */
            TIMETETRA_LLDP_REM_CHASSIS_ID_SUBTYPE_OID,

        /*
         * "The string value used to identify the chassis component
         * associated with the remote system."
         */
            TIMETETRA_LLDP_REM_CHASSIS_ID_OID,

        /*
         * "The type of port identifier encoding used in the associated
         * 'lldpRemPortId' object."
         */
            TIMETETRA_LLDP_REM_PORT_ID_SUBTYPE_OID,

        /*
         * "The string value used to identify the port component
            associated with the remote system."
         */
            TIMETETRA_LLDP_REM_PORT_ID_OID,

        /*
         * 	"The string value used to identify the description of
         *  the given port associated with the remote system."
         */
            TIMETETRA_LLDP_REM_PORT_DESCR_OID,

        /*
         * "The string value used to identify the port component
         * associated with the remote system."
         */
            TIMETETRA_LLDP_REM_SYSNAME_OID

    };

    public static class TimeTetraLldpRemRow extends SnmpRowResult {

		public TimeTetraLldpRemRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
		}

		public Integer getLldpLocalPortNum() {return getInstance().toString().hashCode();}
	    public Integer getLldpRemIndex() {
	    	return getInstance().getSubIdAt(3);
	    }
        public Integer getTmnxLldpRemLocalDestMACAddress() {
            return getInstance().getSubIdAt(2);
        }
        public Integer getIfindex() {
            return getInstance().getSubIdAt(1);
        }

	    public Integer getLldpRemChassisidSubtype() {
	    	return getValue(TIMETETRA_LLDP_REM_CHASSIS_ID_SUBTYPE_OID).toInt();
	    }

	    public SnmpValue getLldpRemChassisId() {
	        return getValue(TIMETETRA_LLDP_REM_CHASSIS_ID_OID);
	    }

	    public Integer getLldpRemPortidSubtype() {
	    	return getValue(TIMETETRA_LLDP_REM_PORT_ID_SUBTYPE_OID).toInt();
	    }

	    public String getLldpRemPortid() {
	    	return LldpSnmpUtils.decodeLldpPortId(LldpPortIdSubType.get(getLldpRemPortidSubtype()), getValue(TIMETETRA_LLDP_REM_PORT_ID_OID));
	    }

	    public String getLldpRemPortDescr() {
	    	if (getValue(TIMETETRA_LLDP_REM_PORT_DESCR_OID) != null)
	    		return getValue(TIMETETRA_LLDP_REM_PORT_DESCR_OID).toDisplayString();
	    	return "";
	    }

	    public String getLldpRemSysname() {
	        return getValue(TIMETETRA_LLDP_REM_SYSNAME_OID).toDisplayString();
	    }


	    public LldpLink getLldpLink() {

            LldpLink lldpLink = new LldpLink();
            lldpLink.setLldpRemLocalPortNum(getIfindex()*31+getTmnxLldpRemLocalDestMACAddress());
            lldpLink.setLldpRemIndex(getLldpRemIndex());
            lldpLink.setLldpPortIfindex(getIfindex());
            lldpLink.setLldpRemChassisIdSubType(LldpChassisIdSubType.get(getLldpRemChassisidSubtype()));
            lldpLink.setLldpRemChassisId(LldpSnmpUtils.decodeLldpChassisId(lldpLink.getLldpRemChassisIdSubType(),
                    getLldpRemChassisId()));
            lldpLink.setLldpRemSysname(getLldpRemSysname());
            lldpLink.setLldpRemPortId(getLldpRemPortid());
            lldpLink.setLldpRemPortIdSubType(LldpPortIdSubType.get(getLldpRemPortidSubtype()));
            lldpLink.setLldpRemPortDescr(getLldpRemPortDescr());

            LOG.debug( "getLldpLink: Rem Index: {}, ifindex: {}, TmnxLldpRemLocalDestMACAddress: {}, identifier: {}, chassis subtype: {}, \n rem sysname: {}, rem port: {}, rem port subtype: {}",
                       lldpLink.getLldpRemIndex(),
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
        System.out.printf("\t\t%s (%s)= %s (%s)\n", TIMETETRA_LLDP_REM_CHASSIS_ID_SUBTYPE_OID + "." +
                row.getInstance().toString(), TIMETETRA_LLDP_REM_CHASSIS_ID_SUBTYPE, row.getLldpRemChassisidSubtype(), LldpChassisIdSubType.getTypeString(row.getLldpRemChassisidSubtype()));
        System.out.printf("\t\t%s (%s)= %s \n", TIMETETRA_LLDP_REM_CHASSIS_ID_OID + "." + row.getInstance().toString(), TIMETETRA_LLDP_REM_CHASSIS_ID,
                LldpSnmpUtils.decodeLldpChassisId(LldpChassisIdSubType.get(row.getLldpRemChassisidSubtype()),
                row.getLldpRemChassisId()));
        System.out.printf("\t\t%s (%s)= %s (%s)\n", TIMETETRA_LLDP_REM_PORT_ID_SUBTYPE_OID + "." + row.getInstance().toString(), TIMETETRA_LLDP_REM_PORT_ID_SUBTYPE, row.getLldpRemPortidSubtype(), LldpPortIdSubType.getTypeString(row.getLldpRemPortidSubtype()));
        System.out.printf("\t\t%s (%s)= %s \n", TIMETETRA_LLDP_REM_PORT_ID_OID + "." + row.getInstance().toString(), TIMETETRA_LLDP_REM_PORT_ID, row.getLldpRemPortid());
        System.out.printf("\t\t%s (%s)= %s \n", TIMETETRA_LLDP_REM_PORT_DESCR_OID + "." + row.getInstance().toString(), TIMETETRA_LLDP_REM_PORT_DESCR, row.getLldpRemPortDescr());
        System.out.printf("\t\t%s (%s)= %s \n", TIMETETRA_LLDP_REM_SYSNAME_OID + "." + row.getInstance().toString(), TIMETETRA_LLDP_REM_SYSNAME, row.getLldpRemSysname());

    }

}
