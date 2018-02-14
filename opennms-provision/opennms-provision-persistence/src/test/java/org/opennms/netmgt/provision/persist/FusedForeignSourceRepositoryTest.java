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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.foreignsource.ForeignSource;
import org.opennms.netmgt.provision.persist.foreignsource.PluginConfig;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.netmgt.provision.persist.requisition.RequisitionInterface;
import org.opennms.netmgt.provision.persist.requisition.RequisitionNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

public class FusedForeignSourceRepositoryTest extends ForeignSourceRepositoryTestCase {
    @Autowired
    @Qualifier("filePending")  // TODO fused doesn't work with fastFilePending
    private ForeignSourceRepository m_pending;
    
    @Autowired
    @Qualifier("fileDeployed") // TODO fused doesn't work with fastFileDeployed
    private ForeignSourceRepository m_active;
    
    @Autowired
    @Qualifier("fused")
    private ForeignSourceRepository m_repository;

    @Before
    public void setUp() throws IOException {
        final Properties props = new Properties();
        props.put("log4j.logger.org.opennms.core.xml.SimpleNamespaceFilter", "WARN");
        props.put("log4j.logger.org.opennms.netmgt.provision.persist.RequisitionFileUtils", "TRACE");
        MockLogAppender.setupLogging(props);

        System.err.println("setUp()");
        m_pending.clear();
        m_active.clear();
        
        FileUtils.deleteDirectory(new File("target/opennms-home/etc/imports/pending"));

        m_pending.flush();
        m_active.flush();
    }

    @After
    public final void tearDown() {
        /* 
         * since we share the filesystem with other tests, best
         * to make sure it's totally clean here.
         */
        for (ForeignSource fs : m_pending.getForeignSources()) {
            m_pending.delete(fs);
        }
        for (ForeignSource fs : m_active.getForeignSources()) {
            m_active.delete(fs);
        }
        for (Requisition r : m_pending.getRequisitions()) {
            m_pending.delete(r);
        }
        for (Requisition r : m_active.getRequisitions()) {
            m_active.delete(r);
        }
        m_pending.flush();
        m_active.flush();
    }

    @Test
    public void simpleSnapshotTest() throws URISyntaxException {
        Requisition pendingReq = new Requisition("test");
        pendingReq.putNode(createNode("1"));
        m_pending.save(pendingReq);
        m_pending.flush();
        pendingReq = m_pending.getRequisition(pendingReq.getForeignSource());
        final File pendingSnapshot = RequisitionFileUtils.createSnapshot(m_pending, pendingReq.getForeignSource(), pendingReq.getDate());

        m_repository.importResourceRequisition(new FileSystemResource(pendingSnapshot));

        assertFalse(pendingSnapshot.exists());
        final URL pendingUrl = m_pending.getRequisitionURL(pendingReq.getForeignSource());
        final File pendingFile = new File(pendingUrl.toURI());
        assertFalse(pendingFile.exists());
    }

    @Test
    public void multipleSnapshotTest() throws URISyntaxException, InterruptedException {
        Requisition pendingReq = new Requisition("test");
        pendingReq.putNode(createNode("1"));
        m_pending.save(pendingReq);
        m_pending.flush();
        final String foreignSource = pendingReq.getForeignSource();
        pendingReq = m_pending.getRequisition(foreignSource);
        final File pendingSnapshotA = RequisitionFileUtils.createSnapshot(m_pending, foreignSource, pendingReq.getDate());

        // Now, start a new pending update after the original snapshot is "in progress"
        pendingReq.updateDateStamp();
        m_pending.save(pendingReq);
        m_pending.flush();

        final File pendingSnapshotB = RequisitionFileUtils.createSnapshot(m_pending, foreignSource, pendingReq.getDate());

        // "import" the A snapshot
        m_repository.importResourceRequisition(new FileSystemResource(pendingSnapshotA));

        assertFalse(pendingSnapshotA.exists());
        assertTrue(pendingSnapshotB.exists());

        // since there's still a newer snapshot in-progress, it is safe to delete the pending test.xml
        URL pendingUrl = m_pending.getRequisitionURL(foreignSource);
        assertNotNull(pendingUrl);
        assertFalse(new File(pendingUrl.toURI()).exists());

        // then, "import" the B snapshot
        final Requisition bReq = m_repository.importResourceRequisition(new FileSystemResource(pendingSnapshotB));
        
        assertFalse(pendingSnapshotA.exists());
        assertFalse(pendingSnapshotB.exists());

        // now the pending test.xml should be gone
        pendingUrl = m_pending.getRequisitionURL(foreignSource);
        assertNotNull(pendingUrl);
        assertFalse(new File(pendingUrl.toURI()).exists());
        
        // the last (B) pending import should match the deployed
        final Requisition deployedRequisition = m_active.getRequisition(foreignSource);
        assertEquals(deployedRequisition.getDate().getTime(), bReq.getDate().getTime());
    }

    @Test
    public void integrationTest() {
        /*
         * First, the user creates a requisition in the UI, or RESTful
         * interface.
         */
        Requisition pendingReq = new Requisition("test");
        pendingReq.putNode(createNode("1"));
        m_pending.save(pendingReq);
        m_pending.flush();

        /* 
         * Then, the user makes a foreign source configuration to go along
         * with that requisition.
         */
        ForeignSource pendingSource = m_repository.getForeignSource("test");
        assertTrue(pendingSource.isDefault());
        pendingSource.setDetectors(new ArrayList<PluginConfig>());
        m_pending.save(pendingSource);
        m_pending.flush();

        /*
         * Now we got an import event, so we import that requisition file,
         * and save it.  The ForeignSource in the pending repository should
         * match the one in the active one, now.
         */
        Requisition activeReq = m_repository.importResourceRequisition(new UrlResource(m_pending.getRequisitionURL("test")));
        ForeignSource activeSource = m_active.getForeignSource("test");
        // and the foreign source should be the same as the one we made earlier, only this time it's active
        
        assertEquals(activeSource.getName(), pendingSource.getName());
        assertEquals(activeSource.getDetectorNames(), pendingSource.getDetectorNames());
        assertEquals(activeSource.getScanInterval(), pendingSource.getScanInterval());
        assertRequisitionsMatch("active and pending requisitions should match", activeReq, pendingReq);
        
        /*
         * Since it's been officially deployed, the requisition and foreign
         * source should no longer be in the pending repo.
         */
        assertNull("the requisition should be null in the pending repo", m_pending.getRequisition("test"));
        assertTrue("the foreign source should be default since there's no specific in the pending repo", m_pending.getForeignSource("test").isDefault());
    }

    @Test
    public void testSpc674RaceCondition() throws Exception {
        final String foreignSource = "spc674";

        System.err.println("=== create a requisition like the ReST service does, import it immediately ===");
        final Requisition initial = new Requisition(foreignSource);
        initial.putNode(createNode("1"));
        initial.updateDateStamp();
        m_pending.save(initial);
        
        final URL node1Snapshot = createSnapshot(foreignSource);
        Resource resource = new UrlResource(node1Snapshot);
        doImport(resource);

        Thread.sleep(5);
        List<String> files = getImports(foreignSource);
        assertEquals(1, files.size());

        System.err.println("=== create another snapshot, but don't import it yet ===");
        initial.putNode(createNode("2"));
        initial.updateDateStamp();
        m_pending.save(initial);
        final URL node2Snapshot = createSnapshot(foreignSource);

        Thread.sleep(5);
        files = getImports(foreignSource);
        assertEquals(3, files.size());

        System.err.println("=== create yet another snapshot, and don't import it yet ===");
        initial.putNode(createNode("3"));
        initial.updateDateStamp();
        m_pending.save(initial);
        final URL node3Snapshot = createSnapshot(foreignSource);

        Thread.sleep(5);
        files = getImports(foreignSource);
        assertEquals(4, files.size());
        
        System.err.println("=== import of the second file finishes ===");
        doImport(new UrlResource(node2Snapshot));

        Thread.sleep(5);
        files = getImports(foreignSource);
        assertEquals(2, files.size());

        System.err.println("=== fourth node is sent to the ReST interface ===");
        final Requisition currentPending = RequisitionFileUtils.getLatestPendingOrSnapshotRequisition(m_pending, foreignSource);
        assertNotNull(currentPending);
        assertEquals(initial.getDate(), currentPending.getDate());
        currentPending.putNode(createNode("4"));
        currentPending.updateDateStamp();
        m_pending.save(currentPending);
        final URL node4Snapshot = createSnapshot(foreignSource);

        Thread.sleep(5);
        files = getImports(foreignSource);
        assertEquals(4, files.size());

        System.err.println("=== import of the third file finishes ===");
        doImport(new UrlResource(node3Snapshot));

        Thread.sleep(5);
        files = getImports(foreignSource);
        assertEquals(2, files.size());

        System.err.println("=== import of the fourth file finishes ===");
        doImport(new UrlResource(node4Snapshot));

        Thread.sleep(5);
        files = getImports(foreignSource);
        assertEquals(1, files.size());
    }

    protected List<String> getImports(final String foreignSource) {
        final List<String> entries = new ArrayList<>();

        final File importsDirectory = new File("target/opennms-home/etc/imports");
        for (final File file : importsDirectory.listFiles()) {
            if (file.getName().startsWith(foreignSource + ".xml")) {
                entries.add(getSummaryForRequisition(file));
            }
        }
        final File pendingDirectory = new File("target/opennms-home/etc/imports/pending");
        for (final File file : pendingDirectory.listFiles()) {
            if (file.getName().startsWith(foreignSource + ".xml")) {
                entries.add(getSummaryForRequisition(file));
            }
        }
        
        System.err.println("--- BEGIN REQUISITIONS ---");
        Collections.sort(entries);
        for (final String entry : entries) {
            System.err.println(entry);
        }
        System.err.println("--- END REQUISITIONS ---");
        return entries;
    }

    protected String getSummaryForRequisition(final File file) {
        final Requisition requisition = JaxbUtils.unmarshal(Requisition.class, new FileSystemResource(file));
        final StringBuilder sb = new StringBuilder();
        if (requisition.getNodeCount() > 0) {
            sb.append("(");
            final Iterator<RequisitionNode> nodeIterator = requisition.getNodes().iterator();
            while (nodeIterator.hasNext()) {
                sb.append(nodeIterator.next().getNodeLabel());
                if (nodeIterator.hasNext()) sb.append(", ");
            }
            sb.append(")");
        }
        final String requisitionSummary = file.getPath() + sb.toString() + ": " + requisition.getDate().getTime();
        return requisitionSummary;
    }

    protected URL createSnapshot(final String foreignSource) throws MalformedURLException {
        System.err.println("--- creating snapshot for " + foreignSource + " ---");
        Requisition pending = m_pending.getRequisition(foreignSource);
        Requisition deployed = m_active.getRequisition(foreignSource);

        final Date deployedDate = deployed == null? null : deployed.getDate();
        final Date pendingDate  = pending  == null? null : pending.getDate();
        
        if (deployedDate == null) return RequisitionFileUtils.createSnapshot(m_pending, foreignSource, pending.getDate()).toURI().toURL();
        if (pendingDate  == null) return m_active.getRequisitionURL(foreignSource);

        final URL url;
        if (deployedDate.before(pendingDate)) {
            url = RequisitionFileUtils.createSnapshot(m_pending, foreignSource, pendingDate).toURI().toURL();
        } else {
            url = m_active.getRequisitionURL(foreignSource);
        }

        System.err.println("deployedDate = " + deployedDate);
        System.err.println("pendingDate  = " + pendingDate);
        System.err.println("url          = " + url);

        return url;
    }

    protected void doImport(final Resource resource) {
        final Requisition req = m_repository.importResourceRequisition(resource);
        req.updateLastImported();
        m_repository.save(req);
    }

    protected RequisitionNode createNode(final String id) {
        RequisitionNode node = new RequisitionNode();
        node.setForeignId(id);
        node.setNodeLabel("node " + id);
        RequisitionInterface iface = new RequisitionInterface();
        iface.setIpAddr("172.16.0." + id);
        node.putInterface(iface);
        return node;
    }
}
