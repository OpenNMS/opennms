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
public class ReverseDnsRecordTest {

	private NetworkBuilder nb;
    private String hostname = "test.opennms.org";
    private String ifname = "onms001";
    private String ip = "192.168.1.10";

    @Before
    public void setUp() throws Exception {
        nb = new NetworkBuilder();
        nb.addNode(hostname).setForeignSource("dns").setForeignId("1");
        nb.addSnmpInterface(100).setIfName(ifname).addIpInterface(ip);
    }

    
    @Test
    public void testDnsRecondDefaultLevel() throws Exception {
    	ReverseDnsRecord dnsRecord = new ReverseDnsRecord(nb.getCurrentNode().getIpInterfaceByIpAddress(ip), -10);
    	assertEquals(ifname+"-"+hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("1.168.192.in-addr.arpa.", dnsRecord.getZone());
    }

    @Test
    public void testDnsRecondZeroLevel() throws Exception {
    	ReverseDnsRecord dnsRecord = new ReverseDnsRecord(nb.getCurrentNode().getIpInterfaceByIpAddress(ip), 0);
    	assertEquals(ifname+"-"+hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("1.168.192.in-addr.arpa.", dnsRecord.getZone());
    }
    

    @Test
    public void testDnsRecondFirstLevel() throws Exception {
    	ReverseDnsRecord dnsRecord = new ReverseDnsRecord(nb.getCurrentNode().getIpInterfaceByIpAddress(ip), 1);
    	assertEquals(ifname+"-"+hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("192.in-addr.arpa.", dnsRecord.getZone());    	
    }

    @Test
    public void testDnsRecondSecondLevel() throws Exception {
    	ReverseDnsRecord dnsRecord = new ReverseDnsRecord(nb.getCurrentNode().getIpInterfaceByIpAddress(ip), 2);
    	assertEquals(ifname+"-"+hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("168.192.in-addr.arpa.", dnsRecord.getZone());    	
    }
    
    @Test
    public void testDnsRecondThirdLevel() throws Exception {
    	ReverseDnsRecord dnsRecord = new ReverseDnsRecord(nb.getCurrentNode().getIpInterfaceByIpAddress(ip), 3);
    	assertEquals(ifname+"-"+hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("1.168.192.in-addr.arpa.", dnsRecord.getZone());
    }

    @Test
    public void testDnsRecondFourthLevel() throws Exception {
    	ReverseDnsRecord dnsRecord = new ReverseDnsRecord(nb.getCurrentNode().getIpInterfaceByIpAddress(ip), 4);
    	assertEquals(ifname+"-"+hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("1.168.192.in-addr.arpa.", dnsRecord.getZone());
    }

    @Test
    public void testDnsRecondFifthLevel() throws Exception {
    	ReverseDnsRecord dnsRecord = new ReverseDnsRecord(nb.getCurrentNode().getIpInterfaceByIpAddress(ip), 5);
    	assertEquals(ifname+"-"+hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("1.168.192.in-addr.arpa.", dnsRecord.getZone());
    }

    @Test
    public void testDnsRecondUpperLevel() throws Exception {
    	ReverseDnsRecord dnsRecord = new ReverseDnsRecord(nb.getCurrentNode().getIpInterfaceByIpAddress(ip), 90);
    	assertEquals(ifname+"-"+hostname+".", dnsRecord.getHostname());
    	assertEquals(ip, dnsRecord.getIp().getHostAddress());
    	assertEquals("1.168.192.in-addr.arpa.", dnsRecord.getZone());
    }
    
}
