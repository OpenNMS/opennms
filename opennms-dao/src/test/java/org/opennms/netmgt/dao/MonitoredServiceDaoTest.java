/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2006, 2008 The OpenNMS Group, Inc.  All rights reserved.
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

import java.util.List;

import org.opennms.netmgt.model.OnmsMonitoredService;


public class MonitoredServiceDaoTest extends AbstractTransactionalDaoTestCase {

    public void testLazy() {
    	
    	List<OnmsMonitoredService> allSvcs = getMonitoredServiceDao().findAll();
    	assertTrue(allSvcs.size() > 1);
    	
    	OnmsMonitoredService svc = allSvcs.iterator().next();
    	assertEquals("192.168.1.1", svc.getIpAddress());
    	assertEquals(1, svc.getIfIndex().intValue());
    	assertEquals(1, svc.getIpInterface().getNode().getId().intValue());
    	assertEquals("M", svc.getIpInterface().getIsManaged());
    	//assertEquals("SNMP", svc.getServiceType().getName());
    	
    }
    
    public void testGetByCompositeId() {
    	OnmsMonitoredService monSvc = getMonitoredServiceDao().get(1, "192.168.1.1", "SNMP");
    	assertNotNull(monSvc);
    	
    	OnmsMonitoredService monSvc2 = getMonitoredServiceDao().get(1, "192.168.1.1", monSvc.getIfIndex(), monSvc.getServiceId());
    	assertNotNull(monSvc2);
    	
    }

}
