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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
//
// Tab Size = 8
//

package org.opennms.netmgt.capsd;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.utils.SnmpHelpers;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;

/**
 * This class is used by capsd to detect if an agent supports SNMPv3.
 * Doesn't require actual authentation for detection.
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * 
 */
public final class SnmpV3Plugin extends AbstractPlugin {

    private static final String PROTOCOL_NAME = "SNMPv3";
    private static final String DEFAULT_PORT = "161";
    private static final String DEFAULT_TIMEOUT = "3000";
    private static final String DEFAULT_RETRY = "2";
    private static final String DEFAULT_SECURITY_NAME = "opennms";
    private static final OID DEFAULT_AUTH_PROTOCOL = AuthMD5.ID;
    private static final OctetString DEFAULT_AUTH_PASSPHRASE = new OctetString("opennms");
    private static final OID DEFAULT_PRIV_PROTOCOL = PrivDES.ID;
    private static final OctetString DEFAULT_PRIV_PASSPHRASE = new OctetString("opennms");
    private static final String DEFAULT_VERSION = "snmpv3";
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0";
    
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }
    
    /**
     * If this signature is called by capsd, make up an empty
     * Hash and pass on its overloaded sister method.
     * 
     * @param address
     *      The IP address of the agent
     * @return the return value of overloaded method
     */

    public boolean isProtocolSupported(InetAddress address) {
        return isProtocolSupported(address, new HashMap());
        
    }
    
    /**
     * This is the method used by the plugin to attempt SNMPv3 communications
     * with an agent specified at @param address using @param qualifiers specified
     * in capsd-configuration.xml
     * 
     * @param address
     *      The IP address of the agent.
     * @param qualifiers
     *      A HashMap of parameter keys set in the config.
     * 
     * @return True if SNMPv3 is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address, Map qualifiers) {

        InetAddress inetAddress = address;
        String port =  (qualifiers.get("port") == null ? DEFAULT_PORT : (String)qualifiers.get("port"));
        String timeout = (qualifiers.get("timeout") == null ? DEFAULT_TIMEOUT : (String)qualifiers.get("timeout"));
        String retry = (qualifiers.get("retry") == null ? DEFAULT_RETRY : (String)qualifiers.get("retry"));
//        String forceVersion = (qualifiers.get("forced version") == null ? DEFAULT_VERSION : (String)qualifiers.get("forced version"));
        String vbValue = (String)qualifiers.get("vbvalue");
        String oid = ParameterMap.getKeyedString(qualifiers, "vbname", DEFAULT_OID);
        
        String uname = (qualifiers.get("security name") == null ? DEFAULT_SECURITY_NAME : (String)qualifiers.get("security name"));
        
        boolean isSupported = false;
        
        MPv3.setEnterpriseID(5813);
        
        String transportAddress = inetAddress.getHostAddress() + "/" + DEFAULT_PORT;
        
        Address targetAddress = new UdpAddress(transportAddress);
        Snmp snmp = null;
        try {
            snmp = SnmpHelpers.createSnmpSession(new OctetString(uname));
            UserTarget target = new UserTarget();
            target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
            target.setVersion(SnmpConstants.version3);
            target.setAddress(targetAddress);
            target.setRetries(Integer.parseInt(retry));
            target.setTimeout(Integer.parseInt(timeout));
            target.setSecurityName(new OctetString(uname));
            snmp.listen();
            PDU request = SnmpHelpers.createPDU(target);
            VariableBinding vb = new VariableBinding(new OID(DEFAULT_OID));
            request.add(vb);
            
            PDU response = null;
            ResponseEvent responseEvent;
            responseEvent = snmp.send(request, target);
            snmp.close();
            
            if (responseEvent.getResponse() != null) {
                isSupported = true;
            } else {
                isSupported = false;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return isSupported;
    }
    
}
