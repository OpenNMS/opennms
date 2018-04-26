/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public abstract class NodeDiscovery extends Discovery {

    private static final Logger LOG = LoggerFactory.getLogger(NodeDiscovery.class);
    /**
     * The node ID of the system used to collect the SNMP information
     */
    protected final Node m_node;
    protected long m_initial_sleep_time = 600000;
    protected boolean m_runned = false;

    
    /**
     * Constructs a new SNMP collector for a node using the passed interface
     * as the collection point. The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
     * @param nodeid
     * @param config
     *            The SnmpPeer object to collect from.
     */
    public NodeDiscovery(final EnhancedLinkd linkd, final Node node) {
        super(linkd,linkd.getRescanInterval());
        m_node = node;
        m_initial_sleep_time = m_linkd.getInitialSleepTime();
    }

    /**
     * <p>
     * schedule
     * </p>
     */
    public void schedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(m_initial_sleep_time, this);
    }

    /**
     * <p>
     * unschedule
     * </p>
     */
    public void unschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "rescedule: Cannot schedule a service whose scheduler is set to null");
        if (m_runned) {
            m_scheduler.unschedule(this, m_poll_interval);
        } else {
            m_scheduler.unschedule(this, m_initial_sleep_time);
        }
    }


    /**
     * <p>
     * Performs the collection for the targeted IP address. The success or
     * failure of the collection should be tested via the <code>failed</code>
     * method.
     * </p>
     * <p>
     * No synchronization is performed, so if this is used in a separate
     * thread context synchronization must be added.
     * </p>
     */
    public void run() {
        if (m_suspendCollection) {
            sendSuspendedEvent(getNodeId());
        } else {
            sendStartEvent(getNodeId());
            LOG.info( "run: node [{}], start {} collection.", 
                      getNodeId(), getName());
            runCollection();
            LOG.info( "run: node [{}], end {} collection.", 
                      getNodeId(),getName());
            sendCompletedEvent(getNodeId());
        }
        m_runned = true;
        reschedule();
    }

    protected void sendSuspendedEvent(int nodeid) {
        EventBuilder builder = new EventBuilder(
                                   "uei.opennms.org/internal/linkd/nodeLinkDiscoverySuspended",
                                   "EnhancedLinkd");
                           builder.setNodeid(getNodeId());
                           builder.setInterface(getPrimaryIpAddress());
                           builder.addParam("runnable", getName());
       m_linkd.getEventForwarder().sendNow(builder.getEvent());
    }
    
    protected void sendStartEvent(int nodeid) {
        EventBuilder builder = new EventBuilder(
                                   "uei.opennms.org/internal/linkd/nodeLinkDiscoveryStarted",
                                   "EnhancedLinkd");
                           builder.setNodeid(nodeid);
                           builder.setInterface(getPrimaryIpAddress());
                           builder.addParam("runnable", getName());
                           m_linkd.getEventForwarder().sendNow(builder.getEvent());
        
    }
    
    protected void sendCompletedEvent(int nodeid) {
        EventBuilder builder = new EventBuilder(
                                   "uei.opennms.org/internal/linkd/nodeLinkDiscoveryCompleted",
                                   "EnhancedLinkd");
                           builder.setNodeid(nodeid);
                           builder.setInterface(getPrimaryIpAddress());
                           builder.addParam("runnable", getName());
                           m_linkd.getEventForwarder().sendNow(builder.getEvent());
    }

    protected abstract void runCollection(); 

    /**
     * Returns the target address that the collection occurred for.
     * 
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getPrimaryIpAddress() {
        return m_node.getSnmpPrimaryIpAddr();
    }

    public String getPrimaryIpAddressString() {
    	return str(m_node.getSnmpPrimaryIpAddr());
    }

    /**
     * <p>
     * getInfo
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getInfo() {
        return  getName()  
        		+ " node=" + getNodeId()
        		+ " ip=" + str(getPrimaryIpAddress());
    }

    public int getNodeId() {
    	return m_node.getNodeId();
    }
    
    public String getSysoid() {
        return m_node.getSysoid();
    }

    public String getSysname() {
        return m_node.getSysname();
    }

    public String getLocation() {
        return m_node.getLocation();
    }
    
    /**
     * <p>
     * getInitialSleepTime
     * </p>
     * 
     * @return Returns the initial_sleep_time.
     */
    public long getInitialSleepTime() {
        return m_initial_sleep_time;
    }

    /**
     * <p>
     * setInitialSleepTime
     * </p>
     * 
     * @param initial_sleep_time
     *            The initial_sleep_timeto set.
     */
    public void setInitialSleepTime(long initial_sleep_time) {
        m_initial_sleep_time = initial_sleep_time;
    }



    public abstract String getName();

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (int) (m_initial_sleep_time ^ (m_initial_sleep_time >>> 32));
		result = prime * result + ((m_node == null) ? 0 : m_node.hashCode());
		result = prime * result
				+ (int) (m_poll_interval ^ (m_poll_interval >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeDiscovery other = (NodeDiscovery) obj;
		if (m_initial_sleep_time != other.m_initial_sleep_time)
			return false;
		if (m_node == null) {
			if (other.m_node != null)
				return false;
		} else if (!m_node.equals(other.m_node))
			return false;
		if (m_poll_interval != other.m_poll_interval)
			return false;
		return true;
	}
	
}
