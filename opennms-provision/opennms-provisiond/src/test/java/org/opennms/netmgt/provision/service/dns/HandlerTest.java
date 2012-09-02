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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.url.GenericURLFactory;
import org.opennms.netmgt.provision.persist.MockForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.springframework.core.io.UrlResource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

public class HandlerTest {
    
    private static final String DNS_URL = "dns://127.0.0.1:53/localhost";

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @Before
    public void registerFactory() {

        GenericURLFactory.initialize();

    }
    
    @Test
    @Ignore
    public void dwOpenConnectionURL() throws IOException {
        
        URL url = new URL(DNS_URL);
        
        UrlResource resource = new UrlResource(url);

        MockForeignSourceRepository fsr = new MockForeignSourceRepository();
        Requisition r = fsr.importResourceRequisition(resource);
        
        Assert.assertTrue("Number of nodes in Model Import > 1", 1 == r.getNodeCount());
        Assert.assertTrue("NodeLabel isn't localhost", "localhost".equals(r.getNodes().get(0).getNodeLabel()));
        Assert.assertTrue("127.0.0.1".equals(r.getNodes().get(0).getInterfaces().get(0).getIpAddr()));
    }

    @Test
    public void dwParseURL() throws MalformedURLException {
        
        String urlString = "dns://localhost:53/opennms";

        URL dnsUrl = null;
        
        dnsUrl = new URL(urlString);

        try {
            
            dnsUrl = new URL(urlString);
            assertNotNull(dnsUrl);
            assertEquals(urlString, dnsUrl.toString());
            assertEquals("localhost", dnsUrl.getHost());
            assertEquals(53, dnsUrl.getPort());
            assertEquals(DnsRequisitionUrlConnection.PROTOCOL, dnsUrl.getProtocol());
            
        } catch (MalformedURLException e) {
            fail(e.getLocalizedMessage());
        }
        
    }

}
