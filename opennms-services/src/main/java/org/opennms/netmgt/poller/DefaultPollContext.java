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

package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.capsd.plugins.IcmpPlugin;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.poller.pollables.PendingPollEvent;
import org.opennms.netmgt.poller.pollables.PollContext;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a DefaultPollContext
 *
 * @author brozow
 * @version $Id: $
 */
public class DefaultPollContext implements PollContext, EventListener {
    
    private volatile PollerConfig m_pollerConfig;
    private volatile QueryManager m_queryManager;
    private volatile EventIpcManager m_eventManager;
    private volatile String m_name;
    private volatile String m_localHostName;
    private volatile boolean m_listenerAdded = false;
    private final List<PendingPollEvent> m_pendingPollEvents = new LinkedList<PendingPollEvent>();

    /**
     * <p>getEventManager</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public EventIpcManager getEventManager() {
        return m_eventManager;
    }
    
    /**
     * <p>setEventManager</p>
     *
     * @param eventManager a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }
    
    /**
     * <p>setLocalHostName</p>
     *
     * @param localHostName a {@link java.lang.String} object.
     */
    public void setLocalHostName(String localHostName) {
        m_localHostName = localHostName;
    }
    
    /**
     * <p>getLocalHostName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getLocalHostName() {
        return m_localHostName;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }

    /**
     * <p>getPollerConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    /**
     * <p>setPollerConfig</p>
     *
     * @param pollerConfig a {@link org.opennms.netmgt.config.PollerConfig} object.
     */
    public void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    /**
     * <p>getQueryManager</p>
     *
     * @return a {@link org.opennms.netmgt.poller.QueryManager} object.
     */
    public QueryManager getQueryManager() {
        return m_queryManager;
    }

    /**
     * <p>setQueryManager</p>
     *
     * @param queryManager a {@link org.opennms.netmgt.poller.QueryManager} object.
     */
    public void setQueryManager(QueryManager queryManager) {
        m_queryManager = queryManager;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#getCriticalServiceName()
     */
    /**
     * <p>getCriticalServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getCriticalServiceName() {
        return getPollerConfig().getCriticalService();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#isNodeProcessingEnabled()
     */
    /**
     * <p>isNodeProcessingEnabled</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isNodeProcessingEnabled() {
        return getPollerConfig().isNodeOutageProcessingEnabled();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#isPollingAllIfCritServiceUndefined()
     */
    /**
     * <p>isPollingAllIfCritServiceUndefined</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isPollingAllIfCritServiceUndefined() {
        return getPollerConfig().shouldPollAllIfNoCriticalServiceDefined();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#sendEvent(org.opennms.netmgt.xml.event.Event)
     */
    /** {@inheritDoc} */
    @Override
    public PollEvent sendEvent(Event event) {
        if (!m_listenerAdded) {
            getEventManager().addEventListener(this);
            m_listenerAdded = true;
        }
        PendingPollEvent pollEvent = new PendingPollEvent(event);
        synchronized (m_pendingPollEvents) {
            m_pendingPollEvents.add(pollEvent);
        }
        //log().info("Sending "+event.getUei()+" for element "+event.getNodeid()+":"+event.getInterface()+":"+event.getService(), new Exception("StackTrace"));
        getEventManager().sendNow(event);
        return pollEvent;
    }

    ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#createEvent(java.lang.String, int, java.net.InetAddress, java.lang.String, java.util.Date)
     */
    /** {@inheritDoc} */
    @Override
    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date, String reason) {
        ThreadCategory log = ThreadCategory.getInstance(this.getClass());
        
        if (log.isDebugEnabled())
            log.debug("createEvent: uei = " + uei + " nodeid = " + nodeId);
        
        EventBuilder bldr = new EventBuilder(uei, this.getName(), date);
        bldr.setNodeid(nodeId);
        if (address != null) {
            bldr.setInterface(address);
        }
        if (svcName != null) {
            bldr.setService(svcName);
        }
        bldr.setHost(this.getLocalHostName());
        
        if (uei.equals(EventConstants.NODE_DOWN_EVENT_UEI)
                && this.getPollerConfig().isPathOutageEnabled()) {
            String[] criticalPath = this.getQueryManager().getCriticalPath(nodeId);
            
            if (criticalPath[0] != null && !criticalPath[0].equals("")) {
                if (!this.testCriticalPath(criticalPath)) {
                    log.debug("Critical path test failed for node " + nodeId);
                    
                    // add eventReason, criticalPathIp, criticalPathService
                    // parms
                    
                    bldr.addParam(EventConstants.PARM_LOSTSERVICE_REASON, EventConstants.PARM_VALUE_PATHOUTAGE);
                    bldr.addParam(EventConstants.PARM_CRITICAL_PATH_IP, criticalPath[0]);
                    bldr.addParam(EventConstants.PARM_CRITICAL_PATH_SVC, criticalPath[1]);
                    
                } else {
                    log.debug("Critical path test passed for node " + nodeId);
                }
            } else {
                log.debug("No Critical path to test for node " + nodeId);
            }
        }
        
        else if (uei.equals(EventConstants.NODE_LOST_SERVICE_EVENT_UEI)) {
            bldr.addParam(EventConstants.PARM_LOSTSERVICE_REASON, (reason == null ? "Unknown" : reason));
        }
        
        // For node level events (nodeUp/nodeDown) retrieve the
        // node's nodeLabel value and add it as a parm
        if (uei.equals(EventConstants.NODE_UP_EVENT_UEI)
                || uei.equals(EventConstants.NODE_DOWN_EVENT_UEI)) {
        
            String nodeLabel = this.getNodeLabel(nodeId);
            bldr.addParam(EventConstants.PARM_NODE_LABEL, nodeLabel);
            
        }
        
        return bldr.getEvent();
    }

    /** {@inheritDoc} */
    @Override
    public void openOutage(final PollableService svc, final PollEvent svcLostEvent) {
        log().debug("openOutage: Opening outage for: "+svc+" with event:"+svcLostEvent);
        final int nodeId = svc.getNodeId();
        final String ipAddr = svc.getIpAddr();
        final String svcName = svc.getSvcName();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                if (log().isDebugEnabled()) log().debug("run: Opening outage with query manager: "+svc+" with event:"+svcLostEvent);

                final int eventId = svcLostEvent.getEventId();
                if (eventId > 0) {
                    getQueryManager().openOutage(getPollerConfig().getNextOutageIdSql(), nodeId, ipAddr, svcName, eventId, EventConstants.formatToString(svcLostEvent.getDate()));
                } else {
                    log().warn("run: Failed to determine an eventId for service outage for: " + svc + " with event: " + svcLostEvent);
                }
            }

        };
        if (svcLostEvent instanceof PendingPollEvent) {
            ((PendingPollEvent)svcLostEvent).addPending(r);
        }
        else {
            r.run();
        }
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#resolveOutage(org.opennms.netmgt.poller.pollables.PollableService, org.opennms.netmgt.xml.event.Event)
     */
    /** {@inheritDoc} */
    @Override
    public void resolveOutage(final PollableService svc, final PollEvent svcRegainEvent) {
        final int nodeId = svc.getNodeId();
        final String ipAddr = svc.getIpAddr();
        final String svcName = svc.getSvcName();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                final int eventId = svcRegainEvent.getEventId();
                if (eventId > 0) {
                    getQueryManager().resolveOutage(nodeId, ipAddr, svcName, eventId, EventConstants.formatToString(svcRegainEvent.getDate()));
                } else {
                    log().warn("run: Failed to determine an eventId for service regained for: " + svc + " with event: " + svcRegainEvent);
                }
            }
        };
        if (svcRegainEvent instanceof PendingPollEvent) {
            ((PendingPollEvent)svcRegainEvent).addPending(r);
        }
        else {
            r.run();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void reparentOutages(String ipAddr, int oldNodeId, int newNodeId) {
        getQueryManager().reparentOutages(ipAddr, oldNodeId, newNodeId);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#isServiceUnresponsiveEnabled()
     */
    /**
     * <p>isServiceUnresponsiveEnabled</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isServiceUnresponsiveEnabled() {
        return getPollerConfig().isServiceUnresponsiveEnabled();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventListener#onEvent(org.opennms.netmgt.xml.event.Event)
     */
    /** {@inheritDoc} */
    @Override
    public void onEvent(final Event e) {
        synchronized (m_pendingPollEvents) {
            log().debug("onEvent: Received event: "+e+" uei: "+e.getUei()+", dbid: "+e.getDbid());
            for (final Iterator<PendingPollEvent> it = m_pendingPollEvents .iterator(); it.hasNext();) {
                final PendingPollEvent pollEvent = it.next();
                log().debug("onEvent: comparing events to poll event: "+pollEvent);
                if (e.equals(pollEvent.getEvent())) {
                    log().debug("onEvent: completing pollevent: "+pollEvent);
                    pollEvent.complete(e);
                }
            }
            
            for (Iterator<PendingPollEvent> it = m_pendingPollEvents.iterator(); it.hasNext(); ) {
                PendingPollEvent pollEvent = it.next();
                log().debug("onEvent: determining if pollEvent is pending: "+pollEvent);
                if (pollEvent.isPending()) continue;
                
                log().debug("onEvent: processing pending pollEvent...: "+pollEvent);
                pollEvent.processPending();
                it.remove();
                log().debug("onEvent: processing of pollEvent completed.: "+pollEvent);
            }
        }
        
    }

    boolean testCriticalPath(String[] criticalPath) {
        // TODO: Generalize the service
        InetAddress addr = null;
        boolean result = true;
    
        ThreadCategory log = log();
        log.debug("Test critical path IP " + criticalPath[0]);
        addr = InetAddressUtils.addr(criticalPath[0]);
        if (addr == null) {
            log.error("failed to convert string address to InetAddress " + criticalPath[0]);
            return true;
        }
        IcmpPlugin p = new IcmpPlugin();
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(
                "retry",
                Long.valueOf(
                         OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathRetries()));
        map.put(
                "timeout",
                Long.valueOf(
                         OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathTimeout()));

        result = p.isProtocolSupported(addr, map);
        return result;
    }

    String getNodeLabel(int nodeId) {
        String nodeLabel = null;
        try {
            nodeLabel = getQueryManager().getNodeLabel(nodeId);
        } catch (SQLException sqlE) {
            // Log a warning
            log().warn("Failed to retrieve node label for nodeid " + nodeId,
                     sqlE);
        }
    
        if (nodeLabel == null) {
            // This should never happen but if it does just
            // use nodeId for the nodeLabel so that the
            // event description has something to display.
            nodeLabel = String.valueOf(nodeId);
        }
        return nodeLabel;
    }

}
