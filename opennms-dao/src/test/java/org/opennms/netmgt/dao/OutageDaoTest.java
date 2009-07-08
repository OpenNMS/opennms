/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2006-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsMonitoredService;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsOutage;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.model.ServiceSelector;

/**
 * @author mhuot
 *
 */
public class OutageDaoTest extends AbstractTransactionalDaoTestCase {
    @Override
    protected void onSetUpBeforeTransaction() throws Exception {
        super.onSetUpBeforeTransaction();
    }
    
    @Override
    protected void onSetUpInTransactionIfEnabled() {
        super.onSetUpInTransactionIfEnabled();
        
        // Ensure that we get a new JdbcFilterDao every time since our DataSource changes
        FilterDaoFactory.setInstance(null);
        FilterDaoFactory.getInstance();
    }
    
    public void testSave() {
        OnmsNode node = new OnmsNode(getLocalHostDistPoller());
        getNodeDao().save(node);

        OnmsIpInterface ipInterface = new OnmsIpInterface("172.16.1.1", node);

        OnmsServiceType serviceType = getServiceTypeDao().findByName("ICMP");
        assertNotNull(serviceType);

        OnmsMonitoredService monitoredService = new OnmsMonitoredService(ipInterface, serviceType);

        OnmsEvent event = new OnmsEvent();

        OnmsOutage outage = new OnmsOutage();
        outage.setServiceLostEvent(event);
        outage.setIfLostService(new Date());
        outage.setMonitoredService(monitoredService);
        getOutageDao().save(outage);

        //it works we're so smart! hehe
        outage = getOutageDao().load(outage.getId());
        assertEquals("ICMP", outage.getMonitoredService().getServiceType().getName());
//        outage.setEventBySvcRegainedEvent();
        
    }
    
    public void testGetMatchingOutages() {
        insertEntitiesAndOutage();
        
        /*
         * We need to flush and finish the transaction because JdbcFilterDao
         * gets its own connection from the DataSource and won't see our data
         * otherwise.
         */
        flushOutageDaoAndStartNewTransaction();
        
        String[] svcs = new String[] { "ICMP" };
        ServiceSelector selector = new ServiceSelector("ipAddr IPLIKE 172.16.1.1", Arrays.asList(svcs));
    	Collection<OnmsOutage> outages = getOutageDao().matchingCurrentOutages(selector);
    	assertEquals("outage count", 1, outages.size());
    }
    
    public void testGetMatchingOutagesWithEmptyServiceList() {
        insertEntitiesAndOutage();
        
        /*
         * We need to flush and finish the transaction because JdbcFilterDao
         * gets its own connection from the DataSource and won't see our data
         * otherwise.
         */
        flushOutageDaoAndStartNewTransaction();

        ServiceSelector selector = new ServiceSelector("ipAddr IPLIKE 172.16.1.1", new ArrayList<String>(0));
    	Collection<OnmsOutage> outages = getOutageDao().matchingCurrentOutages(selector);
    	assertEquals(1, outages.size());
    }

    private OnmsDistPoller getLocalHostDistPoller() {
        return getDistPollerDao().load("localhost");
    }
    
    private OnmsOutage insertEntitiesAndOutage() {
        OnmsNode node = new OnmsNode(getLocalHostDistPoller());
        getNodeDao().save(node);

        OnmsIpInterface ipInterface = new OnmsIpInterface("172.16.1.1", node);
        getIpInterfaceDao().save(ipInterface);

        OnmsServiceType serviceType = getServiceTypeDao().findByName("ICMP");
        assertNotNull(serviceType);
        
        OnmsMonitoredService monitoredService = new OnmsMonitoredService(ipInterface, serviceType);
        getMonitoredServiceDao().save(monitoredService);
        
        OnmsEvent event = new OnmsEvent();
        event.setDistPoller(getLocalHostDistPoller());
        event.setEventUei("foo!");
        event.setEventTime(new Date());
        event.setEventCreateTime(new Date());
        event.setEventSeverity(1);
        event.setEventSource("your mom");
        event.setEventLog("Y");
        event.setEventDisplay("Y");
        getEventDao().save(event);

        OnmsOutage outage = new OnmsOutage();
        outage.setMonitoredService(monitoredService);
        outage.setServiceLostEvent(event);
        outage.setIfLostService(new Date());
        getOutageDao().save(outage);
        
        return outage;
    }

    private void flushOutageDaoAndStartNewTransaction() {
        getOutageDao().flush();
        setComplete();
        endTransaction();
        startNewTransaction();
    }
}
