/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
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
final public class BgpSessionMonitor extends SnmpMonitorStrategy {

    public static final Logger LOG = LoggerFactory.getLogger(BgpSessionMonitor.class);

    /**
     * Default base OID for the table that represents the BGP sessions
     */
    private static final String BGP_BASE_OID_DEFAULT = ".1.3.6.1.2.1.15";
    /**
     * Suffix for the table that represents the BGP-peer states.
     */
    private static final String BGP_PEER_STATE_OID_SUFFIX = ".3.1.2";
    /**
     * Suffix for the table that represents the BGP-peer admin states.
     */
    private static final String BGP_PEER_ADMIN_STATE_OID_SUFFIX = ".3.1.3";
    /**
     * Suffix for the table that represents the BGP-peer remote AS number.
     */
    private static final String BGP_PEER_REMOTEAS_SUFFIX = ".3.1.9";
    /**
     * Suffix for the table that represents the BGP-peer last error code.
     */
    private static final String BGP_PEER_LAST_ERROR_SUFFIX = ".3.1.14";
    /**
     * Suffix for the table that represents the BGP-peer established time.
     */
    private static final String BGP_PEER_FSM_EST_TIME_SUFFIX = ".3.1.16";

    static class BgpOids {
        final String bgpBaseOid;
        /**
         * OID for the table that represents the BGP-peer states.
         */
        final String bgpPeerStateOid;
        /**
         * OID for the table that represents the BGP-peer admin states.
         */
        final String bgpPeerAdminStateOid;
        /**
         * OID for the table that represents the BGP-peer remote AS number.
         */
        final String bgpPeerRemoteAsOid;
        /**
         * OID for the table that represents the BGP-peer last error code.
         */
        final String bgpPeerLastErrorOid;
        /**
         * OID for the table that represents the BGP-peer established time.
         */
        final String bgpPeerFsmEstTimeOid;

        public BgpOids(final Map<String, Object> parameters) {
            this.bgpBaseOid = ParameterMap.getKeyedString(parameters, "bgpBaseOid", BGP_BASE_OID_DEFAULT);
            this.bgpPeerStateOid = ParameterMap.getKeyedString(parameters, "bgpPeerStateOid", this.bgpBaseOid + BGP_PEER_STATE_OID_SUFFIX);
            this.bgpPeerAdminStateOid = ParameterMap.getKeyedString(parameters, "bgpPeerAdminStateOid", this.bgpBaseOid + BGP_PEER_ADMIN_STATE_OID_SUFFIX);
            this.bgpPeerRemoteAsOid = ParameterMap.getKeyedString(parameters, "bgpPeerRemoteAsOid", this.bgpBaseOid + BGP_PEER_REMOTEAS_SUFFIX);
            this.bgpPeerLastErrorOid = ParameterMap.getKeyedString(parameters, "bgpPeerLastErrorOid", this.bgpBaseOid + BGP_PEER_LAST_ERROR_SUFFIX);
            this.bgpPeerFsmEstTimeOid = ParameterMap.getKeyedString(parameters, "bgpPeerFsmEstTimeOid", this.bgpBaseOid + BGP_PEER_FSM_EST_TIME_SUFFIX);
        }
    }

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
        final BgpOids bgpOids = new BgpOids(parameters);

        String returnValue = "";

        PollStatus status = PollStatus.unavailable();
        InetAddress ipaddr = svc.getAddress();

        // Initialize the messages if the session is down
        String adminStateMsg = "N/A";
        String peerStateMsg = "N/A";
        String remoteAsMsg = "N/A";
        String lastErrorMsg = "N/A";
        String estTimeMsg = "N/A";
        
        // Retrieve this interface's SNMP peer object
        //
        final SnmpAgentConfig agentConfig = getAgentConfig(svc, parameters);
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
            SnmpObjId bgpPeerStateSnmpObject = SnmpObjId.get(bgpOids.bgpPeerStateOid + "." + bgpPeerIp);
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
            SnmpObjId bgpPeerAdminStateSnmpObject = SnmpObjId.get(bgpOids.bgpPeerAdminStateOid + "." + bgpPeerIp);
            SnmpValue bgpPeerAdminState = SnmpUtils.get(agentConfig, bgpPeerAdminStateSnmpObject);
            // Check correct MIB-Support
            if (bgpPeerAdminState == null)
            {
                LOG.warn("Cannot receive bgpAdminState");
            } else {
                LOG.debug("poll: bgpPeerAdminState: {}", bgpPeerAdminState);
                adminStateMsg = resolveAdminState(bgpPeerAdminState.toInt());
            }
            
            SnmpObjId bgpPeerRemoteAsSnmpObject = SnmpObjId.get(bgpOids.bgpPeerRemoteAsOid + "." + bgpPeerIp);
            SnmpValue bgpPeerRemoteAs = SnmpUtils.get(agentConfig, bgpPeerRemoteAsSnmpObject);
            // Check correct MIB-Support
            if (bgpPeerRemoteAs == null)
            {
                LOG.warn("Cannot receive bgpPeerRemoteAs");
            } else {
                LOG.debug("poll: bgpPeerRemoteAs: {}", bgpPeerRemoteAs);
                remoteAsMsg = bgpPeerRemoteAs.toString();
            }

            SnmpObjId bgpPeerLastErrorSnmpObject = SnmpObjId.get(bgpOids.bgpPeerLastErrorOid + "." + bgpPeerIp);
            SnmpValue bgpPeerLastError = SnmpUtils.get(agentConfig, bgpPeerLastErrorSnmpObject);
            // Check correct MIB-Support
            if (bgpPeerLastError == null)
            {
                LOG.warn("Cannot receive bgpPeerLastError");
            } else {
                LOG.debug("poll: bgpPeerLastError: {}", bgpPeerLastError);
                lastErrorMsg = resolveBgpErrorCode(bgpPeerLastError.toHexString());
            }
            
            SnmpObjId bgpPeerFsmEstTimeSnmpObject = SnmpObjId.get(bgpOids.bgpPeerFsmEstTimeOid + "." + bgpPeerIp);
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
