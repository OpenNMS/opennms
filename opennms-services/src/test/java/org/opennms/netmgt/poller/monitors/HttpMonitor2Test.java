//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.cookie.CookieSpec;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.opennms.netmgt.mock.MockMonitoredService;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:brozow@opennms.org">Matt Brozowski</a>
 *
 */
public class HttpMonitor2Test extends TestCase {
	
	HttpMonitor2 m_monitor;
	Map<String, String> m_params;
	

    @Override
	protected void setUp() throws Exception {
		super.setUp();
		
    	m_monitor = new HttpMonitor2();
    	m_monitor.initialize(Collections.EMPTY_MAP);
    	
		m_params = new HashMap<String, String>();
		m_params.put("timeout", "1000");
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
		super.tearDown();
	}
    
    // FIXME: test doesn't pass yet
    public void XtestSimple() throws Exception {
		m_params.put("page-sequence", "" +
				"<?xml version=\"1.0\"?>" +
				"<page-sequence>\n" + 
				"  <page path=\"/\" port=\"80\" matches=\"I'm Feeling Lucky\" />\n" + 
				"</page-sequence>\n");
		
		
        PollStatus googleStatus = m_monitor.poll(getHttpService("www.google.com"), m_params);
        assertTrue("Expected available but was "+googleStatus+": reason = "+googleStatus.getReason(), googleStatus.isAvailable());
        
		PollStatus notLikely = m_monitor.poll(getHttpService("bogus", "1.1.1.1"), m_params);
		assertTrue(notLikely.isUnavailable());

        PollStatus yahooStatus = m_monitor.poll(getHttpService("www.yahoo.com"), m_params);
        assertTrue(yahooStatus.isUnavailable());
        
    }

    // FIXME: test doesn't pass yet
	public void XtestLogin() throws Exception {
    	
		m_params.put("page-sequence", "" +
				"<?xml version=\"1.0\"?>" +
				"<page-sequence name=\"opennms-login-seq\" force-logout=\"true\" >\n" + 
				"  <page path=\"/opennms\" port=\"8080\" matches=\"Password\" />\n" + 
				"  <page path=\"/opennms/j_acegi_security_check\"  port=\"8080\" method=\"POST\" matches=\"Log out\">\n" + 
				"    <parameter key=\"j_username\" value=\"demo\"/>\n" + 
				"    <parameter key=\"j_password\" value=\"demo\"/>\n" + 
				"  </page>\n" + 
				"  <page path=\"/opennms/event/index.jsp\" port=\"8080\" matches=\"Event Queries\" />\n" + 
				"  <page path=\"/opennms/j_acegi_logout\" port=\"8080\" matches=\"logged off\" page-type=\"logout\" />\n" + 
				"</page-sequence>\n");
		
		
		PollStatus status = m_monitor.poll(getHttpService("demo.opennms.com"), m_params);
		
		assertTrue(status.isAvailable());
		
	}
	
	public void testActualLogin() throws Exception {
    	
        HttpClientParams params = new HttpClientParams();
        
        //version
        params.setVersion(new HttpVersion(1, 1));
        
        //timeout
        params.setSoTimeout(3000);
        
        // FIXME: what happens on timeout? how do we know that happened?
        
        
        HttpClient client = new HttpClient(params);
        
        String scheme = "http";
        String userinfo = "demo:demo";
        String host = "demo.opennms.com";
        int port = 8080;
        String path = "/opennms";
        String query = null;
        String fragment = null;
        
        URI uri = new URI(scheme, userinfo, host, port, path, query, fragment);
        
        client.getHostConfiguration().setHost(uri);
        client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        
        GetMethod authGet = new GetMethod(path);
        authGet.setFollowRedirects(true);
        
        client.executeMethod(authGet);
        
        assertEquals("Response is 200", 200, authGet.getStatusCode());
        assertTrue("Redirected to login page", authGet.getResponseBodyAsString().indexOf("j_acegi_security_check") > 0);
        
        CookieSpec spec = CookiePolicy.getDefaultSpec();
        boolean secure = false;
        Cookie[] cookies = spec.match(host, port, path, secure , client.getState().getCookies());

        assertTrue("Got cookies", cookies.length > 0);
        authGet.releaseConnection();
        
        PostMethod post = new PostMethod(path+"/j_acegi_security_check");
        
        
        NameValuePair action = new NameValuePair("action", "j_acegi_security_check");
        NameValuePair url = new NameValuePair("ulr", "j_acegi_security_check");
        NameValuePair user = new NameValuePair("j_username", "demo");
        NameValuePair password = new NameValuePair("j_password", "demo");
        
        post.setRequestBody(new NameValuePair[] {action, url, user, password});
        client.executeMethod(post);
        
        System.err.println("Login form post: "+post.getStatusLine());
        
        Cookie[] logonCookies = spec.match(host, port, path, secure, client.getState().getCookies());
        assertTrue("Got logon cookies", logonCookies.length > 0);
        
        int statusCode = post.getStatusCode();
        
        Header header = post.getResponseHeader("Location");
        if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY ||
                statusCode == HttpStatus.SC_MOVED_PERMANENTLY ||
                statusCode == HttpStatus.SC_SEE_OTHER || 
                statusCode == HttpStatus.SC_TEMPORARY_REDIRECT) {
            String newUri = header.getValue();
            GetMethod redirect = new GetMethod(newUri);
            client.executeMethod(redirect);
            redirect.releaseConnection();
        }

        post.releaseConnection();
    }
}
