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
// Tab Size = 8
//

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.utils.SnmpHelpers;
import org.opennms.protocols.snmp.SnmpSession;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityModels;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;

/**
 * <P>
 * Used by the Poller to monitor status of a V3 agent.
 * </P>
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
final public class SnmpV3Monitor extends IPv4Monitor {

    private static final String SERVICE_NAME = "SNMPv3";

    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2"; 

    static final String SNMPV3_TARGET_KEY = "org.snmp4j.UserTarget";

    private static final int DEFAULT_PORT = 161;
    private static final String DEFAULT_TIMEOUT = "3000";
    private static final String DEFAULT_RETRY = "2";

    private static final String DEFAULT_SECURITY_NAME = "opennms";
    private static final OID DEFAULT_AUTH_PROTOCOL = AuthMD5.ID;
    private static final OctetString DEFAULT_AUTH_PASSPHRASE = new OctetString("opennms");
    private static final OID DEFAULT_PRIV_PROTOCOL = PrivDES.ID;
    private static final OctetString DEFAULT_PRIV_PASSPHRASE = new OctetString("opennms");
    private static final String DEFAULT_VERSION = "snmpv3";


    public String serviceName() {
        return SERVICE_NAME;
    }


    public void initialize(NetworkInterface iface) {
        Category log = ThreadCategory.getInstance(getClass());

        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new RuntimeException("Unsupported interface type, only TYPE_IPV4 currently supported");
        InetAddress inetAddress = (InetAddress) iface.getAddress();

        if (log.isDebugEnabled())
            log.debug("initialize: setting SNMPv3 target attributes for this interface: " + inetAddress.getHostAddress());

        String transportAddress = inetAddress.getHostAddress() + "/" + DEFAULT_PORT;
        Address targetAddress = new UdpAddress(transportAddress);

        UserTarget target = new UserTarget();
        target.setSecurityLevel(SecurityLevel.NOAUTH_NOPRIV);
        target.setVersion(SnmpConstants.version3);
        target.setAddress(targetAddress);
        
        iface.setAttribute(SNMPV3_TARGET_KEY, target);

        log.debug("initialize: interface: " + inetAddress.getHostAddress() + " initialized.");

        return;
    }

    public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg) {
        // Log4j category
        //
        Category log = ThreadCategory.getInstance(getClass());

        int status = SERVICE_UNAVAILABLE;
        InetAddress inetAddress = (InetAddress) iface.getAddress();
        SnmpSession session = null;

        // Retrieve this interface's SNMP peer object
        //
        UserTarget target = (UserTarget) iface.getAttribute(SNMPV3_TARGET_KEY);
        
        if (target == null)
            throw new RuntimeException("UserTarget object not available for interface " + inetAddress);

        int timeout = ParameterMap.getKeyedInteger(parameters, "timeout", (int) target.getTimeout());
        int retries = ParameterMap.getKeyedInteger(parameters, "retries", target.getRetries());
        int port = ParameterMap.getKeyedInteger(parameters, "port", DEFAULT_PORT);
        String oid = ParameterMap.getKeyedString(parameters, "oid", DEFAULT_OID);
        String operator = ParameterMap.getKeyedString(parameters, "operator", null);
        String operand = ParameterMap.getKeyedString(parameters, "operand", null);
        
        String uname = ParameterMap.getKeyedString(parameters, "security name", DEFAULT_SECURITY_NAME);

        target.setRetries(retries);
        target.setTimeout(timeout);
        target.setSecurityName(new OctetString(uname));

        if (log.isDebugEnabled())
            log.debug("poll: service= SNMP address= " + inetAddress.getHostAddress() + " port= " + port + " oid=" + oid + " timeout= " + timeout + " retries= " + retries + " operator = " + operator + " operand = " + operand);

        Snmp snmp = null;
        try {
            snmp = SnmpHelpers.createSnmpSession(new OctetString(uname));
            snmp.listen();
            PDU request = SnmpHelpers.createPDU(target);
            VariableBinding vb = new VariableBinding(new OID(DEFAULT_OID));
            request.add(vb);
            
            PDU response = null;
            ResponseEvent responseEvent;
            responseEvent = snmp.send(request, target);
            snmp.close();
            
            if (responseEvent.getResponse() != null) {
                status = SERVICE_AVAILABLE;
            } else {
                status = SERVICE_UNAVAILABLE;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }

}
