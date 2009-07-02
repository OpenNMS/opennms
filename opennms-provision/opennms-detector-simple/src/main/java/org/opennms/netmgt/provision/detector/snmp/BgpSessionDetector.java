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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.detector.snmp;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;

import org.apache.log4j.Category;
import org.jfree.util.Log;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.capsd.plugins.BgpSessionPlugin;
import org.opennms.netmgt.provision.DetectorMonitor;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class BgpSessionDetector extends SnmpDetector {
    /**
     * Name of monitored service.
     */
    private static final String PROTOCOL_NAME = "BGP_Session";

    /**
     * Default OID for the table that represents the BGP-peer states.
     */
    private static final String BGP_PEER_STATE_OID = ".1.3.6.1.2.1.15.3.1.2";
    
    private String m_bgpPeerIp = "";
    public BgpSessionDetector(){
        setServiceName(PROTOCOL_NAME);
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
    @Override
    public boolean isServiceDetected(InetAddress address, DetectorMonitor detectMonitor) {
        try {
            String bgpPeerIp = getBgpPeerIp();
            SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address);
            
            configureAgentPTR(agentConfig);

            configureAgentVersion(agentConfig);

            String bgpPeerState = getValue(agentConfig, BGP_PEER_STATE_OID + "." + bgpPeerIp);
            if (Log.isDebugEnabled()) {
                log().debug("BgpSessionMonitor.capsd: bgpPeerState: " + bgpPeerState);
            }
            if  (bgpPeerState != null && Integer.parseInt(bgpPeerState) >= 1 && Integer.parseInt(bgpPeerState) <= 6){
                return true;
            }
            
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        return false;
    }

    public void setBgpPeerIp(String bgpPeerIp) {
        m_bgpPeerIp = bgpPeerIp;
    }

    public String getBgpPeerIp() {
        return m_bgpPeerIp;
    }
}
