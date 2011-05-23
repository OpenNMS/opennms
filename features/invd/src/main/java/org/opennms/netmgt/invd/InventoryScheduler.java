//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.invd;

import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.config.invd.InvdPackage;
import org.opennms.netmgt.dao.InvdConfigDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionStatus;
import org.springframework.util.Assert;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.net.InetAddress;

public class InventoryScheduler {
    /**
     * Log4j category
     */
    private final static String LOG4J_CATEGORY = "OpenNMS.Invd";
    
    private volatile InvdConfigDao m_inventoryConfigDao;

    private volatile TransactionTemplate m_transTemplate;

    private volatile IpInterfaceDao m_ifaceDao;

    private volatile NodeDao m_nodeDao;

    private volatile ScannerCollection m_scannerCollection;

    private volatile ScanableServices m_scanableServices;
    
    /**
     * Reference to the collection scheduler
     */
    private volatile Scheduler m_scheduler;

    static class SchedulingCompletedFlag {
        volatile boolean m_schedulingCompleted = false;

        public synchronized void setSchedulingCompleted(
                boolean schedulingCompleted) {
            m_schedulingCompleted = schedulingCompleted;
        }

        public synchronized boolean isSchedulingCompleted() {
            return m_schedulingCompleted;
        }

    }

    private final SchedulingCompletedFlag m_schedulingCompletedFlag = new SchedulingCompletedFlag();

    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    public void start() {
        getScheduler().start();
    }

    public void stop() {
        getScheduler().stop();
        setScheduler(null);
    }

    public void pause() {
        getScheduler().pause();
    }

    public void resume() {
        getScheduler().resume();
    }

    public void schedule() {
        Assert.notNull(m_transTemplate, "transTemplate must not be null");

        getScheduler().schedule(0, ifScheduler());
    }


    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    public Scheduler getScheduler() {
        if (m_scheduler == null) {
            createScheduler();
        }
        return m_scheduler;
    }

    private void createScheduler() {

        // Create a scheduler
        try {
            log().debug("init: Creating invd scheduler");

            setScheduler(new LegacyScheduler(
                                             "Invd",
                                             getInvdConfigDao().getSchedulerThreads()));
        } catch (RuntimeException e) {
            log().fatal("init: Failed to create invd scheduler", e);
            throw e;
        }
    }

    private ReadyRunnable ifScheduler() {
        // Schedule existing interfaces for data collection
        return new ReadyRunnable() {

            public boolean isReady() {
                return true;
            }

            public void run() {
                try {
                    ThreadCategory.setPrefix(LOG4J_CATEGORY);
                    scheduleExistingInterfaces();
                } catch (SQLException e) {
                    log().error("start: Failed to schedule existing interfaces", e);
                } finally {
                    setSchedulingCompleted(true);
                }

            }
        };
    }

    /**
     * @param schedulingCompleted
     *            The schedulingCompleted to set.
     */
    private void setSchedulingCompleted(boolean schedulingCompleted) {
        m_schedulingCompletedFlag.setSchedulingCompleted(schedulingCompleted);
    }

    /**
     * Schedule existing interfaces for data collection.
     *
     * @throws SQLException
     *             if database errors encountered.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void scheduleExistingInterfaces() throws SQLException {
        log().debug("scheduleExistingInterfaces: begin");
        try {

            m_transTemplate.execute(new TransactionCallback() {

                public Object doInTransaction(TransactionStatus status) {

                    // Loop through collectors and schedule for each one present
                    for(String name : getScannerCollection().getScannerNames()) {
                        scheduleInterfacesWithService(name);
                    }
                    return null;
                }

            });

        } finally {
            log().debug("scheduleExistingInterfaces: end");
        }
    }

    private void scheduleInterfacesWithService(String svcName) {
        log().debug("scheduleInterfacesWithService: begin: "+svcName);
        try {
            log().info("scheduleInterfacesWithService: svcName = " + svcName);

            Collection<OnmsIpInterface> ifsWithServices = findInterfacesWithService(svcName);
            for (OnmsIpInterface iface : ifsWithServices) {
                scheduleInterface(iface, svcName, true);
            }
        } finally {
            log().debug("scheduleInterfacesWithService: end: " + svcName);
        }
    }

    private Collection<OnmsIpInterface> findInterfacesWithService(String svcName) {
        log().debug("scheduleFindInterfacesWithService: begin: "+svcName);
        int count = -1;
        try {
           Collection<OnmsIpInterface> ifaces = getIpInterfaceDao().findByServiceType(svcName);
           count = ifaces.size();
           return ifaces;
        } finally {
            log().debug("scheduleFindInterfacesWithService: end: "+svcName+". found "+count+" interfaces.");
        }

    }

    /**
     * This method is responsible for scheduling the specified
     * node/address/svcname tuple for data collection.
     *
     * @param nodeId
     *            Node id
     * @param ipAddress
     *            IP address
     * @param svcName
     *            Service name
     * @param existing
     *            True if called by scheduleExistingInterfaces(), false
     *            otheriwse
     */
    public void scheduleInterface(int nodeId, String ipAddress,
            String svcName, boolean existing) {

        OnmsIpInterface iface = getIpInterface(nodeId, ipAddress);
        if (iface == null) {
            log().error("Unable to find interface with address "+ipAddress+" on node "+nodeId);
            return;
        }

        OnmsMonitoredService svc = iface.getMonitoredServiceByServiceType(svcName);
        if (svc == null) {
            log().error("Unable to find service "+svcName+" on interface with address "+ipAddress+" on node "+nodeId);
            return;
        }

        scheduleInterface(iface, svc.getServiceType().getName(), existing);
    }

    private OnmsIpInterface getIpInterface(int nodeId, String ipAddress) {
		OnmsNode node = m_nodeDao.load(nodeId);
		return node.getIpInterfaceByIpAddress(ipAddress);
	}

    private void scheduleInterface(OnmsIpInterface iface, String svcName, boolean existing) {
        //instrumentation().beginScheduleInterface(iface.getNode().getId(), iface.getIpAddress(), svcName);
        log().debug("scheduleInterfaceWithService: begin: "+iface.getNode().getId()+"/"+iface.getIpAddress()+"/"+svcName);
        try {

        Collection<ScannerSpecification> matchingSpecs = getSpecificationsForInterface(iface, svcName);
        StringBuffer sb;

        if (log().isDebugEnabled()) {
            sb = new StringBuffer();
            sb.append("scheduleInterface: found ");
            sb.append(Integer.toString(matchingSpecs.size()));
            sb.append(" matching specs for interface: ");
            sb.append(iface);
            log().debug(sb.toString());
        }

            sb = new StringBuffer();
            sb.append("scheduleInterface: found ");
            sb.append(Integer.toString(matchingSpecs.size()));
            sb.append(" matching specs for interface: ");
            sb.append(iface);
            log().debug(sb.toString());


        for (ScannerSpecification spec : matchingSpecs) {

            if (!existing) {
                /*
                 * It is possible that both a nodeGainedService and a
                 * primarySnmpInterfaceChanged event are generated for an
                 * interface during a rescan. To handle this scenario we must
                 * verify that the ipAddress/pkg pair identified by this event
                 * does not already exist in the collectable services list.
                 */
                if (alreadyScheduled(iface, spec)) {
                    if (log().isDebugEnabled()) {
                        sb = new StringBuffer();
                        sb.append("scheduleInterface: svc/pkgName ");
                        sb.append(iface);
                        sb.append('/');
                        sb.append(spec);
                        sb.append(" already in collectable service list, skipping.");
                        log().debug(sb.toString());
                    }
                    continue;
                }
            }

            try {
                /*
                 * Criteria checks have all passed. The interface/service pair
                 * can be scheduled.
                 */
                if (log().isDebugEnabled()) {
                    sb = new StringBuffer();
                    sb.append("scheduleInterface: now scheduling interface: ");
                    sb.append(iface);
                    sb.append('/');
                    sb.append(svcName);
                    log().debug(sb.toString());
                }
                ScanableService cSvc;

                /*
                 * Create a new SnmpCollector object representing this node,
                 * interface, service and package pairing
                 */

                cSvc = new ScanableService(iface, m_ifaceDao, spec, this, m_schedulingCompletedFlag, m_transTemplate.getTransactionManager());

                // Add new collectable service to the colleable service list.
                m_scanableServices.add(cSvc);

                // Schedule the collectable service for immediate collection
                getScheduler().schedule(0, cSvc.getReadyRunnable());

                if (log().isDebugEnabled()) {
                    sb = new StringBuffer();
                    sb.append("scheduleInterface: ");
                    sb.append(iface);
                    sb.append('/');
                    sb.append(svcName);
                    sb.append(" collection, scheduled");
                    log().debug(sb.toString());
                }
            } catch (RuntimeException rE) {
                sb = new StringBuffer();
                sb.append("scheduleInterface: Unable to schedule ");
                sb.append(iface);
                sb.append('/');
                sb.append(svcName);
                sb.append(", reason: ");
                sb.append(rE.getMessage());
                if (log().isDebugEnabled()) {
                    log().debug(sb.toString(), rE);
                } else if (log().isInfoEnabled()) {
                    log().info(sb.toString());
                }
            } catch (Throwable t) {
                sb = new StringBuffer();
                sb.append("scheduleInterface: Uncaught exception, failed to schedule interface ");
                sb.append(iface);
                sb.append('/');
                sb.append(svcName);
                sb.append(". ");
                sb.append(t);
                log().error(sb.toString(), t);
            }
        } // end while more specifications  exist

        } finally {
            log().debug("scheduleInterfaceWithService: end: "+iface.getNode().getId()+"/"+iface.getIpAddress()+"/"+svcName);
            //instrumentation().endScheduleInterface(iface.getNode().getId(), iface.getIpAddress(), svcName);
        }
    }

    public Collection<ScannerSpecification> getSpecificationsForInterface(OnmsIpInterface iface, String svcName) {
        Collection<ScannerSpecification> matchingPkgs = new LinkedList<ScannerSpecification>();


        /*
         * Compare interface/service pair against each collectd package
         * For each match, create new SnmpCollector object and
         * schedule it for collection
         */
        for(InvdPackage wpkg : getInvdConfigDao().getPackages()) {            
            /*
             * Make certain the the current service is in the package
             * and enabled!
             */
            if (!wpkg.serviceInPackageAndEnabled(svcName)) {
                if (log().isDebugEnabled()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("getSpecificationsForInterface: address/service: ");
                    sb.append(iface);
                    sb.append("/");
                    sb.append(svcName);
                    sb.append(" not scheduled, service is not enabled or does not exist in package: ");
                    sb.append(wpkg.getName());
                    log().debug(sb.toString());
                }
                continue;
            }

            // Is the interface in the package?
            if (!wpkg.interfaceInPackage(iface.getIpAddress())) {
                if (log().isDebugEnabled()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("getSpecificationsForInterface: address/service: ");
                    sb.append(iface);
                    sb.append("/");
                    sb.append(svcName);
                    sb.append(" not scheduled, interface does not belong to package: ");
                    sb.append(wpkg.getName());
                    log().debug(sb.toString());
                }
                continue;
            }

            if (log().isDebugEnabled()) {
                StringBuffer sb = new StringBuffer();
                sb.append("getSpecificationsForInterface: address/service: ");
                sb.append(iface);
                sb.append("/");
                sb.append(svcName);
                sb.append(" scheduled, interface does belong to package: ");
                sb.append(wpkg.getName());
                log().debug(sb.toString());
            }

            matchingPkgs.add(new ScannerSpecification(wpkg, svcName, getScannerCollection().getInventoryScanner(svcName)));
        }
        return matchingPkgs;
    }

    /**
     * Returns true if specified address/pkg pair is already represented in
     * the collectable services list. False otherwise.
     *
     * @param iface
     *            TODO
     * @param spec
     *            TODO
     * @return boolean true if address/pkg pair is represented in scanable services list.
     */
    private boolean alreadyScheduled(OnmsIpInterface iface,
            ScannerSpecification spec) {
        String ipAddress = iface.getIpAddress();
        String svcName = spec.getServiceName();
        String pkgName = spec.getPackageName();
        StringBuffer sb;
        boolean isScheduled = false;

        if (log().isDebugEnabled()) {
            sb = new StringBuffer();
            sb.append("alreadyScheduled: determining if interface: ");
            sb.append(iface);
            sb.append(" is already scheduled.");
        }

        synchronized (getScanableServices().getScanableServices()) {
        	for (ScanableService cSvc : getScanableServices().getScanableServices()) {
                InetAddress addr = (InetAddress) cSvc.getAddress();
                if (addr.getHostAddress().equals(ipAddress)
                        && cSvc.getPackageName().equals(pkgName)
                        && cSvc.getServiceName().equals(svcName)) {
                    isScheduled = true;
                    break;
                }
            }
        }

        if (log().isDebugEnabled()) {
            sb = new StringBuffer();
            sb.append("alreadyScheduled: interface ");
            sb.append(iface);
            sb.append("already scheduled check: ");
            sb.append(isScheduled);
        }
        return isScheduled;
    }

    public void setInvdConfigDao(InvdConfigDao inventoryConfigDao) {
        m_inventoryConfigDao = inventoryConfigDao;
    }

    private InvdConfigDao getInvdConfigDao() {
        return m_inventoryConfigDao;
    }

    public void setTransactionTemplate(TransactionTemplate transTemplate) {
        m_transTemplate = transTemplate;
    }

    public void setIpInterfaceDao(IpInterfaceDao ifSvcDao) {
        m_ifaceDao = ifSvcDao;
    }

    private IpInterfaceDao getIpInterfaceDao() {
        return m_ifaceDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setScannerCollection(ScannerCollection scannerCollection) {
        m_scannerCollection = scannerCollection;
    }

    private ScannerCollection getScannerCollection() {
        return m_scannerCollection;
    }

    public void setScanableServices(ScanableServices scanableService) {
        m_scanableServices = scanableService;
    }

    private ScanableServices getScanableServices() {
        return m_scanableServices;
    }
}
