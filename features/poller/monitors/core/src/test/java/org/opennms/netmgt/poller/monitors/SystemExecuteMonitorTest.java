/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;

public class SystemExecuteMonitorTest {

    private SystemExecuteMonitor monitor;

    private final String NODELABEL = "www.OpenNMS.org";
    private MonitoredService svc;
    private Map<String, Object> parameters;


    @Before
    public void setup() throws Exception {
        monitor = new SystemExecuteMonitor();
        svc = new MockMonitoredService(42, NODELABEL, InetAddress.getLocalHost(), "myService");
        parameters = new HashMap<String, Object>();
    }

    @Test
    public void testPollScriptParameterNotSet() {
        monitor = new SystemExecuteMonitor();
        PollStatus pollStatus = monitor.poll(svc, parameters);
        Assert.assertEquals("Unknown", pollStatus.getStatusName());
    }

    @Test
    public void testPollScriptParameterEmpty() {
        monitor = new SystemExecuteMonitor();
        parameters.put("script", "");
        PollStatus pollStatus = monitor.poll(svc, parameters);
        Assert.assertEquals("Unknown", pollStatus.getStatusName());
    }
}
