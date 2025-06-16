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
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.snmp.AbstractSnmpValue;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpRowResult;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.TableTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LldpRemTableTracker extends TableTracker {
    private final static Logger LOG = LoggerFactory.getLogger(LldpRemTableTracker.class);
	
    public static final SnmpObjId LLDP_REM_TABLE_ENTRY = SnmpObjId.get(".1.0.8802.1.1.2.1.4.1.1"); // start of table (GETNEXT)

    public final static String LLDP_REM_CHASSIS_ID_SUBTYPE = "lldpRemChassisIdSubtype";
    public final static String LLDP_REM_CHASSIS_ID = "lldpRemChassisId";
    public final static String LLDP_REM_PORT_ID_SUBTYPE = "lldpRemPortIdSubtype";
    public final static String LLDP_REM_PORT_ID = "lldpRemPortId";
    public final static String LLDP_REM_PORT_DESCR = "lldpRemPortDesc";
    public final static String LLDP_REM_SYSNAME = "lldpRemSysName";

    public final static SnmpObjId LLDP_REM_CHASSIS_ID_SUBTYPE_OID = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"4");
    public final static SnmpObjId LLDP_REM_CHASSIS_ID_OID = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"5");
    public final static SnmpObjId LLDP_REM_PORT_ID_SUBTYPE_OID = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"6");
    public final static SnmpObjId LLDP_REM_PORT_ID_OID = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"7");
    public final static SnmpObjId LLDP_REM_PORT_DESCR_OID  = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"8");
    public final static SnmpObjId LLDP_REM_SYSNAME_OID = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"9");

    public static final SnmpObjId[] s_lldpremtable_elemList = new SnmpObjId[] {
        
        /*
         *  "The type of encoding used to identify the chassis associated
         *  with the remote system."
         */
            LLDP_REM_CHASSIS_ID_SUBTYPE_OID,
        
        /*
         * "The string value used to identify the chassis component
         * associated with the remote system."
         */
            LLDP_REM_CHASSIS_ID_OID,

        /*
         * "The type of port identifier encoding used in the associated
         * 'lldpRemPortId' object."
         */
            LLDP_REM_PORT_ID_SUBTYPE_OID,

        /*
         * "The string value used to identify the port component
            associated with the remote system."
         */
            LLDP_REM_PORT_ID_OID,
        
        /*
         * 	"The string value used to identify the description of 
         *  the given port associated with the remote system."
         */
        LLDP_REM_PORT_DESCR_OID,

        /*
         * "The string value used to identify the port component
         * associated with the remote system."
         */
            LLDP_REM_SYSNAME_OID

    };
    
    public static String decodeLldpPortId(Integer lldpPortIdSubType,SnmpValue lldpportid) {
        if (lldpPortIdSubType == null) {
            if (lldpportid.isDisplayable())
                return lldpportid.toDisplayString();
            else 
                return lldpportid.toHexString();
        }
        try {
            LldpPortIdSubType type=LldpPortIdSubType.get(lldpPortIdSubType);
        /*
         * 
         *       If the associated LldpPortIdSubtype object has a value of
         *       'interfaceAlias(1)', then the octet string identifies a
         *       particular instance of the ifAlias object (defined in IETF
         *       RFC 2863). If the particular ifAlias object does not contain
         *       any values, another port identifier type should be used.
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'portComponent(2)', then the octet string identifies a
         *       particular instance of the entPhysicalAlias object (defined
         *       in IETF RFC 2737) for a port or backplane component.
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'macAddress(3)', then this string identifies a particular
         *       unicast source address (encoded in network byte order
         *       and IEEE 802.3 canonical bit order) associated with the port
         *       (IEEE Std 802-2001).
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'networkAddress(4)', then this string identifies a network
         *       address associated with the port. The first octet contains
         *       the IANA AddressFamilyNumbers enumeration value for the
         *       specific address type, and octets 2 through N contain the
         *       networkAddress address value in network byte order.
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'interfaceName(5)', then the octet string identifies a
         *       particular instance of the ifName object (defined in IETF
         *       RFC 2863). If the particular ifName object does not contain
         *       any values, another port identifier type should be used.
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'agentCircuitId(6)', then this string identifies a agent-local
         *       identifier of the circuit (defined in RFC 3046).
         *
         *       If the associated LldpPortIdSubtype object has a value of
         *       'local(7)', then this string identifies a locally
         *       assigned port ID."
         */
            switch (type) {
            case LLDP_PORTID_SUBTYPE_PORTCOMPONENT:
            case LLDP_PORTID_SUBTYPE_AGENTCIRCUITID:
            case LLDP_PORTID_SUBTYPE_INTERFACEALIAS:
            case LLDP_PORTID_SUBTYPE_INTERFACENAME:
            case LLDP_PORTID_SUBTYPE_LOCAL:
                if (AbstractSnmpValue.allBytesUTF_8(lldpportid.getBytes()))
                    return lldpportid.toDisplayString();
                else 
                    return lldpportid.toHexString();
            case LLDP_PORTID_SUBTYPE_MACADDRESS:
                return LldpLocalGroupTracker.decodeMacAddress(lldpportid);
            case LLDP_PORTID_SUBTYPE_NETWORKADDRESS:
                LldpUtils.decodeNetworkAddress(lldpportid.toDisplayString());
           }
        } catch (Exception iae) {
            iae.printStackTrace();
        }
        return lldpportid.toHexString();
    }


    public static class LldpRemRow extends SnmpRowResult {

		public LldpRemRow(int columnCount, SnmpInstId instance) {
			super(columnCount, instance);
            LOG.debug( "column count = {}, instance = {}", columnCount, instance);
		}

	    public Integer getLldpRemLocalPortNum() {
	    	return getInstance().getSubIdAt(1);
	    }
        public Integer getLldpRemIndex() {
            return getInstance().getSubIdAt(2);
        }

        public Integer getLldpRemChassisidSubtype() {
	    	return getValue(LLDP_REM_CHASSIS_ID_SUBTYPE_OID).toInt();
	    }
	    
	    public SnmpValue getLldpRemChassisId() {
	        return getValue(LLDP_REM_CHASSIS_ID_OID);
	    }
	    
	    public Integer getLldpRemPortidSubtype() {
	    	return getValue(LLDP_REM_PORT_ID_SUBTYPE_OID).toInt();
	    }

	    public String getLldpRemPortid() {
	    	return decodeLldpPortId(getLldpRemPortidSubtype(), getValue(LLDP_REM_PORT_ID_OID));
	    }
	    
	    public String getLldpRemPortDescr() {
	    	if (getValue(LLDP_REM_PORT_DESCR_OID) != null)
	    		return getValue(LLDP_REM_PORT_DESCR_OID).toDisplayString();
	    	return "";
	    }

	    public String getLldpRemSysname() {
	        return getValue(LLDP_REM_SYSNAME_OID).toDisplayString();
	    }
	    
	    public LldpLink getLldpLink() {

            LldpLink lldpLink = new LldpLink();
            lldpLink.setLldpRemLocalPortNum(getLldpRemLocalPortNum());
            lldpLink.setLldpRemIndex(getLldpRemIndex());
            lldpLink.setLldpRemChassisId(LldpLocalGroupTracker.decodeLldpChassisId(getLldpRemChassisId() , getLldpRemChassisidSubtype()));
            lldpLink.setLldpRemChassisIdSubType(LldpChassisIdSubType.get(getLldpRemChassisidSubtype()));
            lldpLink.setLldpRemSysname(getLldpRemSysname());
            lldpLink.setLldpRemPortId(getLldpRemPortid());
            lldpLink.setLldpRemPortIdSubType(LldpPortIdSubType.get(getLldpRemPortidSubtype()));
            lldpLink.setLldpRemPortDescr(getLldpRemPortDescr());
            LOG.debug( "getLldpLink: local port num: {}, identifier: {}, chassis subtype: {}, \n rem sysname: {}, rem port: {}, rem port subtype: {}",  
                       getLldpRemLocalPortNum(),
                       lldpLink.getLldpRemChassisId(), 
                       LldpChassisIdSubType.getTypeString(getLldpRemChassisidSubtype()), 
                       lldpLink.getLldpRemSysname(),
                       lldpLink.getLldpRemPortId(),
                       LldpPortIdSubType.getTypeString(getLldpRemPortidSubtype()));
            
            return lldpLink;
	    }
    }

    public LldpRemTableTracker() {
        super(s_lldpremtable_elemList);
    }
    
    /** {@inheritDoc} */
    @Override
    public SnmpRowResult createRowResult(final int columnCount, final SnmpInstId instance) {
        return new LldpRemRow(columnCount, instance);
    }

    /** {@inheritDoc} */
    @Override
    public void rowCompleted(final SnmpRowResult row) {
        processLldpRemRow((LldpRemRow)row);
    }

    /**
     * <p>processLldpRemRow</p>
     *
     * @param row a {@link org.opennms.netmgt.enlinkd.snmp.LldpRemTableTracker.LldpRemRow} object.
     */
    public void processLldpRemRow(final LldpRemRow row) {
        System.out.printf("\t\t%s (%s)= %s (%s)\n", LLDP_REM_CHASSIS_ID_SUBTYPE_OID + "." + row.getInstance().toString(), LLDP_REM_CHASSIS_ID_SUBTYPE, row.getLldpRemChassisidSubtype(), LldpChassisIdSubType.getTypeString(row.getLldpRemChassisidSubtype()));
        System.out.printf("\t\t%s (%s)= %s \n", LLDP_REM_CHASSIS_ID_OID + "." + row.getInstance().toString(), LLDP_REM_CHASSIS_ID, LldpLocalGroupTracker.decodeLldpChassisId(row.getLldpRemChassisId() , row.getLldpRemChassisidSubtype()));
        System.out.printf("\t\t%s (%s)= %s (%s)\n", LLDP_REM_PORT_ID_SUBTYPE_OID + "." + row.getInstance().toString(), LLDP_REM_PORT_ID_SUBTYPE, row.getLldpRemPortidSubtype(), LldpPortIdSubType.getTypeString(row.getLldpRemPortidSubtype()));
        System.out.printf("\t\t%s (%s)= %s \n", LLDP_REM_PORT_ID_OID + "." + row.getInstance().toString(), LLDP_REM_PORT_ID, row.getLldpRemPortid());
        System.out.printf("\t\t%s (%s)= %s \n", LLDP_REM_PORT_DESCR_OID + "." + row.getInstance().toString(), LLDP_REM_PORT_DESCR, row.getLldpRemPortDescr());
        System.out.printf("\t\t%s (%s)= %s \n", LLDP_REM_SYSNAME_OID + "." + row.getInstance().toString(), LLDP_REM_SYSNAME, row.getLldpRemSysname());
    }

}
