/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opennms.spring.xmlrpc;

import java.io.IOException;

import junit.framework.TestCase;

import org.apache.xmlrpc.WebServer;
import org.opennms.netmgt.mock.MockLogAppender;
import org.opennms.spring.xmlrpc.XmlRpcProxyFactoryBean;
import org.opennms.spring.xmlrpc.XmlRpcServiceExporter;
import org.opennms.spring.xmlrpc.XmlRpcWebServerFactoryBean;
import org.springframework.remoting.RemoteAccessException;

/**
 */
public class XmlRpcTest extends TestCase {
    
    private WebServer m_webServer;

    protected void setUp() throws Exception {
        
        MockLogAppender.setupLogging();
        
        XmlRpcWebServerFactoryBean wsf = new XmlRpcWebServerFactoryBean();
        wsf.setPort(9192);
        wsf.setSecure(false);
        wsf.afterPropertiesSet();
        
        m_webServer = (WebServer)wsf.getObject();
    }
    
    protected void tearDown() {
        m_webServer.shutdown();
        
    }

	public void testXmlRpcProxyFactoryBeanAndServiceExporter() throws Throwable {
		TestBean target = new TestBean("myname", 99);
        
        
		final XmlRpcServiceExporter exporter = new XmlRpcServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
        exporter.setWebServer(m_webServer);
		exporter.afterPropertiesSet();

		XmlRpcProxyFactoryBean pfb = new XmlRpcProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://localhost:9192/RPC2");
		pfb.afterPropertiesSet();

        ITestBean proxy = (ITestBean) pfb.getObject();
		assertEquals("myname", proxy.getName());
		assertEquals(99, proxy.getAge());
		proxy.setAge(50);
		assertEquals(50, proxy.getAge());

	}

    public void _testXmlRpcProxyFactoryBeanAndServiceExporterWithHttps() throws Throwable {
        TestBean target = new TestBean("myname", 99);
        
        
        final XmlRpcServiceExporter exporter = new XmlRpcServiceExporter();
        exporter.setServiceInterface(ITestBean.class);
        exporter.setService(target);
        exporter.setWebServer(m_webServer);
        exporter.afterPropertiesSet();

        XmlRpcProxyFactoryBean pfb = new XmlRpcProxyFactoryBean();
        pfb.setServiceInterface(ITestBean.class);
        pfb.setServiceUrl("https://localhost:9192/RPC2");
        pfb.afterPropertiesSet();

        ITestBean proxy = (ITestBean) pfb.getObject();
        assertEquals("myname", proxy.getName());
        assertEquals(99, proxy.getAge());
        proxy.setAge(50);
        assertEquals(50, proxy.getAge());

    }

	public void testXmlRpcProxyFactoryBeanAndServiceExporterWithIOException() throws Exception {
		TestBean target = new TestBean("myname", 99);

		final XmlRpcServiceExporter exporter = new XmlRpcServiceExporter();
		exporter.setServiceInterface(ITestBean.class);
		exporter.setService(target);
        exporter.setWebServer(m_webServer);
		exporter.afterPropertiesSet();

		XmlRpcProxyFactoryBean pfb = new XmlRpcProxyFactoryBean();
		pfb.setServiceInterface(ITestBean.class);
		pfb.setServiceUrl("http://myurl"); // this is wrong so we throw an exception
		pfb.afterPropertiesSet();

        ITestBean proxy = (ITestBean) pfb.getObject();
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
        
        public String getName() {
            return this.name;
        }
        
        public int getAge() {
            return this.age;
        }
        
        public void setAge(int age) {
            this.age = age;
        }
    }



}
