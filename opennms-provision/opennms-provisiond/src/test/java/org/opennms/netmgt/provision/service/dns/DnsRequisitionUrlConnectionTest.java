/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 10, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision.service.dns;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import junit.framework.Assert;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * This class tests the new "dns" protocol handling created for the Provisioner.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class DnsRequisitionUrlConnectionTest {

    private static final String TEST_URL = "dns://localhost/localhost";

    
    @Before
    public void registerFactory() {
        
        try {
            new URL(TEST_URL);
        } catch (MalformedURLException e) {
            URL.setURLStreamHandlerFactory(new DnsUrlFactory());
        }
        
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
    @Ignore
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
        
        Assert.assertEquals(1, req.getNodeCount());
        resourceStream.close();
    }
    
    @Test
    @Ignore
    public void dwoUrlAsResourceUsingMatchingExpression() throws IOException, MarshalException, ValidationException, JAXBException {
        String urlString = "dns://localhost/localhost/?expression=[Ll]ocal.*";
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
    @Ignore
    public void dwoUrlAsResourceUsingNonMatchingExpression() throws IOException, MarshalException, ValidationException, JAXBException {
        String urlString = "dns://localhost/localhost/?expression=Local.*";
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
    @Ignore
    public void dwoUrlAsResourceUsingComplexMatchingExpression() throws IOException, MarshalException, ValidationException, JAXBException {
        String urlString = "dns://localhost/localhost/?expression=(%3Fi)^LOCALH.*";
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
    public void dwoConnect() throws IOException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        c.connect();
    }

    @Test
    @Ignore
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
        Assert.assertEquals("/localhost", c.getURL().getPath());
        Assert.assertEquals(DnsRequisitionUrlConnection.PROTOCOL, c.getURL().getProtocol());
        Assert.assertNull(c.getURL().getUserInfo());
    }

    @Test
    public void dwoToString() throws MalformedURLException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        Assert.assertEquals(TEST_URL, c.getURL().toString());
    }

}
