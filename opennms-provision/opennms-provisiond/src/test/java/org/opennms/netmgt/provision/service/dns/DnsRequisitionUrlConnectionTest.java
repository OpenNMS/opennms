/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.dns;

import junit.framework.Assert;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.dns.JUnitDNSServerExecutionListener;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class tests the new "dns" protocol handling created for the Provisioner.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/empty-context.xml"})
@TestExecutionListeners(listeners={
                        JUnitDNSServerExecutionListener.class
})
public class DnsRequisitionUrlConnectionTest {

    private static final String TEST_URL = "dns://localhost:9153/example.com";

    @BeforeClass
    static public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Before
    public void registerFactory() {
        GenericURLFactory.initialize();
    }
    
    @Test
    public void dwoValidateMultipathUrl() {
        
        MalformedURLException e = null;
        
        try {
            DnsRequisitionUrlConnection c = new DnsRequisitionUrlConnection(new URL("dns://localhost/opennms"));
            Assert.assertEquals("opennms", c.getZone());
        } catch (MalformedURLException e1) {
            e = e1;
        }
        Assert.assertNull(e);
        
        try {
            DnsRequisitionUrlConnection c = new DnsRequisitionUrlConnection(new URL("dns://localhost/opennms/raleigh"));
            Assert.assertEquals("opennms", c.getZone());
            Assert.assertEquals("raleigh", c.getForeignSource());
        } catch (MalformedURLException e1) {
            e = e1;
        }
        Assert.assertNull(e);
        
        
        try {
            new DnsRequisitionUrlConnection(new URL("dns://localhost/org/opennms/raleigh"));
        } catch (MalformedURLException e1) {
            e = e1;
        }
        Assert.assertNotNull(e);
        Assert.assertEquals("The specified DNS URL contains invalid path: dns://localhost/org/opennms/raleigh", e.getLocalizedMessage());
        
    }

    @Test
    public void dwoParseZone() {
        MalformedURLException e = null;
        
        URL url = null;
        try {
            url = new URL("dns://localhost/opennms");
        } catch (MalformedURLException e2) {
            e = e2;
        }
        Assert.assertNull(e);
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseZone(url));
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseForeignSource(url));
        
        
        try {
            url = new URL("dns://localhost/opennms/raleigh");
        } catch (MalformedURLException e2) {
            e = e2;
        }
        Assert.assertNull(e);
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseZone(url));
        Assert.assertEquals("raleigh", DnsRequisitionUrlConnection.parseForeignSource(url));
        
        
        try {
            url = new URL("dns://localhost/opennms/?expression=abc[123]");
        } catch (MalformedURLException e2) {
            e = e2;
        }
        Assert.assertNull(e);
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseZone(url));
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseForeignSource(url));

        
        try {
            url = new URL("dns://localhost/opennms/raleigh/?expression=abc[123]");
        } catch (MalformedURLException e2) {
            e = e2;
        }
        Assert.assertNull(e);
        Assert.assertEquals("opennms", DnsRequisitionUrlConnection.parseZone(url));
        Assert.assertEquals("raleigh", DnsRequisitionUrlConnection.parseForeignSource(url));
    }
    
    
    @Test
    public void dwoValidateInvalidQueryParmInUrl() {
        
        MalformedURLException e = null;
        
        try {
            new DnsRequisitionUrlConnection(new URL("dns://localhost/opennms/?expression=abc[123]"));
        } catch (MalformedURLException e1) {
            e = e1;
        }
        Assert.assertNull(e);
        
        
        try {
            new DnsRequisitionUrlConnection(new URL("dns://localhost/opennms/raleigh/?expression=abc[123]"));
        } catch (MalformedURLException e1) {
            e = e1;
        }
        Assert.assertNull(e);
        
        
        try {
            new DnsRequisitionUrlConnection(new URL("dns://localhost/opennms/?string=abc[123]"));
        } catch (MalformedURLException e1) {
            e = e1;
        }
        Assert.assertNotNull(e);
        Assert.assertEquals("The specified DNS URL contains an invalid query string: dns://localhost/opennms/?string=abc[123]", e.getLocalizedMessage());
        
    }
    
    @Test
    public void dwoParseUrlForMatchingExpression() throws MalformedURLException {
        URL url = new URL("dns://localhost/localhost/?expression=abc[0-9]");
        
        String expression = url.getPath();
        Assert.assertNotNull(expression);
        Assert.assertEquals("/localhost/", expression);
        Assert.assertEquals("expression=abc[0-9]", url.getQuery());
        
        String urlExpression = DnsRequisitionUrlConnection.determineExpressionFromUrl(url);
        Assert.assertEquals("abc[0-9]", urlExpression);
        
    }
    
    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="www", address="72.14.204.99")
            })
    })
    public void dwoUrlAsResource() throws IOException, MarshalException, ValidationException, JAXBException {
        Resource resource = new UrlResource(TEST_URL);
        
        Assert.assertEquals(TEST_URL, resource.getURL().toString());
        
        Requisition req = null;
        Assert.assertNotNull(resource);
        InputStream resourceStream = resource.getInputStream();
        JAXBContext context = JAXBContext.newInstance(Requisition.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(null);
        req = (Requisition) um.unmarshal(resourceStream);
        
        Assert.assertEquals("should have 2 A records: 1 for example.com, and 1 for www.example.com", 2, req.getNodeCount());
        resourceStream.close();
    }
    
    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="www", address="72.14.204.99"),
                    @DNSEntry(hostname="monkey", address="72.14.204.99")
            })
    })
    public void dwoUrlAsResourceUsingMatchingExpression() throws IOException, MarshalException, ValidationException, JAXBException {
        String urlString = "dns://localhost:9153/example.com/?expression=[Ww]ww.*";
        Resource resource = new UrlResource(urlString);

        Assert.assertEquals(urlString, resource.getURL().toString());
        
        Requisition req = null;
        Assert.assertNotNull(resource);
        InputStream resourceStream = resource.getInputStream();
        JAXBContext context = JAXBContext.newInstance(Requisition.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(null);
        req = (Requisition) um.unmarshal(resourceStream);
        
        Assert.assertEquals(1, req.getNodeCount());
        resourceStream.close();
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="www", address="72.14.204.99"),
                    @DNSEntry(hostname="monkey", address="72.14.204.99")
            })
    })
    public void dwoUrlAsResourceUsingNonMatchingExpression() throws IOException, MarshalException, ValidationException, JAXBException {
        String urlString = "dns://localhost:9153/example.com/?expression=Local.*";
        Resource resource = new UrlResource(urlString);
        
        Assert.assertEquals(urlString, resource.getURL().toString());
        
        Requisition req = null;
        Assert.assertNotNull(resource);
        InputStream resourceStream = resource.getInputStream();
        JAXBContext context = JAXBContext.newInstance(Requisition.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(null);
        req = (Requisition) um.unmarshal(resourceStream);
        
        Assert.assertEquals(0, req.getNodeCount());
        resourceStream.close();
    }
    
    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="www", address="72.14.204.99")
            })
    })
    public void dwoUrlAsResourceUsingComplexMatchingExpression() throws IOException, MarshalException, ValidationException, JAXBException {
        String urlString = "dns://localhost:9153/example.com/?expression=(%3Fi)^WWW.EXAM.*";
        Resource resource = new UrlResource(urlString);
        
        Assert.assertEquals(urlString, resource.getURL().toString());
        
        Requisition req = null;
        Assert.assertNotNull(resource);
        InputStream resourceStream = resource.getInputStream();
        JAXBContext context = JAXBContext.newInstance(Requisition.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(null);
        req = (Requisition) um.unmarshal(resourceStream);
        
        Assert.assertEquals(1, req.getNodeCount());
        resourceStream.close();
    }
    
    @Test
    public void dwoDnsRequisitionUrlConnection() throws MalformedURLException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        Assert.assertNotNull(c);
    }

    /**
     * Just test that connection doesn't get implementation without fixing this test
     * @throws IOException
     */
    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="www", address="72.14.204.99")
            })
    })
    public void dwoConnect() throws IOException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        c.connect();
    }

    @Test
    @JUnitDNSServer(port=9153, zones={
            @DNSZone(name="example.com", entries={
                    @DNSEntry(hostname="www", address="72.14.204.99")
            })
    })
    public void dwoGetInputStream() throws IOException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        InputStream s = c.getInputStream();
        Assert.assertNotNull(s);
        s.close();
    }

    @Test
    public void dwoGetURL() throws MalformedURLException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        Assert.assertEquals(53, c.getURL().getDefaultPort());
        Assert.assertEquals("localhost", c.getURL().getHost());
        Assert.assertEquals("/example.com", c.getURL().getPath());
        Assert.assertEquals(DnsRequisitionUrlConnection.PROTOCOL, c.getURL().getProtocol());
        Assert.assertNull(c.getURL().getUserInfo());
    }

    @Test
    public void dwoToString() throws MalformedURLException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        Assert.assertEquals(TEST_URL, c.getURL().toString());
    }

}
