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

import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.DataSource;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.Querier;
import org.opennms.core.utils.Updater;
import org.opennms.netmgt.config.OpennmsServerConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfig;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.hibernate.OutageDaoHibernate;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.poller.pollables.DbPollEvent;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableNetwork;
import org.opennms.netmgt.poller.pollables.PollableNode;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.poller.pollables.PollableServiceConfig;
import org.opennms.netmgt.poller.pollables.PollableVisitor;
import org.opennms.netmgt.poller.pollables.PollableVisitorAdaptor;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.Schedule;
import org.opennms.netmgt.scheduler.Scheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>Poller class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class Poller extends AbstractServiceDaemon {
    
    private final static Logger LOG = LoggerFactory.getLogger(Poller.class);

    private final static String LOG4J_CATEGORY = "poller";

    private final static Poller m_singleton = new Poller();

    private boolean m_initialized = false;

    private LegacyScheduler m_scheduler = null;

    private PollerEventProcessor m_eventProcessor;

    private PollableNetwork m_network;

    private QueryManager m_queryManager;

    private PollerConfig m_pollerConfig;

    private PollOutagesConfig m_pollOutagesConfig;

    private EventIpcManager m_eventMgr;

    private DataSource m_dataSource;
    
    private OutageDaoHibernate m_outageDao;

    /**
     * <p>Constructor for Poller.</p>
     */
    public Poller() {
        super(LOG4J_CATEGORY);
    }

    /* Getters/Setters used for dependency injection */
    /**
     * <p>setDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }

    /**
     * <p>getEventManager</p>
     *
     * @return a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    /**
     * <p>setEventManager</p>
     *
     * @param eventMgr a {@link org.opennms.netmgt.model.events.EventIpcManager} object.
     */
    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }

    /**
     * <p>getEventProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.poller.PollerEventProcessor} object.
     */
    public PollerEventProcessor getEventProcessor() {
        return m_eventProcessor;
    }

    /**
     * <p>setEventProcessor</p>
     *
     * @param eventProcessor a {@link org.opennms.netmgt.poller.PollerEventProcessor} object.
     */
    public void setEventProcessor(PollerEventProcessor eventProcessor) {
        m_eventProcessor = eventProcessor;
    }

    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     */
    public PollableNetwork getNetwork() {
        return m_network;
    }

    /**
     * <p>setNetwork</p>
     *
     * @param network a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     */
    public void setNetwork(PollableNetwork network) {
        m_network = network;
    }
    
    /**
     * <p>setQueryManager</p>
     *
     * @param queryManager a {@link org.opennms.netmgt.poller.QueryManager} object.
     */
    public void setQueryManager(QueryManager queryManager) {
        m_queryManager = queryManager;
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
     * <p>getPollOutagesConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.PollOutagesConfig} object.
     */
    public PollOutagesConfig getPollOutagesConfig() {
        return m_pollOutagesConfig;
    }

    /**
     * <p>setPollOutagesConfig</p>
     *
     * @param pollOutagesConfig a {@link org.opennms.netmgt.config.PollOutagesConfig} object.
     */
    public void setPollOutagesConfig(PollOutagesConfig pollOutagesConfig) {
        m_pollOutagesConfig = pollOutagesConfig;
    }

    /**
     * <p>getScheduler</p>
     *
     * @return a {@link org.opennms.netmgt.scheduler.Scheduler} object.
     */
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * <p>setScheduler</p>
     *
     * @param scheduler a {@link org.opennms.netmgt.scheduler.LegacyScheduler} object.
     */
    public void setScheduler(LegacyScheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        
        // serviceUnresponsive behavior enabled/disabled?
        LOG.debug("init: serviceUnresponsive behavior: {}", (getPollerConfig().isServiceUnresponsiveEnabled() ? "enabled" : "disabled"));

        createScheduler();
        
        try {
            LOG.debug("init: Closing outages for unmanaged services");
            
            closeOutagesForUnmanagedServices();
        } catch (Throwable e) {
            LOG.error("init: Failed to close ouates for unmanage services", e);
        }
        

        // Schedule the interfaces currently in the database
        //
        try {
            LOG.debug("start: Scheduling existing interfaces");

            scheduleExistingServices();
        } catch (Throwable sqlE) {
            LOG.error("start: Failed to schedule existing interfaces", sqlE);
        }

        // Create an event receiver. The receiver will
        // receive events, process them, creates network
        // interfaces, and schedulers them.
        //
        try {
            LOG.debug("start: Creating event broadcast event processor");

            setEventProcessor(new PollerEventProcessor(this));
        } catch (Throwable t) {
            LOG.error("start: Failed to initialized the broadcast event receiver", t);

            throw new UndeclaredThrowableException(t);
        }

        m_initialized = true;

    }

    /**
     * 
     */
    private void closeOutagesForUnmanagedServices() {
        Timestamp closeTime = new Timestamp((new java.util.Date()).getTime());
        
        OnmsOutage outage = m_outageDao.findByServiceStatus();
        outage.setIfRegainedService(closeTime);
        
        outage = m_outageDao.findByIpInterfaceIsManaged();
        outage.setIfRegainedService(closeTime);
    }
    
    /**
     * <p>closeOutagesForNode</p>
     *
     * @param closeDate a {@link java.util.Date} object.
     * @param eventId a int.
     * @param nodeId a int.
     */
    public void closeOutagesForNode(Date closeDate, int eventId, int nodeId) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        List<OnmsOutage> outageList = m_outageDao.findbyNodeId(nodeId);
        for(OnmsOutage outage : outageList) {
            if(outage.getIfRegainedService() == null) {
                outage.setIfRegainedService(closeTime);
                outage.getServiceRegainedEvent().setId(eventId);
                m_outageDao.update(outage);
            }
        }
    }
    
    /**
     * <p>closeOutagesForInterface</p>
     *
     * @param closeDate a {@link java.util.Date} object.
     * @param eventId a int.
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     */
    public void closeOutagesForInterface(Date closeDate, int eventId, int nodeId, String ipAddr) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        List<OnmsOutage> outageList = m_outageDao.findbyNodeIdAndIpAddr(nodeId, ipAddr);
        for(OnmsOutage outage : outageList) {
            if(outage.getIfRegainedService() == null) {
                outage.setIfRegainedService(closeTime);
                outage.getServiceRegainedEvent().setId(eventId);
                m_outageDao.update(outage);
            }
        }
    }
    
    /**
     * <p>closeOutagesForService</p>
     *
     * @param closeDate a {@link java.util.Date} object.
     * @param eventId a int.
     * @param nodeId a int.
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     */
    public void closeOutagesForService(Date closeDate, int eventId, int nodeId, String ipAddr, String serviceName) {
        Timestamp closeTime = new Timestamp(closeDate.getTime());
        List<OnmsOutage> outageList = m_outageDao.findbyNodeIdIpAddrAndServiceName(nodeId, ipAddr, serviceName);
        for(OnmsOutage outage : outageList) {
            if(outage.getIfRegainedService() == null) {
                outage.setIfRegainedService(closeTime);
                outage.getServiceRegainedEvent().setId(eventId);
                m_outageDao.update(outage);
            }
        }
    }

    private void createScheduler() {

        // Create a scheduler
        //
        try {
            LOG.debug("init: Creating poller scheduler");

            setScheduler(new LegacyScheduler("Poller", getPollerConfig().getThreads()));
        } catch (RuntimeException e) {
            LOG.error("init: Failed to create poller scheduler", e);
            throw e;
        }
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
		// get the category logger
        // start the scheduler
        //
        try {
            if (LOG.isDebugEnabled())
                LOG.debug("start: Starting poller scheduler");

            getScheduler().start();
        } catch (RuntimeException e) {
            LOG.error("start: Failed to start scheduler", e);
            throw e;
        }
	}

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        if(getScheduler()!=null) {
            getScheduler().stop();
        }
        if(getEventProcessor()!=null) {
            getEventProcessor().close();
        }

        releaseServiceMonitors();
        setScheduler(null);
	}

	private void releaseServiceMonitors() {
		getPollerConfig().releaseAllServiceMonitors();
	}

	/**
	 * <p>onPause</p>
	 */
    @Override
	protected void onPause() {
		getScheduler().pause();
	}

    /**
     * <p>onResume</p>
     */
    @Override
    protected void onResume() {
		getScheduler().resume();
	}

    /**
     * <p>getInstance</p>
     *
     * @return a {@link org.opennms.netmgt.poller.Poller} object.
     */
    public static Poller getInstance() {
        return m_singleton;
    }

    /**
     * <p>getServiceMonitor</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.ServiceMonitor} object.
     */
    public ServiceMonitor getServiceMonitor(String svcName) {
        return getPollerConfig().getServiceMonitor(svcName);
    }

    private void scheduleExistingServices() throws Exception {
        scheduleMatchingServices(null);
        
        getNetwork().recalculateStatus();
        getNetwork().propagateInitialCause();
        getNetwork().resetStatusChanged();
        
        
        // Debug dump pollable network
        //
        LOG.debug("scheduleExistingServices: dumping content of pollable network: ");
        getNetwork().dump();

        

    }
    
    /**
     * <p>scheduleService</p>
     *
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     */
    public void scheduleService(final int nodeId, final String nodeLabel, final String ipAddr, final String svcName) {
        final String normalizedAddress = InetAddressUtils.normalize(ipAddr);
        try {
            /*
             * Do this here so that we can use the treeLock for this node as we
             * add its service and schedule it
             */
            PollableNode node;
            synchronized (getNetwork()) {
                node = getNetwork().getNode(nodeId);
                if (node == null) {
                    node = getNetwork().createNode(nodeId, nodeLabel);
                }
            }

            final PollableNode svcNode = node;
            final Runnable r = new Runnable() {
                @Override
                public void run() {
					final int matchCount = scheduleMatchingServices("ifServices.nodeId = "+nodeId+" AND ifServices.ipAddr = '"+normalizedAddress+"' AND service.serviceName = '"+svcName+"'");
                    if (matchCount > 0) {
                        svcNode.recalculateStatus();
                        svcNode.processStatusChange(new Date());
                    } else {
                        LOG.warn("Attempt to schedule service {}/{}/{} found no active service", nodeId, normalizedAddress, svcName);
                    }
                }
            };
            node.withTreeLock(r);

        } catch (final Throwable e) {
            LOG.error("Unable to schedule service {}/{}/{}", nodeId, normalizedAddress, svcName);
        }
    }
    
    private int scheduleMatchingServices(String criteria) {
        String sql = "SELECT ifServices.nodeId AS nodeId, node.nodeLabel AS nodeLabel, ifServices.ipAddr AS ipAddr, " +
                "ifServices.serviceId AS serviceId, service.serviceName AS serviceName, ifServices.status as status, " +
                "outages.svcLostEventId AS svcLostEventId, events.eventUei AS svcLostEventUei, " +
                "outages.ifLostService AS ifLostService, outages.ifRegainedService AS ifRegainedService " +
        "FROM ifServices " +
        "JOIN node ON ifServices.nodeId = node.nodeId " +
        "JOIN service ON ifServices.serviceId = service.serviceId " +
        "LEFT OUTER JOIN outages ON " +
        "ifServices.nodeId = outages.nodeId AND " +
        "ifServices.ipAddr = outages.ipAddr AND " +
        "ifServices.serviceId = outages.serviceId AND " +
        "ifRegainedService IS NULL " +
        "LEFT OUTER JOIN events ON outages.svcLostEventId = events.eventid " +
        "WHERE ifServices.status in ('A','N')" +
        (criteria == null ? "" : " AND "+criteria);
       
        
        final AtomicInteger count = new AtomicInteger(0);
        
        Querier querier = new Querier(m_dataSource, sql) {
            @Override
            public void processRow(ResultSet rs) throws SQLException {
                if (scheduleService(rs.getInt("nodeId"), rs.getString("nodeLabel"), rs.getString("ipAddr"), rs.getString("serviceName"), 
                                "A".equals(rs.getString("status")), (Number)rs.getObject("svcLostEventId"), rs.getTimestamp("ifLostService"), 
                                rs.getString("svcLostEventUei"))) {
                    count.incrementAndGet();
                }
            }
        };
        querier.execute();
        
        
        return count.get();

    }
    
    private void updateServiceStatus(int nodeId, String ipAddr, String serviceName, String status) {
        final String sql = "UPDATE ifservices SET status = ? WHERE id " +
        		" IN (SELECT ifs.id FROM ifservices AS ifs JOIN service AS svc ON ifs.serviceid = svc.serviceid " +
        		" WHERE ifs.nodeId = ? AND ifs.ipAddr = ? AND svc.servicename = ?)"; 

        Updater updater = new Updater(m_dataSource, sql);
        updater.execute(status, nodeId, ipAddr, serviceName);
        
    }

    private boolean scheduleService(int nodeId, String nodeLabel, String ipAddr, String serviceName, boolean active, Number svcLostEventId, Date date, String svcLostUei) {
        // We don't want to adjust the management state of the service if we're
        // on a machine that uses multiple servers with access to the same database
        // so check the value of OpennmsServerConfigFactory.getInstance().verifyServer()
        // before doing any updates.
        Package pkg = findPackageForService(ipAddr, serviceName);
        if (pkg == null) {
            if(active && !OpennmsServerConfigFactory.getInstance().verifyServer()){
                LOG.warn("Active service {} on {} not configured for any package. Marking as Not Polled.", serviceName, ipAddr);
                updateServiceStatus(nodeId, ipAddr, serviceName, "N");
            }
            return false;
        } else if (!active && !OpennmsServerConfigFactory.getInstance().verifyServer()) {
            LOG.info("Active service {} on {} is now configured for any package. Marking as active.", serviceName, ipAddr);
            updateServiceStatus(nodeId, ipAddr, serviceName, "A");
        }

        ServiceMonitor monitor = m_pollerConfig.getServiceMonitor(serviceName);
        if (monitor == null) {
            LOG.info("Could not find service monitor associated with service {}", serviceName);
            return false;
        }
        
        InetAddress addr;
        addr = InetAddressUtils.addr(ipAddr);
        if (addr == null) {
            LOG.error("Could not convert {} as an InetAddress {}", ipAddr, ipAddr);
            return false;
        }
        
        PollableService svc = getNetwork().createService(nodeId, nodeLabel, addr, serviceName);
        PollableServiceConfig pollConfig = new PollableServiceConfig(svc, m_pollerConfig, m_pollOutagesConfig, pkg, getScheduler());
        svc.setPollConfig(pollConfig);
        synchronized(svc) {
            if (svc.getSchedule() == null) {
                Schedule schedule = new Schedule(svc, pollConfig, getScheduler());
                svc.setSchedule(schedule);
            }
        }
        
        
        if (svcLostEventId == null) 
            if (svc.getParent().getStatus().isUnknown()) {
                svc.updateStatus(PollStatus.up());
            } else {
                svc.updateStatus(svc.getParent().getStatus());
            }
        else {
            svc.updateStatus(PollStatus.down());
            
            PollEvent cause = new DbPollEvent(svcLostEventId.intValue(), svcLostUei, date);

            svc.setCause(cause);

        }
        
        svc.schedule();
        
        return true;

    }

    private Package findPackageForService(String ipAddr, String serviceName) {
        Enumeration<Package> en = m_pollerConfig.enumeratePackage();
        Package lastPkg = null;
        
        while (en.hasMoreElements()) {
            Package pkg = (Package)en.nextElement();
            if (pollableServiceInPackage(ipAddr, serviceName, pkg))
                lastPkg = pkg;
        }
        return lastPkg;
    }
    
    /**
     * <p>pollableServiceInPackage</p>
     *
     * @param ipAddr a {@link java.lang.String} object.
     * @param serviceName a {@link java.lang.String} object.
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @return a boolean.
     */
    protected boolean pollableServiceInPackage(String ipAddr, String serviceName, Package pkg) {
        
        if (pkg.getRemote()) {
            LOG.debug("pollableServiceInPackage: this package: {}, is a remote monitor package.", pkg.getName());
            return false;
        }
        
        if (!m_pollerConfig.isServiceInPackageAndEnabled(serviceName, pkg)) return false;
        
        boolean inPkg = m_pollerConfig.isInterfaceInPackage(ipAddr, pkg);
        
        if (inPkg) return true;
        
        if (m_initialized) {
            m_pollerConfig.rebuildPackageIpListMap();
            return m_pollerConfig.isInterfaceInPackage(ipAddr, pkg);
        }
        
        return false;
    }
    
    /**
     * <p>packageIncludesIfAndSvc</p>
     *
     * @param pkg a {@link org.opennms.netmgt.config.poller.Package} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean packageIncludesIfAndSvc(Package pkg, String ipAddr, String svcName) {
        if (!getPollerConfig().isServiceInPackageAndEnabled(svcName, pkg)) {
            LOG.debug("packageIncludesIfAndSvc: address/service: {}/{} not scheduled, service is not enabled or does not exist in package: {}", ipAddr, svcName, pkg.getName());
            return false;
        }

        // Is the interface in the package?
        //
        if (!getPollerConfig().isInterfaceInPackage(ipAddr, pkg)) {

            if (m_initialized) {
                getPollerConfig().rebuildPackageIpListMap();
                if (!getPollerConfig().isInterfaceInPackage(ipAddr, pkg)) {
                    LOG.debug("packageIncludesIfAndSvc: interface {} gained service {}, but the interface was not in package: {}", ipAddr, svcName, pkg.getName());
                    return false;
                }
            } else {
                LOG.debug("packageIncludesIfAndSvc: address/service: {}/{} not scheduled, interface does not belong to package: {}", ipAddr, svcName, pkg.getName());
                return false;
            }
        }
        return true;
    }

    /**
     * <p>refreshServicePackages</p>
     */
    public void refreshServicePackages() {
        PollableVisitor visitor = new PollableVisitorAdaptor() {
            @Override
            public void visitService(PollableService service) {
                service.refreshConfig();
            }
        };
        getNetwork().visit(visitor);
    }

    /**
     * <p>refreshServiceThresholds</p>
     */
    public void refreshServiceThresholds() {
        PollableVisitor visitor = new PollableVisitorAdaptor() {
            @Override
            public void visitService(PollableService service) {
                service.refreshThresholds();
            }
        };
        getNetwork().visit(visitor);
    }

    public static String getLoggingCategory() {
        return LOG4J_CATEGORY;
	}

    @Autowired
    public void setOutageDao(OutageDaoHibernate outageDao) {
        m_outageDao = outageDao;
    }

}    
