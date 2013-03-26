package org.opennms.netmgt.accesspointmonitor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.AccessPointDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.annotations.EventHandler;
import org.opennms.netmgt.model.events.annotations.EventListener;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfig;
import org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfigFactory;
import org.opennms.netmgt.config.accesspointmonitor.Package;
import org.opennms.netmgt.config.accesspointmonitor.Service;

// TODO: Set the polling context class type using Spring.

/**
 * Access Point Monitor daemon class: Initializes and schedules the defined
 * services Filters are compiled when the service is scheduled, interfaces are
 * to poll are matched at runtime
 * 
 * @author <a href="mailto:jwhite@datavalet.com">Jesse White</a>
 */
@EventListener(name = "AccessPointMonitor")
public class AccessPointMonitord extends AbstractServiceDaemon implements ReadyRunnable {
    private static final String DAEMON_NAME = "AccessPointMonitor";

    private static AccessPointMonitord m_singleton = new AccessPointMonitord();
    private boolean m_initialized = false;
    private LegacyScheduler m_scheduler = null;
    private EventIpcManager m_eventMgr = null;
    private AccessPointMonitorConfig m_pollerConfig;
    private AccessPointDao m_accessPointDao;
    private NodeDao m_nodeDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private volatile Map<String, PollingContext> m_activePollers = new HashMap<String, PollingContext>();

    /**
     * <p>
     * isInitialized
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isInitialized() {
        return m_initialized;
    }

    /**
     * <p>
     * getAccessPointDao
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.dao.AccessPointDao} object.
     */
    public AccessPointDao getAccessPointDao() {
        return m_accessPointDao;
    }

    /**
     * <p>
     * getAccessPointDao
     * </p>
     * 
     * @param accessPointDao
     *            a {@link org.opennms.netmgt.dao.AccessPointDao} object.
     */
    public void setAccessPointDao(AccessPointDao accessPointDao) {
        m_accessPointDao = accessPointDao;
    }

    /**
     * <p>
     * getNodeDao
     * </p>
     * 
     * @param a
     *            {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    /**
     * <p>
     * getNodeDao
     * </p>
     * 
     * @param nodeDao
     *            a {@link org.opennms.netmgt.dao.NodeDao} object.
     */
    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    /**
     * <p>
     * getIpInterfaceDao
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.dao.IpInterfaceDao} object.
     */
    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    /**
     * <p>
     * setIpInterfaceDao
     * </p>
     * 
     * @param ipInterfaceDao
     *            a {@link org.opennms.netmgt.dao.IpInterfaceDao} object.
     */
    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    /**
     * <p>
     * getScheduler
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.scheduler.Scheduler} object.
     */
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * <p>
     * setScheduler
     * </p>
     * 
     * @param scheduler
     *            a {@link org.opennms.netmgt.scheduler.LegacyScheduler}
     *            object.
     */
    public void setScheduler(LegacyScheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>
     * getEventManager
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    /**
     * <p>
     * setEventManager
     * </p>
     * 
     * @param eventMgr
     *            a {@link org.opennms.netmgt.eventd.EventIpcManager} object.
     */
    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    /**
     * <p>
     * getPollerConfig
     * </p>
     * 
     * @return a
     *         {@link org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfig}
     *         object.
     */
    public AccessPointMonitorConfig getPollerConfig() {
        return m_pollerConfig;
    }

    /**
     * <p>
     * setPollerConfig
     * </p>
     * 
     * @param accesspointmonitorConfig
     *            a
     *            {@link org.opennms.netmgt.config.accesspointmonitor.AccessPointMonitorConfig}
     *            object.
     */
    public void setPollerConfig(AccessPointMonitorConfig accesspointmonitorConfig) {
        m_pollerConfig = accesspointmonitorConfig;
    }

    /**
     * <p>
     * onStart
     * </p>
     */
    @Override
    protected void onStart() {
        try {
            log().debug("onStart: Starting Access Point Monitor scheduler");

            // Start the scheduler
            getScheduler().start();
        } catch (RuntimeException e) {
            log().fatal("onStart: Failed to start scheduler", e);
            throw e;
        }

    }

    /**
     * <p>
     * onStop
     * </p>
     */
    @Override
    protected void onStop() {

        if (getScheduler() != null) {
            log().debug("onStop: stopping scheduler");
            getScheduler().stop();
        }

        setScheduler(null);

        log().debug("onStop: releasing pollers");
        synchronized (m_activePollers) {
            if (m_activePollers != null) {
                for (PollingContext p : m_activePollers.values()) {
                    p.release();
                }
                m_activePollers.clear();
            }
        }
    }

    /**
     * <p>
     * onPause
     * </p>
     */
    @Override
    protected void onPause() {
        getScheduler().pause();
    }

    /**
     * <p>
     * onResume
     * </p>
     */
    @Override
    protected void onResume() {
        getScheduler().resume();
    }

    /**
     * <p>
     * setInstance
     * </p>
     * 
     * @param apm
     *            a
     *            {@link org.opennms.netmgt.accesspointmonitor.AccessPointMonitord}
     *            object.
     */
    public static void setInstance(AccessPointMonitord apm) {
        m_singleton = apm;
    }

    /**
     * <p>
     * getInstance
     * </p>
     * 
     * @return a
     *         {@link org.opennms.netmgt.accesspointmonitor.AccessPointMonitord}
     *         object.
     */
    public static AccessPointMonitord getInstance() {
        return m_singleton;
    }

    /**
     * <p>
     * Default constructor for AccessPointMonitord.
     * </p>
     */
    public AccessPointMonitord() {
        super("OpenNMS.AccessPointMonitor");
    }

    /** {@inheritDoc} */
    @Override
    protected void onInit() {
        createScheduler();

        synchronized (m_activePollers) {
            m_activePollers.clear();
        }

        // Schedule the packages that are defined in the configuration file
        log().debug("onInit: Scheduling packages for polling");
        scheduleStaticPackages();
        scheduleDynamicPackages();

        getScheduler().schedule(getPollerConfig().getPackageScanInterval(), this);

        m_initialized = true;
    }

    /**
     * <p>
     * createScheduler
     * </p>
     * Initializes the scheduler.
     */
    private void createScheduler() {
        // Create a scheduler
        try {
            log().debug("init: Creating Access Point Monitor scheduler");

            setScheduler(new LegacyScheduler(DAEMON_NAME, getPollerConfig().getThreads()));
        } catch (RuntimeException e) {
            log().fatal("init: Failed to create Access Point Monitor scheduler", e);
            throw e;
        }
    }

    /**
     * <p>
     * scheduleDynamicPackages
     * </p>
     * Schedules packages that have a wild-card in their name. Remove
     * scheduled packages that are no longer present in the database.
     */
    private void scheduleDynamicPackages() {
        log().debug("scheduleDynamicPackages() was triggered");

        // Build the current list of dynamic packages
        Map<String, Package> dynamicPackages = new HashMap<String, Package>();

        for (Package pkg : m_pollerConfig.getPackages()) {
            if (!pkg.nameHasWildcard()) {
                continue;
            }

            for (String pkgName : m_accessPointDao.findDistinctPackagesLike(pkg.getName())) {
                Package newPkg = new Package(pkg);
                newPkg.setName(pkgName);
                newPkg.setIsDynamic(true);
                dynamicPackages.put(pkgName, newPkg);
            }
        }

        for (Package pkg : dynamicPackages.values()) {
            synchronized (m_activePollers) {
                PollingContext p = m_activePollers.get(pkg.getName());
                if (p != null) {
                    if (p.getPackage().getIsDynamic()) {
                        log().debug("Package '" + pkg.getName() + "' is already active.");
                    } else {
                        log().error("Package '" + pkg.getName() + "' is statically defined and matches a dynamic definitions.");
                    }
                } else {
                    schedulePackage(pkg);
                }
            }
        }

        // Iterate over all of the active pollers
        synchronized (m_activePollers) {
            Iterator<Map.Entry<String, PollingContext>> entries = m_activePollers.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, PollingContext> entry = entries.next();
                PollingContext p = entry.getValue();
                Package pkg = p.getPackage();
                // Un-schedule the package if its dynamic and not in the list
                // of packages we just discovered
                if (pkg.getIsDynamic() && !dynamicPackages.containsKey(entry.getKey())) {
                    log().debug("unscheduling " + pkg.getName());
                    p.release();
                    entries.remove();
                }
            }
        }
    }

    /**
     * <p>
     * scheduleDynamicPackages
     * </p>
     * Schedules packages without a wild-card in their name.
     */
    private void scheduleStaticPackages() {
        for (Package pkg : m_pollerConfig.getPackages()) {
            if (pkg.nameHasWildcard())
                continue;
            schedulePackage(pkg);
        }
    }

    /**
     * <p>
     * schedulePackage
     * </p>
     * Schedules packages without a wild-card in their name.
     */
    private void schedulePackage(Package pkg) {
        Service svc = pkg.getEffectiveService();

        // Create a new polling context
        PollingContext p = new DefaultPollingContext();

        // Initialise the context
        p.setPackage(pkg);
        p.setPollerConfig(getPollerConfig());
        p.setIpInterfaceDao(getIpInterfaceDao());
        p.setNodeDao(getNodeDao());
        p.setAccessPointDao(getAccessPointDao());
        p.setEventManager(getEventManager());
        p.setScheduler(getScheduler());
        p.setInterval(svc.getInterval());
        p.setPropertyMap(svc.getParameterMap());
        p.init();

        // Schedule it
        log().debug("schedulePackages: Scheduling " + pkg.getName() + " every " + svc.getInterval());
        getScheduler().schedule(svc.getInterval(), p);

        // Store in the map
        synchronized (m_activePollers) {
            m_activePollers.put(pkg.getName(), p);
        }
    }

    /**
     * <p>
     * initializeConfiguration
     * </p>
     */
    private void initializeConfiguration() throws IOException, JAXBException {
        setPollerConfig(AccessPointMonitorConfigFactory.getInstance().getConfig());
    }

    /**
     * <p>
     * reloadAndReStart
     * </p>
     */
    private void reloadAndReStart() {
        EventBuilder ebldr = null;
        try {
            initializeConfiguration();
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, DAEMON_NAME);
            this.stop();
            this.init();
            this.start();
        } catch (JAXBException e) {
            fatalf(e, "Unable to initialize the Access Point Monitor configuration factory");
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, DAEMON_NAME);
            ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
        } catch (IOException e) {
            fatalf(e, "Unable to initialize the Access Point Monitor configuration factory");
            ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, getName());
            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, DAEMON_NAME);
            ebldr.addParam(EventConstants.PARM_REASON, e.getLocalizedMessage().substring(0, 128));
        }
        m_eventMgr.sendNow(ebldr.getEvent());
    }

    /**
     * <p>
     * reloadDaemonConfig
     * </p>
     * 
     * @param e
     *            a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    @EventHandler(uei = EventConstants.RELOAD_DAEMON_CONFIG_UEI)
    public void reloadDaemonConfig(Event e) {
        log().info("reloadDaemonConfig: processing reload daemon event...");
        if (isReloadConfigEventTarget(e)) {
            reloadAndReStart();
        }
        log().info("reloadDaemonConfig: reload daemon event processed.");
    }

    /**
     * <p>
     * isReloadConfigEventTarget
     * </p>
     * 
     * @param e
     *            a {@link org.opennms.netmgt.xml.event.Event} object.
     * @return a boolean
     */
    private boolean isReloadConfigEventTarget(Event e) {
        boolean isTarget = false;

        final List<Parm> parmCollection = e.getParmCollection();

        for (final Parm parm : parmCollection) {
            if (EventConstants.PARM_DAEMON_NAME.equals(parm.getParmName()) && DAEMON_NAME.equalsIgnoreCase(parm.getValue().getContent())) {
                isTarget = true;
                break;
            }
        }

        log().debug("isReloadConfigEventTarget: AccessPointMonitor was target of reload event: " + isTarget);
        return isTarget;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
        scheduleDynamicPackages();

        // Reschedule the dynamic package check
        getScheduler().schedule(getPollerConfig().getPackageScanInterval(), this);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * <p>
     * getActivePackageNames
     * </p>
     * Returns the set of package names that are currently scheduled.
     * 
     * @return a Set<String> object.
     */
    public Set<String> getActivePackageNames() {
        synchronized (m_activePollers) {
            return m_activePollers.keySet();
        }
    }
}
