/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.spring.xmlrpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.xmlrpc.WebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.springframework.remoting.RemoteAccessException;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 */
public class XmlRpcTest {
    
    private WebServer m_webServer;

    @Before
    public void setUp() throws Exception {
        
        MockLogAppender.setupLogging();
        
        XmlRpcWebServerFactoryBean wsf = new XmlRpcWebServerFactoryBean();
        wsf.setPort(9192);
        wsf.setSecure(false);
        wsf.afterPropertiesSet();
        
        m_webServer = (WebServer)wsf.getObject();
    }
    
    @After
    public void tearDown() {
        m_webServer.shutdown();
        
    }

    @Test
	public void testXmlRpcProxyFactoryBeanAndServiceExporter() throws Throwable {
		TestBean target = new TestBean("myname", 99);
        
        
		final XmlRpcServiceExporter exporter = new XmlRpcServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
        exporter.setWebServer(m_webServer);
		exporter.afterPropertiesSet();

		XmlRpcProxyFactoryBean<ITestBean> pfb = new XmlRpcProxyFactoryBean<ITestBean>();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://localhost:9192/RPC2");
		pfb.afterPropertiesSet();

        ITestBean proxy = pfb.getObject();
		assertEquals("myname", proxy.getName());
		assertEquals(99, proxy.getAge());
		proxy.setAge(50);
		assertEquals(50, proxy.getAge());

	}

    @Test
    @Ignore("We're not set up for HTTPS for these tests.")
    public void testXmlRpcProxyFactoryBeanAndServiceExporterWithHttps() throws Throwable {
        TestBean target = new TestBean("myname", 99);
        
        
        final XmlRpcServiceExporter exporter = new XmlRpcServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setWebServer(m_webServer);
        exporter.afterPropertiesSet();

        XmlRpcProxyFactoryBean<ITestBean> pfb = new XmlRpcProxyFactoryBean<ITestBean>();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("https://localhost:9192/RPC2");
        pfb.afterPropertiesSet();

        ITestBean proxy = pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
        proxy.setAge(50);
        assertEquals(50, proxy.getAge());

    }

    @Test
    @Ignore("If you're using OpenDNS, myurl gives an IP address")
	public void testXmlRpcProxyFactoryBeanAndServiceExporterWithIOException() throws Exception {
		TestBean target = new TestBean("myname", 99);

		final XmlRpcServiceExporter exporter = new XmlRpcServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
        exporter.setWebServer(m_webServer);
		exporter.afterPropertiesSet();

		XmlRpcProxyFactoryBean<ITestBean> pfb = new XmlRpcProxyFactoryBean<ITestBean>();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://127.0.0.1:9191/RPC2"); // this is wrong (we hope) so we throw an exception
		pfb.afterPropertiesSet();

        ITestBean proxy = pfb.getObject();
		try {
			proxy.setAge(50);
			fail("Should have thrown RemoteAccessException");
		}
		catch (RemoteAccessException ex) {
			// expected
			assertTrue(ex.getCause() instanceof IOException);
		}
	}
    
    public static interface ITestBean {
        public String getName();
        public int getAge();
        public void setAge(int age);
    }
    
     static class TestBean implements ITestBean {
        private String name;
        private int age;

        TestBean(String name, int age) {
            this.name = name;
            this.age = age;
        }
        
        @Override
        public String getName() {
            return this.name;
        }
        
        @Override
        public int getAge() {
            return this.age;
        }
        
        @Override
        public void setAge(int age) {
            this.age = age;
        }
    }



}
