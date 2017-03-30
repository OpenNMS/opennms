/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.model.NetworkBuilder;

import static org.junit.Assert.assertEquals;
public class DnsRecordTest {

	private NetworkBuilder nb;
    private String hostname = "www.test.opennms.org";
    private String ip = "192.168.1.10";

    @Before
    public void setUp() throws Exception {
        nb = new NetworkBuilder();
        nb.addNode(hostname).setForeignSource("dns").setForeignId("1");
        nb.addInterface(ip);
    }

    
    @Test
    public void testDnsRecondDefaultLevel() throws Exception {
    	DnsRecord dnsRecord = new DnsRecord(nb.getCurrentNode(), 0);
    	assertEquals(hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("test.opennms.org.", dnsRecord.getZone());
    	
    }
    
    @Test
    public void testDnsRecondFirstLevel() throws Exception {
    	DnsRecord dnsRecord = new DnsRecord(nb.getCurrentNode(), 1);
    	assertEquals(hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("org.", dnsRecord.getZone());
    	
    }

    @Test
    public void testDnsRecondSecondLevel() throws Exception {
    	DnsRecord dnsRecord = new DnsRecord(nb.getCurrentNode(), 2);
    	assertEquals(hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("opennms.org.", dnsRecord.getZone());
    	
    }
    
    @Test
    public void testDnsRecondThirdLevel() throws Exception {
    	DnsRecord dnsRecord = new DnsRecord(nb.getCurrentNode(), 3);
    	assertEquals(hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("test.opennms.org.", dnsRecord.getZone());    	
    }

    @Test
    public void testDnsRecondFourthLevel() throws Exception {
    	DnsRecord dnsRecord = new DnsRecord(nb.getCurrentNode(), 4);
    	assertEquals(hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("test.opennms.org.", dnsRecord.getZone());    	
    }

    @Test
    public void testDnsRecondFifthLevel() throws Exception {
    	DnsRecord dnsRecord = new DnsRecord(nb.getCurrentNode(), 5);
    	assertEquals(hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("test.opennms.org.", dnsRecord.getZone());    	
    }

    @Test
    public void testDnsRecondUpperLevel() throws Exception {
    	DnsRecord dnsRecord = new DnsRecord(nb.getCurrentNode(), 90);
    	assertEquals(hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("test.opennms.org.", dnsRecord.getZone());    	
    }

    
}
