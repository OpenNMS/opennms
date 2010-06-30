//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
package org.opennms.netmgt.collectd.wmi;

import org.opennms.protocols.wmi.WmiManager;
import org.opennms.protocols.wmi.WmiAgentConfig;
import org.opennms.protocols.wmi.IWmiClient;
import org.opennms.protocols.wmi.WmiClient;
import org.opennms.protocols.wmi.WmiException;
import org.opennms.netmgt.config.WmiPeerFactory;
import org.opennms.core.utils.ThreadCategory;

import java.util.HashMap;
import java.util.Map;
import java.util.Date;
import java.net.InetAddress;

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
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * @author <A HREF="mailto:matt.raykowski@gmail.com">Matt Raykowski </A>
 * @author <A HREF="http://www.opennsm.org">OpenNMS </A>
 * @version $Id: $
 */
public class WmiAgentState {
    private WmiManager m_manager;
    private IWmiClient m_wmiClient;

    private WmiAgentConfig m_agentConfig;
    private String m_address;
    private HashMap<String, WmiGroupState> m_groupStates = new HashMap<String, WmiGroupState>();

    /**
     * <p>Constructor for WmiAgentState.</p>
     *
     * @param address a {@link java.net.InetAddress} object.
     * @param parameters a {@link java.util.Map} object.
     */
    public WmiAgentState(InetAddress address, Map parameters) {
        m_address = address.getHostAddress();
        m_agentConfig = WmiPeerFactory.getInstance().getAgentConfig(address);
        m_manager = new WmiManager(m_address, m_agentConfig.getUsername(),
						m_agentConfig.getPassword(), m_agentConfig.getDomain());

        try {
            m_wmiClient = new WmiClient(m_address);
        } catch(WmiException e) {
            log().error("Failed to create WMI client: " + e.getMessage(), e);
        }
    }

    /**
     * <p>connect</p>
     */
    public void connect() {
        try {
            m_wmiClient.connect(m_agentConfig.getDomain(), m_agentConfig.getUsername(), m_agentConfig.getPassword());
        } catch(WmiException e) {
            log().error("Failed to connect to host: "+ e.getMessage(), e);
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
    public boolean groupIsAvailable(String groupName) {
        WmiGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            return false; // If the group availability hasn't been set
            // yet, it's not available.
        }
        return groupState.isAvailable();
    }

    /**
     * <p>setGroupIsAvailable</p>
     *
     * @param groupName a {@link java.lang.String} object.
     * @param available a boolean.
     */
    public void setGroupIsAvailable(String groupName, boolean available) {
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
    public boolean shouldCheckAvailability(String groupName, int recheckInterval) {
        WmiGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            // If the group hasn't got a status yet, then it should be
            // checked regardless (and setGroupIsAvailable will
            // be called soon to create the status object)
            return true;
        }
        Date lastchecked = groupState.getLastChecked();
        Date now = new Date();
        return (now.getTime() - lastchecked.getTime() > recheckInterval);
    }

    /**
     * <p>didCheckGroupAvailability</p>
     *
     * @param groupName a {@link java.lang.String} object.
     */
    public void didCheckGroupAvailability(String groupName) {
        WmiGroupState groupState = m_groupStates.get(groupName);
        if (groupState == null) {
            // Probably an error - log it as a warning, and give up
            log().warn("didCheckGroupAvailability called on a group without state - this is odd");
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
    public void setWmiClient(IWmiClient wmiClient) {
        this.m_wmiClient = wmiClient;
    }

    private ThreadCategory log() {
		return ThreadCategory.getInstance(getClass());
	}
}
