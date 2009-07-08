/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2005-2006, 2008-2009 The OpenNMS Group, Inc.  All rights reserved.
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

package org.opennms.netmgt.poller.monitors;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.MockMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;

public class LoopMonitorTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /*
     * Test method for 'org.opennms.netmgt.poller.monitors.LoopMonitor.poll(MonitoredService, Map, Package)'
     */
    public void testPoll() throws UnknownHostException {
        
        ServiceMonitor sm = new LoopMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", "127.0.0.1", "LOOP");
        Map<String, Object> parms = new HashMap<String, Object>();

        parms.put("ip-match", "127.0.0.1-2");
        parms.put("is-supported", "true");
        
        PollStatus ps = sm.poll(svc, parms);
        assertTrue(ps.isUp());
        assertFalse(ps.isDown());

    }

}
