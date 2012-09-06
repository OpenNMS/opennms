/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.model.OnmsIpInterface;

/**
 * The CollectorUpdates class encapsulates changes to a SnmpCollector which is
 * actively being collected by the collectd scheduler. When associated with a
 * SnmpCollector object the updates represented by the class are to be applied
 * to the SnmpCollector AFTER being popped from the interval queues for
 * scheduling but BEFORE the next collection takes place.
 * 
 * This is necessary because it isn't possible to make modifications to the
 * SnmpCollector objects at the time that a particular event is received by the
 * BroadcastEventProcessor class...the interface may in fact be in the process
 * of being collected. So we make "note" of the updates using this class and
 * wait for the collector object to be popped from the interval queues before
 * the updates are actually applied.
 * 
 * The only "updates" currently handled by this class are new/modified
 * attributes, a flag indicating that the service has been marked for deletion,
 * a flag indicating the service has been marked for reparenting, a flag
 * indicating the service has been marked for reinitialization, and finally a
 * flag indicating that the SNMP service on the interface has a new status.
 * 
 */
final class CollectorUpdates {
    /**
     * Indicates if there are any updates to be processed
     */
    private boolean m_hasUpdates;

    /**
     * Holds new/modified network interface attributes
     */
    private Map<String, Object> m_properties;

    /**
     * Set to true if the interface has been marked for deletion and should no
     * longer be polled or rescheduled.
     */
    private boolean m_deletionFlag;

    /**
     * Set to true if the interface has been marked for re-initialization.
     */
    private OnmsIpInterface m_reinitFlag = null;

    /**
     * Set to true if the interface has been marked for reparenting.
     */
    private boolean m_reparentFlag;

    /**
     * Old nodeId for reparenting
     */
    private String m_reparentOldNodeId;

    /**
     * New nodeId for reparenting
     */
    private String m_reparentNewNodeId;
    
    private OnmsIpInterface m_newIface = null;

    /**
     * Constructor.
     */
    CollectorUpdates() {
        reset();
    }

    void reset() {
        m_hasUpdates = false;
        m_properties = null;
        m_deletionFlag = false;
        m_reinitFlag = null;
        m_reparentFlag = false;
        m_reparentOldNodeId = null;
        m_reparentNewNodeId = null;
        m_newIface = null;
    }

    /**
     * Set an attribute.
     */
    void setAttribute(String property, Object value) {
        if (m_properties == null)
            m_properties = new HashMap<String, Object>();

        m_properties.put(property, value);

        m_hasUpdates = true;
    }

    /**
     * Retrieve the attribute with the specfied key.
     */
    Object getAttribute(String property) {
        Object rc = null;
        if (m_properties != null)
            rc = m_properties.get(property);
        return rc;
    }

    /**
     * Set the deletion flag.
     */
    void markForDeletion() {
        m_deletionFlag = true;
        m_hasUpdates = true;
    }

    /**
     * Set the reinit flag.
     */
    void markForReinitialization(OnmsIpInterface iface) {
        m_reinitFlag = iface;
        m_hasUpdates = true;
    }

    /**
     * Set the reparent flag.
     * @param iface 
     */
    void markForReparenting(String oldNodeId, String newNodeId, OnmsIpInterface iface) {
        m_reparentFlag = true;
        m_reparentOldNodeId = oldNodeId;
        m_reparentNewNodeId = newNodeId;
        m_newIface = iface;
        m_hasUpdates = true;
    }

    String getReparentOldNodeId() {
        return m_reparentOldNodeId;
    }

    String getReparentNewNodeId() {
        return m_reparentNewNodeId;
    }
    
    OnmsIpInterface getUpdatedInterface() {
    	return m_newIface;
    }

    /**
     * Returns state of the hasUpdates flag
     */
    boolean hasUpdates() {
        return m_hasUpdates;
    }

    /**
     * Returns the state of the deletion flag.
     */
    boolean isDeletionFlagSet() {
        return m_deletionFlag;
    }

    /**
     * Returns the state of the reinit flag.
     */
    OnmsIpInterface isReinitializationNeeded() {
        return m_reinitFlag;
    }

    /**
     * Returns the state of the reparent flag.
     */
    boolean isReparentingFlagSet() {
        return m_reparentFlag;
    }

}
