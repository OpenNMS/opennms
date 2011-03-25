//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006-2010 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2010 Feb 23: Add test for ${matchingGroup[n]} syntax. - jeffg@opennms.org
// 2007 Apr 06: Separate out testSimple into individual tests and
//              pass the virtual host in the config to keep cookie
//              warnings from happening.  Verify that nothing is
//              logged at a level at WARN or higher. - dj@opennms.org
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

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.JUnitHttpServerExecutionListener;
import org.opennms.core.test.annotations.JUnitHttpServer;
import org.opennms.core.test.annotations.Webapp;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.mock.MockMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */

@RunWith(SpringJUnit4ClassRunner.class)
/*
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    JUnitHttpServerExecutionListener.class
})
*/
@ContextConfiguration(locations="classpath:META-INF/opennms/emptyContext.xml")
//@JUnitHttpServer(port=10342)
public class PageSequenceMonitorOpenNMSTest /* implements SystemReportPlugin */ {

    PageSequenceMonitor m_monitor;
    Map<String, Object> m_params;


    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_monitor = new PageSequenceMonitor();
        m_monitor.initialize(Collections.<String, Object>emptyMap());

        m_params = new HashMap<String, Object>();
        m_params.put("timeout", "8000");
        m_params.put("retries", "1");

    }

    protected MonitoredService getHttpService(String hostname) throws Exception {
        return getHttpService(hostname, InetAddress.getByName(hostname));
    }

    protected MonitoredService getHttpService(String hostname, InetAddress inetAddress) throws Exception {
        MonitoredService svc = new MockMonitoredService(1, hostname, inetAddress, "HTTP");
        m_monitor.initialize(svc);
        return svc;
    }


    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    // @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testOpenNMSUserInterface() throws Exception {

        StringBuffer config = new StringBuffer();
        LineNumberReader in = new LineNumberReader(
            new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("opennmsPageSequence.xml"),
                "UTF-8"
            )
        );
        String line;
        while ((line = in.readLine()) != null) {
            config.append(line);
        }
        m_params.put("page-sequence", config.toString());
        m_params.put("virtualHost", "localhost");
        m_params.put("port", "8980");
        m_params.put("adminUsername", "admin");
        m_params.put("adminPassword", "admin");

        try {
            PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
            assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        } finally {
            // Print some debug output if necessary
        }
    }
}
