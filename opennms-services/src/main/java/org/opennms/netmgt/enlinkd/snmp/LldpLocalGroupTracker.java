/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.snmp.AggregateTracker;
import org.opennms.netmgt.snmp.NamedSnmpVar;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpStore;
import org.opennms.netmgt.snmp.SnmpValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LldpLocalGroupTracker extends AggregateTracker {

    private final static Logger LOG = LoggerFactory.getLogger(LldpLocalGroupTracker.class);
	
    public final static String LLDP_LOC_CHASSISID_SUBTYPE_ALIAS = "lldpLocChassisIdSubtype";
    public final static String LLDP_LOC_CHASSISID_SUBTYPE_OID = ".1.0.8802.1.1.2.1.3.1";
    
    public final static String LLDP_LOC_CHASSISID_ALIAS    = "lldpLocChassisId";
    public final static String LLDP_LOC_CHASSISID_OID    = ".1.0.8802.1.1.2.1.3.2";
    
    public final static String LLDP_LOC_SYSNAME_ALIAS = "lldpLocSysName";
    public final static String LLDP_LOC_SYSNAME_OID = ".1.0.8802.1.1.2.1.3.3";
    
    public static NamedSnmpVar[] ms_elemList = null;
    
    static {
        ms_elemList = new NamedSnmpVar[3];
        int ndx = 0;

        /**
         * <P>
         * "The type of encoding used to identify the chassis
         * associated with the local system."
         * </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPINT32,LLDP_LOC_CHASSISID_SUBTYPE_ALIAS,LLDP_LOC_CHASSISID_SUBTYPE_OID);

        /**
         * <P>
         *  "The string value used to identify the chassis component
         *   associated with the local system."
         *   </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,LLDP_LOC_CHASSISID_ALIAS,LLDP_LOC_CHASSISID_OID);
        
        /**
         * <P>
         * "The string value used to identify the system name of the
         * local system.  If the local agent supports IETF RFC 3418,
         * lldpLocSysName object should have the same value of sysName
         * object."
         *   </P>
         */
        ms_elemList[ndx++] = new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING,LLDP_LOC_SYSNAME_ALIAS,LLDP_LOC_SYSNAME_OID);
    }
    
    public static final String LLDP_LOC_OID = ".1.0.8802.1.1.2.1.3";

    public static String decodeLldpChassisId(final SnmpValue lldpchassisid, Integer lldpLocChassisidSubType) {
        if (lldpLocChassisidSubType == null) {
            if (lldpchassisid.isDisplayable())
                return lldpchassisid.toDisplayString();
            else 
                return lldpchassisid.toHexString();
        }
        try {
            LldpChassisIdSubType type = LldpChassisIdSubType.get(lldpLocChassisidSubType);
        
        /*
         *  If the associated LldpChassisIdSubtype object has a value of
         *  'chassisComponent(1)', then the octet string identifies
         *  a particular instance of the entPhysicalAlias object
         *  (defined in IETF RFC 2737) for a chassis component (i.e.,
         *  an entPhysicalClass value of 'chassis(3)').
         *
         *  If the associated LldpChassisIdSubtype object has a value
         *  of 'interfaceAlias(2)', then the octet string identifies
         *  a particular instance of the ifAlias object (defined in
         *  IETF RFC 2863) for an interface on the containing chassis.
         *  If the particular ifAlias object does not contain any values,
         *  another chassis identifier type should be used.
         *
         *  If the associated LldpChassisIdSubtype object has a value
         *  of 'portComponent(3)', then the octet string identifies a
         *  particular instance of the entPhysicalAlias object (defined
         *  in IETF RFC 2737) for a port or backplane component within
         *  the containing chassis.
         *
         *  If the associated LldpChassisIdSubtype object has a value of
         *  'macAddress(4)', then this string identifies a particular
         *  unicast source address (encoded in network byte order and
         *  IEEE 802.3 canonical bit order), of a port on the containing
         *  chassis as defined in IEEE Std 802-2001.
         *
         *  If the associated LldpChassisIdSubtype object has a value of
         *  'networkAddress(5)', then this string identifies a particular
         *  network address, encoded in network byte order, associated
         *  with one or more ports on the containing chassis. The first
         *  octet contains the IANA Address Family Numbers enumeration
         *  value for the specific address type, and octets 2 through
         *  N contain the network address value in network byte order.
         *
         *  If the associated LldpChassisIdSubtype object has a value
         *  of 'interfaceName(6)', then the octet string identifies
         *  a particular instance of the ifName object (defined in
         *  IETF RFC 2863) for an interface on the containing chassis.
         *  If the particular ifName object does not contain any values,
         *  another chassis identifier type should be used.
         *
         * If the associated LldpChassisIdSubtype object has a value of
         * 'local(7)', then this string identifies a locally assigned
         * Chassis ID.
         * 
         * 
         */
            switch (type) {
        /*
         *  entPhysicalAlias          SnmpAdminString
         *
         *   SnmpAdminString ::= TEXTUAL-CONVENTION
         *       DISPLAY-HINT "255a"
         *       STATUS       current
         *       DESCRIPTION "An octet string containing administrative
         *           information, preferably in human-readable form.
         *
         *           To facilitate internationalization, this
         *           information is represented using the ISO/IEC
         *           IS 10646-1 character set, encoded as an octet
         *           string using the UTF-8 transformation format
         *           described in [RFC2279].
         *
         *           Since additional code points are added by
         *           amendments to the 10646 standard from time
         *           to time, implementations must be prepared to
         *           encounter any code point from 0x00000000 to
         *           0x7fffffff.  Byte sequences that do not
         *           correspond to the valid UTF-8 encoding of a
         *           code point or are outside this range are
         *           prohibited.
         *
         *           The use of control codes should be avoided.
         * 
         *           When it is necessary to represent a newline,
         *           the control code sequence CR LF should be used.
         *
         *           The use of leading or trailing white space should
         *           be avoided.
         *
         *           For code points not directly supported by user
         *           interface hardware or software, an alternative
         *           means of entry and display, such as hexadecimal,
         *           may be provided.
         *
         *           For information encoded in 7-bit US-ASCII,
         *           the UTF-8 encoding is identical to the
         *           US-ASCII encoding.
         *
         *           UTF-8 may require multiple bytes to represent a
         *           single character / code point; thus the length
         *           of this object in octets may be different from
         *           the number of characters encoded.  Similarly,
         *           size constraints refer to the number of encoded
         *           octets, not the number of characters represented
         *           by an encoding.
         *           Note that when this TC is used for an object that
         *           is used or envisioned to be used as an index, then
         *           a SIZE restriction MUST be specified so that the
         *           number of sub-identifiers for any object instance
         *           does not exceed the limit of 128, as defined by
         *           [RFC1905].
         * 
         *           Note that the size of an SnmpAdminString object is
         *           measured in octets, not characters.
         *          "
         *      SYNTAX       OCTET STRING (SIZE (0..255))
         */
             case LLDP_CHASSISID_SUBTYPE_CHASSISCOMPONENT:
             case LLDP_CHASSISID_SUBTYPE_PORTCOMPONENT:                 
             case LLDP_CHASSISID_SUBTYPE_INTERFACEALIAS:
             case LLDP_CHASSISID_SUBTYPE_INTERFACENAME:  
             case LLDP_CHASSISID_SUBTYPE_LOCAL:
                 if (lldpchassisid.isDisplayable())
                     return lldpchassisid.toDisplayString();
                 else 
                     return lldpchassisid.toHexString();
             case LLDP_CHASSISID_SUBTYPE_MACADDRESS:
                 return lldpchassisid.toHexString();
             case LLDP_CHASSISID_SUBTYPE_NETWORKADDRESS:
                 LldpUtils.decodeNetworkAddress(lldpchassisid.toDisplayString());
            }
        } catch (IllegalArgumentException iae) {
            iae.printStackTrace();
        } catch (IndexOutOfBoundsException ioe) {
            ioe.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lldpchassisid.toHexString();
    }

    private SnmpStore m_store;
    
    public LldpLocalGroupTracker() {
        super(NamedSnmpVar.getTrackersFor(ms_elemList));
        m_store = new SnmpStore(ms_elemList);
    }
    
    public Integer getLldpLocChassisidSubType() {
        return m_store.getInt32(LLDP_LOC_CHASSISID_SUBTYPE_ALIAS);
    }
    
    public SnmpValue getLldpLocChassisid() {
    	return m_store.getValue(LLDP_LOC_CHASSISID_ALIAS);
    }
    
    public String getLldpLocSysname() {
        return m_store.getDisplayString(LLDP_LOC_SYSNAME_ALIAS);
    }
    
    /** {@inheritDoc} */
    protected void storeResult(SnmpResult res) {
        m_store.storeResult(res);
    }

    /** {@inheritDoc} */
    protected void reportGenErr(String msg) {
        LOG.warn("Error retrieving lldpLocalGroup: {}",msg);
    }

    /** {@inheritDoc} */
    protected void reportNoSuchNameErr(String msg) {
        LOG.info("Error retrieving lldpLocalGroup: {}",msg);
    }

    public LldpElement getLldpElement() {
		LldpElement lldpElement = new LldpElement();
		lldpElement.setLldpChassisId(decodeLldpChassisId(getLldpLocChassisid(),getLldpLocChassisidSubType()));
		lldpElement.setLldpChassisIdSubType(LldpChassisIdSubType.get(getLldpLocChassisidSubType()));
		lldpElement.setLldpSysname(getLldpLocSysname());
		return lldpElement;
    }
	
}
