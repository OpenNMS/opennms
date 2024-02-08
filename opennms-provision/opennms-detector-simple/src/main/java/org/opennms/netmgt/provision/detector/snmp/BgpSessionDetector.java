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
package org.opennms.netmgt.provision.detector.snmp;

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;

import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>BgpSessionDetector class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
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
    public boolean isServiceDetected(final InetAddress address, final SnmpAgentConfig agentConfig) {
        try {
            String bgpPeerIp = getBgpPeerIp();

            configureAgentPTR(agentConfig);

            configureAgentVersion(agentConfig);

            String bgpPeerState = getValue(agentConfig, BGP_PEER_STATE_OID + "." + bgpPeerIp, isHex());
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
