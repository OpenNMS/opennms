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
import org.opennms.netmgt.snmp.snmp4j.SnmpHelpers;
import org.opennms.netmgt.utils.ParameterMap;
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

    private static Category m_log; 
    private static final String SERVICE_NAME = "SNMP";
    private static final String DEFAULT_OID = ".1.3.6.1.2.1.1.2.0"; 
    private static final String SNMPV3_TARGET_KEY = "org.snmp4j.UserTarget";
    private static final OID USM_STATS = new OID(new int[]{1, 3, 6, 1, 6, 3, 15, 1, 1});
    private static final OID SNMP_MPD_STATS = new OID(new int[]{1, 3, 6, 1, 6, 3, 11, 2, 1});

    public SnmpV3Monitor() {
        m_log = ThreadCategory.getInstance(getClass());
    }
    
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

        if (iface.getType() != NetworkInterface.TYPE_IPV4)
            throw new RuntimeException("Unsupported interface type, only TYPE_IPV4 currently supported");
        if (m_log.isDebugEnabled())
            m_log.debug("initialize: setting SNMP target attributes for this interface: " + ((InetAddress) iface.getAddress()).getHostAddress());

        Target target = null;
        if (requestedVersion != -1)
            target = SnmpPeerFactory.getInstance().getTarget((InetAddress) iface.getAddress(), requestedVersion);
        else
            target = SnmpPeerFactory.getInstance().getTarget((InetAddress) iface.getAddress());
        
        iface.setAttribute(SNMPV3_TARGET_KEY, target);

        if (m_log.isDebugEnabled())
            m_log.debug("initialize: interface: " + ((InetAddress) iface.getAddress()).getHostAddress() + " initialized.");

        return;
    }

    public int poll(NetworkInterface iface, Map parameters, org.opennms.netmgt.config.poller.Package pkg) {

        int status = SERVICE_UNAVAILABLE;
        // Retrieve this interface's SNMP peer object
        //
        Target target = (Target)iface.getAttribute(SNMPV3_TARGET_KEY);
        
        if (target == null)
            throw new RuntimeException("Target object not available for interface " + ((InetAddress) iface.getAddress()));

        //Now poll
        Snmp snmp = null;
        try {
            snmp = SnmpHelpers.createSnmpSession();
            
            //This call is an SNMP4J helper that causes all registered transport mappings to listen 
            snmp.listen();
            PDU requestPDU = SnmpHelpers.createPDU(target.getVersion());
            requestPDU.setType(PDU.GET);
            VariableBinding vb = new VariableBinding(new OID(DEFAULT_OID));
            requestPDU.add(vb);
            
            if (m_log.isDebugEnabled()) {
                //Need this for logging only
                TransportIpAddress address = (TransportIpAddress)target.getAddress();
                m_log.debug("poll: service= SNMP address= " + ((InetAddress) iface.getAddress()).getHostAddress() + 
                        " port= " + address.getPort() + 
                        " oid=" + ParameterMap.getKeyedString(parameters, "oid", DEFAULT_OID) + 
                        " timeout= " + target.getTimeout() + " retries= " + target.getRetries() + 
                        " operator = " + ParameterMap.getKeyedString(parameters, "operator", null) + 
                        " operand = " + ParameterMap.getKeyedString(parameters, "operand", null));
            }

            ResponseEvent responseEvent = snmp.send(requestPDU, target);
            snmp.close();
            status = processResponseEvent(iface, parameters, target, responseEvent);
        } catch (IOException e) {
            m_log.error("SnmpV3Monitor:poll incurred an i/o Error: " +e.getMessage());
        }

        return status;
    }

    /**
     * @param iface
     * @param parameters
     * @param log
     * @param target
     * @param responseEvent
     * @return
     */
    private int processResponseEvent(NetworkInterface iface, Map parameters, Target target, ResponseEvent responseEvent) {
        int status = SERVICE_UNAVAILABLE;
        status = checkResponse(responseEvent, status);
        if (status == SERVICE_AVAILABLE) {
            if (m_log.isDebugEnabled())
                m_log.debug("poll: SNMP poll succeeded, addr=" + ((InetAddress) iface.getAddress()).getHostAddress() +
                        " oid=" + ParameterMap.getKeyedString(parameters, "oid", DEFAULT_OID) + 
                        " value=" + responseEvent.getResponse().toString());
            status = checkReponseCriteria(iface, parameters, responseEvent, status);
        } else
            if (m_log.isDebugEnabled())
                m_log.debug("poll: SNMPv3Monitor poll failed, addr=" + ((InetAddress) iface.getAddress()).getHostAddress() + 
                        " oid=" + ParameterMap.getKeyedString(parameters, "oid", DEFAULT_OID));
        
        return status;
    }

    /**
     * @param iface
     * @param parameters
     * @param responseEvent
     * @param log
     * @param currentStatus
     * @return
     */
    private int checkReponseCriteria(NetworkInterface iface, Map parameters, ResponseEvent responseEvent, int currentStatus) {
        Variable var = ((VariableBinding)responseEvent.getResponse().getVariableBindings().firstElement()).getVariable();
        if (currentStatus == SERVICE_AVAILABLE) {
            try {
                currentStatus = (meetsCriteria(var, ParameterMap.getKeyedString(parameters, "operator", null), ParameterMap.getKeyedString(parameters, "operand", null)) ? ServiceMonitor.SERVICE_AVAILABLE : ServiceMonitor.SERVICE_UNAVAILABLE);
            } catch (NumberFormatException e) {
                m_log.warn("Number operator used on a non-number " + e.getMessage());
                currentStatus = ServiceMonitor.SERVICE_AVAILABLE;
            } catch (IllegalArgumentException e) {
                m_log.warn("Invalid Snmp Criteria: " + e.getMessage());
                currentStatus = ServiceMonitor.SERVICE_UNAVAILABLE;
            }
        }
        return currentStatus;
    }

    /**
     * @param responseEvent
     * @param log
     * @param status
     * @return
     */
    private int checkResponse(ResponseEvent responseEvent, int status) {
        if (responseEvent.getResponse() != null) {
            if (responseEvent.getResponse().getErrorStatus() != 0) {
                m_log.error("SnmpV3Monitor: PDU reponse errorStatus > 0.  The errorStatus is: "+responseEvent.getResponse().getErrorStatus());
                status = SERVICE_UNAVAILABLE;
            } else {
                status = SERVICE_AVAILABLE;

                //got a valid SNMPv3 response from the agent, but the response may contain
                //v3 error codes in the first varbind
                if (responseEvent.getResponse().getVariableBindings() != null && responseEvent.getResponse().getVariableBindings().size() >0) {
                    
                    //this comparison is very v3 protocol specific.  This left most compare
                    //efficiently matches most all errors found in the first varbind.
                    if (USM_STATS.leftMostCompare(USM_STATS.size(), ((VariableBinding)responseEvent.getResponse().getVariableBindings().firstElement()).getOid()) == 0 || 
                            SNMP_MPD_STATS.leftMostCompare(SNMP_MPD_STATS.size(), ((VariableBinding)responseEvent.getResponse().getVariableBindings().firstElement()).getOid()) ==0 ) {
                        status = SERVICE_UNAVAILABLE;
                        m_log.error("SnmpV3Monitor: responseError: " +((VariableBinding)responseEvent.getResponse().getVariableBindings().firstElement()).getOid());
                    }
                }
            }
        }
        return status;
    }

}
