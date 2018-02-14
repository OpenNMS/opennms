/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.accesspointmonitor;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.opennms.core.criteria.Alias;
import org.opennms.core.criteria.Alias.JoinType;
import org.opennms.core.criteria.Criteria;
import org.opennms.core.criteria.restrictions.EqRestriction;
import org.opennms.netmgt.accesspointmonitor.poller.AccessPointPoller;
import org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfig;
import org.opennms.netmgt.config.accesspointmonitor.Package;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventProxyException;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.AccessPointStatus;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsAccessPointCollection;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default polling context that is instantiated on a per package basis.
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 * @version $Id: $
 */
public class DefaultPollingContext implements PollingContext {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultPollingContext.class);

    private static final String PASSIVE_STATUS_UEI = "uei.opennms.org/services/passiveServiceStatus";

    private EventIpcManager m_eventMgr;
    private IpInterfaceDao m_ipInterfaceDao;
    private NodeDao m_nodeDao;
    private AccessPointDao m_accessPointDao;
    private Map<String, String> m_parameters;
    private Package m_package;
    private Scheduler m_scheduler;
    private long m_interval;
    private AccessPointMonitorConfig m_pollerConfig;
    private ExecutorService m_pool = null;


    @Override
    public void setPackage(Package pkg) {
        m_package = pkg;
    }

    @Override
    public Package getPackage() {
        return m_package;
    }

    @Override
    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    @Override
    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    @Override
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    @Override
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    @Override
    public void setAccessPointDao(AccessPointDao accessPointDao) {
        m_accessPointDao = accessPointDao;
    }

    @Override
    public AccessPointDao getAccessPointDao() {
        return m_accessPointDao;
    }

    @Override
    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    @Override
    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    @Override
    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    @Override
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    @Override
    public void setInterval(long interval) {
        m_interval = interval;
    }

    @Override
    public long getInterval() {
        return m_interval;
    }

    @Override
    public void setPropertyMap(Map<String, String> parameters) {
        m_parameters = parameters;
    }

    @Override
    public Map<String, String> getPropertyMap() {
        return m_parameters;
    }

    @Override
    public void setPollerConfig(AccessPointMonitorConfig accesspointmonitorConfig) {
        m_pollerConfig = accesspointmonitorConfig;
    }

    @Override
    public AccessPointMonitorConfig getPollerConfig() {
        return m_pollerConfig;
    }

    public ReadyRunnable getReadyRunnable() {
        return this;
    }

    @Override
    public void init() {
        // Fire up a thread pool
        m_pool = Executors.newFixedThreadPool(getPackage().getEffectiveService().getThreads());
    }

    @Override
    public void release() {
        // Shutdown the thread pool
        m_pool.shutdown();
        // Set the pool to null so that isReady returns false
        m_pool = null;
    }

    @Override
    @Transactional
    public void run() {
        // Determine the list of interfaces to poll at runtime
        OnmsIpInterfaceList ifaces = getInterfaceList();

        // If the list of interfaces is empty, print a warning message
        if (ifaces.getIpInterfaces().isEmpty()) {
            LOG.warn("Package '{}' was scheduled, but no interfaces were matched.", getPackage().getName());
        }

        // Get the complete list of APs that we are responsible for polling
        OnmsAccessPointCollection apsDown = m_accessPointDao.findByPackage(getPackage().getName());
        LOG.debug("Found {} APs in package '{}'", apsDown.size(), getPackage().getName());

        // Keep track of all APs that we've confirmed to be ONLINE
        OnmsAccessPointCollection apsUp = new OnmsAccessPointCollection();

        Set<Callable<OnmsAccessPointCollection>> callables = new HashSet<Callable<>();

        // Iterate over all of the matched interfaces
        for (final OnmsIpInterface iface : ifaces.getIpInterfaces()) {
            // Create a new instance of the poller
            final AccessPointPoller p = m_package.getPoller(m_pollerConfig.getMonitors());
            p.setInterfaceToPoll(iface);
            p.setAccessPointDao(m_accessPointDao);
            p.setPackage(m_package);
            p.setPropertyMap(m_parameters);

            // Schedule the poller for execution
            callables.add(p);
        }

        boolean succesfullyPolledAController = false;

        try {
            if (m_pool == null) {
                LOG.warn("run() called, but no thread pool has been initialized.  Calling init()");
                init();
            }

            // Invoke all of the pollers using the thread pool
            List<Future<OnmsAccessPointCollection>> futures = m_pool.invokeAll(callables);

            // Gather the list of APs that are ONLINE
            for (Future<OnmsAccessPointCollection> future : futures) {
                try {
                    apsUp.addAll(future.get().getObjects());
                    succesfullyPolledAController = true;
                } catch (ExecutionException e) {
                    LOG.error("An error occurred while polling", e);
                }
            }
        } catch (InterruptedException e) {
            LOG.error("I was interrupted", e);
        }

        // Remove the APs from the list that are ONLINE
        apsDown.removeAll(apsUp.getObjects());

        LOG.debug("({}) APs Online, ({}) APs offline in package '{}'", apsUp.size(), apsDown.size(), getPackage().getName());

        if (!succesfullyPolledAController) {
            LOG.warn("Failed to poll at least one controller in the package '{}'", getPackage().getName());
        }

        updateApStatus(apsUp, apsDown);

        // Reschedule the service
        LOG.debug("Re-scheduling the package '{}' in {}", getPackage().getName(), m_interval);
        m_scheduler.schedule(m_interval, getReadyRunnable());
    }

    private void updateApStatus(OnmsAccessPointCollection apsUp, OnmsAccessPointCollection apsDown) {
        // Update the AP status in the database and send the appropriate
        // events
        for (OnmsAccessPoint ap : apsUp) {
            // Update the status in the database
            ap.setStatus(AccessPointStatus.ONLINE);
            // Use merge() here because the object may have been updated in a separate thread
            m_accessPointDao.merge(ap);

            try {
                // Generate an AP UP event
                Event e = createApStatusEvent(ap.getPhysAddr(), ap.getNodeId(), "UP");
                m_eventMgr.send(e);
            } catch (EventProxyException e) {
                LOG.error("Error occured sending events ", e);
            }
        }

        // Update the AP status in the database and send the appropriate
        // events
        for (OnmsAccessPoint ap : apsDown) {
            // Update the status in the database
            ap.setStatus(AccessPointStatus.OFFLINE);
            // Use merge() here because the object may have been updated in a separate thread
            m_accessPointDao.merge(ap);

            try {
                // Generate an AP DOWN event
                Event e = createApStatusEvent(ap.getPhysAddr(), ap.getNodeId(), "DOWN");
                m_eventMgr.send(e);
            } catch (EventProxyException e) {
                LOG.error("Error occured sending events ", e);
            }
        }

        m_accessPointDao.flush();
    }

    protected OnmsIpInterfaceList getInterfaceList() {
        StringBuffer filterRules = new StringBuffer(getPackage().getEffectiveFilter());
        List<InetAddress> ipList = FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules.toString());

        OnmsIpInterfaceList ifaces = new OnmsIpInterfaceList();
        // Only poll the primary interface
        final Criteria criteria = new Criteria(OnmsIpInterface.class);
        criteria.addRestriction(new EqRestriction("isSnmpPrimary", PrimaryType.PRIMARY));

        List<OnmsIpInterface> allValidIfaces = getIpInterfaceDao().findMatching(criteria);
        for (OnmsIpInterface iface : allValidIfaces) {
            if (ipList.contains(iface.getIpAddress())) {
                ifaces.add(iface);
            }
        }

        return ifaces;
    }

    /*
     * Return the IP address of the first interface on the node
     */
    protected InetAddress getNodeIpAddress(OnmsNode node) {
        final Criteria criteria = new Criteria(OnmsIpInterface.class)
            .setAliases(Arrays.asList(new Alias[] {
                new Alias("node", "node", JoinType.LEFT_JOIN)
            }))
            .addRestriction(new EqRestriction("node.id", node.getId()));
        List<OnmsIpInterface> matchingIfaces = getIpInterfaceDao().findMatching(criteria);
        return matchingIfaces.get(0).getIpAddress();
    }

    protected Event createApStatusEvent(String physAddr, Integer nodeId, String status) {
        final List<Parm> parms = new ArrayList<>();

        OnmsNode node = getNodeDao().get(nodeId);
        parms.add(buildParm(EventConstants.PARM_PASSIVE_IPADDR, getNodeIpAddress(node).getHostAddress()));
        parms.add(buildParm(EventConstants.PARM_PASSIVE_NODE_LABEL, node.getLabel()));
        parms.add(buildParm(EventConstants.PARM_PASSIVE_SERVICE_NAME, getPackage().getEffectiveService().getPassiveServiceName()));
        parms.add(buildParm(EventConstants.PARM_PASSIVE_SERVICE_STATUS, status));
        parms.add(buildParm("physAddr", physAddr));

        EventBuilder bldr = new EventBuilder(PASSIVE_STATUS_UEI, "accesspointmonitord");
        bldr.setParms(parms);
        return bldr.getEvent();
    }

    protected static Parm buildParm(String parmName, String parmValue) {
        Value v = new Value();
        v.setContent(parmValue);
        Parm p = new Parm();
        p.setParmName(parmName);
        p.setValue(v);
        return p;
    }

    @Override
    public boolean isReady() {
        return m_pool != null;
    }
}
