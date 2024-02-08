/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
