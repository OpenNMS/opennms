/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.v1;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.core.test.dns.annotations.SRVEntry;
import org.opennms.core.test.dns.annotations.TXTEntry;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.ApplicationDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class DnsLookupRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private DatabasePopulator populator;

    @Autowired
    private ServletContext m_context;

    @Before
    @Override
    public void setUp() throws Throwable {
        super.setUp();
        Assert.assertNotNull(populator);
        Assert.assertNotNull(applicationDao);

        populator.populateDatabase();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        populator.resetDatabase();
    }
    
    @Test
    @JUnitTemporaryDatabase
    @JUnitDNSServer(port=9153, zones={
    		@DNSZone(name="example.com", entries={
    				@DNSEntry(hostname="test", address="192.168.0.1")
    		}, srvs={
    				@SRVEntry(name = "_ldap._tcp", port = 389, priority = 10, target = "ldap1.example.com.", weight = 10),
    				@SRVEntry(name = "_ldap._tcp", port = 389, priority = 20, target = "ldap2.example.com.", weight = 10)
    		}, txts={
    				@TXTEntry(name = "_kerberos", strings = { "krb.example.com" })
    		}
    		)
    })
    public void testMockDnsNameOnly() throws Exception {
        MockHttpServletRequest jsonRequest = createRequest(m_context, GET, "/dnsLookup/127.0.0.1:9153/A/test.example.com");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String jsonA = sendRequest(jsonRequest, 200);

        jsonRequest = createRequest(m_context, GET, "/dnsLookup/127.0.0.1:9153/SRV/_ldap._tcp.example.com");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String jsonSRV = sendRequest(jsonRequest, 200);
        
        jsonRequest = createRequest(m_context, GET, "/dnsLookup/127.0.0.1:9153/TXT/_kerberos.example.com");
        jsonRequest.addHeader("Accept", MediaType.APPLICATION_JSON);
        String jsonTXT = sendRequest(jsonRequest, 200);

        Assert.assertNotNull(jsonA);
        Assert.assertTrue(jsonA.contains("192.168.0.1"));
        
        Assert.assertNotNull(jsonSRV);
        Assert.assertTrue(jsonSRV.contains("ldap1.example.com"));
        Assert.assertTrue(jsonSRV.contains("ldap2.example.com"));        
        
        Assert.assertNotNull(jsonTXT);;
        Assert.assertTrue(jsonTXT.contains("krb.example.com"));
    }
    
}
