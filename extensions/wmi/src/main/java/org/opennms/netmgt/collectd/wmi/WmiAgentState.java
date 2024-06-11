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
package org.opennms.netmgt.collectd.wmi;

import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.wmi.WmiAgentConfig;
import org.opennms.protocols.wmi.IWmiClient;
import org.opennms.protocols.wmi.WmiClient;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.protocols.wmi.WmiManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <P>
 * Contains a WmiManager and WmiClient instance referring to the agent loaded via
 * the InetAddress parameter provided in the constructor. Uses the InetAddress to
 * look up the agent configuration to properly connect the client and manager
 * to the remote agent. Provides the collector with access to the client and manager
 * as well as information regarding the availability of WPM (Windows Performance Metric)
 * groups.
 * </P>
 *
 * @author <a href="mailto:matt.raykowski@gmail.com">Matt Raykowski</a>
 * @author <a href="http://www.opennms.org">OpenNMS</a>
 */
public class WmiAgentState {
	
	private static final Logger LOG = LoggerFactory.getLogger(WmiAgentState.class);

    private final WmiManager m_manager;
    private IWmiClient m_wmiClient;

    private final WmiAgentConfig m_agentConfig;
    private final String m_address;
    private Map<String, WmiGroupState> m_groupStates = new HashMap<String, WmiGroupState>();

    public WmiAgentState(final InetAddress address, final WmiAgentConfig agentConfig, final Map<?,?> parameters) {
        m_address = InetAddressUtils.str(address);
        m_agentConfig = agentConfig;
        m_manager = new WmiManager(m_address, m_agentConfig.getUsername(), m_agentConfig.getPassword(), m_agentConfig.getDomain());

        try {
            m_wmiClient = new WmiClient(m_address);
        } catch(final WmiException e) {
            LOG.error("Failed to create WMI client.", e);
        }
    }

    /**
     * <p>connect</p>
     * 
     * @param namespace the WMI namespace to request
     */
    public void connect(final String namespace) {
        try {
            m_wmiClient.connect(m_agentConfig.getDomain(), m_agentConfig.getUsername(), m_agentConfig.getPassword(), namespace);
        } catch(final WmiException e) {
            LOG.error("Failed to connect to host.", e);
        }
    }

    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getAddress() {
        return m_address;
    }

    /**
     * <p>getManager</p>
     *
     * @return a {@link org.opennms.protocols.wmi.WmiManager} object.
     */
    public WmiManager getManager() {
        return m_manager;
    }

    /**
     * <p>groupIsAvailable</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean groupIsAvailable(final String groupName) {
        final WmiGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            return false; // If the group availability hasn't been set yet, it's not available.
        }
        return groupState.isAvailable();
    }

    /**
     * <p>setGroupIsAvailable</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param available a boolean.
     */
    public void setGroupIsAvailable(final String groupName, final boolean available) {
        WmiGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            groupState = new WmiGroupState(available);
        }
        groupState.setAvailable(available);
        m_groupStates.put(groupName, groupState);
    }

    /**
     * <p>shouldCheckAvailability</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param recheckInterval a int.
     * @return a boolean.
     */
    public boolean shouldCheckAvailability(final String groupName, final int recheckInterval) {
        final WmiGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            // If the group hasn't got a status yet, then it should be
            // checked regardless (and setGroupIsAvailable will
            // be called soon to create the status object)
            return true;
        }
        final Date lastchecked = groupState.getLastChecked();
        final Date now = new Date();
        return (now.getTime() - lastchecked.getTime() > recheckInterval);
    }

    /**
     * <p>didCheckGroupAvailability</p>
     *
     * @param groupName a {@link java.lang.String} object.
     */
    public void didCheckGroupAvailability(final String groupName) {
        final WmiGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            // Probably an error - log it as a warning, and give up
            LOG.warn("didCheckGroupAvailability called on a group without state - this is odd.");
            return;
        }
        groupState.setLastChecked(new Date());
    }

    /**
     * <p>getWmiClient</p>
     *
     * @return a {@link org.opennms.protocols.wmi.IWmiClient} object.
     */
    public IWmiClient getWmiClient() {
        return m_wmiClient;
    }

    /**
     * <p>setWmiClient</p>
     *
     * @param wmiClient a {@link org.opennms.protocols.wmi.IWmiClient} object.
     */
    public void setWmiClient(final IWmiClient wmiClient) {
        this.m_wmiClient = wmiClient;
    }
}
