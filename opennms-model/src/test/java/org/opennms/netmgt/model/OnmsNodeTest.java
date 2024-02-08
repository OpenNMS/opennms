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
package org.opennms.netmgt.model;

import static org.junit.Assert.assertFalse;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;

/**
 * Basic unit tests for OnmsNode Class
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class OnmsNodeTest {

    /*
     * Test the equals method of the PrimaryType class and therefore the getPrimaryInterface method
     * of the node.
     */
    @Test
    public void testGetPrimaryInterface() {
        
        OnmsNode node = new OnmsNode();
        OnmsIpInterface iface = new OnmsIpInterface();
        PrimaryType primary1 = PrimaryType.PRIMARY;
        iface.setIsSnmpPrimary(primary1);
        node.addIpInterface(iface);
        
        OnmsIpInterface iface2 = new OnmsIpInterface();
        PrimaryType not_eligible1 = PrimaryType.NOT_ELIGIBLE;
        iface2.setIsSnmpPrimary(not_eligible1);
        node.addIpInterface(iface2);
        
        
        Object o = node.getPrimaryInterface();
        
        Assert.assertSame(o, iface);
        
    }

    @Test
    public void testSerialization() {
        OnmsNode node = new OnmsNode();
        node.setLabel("node1");
        node.setType(OnmsNode.NodeType.ACTIVE);
        node.setForeignSource("test");
        node.setForeignId("node1");

        OnmsIpInterface ipInterface = new OnmsIpInterface();
        ipInterface.setNode(node);
        ipInterface.setIpAddress(InetAddressUtils.getInetAddress("192.168.1.1"));
        ipInterface.setIpHostName("192.168.1.1");
        node.addIpInterface(ipInterface);

        OnmsMonitoredService service = new OnmsMonitoredService();
        OnmsServiceType serviceType = new OnmsServiceType();
        serviceType.setName("HTTP");
        service.setServiceType(serviceType);
        ipInterface.addMonitoredService(service);

        String xml = JaxbUtils.marshal(node);
        assertFalse(xml.contains("xmlns.opennms.org")); // passes

        xml = JaxbUtils.marshal(service);
        System.err.println(xml);
        assertFalse(xml.contains("xmlns.opennms.org")); // blows up!
    }
}
