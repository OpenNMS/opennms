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
import java.util.Vector;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.utils.ParameterMap;
import org.opennms.netmgt.utils.SnmpHelpers;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.TransportIpAddress;
import org.snmp4j.smi.Variable;
import org.snmp4j.smi.VariableBinding;

/**
 * <P>
 * Used by the Poller to monitor status of a v1, v2c, or v3 agent.
 * </P>
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
final public class SnmpV3Monitor extends SnmpMonitorStrategy {

    private static final String SERVICE_NAME = "SNMP";

    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0"; 

    private static final String SNMPV3_TARGET_KEY = "org.snmp4j.UserTarget";
    
    private static final OID USM_STATS = new OID(new int[]{1, 3, 6, 1, 6, 3, 15, 1, 1});

    private static final OID SNMP_MPD_STATS = new OID(new int[]{1, 3, 6, 1, 6, 3, 11, 2, 1});

    public String serviceName() {
        return SERVICE_NAME;
    }
    
    /**
     * This helper method was added to maintain compatibility while adding support
     * for the new overloaded method that takes requested verion of SNMP in this
     * method.  Right now, support for this was added only to simplify JUnit testing.
     */
    public void initialize(NetworkInterface iface) {
        initialize(iface, -1);
    }

    /**
     * Currently, this implmentation is only called directly from JUnit tests
     * and the overloaded partner method that is called by the poller.
     * @param iface
     * @param requestedVersion
     */
    public void initialize(NetworkInterface iface, int requestedVersion) {
        Category log = ThreadCategory.getInstance(getClass());

        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new RuntimeException("Unsupported interface type, only TYPE_IPV4 currently supported");
        InetAddress inetAddress = (InetAddress) iface.getAddress();

        if (log.isDebugEnabled())
            log.debug("initialize: setting SNMP target attributes for this interface: " + inetAddress.getHostAddress());

        Target target = null;
        if (requestedVersion != -1)
            target = SnmpPeerFactory.getInstance().getTarget(inetAddress, requestedVersion);
        else
            target = SnmpPeerFactory.getInstance().getTarget(inetAddress);
        
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
        Target target = (Target)iface.getAttribute(SNMPV3_TARGET_KEY);
        
        if (target == null)
            throw new RuntimeException("Target object not available for interface " + inetAddress);

        String oid = ParameterMap.getKeyedString(parameters, "oid", DEFAULT_OID);
        String operator = ParameterMap.getKeyedString(parameters, "operator", null);
        String operand = ParameterMap.getKeyedString(parameters, "operand", null);

        //Need this for logging only
        TransportIpAddress address = (TransportIpAddress)target.getAddress();
        
        if (log.isDebugEnabled())
            log.debug("poll: service= SNMP address= " + inetAddress.getHostAddress() + " port= " + address.getPort() + " oid=" + oid + " timeout= " + target.getTimeout() + " retries= " + target.getRetries() + " operator = " + operator + " operand = " + operand);

        //Now poll
        Snmp snmp = null;
        try {
            snmp = SnmpHelpers.createSnmpSession();
            snmp.listen();
            PDU request = SnmpHelpers.createPDU(target.getVersion());
            VariableBinding vb = new VariableBinding(new OID(DEFAULT_OID));
            request.add(vb);
            
            PDU response = null;
            ResponseEvent responseEvent = snmp.send(request, target);
            snmp.close();

            Vector vbs = responseEvent.getResponse().getVariableBindings();
            VariableBinding firstVB = (VariableBinding)vbs.firstElement();
            status = SERVICE_UNAVAILABLE;
            if (responseEvent.getResponse() != null) {
                if (responseEvent.getResponse().getErrorStatus() != 0) {
                    log.error("SnmpV3Monitor: PDU reponse errorStatus > 0.  The errorStatus is: "+responseEvent.getResponse().getErrorStatus());
                    status = SERVICE_UNAVAILABLE;
                } else {
                    status = SERVICE_AVAILABLE;

                    //got a valid SNMPv3 response from the agent, but the response may contain
                    //v3 error codes in the first varbind
                    if (vbs != null && vbs.size() >0) {
                        
                        //this comparison is very v3 protocol specific.  This left most compare
                        //efficiently matches most all errors found in the first varbind.
                        if (USM_STATS.leftMostCompare(USM_STATS.size(), firstVB.getOid()) == 0 || 
                                SNMP_MPD_STATS.leftMostCompare(SNMP_MPD_STATS.size(), firstVB.getOid()) ==0 ) {
                            status = SERVICE_UNAVAILABLE;
                            log.error("SnmpV3Monitor: responseError: " +firstVB.getOid());
                        }
                    }
                }
            }
            
            String hostAddress = inetAddress.getHostAddress();
            Variable var = firstVB.getVariable();

            if (status == SERVICE_AVAILABLE) {
                log.debug("poll: SNMP poll succeeded, addr=" + hostAddress + " oid=" + oid + " value=" + responseEvent.getResponse().toString());
                try {
                    status = (meetsCriteria(var, operator, operand) ? ServiceMonitor.SERVICE_AVAILABLE : ServiceMonitor.SERVICE_UNAVAILABLE);
                } catch (NumberFormatException e) {
                    log.warn("Number operator used on a non-number " + e.getMessage());
                    status = ServiceMonitor.SERVICE_AVAILABLE;
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid Snmp Criteria: " + e.getMessage());
                    status = ServiceMonitor.SERVICE_UNAVAILABLE;
                }
            } else {
                log.debug("poll: SNMPv3Monitor poll failed, addr=" + hostAddress + " oid=" + oid);
                status = ServiceMonitor.SERVICE_UNAVAILABLE;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }

        return status;
    }

}
