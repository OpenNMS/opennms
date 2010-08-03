//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Jun 17: Change tests to use google.com
// 2008 Feb 09: Eliminate warnings. - dj@opennms.org
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.JUnitHttpServerExecutionListener;
import org.opennms.core.test.annotations.JUnitHttpServer;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.test.mock.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;


@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TransactionalTestExecutionListener.class,
    JUnitHttpServerExecutionListener.class
})
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
public class TcpMonitorTest {

    private boolean m_runTests = true;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    /*
     * Test method for 'org.opennms.netmgt.poller.monitors.TcpMonitor.poll(NetworkInterface, Map, Package)'
     */
    @Test
    public void testExternalServerConnection() throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();

        ServiceMonitor monitor = new TcpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, "www.opennms.org", "TCP");

        p.setKey("port");
        p.setValue("3020");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());

        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(status.getReason());

    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testLocalhostConnection() throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();

        ServiceMonitor monitor = new TcpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "TCP");

        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());

        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testLocalhostIPv6Connection() throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();

        ServiceMonitor monitor = new TcpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "::1", "TCP");

        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());

        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());
    }
}
