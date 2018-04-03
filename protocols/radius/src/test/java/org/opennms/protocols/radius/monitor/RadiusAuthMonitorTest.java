/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.protocols.radius.monitor;

import static org.junit.Assert.assertEquals;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.Level;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.context.ContextConfiguration;
import org.tinyradius.util.RadiusServer;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitConfigurationEnvironment
public class RadiusAuthMonitorTest {
	RadiusServer mockSrv = null;
	@Before
	public void setup() throws Exception {
	    MockLogAppender.setupLogging();
	    //Create the radius server, do not start it
		mockSrv = new MockRadiusServer();
		
	}
	@After
	public void tearDown() {
		if (mockSrv != null) mockSrv.stop();
	}
	
	@Test
	public void testResponses() throws Exception {
		mockSrv.start(true,false);
		final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

		final ServiceMonitor monitor = new RadiusAuthMonitor();
		final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddressUtils.addr("127.0.0.1"), "RADIUS");

        m.put("user", "testing");
        m.put("password", "password");
        m.put("retry", "1");
        m.put("secret", "testing123");
        m.put("authtype", "chap");
        final PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
	}
	@Test
	public void testBadResponses() throws Exception {
		mockSrv.start(true,false);
		
		final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

		final ServiceMonitor monitor = new RadiusAuthMonitor();
		final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddressUtils.addr("127.0.0.1"), "RADIUS");

        m.put("user", "testing");
        m.put("password", "12");
        m.put("retry", "1");
        m.put("secret", "testing123");
        m.put("authtype", "chap");
        final PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
		MockLogAppender.assertLogMatched(Level.DEBUG, "response returned, but request was not accepted");
	}
	@Test
	public void testTimeOut() throws Exception {
		// do not start raddius server
		final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

		final ServiceMonitor monitor = new RadiusAuthMonitor();
		final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddressUtils.addr("127.0.0.1"), "RADIUS");

        m.put("user", "testing");
        m.put("password", "12");
        m.put("retry", "1");
        m.put("secret", "testing123");
        m.put("authtype", "chap");
        final PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
		MockLogAppender.assertLogMatched(Level.DEBUG, "Error while attempting to connect to the RADIUS service on localhost");
	}

	@Test
	@Ignore("have to have a EAP-TTLS radius server set up")
	public void testTTLSResponse() throws Exception {
	    mockSrv.start(true,false);

	    final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

	    final ServiceMonitor monitor = new RadiusAuthMonitor();
	    final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddressUtils.addr("127.0.0.1"), "RADIUS");

	    m.put("user", "testing");
	    m.put("password", "12");
	    m.put("retry", "1");
	    m.put("secret", "testing123");
	    m.put("authtype", "eap-ttls");
	    m.put("inner-user", "anonymous");
	    final PollStatus status = monitor.poll(svc, m);
	    MockUtil.println("Reason: "+status.getReason());
	    System.out.println("Reason"+status.getReason());
	    assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
	}

}
