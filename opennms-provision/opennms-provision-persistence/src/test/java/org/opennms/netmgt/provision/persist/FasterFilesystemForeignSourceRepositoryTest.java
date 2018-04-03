/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.Duration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/org/opennms/netmgt/provision/persist/emptyContext.xml")
public class FasterFilesystemForeignSourceRepositoryTest {

    @Test
    public void testActiveForeignSourceNames() throws Exception {
        FileSystemBuilder bldr = new FileSystemBuilder("target", "testActiveForeignSourceNames");

        File fsDir = bldr.dir("foreignSource").file("test.xml").file("noreq.xml").pop();
        File reqDir = bldr.dir("requisitions").file("test.xml").file("pending.xml").pop();

        FasterFilesystemForeignSourceRepository repo = repo(fsDir, reqDir);

        assertEquals(set("test", "pending", "noreq"), repo.getActiveForeignSourceNames());
    }

    @Test
    public void testGetForeignSourceCount() throws Exception {
        FileSystemBuilder bldr = new FileSystemBuilder("target", "testGetForeignSourceCount");

        File fsDir = bldr.dir("foreignSource").file("test.xml", fs("test")).file("noreq.xml", fs("noreq")).file("another.xml", fs("another")).pop();
        File reqDir = bldr.dir("requisitions").file("test.xml").file("pending.xml").pop();

        FasterFilesystemForeignSourceRepository repo = repo(fsDir, reqDir);

        assertEquals(3, repo.getForeignSourceCount());
    }

    @Test
    public void testGetForeignSources() throws Exception {
        FileSystemBuilder bldr = new FileSystemBuilder("target", "testGetForeignSources");

        File fsDir = bldr.dir("foreignSource").file("test.xml", fs("test")).pop();
        File reqDir = bldr.dir("requisitions").file("test.xml").file("pending.xml").pop();

        FasterFilesystemForeignSourceRepository repo = repo(fsDir, reqDir);

        Set<ForeignSource> foreignSources = repo.getForeignSources();

        assertEquals(1, foreignSources.size());
        ForeignSource testFS = foreignSources.iterator().next();
        assertEquals("test", testFS.getName());
        assertEquals(Duration.standardDays(1), testFS.getScanInterval());
    }

    @Test
    public void testGetForeignSource() throws Exception {

        FileSystemBuilder bldr = new FileSystemBuilder("target", "testGetForeignSource");

        File fsDir = bldr.dir("foreignSource").file("test.xml", fs("test")).file("noreq.xml", fs("noreq")).pop();
        File reqDir = bldr.dir("requisitions").file("test.xml").file("pending.xml").pop();

        FasterFilesystemForeignSourceRepository repo = repo(fsDir, reqDir);

        ForeignSource testFS = repo.getForeignSource("test");

        assertEquals("test", testFS.getName());
        assertEquals(Duration.standardDays(1), testFS.getScanInterval());
    }

    @Test
    public void testGetRequisition() throws Exception {

        FileSystemBuilder bldr = new FileSystemBuilder("target", "testGetForeignSource");

        File fsDir = bldr.dir("foreignSource").file("test.xml", fs("test")).file("noreq.xml", fs("noreq")).pop();
        File reqDir = bldr.dir("requisitions").file("test.xml", req("test")).file("pending.xml", req("pending")).pop();

        FasterFilesystemForeignSourceRepository repo = repo(fsDir, reqDir);

        Requisition testReq = repo.getRequisition("test");

        assertEquals("test", testReq.getForeignSource());
        RequisitionNode node = testReq.getNode("1234");
        assertNotNull(node);
        assertEquals("node1", node.getNodeLabel());
    }

    @Test
    @JUnitHttpServer(port=9162)
    public void testImportHttpSource() throws Exception {

        FileSystemBuilder bldr = new FileSystemBuilder("target", "testGetForeignSource");

        File fsDir = bldr.dir("foreignSource").file("test.xml", fs("test")).file("noreq.xml", fs("noreq")).pop();
        File reqDir = bldr.dir("requisitions").file("test.xml", req("test")).file("pending.xml", req("pending")).pop();

        FasterFilesystemForeignSourceRepository repo = repo(fsDir, reqDir);

        Resource resource = new UrlResource("http://localhost:9162/requisition-test.xml");
        Requisition req = repo.importResourceRequisition(resource);
        assertNotNull(req);
        System.err.println(JaxbUtils.marshal(req));
        assertNotNull(req.getNode("4243"));
        assertNotNull(req.getNode("4244"));
    }

    private static FasterFilesystemForeignSourceRepository repo(File foreignSourceDir, File requisitionDir) throws Exception {
        FasterFilesystemForeignSourceRepository repo = new FasterFilesystemForeignSourceRepository();
        repo.setForeignSourcePath(foreignSourceDir.getAbsolutePath());
        repo.setRequisitionPath(requisitionDir.getAbsolutePath());
        repo.afterPropertiesSet();
        return repo;
    }

    private static Set<String> set(String... items) {
        Set<String> set = new HashSet<>();
        Collections.addAll(set, items);
        return set;
    }

    private static String fs(String name) {
        String template = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
                "<foreign-source date-stamp=\"2012-12-17T13:59:04.299-05:00\" name=\"_TEMPLATE_\" xmlns=\"http://xmlns.opennms.org/xsd/config/foreign-source\">\n" + 
                "    <scan-interval>1d</scan-interval>\n" + 
                "    <detectors>\n" + 
                "        <detector class=\"org.opennms.netmgt.provision.detector.icmp.IcmpDetector\" name=\"ICMP\"/>\n" + 
                "        <detector class=\"org.opennms.netmgt.provision.detector.snmp.SnmpDetector\" name=\"SNMP\"/>\n" + 
                "    </detectors>\n" + 
                "    <policies/>\n" + 
                "</foreign-source>";
		
        return template.replaceAll("_TEMPLATE_", name);
    }

    private static String req(String name) {
        String template = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" + 
                "<model-import last-import=\"2012-12-17T14:00:08.997-05:00\" foreign-source=\"_TEMPLATE_\" date-stamp=\"2012-12-17T14:00:08.757-05:00\" xmlns=\"http://xmlns.opennms.org/xsd/config/model-import\">\n" + 
                "    <node node-label=\"node1\" foreign-id=\"1234\" building=\"_TEMPLATE_\">\n" + 
                "        <interface snmp-primary=\"P\" status=\"1\" ip-addr=\"127.0.0.1\" descr=\"\"/>\n" + 
                "    </node>\n" + 
                "</model-import>\n" + 
                "";

        return template.replaceAll("_TEMPLATE_", name);

    }

}
