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

import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.InvdConfigDao;
import org.springframework.util.Assert;

public class Invd extends AbstractServiceDaemon {
    /**
     * Log4j category
     */
    private final static String LOG4J_CATEGORY = "OpenNMS.Invd";

    private volatile InvdConfigDao m_inventoryConfigDao;

    private volatile ScannerCollection m_scannerCollection;

    private volatile ScanableServices m_scanableServices;

    private volatile IpInterfaceDao m_ifaceDao;

    private volatile InventoryScheduler m_inventoryScheduler;
    
    /**
     * Constructor.
     */
    public Invd() {
        super(LOG4J_CATEGORY);
    }

    protected void onInit() {
        Assert.notNull(m_inventoryConfigDao, "invdConfigDao must not be null");
        Assert.notNull(m_ifaceDao, "ifaceDao must not be null");
        Assert.notNull(m_scannerCollection, "scannerCollection must not be null");
        Assert.notNull(m_inventoryScheduler, "inventoryScheduler must not be null");

        log().debug("init: Initializing inventory daemon.");
        
        // TODO implement instrumentation.
        // make sure the instrumentation gets initialized
        //instrumentation();
        
        getScannerCollection().instantiateScanners();

        getInventoryScheduler().schedule();
    }

    @Override
    protected void onStart() {
        // start the scheduler
        try {
            log().debug("start: Starting Invd scheduler");

            getInventoryScheduler().start();
        } catch (RuntimeException e) {
            log().fatal("start: Failed to start Invd scheduler", e);
            throw e;
        }
    }

    @Override
    protected void onStop() {
        getInventoryScheduler().stop();
    }

    @Override
    protected void onPause() {
        getInventoryScheduler().pause();
    }

    @Override
    protected void onResume() {
        getInventoryScheduler().resume();
    }

    public void setInvdConfigDao(InvdConfigDao inventoryConfigDao) {
        m_inventoryConfigDao = inventoryConfigDao;
    }

    @SuppressWarnings("unused")
	private InvdConfigDao getInvdConfigDao() {
        return m_inventoryConfigDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ifSvcDao) {
        m_ifaceDao = ifSvcDao;
    }

    @SuppressWarnings("unused")
	private IpInterfaceDao getIpInterfaceDao() {
        return m_ifaceDao;
    }

    public void setScannerCollection(ScannerCollection scannerCollection) {
        m_scannerCollection = scannerCollection;
    }

    private ScannerCollection getScannerCollection() {
        return m_scannerCollection;
    }

    public void setInventoryScheduler(InventoryScheduler inventoryScheduler) {
        m_inventoryScheduler = inventoryScheduler;
    }

    private InventoryScheduler getInventoryScheduler() {
        return m_inventoryScheduler;
    }

    public void setScanableServices(ScanableServices scanableService) {
        m_scanableServices = scanableService;
    }

    @SuppressWarnings("unused")
	private ScanableServices getScanableServices() {
        return m_scanableServices;
    }
}
