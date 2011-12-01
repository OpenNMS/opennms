package org.opennms.netmgt.poller.monitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.poller.monitors.SeleniumMonitor;
import org.opennms.netmgt.poller.monitors.SeleniumMonitor.BaseUrlUtils;


public class SeleniumMonitorTest {
	
	
	public static class MockMonService implements MonitoredService{
	    
	    private int m_nodeId;
        private String m_nodeLabel;
        private InetAddress m_inetAddr;
        private String m_svcName;
        private String m_ipAddr;

        public MockMonService(int nodeId, String nodeLabel, InetAddress inetAddress, String svcName) throws UnknownHostException {
	        m_nodeId = nodeId;
	        m_nodeLabel = nodeLabel;
	        m_inetAddr = inetAddress;
	        m_svcName = svcName;
	        m_ipAddr = InetAddressUtils.toIpAddrString(inetAddress.getAddress());
	    }
	    
        public String getSvcUrl() {
            return null;
        }

        public String getSvcName() {
            return m_svcName;
        }

        public String getIpAddr() {
            return m_ipAddr;
        }

        public int getNodeId() {
            return m_nodeId;
        }

        public String getNodeLabel() {
            return m_nodeLabel;
        }

        public NetworkInterface getNetInterface() {
            return new IPv4NetworkInterface(m_inetAddr);
        }

        public InetAddress getAddress() {
            return m_inetAddr;
        }
	    
	}
	
	@Before
	public void setup() throws Exception{
		System.setProperty("opennms.home", "src/test/resources");
	}
	
	@Test
	public void testPollStatusNotNull() throws UnknownHostException{
	    MonitoredService monSvc = new MockMonService(1, "papajohns", InetAddress.getByName("http://www.papajohns.co.uk"), "PapaJohnsSite");
	    
	    Map<String, Object> params = new HashMap<String, Object>();
	    params.put("selenium-test", "SeleniumGroovyTest.groovy");
	    params.put("base-url", "${ipAddr}");
	    
		SeleniumMonitor ajaxPSM = new SeleniumMonitor();
		PollStatus pollStatus = ajaxPSM.poll(monSvc, params);
		
		assertNotNull("PollStatus must not be null", pollStatus);
		
		System.err.println("PollStatus message: " + pollStatus.getReason());
		assertEquals(PollStatus.available(), pollStatus);
		
	}
	
	@Test
    public void testDefaultGroovyJUnitTest() throws UnknownHostException{
	    System.setProperty("opennms.selenium.test.dir", "/Users/thedesloge/git/opennms/target/opennms-1.9.93-SNAPSHOT/etc");
        MonitoredService monSvc = new MockMonService(1, "papajohns", InetAddress.getByName("213.187.33.164"), "PapaJohnsSite");
        
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("selenium-test", "OpennmsSeleniumExample.groovy");
        params.put("base-url", "http://${ipAddr}");
        
        SeleniumMonitor ajaxPSM = new SeleniumMonitor();
        PollStatus pollStatus = ajaxPSM.poll(monSvc, params);
        
        assertNotNull("PollStatus must not be null", pollStatus);
        
        System.err.println("PollStatus message: " + pollStatus.getReason());
        assertEquals(PollStatus.available(), pollStatus);
        
    }
	
	
	@Test
	public void testBaseUrlUtils() 
	{
	    
	    String baseUrl = "http://${ipAddr}:8080";
	    String monSvcIpAddr = "192.168.1.1";
	    String finalUrl = "";
	    
	    finalUrl = BaseUrlUtils.replaceIpAddr(baseUrl, monSvcIpAddr);
	    
	    assertEquals("http://192.168.1.1:8080", finalUrl);
	}
	
	
	
	@After
	public void tearDown(){
		
	}
}
