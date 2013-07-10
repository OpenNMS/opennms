/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Check for BgpPeering states via RFC1269-MIB.
 * </p>
 * <p>
 * This does SNMP and therefore relies on the SNMP configuration so it is not distributable.
 * </p>
 *
 * @author <A HREF="mailto:r.trommer@open-factory.org">Ronny Trommer</A>
 * @version $Id: $
 */
@Distributable(DistributionContext.DAEMON)
final public class BgpSessionMonitor extends SnmpMonitorStrategy {
    
    public static final Logger LOG = LoggerFactory.getLogger(BgpSessionMonitor.class);
    
    /**
     * Name of monitored service.
     */
    private static final String m_serviceName = "BGP_Session";
    
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
    
    /**
     * Implement the BGP Peer states
     */
    private enum BGP_PEER_STATE {
        IDLE(1), CONNECT(2), ACTIVE(3), OPEN_SENT(4), OPEN_CONFIRM(5), ESTABLISHED(
                6);

        private final int state; // state code

        BGP_PEER_STATE(int s) {
            this.state = s;
        }

        private int value() {
            return this.state;
        }
    };
    
    /**
     * Implement the BGP Peer admin states
     */
    private static enum BGP_PEER_ADMIN_STATE {
        STOP(1), START(2);
        private final int state; // state code

        BGP_PEER_ADMIN_STATE(int s) {
            this.state = s;
        }

        private int value() {
            return this.state;
        }
    };
    
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
     * {@inheritDoc}
     *
     * <P>
     * Initialize the service monitor.
     * </P>
     * @exception RuntimeException
     *                Thrown if an unrecoverable error occurs that prevents the
     *                plug-in from functioning.
     */
    @Override
    public void initialize(Map<String, Object> parameters) {
        // Initialize the SnmpPeerFactory
        //
        try {
            SnmpPeerFactory.init();
        } catch (IOException ex) {
            LOG.error("initialize: Failed to load SNMP configuration", ex);
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
     * @param svc a {@link org.opennms.netmgt.poller.MonitoredService} object.
     */
    @Override
    public void initialize(MonitoredService svc) {
        super.initialize(svc);
        return;
    }

    /**
     * {@inheritDoc}
     *
     * <P>
     * The poll() method is responsible for polling the specified address for
     * SNMP service availability.
     * </P>
     * @exception RuntimeException
     *                Thrown for any uncrecoverable errors.
     */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {   
        NetworkInterface<InetAddress> iface = svc.getNetInterface();
        
        String returnValue = "";

        PollStatus status = PollStatus.unavailable();
        InetAddress ipaddr = (InetAddress) iface.getAddress();
        
        // Initialize the messages if the session is down
        String adminStateMsg = "N/A";
        String peerStateMsg = "N/A";
        String remoteAsMsg = "N/A";
        String lastErrorMsg = "N/A";
        String estTimeMsg = "N/A";
        
        // Retrieve this interface's SNMP peer object
        //
        SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipaddr);
        if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);
        final String hostAddress = InetAddressUtils.str(ipaddr);
		LOG.debug("poll: setting SNMP peer attribute for interface {}", hostAddress);

        // Get configuration parameters
        //
        // This should never need to be overridden, but it can be in order to be used with similar tables.
        String bgpPeerIp = ParameterMap.getKeyedString(parameters, "bgpPeerIp", null);
        if (bgpPeerIp == null) {
            LOG.warn("poll: No BGP-Peer IP Defined! ");
            return status;
        }

        // set timeout and retries on SNMP peer object
        //
        agentConfig.setTimeout(ParameterMap.getKeyedInteger(parameters, "timeout", agentConfig.getTimeout()));
        agentConfig.setRetries(ParameterMap.getKeyedInteger(parameters, "retry", ParameterMap.getKeyedInteger(parameters, "retries", agentConfig.getRetries())));
        agentConfig.setPort(ParameterMap.getKeyedInteger(parameters, "port", agentConfig.getPort()));
            
        // Establish SNMP session with interface
        //
        try {
            LOG.debug("poll: SnmpAgentConfig address: {}", agentConfig);
    
            // Get the BGP peer state
            SnmpObjId bgpPeerStateSnmpObject = SnmpObjId.get(BGP_PEER_STATE_OID + "." + bgpPeerIp);
            SnmpValue bgpPeerState = SnmpUtils.get(agentConfig, bgpPeerStateSnmpObject);
            
            // If no peer state is received or SNMP is not possible, service is down
            if (bgpPeerState == null) {
                LOG.warn("No BGP peer state received!");
                return status;
            } else {
                LOG.debug("poll: bgpPeerState: {}", bgpPeerState);
                peerStateMsg = resolvePeerState(bgpPeerState.toInt());
            }
            
            /*
             *  Do no unnecessary SNMP requests, if peer state is up, return with 
             *  service available and go away.
             */
            if (bgpPeerState.toInt() == BGP_PEER_STATE.ESTABLISHED.value()) {
                LOG.debug("poll: bgpPeerState: {}", BGP_PEER_STATE.ESTABLISHED.name());
                return PollStatus.available();
            }
            
            // Peer state is not established gather some information
            SnmpObjId bgpPeerAdminStateSnmpObject = SnmpObjId.get(BGP_PEER_ADMIN_STATE_OID + "." + bgpPeerIp);
            SnmpValue bgpPeerAdminState = SnmpUtils.get(agentConfig, bgpPeerAdminStateSnmpObject);
            // Check correct MIB-Support
            if (bgpPeerAdminState == null)
            {
                LOG.warn("Cannot receive bgpAdminState");
            } else {
                LOG.debug("poll: bgpPeerAdminState: {}", bgpPeerAdminState);
                adminStateMsg = resolveAdminState(bgpPeerAdminState.toInt());
            }
            
            SnmpObjId bgpPeerRemoteAsSnmpObject = SnmpObjId.get(BGP_PEER_REMOTEAS_OID + "." + bgpPeerIp);
            SnmpValue bgpPeerRemoteAs = SnmpUtils.get(agentConfig, bgpPeerRemoteAsSnmpObject);
            // Check correct MIB-Support
            if (bgpPeerRemoteAs == null)
            {
                LOG.warn("Cannot receive bgpPeerRemoteAs");
            } else {
                LOG.debug("poll: bgpPeerRemoteAs: {}", bgpPeerRemoteAs);
                remoteAsMsg = bgpPeerRemoteAs.toString();
            }

            SnmpObjId bgpPeerLastErrorSnmpObject = SnmpObjId.get(BGP_PEER_LAST_ERROR_OID + "." + bgpPeerIp);
            SnmpValue bgpPeerLastError = SnmpUtils.get(agentConfig, bgpPeerLastErrorSnmpObject);
            // Check correct MIB-Support
            if (bgpPeerLastError == null)
            {
                LOG.warn("Cannot receive bgpPeerLastError");
            } else {
                LOG.debug("poll: bgpPeerLastError: {}", bgpPeerLastError);
                lastErrorMsg = resolveBgpErrorCode(bgpPeerLastError.toHexString());
            }
            
            SnmpObjId bgpPeerFsmEstTimeSnmpObject = SnmpObjId.get(BGP_PEER_FSM_EST_TIME_OID + "." + bgpPeerIp);
            SnmpValue bgpPeerFsmEstTime = SnmpUtils.get(agentConfig, bgpPeerFsmEstTimeSnmpObject);
            // Check correct MIB-Support
            if (bgpPeerFsmEstTime == null)
            {
                LOG.warn("Cannot receive bgpPeerFsmEstTime");
            } else {
                LOG.debug("poll: bgpPeerFsmEsmTime: {}", bgpPeerFsmEstTime);
                estTimeMsg = bgpPeerFsmEstTime.toString();
            }
            
            returnValue = "BGP Session state to AS-" + remoteAsMsg
                + " via " + bgpPeerIp + " is " + peerStateMsg + "! Last peer "
                +"error message is " + lastErrorMsg + ". BGP admin state is "
                + adminStateMsg + ". BGP Session established time: " 
                + estTimeMsg;
            // Set service down and return gathered information            
            status = PollStatus.unavailable(returnValue);
                
        } catch (NullPointerException e) {
            String reason = "Unexpected error during SNMP poll of interface " + hostAddress;
            LOG.debug(reason, e);
            status = PollStatus.unavailable(reason);
        } catch (NumberFormatException e) {
            String reason = "Number operator used on a non-number " + e.getMessage();
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        } catch (IllegalArgumentException e) {
            String reason = "Invalid SNMP Criteria: " + e.getMessage();
            LOG.debug(reason);
            status = PollStatus.unavailable(reason);
        } catch (Throwable t) {
            String reason = "Unexpected exception during SNMP poll of interface " + hostAddress;
            LOG.debug(reason, t);
            status = PollStatus.unavailable(reason);
        }

        // If matchAll is set to true, then the status is set to available above with a single match.
        // Otherwise, the service will be unavailable.
        return status;
    }
    
    /**
     * Method to convert BGP Error codes in plain text messages.
     * 
     * @param bgpCode
     *            BGP Hex code
     * @return Plain text error message
     */
    private String resolveBgpErrorCode (String bgpCode) {
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
    
    /**
     * Method to resolve a given peer state to human readable string. TODO:
     * Check if there is a better way to resolve the states backward
     * 
     * @param sc
     *            BGP-Peer admin state code
     * @return Human readable BGP peer admin state
     */
    private String resolveAdminState(int sc) {
        String name = "UNKNOWN";
        if (BGP_PEER_ADMIN_STATE.STOP.value() == sc)
            name = BGP_PEER_ADMIN_STATE.STOP.name();
        if (BGP_PEER_ADMIN_STATE.START.value() == sc)
            name = BGP_PEER_ADMIN_STATE.START.name();
        return name;
    }
    
    /**
     * Method to resolve a given peer state to human readable string. TODO:
     * Check if there is a better way to resolve the states backward
     * 
     * @param sc
     *            BGP-Peer state code
     * @return Human readable BGP peer state
     */
    private String resolvePeerState(int sc) {
        String name = "UNKNOWN";
        if (BGP_PEER_STATE.IDLE.value() == sc)
            name = BGP_PEER_STATE.IDLE.name();
        if (BGP_PEER_STATE.CONNECT.value() == sc)
            name = BGP_PEER_STATE.CONNECT.name();
        if (BGP_PEER_STATE.ACTIVE.value() == sc)
            name = BGP_PEER_STATE.ACTIVE.name();
        if (BGP_PEER_STATE.OPEN_SENT.value() == sc)
            name = BGP_PEER_STATE.OPEN_SENT.name();
        if (BGP_PEER_STATE.OPEN_CONFIRM.value() == sc)
            name = BGP_PEER_STATE.OPEN_CONFIRM.name();
        if (BGP_PEER_STATE.ESTABLISHED.value() == sc)
            name = BGP_PEER_STATE.ESTABLISHED.name();
        return name;
    }
}
