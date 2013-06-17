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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitConfigurationEnvironment
public class HttpMonitorTest {

    private boolean m_runTests = true;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    /*
     * Test method for 'org.opennms.netmgt.poller.monitors.HttpMonitor.poll(NetworkInterface, Map, Package)'
     */
    @Test
    public void testPollStatusReason() throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        Parameter p = new Parameter();

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, "www.opennms.org", "HTTP");


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

        /*
        // TODO: Enable this portion of the test as soon as there is a IPv6 www.opennms.org
        // Try with IPv6
        svc = MonitorTestUtils.getMonitoredService(99, "www.opennms.org", "HTTP", true);
        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(status.getReason());
         */
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testResponseRange() throws UnknownHostException {
        callTestResponseRange(false);
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testResponseRangeIPv6() throws UnknownHostException {
        callTestResponseRange(true);
    }

    public void callTestResponseRange(boolean preferIPv6) throws UnknownHostException {
        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "HTTP", preferIPv6);

        m.put("port", "10342");
        m.put("retry", "1");
        m.put("timeout", "500");
        m.put("response", "100-199");

        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(status.getReason());

        m.put("response", "100,200,302,400-500");

        monitor = new HttpMonitor();
        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());

        m.put("response", "*");

        monitor = new HttpMonitor();
        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());
    }

    /**
     * This throws a java.net.NoRouteToHostException because the {@link InetAddressUtils#UNPINGABLE_ADDRESS} 
     * address is in an unroutable test range. :-/  Dear reader, if you can find an address that works with
     * this test, then please replace {@link InetAddressUtils#UNPINGABLE_ADDRESS} inside {@link #callTestTimeout(boolean)}.
     */
    @Test
    @Ignore
    public void testTimeout() throws UnknownHostException {
        callTestTimeout(false);
    }

    /**
     * <p>
     * This test works fine because the "Unique Unicast" range for IPv6 is so big,
     * you can use it for testing, local communications, etc. so it is always routable.
     * Yay!
     * </p>
     * 
     * <p>
     * This test was created to test the issue documented in NMS-5028.
     * </p> 
     * 
     * {@see http://issues.opennms.org/browse/NMS-5028}
     */
    @Test
    public void testTimeoutIPv6() throws UnknownHostException {
        callTestTimeout(true);
    }

    public void callTestTimeout(boolean preferIPv6) throws UnknownHostException {
        if (m_runTests == false) return;

        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        final ServiceMonitor monitor = new HttpMonitor();
        // We need a routable but unreachable address in order to simulate a timeout
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(3, preferIPv6 ? InetAddressUtils.UNPINGABLE_ADDRESS_IPV6 : InetAddressUtils.UNPINGABLE_ADDRESS, "HTTP");

        m.put("port", "10342");
        m.put("retry", "1");
        m.put("timeout", "500");
        m.put("response", "100-199");

        final PollStatus status = monitor.poll(svc, m);
        final String reason = status.getReason();
        MockUtil.println("Reason: "+reason);
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(reason);
        assertTrue(reason + "should be 'HTTP connection timeout' or 'No route to host'", reason.contains("HTTP connection timeout") || reason.contains("No route to host"));
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testMatchingTextInResponse() throws UnknownHostException {
        callTestMatchingTextInResponse(false);
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testMatchingTextInResponseIPv6() throws UnknownHostException {
        callTestMatchingTextInResponse(true);
    }

    public void callTestMatchingTextInResponse(boolean preferIPv6) throws UnknownHostException {

        if (m_runTests == false) return;

        PollStatus status = null;
        ServiceMonitor monitor = new HttpMonitor();

        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "HTTP", preferIPv6);

        m.put("port", "10342");
        m.put("retry", "0");
        m.put("timeout", "500");
        m.put("response", "100-499");
        m.put("verbose", "true");
        m.put("host-name", "localhost");
        m.put("url", "/");
        m.put("response-text", "opennmsrulz");

        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(status.getReason());

        m.put("response-text", "written by monkeys");

        MockUtil.println("\nliteral text check: \"written by monkeys\"");
        monitor = new HttpMonitor();
        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());

        m.put("response-text", "~.*[Tt]est HTTP [Ss]erver.*");

        MockUtil.println("\nregex check: \".*[Tt]est HTTP [Ss]erver.*\"");
        monitor = new HttpMonitor();
        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());

    }

    @Test
    public void testBase64Encoding() {
        if (m_runTests == false) return;

        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        m.put("basic-authentication", "Aladdin:open sesame");
        assertEquals("QWxhZGRpbjpvcGVuIHNlc2FtZQ==", HttpMonitor.determineBasicAuthentication(m));
        assertFalse( "QWxhZGRpbjpvcZVuIHNlc2FtZQ==".equals(HttpMonitor.determineBasicAuthentication(m)));
    }

    @Test
    @JUnitHttpServer(port=10342, basicAuth=true)
    public void testBasicAuthentication() throws UnknownHostException {
        callTestBasicAuthentication(false);
    }

    @Test
    @JUnitHttpServer(port=10342, basicAuth=true)
    public void testBasicAuthenticationIPv6() throws UnknownHostException {
        callTestBasicAuthentication(true);
    }

    public void callTestBasicAuthentication(boolean preferIPv6) throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        PollStatus status = null;

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(1, "localhost", "HTTP", preferIPv6);

        m.put("port", "10342");
        m.put("retry", "0");
        m.put("timeout", "500");
        m.put("response", "100-302");
        m.put("verbose", "true");
        m.put("host-name", "localhost");
        m.put("url", "/");
        m.put("basic-authentication", "admin:istrator");

        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());

        m.put("basic-authentication", "admin:flagrator");

        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(status.getReason());


    }

    @Test
    @JUnitHttpServer(port=10342, https=true, basicAuth=true)
    public void testBasicAuthenticationWithHttps() throws UnknownHostException {
        callTestBasicAuthenticationWithHttps(false);
    }

    @Test
    @JUnitHttpServer(port=10342, https=true, basicAuth=true)
    public void testBasicAuthenticationWithHttpsIPv6() throws UnknownHostException {
        callTestBasicAuthenticationWithHttps(true);
    }

    public void callTestBasicAuthenticationWithHttps(boolean preferIPv6) throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        PollStatus status = null;

        ServiceMonitor monitor = new HttpsMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(1, "localhost", "HTTPS", preferIPv6);

        m.put("port", "10342");
        m.put("retry", "1");
        m.put("timeout", "500");
        m.put("response", "100-302");
        m.put("verbose", "true");
        m.put("host-name", "localhost");
        m.put("url", "/index.html");

        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertEquals("HTTP response value: 401. Expecting: 100-302./Ports: 10342", status.getReason());
        
        m.put("basic-authentication", "admin:istrator");

        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testWithUrl() throws UnknownHostException {
        callTestWithUrl(false);
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testWithUrlIPv6() throws UnknownHostException {
        callTestWithUrl(true);
    }

    public void callTestWithUrl(boolean preferIPv6) throws UnknownHostException {
        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        PollStatus status = null;

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "HTTP", preferIPv6);

        m.put("port", "10342");
        m.put("retry", "0");
        m.put("timeout", "500");
        m.put("response", "100-499");
        m.put("verbose", "true");
        m.put("host-name", "localhost");
        m.put("url", "/twinkies.html");
        m.put("response-text", "~.*Don.t you love twinkies..*");

        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());

    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testWithInvalidNodelabelHostName() throws UnknownHostException {
        callTestWithInvalidNodelabelHostName(false);
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testWithInvalidNodelabelHostNameIPv6() throws UnknownHostException {
        callTestWithInvalidNodelabelHostName(true);
    }

    public void callTestWithInvalidNodelabelHostName(boolean preferIPv6) throws UnknownHostException {
        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        PollStatus status = null;

        ServiceMonitor monitor = new HttpMonitor();
        MockMonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "HTTP", preferIPv6);
        svc.setNodeLabel("bad.virtual.host.example.com");

        m.put("port", "10342");
        m.put("retry", "0");
        m.put("timeout", "500");
        // Ensure that we get a 404 for this GET since we're using an inappropriate virtual host
        m.put("response", "404");
        m.put("verbose", "true");
        m.put("nodelabel-host-name", "true");

        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());

    }

    @Test
    @JUnitHttpServer(port=10342, vhosts={"opennms.com"})
    public void testPollInInvalidVirtualDomain() throws UnknownHostException {
        callTestPollInInvalidVirtualDomain(false);
    }

    @Test
    @JUnitHttpServer(port=10342, vhosts={"opennms.com"})
    public void testPollInInvalidVirtualDomainIPv6() throws UnknownHostException {
        callTestPollInInvalidVirtualDomain(true);
    }

    public void callTestPollInInvalidVirtualDomain(boolean preferIPv6) throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "HTTP", preferIPv6);

        m.put("port", "10342");
        m.put("retry", "0");
        m.put("timeout", "500");
        m.put("host-name", "www.google.com");
        m.put("url", "/twinkies.html");
        m.put("response-text", "~.*twinkies.*");

        PollStatus status = monitor.poll(svc, m);
        assertEquals("poll status available", PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
    }

    @Test
    @JUnitHttpServer(port=10342, vhosts={"www.opennms.org"})
    public void testPollValidVirtualDomain() throws UnknownHostException {
        callTestPollValidVirtualDomain(false);
    }

    @Test
    @JUnitHttpServer(port=10342, vhosts={"www.opennms.org"})
    public void testPollValidVirtualDomainIPv6() throws UnknownHostException {
        callTestPollValidVirtualDomain(true);
    }

    public void callTestPollValidVirtualDomain(boolean preferIPv6) throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "HTTP", preferIPv6);

        m.put("port", "10342");
        m.put("retry", "1");
        m.put("timeout", "500");
        m.put("host-name", "www.opennms.org");
        m.put("url", "/twinkies.html");
        m.put("response-text", "~.*twinkies.*");

        PollStatus status = monitor.poll(svc, m);
        assertEquals("poll status not available", PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testNMS2702() throws UnknownHostException {
        HttpMonitor monitor = new HttpMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "10342");
        parameters.put("url", "/test-NMS2702.html");
        parameters.put("retry", "1");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");

        // Match a string included on Initial Server Response
        parameters.put("response-text", "~.*OK.*");
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "HTTP", false);
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isAvailable());

        // Match a string included on Header
        parameters.put("response-text", "~.*Jetty.*");
        svc = MonitorTestUtils.getMonitoredService(3, "localhost", "HTTP", false);
        status = monitor.poll(svc, parameters);
        assertTrue(status.isAvailable());
    }

}
