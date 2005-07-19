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

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.UsmUser;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * @author david
 *
 */
public class SnmpHelpers {
    
    /**
     * Creates an SNMP4J PDU based on version and PDU type
     * set in the agentConfig parameter
     * 
     * @param agentConfig
     * @return
     */
    public static PDU createPDU(SnmpAgentConfig agentConfig) {
        
        //TODO: need to do something better here.
        if (!agentConfig.isAdapted()) {
            return null;
        }
        
        PDU request = createPDU(agentConfig.getVersion());
        request.setType((agentConfig.getPduType()));
        return request;
    }
    
    /**
     * Creates an SNMP4J PDU using the OpenNMS default version constant
     * 
     * @return
     */
    public static PDU createPDU() {
        return createPDU(SnmpConstants.version1);
    }

    /**
     * Creates an SNMP4J PDU based on the SNMP4J version constants.
     * A v3 request requires a ScopedPDU.
     * 
     * @param version
     * @return
     */
    public static PDU createPDU(int version) {
        PDU request;
        if (version == SnmpConstants.version3)
            request = new ScopedPDU();
        else
            request = new PDU();
        return request;
    }
    
    public static Snmp createSnmpSession() throws IOException {
        TransportMapping transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        return snmp;
    }
    
    public static Snmp createSnmpSession(SnmpAgentConfig agentConfig) throws IOException {
        if (!agentConfig.isAdapted()) {
            throw new IllegalArgumentException("Error creating SNMP session... agentConfig has not been adapted to SNMP4J.");
        }
        
        Snmp session = createSnmpSession();
        if (agentConfig.getVersion() == SnmpConstants.version3) {
            session.getUSM().addUser((Snmp4JStrategy.createOctetString(agentConfig.getSecurityName())),
                    new UsmUser(Snmp4JStrategy.createOctetString(agentConfig.getSecurityName()),
                            Snmp4JStrategy.convertAuthProtocol(agentConfig.getAuthProtocol()),
                            Snmp4JStrategy.createOctetString(agentConfig.getAuthPassPhrase()),
                            Snmp4JStrategy.convertPrivProtocol(agentConfig.getPrivProtocol()),
                            Snmp4JStrategy.createOctetString(agentConfig.getPrivPassPhrase())));

        }
        return session;
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
