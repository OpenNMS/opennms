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

package org.opennms.netmgt.provision.detector.snmp;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
/**
 * <p>BgpSessionDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@Scope("prototype")
public class BgpSessionDetector extends SnmpDetector {

    private static final Logger LOG = LoggerFactory.getLogger(BgpSessionDetector.class);

    /**
     * Name of monitored service.
     */
    private static final String PROTOCOL_NAME = "BGP_Session";

    /**
     * Default OID for the table that represents the BGP-peer states.
     */
    private static final String BGP_PEER_STATE_OID = ".1.3.6.1.2.1.15.3.1.2";
    
    private String m_bgpPeerIp = "";
    /**
     * <p>Constructor for BgpSessionDetector.</p>
     */
    public BgpSessionDetector(){
        setServiceName(PROTOCOL_NAME);
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
    @Override
    public boolean isServiceDetected(InetAddress address) {
        try {
            String bgpPeerIp = getBgpPeerIp();
            SnmpAgentConfig agentConfig = getAgentConfigFactory().getAgentConfig(address);
            
            configureAgentPTR(agentConfig);

            configureAgentVersion(agentConfig);

            String bgpPeerState = getValue(agentConfig, BGP_PEER_STATE_OID + "." + bgpPeerIp);
            LOG.debug("BgpSessionMonitor.capsd: bgpPeerState: {}", bgpPeerState);
            if  (bgpPeerState != null && Integer.parseInt(bgpPeerState) >= 1 && Integer.parseInt(bgpPeerState) <= 6){
                return true;
            }
            
        } catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
        return false;
    }

    /**
     * <p>setBgpPeerIp</p>
     *
     * @param bgpPeerIp a {@link java.lang.String} object.
     */
    public void setBgpPeerIp(String bgpPeerIp) {
        m_bgpPeerIp = bgpPeerIp;
    }

    /**
     * <p>getBgpPeerIp</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getBgpPeerIp() {
        return m_bgpPeerIp;
    }
}
