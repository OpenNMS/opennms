/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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
import static org.junit.Assert.assertTrue;
import static org.opennms.core.utils.InetAddressUtils.addr;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.context.ContextConfiguration;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="test", address="192.168.0.1")
            }),
            @DNSZone(name="ipv6.example.com", entries= {
                    @DNSEntry(hostname="ipv6test", address="2001:4860:8007::63", ipv6=true)
            })
    })
@JUnitConfigurationEnvironment
public class DnsMonitorTest {
    
    @Before
    public void setup() throws Exception {
        MockLogAppender.setupLogging(true);
    }
    
    @Test
    public void testIPV6Response() throws UnknownHostException {
        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        final ServiceMonitor monitor = new DnsMonitor();
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, addr("::1"), "DNS");

        m.put("port", "9153");
        m.put("retry", "1");
        m.put("timeout", "1000");
        m.put("lookup", "ipv6.example.com");
        
        final PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }
    
    @Test
    // type not found is still considered a valid response with the default response codes
    public void testNotFound() throws UnknownHostException {
        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        final ServiceMonitor monitor = new DnsMonitor();
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddressUtils.getLocalHostAddress(), "DNS");

        m.put("port", "9153");
        m.put("retry", "2");
        m.put("timeout", "5000");
        m.put("lookup", "bogus.example.com");
        
        final PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }
    
    @Test
    // type not found is still considered a valid response with the default response codes
    public void testNotFoundWithCustomRcode() throws UnknownHostException {
        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        final ServiceMonitor monitor = new DnsMonitor();
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddressUtils.getLocalHostAddress(), "DNS");

        m.put("port", "9153");
        m.put("retry", "2");
        m.put("timeout", "5000");
        m.put("lookup", "bogus.example.com");
        m.put("fatal-response-codes", "3");
        
        final PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
    }
    
    @Test
    public void testUnrecoverable() throws UnknownHostException {
        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        final ServiceMonitor monitor = new DnsMonitor();
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, addr("192.168.1.120"), "DNS");

        m.put("port", "9000");
        m.put("retry", "2");
        m.put("timeout", "500");
        
        final PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
    }
    
    @Test
    public void testDNSIPV4Response() throws UnknownHostException {
        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();

        final ServiceMonitor monitor = new DnsMonitor();
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddressUtils.getLocalHostAddress(), "DNS");

        m.put("port", "9153");
        m.put("retry", "1");
        m.put("timeout", "3000");
        m.put("lookup", "example.com");
        
        final PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }
    
    @Test
    public void testDnsJavaResponse() throws IOException {
        final Lookup l = new Lookup("example.com");
        final SimpleResolver resolver = new SimpleResolver("127.0.0.1");
        resolver.setPort(9153);
        l.setResolver(resolver);
        l.run();
        
        System.out.println("result: " + l.getResult());
        if(l.getResult() == Lookup.SUCCESSFUL) {
            System.out.println(l.getAnswers()[0].rdataToString());
        }
        assertTrue(l.getResult() == Lookup.SUCCESSFUL);
    }
    
    @Test
    public void testDnsJavaQuadARecord() throws IOException {
        final Lookup l = new Lookup("ipv6.example.com", Type.AAAA);
        final SimpleResolver resolver = new SimpleResolver("::1");
        resolver.setPort(9153);
        l.setResolver(resolver);
        l.run();
        
        System.out.println("result: " + l.getResult());
        if(l.getResult() == Lookup.SUCCESSFUL) {
            System.out.println(l.getAnswers()[0].rdataToString());
        }
        assertTrue(l.getResult() == Lookup.SUCCESSFUL);
    }
    
    @Test
    public void testDnsJavaWithDnsServer() throws TextParseException, UnknownHostException {
        final Lookup l = new Lookup("example.com", Type.AAAA);
        final SimpleResolver resolver = new SimpleResolver("::1");
        resolver.setPort(9153);
        l.setResolver(resolver);
        l.run();
        
        System.out.println("result: " + l.getResult());
        final Record[] answers = l.getAnswers();
        assertEquals(answers.length, 1);
        
        final Record record = answers[0];
        System.err.println(record.getTTL());
        
        if(l.getResult() == Lookup.SUCCESSFUL) {
            System.out.println(l.getAnswers()[0].rdataToString());
        }
        assertTrue(l.getResult() == Lookup.SUCCESSFUL);
    }

    @Test
    @JUnitDNSServer(port=9153, zones={})
    public void testNoAnswer() throws Exception {
        final Lookup l = new Lookup("example.com", Type.AAAA);
        final SimpleResolver resolver = new SimpleResolver("::1");
        resolver.setPort(9153);
        l.setResolver(resolver);
        l.run();
        
        System.out.println("result: " + l.getResult());
        final Record[] answers = l.getAnswers();
        assertEquals(answers.length, 1);
        
        final Record record = answers[0];
        System.err.println(record.getTTL());
        
        if(l.getResult() == Lookup.SUCCESSFUL) {
            System.out.println(l.getAnswers()[0].rdataToString());
        }
        assertTrue(l.getResult() == Lookup.SUCCESSFUL);
    }
}
