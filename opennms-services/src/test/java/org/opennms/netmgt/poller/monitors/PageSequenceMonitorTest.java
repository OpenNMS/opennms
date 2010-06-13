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

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

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
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    JUnitHttpServerExecutionListener.class
})
@ContextConfiguration(locations="classpath:META-INF/opennms/emptyContext.xml")
@JUnitHttpServer(port=10342)
public class PageSequenceMonitorTest {
	
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
    	return getHttpService(hostname, InetAddress.getByName(hostname).getHostAddress());
    }
    
    protected MonitoredService getHttpService(String hostname, String ip) throws Exception {
    	MonitoredService svc = new MockMonitoredService(1, hostname, ip, "HTTP");
    	m_monitor.initialize(svc);
    	return svc;
    }

    
    @After
	public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
	}
    
    @Test
    public void testSimple() throws Exception {
        setPageSequenceParam("localhost");
        PollStatus googleStatus = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+googleStatus+": reason = "+googleStatus.getReason(), googleStatus.isAvailable());
    }

    @Test
    public void testSimpleBogus() throws Exception {
        setPageSequenceParam(null);
        m_params.put("timeout", "500");
        m_params.put("retries", "0");
		PollStatus notLikely = m_monitor.poll(getHttpService("bogus", "1.1.1.1"), m_params);
		assertTrue("should not be available", notLikely.isUnavailable());
    }

    private void setPageSequenceParam(String virtualHost) {
        String virtualHostParam;
        if (virtualHost == null) {
            virtualHostParam = "";
        } else {
            virtualHostParam = "virtual-host=\"" + virtualHost + "\"";
        }
        
        m_params.put("page-sequence", "" +
				"<?xml version=\"1.0\"?>" +
				"<page-sequence>\n" + 
				"  <page path=\"/index.html\" port=\"10342\" user-agent=\"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)\" successMatch=\"It was written by monkeys.\" " + virtualHostParam + "/>\n" + 
				"</page-sequence>\n");
    }

    @Test
    public void testHttps() throws Exception {
		m_params.put("page-sequence", "" +
				"<?xml version=\"1.0\"?>" +
				"<page-sequence>\n" + 
				"  <page scheme=\"https\" path=\"/ws/eBayISAPI.dll?RegisterEnterInfo\" port=\"443\" user-agent=\"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)\" successMatch=\"Hi! Ready to register with eBay?\" virtual-host=\"support.opennms.com\"/>\n" + 
				"</page-sequence>\n");
		
		
        PollStatus googleStatus = m_monitor.poll(getHttpService("scgi.ebay.com"), m_params);
        assertTrue("Expected available but was "+googleStatus+": reason = "+googleStatus.getReason(), googleStatus.isAvailable());
        

    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
	public void testLogin() throws Exception {
        
		m_params.put("page-sequence", "" +
				"<?xml version=\"1.0\"?>" +
				"<page-sequence>\n" + 
				"  <page virtual-host=\"localhost\" path=\"/opennms/\" port=\"10342\" successMatch=\"Password\" />\n" + 
				"  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\">\n" + 
                "    <parameter key=\"j_username\" value=\"demo\"/>\n" + 
                "    <parameter key=\"j_password\" value=\"demo\"/>\n" + 
                "  </page>\n" + 
				"  <page virtual-host=\"localhost\" path=\"/opennms/events.html\" port=\"10342\" successMatch=\"Event Queries\" />\n" + 
				"  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_logout\" port=\"10342\" successMatch=\"Login with Username and Password\" />\n" + 
				"</page-sequence>\n");
		
		PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
		assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
		
	}
	
    @Test
	public void testVirtualHost() throws Exception {
		m_params.put("page-sequence", "" +
				"<?xml version=\"1.0\"?>" +
				"<page-sequence>\n" + 
				"  <page user-agent=\"Donald\" path=\"/\" port=\"80\" successMatch=\"Get the Network to Work\" virtual-host=\"www.opennms.com\"/>\n" + 
				"</page-sequence>\n");
		
		
		PollStatus status = m_monitor.poll(getHttpService("www.opennms.com"), m_params);
		assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
		
	}
    
    @Test
    public void testVirtualHostBadBehaviorForWordpressPlugin() throws Exception {
        m_params.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence>\n" + 
                "  <page path=\"/\" port=\"80\" successMatch=\"Get the Network to Work\" user-agent=\"Jakarta Commons-HttpClient/3.0.1\" virtual-host=\"www.opennms.com\"/>\n" + 
                "</page-sequence>\n");
        
        
        PollStatus status = m_monitor.poll(getHttpService("www.opennms.com"), m_params);
        assertTrue("Expected unavailable but was "+status+": reason = "+status.getReason(), status.isDown());
        
    }
	
    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testLoginDynamicCredentials() throws Exception {
        m_params.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence>\n" + 
                "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&lt;hea(.)&gt;&lt;titl(.)&gt;.*&lt;/for(.)&gt;&lt;/b(.)dy&gt;\">\n" +
                "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
                "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
                "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
                "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
                "  </page>\n" +
                "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\">\n" +
                "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                "  </page>\n" + 
                "  <page virtual-host=\"localhost\" path=\"/opennms/events.html\" port=\"10342\" successMatch=\"Event Queries\" />\n" + 
                "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_logout\" port=\"10342\" successMatch=\"Login with Username and Password\" />\n" + 
                "</page-sequence>\n");
        
        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testLoginDynamicCredentialsTwice() throws Exception {

        m_params.put("page-sequence", "" +
                     "<?xml version=\"1.0\"?>" +
                     "<page-sequence>\n" + 
                     "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&gt;Login (.)(.)(.)(.) Username and Password&lt;\">\n" +
                     "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
                     "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
                     "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
                     "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
                     "  </page>\n" +
                     "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\"  response-range=\"300-399\" port=\"10342\" method=\"POST\">\n" +
                     "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                     "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                     "  </page>\n" + 
                     "  <page virtual-host=\"localhost\" path=\"/opennms/spring_security_login?login_error\"  port=\"10342\" method=\"POST\" failureMatch=\"(?s)Log out\" failureMessage=\"Login should have Failed but did not\" successMatch=\"(?s)Your login attempt was not successful.*\" />\n" +
                     "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&lt;hea(.)&gt;&lt;titl(.)&gt;.*&lt;/for(.)&gt;&lt;/b(.)dy&gt;\">\n" +
                     "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
                     "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
                     "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
                     "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
                     "  </page>\n" +
                     "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\"  response-range=\"300-399\" port=\"10342\" method=\"POST\">\n" +
                     "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                     "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                     "  </page>\n" + 
                     "  <page virtual-host=\"localhost\" path=\"/opennms/events.html\" port=\"10342\" successMatch=\"Event Queries\" />\n" + 
                     "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_logout\" port=\"10342\" successMatch=\"Login with Username and Password\" />\n" + 
                     "</page-sequence>\n");
             
             PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
             assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
             
    }
    
    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testLoginDynamicCredentialsRedirectPost() throws Exception {
        m_params.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence>\n" + 
                "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&lt;hea(.)&gt;&lt;titl(.)&gt;.*&lt;/for(.)&gt;&lt;/b(.)dy&gt;\">\n" +
                "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
                "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
                "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
                "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
                "  </page>\n" +
                "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" failureMatch=\"(?s)Your login attempt was not successful.*Reason: ([^&lt;]*)\" failureMessage=\"Login in Failed: ${1}\" successMatch=\"Log out\">\n" +
                "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                "  </page>\n" + 
                "  <page virtual-host=\"localhost\" path=\"/opennms/events.html\" port=\"10342\" successMatch=\"Event Queries\" />\n" + 
                "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_logout\" port=\"10342\" successMatch=\"Login with Username and Password\" />\n" + 
                "</page-sequence>\n");
        
        Map<String,Object> params = new HashMap<String,Object>();
        for (Entry<String,Object> entry : m_params.entrySet()) {
            params.put(entry.getKey(), entry.getValue());
        }
        params.put("redirect-post", "true");
        PollStatus status = m_monitor.poll(getHttpService("localhost"), params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testRedirectLocationMatch() throws Exception {
        m_params.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence>\n" + 
                "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&lt;hea(.)&gt;&lt;titl(.)&gt;.*&lt;/for(.)&gt;&lt;/b(.)dy&gt;\">\n" +
                "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
                "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
                "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
                "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
                "  </page>\n" +
                "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\" locationMatch=\"/opennms/\">\n" +
                "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                "  </page>\n" + 
                "</page-sequence>\n");
        
        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        
    }

    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/loginTestWar"))
    public void testRedirectLocationDoesNotMatch() throws Exception {
        m_params.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence>\n" + 
                "  <page path=\"/opennms/\" port=\"10342\" virtual-host=\"localhost\" successMatch=\"(?s)&lt;hea(.)&gt;&lt;titl(.)&gt;.*&lt;/for(.)&gt;&lt;/b(.)dy&gt;\">\n" +
                "    <session-variable name=\"ltr1\" match-group=\"1\" />\n" +
                "    <session-variable name=\"ltr2\" match-group=\"2\" />\n" +
                "    <session-variable name=\"ltr3\" match-group=\"3\" />\n" +
                "    <session-variable name=\"ltr4\" match-group=\"4\" />\n" +
                "  </page>\n" +
                "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\" locationMatch=\"/opensadfnms/\">\n" +
                "    <parameter key=\"j_username\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                "    <parameter key=\"j_password\" value=\"${ltr1}${ltr2}${ltr3}${ltr4}\"/>\n" + 
                "  </page>\n" + 
                "</page-sequence>\n");
        
        PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
        assertTrue("Expected down but was "+status+": reason = "+status.getReason(), status.isDown());
        
    }

    @Test
    public void testDsNamePerPage() throws Exception {
        m_params.put("page-sequence", "" +
                     "<?xml version=\"1.0\"?>" +
                     "<page-sequence>\n" + 
                     "  <page path=\"/opennms/\" ds-name=\"slash\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"(?s)User:.*&lt;strong&gt;(.*?)&lt;/strong&gt;.*?Password:.*?&lt;strong&gt;(.*?)&lt;/strong&gt;\" />\n" +
                     "  <page path=\"/opennms/acegilogin.jsp\" ds-name=\"acegilogin\"  port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"(?s)User:.*&lt;strong&gt;(.*?)&lt;/strong&gt;.*?Password:.*?&lt;strong&gt;(.*?)&lt;/strong&gt;\" />\n" +
                     "</page-sequence>\n");
                          
             PollStatus status = m_monitor.poll(getHttpService("demo.opennms.org"), m_params);
             assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
             assertTrue("Expected three DSes", (3 == status.getProperties().size()));
             assertTrue("Expected a DS called 'slash' but did not find one", status.getProperties().containsKey("slash"));
             assertTrue("Expected a DS called 'acegilogin' but did not find one", status.getProperties().containsKey("acegilogin"));
    }
    
}
