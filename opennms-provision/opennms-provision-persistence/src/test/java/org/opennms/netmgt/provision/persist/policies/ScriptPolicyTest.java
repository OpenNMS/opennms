/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist.policies;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockSessionUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.monitoringLocations.OnmsMonitoringLocation;

public class ScriptPolicyTest {

    @Test
    public void testScriptPolicy() throws Exception {
        // create one node
        OnmsNode node1 = new OnmsNode();
        node1.setNodeId("1");
        node1.setLocation(new OnmsMonitoringLocation("Default", ""));

        final OnmsIpInterface iface1_1 = new OnmsIpInterface();
        iface1_1.setIpAddress(InetAddress.getByName("172.16.0.1"));
        iface1_1.setIsSnmpPrimary(PrimaryType.PRIMARY);

        final OnmsIpInterface iface1_2 = new OnmsIpInterface();
        iface1_2.setIpAddress(InetAddress.getByName("172.17.0.1"));
        iface1_2.setIsSnmpPrimary(PrimaryType.SECONDARY);

        final OnmsIpInterface iface1_3 = new OnmsIpInterface();
        iface1_3.setIpAddress(InetAddress.getByName("192.168.0.10"));
        iface1_3.setIsSnmpPrimary(PrimaryType.NOT_ELIGIBLE);

        node1.addIpInterface(iface1_1);
        node1.addIpInterface(iface1_2);
        node1.addIpInterface(iface1_3);

        node1.setForeignSource("nodes");
        node1.setForeignId("1");
        node1.setLabel("Node-1-Foo");

        // create another one
        OnmsNode node2 = new OnmsNode();
        node2.setNodeId("2");
        node2.setLocation(new OnmsMonitoringLocation("Default", ""));

        final OnmsIpInterface iface2_1 = new OnmsIpInterface();
        iface2_1.setIpAddress(InetAddress.getByName("172.16.0.1"));
        iface2_1.setIsSnmpPrimary(PrimaryType.PRIMARY);

        final OnmsIpInterface iface2_2 = new OnmsIpInterface();
        iface2_2.setIpAddress(InetAddress.getByName("172.17.0.1"));
        iface2_2.setIsSnmpPrimary(PrimaryType.SECONDARY);

        final OnmsIpInterface iface2_3 = new OnmsIpInterface();
        iface2_3.setIpAddress(InetAddress.getByName("192.168.0.20"));
        iface2_3.setIsSnmpPrimary(PrimaryType.NOT_ELIGIBLE);

        node2.addIpInterface(iface2_1);
        node2.addIpInterface(iface2_2);
        node2.addIpInterface(iface2_3);

        node2.setForeignSource("nodes");
        node2.setForeignId("2");
        node2.setLabel("Node-2-Bar");

        // create policy that matches only the first node
        final ScriptPolicy p = new ScriptPolicy(Paths.get("src", "test", "resources").toAbsolutePath());
        p.setLabel("~.*Foo$");
        p.setMatchBehavior("ALL_PARAMETERS");
        p.setScript("policy.groovy");
        NodeDao mockNodeDao = Mockito.mock(NodeDao.class);
        when(mockNodeDao.get(Mockito.eq(1))).thenReturn(node1);
        when(mockNodeDao.get(Mockito.eq(2))).thenReturn(node2);
        p.setNodeDao(mockNodeDao);
        p.setSessionUtils(new MockSessionUtils());

        node1 = p.apply(node1, Collections.emptyMap());

        // check that the data of the first node is altered
        assertEquals(PrimaryType.NOT_ELIGIBLE, node1.getIpInterfaceByIpAddress("172.16.0.1").getIsSnmpPrimary());
        assertEquals(PrimaryType.NOT_ELIGIBLE, node1.getIpInterfaceByIpAddress("172.17.0.1").getIsSnmpPrimary());
        assertEquals(PrimaryType.PRIMARY, node1.getIpInterfaceByIpAddress("192.168.0.10").getIsSnmpPrimary());
        assertEquals("custom-location", node1.getLocation().getLocationName());

        node2 = p.apply(node2, Collections.emptyMap());

        // check that the second node is not modified
        assertEquals(PrimaryType.PRIMARY, node2.getIpInterfaceByIpAddress("172.16.0.1").getIsSnmpPrimary());
        assertEquals(PrimaryType.SECONDARY, node2.getIpInterfaceByIpAddress("172.17.0.1").getIsSnmpPrimary());
        assertEquals(PrimaryType.NOT_ELIGIBLE, node2.getIpInterfaceByIpAddress("192.168.0.20").getIsSnmpPrimary());
        assertEquals("Default", node2.getLocation().getLocationName());
    }

    @Test
    public void testScriptCompilation() throws Exception {
        // create one node
        OnmsNode node1 = new OnmsNode();
        node1.setNodeId("1");
        node1.setLocation(new OnmsMonitoringLocation("Default", ""));

        final OnmsIpInterface iface1_1 = new OnmsIpInterface();
        iface1_1.setIpAddress(InetAddress.getByName("172.16.0.1"));
        iface1_1.setIsSnmpPrimary(PrimaryType.PRIMARY);

        final OnmsIpInterface iface1_2 = new OnmsIpInterface();
        iface1_2.setIpAddress(InetAddress.getByName("172.17.0.1"));
        iface1_2.setIsSnmpPrimary(PrimaryType.SECONDARY);

        final OnmsIpInterface iface1_3 = new OnmsIpInterface();
        iface1_3.setIpAddress(InetAddress.getByName("192.168.0.10"));
        iface1_3.setIsSnmpPrimary(PrimaryType.NOT_ELIGIBLE);

        node1.addIpInterface(iface1_1);
        node1.addIpInterface(iface1_2);
        node1.addIpInterface(iface1_3);

        node1.setForeignSource("nodes");
        node1.setForeignId("1");
        node1.setLabel("Node-1-Foo");

        // create temporary file...
        final File scriptFile = File.createTempFile("foobar", ".groovy");

        // ...and attach it to the ScriptPolicy
        final ScriptPolicy p = new ScriptPolicy(Paths.get("src", "test", "resources").toAbsolutePath());
        p.setLabel("~.*");
        p.setMatchBehavior("ALL_PARAMETERS");
        p.setScript(scriptFile.getAbsolutePath());
        NodeDao mockNodeDao = Mockito.mock(NodeDao.class);
        when(mockNodeDao.get(Mockito.eq(1))).thenReturn(node1);
        p.setNodeDao(mockNodeDao);
        p.setSessionUtils(new MockSessionUtils());

        // create script file's content and modify lastModified
        createScriptFile(scriptFile, 1, false);

        // first run, it should compile and results in a "Test #1" node label
        node1 = p.apply(node1, Collections.emptyMap());
        assertEquals("Test #1", node1.getLabel());
        Thread.sleep(500);

        // update script file's content and leave lastModified unchanged
        createScriptFile(scriptFile, 2, true);

        // second run with an unmodified lastModified, it should not compile and should still results in a "Test #1" node label
        node1 = p.apply(node1, Collections.emptyMap());
        assertEquals("Test #1", node1.getLabel());
        Thread.sleep(500);

        // update script file's content and modify lastModified
        createScriptFile(scriptFile, 3, false);

        // third run with a modified lastModified, it should compile and result in a "Test #3" node label
        node1 = p.apply(node1, Collections.emptyMap());
        assertEquals("Test #3", node1.getLabel());
    }

    private void createScriptFile(final File file, final int run, final boolean preserveLastModified) throws IOException {
        final long lastModified = file.lastModified();
        try (final PrintWriter printWriter = new PrintWriter(new FileWriter(file))) {
            printWriter.println("node.setLabel(\"Test #" + run + "\")");
            printWriter.println("return node");
        }
        if (preserveLastModified) {
            file.setLastModified(lastModified);
        }
    }
}
