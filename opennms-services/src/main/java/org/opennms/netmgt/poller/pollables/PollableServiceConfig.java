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
package org.opennms.netmgt.poller.pollables;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.opennms.core.rpc.api.RpcExceptionHandler;
import org.opennms.core.rpc.api.RpcExceptionUtils;
import org.opennms.netmgt.collection.api.PersisterFactory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.poller.LocationAwarePollerClient;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.PollerResponse;
import org.opennms.netmgt.poller.ServiceMonitorAdaptor;
import org.opennms.netmgt.scheduler.ScheduleInterval;
import org.opennms.netmgt.scheduler.Timer;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a PollableServiceConfig
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PollableServiceConfig implements PollConfig, ScheduleInterval {
    private static final Logger LOG = LoggerFactory.getLogger(PollableServiceConfig.class);

    private PollerConfig m_pollerConfig;
    private PollableService m_service;
    private Map<String,Object> m_parameters = null;
    private Package m_pkg;
    private Timer m_timer;
    private Service m_configService;

    private Map<String, String> m_patternVariables = Collections.emptyMap();

    private final LocationAwarePollerClient m_locationAwarePollerClient;
    private final LatencyStoringServiceMonitorAdaptor m_latencyStoringServiceMonitorAdaptor;
    private final StatusStoringServiceMonitorAdaptor m_statusStoringServiceMonitorAdaptor;
    private final InvertedStatusServiceMonitorAdaptor m_invertedStatusServiceMonitorAdaptor = new InvertedStatusServiceMonitorAdaptor();
    private final ServiceMonitorAdaptor m_DeviceConfigMonitorAdaptor;

    private final ReadablePollOutagesDao m_pollOutagesDao;
    
    /**
     * <p>Constructor for PollableServiceConfig.</p>
     *
     * @param svc a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     * @param pollerConfig a {@link org.opennms.netmgt.config.PollerConfig} object.
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @param timer a {@link org.opennms.netmgt.scheduler.Timer} object.
     */
    public PollableServiceConfig(PollableService svc, PollerConfig pollerConfig, Package pkg, Timer timer, PersisterFactory persisterFactory,
                                 ThresholdingService thresholdingService, LocationAwarePollerClient locationAwarePollerClient,
                                 ReadablePollOutagesDao pollOutagesDao, ServiceMonitorAdaptor serviceMonitorAdaptor) {
        m_service = svc;
        m_pollerConfig = pollerConfig;
        m_pkg = pkg;
        m_timer = timer;
        m_locationAwarePollerClient = Objects.requireNonNull(locationAwarePollerClient);
        m_latencyStoringServiceMonitorAdaptor = new LatencyStoringServiceMonitorAdaptor(pollerConfig, pkg, persisterFactory, thresholdingService);
        m_statusStoringServiceMonitorAdaptor = new StatusStoringServiceMonitorAdaptor(pollerConfig, pkg, persisterFactory);
        m_DeviceConfigMonitorAdaptor = serviceMonitorAdaptor;
        m_pollOutagesDao = Objects.requireNonNull(pollOutagesDao);

        this.findService();
    }

    private synchronized void findService() {
        final Package.ServiceMatch service = m_pkg.findService(m_service.getSvcName())
                .orElseThrow(() -> new RuntimeException("Service name not part of package!"));

        m_configService = service.service;
        m_patternVariables = service.patternVariables;
    }

    @Override
    public CompletionStage<PollStatus> asyncPoll() {
        // Use the service's configured interval as the TTL for this request
        final Long ttlInMs = m_configService.getInterval();
        return m_locationAwarePollerClient.poll()
                .withService(m_service)
                .withMonitorLocator(m_pollerConfig.getServiceMonitorLocator(m_configService.getName()).orElseThrow())
                .withTimeToLive(ttlInMs)
                .withAttributes(getParameters())
                .withAdaptor(m_latencyStoringServiceMonitorAdaptor)
                .withAdaptor(m_statusStoringServiceMonitorAdaptor)
                .withAdaptor(m_invertedStatusServiceMonitorAdaptor)
                .withAdaptor(m_DeviceConfigMonitorAdaptor)
                .withPatternVariables(m_patternVariables)
                .execute()
                .thenApply(PollerResponse::getPollStatus);
    }

    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    @Override
    public PollStatus poll() {
        try {
            final String packageName = getPackageName();
            LOG.debug("Polling {} with TTL {} using pkg {}", m_service, m_configService.getInterval(), packageName);

            PollStatus result = asyncPoll().toCompletableFuture().get();

            LOG.debug("Finish polling {} using pkg {} result = {}", m_service, packageName, result);

            // Track the results of the poll
            m_service.getContext().trackPoll(m_service, result);
            return result;
        } catch (Throwable t) {
            return errorToPollStatus(m_service, t);
        }
    }

    public static PollStatus errorToPollStatus(PollableService service, Throwable t) {
        return RpcExceptionUtils.handleException(t, new RpcExceptionHandler<>() {
            @Override
            public PollStatus onInterrupted(Throwable cause) {
                LOG.warn("Interrupted while invoking the poll for {}."
                        + " Marking the service as UNKNOWN.", service);
                return PollStatus.unknown("Interrupted while invoking the poll for "+service+". "+t);
            }

            @Override
            public PollStatus onTimedOut(Throwable cause) {
                LOG.warn("No response received when remotely invoking the poll for {}."
                        + " Marking the service as UNKNOWN.", service);
                return PollStatus.unknown(String.format("No response received for %s. %s", service, cause));
            }

            @Override
            public PollStatus onRejected(Throwable cause) {
                LOG.warn("The request to remotely invoke the poll for {} was rejected."
                        + " Marking the service as UNKNOWN.", service);
                return PollStatus.unknown(String.format("Remote poll request rejected for %s. %s", service, cause));
            }

            @Override
            public PollStatus onUnknown(Throwable cause) {
                LOG.error("Unexpected exception while polling {}. Marking service as DOWN", service, t);
                return PollStatus.down("Unexpected exception while polling "+service+". "+t);
            }
        });
    }

    /**
     * Uses the existing package name to try and re-obtain the package from the poller config factory.
     * Should be called when the poller config has been reloaded.
     */
    @Override
    public synchronized void refresh() {
        Package newPkg = m_pollerConfig.getPackage(m_pkg.getName());
        if (newPkg == null) {
            LOG.warn("Package named {} no longer exists.", m_pkg.getName());
        }
        m_pkg = newPkg;

        this.findService();
    }

    private synchronized Map<String,Object> getParameters() {
        if (m_parameters == null) {
            m_parameters = m_configService.getParameterMap();
        }
        return m_parameters;
    }

    /**
     * <p>getCurrentTime</p>
     *
     * @return a long.
     */
    @Override
    public long getCurrentTime() {
        return m_timer.getCurrentTime();
    }

    /**
     * <p>getInterval</p>
     *
     * @return a long.
     */
    @Override
    public synchronized long getInterval() {
        if (m_service.isDeleted()) {
            LOG.debug("getInterval(): {} is deleted", m_service);
            return -1;
        }

        long when = m_configService.getInterval();
        boolean ignoreUnmanaged = false;

        if (m_service.getStatus().isDown()) {
            final long downFor = m_timer.getCurrentTime() - m_service.getStatusChangeTime();
            LOG.debug("getInterval(): Service {} has been down for {} seconds, checking downtime model.", m_service, TimeUnit.SECONDS.convert(downFor, TimeUnit.MILLISECONDS));
            boolean matched = false;
            for (final Downtime dt : m_pkg.getDowntimes()) {
                LOG.debug("getInterval(): Checking downtime: {}", dt);
                if (dt.getBegin() <= downFor) {
                    LOG.debug("getInterval(): begin ({}) <= {}", dt.getBegin(), downFor);
                    final String delete = dt.getDelete();
                    if (Downtime.DELETE_ALWAYS.equals(delete)) {
                        when = -1;
                        ignoreUnmanaged = true;
                        matched = true;
                    } else if (Downtime.DELETE_MANAGED.equals(delete)) {
                        when = -1;
                        matched = true;
                    }
                    else if (dt.getEnd() != null && dt.getEnd() > downFor) {
                        // in this interval
                        when = dt.getInterval();
                        matched = true;
                    } else // no end
                    {
                        when = dt.getInterval();
                        matched = true;
                    }
                }
            }
            LOG.debug("getInterval(): when={}, matched={}, ignoreUnmanaged={}", when, matched, ignoreUnmanaged);
            if (!matched) {
                LOG.error("Downtime model is invalid on package {}, cannot schedule service {}", m_pkg.getName(), m_service);
                return -1;
            }
        }

        if (when < 0) {
            m_service.sendDeleteEvent(ignoreUnmanaged);
        }

        return when;
    }

    /**
     * <p>scheduledSuspension</p>
     *
     * @return a boolean.
     */
    @Override
    public synchronized boolean scheduledSuspension() {
        long nodeId=m_service.getNodeId();
        for (String outageName : m_pkg.getOutageCalendars()) {
            // Does the outage apply to the current time?
            if (m_pollOutagesDao.isTimeInOutage(m_timer.getCurrentTime(), outageName)) {
                // Does the outage apply to this interface?

                if (m_pollOutagesDao.isNodeIdInOutage(nodeId, outageName) || 
                        (m_pollOutagesDao.isInterfaceInOutage(m_service.getIpAddr(), outageName)) || 
                        (m_pollOutagesDao.isInterfaceInOutage("match-any", outageName))) {
                    LOG.debug("scheduledOutage: configured outage '{}' applies, {} will not be polled.", outageName, m_configService);
                    return true;
                }
            }
        }
        return false;
    }

    public synchronized String getPackageName() {
        return m_pkg.getName();
    }

    public int getNodeId() {
        return m_service.getNodeId();
    }
}
