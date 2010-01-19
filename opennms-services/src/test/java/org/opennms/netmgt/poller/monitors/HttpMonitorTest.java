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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
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
import org.opennms.netmgt.mock.MockMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
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
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/emptyContext.xml",
})
public class HttpMonitorTest {

    private boolean m_runTests = true;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }
    
    @Test
    public void testParms() {
        Parms eventParms = new Parms();
        Parm eventParm = new Parm();
        Value parmValue = new Value();
        
        assertTrue(eventParms.getParmCount() == 0);
        
        eventParm.setParmName("test");
        parmValue.setContent("test value");
        eventParm.setValue(parmValue);
        eventParms.addParm(eventParm);
        
        assertTrue(eventParms.getParmCount() == 1);
        assertTrue(eventParms.getParm(0).getParmName() == "test");
        assertTrue(eventParms.getParm(0).getValue().getContent() == "test value");
        
    }

    /*
     * Test method for 'org.opennms.netmgt.poller.monitors.HttpMonitor.poll(NetworkInterface, Map, Package)'
     */
    @Test
    public void testPollStatusReason() throws UnknownHostException {
        
        if (m_runTests == false) return;
        
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();
        
        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = getMonitoredService(99, "1.1.1.1", "HTTP");

        
        p.setKey("port");
        p.setValue("80");
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
    public void testResponseRange() throws UnknownHostException {
        
        if (m_runTests == false) return;
        
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();
        
        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = getMonitoredService(3, "localhost", "HTTP");

        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("response");
        p.setValue("100-199");
        m.put(p.getKey(), p.getValue());
        
        PollStatus status = monitor.poll(svc, m);        
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(status.getReason());

        p.setKey("response");
        p.setValue("100,200,302,400-500");
        m.put(p.getKey(), p.getValue());
        
        monitor = new HttpMonitor();
        status = monitor.poll(svc, m);        
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());

        p.setKey("response");
        p.setValue("*");
        m.put(p.getKey(), p.getValue());
        
        monitor = new HttpMonitor();
        status = monitor.poll(svc, m);        
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testMatchingTextInResponse() throws UnknownHostException {
        
        if (m_runTests == false) return;
        
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();
        PollStatus status = null;
        
        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = getMonitoredService(3, "localhost", "HTTP");

        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("retry");
        p.setValue("0");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("response");
        p.setValue("100-499");
        m.put(p.getKey(), p.getValue());
                
        p.setKey("verbose");
        p.setValue("true");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("host-name");
        p.setValue("localhost");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("url");
        p.setValue("/");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("response-text");
        p.setValue("opennmsrulz");
        m.put(p.getKey(), p.getValue());

        status = monitor.poll(svc, m);        
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(status.getReason());

        p.setKey("response-text");
        p.setValue("written by monkeys");
        m.put(p.getKey(), p.getValue());
        
        MockUtil.println("\nliteral text check: \"written by monkeys\"");
        monitor = new HttpMonitor();
        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());

        p.setKey("response-text");
        p.setValue("~.*[Tt]est HTTP [Ss]erver.*");
        m.put(p.getKey(), p.getValue());

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
        
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();
        HttpMonitor monitor = new HttpMonitor();
        p.setKey("basic-authentication");
        p.setValue("Aladdin:open sesame");
        m.put(p.getKey(), p.getValue());
        assertEquals("QWxhZGRpbjpvcGVuIHNlc2FtZQ==", monitor.determineBasicAuthentication(m));
        assertFalse( "QWxhZGRpbjpvcZVuIHNlc2FtZQ==".equals(monitor.determineBasicAuthentication(m)));
    }
    
    private MonitoredService getMonitoredService(int nodeId, String hostname, String svcName) throws UnknownHostException {
        return new MockMonitoredService(nodeId, hostname, InetAddress.getByName(hostname).getHostAddress(), svcName);
    }

    @Test
    @JUnitHttpServer(port=10342, basicAuth=true)
    public void testBasicAuthentication() throws UnknownHostException {
        
        if (m_runTests == false) return;
        
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();
        PollStatus status = null;
        
        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = getMonitoredService(1, "localhost", "HTTP");
        
        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("retry");
        p.setValue("0");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("response");
        p.setValue("100-302");
        m.put(p.getKey(), p.getValue());

        p.setKey("verbose");
        p.setValue("true");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("host-name");
        p.setValue("localhost");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("url");
        p.setValue("/");
        m.put(p.getKey(), p.getValue());

        p.setKey("basic-authentication");
        p.setValue("admin:istrator");
        m.put(p.getKey(), p.getValue());
        
        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());
        
        
        p.setKey("basic-authentication");
        p.setValue("admin:flagrator");
        m.put(p.getKey(), p.getValue());
        
        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(status.getReason());

        
    }

    @Test
    @JUnitHttpServer(port=10342, https=true)
    public void testBasicAuthenticationWithHttps() throws UnknownHostException {
        
        if (m_runTests == false) return;
        
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();
        PollStatus status = null;
        
        ServiceMonitor monitor = new HttpsMonitor();
        MonitoredService svc = getMonitoredService(1, "localhost", "HTTP");
        
        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("response");
        p.setValue("100-302");
        m.put(p.getKey(), p.getValue());
                
        p.setKey("verbose");
        p.setValue("true");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("host-name");
        p.setValue("localhost");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("url");
        p.setValue("/index.html");
        m.put(p.getKey(), p.getValue());

        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());
        
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testWithUrl() throws UnknownHostException {
        if (m_runTests == false) return;
        
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();
        PollStatus status = null;
        
        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = getMonitoredService(3, "localhost", "HTTP");
        
        p.setKey("host-name");
        p.setValue("localhost");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("url");
        p.setValue("/twinkies.html");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("retry");
        p.setValue("0");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("response");
        p.setValue("100-499");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("response-text");
        p.setValue("~.*Don.t you love twinkies..*");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("verbose");
        p.setValue("true");
        m.put(p.getKey(), p.getValue());
        
        status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());

    }

    @Test
    @JUnitHttpServer(port=10342, vhosts={"opennms.com"})
    public void testPollInInvalidVirtualDomain() throws UnknownHostException {

        if (m_runTests == false) return;
        
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();
        
        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = getMonitoredService(3, "localhost", "HTTP");
        
        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());

        p.setKey("host-name");
        p.setValue("www.google.com");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("url");
        p.setValue("/twinkies.html");
        m.put(p.getKey(), p.getValue());

        p.setKey("response-text");
        p.setValue("~.*twinkies.*");
        m.put(p.getKey(), p.getValue());

        PollStatus status = monitor.poll(svc, m);
        assertEquals("poll status available", PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
    }

    @Test
    @JUnitHttpServer(port=10342, vhosts={"www.opennms.org"})
    public void testPollInValidVirtualDomain() throws UnknownHostException {

        if (m_runTests == false) return;
        
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();
        
        ServiceMonitor monitor = new HttpMonitor();
        MonitoredService svc = getMonitoredService(3, "localhost", "HTTP");
        
        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());

        p.setKey("host-name");
        p.setValue("www.opennms.org");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("url");
        p.setValue("/twinkies.html");
        m.put(p.getKey(), p.getValue());

        p.setKey("response-text");
        p.setValue("~.*twinkies.*");
        m.put(p.getKey(), p.getValue());

        PollStatus status = monitor.poll(svc, m);
        assertEquals("poll status not available", PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }

}
