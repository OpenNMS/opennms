/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.dao.mock.MockMonitoringLocationDao;
import org.opennms.netmgt.dao.mock.MockNodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.netmgt.utils.DnsUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Autowired;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-mockDao.xml"
})
@JUnitConfigurationEnvironment
@DirtiesContext
public class HttpMonitorIT {
    @Autowired
    private MockMonitoringLocationDao m_locationDao;

    @Autowired
    private MockNodeDao m_nodeDao;

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
        InetAddress address = DnsUtils.resolveHostname("www.opennms.org");
        assertNotNull("Failed to resolved address: www.opennms.org", address);
        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, "www.opennms.org", address, "HTTP");

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
    @JUnitHttpServer(port=0)
    public void testResponseRange() throws UnknownHostException {
        callTestResponseRange(false);
    }

    @Test
    @JUnitHttpServer(port=0)
    public void testResponseRangeIPv6() throws UnknownHostException {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        callTestResponseRange(true);
    }

    public void callTestResponseRange(boolean preferIPv6) throws UnknownHostException {
        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", preferIPv6), "HTTP");

        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            m.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
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
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        callTestTimeout(true);
    }

    public void callTestTimeout(boolean preferIPv6) throws UnknownHostException {
        if (m_runTests == false) return;

        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        final ServiceMonitor monitor = new HttpMonitor();
        // We need a routable but unreachable address in order to simulate a timeout
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(3, preferIPv6 ? InetAddressUtils.UNPINGABLE_ADDRESS_IPV6 : InetAddressUtils.UNPINGABLE_ADDRESS, "HTTP");

        m.put("port", "12345");
        m.put("retry", "1");
        m.put("timeout", "500");
        m.put("response", "100-199");

        final PollStatus status = monitor.poll(svc, m);
        final String reason = status.getReason();
        MockUtil.println("Reason: "+reason);
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(reason);
        assertTrue(reason + "should be 'HTTP connection timeout', 'No route to host', or 'Network is unreachable'", reason.contains("HTTP connection timeout") || reason.contains("No route to host") || reason.contains("Network is unreachable"));
    }

    @Test
    @JUnitHttpServer()
    public void testMatchingTextInResponse() throws UnknownHostException {
        callTestMatchingTextInResponse(false);
    }

    @Test
    @JUnitHttpServer()
    public void testMatchingTextInResponseIPv6() throws UnknownHostException {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        callTestMatchingTextInResponse(true);
    }

    public void callTestMatchingTextInResponse(boolean preferIPv6) throws UnknownHostException {

        if (m_runTests == false) return;

        PollStatus status = null;
        ServiceMonitor monitor = new HttpMonitor();

        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", preferIPv6), "HTTP");

        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            m.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
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
    @JUnitHttpServer(basicAuth=true)
    public void testBasicAuthentication() throws UnknownHostException {
        callTestBasicAuthentication(false);
    }

    @Test
    @JUnitHttpServer(basicAuth=true)
    public void testBasicAuthenticationIPv6() throws UnknownHostException {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        callTestBasicAuthentication(true);
    }

    public void callTestBasicAuthentication(boolean preferIPv6) throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        PollStatus status = null;

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(1, "localhost", DnsUtils.resolveHostname("localhost", preferIPv6), "HTTP");

        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            m.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
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
    @JUnitHttpServer(https=true, basicAuth=true)
    public void testBasicAuthenticationWithHttps() throws UnknownHostException {
        callTestBasicAuthenticationWithHttps(false);
    }

    @Test
    @JUnitHttpServer(https=true, basicAuth=true)
    public void testBasicAuthenticationWithHttpsIPv6() throws UnknownHostException {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        callTestBasicAuthenticationWithHttps(true);
    }

    public void callTestBasicAuthenticationWithHttps(boolean preferIPv6) throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        PollStatus status = null;

        ServiceMonitor monitor = new HttpsMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(1, "localhost", DnsUtils.resolveHostname("localhost", preferIPv6), "HTTPS");

        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            m.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
        m.put("retry", "1");
        m.put("timeout", "500");
        m.put("response", "100-302");
        m.put("verbose", "true");
        m.put("host-name", "localhost");
        m.put("url", "/index.html");

        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertEquals("HTTP response value: 401. Expecting: 100-302./Ports: " + port, status.getReason());

        m.put("basic-authentication", "admin:istrator");

        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());
    }

    @Test
    @JUnitHttpServer()
    public void testWithUrl() throws UnknownHostException {
        callTestWithUrl(false);
    }

    @Test
    @JUnitHttpServer()
    public void testWithUrlIPv6() throws UnknownHostException {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        callTestWithUrl(true);
    }

    public void callTestWithUrl(boolean preferIPv6) throws UnknownHostException {
        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        PollStatus status = null;

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", preferIPv6), "HTTP");

        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            m.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
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
    @JUnitHttpServer()
    public void testWithInvalidNodelabelHostName() throws UnknownHostException {
        callTestWithInvalidNodelabelHostName(false);
    }

    @Test
    @JUnitHttpServer()
    public void testWithInvalidNodelabelHostNameIPv6() throws UnknownHostException {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        callTestWithInvalidNodelabelHostName(true);
    }

    public void callTestWithInvalidNodelabelHostName(boolean preferIPv6) throws UnknownHostException {
        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        PollStatus status = null;

        ServiceMonitor monitor = new HttpMonitor();
        MockMonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", preferIPv6), "HTTP");
        svc.setNodeLabel("bad.virtual.host.example.com");

        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            m.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
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
    @JUnitHttpServer(vhosts={"opennms.com"})
    public void testPollInInvalidVirtualDomain() throws UnknownHostException {
        callTestPollInInvalidVirtualDomain(false);
    }

    @Test
    @JUnitHttpServer(vhosts={"opennms.com"})
    public void testPollInInvalidVirtualDomainIPv6() throws UnknownHostException {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        callTestPollInInvalidVirtualDomain(true);
    }

    public void callTestPollInInvalidVirtualDomain(boolean preferIPv6) throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", preferIPv6), "HTTP");

        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            m.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
        m.put("retry", "0");
        m.put("timeout", "500");
        m.put("host-name", "www.google.com");
        m.put("url", "/twinkies.html");
        m.put("response-text", "~.*twinkies.*");

        PollStatus status = monitor.poll(svc, m);
        assertEquals("poll status available", PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
    }

    @Test
    @JUnitHttpServer(vhosts={"www.opennms.org"})
    public void testPollValidVirtualDomain() throws UnknownHostException {
        callTestPollValidVirtualDomain(false);
    }

    @Test
    @JUnitHttpServer(vhosts={"www.opennms.org"})
    public void testPollValidVirtualDomainIPv6() throws UnknownHostException {
        assumeTrue(!Boolean.getBoolean("skipIpv6Tests"));
        callTestPollValidVirtualDomain(true);
    }

    public void callTestPollValidVirtualDomain(boolean preferIPv6) throws UnknownHostException {

        if (m_runTests == false) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", preferIPv6), "HTTP");

        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            m.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
        m.put("retry", "1");
        m.put("timeout", "500");
        m.put("host-name", "www.opennms.org");
        m.put("url", "/twinkies.html");
        m.put("response-text", "~.*twinkies.*");

        PollStatus status = monitor.poll(svc, m);
        assertEquals("poll status not available", PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }

    @Test
    @JUnitHttpServer()
    public void testNMS2702() throws UnknownHostException {
        HttpMonitor monitor = new HttpMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            parameters.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
        parameters.put("url", "/test-NMS2702.html");
        parameters.put("retry", "1");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");

        // Match a string included on Initial Server Response
        parameters.put("response-text", "~.*OK.*");
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "HTTP");
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isAvailable());

        // Match a string included on Header
        parameters.put("response-text", "~.*Jetty.*");
        svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "HTTP");
        status = monitor.poll(svc, parameters);
        assertTrue(status.isAvailable());
    }

    @Test
    @JUnitHttpServer()
    public void testParameterSubstitution() throws UnknownHostException {
        HttpMonitor monitor = new HttpMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            parameters.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
        parameters.put("url", "/test-NMS2702.html");
        parameters.put("retry", "1");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("user-agent", "Hello from {ipAddr} {ipAddress} {nodeId} {nodeLabel}");
        parameters.put("response-text", "~.*OK.*");
        MockMonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "HTTP");
        svc.setNodeLabel("localhost.localdomain");
        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, parameters);
        assertTrue(subbedParams.get("subbed-user-agent").toString().equals("Hello from 127.0.0.1 127.0.0.1 3 localhost.localdomain"));
    }

    @Test
    @JUnitHttpServer()
    public void testParameterSubstitutionWithNodeAssets() throws UnknownHostException {
        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "devjam2018nodelabel");
        node.setForeignSource("AlienSource");
        node.setForeignId("31337");
        node.setId(m_nodeDao.getNextNodeId());

        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 1);
        snmpInterface.setId(1);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("en1");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:01");

        Set<OnmsIpInterface> ipInterfaces = new LinkedHashSet<OnmsIpInterface>(1);
        InetAddress address = InetAddress.getByName("10.0.1.1");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setId(1);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName("devjam2018nodelabel");
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);

        ipInterfaces.add(onmsIf);

        node.setIpInterfaces(ipInterfaces);
        m_nodeDao.save(node);
        m_nodeDao.flush();
        HttpMonitor monitor = new HttpMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            parameters.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
        parameters.put("url", "/test-NMS2702.html");
        parameters.put("retry", "1");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("user-agent", "Hello from {foreignId} {foreignSource} {nodeLabel}");
        parameters.put("response-text", "~.*OK.*");
        MockMonitoredService svc = MonitorTestUtils.getMonitoredService(Integer.parseInt(node.getNodeId()), "devjam2018nodelabel", InetAddress.getByName("10.0.1.1"), "HTTP");
        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, parameters);
        assertTrue(subbedParams.get("subbed-user-agent").toString().equals("Hello from 31337 AlienSource devjam2018nodelabel"));
    }

    @Test
    @JUnitHttpServer()
    public void testUserPasswordParameterSubstitution() throws UnknownHostException {
        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "devjam2018nodelabel2");
        node.setForeignSource("AlienSource");
        node.setForeignId("31338");
        node.setId(m_nodeDao.getNextNodeId());
        OnmsAssetRecord oar = node.getAssetRecord();
        oar.setUsername("peterman");
        oar.setPassword("nine");

        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 2);
        snmpInterface.setId(2);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("en1");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:02");

        Set<OnmsIpInterface> ipInterfaces = new LinkedHashSet<OnmsIpInterface>(1);
        InetAddress address = InetAddress.getByName("10.0.1.2");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setId(2);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName("devjam2018nodelabel2");
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);

        ipInterfaces.add(onmsIf);

        node.setIpInterfaces(ipInterfaces);
        m_nodeDao.save(node);
        m_nodeDao.flush();
        HttpMonitor monitor = new HttpMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            parameters.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
        parameters.put("url", "/test-NMS2702.html");
        parameters.put("retry", "1");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("user", "dude{username}");
        parameters.put("password", "channel{password}");
        parameters.put("response-text", "~.*OK.*");
        MockMonitoredService svc = MonitorTestUtils.getMonitoredService(Integer.parseInt(node.getNodeId()), "devjam2018nodelabel2", InetAddress.getByName("10.0.1.2"), "HTTP");
        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, parameters);
        assertTrue(subbedParams.get("subbed-user").toString().equals("dudepeterman"));
        assertTrue(subbedParams.get("subbed-password").toString().equals("channelnine"));
    }

    @Test
    @JUnitHttpServer()
    public void testUrlParameterSubstitution() throws UnknownHostException {
        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "devjam2018nodelabel3");
        node.setForeignSource("AlienSource");
        node.setForeignId("31339");
        node.setId(m_nodeDao.getNextNodeId());
        OnmsAssetRecord oar = node.getAssetRecord();
        oar.setCity("NMS2702");

        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 3);
        snmpInterface.setId(3);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("en1");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:03");

        Set<OnmsIpInterface> ipInterfaces = new LinkedHashSet<OnmsIpInterface>(1);
        InetAddress address = InetAddress.getByName("127.0.0.1");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setId(3);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName("devjam2018nodelabel3");
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);

        ipInterfaces.add(onmsIf);

        node.setIpInterfaces(ipInterfaces);
        m_nodeDao.save(node);
        m_nodeDao.flush();
        HttpMonitor monitor = new HttpMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            parameters.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
        parameters.put("url", "/test-{city}.html");
        parameters.put("retry", "1");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("response-text", "~.*OK.*");
        MockMonitoredService svc = MonitorTestUtils.getMonitoredService(Integer.parseInt(node.getNodeId()), "devjam2018nodelabel3", InetAddress.getByName("127.0.0.1"), "HTTP");
        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, parameters);
        // this would normally happen in the poller request builder implementation
        subbedParams.forEach((k, v) -> {
            parameters.put(k, v);
        });
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isAvailable());
    }

    @Test
    @JUnitHttpServer()
    public void testBasicAuthParameterSubstitution() throws UnknownHostException {
        OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "devjam2018nodelabel4");
        node.setForeignSource("AlienSource");
        node.setForeignId("31340");
        node.setId(m_nodeDao.getNextNodeId());
        OnmsAssetRecord oar = node.getAssetRecord();
        oar.setUsername("nimda");
        oar.setPassword("@dm1n");

        OnmsSnmpInterface snmpInterface = new OnmsSnmpInterface(node, 4);
        snmpInterface.setId(4);
        snmpInterface.setIfAlias("Connection to OpenNMS Wifi");
        snmpInterface.setIfDescr("en1");
        snmpInterface.setIfName("en1/0");
        snmpInterface.setPhysAddr("00:00:00:00:00:04");

        Set<OnmsIpInterface> ipInterfaces = new LinkedHashSet<OnmsIpInterface>(1);
        InetAddress address = InetAddress.getByName("10.0.1.4");
        OnmsIpInterface onmsIf = new OnmsIpInterface(address, node);
        onmsIf.setSnmpInterface(snmpInterface);
        onmsIf.setId(4);
        onmsIf.setIfIndex(1);
        onmsIf.setIpHostName("devjam2018nodelabel4");
        onmsIf.setIsSnmpPrimary(PrimaryType.PRIMARY);

        ipInterfaces.add(onmsIf);

        node.setIpInterfaces(ipInterfaces);
        m_nodeDao.save(node);
        m_nodeDao.flush();
        HttpMonitor monitor = new HttpMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        final int port = JUnitHttpServerExecutionListener.getPort();
        if (port > 0) {
            parameters.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }
        parameters.put("url", "/test-NMS2702.html");
        parameters.put("retry", "1");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("basic-authentication", "{username}:{password}");
        parameters.put("response-text", "~.*OK.*");
        MockMonitoredService svc = MonitorTestUtils.getMonitoredService(Integer.parseInt(node.getNodeId()), "devjam2018nodelabel2", InetAddress.getByName("10.0.1.2"), "HTTP");
        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, parameters);
        assertTrue(subbedParams.get("subbed-basic-authentication").toString().equals("nimda:@dm1n"));
    }
}
