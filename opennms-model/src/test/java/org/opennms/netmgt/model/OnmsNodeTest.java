/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.events.api.EventForwarder;

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

    @Test
    public void testNMS16347() {
        final OnmsNode node = new OnmsNode();
        node.setId(1);
        node.addIpInterface(createInterface("10.33.1.100", PrimaryType.PRIMARY));
        node.addIpInterface(createInterface("10.33.2.100", PrimaryType.SECONDARY));
        node.addIpInterface(createInterface("10.33.3.100", PrimaryType.SECONDARY));
        node.addIpInterface(createInterface("10.33.4.100", PrimaryType.SECONDARY));
        final OnmsNode scannedNode = new OnmsNode();
        scannedNode.addIpInterface(createInterface("10.33.5.100", PrimaryType.PRIMARY));
        scannedNode.addIpInterface(createInterface("10.33.2.100", PrimaryType.SECONDARY));
        scannedNode.addIpInterface(createInterface("10.33.3.100", PrimaryType.SECONDARY));
        scannedNode.addIpInterface(createInterface("10.33.6.100", PrimaryType.SECONDARY));
        final EventForwarder eventForwarder = mock(EventForwarder.class);
        node.mergeIpInterfaces(scannedNode, eventForwarder, true);
        final Map<String, Boolean> interfaceMap = node.getIpInterfaces().stream().collect(Collectors.toMap(OnmsIpInterface::getIpAddressAsString, OnmsIpInterface::isPrimary));
        assertEquals(4, interfaceMap.size());
        assertTrue(interfaceMap.containsKey("10.33.2.100"));
        assertTrue(interfaceMap.containsKey("10.33.3.100"));
        assertTrue(interfaceMap.containsKey("10.33.5.100"));
        assertTrue(interfaceMap.containsKey("10.33.6.100"));
        assertFalse(interfaceMap.get("10.33.2.100"));
        assertFalse(interfaceMap.get("10.33.3.100"));
        assertTrue(interfaceMap.get("10.33.5.100"));
        assertFalse(interfaceMap.get("10.33.6.100"));
    }

    private OnmsIpInterface createInterface(final String address, PrimaryType primaryType) {
        final OnmsIpInterface onmsIpInterface = new OnmsIpInterface();
        onmsIpInterface.setIpAddress(InetAddressUtils.addr(address));
        onmsIpInterface.setIsSnmpPrimary(primaryType);
        return onmsIpInterface;
    }
}
