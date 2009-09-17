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
    @Ignore
    public void dwUrlAsResource() throws IOException, MarshalException, ValidationException, JAXBException {
        Resource r = new UrlResource(TEST_URL);
        
        Assert.assertEquals(TEST_URL, r.getURL().toString());
        
        InputStream s = r.getInputStream();
        
        Requisition req = null;
        Assert.assertNotNull(r);
        InputStream resourceStream = r.getInputStream();
        JAXBContext context = JAXBContext.newInstance(Requisition.class);
        Unmarshaller um = context.createUnmarshaller();
        um.setSchema(null);
        req = (Requisition) um.unmarshal(resourceStream);
        
        Assert.assertEquals(1, req.getNodeCount());
        s.close();
    }
    

    @Test
    public void dwDnsRequisitionUrlConnection() throws MalformedURLException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        Assert.assertNotNull(c);
    }

    /**
     * Just test that connection doesn't get implementation without fixing this test
     * @throws IOException
     */
    @Test
    public void dwConnect() throws IOException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        c.connect();
    }

    @Test
    @Ignore
    public void dwGetInputStream() throws IOException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        InputStream s = c.getInputStream();
        Assert.assertNotNull(s);
        s.close();
    }

    @Test
    public void dwGetURL() throws MalformedURLException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        Assert.assertEquals(53, c.getURL().getDefaultPort());
        Assert.assertEquals("localhost", c.getURL().getHost());
        Assert.assertEquals("/localhost", c.getURL().getPath());
        Assert.assertEquals(DnsRequisitionUrlConnection.PROTOCOL, c.getURL().getProtocol());
        Assert.assertNull(c.getURL().getUserInfo());
    }

    @Test
    public void dwToString() throws MalformedURLException {
        URLConnection c = new DnsRequisitionUrlConnection(new URL(TEST_URL));
        Assert.assertEquals(TEST_URL, c.getURL().toString());
    }

}
