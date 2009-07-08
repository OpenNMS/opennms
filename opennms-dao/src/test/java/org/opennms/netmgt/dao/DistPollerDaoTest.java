/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
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

import java.util.Date;

import org.opennms.netmgt.model.OnmsDistPoller;

public class DistPollerDaoTest extends AbstractTransactionalDaoTestCase {
	public void testCreate() {
        OnmsDistPoller distPoller = new OnmsDistPoller("otherpoller", "192.168.7.7");   
        distPoller.setLastEventPull(new Date(1000000));
        getDistPollerDao().save(distPoller);
        
    }
    
    public void testGet() {
        assertNull(getDistPollerDao().get("otherpoller"));
        
        testCreate();
        
        OnmsDistPoller distPoller = getDistPollerDao().get("otherpoller");
        assertNotNull(distPoller);
        assertEquals(new Date(1000000), distPoller.getLastEventPull());
        
    }


}
