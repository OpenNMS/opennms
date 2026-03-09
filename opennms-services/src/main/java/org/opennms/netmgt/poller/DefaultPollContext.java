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
package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.opennms.core.rpc.api.RequestRejectedException;
import org.opennms.core.rpc.api.RequestTimedOutException;
import org.opennms.core.tsid.TsidFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.dao.api.CriticalPath;
import org.opennms.netmgt.dao.hibernate.PathOutageManagerDaoImpl;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.icmp.proxy.PingSequence;
import org.opennms.netmgt.icmp.proxy.PingSummary;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.poller.pollables.DbPollEvent;
import org.opennms.netmgt.poller.pollables.PollContext;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.snmp.InetAddrUtils;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * DefaultPollContext manages poller event lifecycle and outage state.
 *
 * Events are assigned a TSID (time-sorted unique ID) before being sent,
 * eliminating the need for the old PendingPollEvent round-trip pattern.
 *
 * @author brozow
 */
public class DefaultPollContext implements PollContext, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultPollContext.class);

    /**
     * Poll timestamps are updated using a DB transaction in the same thread and immediately following the poll.
     * This may cause unnecessary overhead in extreme cases, so we add the ability to disable this functionality.
     */
    public static final boolean DISABLE_POLL_TIMESTAMP_TRACKING = Boolean.getBoolean("org.opennms.netmgt.poller.disablePollTimestampTracking");

    private volatile PollerConfig m_pollerConfig;
    private volatile QueryManager m_queryManager;
    private volatile EventIpcManager m_eventManager;
    private volatile LocationAwarePingClient m_locationAwarePingClient;
    private volatile TsidFactory m_tsidFactory;
    private volatile String m_name;
    private volatile String m_localHostName;
    private volatile AsyncPollingEngine m_asyncPollingEngine;

    @Override
    public void afterPropertiesSet() throws Exception {
        m_asyncPollingEngine = new AsyncPollingEngine(getPollerConfig().getMaxConcurrentAsyncPolls());
    }

    public EventIpcManager getEventManager() {
        return m_eventManager;
    }

    public void setEventManager(EventIpcManager eventManager) {
        m_eventManager = eventManager;
    }

    public void setLocalHostName(String localHostName) {
        m_localHostName = localHostName;
    }

    public String getLocalHostName() {
        return m_localHostName;
    }

    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public PollerConfig getPollerConfig() {
        return m_pollerConfig;
    }

    public void setPollerConfig(PollerConfig pollerConfig) {
        m_pollerConfig = pollerConfig;
    }

    public QueryManager getQueryManager() {
        return m_queryManager;
    }

    public void setQueryManager(QueryManager queryManager) {
        m_queryManager = queryManager;
    }

    public LocationAwarePingClient getLocationAwarePingClient() {
        return m_locationAwarePingClient;
    }

    public void setLocationAwarePingClient(LocationAwarePingClient locationAwarePingClient) {
        m_locationAwarePingClient = locationAwarePingClient;
    }

    public TsidFactory getTsidFactory() {
        return m_tsidFactory;
    }

    public void setTsidFactory(TsidFactory tsidFactory) {
        m_tsidFactory = tsidFactory;
    }

    @Override
    public String getCriticalServiceName() {
        return getPollerConfig().getCriticalService();
    }

    @Override
    public boolean isNodeProcessingEnabled() {
        return getPollerConfig().isNodeOutageProcessingEnabled();
    }

    @Override
    public boolean isPollingAllIfCritServiceUndefined() {
        return getPollerConfig().shouldPollAllIfNoCriticalServiceDefined();
    }

    /**
     * Assigns a TSID to the event before sending it via EventIpcManager.
     * Returns a DbPollEvent with the event ID immediately available,
     * eliminating the need for PendingPollEvent and onEvent() callback.
     */
    @Override
    public PollEvent sendEvent(Event event) {
        long tsid = m_tsidFactory.create();
        event.setDbid(tsid);

        getEventManager().sendNow(event);

        return new DbPollEvent(tsid, event.getUei(), event.getTime());
    }

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

    /**
     * Opens an outage and sets the svcLostEventId immediately using the TSID
     * already assigned by sendEvent().
     */
    @Override
    public void openOutage(final PollableService svc, final PollEvent svcLostEvent) {
        final long eventId = svcLostEvent.getEventId();
        final Integer outageId = getQueryManager().openOutagePendingLostEventId(svc.getNodeId(),
                svc.getIpAddr(), svc.getSvcName(), svcLostEvent.getDate());

        if (eventId > 0) {
            getQueryManager().updateOpenOutageWithEventId(outageId, eventId);
        } else {
            LOG.warn("openOutage: svcLostEvent has no eventId for: {}", svc);
        }

    }

    /**
     * Resolves an outage and sets the svcRegainedEventId immediately using the TSID
     * already assigned by sendEvent().
     */
    @Override
    public void resolveOutage(final PollableService svc, final PollEvent svcRegainEvent) {
        final long eventId = svcRegainEvent.getEventId();
        final Integer outageId = getQueryManager().resolveOutagePendingRegainEventId(svc.getNodeId(),
                svc.getIpAddr(), svc.getSvcName(), svcRegainEvent.getDate());

        // There may be no outage for this particular service. This can happen when interfaces
        // are reparented or when a node gains a new service while down.
        if (outageId == null) {
            LOG.info("resolveOutage: no outstanding outage for {} on {} with node id {}", svc.getSvcName(), svc.getIpAddr(), svc.getNodeId());
            return;
        }

        if (eventId > 0) {
            getQueryManager().updateResolvedOutageWithEventId(outageId, eventId);
        } else {
            LOG.warn("resolveOutage: svcRegainEvent has no eventId for: {}", svc);
        }

    }

    @Override
    public boolean isServiceUnresponsiveEnabled() {
        return getPollerConfig().isServiceUnresponsiveEnabled();
    }

    @Override
    public void trackPoll(PollableService service, PollStatus result) {
        try {
            if (!result.isUnknown() && !DISABLE_POLL_TIMESTAMP_TRACKING) {
                getQueryManager().updateLastGoodOrFail(service, result);
            }
        } catch (Exception e) {
            LOG.warn("Error occurred while tracking poll for service: {}", service, e);
        }
    }

    @Override
    public boolean isAsyncEngineEnabled() {
        return m_pollerConfig.isAsyncEngineEnabled();
    }

    @Override
    public AsyncPollingEngine getAsyncPollingEngine() {
        return m_asyncPollingEngine;
    }

    private boolean testCriticalPath(CriticalPath criticalPath) {
        if (!"ICMP".equalsIgnoreCase(criticalPath.getServiceName())) {
            LOG.warn("Critical paths using services other than ICMP are not currently supported."
                    + " ICMP will be used for testing {}.", criticalPath);
        }

        final InetAddress ipAddress = criticalPath.getIpAddress();
        final int retries = getPollerConfig().getDefaultCriticalPathRetries();
        final int timeout = getPollerConfig().getDefaultCriticalPathTimeout();

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

    /**
     * Returns the name used for EventListener registration. Kept for API
     * compatibility even though this class no longer implements EventListener.
     */
    public String getListenerName() {
        return m_name;
    }
}
