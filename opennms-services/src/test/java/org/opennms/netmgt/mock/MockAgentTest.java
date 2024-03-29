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
package org.opennms.netmgt.mock;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import junit.framework.TestCase;

import org.opennms.core.test.MockLogAppender;
import org.opennms.test.mock.MockUtil;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.PDUv1;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.Integer32;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.TimeTicks;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.smi.VariableBinding;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.snmp4j.util.DefaultPDUFactory;
import org.snmp4j.util.TableEvent;
import org.snmp4j.util.TableUtils;

/**
 * Represents a MockAgentTest 
 *
 * @author brozow
 */
public class MockAgentTest extends TestCase {

    private MockNetwork m_network;
    private MockProxy m_proxy;

    @Override
    protected void setUp() throws Exception {
        MockUtil.println("------------ Begin Test "+getName()+" --------------------------");
        MockLogAppender.setupLogging();
        
        m_network = new MockNetwork();
        m_network.setCriticalService("ICMP");
        m_network.addNode(1, "Router");
        m_network.addInterface("192.168.1.1");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addInterface("192.168.1.2");
        m_network.addService("ICMP");
        m_network.addService("SMTP");
        m_network.addNode(2, "Server");
        m_network.addInterface("192.168.1.3");
        m_network.addService("ICMP");
        m_network.addService("HTTP");
        m_network.addInterface("192.168.1.2");
        
        MapSubAgent systemGroup = new MapSubAgent("1.3.6.1.2.1.1");
        systemGroup.put("1.0", new OctetString("MockAgent!"));
        systemGroup.put("2.0", new OID("1.3.6.1.4.1.5813.1"));
        systemGroup.put("3.0", new TimeTicks(1234));
        systemGroup.put("4.0", new OctetString("Mr. Personality"));
        systemGroup.put("5.0", new OctetString("mockhost"));
        systemGroup.put("6.0", new OctetString("Wouldn't you like to know"));
        // what happened to 7.0?
        
        MapSubAgent interfaces = new MapSubAgent("1.3.6.1.2.1.2");
        interfaces.put("1.0", new Integer32(2));
        
        try {
            m_proxy = new MockProxy(9161);
        } catch (Throwable t) {
            throw new Exception("Could not start MockProxy on 9161: " + t, t);
        }
        
        MockAgent agent = new MockAgent();
        agent.addSubAgent(systemGroup);
        agent.addSubAgent(interfaces);
        
        m_proxy.addAgent(agent);

    }

    @Override
    protected void tearDown() throws Exception {
        m_proxy.stop();

        /*
         * Future calls to setUp() fail due to the port still being in use
         * if I don't have the sleep:
         *
         * OpenJDK Runtime Environment (build 1.8.0_45-b13) on Amazon Linux AMI release 2015.03
         *
         * I hate it when resources don't get fully deallocated.
         */
	Thread.sleep(5);
        
        MockLogAppender.assertNoWarningsOrGreater();
        MockUtil.println("------------ End Test "+getName()+" --------------------------");

    }

    public void testWalkSystem() throws IOException {
        
        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
        TableUtils walker = new TableUtils(snmp, new DefaultPDUFactory());
        snmp.listen();
        
        Address addr = new UdpAddress(InetAddress.getLocalHost(), 9161);
        //Address addr = new UdpAddress(InetAddressUtils.addr("192.168.0.100"), 161);
        Target target = new CommunityTarget(addr, new OctetString("public"));
        target.setVersion(SnmpConstants.version1);
        target.setTimeout(3000);
        target.setRetries(3);
        
        // Implements snmp4j API
        @SuppressWarnings("rawtypes")
        List results = walker.getTable(target, new OID[] {new OID("1.3.6.1.2.1.1")}, null, null);
        
        assertNotNull(results);
        assertFalse(results.isEmpty());
        
        assertTrue(results.get(results.size()-1) instanceof TableEvent);
        
        TableEvent lastEvent = (TableEvent)results.get(results.size()-1);
        MockUtil.println("Status of lastEvent is "+lastEvent.getStatus());
        assertEquals(TableEvent.STATUS_OK, lastEvent.getStatus());
        
        
        
    }
    
    public void testGetSysName() throws IOException {
        
        Snmp snmp = new Snmp(new DefaultUdpTransportMapping());
        snmp.listen();
        
        Address addr = new UdpAddress(InetAddress.getLocalHost(), 9161);
        //Address addr = new UdpAddress(InetAddressUtils.addr("192.168.0.100"), 161);
        Target target = new CommunityTarget(addr, new OctetString("public"));
        target.setVersion(SnmpConstants.version1);
        target.setTimeout(3000);
        target.setRetries(3);
        
        PDUv1 getRequest = new PDUv1();
        getRequest.add(new VariableBinding(new OID("1.3.6.1.2.1.1.5.0")));
        
        ResponseEvent e = snmp.get(getRequest, target);
        PDU response = e.getResponse();
        
        assertEquals(new OctetString("mockhost"), response.get(0).getVariable());
        
        
    }
    
}
