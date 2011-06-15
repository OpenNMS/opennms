package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.annotations.DNSEntry;
import org.opennms.core.test.annotations.DNSZone;
import org.opennms.core.test.annotations.JUnitDNSServer;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.dao.db.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.db.OpenNMSJUnit4ClassRunner;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;
import org.opennms.test.mock.MockLogAppender;
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
	    MockLogAppender.setupLogging();
	}
	
	@Test
	public void testParams() {
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
	
	@Test
	public void testIPV6Response() throws UnknownHostException {
	    Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();

        ServiceMonitor monitor = new DnsMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, "localhost", "DNS", true);


        p.setKey("port");
        p.setValue("9153");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("1000");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("lookup");
        p.setValue("ipv6.example.com");
        m.put(p.getKey(), p.getValue());
        
        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
	}
	
	@Test
    public void testTypeNotFound() throws UnknownHostException {
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();

        ServiceMonitor monitor = new DnsMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, "localhost", "DNS", true);

        p.setKey("port");
        p.setValue("9153");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("2");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("5000");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("lookup");
        p.setValue("bogus.example.com");
        m.put(p.getKey(), p.getValue());
        
        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
    }
	
	@Test
    public void testUnrecoverable() throws UnknownHostException {
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();

        ServiceMonitor monitor = new DnsMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddressUtils.addr("192.168.1.120"), "DNS");


        p.setKey("port");
        p.setValue("9000");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("2");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());
        
        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
    }
	
	@Test
    public void testDNSIPV4Response() throws UnknownHostException {
        Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        Parameter p = new Parameter();

        ServiceMonitor monitor = new DnsMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, "localhost", "DNS", false);


        p.setKey("port");
        p.setValue("9153");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("3000");
        m.put(p.getKey(), p.getValue());
        
        p.setKey("lookup");
        p.setValue("example.com");
        m.put(p.getKey(), p.getValue());
        
        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }
	
	@Test
    public void testDnsJavaResponse() throws IOException {
        Lookup l = new Lookup("example.com");
        SimpleResolver resolver = new SimpleResolver("127.0.0.1");
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
	    Lookup l = new Lookup("ipv6.example.com", Type.AAAA);
	    SimpleResolver resolver = new SimpleResolver("::1");
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
	    Lookup l = new Lookup("example.com", Type.AAAA);
        SimpleResolver resolver = new SimpleResolver("::1");
        resolver.setPort(9153);
        l.setResolver(resolver);
        l.run();
        
        System.out.println("result: " + l.getResult());
        Record[] answers = l.getAnswers();
        assertEquals(answers.length, 1);
        
        Record record = answers[0];
        System.err.println(record.getTTL());
        
        if(l.getResult() == Lookup.SUCCESSFUL) {
            System.out.println(l.getAnswers()[0].rdataToString());
        }
        assertTrue(l.getResult() == Lookup.SUCCESSFUL);
	}
}
