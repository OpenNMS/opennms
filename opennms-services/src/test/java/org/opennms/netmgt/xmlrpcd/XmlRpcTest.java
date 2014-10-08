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

package org.opennms.netmgt.xmlrpcd;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.WebServer;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.spring.xmlrpc.XmlRpcProxyFactoryBean;
import org.opennms.spring.xmlrpc.XmlRpcServiceExporter;
import org.opennms.spring.xmlrpc.XmlRpcWebServerFactoryBean;
import org.opennms.test.mock.EasyMockUtils;

/**
 * Represents a XmlRpcTest 
 *
 * @author brozow
 */
public class XmlRpcTest  {


    static private WebServer m_webServer;
    private Provisioner m_provisioner;
    private Provisioner m_proxy;
    private XmlRpcServiceExporter m_exporter;

    private EasyMockUtils m_mocks = new EasyMockUtils();
    
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
        
        m_provisioner = m_mocks.createMock(Provisioner.class);
        m_proxy = createRemoteProxy(m_provisioner);
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
        XmlRpcProxyFactoryBean<Provisioner> pfb = new XmlRpcProxyFactoryBean<Provisioner>();
        pfb.setServiceInterface(Provisioner.class);
        pfb.setServiceUrl(serverUrl);
        pfb.afterPropertiesSet();
        return pfb.getObject();
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
    
    @After
    public void tearDown() throws Exception {
        if (m_exporter != null) {
            m_exporter.destroy();
        }
    }

    @Test
    public void testXmlRpcAddServiceICMP() throws Throwable {
        EasyMock.expect(m_provisioner.addServiceICMP("RS-ICMP-1", 3, 1000, 300000, 30000, 300000)).andReturn(true);
        m_mocks.replayAll();
        boolean retVal = m_proxy.addServiceICMP("RS-ICMP-1", 3, 1000, 300000, 30000, 300000);
        assertTrue(retVal);
        m_mocks.verifyAll();
    }

    @Test(expected=IllegalArgumentException.class)
    public void testXmlRpcAddServiceICMPIllegalArg() throws Throwable {
        String msg = "retries must be greater than or equals to zero";
        EasyMock.expect(m_provisioner.addServiceICMP("RS-ICMP-1", -1, 1000, 300000, 30000, 300000)).andThrow(new IllegalArgumentException(msg));
        m_mocks.replayAll();
        m_proxy.addServiceICMP("RS-ICMP-1", -1, 1000, 300000, 30000, 300000);
    }

    @Test
    public void testAddServiceDNS() {
        EasyMock.expect(m_provisioner.addServiceDNS("RS-DNS-1", 3, 1000, 300000, 30000, 300000, 1234, "www.opennms.org")).andReturn(true);
        m_mocks.replayAll();
        boolean retVal = m_proxy.addServiceDNS("RS-DNS-1", 3, 1000, 300000, 30000, 300000, 1234, "www.opennms.org");
        assertTrue(retVal);
        m_mocks.verifyAll();
    }

    @Test
    public void testAddServiceTCP() {
        EasyMock.expect(m_provisioner.addServiceTCP("RS-TCP-1", 3, 1000, 300000, 30000, 300000, 1234, "HELO")).andReturn(true);
        m_mocks.replayAll();
        boolean retVal = m_proxy.addServiceTCP("RS-TCP-1", 3, 1000, 300000, 30000, 300000, 1234, "HELO");
        assertTrue(retVal);
        m_mocks.verifyAll();
    }

    @Test
    public void testAddServiceHTTP() throws MalformedURLException {
        String url = "http://www.opennms.org";
        EasyMock.expect(m_provisioner.addServiceHTTP("RS-HTTP-1", 3, 1000, 300000, 30000, 300000, "", 80, "200", "Login", url, "user", "pw", "OpenNMS Monitor")).andReturn(true);
        m_mocks.replayAll();
        boolean retVal = m_proxy.addServiceHTTP("RS-HTTP-1", 3, 1000, 300000, 30000, 300000, "", 80, "200", "Login", url, "user", "pw", "OpenNMS Monitor");
        assertTrue(retVal);
        m_mocks.verifyAll();
    }

    @Test(expected=MalformedURLException.class)
    public void testAddServiceHTTPInvalidURL() throws MalformedURLException {
        String url = "htt://www.opennms.org";
        EasyMock.expect(m_provisioner.addServiceHTTP("RS-HTTP-1", 3, 1000, 300000, 30000, 300000, "", 80, "200", "Login", url, "user", "pw", "OpenNMS Monitor")).andThrow(getMalformedUrlException(url));
        m_mocks.replayAll();
        m_proxy.addServiceHTTP("RS-HTTP-1", 3, 1000, 300000, 30000, 300000, "", 80, "200", "Login", url, "user", "pw", "OpenNMS Monitor");
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
    
    @Test
    public void testAddServiceHTTPS() throws MalformedURLException {
        String url = "https://www.opennms.org";
        EasyMock.expect(m_provisioner.addServiceHTTPS("RS-HTTPS-1", 3, 1000, 300000, 30000, 300000, "", 80, "200", "Login", url, "user", "pw", "OpenNMS Monitor")).andReturn(true);
        m_mocks.replayAll();
        boolean retVal = m_proxy.addServiceHTTPS("RS-HTTPS-1", 3, 1000, 300000, 30000, 300000, "", 80, "200", "Login", url, "user", "pw", "OpenNMS Monitor");
        assertTrue(retVal);
        m_mocks.verifyAll();
    }

    @Test
    public void testAddServiceDatabase() throws MalformedURLException {
        String url = "jdbc://localhost/database";
        EasyMock.expect(m_provisioner.addServiceDatabase("RS-POSTGRES-1", 3, 1000, 300000, 30000, 300000, "sa", "", "org.postgresql.Driver", url)).andReturn(true);
        m_mocks.replayAll();
        boolean retVal = m_proxy.addServiceDatabase("RS-POSTGRES-1", 3, 1000, 300000, 30000, 300000, "sa", "", "org.postgresql.Driver", url);
        assertTrue(retVal);
        m_mocks.verifyAll();
    }
}
