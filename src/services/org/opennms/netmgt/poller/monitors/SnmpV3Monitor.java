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
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.utils.SnmpHelpers;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.UserTarget;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.VariableBinding;

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

        Target target = SnmpPeerFactory.getInstance().getTarget(inetAddress);
        
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

        // Retrieve this interface's SNMP peer object
        //
        UserTarget target = (UserTarget) iface.getAttribute(SNMPV3_TARGET_KEY);
        
        if (target == null)
            throw new RuntimeException("UserTarget object not available for interface " + inetAddress);

        String oid = ParameterMap.getKeyedString(parameters, "oid", DEFAULT_OID);
        String operator = ParameterMap.getKeyedString(parameters, "operator", null);
        String operand = ParameterMap.getKeyedString(parameters, "operand", null);

        //Need this for logging only
        TransportIpAddress address = (TransportIpAddress)target.getAddress();
        
        if (log.isDebugEnabled())
            log.debug("poll: service= SNMP address= " + inetAddress.getHostAddress() + " port= " + address.getPort() + " oid=" + oid + " timeout= " + target.getTimeout() + " retries= " + target.getRetries() + " operator = " + operator + " operand = " + operand);

        Snmp snmp = null;
        try {
            snmp = SnmpHelpers.createSnmpSession();
            snmp.listen();
            PDU request = SnmpHelpers.createPDU();
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
