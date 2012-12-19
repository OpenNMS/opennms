package org.opennms.netmgt.accesspointmonitor;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.hibernate.criterion.Restrictions;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.accesspointmonitor.poller.AccessPointPoller;
import org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfig;
import org.opennms.netmgt.config.accesspointmonitor.Package;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.AccessPointStatus;
import org.opennms.netmgt.model.OnmsAccessPoint;
import org.opennms.netmgt.model.OnmsAccessPointCollection;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsIpInterfaceList;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxyException;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * Default polling context that is instantiated on a per package basis.
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 * @version $Id: $
 */
public class DefaultPollingContext implements PollingContext {

    private static final String PASSIVE_STATUS_UEI = "uei.opennms.org/services/passiveServiceStatus";

    private ThreadCategory m_log;
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

    public DefaultPollingContext() {
        m_log = ThreadCategory.getInstance(getClass());
    }

    public void setPackage(Package pkg) {
        m_package = pkg;
    }

    public Package getPackage() {
        return m_package;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setAccessPointDao(AccessPointDao accessPointDao) {
        m_accessPointDao = accessPointDao;
    }

    public AccessPointDao getAccessPointDao() {
        return m_accessPointDao;
    }

    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        return m_scheduler;
    }

    public void setInterval(long interval) {
        m_interval = interval;
    }

    public long getInterval() {
        return m_interval;
    }

    public void setPropertyMap(Map<String, String> parameters) {
        m_parameters = parameters;
    }

    public Map<String, String> getPropertyMap() {
        return m_parameters;
    }

    public void setPollerConfig(AccessPointMonitorConfig accesspointmonitorConfig) {
        m_pollerConfig = accesspointmonitorConfig;
    }

    public AccessPointMonitorConfig getPollerConfig() {
        return m_pollerConfig;
    }

    public ReadyRunnable getReadyRunnable() {
        return this;
    }

    public void init() {
        // Fire up a thread pool
        m_pool = Executors.newFixedThreadPool(getPackage().getEffectiveService().getThreads());
    }

    public void release() {
        // Shutdown the thread pool
        m_pool.shutdown();
        // Set the pool to null so that isReady returns false
        m_pool = null;
    }

    public void run() {
        // Determine the list of interfaces to poll at runtime
        OnmsIpInterfaceList ifaces = getInterfaceList();

        // If the list of interfaces is empty, print a warning message
        if (ifaces.isEmpty()) {
            log().warn("Package '" + getPackage().getName() + "' was scheduled, but no interfaces were matched.");
        }

        // Get the complete list of APs that we are responsible for polling
        OnmsAccessPointCollection apsDown = m_accessPointDao.findByPackage(getPackage().getName());
        if (log().isDebugEnabled()) {
            log().debug("Found " + apsDown.size() + " APs in package '" + getPackage().getName() + "'");
        }

        // Keep track of all APs that we've confirmed to be ONLINE
        OnmsAccessPointCollection apsUp = new OnmsAccessPointCollection();

        Set<Callable<OnmsAccessPointCollection>> callables = new HashSet<Callable<OnmsAccessPointCollection>>();

        // Iterate over all of the matched interfaces
        for (Iterator<OnmsIpInterface> it = ifaces.iterator(); it.hasNext();) {
            OnmsIpInterface iface = it.next();

            // Create a new instance of the poller
            AccessPointPoller p = m_package.getPoller(m_pollerConfig.getMonitors());
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
                log().warn("run() called, but no thread pool has been initialized.  Calling init()");
                init();
            }

            // Invoke all of the pollers using the thread pool
            List<Future<OnmsAccessPointCollection>> futures = m_pool.invokeAll(callables);

            // Gather the list of APs that are ONLINE
            for (Future<OnmsAccessPointCollection> future : futures) {
                try {
                    apsUp.addAll(future.get());
                    succesfullyPolledAController = true;
                } catch (ExecutionException e) {
                    log().error("An error occurred while polling :" + e);
                }
            }
        } catch (InterruptedException e) {
            log().error("I was interrupted :" + e);
        }

        // Remove the APs from the list that are ONLINE
        apsDown.removeAll(apsUp);

        if (log().isDebugEnabled()) {
            log().debug("(" + apsUp.size() + ") APs Online, (" + apsDown.size() + ") APs offline in package '" + getPackage().getName() + "'");
        }

        if (!succesfullyPolledAController) {
            log().warn("Failed to poll at least one controller in the package '" + getPackage().getName() + "'");
        }

        updateApStatus(apsUp, apsDown);

        // Reschedule the service
        log().debug("Re-scheduling the package '" + getPackage().getName() + "' in " + m_interval);
        m_scheduler.schedule(m_interval, getReadyRunnable());
    }

    private void updateApStatus(OnmsAccessPointCollection apsUp, OnmsAccessPointCollection apsDown) {
        // Update the AP status in the database and send the appropriate
        // events
        for (OnmsAccessPoint ap : apsUp) {
            // Update the status in the database
            ap.setStatus(AccessPointStatus.ONLINE);
            m_accessPointDao.update(ap);

            try {
                // Generate an AP UP event
                Event e = createApStatusEvent(ap.getPhysAddr(), ap.getNodeId(), "UP");
                m_eventMgr.send(e);
            } catch (EventProxyException e) {
                log().fatal("Error occured sending events ", e);
            }
        }

        // Update the AP status in the database and send the appropriate
        // events
        for (OnmsAccessPoint ap : apsDown) {
            // Update the status in the database
            ap.setStatus(AccessPointStatus.OFFLINE);
            m_accessPointDao.update(ap);

            try {
                // Generate an AP DOWN event
                Event e = createApStatusEvent(ap.getPhysAddr(), ap.getNodeId(), "DOWN");
                m_eventMgr.send(e);
            } catch (EventProxyException e) {
                log().fatal("Error occured sending events ", e);
            }
        }

        m_accessPointDao.flush();
    }

    protected OnmsIpInterfaceList getInterfaceList() {
        StringBuffer filterRules = new StringBuffer(getPackage().getEffectiveFilter());
        List<InetAddress> ipList = FilterDaoFactory.getInstance().getActiveIPAddressList(filterRules.toString());

        OnmsIpInterfaceList ifaces = new OnmsIpInterfaceList();
        // Only poll the primary interface
        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.add(Restrictions.sqlRestriction("issnmpprimary = 'P'"));

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
        final OnmsCriteria criteria = new OnmsCriteria(OnmsIpInterface.class);
        criteria.add(Restrictions.sqlRestriction("nodeid = " + node.getId()));
        List<OnmsIpInterface> matchingIfaces = getIpInterfaceDao().findMatching(criteria);
        return matchingIfaces.get(0).getIpAddress();
    }

    protected Event createApStatusEvent(String physAddr, Integer nodeId, String status) {
        final List<Parm> parms = new ArrayList<Parm>();

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

    protected Parm buildParm(String parmName, String parmValue) {
        Value v = new Value();
        v.setContent(parmValue);
        Parm p = new Parm();
        p.setParmName(parmName);
        p.setValue(v);
        return p;
    }

    public boolean isReady() {
        return m_pool != null;
    }

    private ThreadCategory log() {
        return m_log;
    }
}
