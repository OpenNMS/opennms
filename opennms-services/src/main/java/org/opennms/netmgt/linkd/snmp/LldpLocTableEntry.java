/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.linkd.snmp;

import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpResult;
import org.opennms.netmgt.snmp.SnmpUtils;

public class LldpLocTableEntry extends SnmpStore {

    public final static String LLDP_LOC_PORTNUM_ALIAS         = "lldpLocPortNum";
    public final static String LLDP_LOC_PORTID_SUBTYPE_ALIAS  = "lldpLocPortIdSubtype";
    public final static String LLDP_LOC_PORTID_ALIAS          = "lldpLocPortId";
    
    public final static String LLDP_LOC_PORTNUM_OID           = ".1.0.8802.1.1.2.1.3.7.1.1";
    public final static String LLDP_LOC_PORTID_SUBTYPE_OID = ".1.0.8802.1.1.2.1.3.7.1.2";
    public final static String LLDP_LOC_PORTID_OID            = ".1.0.8802.1.1.2.1.3.7.1.3";

    public static final NamedSnmpVar[] lldploctable_elemList = new NamedSnmpVar[] {
        
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, LLDP_LOC_PORTNUM_ALIAS, LLDP_LOC_PORTNUM_OID, 1),

        /**
         *  "The type of port identifier encoding used in the associated
         *  'lldpLocPortId' object."
        */
        new NamedSnmpVar(NamedSnmpVar.SNMPINT32, LLDP_LOC_PORTID_SUBTYPE_ALIAS, LLDP_LOC_PORTID_SUBTYPE_OID, 2),
        
        /**
         * "The string value used to identify the port component
         * associated with a given port in the local system."
         */
        new NamedSnmpVar(NamedSnmpVar.SNMPOCTETSTRING, LLDP_LOC_PORTID_ALIAS, LLDP_LOC_PORTID_OID, 3),

    };
    
    public static final String TABLE_OID = ".1.0.8802.1.1.2.1.3.7.1"; // start of table (GETNEXT)
    
    private boolean hasLldpLocPortId = false;

    public LldpLocTableEntry() {
        super(lldploctable_elemList);
    }

    public Integer getLldpLocPortNum() {
        return getInt32(LLDP_LOC_PORTNUM_ALIAS);
    }
    public Integer getLldpLocPortIdSubtype() {
        return getInt32(LLDP_LOC_PORTID_SUBTYPE_ALIAS);
    }
    
    public String getLldpLocPortid() {
        return getDisplayString(LLDP_LOC_PORTID_ALIAS);
    }
                
    public String getLldpLocMacAddress() {
        return getHexString(LLDP_LOC_PORTID_ALIAS);
    }
    
    public InetAddress getLldpLocIpAddress() {
        return getIPAddress(LLDP_LOC_PORTID_ALIAS);
    }

    /** {@inheritDoc} */
    @Override
    public void storeResult(SnmpResult res) {
        if (!hasLldpLocPortId) {
            int lldpLocPortId = res.getInstance().getLastSubId();
            super.storeResult(new SnmpResult(SnmpObjId.get(LLDP_LOC_PORTNUM_OID), res.getInstance(), 
                        SnmpUtils.getValueFactory().getInt32(lldpLocPortId)));
            hasLldpLocPortId = true;
        }
        super.storeResult(res);
    }


}
