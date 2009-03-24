/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 * Tab Size = 8
 */

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.DistributionContext;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;

/**
 * <p>
 * Check for BgpPeering states via RFC1269-MIB.
 * </p>
 * 
 * @author <A HREF="mailto:r.trommer@open-factory.org">Ronny Trommer</A>
 */

//this does snmp and there relies on the snmp configuration so it is not distributable
@Distributable(DistributionContext.DAEMON)
final public class BgpSessionMonitor extends SnmpMonitorStrategy {
    /**
     * Name of monitored service.
     */
    private static final String m_serviceName = "BGP-Session-Monitor";
    
    /**
     * Interface attribute key used to store the interface's SnmpAgentConfig
     * object.
     */
    static final String SNMP_AGENTCONFIG_KEY = "org.opennms.netmgt.snmp.SnmpAgentConfig";
    
    /**
     * Default OID for the table that represents the BGP-peer states.
     */
    private static final String BGP_PEER_STATE_OID = ".1.3.6.1.2.1.15.3.1.2";
    
    /**
     * Default OID for the table that represents the BGP-peer admin states.
     */
    private static final String BGP_PEER_ADMIN_STATE_OID = ".1.3.6.1.2.1.15.3.1.3";
    
    /**
     * Default OID for the table that represents the BGP-peer remote AS number.
     */
    private static final String BGP_PEER_REMOTEAS_OID = ".1.3.6.1.2.1.15.3.1.9";
    
    /**
     * Default OID for the table that represents the BGP-peer last error code.
     */
    private static final String BGP_PEER_LAST_ERROR_OID = ".1.3.6.1.2.1.15.3.1.14";
    
    /**
     * Default OID for the table that represents the BGP-peer established time.
     */
    private static final String BGP_PEER_FSM_EST_TIME_OID = ".1.3.6.1.2.1.15.3.1.16";
    
    /*
     * "1" => "Idle",
     * "2" => "Connect",
     * "3" => "Active",
     * "4" => "OpenSent",
     * "5" => "OpenConfirm",
     * "6" => "Established"
     */
    private static final int BGP_STATE_ESTABLISHED = 6;

    
    /**
     * <P>
     * Returns the name of the service that the plug-in monitors 
     * ("BGP-SessionMonitor").
     * </P>
     * 
     * @return The service that the plug-in monitors.
     */
    public String serviceName() {
        return m_serviceName;
    }

    /**
     * <P>
     * Initialize the service monitor.
     * </P>
     * @param parameters
     *            Not currently used.
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     * 
     */
    public void initialize(Map parameters) {
        // Initialize the SnmpPeerFactory
        //
        try {
            SnmpPeerFactory.init();
        } catch (MarshalException ex) {
        	log().fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (ValidationException ex) {
        	log().fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        } catch (IOException ex) {
        	log().fatal("initialize: Failed to load SNMP configuration", ex);
            throw new UndeclaredThrowableException(ex);
        }

        return;
    }

    /**
     * <P>
     * Called by the poller framework when an interface is being added to the
     * scheduler. Here we perform any necessary initialization to prepare the
     * NetworkInterface object for polling.
     * </P>
     * 
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                interface from being monitored.
     */
    public void initialize(MonitoredService svc) {
        NetworkInterface iface = svc.getNetInterface();
        // Log4j category
        //
        // Get interface address from NetworkInterface
        //
        super.initialize(svc);

        InetAddress ipAddr = (InetAddress) iface.getAddress();

        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipAddr);
        if (log().isDebugEnabled()) {
            log().debug("initialize: SnmpAgentConfig address: " + agentConfig);
        }

        // Add the snmp config object as an attribute of the interface
        //
        if (log().isDebugEnabled())
            log().debug("initialize: BgpSessionMonitor setting SNMP peer attribute for interface " + ipAddr.getHostAddress());

        iface.setAttribute(SNMP_AGENTCONFIG_KEY, agentConfig);

        log().debug("initialize: interface: " + agentConfig.getAddress() + " initialized.");

        return;
    }

    /**
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * @param parameters
     *            The package parameters (timeout, retry, etc...) to be used for
     *            this poll.
     * @param iface
     *            The network interface to test the service on.
     * @return The availability of the interface and if a transition event
     *         should be supressed.
     * 
     * @exception RuntimeException
     *                Thrown for any uncrecoverable errors.
     */
    public PollStatus poll(MonitoredService svc, Map parameters) {   
        NetworkInterface iface = svc.getNetInterface();
        
        String returnValue = new String ();

        PollStatus status = PollStatus.unavailable();
        InetAddress ipaddr = (InetAddress) iface.getAddress();
        
        // Retrieve this interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = (SnmpAgentConfig) iface.getAttribute(SNMP_AGENTCONFIG_KEY);
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);

        // Get configuration parameters
        //
        // This should never need to be overridden, but it can be in order to be used with similar tables.
        String bgpPeerIp = ParameterMap.getKeyedString(parameters, "bgpPeerIp", null);

        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));
            
        // Establish SNMP session with interface
        //
        try {
            if (log().isDebugEnabled()) {
                log().debug("BgpSessionMonitor.poll: SnmpAgentConfig address: " +agentConfig);
            }

            if (bgpPeerIp == null) {
                log().warn("BgpSessionMonitor.poll: No BGP-Peer IP Defined! ");
                return status;
            }
    
            // This returns two maps: one of instance and service name, and one of instance and status.
            SnmpObjId bgpPeerStateSnmpObject = SnmpObjId.get(BGP_PEER_STATE_OID + "." + bgpPeerIp);
            SnmpValue bgpPeerState = SnmpUtils.get(agentConfig, bgpPeerStateSnmpObject);
            if (log().isDebugEnabled()) {
                log().debug("BgpSessionMonitor.poll: bgpPeerState: " + bgpPeerState);
            }
            
            SnmpObjId bgpPeerAdminStateSnmpObject = SnmpObjId.get(BGP_PEER_ADMIN_STATE_OID + "." + bgpPeerIp);
            SnmpValue bgpPeerAdminState = SnmpUtils.get(agentConfig, bgpPeerAdminStateSnmpObject);
            if (log().isDebugEnabled()) {
                log().debug("BgpSessionMonitor.poll: bgpPeerAdminState: " + bgpPeerAdminState);
            }
            
            SnmpObjId bgpPeerRemoteAsSnmpObject = SnmpObjId.get(BGP_PEER_REMOTEAS_OID + "." + bgpPeerIp);
            SnmpValue bgpPeerRemoteAs = SnmpUtils.get(agentConfig, bgpPeerRemoteAsSnmpObject);
            if (log().isDebugEnabled()) {
                log().debug("BgpSessionMonitor.poll: bgpPeerRemoteAs: " + bgpPeerRemoteAs);
            }

            SnmpObjId bgpPeerLastErrorSnmpObject = SnmpObjId.get(BGP_PEER_LAST_ERROR_OID + "." + bgpPeerIp);
            SnmpValue bgpPeerLastError = SnmpUtils.get(agentConfig, bgpPeerLastErrorSnmpObject);
            if (log().isDebugEnabled()) {
                log().debug("BgpSessionMonitor.poll: bgpPeerLastError: " + bgpPeerLastError.toHexString() + 
                    " - decoded: " + resolveBgpErrorCode(bgpPeerLastError.toHexString()));
            }
            
            SnmpObjId bgpPeerFsmEstTimeSnmpObject = SnmpObjId.get(BGP_PEER_FSM_EST_TIME_OID + "." + bgpPeerIp);
            SnmpValue bgpPeerFsmEstTime = SnmpUtils.get(agentConfig, bgpPeerFsmEstTimeSnmpObject);
            if (log().isDebugEnabled()) {
                log().debug("BgpSessionMonitor.poll: bgpPeerFsmEstTime: " + bgpPeerFsmEstTime);
            }
            
            if (bgpPeerState == null || bgpPeerState.isNull()) {
                log().debug("BgpSessionMonitor poll failed: no results, addr=" + ipaddr.getHostAddress() + " oid=" + bgpPeerStateSnmpObject);
                returnValue = "No result for " + bgpPeerIp;
                status = PollStatus.unavailable(returnValue);
            }
            
            if (bgpPeerState.toInt() != BGP_STATE_ESTABLISHED) {
                returnValue = "BGP Session to AS" + bgpPeerRemoteAs + 
                " via " + bgpPeerIp + " is not established! Last peer " +
                "error message is " + resolveBgpErrorCode(bgpPeerLastError.toHexString()) + 
                ". BGP admin state is " + bgpPeerAdminState + ". BGP Session established time: "
                + bgpPeerFsmEstTime;
                status = PollStatus.unavailable(returnValue);
            }
            
            if (bgpPeerState.toInt() == BGP_STATE_ESTABLISHED) {
                status = PollStatus.available();
            }
            
        } catch (NumberFormatException e) {
            status = logDown(Level.ERROR, "Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            status = logDown(Level.ERROR, "Invalid Snmp Criteria: " + e.getMessage());
        } catch (Throwable t) {
            status = logDown(Level.WARN, "Unexpected exception during SNMP poll of interface " + ipaddr.getHostAddress(), t);
        }

        // If matchAll is set to true, then the status is set to available above with a single match.
        // Otherwise, the service will be unavailable.
        return status;
    }
    
    public String resolveBgpErrorCode (String bgpCode) {
        String clearCode = "unknown error";
        HashMap<String, String> codeMap = new HashMap<String, String> ();
        codeMap.put("0100", "Message Header Error");
        codeMap.put("0101", "Message Header Error - Connection Not Synchronized");
        codeMap.put("0102", "Message Header Error - Bad Message Length");
        codeMap.put("0103", "Message Header Error - Bad Message Type");
        codeMap.put("0200", "OPEN Message Error");
        codeMap.put("0201", "OPEN Message Error - Unsupported Version Number");
        codeMap.put("0202", "OPEN Message Error - Bad Peer AS");
        codeMap.put("0203", "OPEN Message Error - Bad BGP Identifier");
        codeMap.put("0204", "OPEN Message Error - Unsupported Optional Parameter");
        codeMap.put("0205", "OPEN Message Error (deprecated)");
        codeMap.put("0206", "OPEN Message Error - Unacceptable Hold Time");
        codeMap.put("0300", "UPDATE Message Error");
        codeMap.put("0301", "UPDATE Message Error - Malformed Attribute List");
        codeMap.put("0302", "UPDATE Message Error - Unrecognized Well-known Attribute");
        codeMap.put("0303", "UPDATE Message Error - Missing Well-known Attribute");
        codeMap.put("0304", "UPDATE Message Error - Attribute Flags Error");
        codeMap.put("0305", "UPDATE Message Error - Attribute Length Error");
        codeMap.put("0306", "UPDATE Message Error - Invalid ORIGIN Attribute");
        codeMap.put("0307", "UPDATE Message Error (deprecated)");
        codeMap.put("0308", "UPDATE Message Error - Invalid NEXT_HOP Attribute");
        codeMap.put("0309", "UPDATE Message Error - Optional Attribute Error");
        codeMap.put("030A", "UPDATE Message Error - Invalid Network Field");
        codeMap.put("030B", "UPDATE Message Error - Malformed AS_PATH");
        codeMap.put("0400", "Hold Timer Expired");
        codeMap.put("0500", "Finite State Machine Error");
        codeMap.put("0600", "Cease");
        codeMap.put("0601", "Cease - Maximum Number of Prefixes Reached");
        codeMap.put("0602", "Cease - Administrative Shutdown");
        codeMap.put("0603", "Cease - Peer De-configured");
        codeMap.put("0604", "Cease - Administrative Reset");
        codeMap.put("0605", "Cease - Connection Rejected");
        codeMap.put("0606", "Cease - Other Configuration Change");
        codeMap.put("0607", "Cease - Connection Collision Resolution");
        codeMap.put("0608", "Cease - Out of Resources");
        
        if (codeMap.containsKey(bgpCode))
        {
            clearCode = codeMap.get(bgpCode);
        }
        
        return clearCode;
    }
}
