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


import org.opennms.core.utils.LldpUtils;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.snmp.RowCallback;
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
    
    
    public final static SnmpObjId LLDP_REM_CHASSIS_ID_SUBTYPE = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"4");
    public final static SnmpObjId LLDP_REM_CHASSIS_ID         = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"5");
    public final static SnmpObjId LLDP_REM_PORT_ID_SUBTYPE    = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"6");
    public final static SnmpObjId LLDP_REM_PORT_ID            = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"7");
    public final static SnmpObjId LLDP_REM_PORT_DESCR         = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"8");
    public final static SnmpObjId LLDP_REM_SYSNAME            = SnmpObjId.get(LLDP_REM_TABLE_ENTRY,"9");

    public static final SnmpObjId[] s_lldpremtable_elemList = new SnmpObjId[] {
        
        /**
         *  "The type of encoding used to identify the chassis associated
         *  with the remote system."
         */
        LLDP_REM_CHASSIS_ID_SUBTYPE,
        
        /**
         * "The string value used to identify the chassis component
         * associated with the remote system."
         */
       LLDP_REM_CHASSIS_ID,

        /**
         * "The type of port identifier encoding used in the associated
         * 'lldpRemPortId' object."
         */
        LLDP_REM_PORT_ID_SUBTYPE,

        /**
         * "The string value used to identify the port component
            associated with the remote system."
         */
        LLDP_REM_PORT_ID,
        
        /**
         * 	"The string value used to identify the description of 
         *  the given port associated with the remote system."
         */
        LLDP_REM_PORT_DESCR,

        /**
         * "The string value used to identify the port component
         * associated with the remote system."
         */
        LLDP_REM_SYSNAME

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
                if (lldpportid.isDisplayable())
                    return lldpportid.toDisplayString();
                else 
                    return lldpportid.toHexString();
            case LLDP_PORTID_SUBTYPE_MACADDRESS:
                return lldpportid.toHexString();
            case LLDP_PORTID_SUBTYPE_NETWORKADDRESS:
                LldpUtils.decodeNetworkAddress(lldpportid.toDisplayString());
           }
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (IndexOutOfBoundsException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
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
	    
	    public Integer getLldpRemChassisidSubtype() {
	    	return getValue(LLDP_REM_CHASSIS_ID_SUBTYPE).toInt();
	    }
	    
	    public SnmpValue getLldpRemChassisId() {
	        return getValue(LLDP_REM_CHASSIS_ID);
	    }
	    
	    public Integer getLldpRemPortidSubtype() {
	    	return getValue(LLDP_REM_PORT_ID_SUBTYPE).toInt();
	    }

	    public String getLldpRemPortid() {
	    	return decodeLldpPortId(getLldpRemPortidSubtype(), getValue(LLDP_REM_PORT_ID));
	    }
	    
	    public String getLldpRemPortDescr() {
	    	if (getValue(LLDP_REM_PORT_DESCR) != null)
	    		return getValue(LLDP_REM_PORT_DESCR).toDisplayString();
	    	return "";
	    }

	    public String getLldpRemSysname() {
	        return getValue(LLDP_REM_SYSNAME).toDisplayString();
	    }
	    
	    public LldpLink getLldpLink(LldpLocPortGetter lldpLocPort) {
            LOG.info( "getLldpLink: row local port num: {}",  getLldpRemLocalPortNum());

            LldpLink lldpLink = lldpLocPort.get(getLldpRemLocalPortNum());
            // Check if lldpLink is null.....and do what?

            LOG.info( "getLldpLink: row local port id: {}", lldpLink.getLldpPortId());
            LOG.info( "getLldpLink: row local port subtype: {}", LldpPortIdSubType.getTypeString(lldpLink.getLldpPortIdSubType().getValue()));
    	
            lldpLink.setLldpRemChassisId(LldpLocalGroupTracker.decodeLldpChassisId(getLldpRemChassisId() , getLldpRemChassisidSubtype()));
            LOG.info( "getLldpLink: row rem lldp identifier: {}", lldpLink.getLldpRemChassisId());
            
            lldpLink.setLldpRemChassisIdSubType(LldpChassisIdSubType.get(getLldpRemChassisidSubtype()));
            LOG.info( "getLldpLink: row rem lldp chassis id subtype: {}", LldpChassisIdSubType.getTypeString(getLldpRemChassisidSubtype()));
    	
            lldpLink.setLldpRemSysname(getLldpRemSysname());
            LOG.info( "getLldpLink: row rem lldp sysname: {}", lldpLink.getLldpRemSysname());

            lldpLink.setLldpRemPortId(getLldpRemPortid());
            LOG.info( "getLldpLink: row rem lldp port id: {}", lldpLink.getLldpRemPortId());

            lldpLink.setLldpRemPortIdSubType(LldpPortIdSubType.get(getLldpRemPortidSubtype()));
            LOG.info( "getLldpLink: row rem lldp port id subtype: {}", LldpPortIdSubType.getTypeString(getLldpRemPortidSubtype()));
 
            lldpLink.setLldpRemPortDescr(getLldpRemPortDescr());
            
    		return lldpLink;
	    }
    }

    public LldpRemTableTracker() {
        super(s_lldpremtable_elemList);
    }
    
    /**
     * <p>Constructor for LldpRemTableTracker.</p>
     *
     * @param rowProcessor a {@link org.opennms.netmgt.snmp.RowCallback} object.
     */
    public LldpRemTableTracker(final RowCallback rowProcessor) {
        super(rowProcessor, s_lldpremtable_elemList);
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
    }

}
