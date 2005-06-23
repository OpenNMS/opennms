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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;

import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OctetString;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * @author david
 *
 */
public class SnmpHelpers {
    
    public static PDU createPDU(int version) {
        PDU request;
        if (version == SnmpConstants.version3)
            request = new ScopedPDU();
        else
            request = new PDU();
        return request;
    }
    
    public static PDU createPDU() {
        return createPDU(SnmpConstants.version1);
    }

    
    public static Snmp createSnmpSession() throws IOException {
        TransportMapping transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        return snmp;
    }
    
    public static Snmp createSnmpSession(Target target) throws IOException {
        Snmp snmp = createSnmpSession();
        if (target.getVersion() == SnmpConstants.version3) {
            UserTarget userTarget = (UserTarget)target;
            USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
            SecurityModels.getInstance().addSecurityModel(usm);
//            UsmUser user = new UsmUser(DEFAULT_SECURITY_NAME, DEFAULT_AUTH_PROTOCOL, DEFAULT_AUTH_PASSPHRASE, DEFAULT_PRIV_PROTOCOL, DEFAULT_PRIV_PASSPHRASE);
            UsmUser user = new UsmUser(userTarget.getSecurityName(), null, null, null, null);
            snmp.getUSM().addUser(userTarget.getSecurityName(), user);
        }
        return snmp;
    }


    /**
     * Returns a string representation of the SNMP4J version constants
     * @param version
     * @return
     */
    public static String versionString(int version) {
        String retVal = null;
            if(version == SnmpConstants.version1)
                retVal = "SNMPv1";
            if (version == SnmpConstants.version2c)
                retVal = "SNMPv2c";
            if (version == SnmpConstants.version3)
                retVal = "SNMPv3";
        return retVal;
    }


}
