/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2009 Mar 24: Created file. - r.trommer@open-factory.org
 * 2009 Jun 28: Fix: Bugzilla #3198 Catch undecleared exception
 *              Added: Do not detect administratively STOP sessions
 *              Refactor: Implement peer states and admin states as enum
 *              - r.trommer@open-factory.org 
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
 */

package org.opennms.netmgt.capsd.plugins;

import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.utils.ParameterMap;

/**
 * <p>
 * This class is used to test if BGP Sessions for a specific peer is
 * available. Check for BgpSession via RFC1269-MIB.
 * </p>
 *
 * @author <A HREF="mailto:r.trommer@open-factory.org">Ronny Trommer </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * @author <A HREF="mailto:r.trommer@open-factory.org">Ronny Trommer </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS </A>
 * @version $Id: $
 */
public final class BgpSessionPlugin extends SnmpPlugin {
    /**
     * Name of monitored service.
     */
    private static final String PROTOCOL_NAME = "BGP_Session";

    /**
     * Default OID for the table that represents the BGP-peer states.
     */
    private static final String BGP_PEER_STATE_OID = ".1.3.6.1.2.1.15.3.1.2";
    
    /**
     * Default OID for the table that represents the BGP-peer admin states.
     */
    private static final String BGP_PEER_ADMIN_STATE_OID = ".1.3.6.1.2.1.15.3.1.3";

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
     * Returns the name of the protocol that this plugin checks on the target
     * system for support.
     *
     * @return The protocol name for this plugin.
     */
    public String getProtocolName() {
        return PROTOCOL_NAME;
    }

    /**
     * {@inheritDoc}
     *
     * Returns true if the protocol defined by this plugin is supported. If
     * the protocol is not supported then a false value is returned to the
     * caller. The qualifier map passed to the method is used by the plugin to
     * return additional information by key-name. These key-value pairs can be
     * added to service events if needed.
     */
    public boolean isProtocolSupported(InetAddress ipaddr,
            Map<String, Object> qualifiers) {
        try {
            String bgpPeerIp = ParameterMap.getKeyedString(qualifiers,"bgpPeerIp", null);
            
            // If no parameter for bgpPeerIp, do not detect the protocol and quit
            if (bgpPeerIp == null) {
                log().warn("poll: No BGP-Peer IP Defined! ");
                return false;
            }
            
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(ipaddr);
            if (agentConfig == null) throw new RuntimeException("SnmpAgentConfig object not available for interface " + ipaddr);
            
            if (qualifiers != null) {
                // "port" parm
                //
                if (qualifiers.get("port") != null) {
                    int port = ParameterMap.getKeyedInteger(qualifiers,"port",agentConfig.getPort());
                    agentConfig.setPort(port);
                }

                // "timeout" parm
                //
                if (qualifiers.get("timeout") != null) {
                    int timeout = ParameterMap.getKeyedInteger(qualifiers,"timeout",agentConfig.getTimeout());
                    agentConfig.setTimeout(timeout);
                }

                // "retry" parm
                //
                if (qualifiers.get("retry") != null) {
                    int retry = ParameterMap.getKeyedInteger(qualifiers,"retry",agentConfig.getRetries());
                    agentConfig.setRetries(retry);
                }

                // "force version" parm
                //
                if (qualifiers.get("force version") != null) {
                    String version = (String) qualifiers.get("force version");
                    if (version.equalsIgnoreCase("snmpv1"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION1);
                    else if (version.equalsIgnoreCase("snmpv2")
                            || version.equalsIgnoreCase("snmpv2c"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION2C);

                    // TODO: make sure JoeSnmpStrategy correctly handles this.
                    else if (version.equalsIgnoreCase("snmpv3"))
                        agentConfig.setVersion(SnmpAgentConfig.VERSION3);
                }

                // Get the BGP admin state
                SnmpObjId bgpPeerAdminStateSnmpObject = SnmpObjId.get(BGP_PEER_ADMIN_STATE_OID + "." + bgpPeerIp);
                SnmpValue bgpPeerAdminState = SnmpUtils.get(agentConfig, bgpPeerAdminStateSnmpObject);
                
                // If no admin state received, do not detect the protocol and quit
                if (bgpPeerAdminState == null)
                {
                    log().warn("Cannot receive bgpAdminState");
                    return false;
                } else {
                    if (log().isDebugEnabled()) {
                        log().debug("poll: bgpPeerAdminState: " + bgpPeerAdminState);
                    }
                }
                
                // If BGP peer session administratively STOP do not detect
                if  (Integer.parseInt(bgpPeerAdminState.toString()) != BGP_PEER_ADMIN_STATE.START.value())
                {
                    return false;
                }
                
                // BGP peer session is administratively START check valid state
                SnmpObjId bgpPeerStateSnmpObject = SnmpObjId.get(BGP_PEER_STATE_OID + "." + bgpPeerIp);
                SnmpValue bgpPeerState = SnmpUtils.get(agentConfig, bgpPeerStateSnmpObject);
                
                // If no peer state is received or SNMP is not possible, do not detect and quit
                if (bgpPeerState == null) {
                    log().warn("No BGP peer state received!");
                    return false;
                } else {
                    if (log().isDebugEnabled()) {
                        log().debug("poll: bgpPeerState: " + bgpPeerState);
                    }
                }

                // Validate sessions, check state is somewhere between IDLE and ESTABLISHED
                if  (Integer.parseInt(bgpPeerState.toString()) >= BGP_PEER_STATE.IDLE.value() && 
                    Integer.parseInt(bgpPeerState.toString()) <= BGP_PEER_STATE.ESTABLISHED.value())
                {
                    // Session detected
                    if (log().isDebugEnabled()) {
                        log().debug("poll: bgpPeerState: "
                                    + bgpPeerState
                                    + " is valid, protocol supported.");
                    }
                    return true;
                }
            }
        } catch (NullPointerException e) {
            log().warn("SNMP not available or RFC1269-MIB not supported!");
        } catch (NumberFormatException e) {
            log().warn("Number operator used on a non-number " + e.getMessage());
        } catch (IllegalArgumentException e) {
            log().warn("Invalid Snmp Criteria: " + e.getMessage());
        } catch (Throwable t) {
            log().warn("Unexpected exception during SNMP poll of interface " + ipaddr, t);
        }
        return false;
    }

    /**
     * <p>log</p>
     *
     * @return a {@link org.apache.log4j.Category} object.
     */
    public static Category log() {
        return ThreadCategory.getInstance(BgpSessionPlugin.class);
    }
}
