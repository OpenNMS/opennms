//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2007 Jun 23: Add whitespace, be explicit about field visibility, and use
//              Java 5 generics. - dj@opennms.org  
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.snmp;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;

public class TrapIdentity {
    private int m_generic;
    private int m_specific;
    private String m_enterpriseId;
    
    /**
     * The standard traps list
     */
    private static final List<SnmpObjId> GENERIC_TRAPS;
    
    /**
     * The dot separator in an OID
     */
    private static final char DOT_CHAR = '.';
    
    /**
     * The snmp trap enterprise OID, which if present in a V2 trap is the last
     * varbind.
     * 
     * ref - book 'SNMP, SNMPv2, SNMPv3..' by William Stallings, third edition,
     * section 13.1.3
     */
    private static final String SNMP_TRAP_ENTERPRISE_ID = ".1.3.6.1.6.3.1.1.4.3.0";
    
    /**
     * The snmpTraps value to be used in case a standard trap comes in without
     * the SNMP_TRAP_ENTERPRISE_ID as the last varbind.
     */
    private static final String SNMP_TRAPS = ".1.3.6.1.6.3.1.1.5";
    
    /**
     * Create the standard traps list - used in v2 processing
     */
    static {
        GENERIC_TRAPS = new ArrayList<SnmpObjId>();
        GENERIC_TRAPS.add(new SnmpObjId("1.3.6.1.6.3.1.1.5.1")); // coldStart
        GENERIC_TRAPS.add(new SnmpObjId("1.3.6.1.6.3.1.1.5.2")); // warmStart
        GENERIC_TRAPS.add(new SnmpObjId("1.3.6.1.6.3.1.1.5.3")); // linkDown
        GENERIC_TRAPS.add(new SnmpObjId("1.3.6.1.6.3.1.1.5.4")); // linkUp
        GENERIC_TRAPS.add(new SnmpObjId("1.3.6.1.6.3.1.1.5.5")); // authenticationFailure
        GENERIC_TRAPS.add(new SnmpObjId("1.3.6.1.6.3.1.1.5.6")); // egpNeighborLoss
    }
    
    public TrapIdentity(SnmpObjId snmpTrapOid, SnmpObjId lastVarBindOid, SnmpValue lastVarBindValue) {
        String snmpTrapOidValue = snmpTrapOid.toString();
        
        if (log().isDebugEnabled()) {
            log().debug("snmpTrapOID: " + snmpTrapOidValue);
        }

        // get the last subid
        int lastIndex = snmpTrapOidValue.lastIndexOf(TrapIdentity.DOT_CHAR);
        String lastSubIdStr = snmpTrapOidValue.substring(lastIndex + 1);
        int lastSubId = -1;
        try {
            lastSubId = Integer.parseInt(lastSubIdStr);
        } catch (NumberFormatException nfe) {
            lastSubId = -1;
        }
        // Check if standard trap
        if (TrapIdentity.GENERIC_TRAPS.contains(snmpTrapOid)) {
            // set generic
            setGeneric(lastSubId - 1);
            
            // set specific to zero
            setSpecific(0);
            
            // if present, the 'snmpTrapEnterprise' OID occurs as
            // the last OID
            // Check the last varbind to see if it is the enterprise ID
            String varBindName = lastVarBindOid.toString();
            if (varBindName.equals(TrapIdentity.SNMP_TRAP_ENTERPRISE_ID)) {
                // if present, set the value of the varbind as the
                // enterprise id
                setEnterpriseId(lastVarBindValue.toString());
            } else {
                // if not present, set the value of the varbind as the
                // snmpTraps value defined as in RFC 1907
                setEnterpriseId(TrapIdentity.SNMP_TRAPS + "." + snmpTrapOidValue.charAt(snmpTrapOidValue.length() - 1));
            }
            
        } else // not standard trap
        {
            // set generic to 6
            setGeneric(6);
            
            setSpecific(lastSubId);
            
            // get the next to last subid
            int nextToLastIndex = snmpTrapOidValue.lastIndexOf(TrapIdentity.DOT_CHAR, lastIndex - 1);
            // check if value is zero
            String nextToLastSubIdStr = snmpTrapOidValue.substring(nextToLastIndex + 1, lastIndex);
            if (nextToLastSubIdStr.equals("0")) {
                // set enterprise value to trap oid minus the
                // the last two subids
                setEnterpriseId(snmpTrapOidValue.substring(0, nextToLastIndex));
            } else {
                setEnterpriseId(snmpTrapOidValue.substring(0, lastIndex));
            }
        }
    }
    
    public TrapIdentity(SnmpObjId entId, int generic, int specific) {
        m_enterpriseId = entId.toString();
        m_generic = generic;
        m_specific = specific;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public int getGeneric() {
        return m_generic;
    }
    
    private void setGeneric(int generic) {
        m_generic = generic;
    }
    
    public int getSpecific() {
        return m_specific;
    }
    
    private void setSpecific(int specific) {
        m_specific = specific;
    }
    
    public String getEnterpriseId() {
        return m_enterpriseId;
    }
    
    private void setEnterpriseId(String enterpriseId) {
        m_enterpriseId = enterpriseId;
    }

    public String toString() {
        return "[Generic="+getGeneric()+", Specific="+getSpecific()+", EnterpriseId="+getEnterpriseId()+"]";
    }
    
    
}