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

package org.opennms.netmgt.capsd.plugins;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.capsd.AbstractPlugin;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.snmp4j.SnmpHelpers;
import org.opennms.netmgt.utils.ParameterMap;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.OID;
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

    private static final String PROTOCOL_NAME = "SNMP";
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
        
        //Get a target from the PeerFactory and force to version 3
        //Target target = SnmpPeerFactory.getInstance().getTarget(inetAddress, SnmpConstants.version3);
        
        String vbValue = (String)qualifiers.get("vbvalue");
        String oid = ParameterMap.getKeyedString(qualifiers, "vbname", DEFAULT_OID);
        String forcedVersion = ParameterMap.getKeyedString(qualifiers, "forced version", null);
        Target target = null;

        // "force version" parm
        //
        if (forcedVersion != null) {
            if (forcedVersion.equalsIgnoreCase("snmpv1"))
                target = SnmpPeerFactory.getInstance().getTarget(inetAddress, SnmpConstants.version1);
            else if (forcedVersion.equalsIgnoreCase("snmpv2") || forcedVersion.equalsIgnoreCase("snmpv2c"))
                target = SnmpPeerFactory.getInstance().getTarget(inetAddress, SnmpConstants.version2c);
            else if (forcedVersion.equalsIgnoreCase("snmpv3"))
                target = SnmpPeerFactory.getInstance().getTarget(inetAddress, SnmpConstants.version2c);
        } else {
            target = SnmpPeerFactory.getInstance().getTarget(inetAddress);
        }
        
        boolean isSupported = false;

        if (target.getVersion() == SnmpConstants.version3)
            MPv3.setEnterpriseID(5813);
        
        Snmp snmp = null;
        try {
            snmp = SnmpHelpers.createSnmpSession();
            snmp.listen();
            PDU requestPDU = SnmpHelpers.createPDU(target.getVersion());
            VariableBinding vb = new VariableBinding(new OID(oid));
            requestPDU.add(vb);
            
            PDU responsePDU = null;
            ResponseEvent responseEvent;
            responseEvent = snmp.get(requestPDU, target);
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
