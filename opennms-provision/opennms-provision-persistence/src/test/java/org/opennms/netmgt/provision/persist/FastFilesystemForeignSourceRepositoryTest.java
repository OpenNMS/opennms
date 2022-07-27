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

package org.opennms.netmgt.provision.persist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;

public class FastFilesystemForeignSourceRepositoryTest extends ForeignSourceRepositoryTestCase {
    private String m_defaultForeignSourceName;
    private File m_requisitionDirectory;

    @Autowired
    @Qualifier("fastFilePending")
    private ForeignSourceRepository m_foreignSourceRepository;

    @Before
    public void setUp() throws Exception {
        m_requisitionDirectory = new File("target/opennms-home/etc/imports/pending");
        m_requisitionDirectory.mkdirs();

        m_defaultForeignSourceName = "imported:";
        m_foreignSourceRepository.clear();
        m_foreignSourceRepository.flush();

        FileUtils.copyFile(new File("src/test/resources/requisition-test.xml"), getRequisitionFile());
    }

    @After
    public void tearDown() throws Exception {
       FileUtils.deleteDirectory(m_requisitionDirectory); 
    }

    private File getRequisitionFile() {
        return new File(m_requisitionDirectory, m_defaultForeignSourceName + ".xml");
    }

    private Requisition createRequisition() throws Exception {
        FileSystemResource resource = new FileSystemResource(getRequisitionFile());
        Requisition r = m_foreignSourceRepository.importResourceRequisition(resource);
        m_foreignSourceRepository.save(r);
        m_foreignSourceRepository.flush();
        Thread.sleep(2000); // Give enough time to watcher's thread to cache the requisition
        return r;
    }

    private ForeignSource createForeignSource(String foreignSource) throws Exception {
        ForeignSource fs = new ForeignSource(foreignSource);
        fs.addDetector(new PluginConfig("HTTP", "org.opennms.netmgt.provision.detector.simple.HttpDetector"));
        fs.addPolicy(new PluginConfig("all-ipinterfaces", "org.opennms.netmgt.provision.persist.policies.InclusiveInterfacePolicy"));
        m_foreignSourceRepository.save(fs);
        m_foreignSourceRepository.flush();
        Thread.sleep(2000); // Give enough time to watcher's thread to cache the requisition
        return fs;
    }

    private void modifyRequisition() throws Exception {
        Requisition r = JaxbUtils.unmarshal(Requisition.class, getRequisitionFile());
        Assert.assertNotNull(r);
        r.getNode("4243").setNodeLabel("apknd_2"); // Modify existing node
        RequisitionNode n = new RequisitionNode();
        n.setForeignId("R2D2");
        n.setNodeLabel("utility-robot");
        n.getCategories().add(new RequisitionCategory("StarWars"));
        n.getCategories().add(new RequisitionCategory("Rebels"));
        r.getNodes().add(n); // Add a new node
        JaxbUtils.marshal(r, new FileWriter(getRequisitionFile()));
        Thread.sleep(2000); // Give enough time to watcher's thread to cache the requisition
    }

    private void deleteRequisition() throws Exception {
        Assert.assertTrue(getRequisitionFile().delete());
        Thread.sleep(2000); // Give enough time to watcher's thread to cache the requisition
    }

    @Test
    public void testRequisition() throws Exception {
        createRequisition();
        Requisition r = m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName);
        TestVisitor v = new TestVisitor();
        r.visit(v);
        assertEquals("number of nodes visited", 2, v.getNodeReqs().size());
        assertEquals("node name matches", "apknd", v.getNodeReqs().get(0).getNodeLabel());

        // Modifying the requisition outside the repository and verifying that the repository cache was updated.
        modifyRequisition();
        r = m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName);
        v = new TestVisitor();
        r.visit(v);
        assertEquals("number of nodes visited", 3, v.getNodeReqs().size());
        assertEquals("node name matches", "apknd_2", v.getNodeReqs().get(0).getNodeLabel());
        assertEquals("node name matches", "wan0", v.getNodeReqs().get(1).getNodeLabel());
        assertEquals("node name matches", "utility-robot", v.getNodeReqs().get(2).getNodeLabel());

        // Removing the requisition outside the repository and verifying that the repository cache was updated.
        deleteRequisition();
        r = m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName);
        Assert.assertNull(r);
    }

    @Test
    public void testForeignSource() throws Exception {
        createRequisition();
        ForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        Set<ForeignSource> foreignSources = m_foreignSourceRepository.getForeignSources();
        assertEquals("number of foreign sources must be 1", 1, foreignSources.size());
        assertEquals("getAll() foreign source name must match", m_defaultForeignSourceName, foreignSources.iterator().next().getName());
        
        // check that the foreign source matches
        final ForeignSource newForeignSource = m_foreignSourceRepository.getForeignSource(m_defaultForeignSourceName);
        
        assertEquals(foreignSource.getName(), newForeignSource.getName());
        assertEquals(foreignSource.getDateStampAsDate(), newForeignSource.getDateStampAsDate());
        assertEquals(foreignSource.getDetectorNames(), newForeignSource.getDetectorNames());
        assertEquals(foreignSource.getScanInterval(), newForeignSource.getScanInterval());
    }

    @Test
    public void testGetRequisition() throws Exception {
        Requisition requisition = createRequisition();
        ForeignSource foreignSource = createForeignSource(m_defaultForeignSourceName);
        assertRequisitionsMatch("requisitions must match", m_foreignSourceRepository.getRequisition(m_defaultForeignSourceName), m_foreignSourceRepository.getRequisition(foreignSource));
        assertRequisitionsMatch("foreign source is the expected one", requisition, m_foreignSourceRepository.getRequisition(foreignSource));
    }

    @Test
    public void testDefaultForeignSource() throws Exception {
        createRequisition();
        List<String> detectorList = Arrays.asList(new String[]{ "DNS", "FTP", "HTTP", "HTTPS", "ICMP", "IMAP", "LDAP", "NRPE", "POP3", "SMTP", "SNMP", "SSH" });
        String uuid = UUID.randomUUID().toString();
        ForeignSource defaultForeignSource = m_foreignSourceRepository.getForeignSource(uuid);
        assertEquals("name must match requested foreign source repository name", uuid, defaultForeignSource.getName());
        assertEquals("scan-interval must be 1 day", 86400000, defaultForeignSource.getScanInterval().getMillis());
        assertEquals("foreign source must have no default policies", 0, defaultForeignSource.getPolicies().size());
        List<String> fsNames = new ArrayList<>();
        for (PluginConfig config : defaultForeignSource.getDetectors()) {
            fsNames.add(config.getName());
        }
        assertEquals("detector list must match expected defaults", detectorList, fsNames);
        assertTrue("foreign source must be tagged as default", defaultForeignSource.isDefault());
    }
}
