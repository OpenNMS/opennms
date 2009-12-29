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

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.opennms.netmgt.mock.MockMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.test.mock.MockLogAppender;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
public class PageSequenceMonitorTest extends TestCase {
	
	PageSequenceMonitor m_monitor;
	Map<String, String> m_params;
	

    @Override
	protected void setUp() throws Exception {
		super.setUp();
        
        MockLogAppender.setupLogging();
		
    	m_monitor = new PageSequenceMonitor();
    	m_monitor.initialize(Collections.EMPTY_MAP);
    	
		m_params = new HashMap<String, String>();
		m_params.put("timeout", "6000");
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

    
    @Override
	protected void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
        
		super.tearDown();
	}
    
    public void testSimpleGoogle() throws Exception {
        setPageSequenceParam("www.google.com");
        PollStatus googleStatus = m_monitor.poll(getHttpService("www.google.com"), m_params);
        assertTrue("Expected available but was "+googleStatus+": reason = "+googleStatus.getReason(), googleStatus.isAvailable());
    }
        
    public void testSimpleBogus() throws Exception {
        setPageSequenceParam(null);
		PollStatus notLikely = m_monitor.poll(getHttpService("bogus", "1.1.1.1"), m_params);
		assertTrue("should not be available", notLikely.isUnavailable());
    }

    public void testSimpleYahoo() throws Exception {
        setPageSequenceParam("www.yahoo.com");
        PollStatus yahooStatus = m_monitor.poll(getHttpService("www.yahoo.com"), m_params);
        assertTrue("should not be available", yahooStatus.isUnavailable());
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
				"  <page path=\"/\" port=\"80\" user-agent=\"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)\" successMatch=\"I'm Feeling Lucky\" " + virtualHostParam + "/>\n" + 
				"</page-sequence>\n");
    }

    public void _testHttps() throws Exception {
		m_params.put("page-sequence", "" +
				"<?xml version=\"1.0\"?>" +
				"<page-sequence>\n" + 
				"  <page scheme=\"https\" path=\"/ws/eBayISAPI.dll?RegisterEnterInfo\" port=\"443\" user-agent=\"Mozilla/4.0 (compatible; MSIE 5.5; Windows NT 5.0)\" successMatch=\"Hi! Ready to register with eBay?\" virtual-host=\"support.opennms.com\"/>\n" + 
				"</page-sequence>\n");
		
		
        PollStatus googleStatus = m_monitor.poll(getHttpService("scgi.ebay.com"), m_params);
        assertTrue("Expected available but was "+googleStatus+": reason = "+googleStatus.getReason(), googleStatus.isAvailable());
        

    }

	public void testLogin() throws Exception {
		m_params.put("page-sequence", "" +
				"<?xml version=\"1.0\"?>" +
				"<page-sequence name=\"opennms-login-seq\">\n" + 
				"  <page path=\"/opennms\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"Password\" />\n" + 
				"  <page path=\"/opennms/j_acegi_security_check\"  port=\"80\" virtual-host=\"demo.opennms.org\" method=\"POST\" failureMatch=\"(?s)Your log-in attempt failed.*Reason: ([^&lt;]*)\" failureMessage=\"Login in Failed: ${1}\" successMatch=\"Log out\">\n" + 
				"    <parameter key=\"j_username\" value=\"demo\"/>\n" + 
				"    <parameter key=\"j_password\" value=\"demo\"/>\n" + 
				"  </page>\n" + 
				"  <page path=\"/opennms/event/index.jsp\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"Event Queries\" />\n" + 
				"  <page path=\"/opennms/j_acegi_logout\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"logged off\" />\n" + 
				"</page-sequence>\n");
		
		
		PollStatus status = m_monitor.poll(getHttpService("demo.opennms.org"), m_params);
		assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
		
	}
	
	public void testVirtualHost() throws Exception {
		m_params.put("page-sequence", "" +
				"<?xml version=\"1.0\"?>" +
				"<page-sequence>\n" + 
				"  <page path=\"/\" port=\"80\" successMatch=\"Zero Bull\" virtual-host=\"www.opennms.com\"/>\n" + 
				"</page-sequence>\n");
		
		
		PollStatus status = m_monitor.poll(getHttpService("www.opennms.com"), m_params);
		assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
		
	}
	
    public void testLoginDynamicCredentials() throws Exception {
        m_params.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence name=\"opennms-login-seq-dynamic-credentials-twice\">\n" + 
                "  <page path=\"/opennms\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"(?s)User:.*&lt;strong&gt;(.*?)&lt;/strong&gt;.*?Password:.*?&lt;strong&gt;(.*?)&lt;/strong&gt;\">\n" +
                "    <session-variable name=\"username\" match-group=\"1\" />\n" +
                "    <session-variable name=\"password\" match-group=\"2\" />\n" +
                "  </page>\n" +
                "  <page path=\"/opennms/j_acegi_security_check\"  port=\"80\" virtual-host=\"demo.opennms.org\" method=\"POST\" failureMatch=\"(?s)Your log-in attempt failed.*Reason: ([^&lt;]*)\" failureMessage=\"Login Failed: ${1}\" successMatch=\"Log out\">\n" + 
                "    <parameter key=\"j_username\" value=\"${username}\"/>\n" + 
                "    <parameter key=\"j_password\" value=\"${password}\"/>\n" + 
                "  </page>\n" + 
                "  <page path=\"/opennms/event/index.jsp\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"Event Queries\" />\n" + 
                "  <page path=\"/opennms/j_acegi_logout\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"logged off\" />\n" +
                "</page-sequence>\n");
        
        
        PollStatus status = m_monitor.poll(getHttpService("demo.opennms.org"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        
    }

    public void testLoginDynamicCredentialsTwice() throws Exception {
        m_params.put("page-sequence", "" +
                "<?xml version=\"1.0\"?>" +
                "<page-sequence name=\"opennms-login-seq-dynamic-credentials\">\n" + 
                "  <page path=\"/opennms\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"(?s)User:.*&lt;strong&gt;(.*?)&lt;/strong&gt;.*?Password:.*?&lt;strong&gt;(.*?)&lt;/strong&gt;\">\n" +
                "    <session-variable name=\"username\" match-group=\"1\" />\n" +
                "    <session-variable name=\"password\" match-group=\"2\" />\n" +
                "  </page>\n" +
                "  <page path=\"/opennms/j_acegi_security_check\"  port=\"80\" virtual-host=\"demo.opennms.org\" method=\"POST\" failureMatch=\"(?s)Your log-in attempt failed.*Reason: ([^&lt;]*)\" failureMessage=\"Login Failed: ${1}\" successMatch=\"Log out\">\n" + 
                "    <parameter key=\"j_username\" value=\"${username}\"/>\n" + 
                "    <parameter key=\"j_password\" value=\"${password}\"/>\n" + 
                "  </page>\n" + 
                "  <page path=\"/opennms/event/index.jsp\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"Event Queries\" />\n" + 
                "  <page path=\"/opennms/j_acegi_logout\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"logged off\" />\n" + 
                "\n" +
                "  <page path=\"/opennms\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"(?s).*(.*?) is the world's first (.*?) grade network management platform.*\">\n" +
                "    <session-variable name=\"username\" match-group=\"1\" />\n" +
                "    <session-variable name=\"password\" match-group=\"2\" />\n" +
                "  </page>\n" +
                "  <page path=\"/opennms/j_acegi_security_check\"  port=\"80\" virtual-host=\"demo.opennms.org\" method=\"POST\" successMatch=\"(?s)Your log-in attempt failed.*Reason: ([^&lt;]*)\" failureMessage=\"Login succeeded but should have failed.\" failureMatch=\"Log out\">\n" + 
                "    <parameter key=\"j_username\" value=\"${username}\"/>\n" + 
                "    <parameter key=\"j_password\" value=\"${password}\"/>\n" + 
                "  </page>\n" + 
                "</page-sequence>\n");
        
        
        PollStatus status = m_monitor.poll(getHttpService("demo.opennms.org"), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
    }
    
    public void testDsNamePerPage() throws Exception {
        m_params.put("page-sequence", "" +
                     "<?xml version=\"1.0\"?>" +
                     "<page-sequence name=\"ds-name-per-page-test\">\n" + 
                     "  <page path=\"/opennms/\" ds-name=\"slash\" port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"(?s)User:.*&lt;strong&gt;(.*?)&lt;/strong&gt;.*?Password:.*?&lt;strong&gt;(.*?)&lt;/strong&gt;\" />\n" +
                     "  <page path=\"/opennms/acegilogin.jsp\" ds-name=\"acegilogin\"  port=\"80\" virtual-host=\"demo.opennms.org\" successMatch=\"(?s)User:.*&lt;strong&gt;(.*?)&lt;/strong&gt;.*?Password:.*?&lt;strong&gt;(.*?)&lt;/strong&gt;\" />\n" +
                     "</page-sequence>\n");
                          
             PollStatus status = m_monitor.poll(getHttpService("demo.opennms.org"), m_params);
             assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
             assertEquals("Expected three DSes", 3, status.getProperties().size());
             assertTrue("Expected a DS called 'slash' but did not find one", status.getProperties().containsKey("slash"));
             assertTrue("Expected a DS called 'acegilogin' but did not find one", status.getProperties().containsKey("acegilogin"));
    }
    
    public void DISABLED_testFreemanBasicShowSearchTwice() throws Exception {
        m_params.put("page-sequence", "" +
                     "<?xml version=\"1.0\"?>\n" +
                     "<page-sequence name=\"freeman-www-basic-show-search\">\n" + 
                     "  <page path=\"/freemanco/\" port=\"80\" method=\"GET\" successMatch=\"(?s)&lt;[Ii][Nn][Pp][Uu][Tt]\\s+.*?id=&quot;simpleShowSearchShowCity&quot;\" />\n" +
                     "  <page path=\"/store/freemanco/simpleShowSearch.jsp\" port=\"80\" method=\"POST\" successMatch=\"(?s)&lt;input\\s+value=&quot;(-?[0-9]+)&quot;\\s+type=&quot;hidden&quot;\\s+name=&quot;_dynSessConf&quot;\">\n" +
                     "    <session-variable name=\"dynSessConf\" match-group=\"1\" />\n" +
                     "    <session-variable name=\"wholeResponse\" match-group=\"0\" />\n" +
                     "  </page>\n" +
                     "  <page path=\"/store/freemanco/showSearch/simpleShowSrchRslt.jsp\" port=\"80\" method=\"POST\"\n" +
                     "        successMatch=\"(?s).*Your\\s+search\\s+for\\s+&lt;strong&gt;.*?Las\\s+Vegas.*?&lt;/strong&gt;.*?returned\\s+&lt;strong&gt;.*?([1-9][0-9]*).*?&lt;/strong&gt;\\s+results?\\..*\">\n" +
                     "    <parameter key=\"/freeman/show/search/ShowStructureSearchHandler.search.x\" value=\"1\" />\n" +
                     "    <parameter key=\"/freeman/show/search/ShowStructureSearchHandler.search\" value=\"Search\" />\n" +
                     "    <parameter key=\"/freeman/show/search/ShowStructureSearchHandler.style\" value=\"simple\" />\n" +
                     "    <parameter key=\"_D:/freeman/show/search/ShowStructureSearchHandler.style\" value=\"\" />\n" +
                     "    <parameter key=\"_D:/freeman/show/search/ShowStructureSearchHandler.search\" value=\"\" />\n" +
                     "    <parameter key=\"/freeman/show/search/ShowStructureSearchHandler.search.y\" value=\"1\" />\n" +
                     "    <parameter key=\"_DARGS\" value=\"/store/freemanco/simpleShowSearch.jsp.simpleShowSearchForm\" />\n" +
                     "    <parameter key=\"/freeman/show/search/ShowStructureSearchHandler.searchRequest.searchEnvironmentName\" value=\"fol_show_search\" />\n" +
                     "    <parameter key=\"_D:/freeman/show/search/ShowStructureSearchHandler.searchRequest.searchEnvironmentName\" value=\"\" />\n" +
                     "    <parameter key=\"_dynSessConf\" value=\"${dynSessConf}\" />\n" +
                     "    <parameter key=\"/freeman/show/constraints/CityStringConstraint.value\" value=\"Las Vegas\" />\n" +
                     "    <parameter key=\"/freeman/show/constraints/ShowNameStringConstraint.value\" value=\"\" />\n" +
                     "    <parameter key=\"_D:/freeman/show/constraints/CityStringConstraint.value\" value=\"\" />\n" +
                     "    <parameter key=\"_D:/freeman/show/constraints/ShowNameStringConstraint.value\" value=\"\" />\n" +
                     "    <parameter key=\"_dyncharset\" value=\"UTF-8\" />\n" +
                     "  </page>\n" +
                     "</page-sequence>\n");
        PollStatus status = m_monitor.poll(getHttpService("www.freemanco.com"), m_params);
        assertTrue("Expected available (first poll) but was "+status+": reason = "+status.getReason(), status.isAvailable());
        
        // Do it again
        status = m_monitor.poll(getHttpService("www.freemanco.com"), m_params);
        assertTrue("Expected available (second poll) but was "+status+": reason = "+status.getReason(), status.isAvailable());
    }
	
}
