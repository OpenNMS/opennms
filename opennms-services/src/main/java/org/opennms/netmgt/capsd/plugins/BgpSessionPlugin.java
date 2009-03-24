/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2002-2008 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.capsd.plugins;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.util.Map;

import org.apache.log4j.Category;
import org.jfree.util.Log;
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
 */
public final class BgpSessionPlugin extends SnmpPlugin {
    /**
     * Name of monitored service.
     */
    private static final String PROTOCOL_NAME = "BGP-Session-Monitor";

    /**
     * Default OID for the table that represents the BGP-peer states.
     */
    private static final String BGP_PEER_STATE_OID = ".1.3.6.1.2.1.15.3.1.2";

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
     * Returns true if the protocol defined by this plugin is supported. If
     * the protocol is not supported then a false value is returned to the
     * caller. The qualifier map passed to the method is used by the plugin to
     * return additional information by key-name. These key-value pairs can be
     * added to service events if needed.
     * 
     * @param address
     *            The address to check for support.
     * @param qualifiers
     *            The map where qualification are set by the plugin.
     * @return True if the protocol is supported by the address.
     */
    public boolean isProtocolSupported(InetAddress address,
            Map<String, Object> qualifiers) {
        try {
            String bgpPeerIp = ParameterMap.getKeyedString(qualifiers,"bgpPeerIp", null);
            SnmpAgentConfig agentConfig = SnmpPeerFactory.getInstance().getAgentConfig(address);
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

                SnmpObjId bgpPeerStateSnmpObject = SnmpObjId.get(BGP_PEER_STATE_OID + "." + bgpPeerIp);
                SnmpValue bgpPeerState = SnmpUtils.get(agentConfig, bgpPeerStateSnmpObject);
                if (Log.isDebugEnabled()) {
                    log().debug("BgpSessionMonitor.capsd: bgpPeerState: " + bgpPeerState);
                }
                if  (Integer.parseInt(bgpPeerState.toString()) >= 1 && 
                    Integer.parseInt(bgpPeerState.toString()) <= 6)
                {
                    return true;
                }
            }
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        return false;
    }

    public static Category log() {
        return ThreadCategory.getInstance(BgpSessionPlugin.class);
    }
}
