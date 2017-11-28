/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import org.opennms.core.rpc.api.RequestRejectedException;
import org.opennms.core.rpc.api.RequestTimedOutException;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.dao.api.CriticalPath;
import org.opennms.netmgt.dao.hibernate.PathOutageManagerDaoImpl;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.icmp.proxy.PingSequence;
import org.opennms.netmgt.icmp.proxy.PingSummary;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.pollables.PendingPollEvent;
import org.opennms.netmgt.poller.pollables.PollContext;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a DefaultPollContext
 *
 * @author brozow
 * @version $Id: $
 */
public class DefaultPollContext implements PollContext, EventListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPollContext.class);
    private static final String[] UEIS = {
        // service events without node processing enable
        EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI,
        EventConstants.SERVICE_RESPONSIVE_EVENT_UEI,
        
        // service events with node processing enabled
        EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI,
        EventConstants.NODE_LOST_SERVICE_EVENT_UEI,

        // interface events
        EventConstants.INTERFACE_DOWN_EVENT_UEI,
        EventConstants.INTERFACE_UP_EVENT_UEI,
        
        // node events
        EventConstants.NODE_DOWN_EVENT_UEI,
        EventConstants.NODE_UP_EVENT_UEI
    };
    
    private volatile PollerConfig m_pollerConfig;
    private volatile QueryManager m_queryManager;
    private volatile EventIpcManager m_eventManager;
    private volatile LocationAwarePingClient m_locationAwarePingClient;
    private volatile String m_name;
    private volatile String m_localHostName;
    private volatile boolean m_listenerAdded = false;
    private final Queue<PendingPollEvent> m_pendingPollEvents = new ConcurrentLinkedQueue<>();

    /**
     * <p>getEventManager</p>
     *
     * @return a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public EventIpcManager getEventManager() {
        return m_eventManager;
    }
    
    /**
     * <p>setEventManager</p>
     *
     * @param eventManager a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
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

    public LocationAwarePingClient getLocationAwarePingClient() {
        return m_locationAwarePingClient;
    }

    public void setLocationAwarePingClient(LocationAwarePingClient locationAwarePingClient) {
        m_locationAwarePingClient = locationAwarePingClient;
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
            getEventManager().addEventListener(this, Arrays.asList(UEIS));
            m_listenerAdded = true;
        }
        PendingPollEvent pollEvent = new PendingPollEvent(event);
        m_pendingPollEvents.add(pollEvent);

        //log().info("Sending "+event.getUei()+" for element "+event.getNodeid()+":"+event.getInterface()+":"+event.getService(), new Exception("StackTrace"));
        getEventManager().sendNow(event);
        return pollEvent;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#createEvent(java.lang.String, int, java.net.InetAddress, java.lang.String, java.util.Date)
     */
    /** {@inheritDoc} */
    @Override
    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date, String reason) {
        LOG.debug("createEvent: uei = {} nodeid = {}", uei, nodeId);
        
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
            final CriticalPath criticalPath = PathOutageManagerDaoImpl.getInstance().getCriticalPath(nodeId);
            if (criticalPath != null && criticalPath.getIpAddress() != null) {
                if (!testCriticalPath(criticalPath)) {
                    LOG.debug("Critical path test failed for node {}", nodeId);
                    bldr.addParam(EventConstants.PARM_LOSTSERVICE_REASON, EventConstants.PARM_VALUE_PATHOUTAGE);
                    bldr.addParam(EventConstants.PARM_CRITICAL_PATH_IP, InetAddrUtils.str(criticalPath.getIpAddress()));
                    bldr.addParam(EventConstants.PARM_CRITICAL_PATH_SVC, criticalPath.getServiceName());
                } else {
                    LOG.debug("Critical path test passed for node {}", nodeId);
                }
            } else {
                LOG.debug("No Critical path to test for node {}", nodeId);
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
        // Open the outage immediately
        final Integer outageId = getQueryManager().openOutagePendingLostEventId(svc.getNodeId(),
                svc.getIpAddr(), svc.getSvcName(), svcLostEvent.getDate());

        // Defer updating the outage with the event id until we receive back
        // from the event bus
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                final int eventId = svcLostEvent.getEventId();
                if (eventId > 0) {
                    getQueryManager().updateOpenOutageWithEventId(outageId, eventId);
                } else {
                    LOG.warn("run: Failed to determine an eventId for service lost for: {} with event: {}", svc, svcLostEvent);
                }
            }
        };
        if (svcLostEvent instanceof PendingPollEvent) {
            ((PendingPollEvent)svcLostEvent).addPending(r);
        }
        else {
            r.run();
        }
        LOG.debug("openOutage: sending outageCreated event for: {} on {}", svc.getSvcName(), svc.getIpAddr());
        sendEvent(createEvent(EventConstants.OUTAGE_CREATED_EVENT_UEI, svc.getNodeId(), svc.getAddress(), svc.getSvcName(), svcLostEvent.getDate(), null));
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#resolveOutage(org.opennms.netmgt.poller.pollables.PollableService, org.opennms.netmgt.xml.event.Event)
     */
    /** {@inheritDoc} */
    @Override
    public void resolveOutage(final PollableService svc, final PollEvent svcRegainEvent) {
        // Resolve the outage immediately
        final Integer outageId = getQueryManager().resolveOutagePendingRegainEventId(svc.getNodeId(),
                svc.getIpAddr(), svc.getSvcName(), svcRegainEvent.getDate());

        // There may be no outage for this particular service. This can happen when interfaces
        // are reparented or when a node gains a new service while down.
        if (outageId == null) {
            LOG.info("resolveOutage: no outstanding outage for {} on {} with node id {}", svc.getSvcName(), svc.getIpAddr(), svc.getNodeId());
            return;
        }

        // Defer updating the outage with the event id until we receive back
        // from the event bus
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                final int eventId = svcRegainEvent.getEventId();
                if (eventId > 0) {
                    getQueryManager().updateResolvedOutageWithEventId(outageId, eventId);
                } else {
                    LOG.warn("run: Failed to determine an eventId for service regained for: {} with event: {}", svc, svcRegainEvent);
                }
            }
        };
        if (svcRegainEvent instanceof PendingPollEvent) {
            ((PendingPollEvent)svcRegainEvent).addPending(r);
        }
        else {
            r.run();
        }
        LOG.debug("resolveOutage: sending outageResolved event for: {} on {}", svc.getSvcName(), svc.getIpAddr());
        sendEvent(createEvent(EventConstants.OUTAGE_RESOLVED_EVENT_UEI, svc.getNodeId(), svc.getAddress(), svc.getSvcName(), svcRegainEvent.getDate(), null));
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
    public void onEvent(final Event event) {
        if (LOG.isDebugEnabled()) {
            // CAUTION: m_pendingPollEvents.size() is not a constant-time operation
            LOG.debug("onEvent: Received event: {} uei: {}, dbid: {}, pendingEventCount: {}", event, event.getUei(), event.getDbid(), m_pendingPollEvents.size());
        }

        for (final PendingPollEvent pollEvent : m_pendingPollEvents) {
            LOG.trace("onEvent: comparing event to pollEvent: {}", pollEvent);
            // TODO: This equals comparison is more like a '==' operation because
            // I think that both events would have to be identical instances to
            // have the same event ID. This will probably cause problems if we
            // cluster event processing and the event instances are ever not 
            // identical.
            if (event.equals(pollEvent.getEvent())) {
                LOG.trace("onEvent: found matching pollEvent, completing pollEvent: {}", pollEvent);
                // Thread-safe and idempotent
                pollEvent.complete(event);
                // TODO: Can we break here? I think there should only be one 
                // instance of any given event in m_pendingPollEvents
                // break;
            }
        }

        for (final Iterator<PendingPollEvent> it = m_pendingPollEvents.iterator(); it.hasNext();) {
            final PendingPollEvent pollEvent = it.next();
            LOG.trace("onEvent: determining if pollEvent is pending: {}", pollEvent);
            if (!pollEvent.isPending()) {
                try {
                    // Thread-safe and idempotent
                    processPending(pollEvent);
                } catch (Throwable e) {
                    LOG.error("Unexpected exception while processing pollEvent: " + pollEvent, e);
                }
                // TODO: Should we remove the task before processing it? This would
                // reduce the chances that two threads could process the same event
                // simultaneously, although since the call is now thread-safe and
                // idempotent, that's not really a problem.
                it.remove();
                continue;
            }

            // If the event was not completed and it is still pending, then don't do anything to it
        }
        LOG.debug("onEvent: Finished processing event: {} uei: {}, dbid: {}", event, event.getUei(), event.getDbid());
    }

    private static void processPending(final PendingPollEvent pollEvent) {
        LOG.trace("onEvent: pollEvent is no longer pending, processing pollEvent: {}", pollEvent);
        // Thread-safe and idempotent
        pollEvent.processPending();
        LOG.trace("onEvent: processing of pollEvent completed: {}", pollEvent);
    }

    private boolean testCriticalPath(CriticalPath criticalPath) {
        if (!"ICMP".equalsIgnoreCase(criticalPath.getServiceName())) {
            LOG.warn("Critical paths using services other than ICMP are not currently supported."
                    + " ICMP will be used for testing {}.", criticalPath);
        }

        final InetAddress ipAddress = criticalPath.getIpAddress();
        final int retries = OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathRetries();
        final int timeout = OpennmsServerConfigFactory.getInstance().getDefaultCriticalPathTimeout();

        boolean available = false;
        try {
            final PingSummary pingSummary = m_locationAwarePingClient.ping(ipAddress)
                    .withLocation(criticalPath.getLocationName())
                    .withTimeout(timeout, TimeUnit.MILLISECONDS)
                    .withRetries(retries)
                    .execute()
                    .get();

            // We consider the path to be available if any of the requests were successful
            available = pingSummary.getSequences().stream()
                            .filter(PingSequence::isSuccess)
                            .count() > 0;
        } catch (InterruptedException e) {
            LOG.warn("Interrupted while testing {}. Marking the path as available.", criticalPath);
            available = true;
        } catch (Throwable e) {
            final Throwable cause = e.getCause();
            if (cause != null && cause instanceof RequestTimedOutException) {
                LOG.warn("No response was received when remotely testing {}."
                        + " Marking the path as available.", criticalPath);
                available = true;
            } else if (cause != null && cause instanceof RequestRejectedException) {
                LOG.warn("Request was rejected when attemtping to test the remote path {}."
                        + " Marking the path as available.", criticalPath);
                available = true;
            }
            LOG.warn("An unknown error occured while testing the critical path: {}."
                    + " Marking the path as unavailable.", criticalPath, e);
            available = false;
        }
        LOG.debug("testCriticalPath: checking {}@{}, available ? {}", criticalPath.getServiceName(), ipAddress, available);
        return available;
    }

    private String getNodeLabel(int nodeId) {
        String nodeLabel = null;
        try {
            nodeLabel = getQueryManager().getNodeLabel(nodeId);
        } catch (SQLException sqlE) {
            // Log a warning
            LOG.warn("Failed to retrieve node label for nodeid {}", nodeId, sqlE);
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
