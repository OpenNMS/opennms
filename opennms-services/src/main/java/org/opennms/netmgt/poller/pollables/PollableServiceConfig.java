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

package org.opennms.netmgt.poller.pollables;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.scheduler.ScheduleInterval;
import org.opennms.netmgt.scheduler.Timer;
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
    private PollOutagesConfig m_pollOutagesConfig;
    private PollableService m_service;
    private Map<String,Object> m_parameters = null;
    private Package m_pkg;
    private Timer m_timer;
    private Service m_configService;
    private ServiceMonitor m_serviceMonitor;

    /**
     * <p>Constructor for PollableServiceConfig.</p>
     *
     * @param svc a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     * @param pollerConfig a {@link org.opennms.netmgt.config.PollerConfig} object.
     * @param pollOutagesConfig a {@link org.opennms.netmgt.config.PollOutagesConfig} object.
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @param timer a {@link org.opennms.netmgt.scheduler.Timer} object.
     */
    public PollableServiceConfig(PollableService svc, PollerConfig pollerConfig, PollOutagesConfig pollOutagesConfig, Package pkg, Timer timer) {
        m_service = svc;
        m_pollerConfig = pollerConfig;
        m_pollOutagesConfig = pollOutagesConfig;
        m_pkg = pkg;
        m_timer = timer;
        m_configService = findService(pkg);

        ServiceMonitor monitor = getServiceMonitor();
        monitor.initialize(m_service);
    }

    /**
     * @param pkg
     * @return
     */
    private synchronized Service findService(Package pkg) {
        for (Service s : m_pkg.getServices()) {
            if (s.getName().equalsIgnoreCase(m_service.getSvcName())) {
                return s;
            }
        }

        throw new RuntimeException("Service name not part of package!");

    }

    /**
     * <p>poll</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    @Override
    public PollStatus poll() {
        String packageName = null;
        synchronized(this) {
            packageName = m_pkg.getName();
        }
        try {
            ServiceMonitor monitor = getServiceMonitor();
            LOG.debug("Polling {} using pkg {}", packageName, m_service);
            PollStatus result = monitor.poll(m_service, getParameters());
            LOG.debug("Finish polling {} using pkg {} result = {}", result, m_service, packageName);
            return result;
        } catch (Throwable e) {
            LOG.error("Unexpected exception while polling {}. Marking service as DOWN", m_service, e);
            return PollStatus.down("Unexpected exception while polling "+m_service+". "+e);
        }
    }

    private synchronized ServiceMonitor getServiceMonitor() {
        if (m_serviceMonitor == null) {
            ServiceMonitor monitor = m_pollerConfig.getServiceMonitor(m_service.getSvcName());
            m_serviceMonitor = new LatencyStoringServiceMonitorAdaptor(monitor, m_pollerConfig, m_pkg);

        }
        return m_serviceMonitor;
    }

    void setServiceMonitor(final ServiceMonitor serviceMonitor) {
        m_serviceMonitor = serviceMonitor;
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
        m_configService = findService(m_pkg);
        m_parameters = null;

    }

    /**
     * Should be called when thresholds configuration has been reloaded
     */
    @Override
    public synchronized void refreshThresholds() {
        ((LatencyStoringServiceMonitorAdaptor)getServiceMonitor()).refreshThresholds();
    }


    /**
     * @return
     */
    private synchronized Map<String,Object> getParameters() {
        if (m_parameters == null) {
            m_parameters = createParameterMap(m_configService);
        }
        return m_parameters;
    }

    private Map<String,Object> createParameterMap(final Service svc) {
        final Map<String,Object> m = new ConcurrentSkipListMap<String,Object>();
        for (final Parameter p : svc.getParameters()) {
            Object val = p.getValue();
            if (val == null) {
                val = (p.getAnyObject() == null ? "" : p.getAnyObject());
            }

            m.put(p.getKey(), val);
        }
        return m;
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
        LOG.debug("getInterval()", new Exception());
        if (m_service.isDeleted()) {
            LOG.debug("getInterval(): {} is deleted", m_service);
            return -1;
        }

        long when = m_configService.getInterval();

        if (m_service.getStatus().isDown()) {
            final long downFor = m_timer.getCurrentTime() - m_service.getStatusChangeTime();
            LOG.debug("getInterval(): Service {} has been down for {} seconds, checking downtime model.", m_service, TimeUnit.SECONDS.convert(downFor, TimeUnit.MILLISECONDS));
            boolean matched = false;
            for (final Downtime dt : m_pkg.getDowntimes()) {
                LOG.debug("getInterval(): Checking downtime: {}", dt);
                if (dt.getBegin() <= downFor) {
                    LOG.debug("getInterval(): begin ({}) <= {}", dt.getBegin(), downFor);
                    if (isTrue(dt.getDelete())) {
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
            LOG.debug("getInterval(): when={}, matched={}", when, matched);
            if (!matched) {
                LOG.error("Downtime model is invalid on package {}, cannot schedule service {}", m_pkg.getName(), m_service);
                return -1;
            }
        }

        if (when < 0) {
            m_service.sendDeleteEvent();
        }

        return when;
    }

    private boolean isTrue(final String value) {
        return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("yes"));
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
            if (m_pollOutagesConfig.isTimeInOutage(m_timer.getCurrentTime(), outageName)) {
                // Does the outage apply to this interface?

                if (m_pollOutagesConfig.isNodeIdInOutage(nodeId, outageName) || 
                        (m_pollOutagesConfig.isInterfaceInOutage(m_service.getIpAddr(), outageName)) || 
                        (m_pollOutagesConfig.isInterfaceInOutage("match-any", outageName))) {
                    LOG.debug("scheduledOutage: configured outage '{}' applies, {} will not be polled.", outageName, m_configService);
                    return true;
                }
            }
        }

        //return outageFound;
        return false;
    }

}
