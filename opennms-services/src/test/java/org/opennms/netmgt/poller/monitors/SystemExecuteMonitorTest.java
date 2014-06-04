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

package org.opennms.netmgt.poller.monitors;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.mock.MockMonitoredService;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

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

    //TODO add test files to test/resources
    @Test
    @Ignore
    public void testPollScriptParameterNotExecutable() {
        monitor = new SystemExecuteMonitor();
        parameters.put("script", "/tmp/log.log");
        PollStatus pollStatus = monitor.poll(svc, parameters);
        Assert.assertEquals("Unknown", pollStatus.getStatusName());
    }

    //TODO system dependent
    @Test
    @Ignore
    public void testPollScriptParameterNotExecutableFoo() {
        monitor = new SystemExecuteMonitor();
        parameters.put("script", "/tmp/loadspeed.sh");
        parameters.put("timeout", "30000");
        parameters.put("args", "http://${nodelabel} ${timeout}");
        PollStatus pollStatus = monitor.poll(svc, parameters);
        Assert.assertEquals("Up", pollStatus.getStatusName());
    }
}
