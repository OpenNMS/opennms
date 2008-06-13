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
// Modificiations:
//
// 2008 Feb 10: Eliminate warnings. - dj@opennms.org
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
package org.opennms.netmgt.xmlrpcd;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;

import org.apache.xmlrpc.WebServer;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.opennms.netmgt.config.XmlrpcdConfigFactory;
import org.opennms.netmgt.config.xmlrpcd.ExternalServers;
import org.opennms.netmgt.config.xmlrpcd.Subscription;
import org.opennms.spring.xmlrpc.XmlRpcProxyFactoryBean;
import org.opennms.spring.xmlrpc.XmlRpcServiceExporter;
import org.opennms.spring.xmlrpc.XmlRpcWebServerFactoryBean;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;

/**
 * Represents a XmlRpcTest 
 *
 * @author brozow
 */
public class XmlRpcTest extends MockObjectTestCase {


    static private WebServer m_webServer;
    private Mock m_mockProvisioner;
    private Provisioner m_proxy;
    private XmlRpcServiceExporter m_exporter;

    protected void setUp() throws Exception {
        MockLogAppender.setupLogging();
        

        m_mockProvisioner = mock(Provisioner.class);
        Provisioner bean = (Provisioner)m_mockProvisioner.proxy();
        
        m_proxy = createRemoteProxy(bean);

    }
    
    private Provisioner createRemoteProxy(Provisioner bean) throws Exception {
        setUpWebServer();
        
        m_exporter = new XmlRpcServiceExporter();
        m_exporter.setServiceInterface(Provisioner.class);
        m_exporter.setService(bean);
        m_exporter.setWebServer(m_webServer);
        m_exporter.afterPropertiesSet();
        
        Thread.sleep(1000);
        
        return createRemoteProxy("http://localhost:9192/RPC2");
    }

    private Provisioner createRemoteProxy(String serverUrl) throws Exception {
        XmlRpcProxyFactoryBean pfb = new XmlRpcProxyFactoryBean();
        pfb.setServiceInterface(Provisioner.class);
        pfb.setServiceUrl(serverUrl);
        pfb.afterPropertiesSet();
        return (Provisioner) pfb.getObject();
    }

    private void setUpWebServer() throws Exception {
        if (m_webServer == null) {
            //XmlRpc.debug = true;
            XmlRpcWebServerFactoryBean wsf = new XmlRpcWebServerFactoryBean();
            wsf.setPort(9192);
            wsf.setSecure(false);
            wsf.afterPropertiesSet();
            m_webServer = (WebServer)wsf.getObject();
            Thread.sleep(1000);
        }
    }
    
    protected void tearDown() throws Exception {
        if (m_exporter != null)
            m_exporter.destroy();
    }

    public void testXmlRpcAddServiceICMP() throws Throwable {
        m_mockProvisioner.expects(once())
        .method("addServiceICMP")
        .with(new Constraint[]{ eq("RS-ICMP-1"), eq(3), eq(1000), eq(300000), eq(30000), eq(300000) })
        .will(returnValue(true));
        
        boolean retVal = m_proxy.addServiceICMP("RS-ICMP-1", 3, 1000, 300000, 30000, 300000);
        
        assertTrue(retVal);
    }

    public void testXmlRpcAddServiceICMPIllegalArg() throws Throwable {
        String msg = "retries must be greater than or equals to zero";
        m_mockProvisioner.expects(once())
        .method("addServiceICMP")
        .with(new Constraint[]{ eq("RS-ICMP-1"), eq(-1), eq(1000), eq(300000), eq(30000), eq(300000) })
        .will(throwException(new IllegalArgumentException(msg)));
        
        try {
            m_proxy.addServiceICMP("RS-ICMP-1", -1, 1000, 300000, 30000, 300000);
            fail("Expected exception to be thrown");
        } catch(IllegalArgumentException e) {
            assertEquals(msg, e.getMessage());
        } 
        
    }

    public void testAddServiceDNS() {
        m_mockProvisioner.expects(once())
        .method("addServiceDNS")
        .with(new Constraint[]{ eq("RS-DNS-1"), eq(3), eq(1000), eq(300000), eq(30000), eq(300000), eq(1234), eq("www.opennms.org") })
        .will(returnValue(true));
        
        boolean retVal = m_proxy.addServiceDNS("RS-DNS-1", 3, 1000, 300000, 30000, 300000, 1234, "www.opennms.org");
        
        
        assertTrue(retVal);
    }
    
    public void testAddServiceTCP() {
        m_mockProvisioner.expects(once())
        .method("addServiceTCP")
        .with(new Constraint[]{ eq("RS-TCP-1"), eq(3), eq(1000), eq(300000), eq(30000), eq(300000), eq(1234), eq("HELO")})
        .will(returnValue(true));
        
        boolean retVal = m_proxy.addServiceTCP("RS-TCP-1", 3, 1000, 300000, 30000, 300000, 1234, "HELO");
        
        assertTrue(retVal);
    }
    
    public void testAddServiceHTTP() throws MalformedURLException {
        String url = "http://www.opennms.org";
        m_mockProvisioner.expects(once())
        .method("addServiceHTTP")
        .with(new Constraint[]{ eq("RS-HTTP-1"), eq(3), eq(1000), eq(300000), eq(30000), eq(300000), eq(""), eq(80), eq("200"), eq("Login"), eq(url), eq("user"), eq("pw"), eq("OpenNMS Monitor") })
        .will(returnValue(true));
        
        boolean retVal = m_proxy.addServiceHTTP("RS-HTTP-1", 3, 1000, 300000, 30000, 300000, "", 80, "200", "Login", url, "user", "pw", "OpenNMS Monitor");
        
        assertTrue(retVal);
    }
    
    public void testAddServiceHTTPInvalidURL() throws MalformedURLException {
        String url = "htt://www.opennms.org";
        MalformedURLException urlException = getMalformedUrlException(url);
        m_mockProvisioner.expects(once())
        .method("addServiceHTTP")
        .with(new Constraint[]{ eq("RS-HTTP-1"), eq(3), eq(1000), eq(300000), eq(30000), eq(300000), eq(""), eq(80), eq("200"), eq("Login"), eq(url), eq("user"), eq("pw"), eq("OpenNMS Monitor") })
        .will(throwException(urlException));
        
        try {
            m_proxy.addServiceHTTP("RS-HTTP-1", 3, 1000, 300000, 30000, 300000, "", 80, "200", "Login", url, "user", "pw", "OpenNMS Monitor");
            fail("Expected exception");
        } catch (MalformedURLException e) {
            assertEquals(urlException.getMessage(), e.getMessage());
        }
    }

    private MalformedURLException getMalformedUrlException(String url) {
        MalformedURLException urlException = null;
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            urlException = e;
        }
        return urlException;
    }
    
    public void testAddServiceHTTPS() throws MalformedURLException {
        String url = "https://www.opennms.org";
        m_mockProvisioner.expects(once())
        .method("addServiceHTTPS")
        .with(new Constraint[]{ eq("RS-HTTPS-1"), eq(3), eq(1000), eq(300000), eq(30000), eq(300000), eq(""), eq(80), eq("200"), eq("Login"), eq(url), eq("user"), eq("pw"), eq("OpenNMS Monitor") })
        .will(returnValue(true));
        
        boolean retVal = m_proxy.addServiceHTTPS("RS-HTTPS-1", 3, 1000, 300000, 30000, 300000, "", 80, "200", "Login", url, "user", "pw", "OpenNMS Monitor");
        
        assertTrue(retVal);
    }
    
    public void testAddServiceDatabase() throws MalformedURLException {
        String url = "jdbc://localhost/database";
        m_mockProvisioner.expects(once())
        .method("addServiceDatabase")
        .with(new Constraint[]{ eq("RS-POSTGRES-1"), eq(3), eq(1000), eq(300000), eq(30000), eq(300000), eq("sa"), eq(""), eq("org.postgresql.Driver"), eq(url)})
        .will(returnValue(true));
        
        boolean retVal = m_proxy.addServiceDatabase("RS-POSTGRES-1", 3, 1000, 300000, 30000, 300000, "sa", "", "org.postgresql.Driver", url);
        
        assertTrue(retVal);
    }
    
    public void testReadOldStyleConfiguration() throws MarshalException, ValidationException, IOException {
    	File cfgIn = ConfigurationTestUtils.getFileForResource(this, "/etc/xmlrpcd-configuration-old.xml");
        XmlrpcdConfigFactory.reload();
    	XmlrpcdConfigFactory.init(cfgIn);
    	XmlrpcdConfigFactory f = XmlrpcdConfigFactory.getInstance();
    	assertNotNull("Xmlrpcd instance", f);

    	// should have exactly one external server entry, and one subscription entry
    	assertEquals("one external server", 1, f.getExternalServerCollection().size());
    	assertEquals("one subscription", 1, f.getSubscriptionCollection().size());

    	// check the subscription references
    	ExternalServers e = f.getExternalServerCollection().iterator().next();
    	assertEquals("one subscription", 1, e.getServerSubscriptionCount());
    	assertTrue("subscription should be autogenerated", e.getServerSubscription(0).startsWith("legacyServerSubscription-"));
    	
    	// subscription should match the one from the first external server
    	Subscription s = f.getSubscriptionCollection().iterator().next();
    	assertEquals("subscription name matches external-servers entry", e.getServerSubscription(0), s.getName());

    	// subscription should have 6 UEIs
    	assertEquals("subscription should have 6 UEIs", 6, s.getSubscribedEventCount());
    }

    public void testReadNewStyleConfiguration() throws MarshalException, ValidationException, IOException {
    	File cfgIn = ConfigurationTestUtils.getFileForResource(this, "/etc/xmlrpcd-configuration-new.xml");
        XmlrpcdConfigFactory.reload();
    	XmlrpcdConfigFactory.init(cfgIn);
    	XmlrpcdConfigFactory f = XmlrpcdConfigFactory.getInstance();
    	assertNotNull("Xmlrpcd instance", f);
    	ExternalServers e = null;
    	Subscription s = null;

    	// check entries
    	assertEquals("number of external-server entries", 2, f.getExternalServerCollection().size());
    	assertEquals("number of subscriptions", 3, f.getSubscriptionCollection().size());

    	Iterator<ExternalServers> es = f.getExternalServerCollection().iterator();

    	// first external-server should have 2 subscriptions
    	e = es.next();
    	assertEquals("two subscriptions", 2, e.getServerSubscriptionCount());
    	assertTrue("first subscription should be serviceEvents", e.getServerSubscription(0).equals("serviceEvents"));

    	// second one should be automatically generated
    	e = es.next();
    	assertEquals("should have one serverSubscription", 1, e.getServerSubscriptionCount());
    	assertTrue("should be autogenerated", e.getServerSubscription(0).startsWith("legacyServerSubscription-"));

    	Iterator<Subscription> is = f.getSubscriptionCollection().iterator();
    	
    	// first entry, serviceEvents
    	s = is.next();
    	assertEquals("first entry is serviceEvents", "serviceEvents", s.getName());
    	assertEquals("serviceEvents has 2 entries", 2, s.getSubscribedEventCount());
    	
    	// second entry, otherEvents
    	s = is.next();
    	assertEquals("second entry is otherEvents", "otherEvents", s.getName());
    	assertEquals("otherEvents has 5 entries", 7, s.getSubscribedEventCount());
    	
    	// third entry, generated
    	s = is.next();
    	assertEquals("third entry is generated", e.getServerSubscription(0), s.getName());
    	assertEquals("autogenerated entries have 9 UEIs", 9, s.getSubscribedEventCount());

    }

}
